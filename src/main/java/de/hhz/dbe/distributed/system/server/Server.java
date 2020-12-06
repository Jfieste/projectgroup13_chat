package de.hhz.dbe.distributed.system.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hhz.dbe.distributed.system.message.Message;
import de.hhz.dbe.distributed.system.multicast.MulticastSender;
import de.hhz.dbe.distributed.system.testfield.MessageHandler;

public class Server extends Thread {
	private static Logger logger = LogManager.getLogger(Server.class);
	private volatile boolean running = true;
	final static String INET_ADDR = "224.0.0.3";
	private Vector<Message> history;
	private ServerSocket serverSocket;

	public Server(int port) throws IOException {
		this.history = new Vector<Message>();
		this.serverSocket = new ServerSocket(port);
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
	 * Thread that listens for retransmission requests
	 */
	public void run() {
		Socket client;
		Message message = null;

		while (running) {
			try {
				client = serverSocket.accept();
				InputStream in = client.getInputStream();
				ObjectInputStream objectInputStream = new ObjectInputStream(in);
				message = (Message) objectInputStream.readObject();
				// receive message from client and send it
//        message = MessageHandler.getMessageFrom(buffer);
				MulticastSender t =	new MulticastSender(8888, INET_ADDR);
				t.sendMessage(MessageHandler.getByteFrom(message));
				System.out.println(message.getPayload().getAuthor());
				System.out.println("Received message from " + client.getInetAddress().getHostAddress()
						+ " to retransmit message " + message.getMessageId() + " | found: " + (message != null));
				history.add(message);

//        out.write(MessageHandler.getByteFrom(message));
//        out.flush();
				client.close();
				in.close();
//        out.close();
			} catch (Exception e) {
				System.out.println("IOException in RepeaterHandler Thread " + e.getMessage());
			}
		}
	}

	public static void main(String[] args) {
		System.out.println("Running....");
		try {
			new Server(4001).start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}