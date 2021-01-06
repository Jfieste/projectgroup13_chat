package de.hhz.dbe.distributed.system.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hhz.dbe.distributed.system.message.BaseMessage;
import de.hhz.dbe.distributed.system.message.MessageHandler;
import de.hhz.dbe.distributed.system.multicast.MulticastReceiver;
import de.hhz.dbe.distributed.system.multicast.MulticastSender;

public class Client {
	private static Logger logger = LogManager.getLogger(Client.class);

	private String serverIp;
	private int serverPort;
	private Socket clientSocket;
	private OutputStream out;

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

	public void sendMessage(BaseMessage msg) throws IOException {
		OutputStream out = clientSocket.getOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
		objectOutputStream.writeObject(msg);
		objectOutputStream.close();
		out.close();

	}

	public void stopConnection() throws IOException {
		out.close();
		clientSocket.close();
	}

	public void leaveGroup() {
		try {
			receiver.leaveGroup();
		} catch (IOException e) {
			logger.debug("Error leaving the group: " + e.getMessage());
		}
	}

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

}
