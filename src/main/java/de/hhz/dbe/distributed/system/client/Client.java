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
import de.hhz.dbe.distributed.system.testfield.VectorClock;

public class Client {
	private static Logger logger = LogManager.getLogger(Client.class);
	private String ip;
	private int port;

	private Socket clientSocket;
	private OutputStream out;
	private ObjectOutputStream objectOutputStream;

	public Client(String ip, int port) {
		this.ip = ip;
		this.port = port;
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

	public static void main(String[] args) {
		try {
			Client c = new Client("192.168.56.1", 4001);
			c.startConnection();
			c.sendMessage(new Message("test", 1, new Payload("Eric", "Hi"), new VectorClock()));
			c.stopConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("somthing");
			e.printStackTrace();
		}
	}
}
