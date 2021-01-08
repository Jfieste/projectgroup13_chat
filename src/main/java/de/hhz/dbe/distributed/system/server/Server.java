package de.hhz.dbe.distributed.system.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
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
 * @author Project Group 13 The Server receives message and send via multi cast
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
	private int heartbeat_interval = 0;
	private MessageProcessorIF messageProcessor = new MessageProcessorIF() {

		public void processMessage(BaseMessage msg) {
			switch (msg.getMessageType()) {
			case JOIN_MESSAGE:
				try {
					BaseMessage connectionDetails = new MessageObject(MessageType.SERVER_RESPONSE);
					connectionDetails.setParticipant(participant);
					if (msg.getParticipant() == null && isLeader && !inElection) {
						sender.sendMessage(MessageHandler.getByteFrom(connectionDetails));
					} else if (msg.getParticipant() != null) {
						if (isLeader && !inElection && !msg.getParticipant().getId().equals(id))
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

					// check if the sender is different from current process
					if (!chatMessage.getProcessId().equals(id.toString())) {
						VectorClock piggybackedVectorClock = causalityHandling(chatMessage);
						// merge local and received vector clocks
						vectorClock = VectorClock.mergeClocks(piggybackedVectorClock, vectorClock);
					}
				} catch (UnknownHostException e) {
					logger.info("UnknownHostException in Server Thread: " + e.getMessage());
				} catch (IOException e) {
					logger.info("IOException in Server Thread " + e.getMessage());
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case CONNECTION_LOST:
				logger.info(String.format("The Leader is lost %s", msg.getParticipant().getId()));
				inElection = true;
				leader = null;
				if (msg.getParticipant().compareTo(participant) == 0 && !isConnected) {
					inElection = false;
					isLeader = true;
					serverComponent.clear();
				} else {
					try {
						handShake();
					} catch (Exception e) {
						logger.info("Error during hand shake: " + e.getMessage());
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

		this.multicast = multicast;
		this.id = UUID.randomUUID();
		this.vectorClock = new VectorClock();

		this.serverSocket = new ServerSocket(port);
		receiver = new MulticastReceiver(multicast, multiPort, messageProcessor);
		Properties prop = new LoadProperties().readProperties();
		MAX_HISTORY_SIZE = Integer.parseInt(prop.getProperty("MAX_HISTORY_SIZE"));
		heartbeat_interval = Integer.parseInt(prop.getProperty("HEARTHBEAT_INTERVAL_SEC"));
		participant = new Participant(Inet4Address.getLocalHost().getHostAddress(), serverSocket.getLocalPort(), id,
				true);
		sender = new MulticastSender(this.multicast, multiPort);
		Runnable hearthbeat = new Runnable() {

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
						logger.info(String.format("Something when wrong sending Hearthbeat to the leader with the id: %s",
								leader.getId()));
						hearthbeatMessage = new MessageObject(MessageType.CONNECTION_LOST);
						hearthbeatMessage.setParticipant(participant);
						try {
							sender.sendMessage(MessageHandler.getByteFrom(hearthbeatMessage));
						} catch (IOException e2) {
							logger.error(String.format("send connection lost inside the group when wrong: %s",
									e2.getMessage()));
						} catch (Exception e2) {
							logger.error(String.format("send connection lost inside the group when wrong: %s",
									e2.getMessage()));
						}
					}
				}

			}
		};
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleAtFixedRate(hearthbeat, 10, heartbeat_interval, TimeUnit.SECONDS);
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
				Socket client = serverSocket.accept();
				InputStream in = client.getInputStream();
				ObjectInputStream objectInputStream = new ObjectInputStream(in);
				BaseMessage message = (BaseMessage) objectInputStream.readObject();
				logger.info(String.format("Receive message from type %s", message.getMessageType()));
				Participant part = message.getParticipant();
				switch (message.getMessageType()) {
				case HEARTBEAT:
					logger.info(String.format("Heartbeat from: %s %s", part.getAddr(), part.getPort()));
					break;
				case START_ELECTION:
					if (!part.getId().equals(id)) {
						startElection(part);
						logger.info(String.format("Receive a tcp message and start election:  %s",
								message.getMessageType()));

					}
					break;
				case CHAT_MESSAGE:
					if (isLeader) {
						Payload payload = (Payload) message;
						vectorClock.addOneTo(id.toString());
						int messageId = vectorClock.get(id.toString());
						VectorClock piggybackedVectorClock = (VectorClock) vectorClock.Clone();
						Message chatMessage = new Message(id.toString(), messageId, payload, piggybackedVectorClock);
						logger.info(String.format("forwarding a chat message:  %s", message.getMessageType()));
						// receive message from client and send it
						sender.sendMessage(MessageHandler.getByteFrom(chatMessage));
						history.add((Message) chatMessage);
					}else {
						ObjectOutputStream os = new ObjectOutputStream(client.getOutputStream());
						MessageObject nesMaster = new MessageObject(MessageType.MASTER_ELECTED);
						os.writeObject(nesMaster);						
					}
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
					// Request message
					if (!message.getParticipant().getId().equals(id)) {
						serverComponent.clear();
						MessageObject request = new MessageObject(MessageType.REQUEST_MESSAGES);
						MessageHandler.requestMessage(part.getAddr(), part.getPort(), request);
						Participant participant = message.getNeighbor() == null ? part : message.getNeighbor();
						serverComponent.add(participant);
						startElection(part);
					}
					break;
				case REQUEST_LOST_MESSAGE:
					Request request = (Request) message;
					Message mesgeToResent = findMessageInHistory(request.getMessageId());
					ObjectOutputStream os = new ObjectOutputStream(client.getOutputStream());

					os.writeObject(mesgeToResent);
					logger.info("Received request from " + client.getInetAddress().getHostAddress()
							+ " to retransmit message " + request.getMessageId() + " | found: " + (message != null));

					os.close();
					break;
				case REQUEST_MESSAGES:
					ObjectOutputStream os1 = new ObjectOutputStream(client.getOutputStream());
					MessageObject requested = new MessageObject(MessageType.REQUESTED_MESSAGES);
					requested.setMessgaes(history);
					os1.writeObject(requested);
					os1.close();
					break;
				case REQUESTED_MESSAGES:
					MessageObject reqMsges = (MessageObject) message;
					for (int i = 0; i < reqMsges.getMessgaes().size(); i++) {
						vectorClock = VectorClock.mergeClocks(causalityHandling(reqMsges.getMessgaes().get(i)),
								vectorClock);
					}
					break;
				default:
					break;
				}

				in.close();
				objectInputStream.close();
				client.close();
			} catch (Exception e) {
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
				logger.debug("Join Error " + e1.getMessage());
			}
		}

	}

	private VectorClock causalityHandling(Message chatMessage) throws IOException, ClassNotFoundException {
		String process;
		int seen, sent;
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

					for (int messageId = seen + 1; messageId < sent; messageId++) {
						logger.info("Lost message " + messageId + " from " + process);
						Request req = new Request(messageId);
						MessageHandler.requestMessage(leader.getAddr(), leader.getPort(), req);
					}
					// push first received message
					if (process.equals(chatMessage.getProcessId())) {
						addToHistory(chatMessage);
					}
				}
			}
		}
		return piggybackedVectorClock;
	}
}