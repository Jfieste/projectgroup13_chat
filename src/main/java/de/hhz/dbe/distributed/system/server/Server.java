package de.hhz.dbe.distributed.system.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.ObjectInputFilter.Config;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hhz.dbe.distributed.system.client.Participant;
import de.hhz.dbe.distributed.system.message.BaseMessage;
import de.hhz.dbe.distributed.system.message.Message;
import de.hhz.dbe.distributed.system.message.MessageHandler;
import de.hhz.dbe.distributed.system.message.MessageObject;
import de.hhz.dbe.distributed.system.message.MessageProcessorIF;
import de.hhz.dbe.distributed.system.message.MessageType;
import de.hhz.dbe.distributed.system.message.Payload;
import de.hhz.dbe.distributed.system.message.Request;
import de.hhz.dbe.distributed.system.message.VectorClock;
import de.hhz.dbe.distributed.system.multicast.MulticastReceiver;
import de.hhz.dbe.distributed.system.multicast.MulticastSender;
import de.hhz.dbe.distributed.system.utils.LoadProperties;

/**
 * @author Project Group 13 The Server receives message and send via multicast
 *         to chat participant
 */
public class Server extends Thread {
	private static Logger logger = LogManager.getLogger(Server.class);
	private volatile boolean running = true;
	private volatile boolean isLeader = true, inElection = false, isConnected = true;
	private Vector<Message> history;
	private ServerSocket serverSocket;
	private MulticastSender sender;
	private String multicast;
	private MulticastReceiver receiver;
	private Participant participant;
	private Participant leader;
	private List<Participant> serverComponent = new LinkedList<Participant>();
	private UUID id;
	private int MAX_HISTORY_SIZE;
	private VectorClock vectorClock;
	private MessageProcessorIF messageProcessor = new MessageProcessorIF() {

		public void processMessage(BaseMessage msg) {
			switch (msg.getMessageType()) {
			case JOIN_MESSAGE:
				try {
					BaseMessage connectionDetails = new MessageObject(MessageType.SERVER_RESPONSE);
					connectionDetails.setParticipant(participant);
					if (msg.getParticipant() == null && isLeader && !inElection) {
						sender.sendMessage(MessageHandler.getByteFrom(connectionDetails));
					} else if (!msg.getParticipant().getId().equals(id)) {
						logger.info("send message" + id);
						if (isLeader && !inElection)
							if (msg.getParticipant().isServerComponent()) {
								if (!serverComponent.isEmpty()) {
									connectionDetails.setNeighbors(serverComponent.get(0));
									serverComponent.clear();
								}
								serverComponent.add(msg.getParticipant());
								sendTcpMessage(msg.getParticipant(), connectionDetails);
							}
					}

				} catch (IOException e) {
					logger.error(String.format("Somthing went wrong sending connection details: %s", e));
				} catch (Exception e) {
					logger.error(String.format("Somthing went wrong sending connection details: %s", e));
				}
				break;
			case CHAT_MESSAGE:
				try {
					Message chatMessage = (Message) msg;
					String process;
					int seen, sent;
					// check if the sender is different from current process
					if (!chatMessage.getProcessId().equals(id.toString())) {
						// for testing we miss messages with a certain probability
						logger.info("Received message " + chatMessage.toString());

						VectorClock piggybackedVectorClock = chatMessage.getPiggybackedVectorClock();
						Iterator<String> it = piggybackedVectorClock.getProcessIds().iterator();

						// compare local and received vector clock
						while (it.hasNext()) {
							process = it.next();
							if (!process.equals(id.toString())) {
								seen = vectorClock.get(process);
								sent = piggybackedVectorClock.get(process);
								if (seen <= sent - 1) {
									// if vector clocks do not agree, then pull messages
									logger.info(
											"Lost messages " + (seen + 1) + " to " + (sent - 1) + " from " + process);
									for (int messageId = seen + 1; messageId < sent; messageId++) {
										Request req = new Request(messageId);
										sendTcpMessage(leader, req);
									}
									// push first received message
									if (process.equals(chatMessage.getProcessId())) {
										addToHistory(chatMessage);
									}
								}
							}
						}
						// merge local and received vector clocks
						vectorClock = VectorClock.mergeClocks(piggybackedVectorClock, vectorClock);
					}
				} catch (UnknownHostException e) {
					logger.info("UnknownHostException in CausalHandler Thread: " + e.getMessage());
				} catch (IOException e) {
					logger.info("IOException in CausalHandler Thread " + e.getMessage());
				}
				break;

			case MASTER_ELECTED:
				if (!msg.getParticipant().getId().equals(id)) {
					logger.info(String.format("Receive new Elected leader from type %s", msg.getParticipant().getId()));
				}
				break;
			case CONNECTION_LOST:
				logger.info(String.format("The Leader is lost %s", msg.getParticipant().getId()));
				inElection = true;
//				Participant neighbor = serverComponent.get(0);
//				if (leader != null) {
//					if (neighbor.compareTo(leader) == 0) {
//						serverComponent.clear();
//					}
//				}

				leader = null;
				if (msg.getParticipant().compareTo(participant) == 0 && !isConnected) {
					inElection = false;
					isLeader = true;
					serverComponent.clear();
				} else {
					try {
						handShake();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
			default:
				break;
			}
		}
	};

	public Server(int port, String multicast, int multiPort) throws IOException {
		this.history = new Vector<Message>();
		this.serverSocket = new ServerSocket(port);
		this.multicast = multicast;
		this.id = UUID.randomUUID();
		this.vectorClock = new VectorClock();
		Properties prop = new LoadProperties().readProperties();
		MAX_HISTORY_SIZE = Integer.parseInt(prop.getProperty("MAX_HISTORY_SIZE"));
		participant = new Participant(Inet4Address.getLocalHost().getHostAddress(), serverSocket.getLocalPort(), id,
				true);
		receiver = new MulticastReceiver(multicast, multiPort, messageProcessor);
		sender = new MulticastSender(this.multicast, multiPort);
		Runnable harthbeat = new Runnable() {

			public void run() {
				if (leader != null && !isLeader && !inElection) {
					logger.info(String.format("Harthbeat"));
					BaseMessage hearthbeatMessage;
					try {
						hearthbeatMessage = new MessageObject(MessageType.HEARTBEAT);
						hearthbeatMessage.setParticipant(participant);
						sendTcpMessage(leader, hearthbeatMessage);
					} catch (IOException e) {
						inElection = true;
						isConnected = false;
						logger.info(String.format("Somthing when wrong sending Harthbeat to the leader with the id: %s",
								leader.getId()));
						hearthbeatMessage = new MessageObject(MessageType.CONNECTION_LOST);
						hearthbeatMessage.setParticipant(participant);
						try {
							sender.sendMessage(MessageHandler.getByteFrom(hearthbeatMessage));
						} catch (IOException e2) {
							logger.error(String.format("send connection lost inside the group when wrong: %s",
									e2.getMessage()));
							e2.printStackTrace();
						} catch (Exception e2) {
							logger.error(String.format("send connection lost inside the group when wrong: %s",
									e2.getMessage()));
							e2.printStackTrace();
						}
//						if (!serverComponent.isEmpty()) {
//							try {
//								hearthbeatMessage = new MessageObject(MessageType.START_ELECTION);
//								hearthbeatMessage.setParticipant(participant);
//								sendTcpMessage(serverComponent.get(0), hearthbeatMessage);
//							} catch (IOException e1) {
//								logger.info(String.format("Couldn´t find my neighbor with id: %s",
//										serverComponent.get(0).getId()));
//								serverComponent.clear();
//							}
//						}
					}
				}

			}
		};
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleAtFixedRate(harthbeat, 20, 20, TimeUnit.SECONDS);
	}

	/**
	 * Update history of locally sent messages
	 *
	 * @param message
	 */
	public synchronized void addToHistory(Message message) {
		if (history.size() > MAX_HISTORY_SIZE) {
			// if history hits the limit then remove first message
			history.remove(0);
		}
		history.add(message);
	}

	/**
	 * Find message in history from its ID
	 *
	 * @param messageId
	 * @return
	 */
	public synchronized Message findMessageInHistory(int messageId) {
		for (Message message : history) {
			if (message.getMessageId() == messageId) {
				return message;
			}
		}
		return null;
	}

	/**
	 * Discovery service
	 * 
	 * @throws Exception
	 */
	private void handShake() throws Exception {
		logger.info(String.format("Sending a message of type %s to group: %s", MessageType.JOIN_MESSAGE.toString(),
				this.multicast));
		BaseMessage conMsg = new MessageObject(MessageType.JOIN_MESSAGE);
		conMsg.setParticipant(participant);
		sender.sendMessage(MessageHandler.getByteFrom(conMsg));
	}

	/**
	 * Stop service
	 * 
	 * @throws IOException
	 */
	public void logoff() throws IOException {
		running = false;
		if (!serverSocket.isClosed()) {
			serverSocket.close();
		}
	}

	public void sendTcpMessage(Participant toParticipant, BaseMessage msgObject) throws IOException {
		Socket client = new Socket(toParticipant.getAddr(), toParticipant.getPort());
		logger.info(String.format("Sendig a tcp message to:  %s on port: %s", client.getLocalAddress().getHostAddress(),
				toParticipant.getPort()));
		ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
		// write object to Socket
		oos.writeObject(msgObject);
		// close resources
		oos.close();
		client.close();
	}

	private BaseMessage readTCPMessage(Socket client) throws IOException, ClassNotFoundException {
		InputStream in = client.getInputStream();
		ObjectInputStream objectInputStream = new ObjectInputStream(in);
		BaseMessage msg = (BaseMessage) objectInputStream.readObject();
//		logger.info(String.format("Received a tcp message from %s : %s of type %s",
//				getParticipant.get, msg.getParticipant().getPort(), msg.getMessageType()));
		client.close();
		in.close();
		objectInputStream.close();
		return msg;
	}

	/**
	 * Thread that listens for incoming messages
	 */
	public void run() {
		Thread receiverThread = new Thread(receiver);
		receiverThread.start();
		try {
			handShake();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (running) {
			try {
				BaseMessage message = readTCPMessage(serverSocket.accept());
				Participant part = message.getParticipant();
				switch (message.getMessageType()) {
				case HEARTBEAT:
					logger.info(String.format("Heartbeath:  %s",
							message.getMessageType()));
					break;
				case START_ELECTION:
					if (!part.getId().equals(id)) {
						startElection(part);
						logger.info(String.format("Receive a tcp message and start election:  %s",
								message.getMessageType()));

					}
					break;
				case CHAT_MESSAGE:
					Payload payload = (Payload) message;
					vectorClock.addOneTo(id.toString());
					int messageId = vectorClock.get(id.toString());
					VectorClock piggybackedVectorClock = (VectorClock) vectorClock.Clone();
					Message chatMessage = new Message(id.toString(), messageId, payload, piggybackedVectorClock);
					logger.info(String.format("forwarding a chat message:  %s", message.getMessageType()));
					// receive message from client and send it
					sender.sendMessage(MessageHandler.getByteFrom(chatMessage));
					history.add((Message) chatMessage);

					break;
				case MASTER_IN_ELECTION:
					startElection(part);
					break;
				case MASTER_ELECTED:
					this.leader = part;
					this.inElection = false;
					logger.info(String.format("The Current leader address is %s %s", part.getAddr(), part.getPort()));
					Participant neighbor = serverComponent.get(0);
					if (neighbor.compareTo(part) != 0) {
						BaseMessage elec = new MessageObject(MessageType.MASTER_ELECTED);
						elec.setParticipant(part);
						sendTcpMessage(neighbor, elec);
					}

					break;
				case SERVER_RESPONSE:
					if (!message.getParticipant().getId().equals(id)) {
						serverComponent.clear();
						logger.info(String.format("Receive message from type %s", message.getMessageType()));
						Participant participant = message.getNeighbor() == null ? part : message.getNeighbor();
						serverComponent.add(participant);
						startElection(part);
					}
					break;
				case REQUEST_LOST_MESSAGE:
					Request request = (Request) message;
					Message mesgeToResent = findMessageInHistory(request.getMessageId());
					sendTcpMessage(request.getParticipant(), mesgeToResent);
					break;
				case RESPONSE_LOST_MESSAGE:
					Message requested = (Message) message;
					addToHistory(requested);
					logger.info("Pulled message " + requested.toString());
					break;
				default:
					break;
				}

			} catch (Exception e) {
				e.printStackTrace();
				logger.error("IOException in Thread " + e.toString());
			}
		}
	}

	public void leaveGroup() {
		try {
			receiver.leaveGroup();
		} catch (IOException e) {
			logger.error("IOException in Thread " + e.toString());
		}

	}

	public List<Participant> getKnownNodes() {
		return serverComponent;
	}


	public Participant getParticipant() {
		return participant;
	}

	public void setParticipant(Participant participant) {
		this.participant = participant;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	/**
	 * Selects the next neighbor according to their IDs.
	 * 
	 * 
	 * 
	 * @return
	 */
	private Participant getNextNeighbor(Participant base) {

		List<Participant> neighbors = getKnownNodes();
		Collections.sort(neighbors);

		for (Participant n : neighbors) {
			if (n.compareTo(base) > 0) {
				return n;
			}
		}

		return neighbors.get(0);

	}

	protected synchronized void startElection(Participant elected) {
		inElection = true;
		isLeader = false;
		this.leader = null;

		BaseMessage message = new MessageObject(MessageType.MASTER_IN_ELECTION);
		Participant neighbor = getNextNeighbor(participant);
		try {
			if (elected.compareTo(participant) == 1) {
				message.setParticipant(elected);
				sendTcpMessage(neighbor, message);
				return;
			}
			if (elected.compareTo(participant) == 0) {
				inElection = false;
				isLeader = true;
				message = new MessageObject(MessageType.MASTER_ELECTED);
				message.setParticipant(participant);
				sendTcpMessage(neighbor, message);
			} else if (neighbor.compareTo(participant) == 1) {
				message.setParticipant(neighbor);
				sendTcpMessage(neighbor, message);
				return;
			} else if (neighbor.compareTo(participant) == -1) {
				this.isLeader = true;
				message.setParticipant(participant);
				sendTcpMessage(neighbor, message);
				return;
			}
		} catch (Exception e) {
			logger.debug("Error conntacting my neighbor: " + e.getMessage());
			try {
				handShake();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}

}