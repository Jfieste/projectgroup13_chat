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

	public MulticastReceiver(String hostname, int port, MessageProcessorIF messageProcessor) throws IOException {
		buffer = new byte[1024];
		this.port = port;
		this.hostname = hostname;
		this.messageProcessor = messageProcessor;
	}

	public void receiveUDPMessage() throws IOException {
		MulticastSocket socket = new MulticastSocket(port);
		InetAddress group = InetAddress.getByName(hostname);
		socket.setReuseAddress(true);
		socket.setSoTimeout(15000);
		socket.joinGroup(group);
		logger.info("Waiting for multicast message...");
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		socket.receive(packet);
		MessageObject msg = MessageHandler.getMessageFrom(buffer);
		this.messageProcessor.processMessage(msg.getMessageType());
//		socket.leaveGroup(group);
//		socket.close();
	}

	public void run() {
		try {
			receiveUDPMessage();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
