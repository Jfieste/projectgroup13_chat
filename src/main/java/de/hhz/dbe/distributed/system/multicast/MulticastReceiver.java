package de.hhz.dbe.distributed.system.multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hhz.dbe.distributed.system.message.BaseMessage;
import de.hhz.dbe.distributed.system.message.MessageHandler;
import de.hhz.dbe.distributed.system.message.MessageProcessorIF;

public class MulticastReceiver implements Runnable {
	private static Logger logger = LogManager.getLogger(MulticastReceiver.class);
	private byte buffer[];
	private int port;
	private String hostname;
	private MessageProcessorIF messageProcessor;
	private MulticastSocket socket;
	InetAddress group;
	DatagramPacket packet;

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

	public void listenUDPMessage() {
		try {
			logger.info("Waiting for multicast message...");
			packet = new DatagramPacket(buffer, buffer.length);
			socket.receive(packet);
			BaseMessage msg = MessageHandler.getMessageFrom(buffer);
			this.messageProcessor.processMessage(msg);
		} catch (IOException e) {
			logger.info("Connection error..." + e.getMessage());
		}

	}

	public void run() {
		while (true) {
			listenUDPMessage();

		}
	}
}
