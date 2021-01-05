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
import de.hhz.dbe.distributed.system.message.Payload;
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

//	MessageProcessorIF messageProcessor = new MessageProcessorIF() {
//		public void processMessage(BaseMessage message) {
//			logger.info(String.format("Receive message from type %s", message.getMessageType()));
//			switch (message.getMessageType()) {
//			case SERVER_RESPONSE:
//
//				try {
//					Participant participant = ((MessageObject) message).getParticipant();
//					serverIp = participant.getAddr();
//					serverPort = participant.getPort();
//					latch.countDown();
//				} catch (Exception e) {
//					logger.error(String.format("Somthing went wrong sending connection details: %s", e));
//				}
//				break;
//			case CONNECTION_DETAIL:
//				logger.info(String.format("Receive message from type %s", message.getMessageType()));
//				break;
//			case CHAT_MESSAGE:
//				Message mesg = (Message) message;
//				Payload payload = mesg.getPayload();
//				setReadedMessage(payload);
//				logger.info(String.format("Receive message from type %s %s", payload.getAuthor(), payload.getText()));
//				latch.countDown();
//			default:
//				break;
//			}
//
//		}
//	};

	public void joinGroup(BaseMessage msg) throws IOException, Exception {
//		listenToMessage();
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

//	public void readMessage() throws IOException, ClassNotFoundException {
//		InputStream in = clientSocket.getInputStream();
//		ObjectInputStream objectInputStream = new ObjectInputStream(in);
//		String mess = (String) objectInputStream.readObject();
//		logger.info(mess);
//		clientSocket.close();
//		in.close();
//		objectInputStream.close();
//	}

	public void stopConnection() throws IOException {
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
