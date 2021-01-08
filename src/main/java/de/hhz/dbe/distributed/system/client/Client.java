package de.hhz.dbe.distributed.system.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hhz.dbe.distributed.system.message.BaseMessage;
import de.hhz.dbe.distributed.system.message.MessageHandler;
import de.hhz.dbe.distributed.system.message.MessageObject;
import de.hhz.dbe.distributed.system.multicast.MulticastReceiver;
import de.hhz.dbe.distributed.system.multicast.MulticastSender;

public class Client {
	private static Logger logger = LogManager.getLogger(Client.class);

	private Socket clientSocket;

	private MulticastSender sender;
	private MulticastReceiver receiver;

	public Client(MulticastSender sender, MulticastReceiver receiver) {
		this.sender = sender;
		this.receiver = receiver;
	}

	public void joinGroup(BaseMessage msg) throws Exception {
		this.sender.sendMessage(MessageHandler.getByteFrom(msg));
	}

	public void startConnection(String serverIp, int serverPort)
			throws UnknownHostException, IOException, InterruptedException {
		logger.info(String.format("IP %s port %s", serverIp, serverPort));
		clientSocket = new Socket(serverIp, serverPort);
	}

	public MessageObject sendMessage(BaseMessage msg) throws IOException, ClassNotFoundException {
		ObjectOutputStream os = new ObjectOutputStream(clientSocket.getOutputStream());
		os.writeObject(msg);
		// read response from server
		try {
			ObjectInputStream is = new ObjectInputStream(clientSocket.getInputStream());
			MessageObject message = (MessageObject) is.readObject();
			is.close();
			os.close();
			return message;
		} catch (Exception e) {
		}

		os.close();
		return null;
	}

	public void stopConnection() throws IOException {
		clientSocket.close();
	}

	public void leaveGroup() {
		try {
			receiver.leaveGroup();
			System.exit(0);
			clientSocket.close();
		} catch (IOException e) {
			logger.debug("Error leaving the group: " + e.getMessage());
		}
	}

}
