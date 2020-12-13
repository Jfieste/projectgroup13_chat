package de.hhz.dbe.distributed.system.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hhz.dbe.distributed.system.message.ConnectionMessage;
import de.hhz.dbe.distributed.system.message.Message;
import de.hhz.dbe.distributed.system.message.MessageHandler;
import de.hhz.dbe.distributed.system.message.MessageObject;
import de.hhz.dbe.distributed.system.message.MessageProcessorIF;
import de.hhz.dbe.distributed.system.message.MessageType;
import de.hhz.dbe.distributed.system.multicast.MulticastReceiver;
import de.hhz.dbe.distributed.system.multicast.MulticastSender;
import de.hhz.dbe.distributed.system.utils.LoadProperties;

/**
 * @author Eric Manjone The Server receives message and send via multicast to
 *         chta participant
 */
public class Server extends Thread {
	private static Logger logger = LogManager.getLogger(Server.class);
	private volatile boolean running = true;
	private Vector<Message> history;
	private ServerSocket serverSocket;
	private MulticastSender sender;
	private String multicast;
//	private int multiPort;
	MulticastReceiver receiver;
	MessageProcessorIF messageProcessor = new MessageProcessorIF() {

		public void processMessage(MessageType messageType) {
			logger.info(String.format("Receive message from type %s", messageType));
			switch (messageType) {
			case JOIN_MESSAGE:
				logger.info(String.format("Processing message type: %s", messageType));
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
		sender = new MulticastSender(this.multicast, multiPort);
		receiver = new MulticastReceiver(multicast, multiPort, messageProcessor);
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
		ConnectionMessage conMsg = new ConnectionMessage(MessageType.JOIN_MESSAGE, "message");
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

	/**
	 * Thread that listens for incoming messages
	 */
	public void run() {
		Socket client;
		Message message = null;
		Thread rt = new Thread(receiver);
		rt.start();
		while (running) {
			try {
				handShake();
				client = serverSocket.accept();
				InputStream in = client.getInputStream();
				ObjectInputStream objectInputStream = new ObjectInputStream(in);
				message = (Message) objectInputStream.readObject();
				// receive message from client and send it

				sender.sendMessage(MessageHandler.getByteFrom(message));
				logger.info(String.format("Received message from  %s of type %s",
						client.getInetAddress().getHostAddress(), message.getMessageType()));
				history.add(message);
				client.close();
				in.close();
				objectInputStream.close();
			} catch (Exception e) {
				logger.error("IOException in RepeaterHandler Thread " + e.getMessage());
			}
		}
	}
}