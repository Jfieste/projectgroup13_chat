package de.hhz.dbe.distributed.system.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hhz.dbe.distributed.system.client.Participant;
import de.hhz.dbe.distributed.system.message.ConnectionDetails;
import de.hhz.dbe.distributed.system.message.ConnectionMessage;
import de.hhz.dbe.distributed.system.message.ElectionRequestMessage;
import de.hhz.dbe.distributed.system.message.MasterElected;
import de.hhz.dbe.distributed.system.message.Message;
import de.hhz.dbe.distributed.system.message.MessageHandler;
import de.hhz.dbe.distributed.system.message.MessageObject;
import de.hhz.dbe.distributed.system.message.MessageProcessorIF;
import de.hhz.dbe.distributed.system.message.MessageType;
import de.hhz.dbe.distributed.system.multicast.MulticastReceiver;
import de.hhz.dbe.distributed.system.multicast.MulticastSender;

/**
 * @author Project Group 13 The Server receives message and send via multicast
 *         to chat participant
 */
public class Server extends Thread {
	private static Logger logger = LogManager.getLogger(Server.class);
	private volatile boolean running = true;
	private volatile boolean isLeader = true, inElection = false;
	private Vector<MessageObject> history;
	private ServerSocket serverSocket;
	private MulticastSender sender;
	private String multicast;
	private MulticastReceiver receiver;
	private Participant participant;
	private List<Participant> serverComponent = new LinkedList<Participant>();
	private UUID id;
	private MessageProcessorIF messageProcessor = new MessageProcessorIF() {

		public void processMessage(MessageObject msg) {
			switch (msg.getMessageType()) {
			case JOIN_MESSAGE:
				try {
					if (!msg.getParticipant().getId().equals(id)) {
						logger.info("send message" + id);
						ConnectionDetails connectionDetails = new ConnectionDetails(MessageType.SERVER_RESPONSE);
						connectionDetails.setParticipant(participant);
						serverComponent.add(msg.getParticipant());
						if (isLeader)
							if (msg.getParticipant().isServerComponent()) {
								sendTcpMessage(msg.getParticipant(), connectionDetails);
							} else {
								sender.sendMessage(MessageHandler.getByteFrom(connectionDetails));
							}
					}
				} catch (IOException e) {
					logger.error(String.format("Somthing went wrong sending connection details: %s", e));
				} catch (Exception e) {
					logger.error(String.format("Somthing went wrong sending connection details: %s", e));
				}
				break;
			case CONNECTION_DETAIL:
				if (isLeader && !inElection) {
					logger.info(String.format("Receive message from type %s", msg.getMessageType()));
				}
				break;
			case CHAT_MESSAGE:

				break;

			case MASTER_ELECTED:
				if (!msg.getParticipant().getId().equals(id)) {
					logger.info(String.format("Receive new Elected leader from type %s", msg.getParticipant().getId()));
				}
				break;
			default:
				break;
			}
		}
	};

	public Server(int port, String multicast, int multiPort) throws IOException {
		this.history = new Vector<MessageObject>();
		this.serverSocket = new ServerSocket(port);
		this.multicast = multicast;
		this.id = UUID.randomUUID();
		participant = new Participant(Inet4Address.getLocalHost().getHostAddress(), serverSocket.getLocalPort(), id,
				true);
		receiver = new MulticastReceiver(multicast, multiPort, messageProcessor);
		sender = new MulticastSender(this.multicast, multiPort);

	}

	/**
	 * Update history of locally sent messages
	 *
	 * @param message
	 */
	public synchronized void addToHistory(Message message) {
		history.add(message);
	}

	/**
	 * Find message in history from its ID
	 *
	 * @param messageId
	 * @return
	 */
	public synchronized Message findMessageInHistory(int messageId) {
//		for (MessageObject message : history) {
//			if (message.getMessageId() == messageId) {
//				return message;
//			}
//		}
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
		ConnectionMessage conMsg = new ConnectionMessage(MessageType.JOIN_MESSAGE);
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

	public void sendTcpMessage(Participant toParticipant, MessageObject msgObject) throws IOException {
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

	private MessageObject readTCPMessage(Socket client) throws IOException, ClassNotFoundException {
		InputStream in = client.getInputStream();
		ObjectInputStream objectInputStream = new ObjectInputStream(in);
		MessageObject msg = (MessageObject) objectInputStream.readObject();
		logger.info(String.format("Received a tcp message from %s : %s of type %s",
				client.getInetAddress().getHostAddress(), msg.getParticipant().getPort(), msg.getMessageType()));
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
				MessageObject message = readTCPMessage(serverSocket.accept());
				Participant part = message.getParticipant();
				switch (message.getMessageType()) {
				case START_ELECTION:
					if (!part.getId().equals(id)) {
						startElection();
						logger.info(String.format("Receive a tcp message and start election:  %s",
								message.getMessageType()));

					}
					break;
				case CHAT_MESSAGE:
					logger.info(String.format("forwarding a chat message:  %s", message.getMessageType()));
					// receive message from client and send it
					sender.sendMessage(MessageHandler.getByteFrom(message));
					history.add(message);

					break;
				case MASTER_ELECTED:
					if (!part.getId().equals(id)) {
						startElection();
					}
					if (isLeader && !inElection) {
						logger.info(String.format("New Master has been elected with chat message:  %s %s",
								participant.getId(), participant.getPort()));
						MessageObject masterElected = new MasterElected(MessageType.MASTER_ELECTED);
						masterElected.setParticipant(participant);
						sender.sendMessage(MessageHandler.getByteFrom(masterElected));
					}
					break;
				case SERVER_RESPONSE:
					if (!message.getParticipant().getId().equals(id)) {
						logger.info(String.format("Receive message from type %s", message.getMessageType()));
						try {
							MessageObject msgObject = new ElectionRequestMessage(MessageType.START_ELECTION);
							msgObject.setParticipant(participant);
							sendTcpMessage(message.getParticipant(), msgObject);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					break;
				default:
					break;
				}

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

	public void supectedFail(Participant participant) {
		// TODO Auto-generated method stub

	}

	public void candidate(Participant participant) {
		// TODO Auto-generated method stub

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

	protected synchronized void startElection() {
		inElection = true;
		isLeader = false;
//		leader = null;

		MessageObject message = new ElectionRequestMessage(MessageType.MASTER_ELECTED);
		Participant base = participant;
		message.setParticipant(participant);
		if (getKnownNodes().size() > 0) {
			Participant neighbor = getNextNeighbor(base);
			try {
				isLeader = false;
				sendTcpMessage(neighbor, message);
				return;
			} catch (Exception e) {
				logger.debug("Exception sending election message: " + e.getMessage());
				// set failed neighbor as next, try again
				base = neighbor;
			}
		}

		// none of the nodes was available, therefore
		// current node is the leader
//		Participant = node.getAddress();
		this.isLeader = true;
		inElection = false;

	}
}