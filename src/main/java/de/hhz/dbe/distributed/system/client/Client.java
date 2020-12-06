package de.hhz.dbe.distributed.system.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hhz.dbe.distributed.system.message.Message;
import de.hhz.dbe.distributed.system.message.Payload;
import de.hhz.dbe.distributed.system.message.VectorClock;
import de.hhz.dbe.distributed.system.multicast.MulticastReceiver;

public class Client {
	private static Logger logger = LogManager.getLogger(Client.class);
	private String ip;
	private int port;

	private Socket clientSocket;
	private OutputStream out;
	private ObjectOutputStream objectOutputStream;

	private String multicasAddr;
	private int multicastPort;

	public Client(String ip, int port, String multicasAddr, int multicastPort) {
		this.ip = ip;
		this.port = port;
		this.multicasAddr = multicasAddr;
		this.multicastPort = multicastPort;
	}

	public void startConnection() throws UnknownHostException, IOException {
		clientSocket = new Socket(ip, port);
		out = clientSocket.getOutputStream();
		objectOutputStream = new ObjectOutputStream(out);
	}

	public void sendMessage(Message msg) throws IOException {
		objectOutputStream.writeObject(msg);
	}

	public void stopConnection() throws IOException {
		objectOutputStream.close();
		out.close();
		clientSocket.close();
	}

	public void listenToMessage() throws IOException {
		MulticastReceiver r = new MulticastReceiver(multicasAddr, multicastPort);
		Thread rt = new Thread(r);
		rt.start();
	}

}
