package de.hhz.dbe.distributed.system.multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hhz.dbe.distributed.system.message.Message;
import de.hhz.dbe.distributed.system.message.MessageHandler;
import de.hhz.dbe.distributed.system.message.MessageObject;
import de.hhz.dbe.distributed.system.message.MessageProcessorIF;
import de.hhz.dbe.distributed.system.message.MessageType;

public class MulticastReceiver implements Runnable {
	private static Logger logger = LogManager.getLogger(MulticastReceiver.class);
	private byte buffer[];
	private int port;
	private String hostname;
	private MessageProcessorIF messageProcessor;
	private MulticastSocket socket;
	InetAddress group;

	public MulticastReceiver(String hostname, int port, MessageProcessorIF messageProcessor) throws IOException {
		buffer = new byte[1024 * 10];
		this.port = port;
		this.hostname = hostname;
		this.messageProcessor = messageProcessor;
		socket = new MulticastSocket(port);
		group = InetAddress.getByName(hostname);
		socket.setReuseAddress(true);
		socket.joinGroup(group);

	}

	public void leaveGroup() throws IOException {
		socket.leaveGroup(group);
		socket.close();
	}

	public void listenUDPMessage() throws IOException {
//		socket.setSoTimeout(15000);
		logger.info("Waiting for multicast message...");
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		socket.receive(packet);
		MessageObject msg = MessageHandler.getMessageFrom(buffer);
//		logger.info("Receive message of type: " + msg.getMessageType());
		this.messageProcessor.processMessage(msg);

	}

	public void run() {
		while (true) {
			try {
				listenUDPMessage();
			} catch (IOException ex) {
				logger.info("Error during multicast message" + ex);
			}

		}

	}
}
