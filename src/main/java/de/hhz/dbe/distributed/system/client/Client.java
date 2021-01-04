package de.hhz.dbe.distributed.system.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hhz.dbe.distributed.system.message.BaseMessage;
import de.hhz.dbe.distributed.system.message.MessageHandler;
import de.hhz.dbe.distributed.system.message.MessageObject;
import de.hhz.dbe.distributed.system.message.MessageProcessorIF;
import de.hhz.dbe.distributed.system.multicast.MulticastReceiver;
import de.hhz.dbe.distributed.system.multicast.MulticastSender;

public class Client {
	private static Logger logger = LogManager.getLogger(Client.class);
	private String serverIp;
	private int serverPort;
	private Socket clientSocket;
	private OutputStream out;
	private ObjectOutputStream objectOutputStream;

	private String multicasAddr;
	private int multicastPort;
	private MulticastSender sender;
	private MulticastReceiver receiver;
	final CountDownLatch latch = new CountDownLatch(1);

	public Client(String multicasAddr, int multicastPort) {
		this.multicasAddr = multicasAddr;
		this.multicastPort = multicastPort;
		try {
			sender = new MulticastSender(multicasAddr, multicastPort);
			receiver = new MulticastReceiver(multicasAddr, multicastPort, messageProcessor);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	MessageProcessorIF messageProcessor = new MessageProcessorIF() {
		public void processMessage(BaseMessage message) {
			logger.info(String.format("Receive message from type %s", message.getMessageType()));
			switch (message.getMessageType()) {
			case SERVER_RESPONSE:

				try {
					Participant participant = ((MessageObject) message).getParticipant();
					serverIp = participant.getAddr();
					serverPort = participant.getPort();
					latch.countDown();
				} catch (Exception e) {
					logger.error(String.format("Somthing went wrong sending connection details: %s", e));
				}
				break;
			case CONNECTION_DETAIL:
				logger.info(String.format("Receive message from type %s", message.getMessageType()));
				break;
			case CHAT_MESSAGE:
			default:
				break;
			}

		}
	};

	public void joinGroup(BaseMessage msg) throws IOException, Exception {
		listenToMessage();
		this.sender.sendMessage(MessageHandler.getByteFrom(msg));
	}

	public void startConnection() throws UnknownHostException, IOException, InterruptedException {
		latch.await();
		logger.info(String.format("IP %s port %s", serverIp, serverPort));
		clientSocket = new Socket(serverIp, serverPort);
		out = clientSocket.getOutputStream();
		objectOutputStream = new ObjectOutputStream(out);
	}

	public void sendMessage(BaseMessage msg) throws IOException {
		objectOutputStream.writeObject(msg);
	}

	public void readMessage() throws IOException, ClassNotFoundException {
		InputStream in = clientSocket.getInputStream();
		ObjectInputStream objectInputStream = new ObjectInputStream(in);
		String mess = (String) objectInputStream.readObject();
		logger.info(mess);
	}

	public void stopConnection() throws IOException {
		objectOutputStream.close();
		out.close();
		clientSocket.close();
	}
	
	public void leaveGroup() {
		try {
			receiver.leaveGroup();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void listenToMessage() throws IOException {
		Thread rt = new Thread(receiver);
		rt.start();
	}

}
