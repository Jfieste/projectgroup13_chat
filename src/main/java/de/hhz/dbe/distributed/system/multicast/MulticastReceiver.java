package de.hhz.dbe.distributed.system.multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hhz.dbe.distributed.system.message.Message;
import de.hhz.dbe.distributed.system.testfield.MessageHandler;

public class MulticastReceiver implements Runnable {
	private static Logger logger = LogManager.getLogger(MulticastReceiver.class);
	byte buffer[];
	int port;
	 private String hostname;
	public MulticastReceiver(int port,String hostname) throws IOException {
		buffer = new byte[1024];
		this.port = port;
		this.hostname=hostname;
	}

	public void receiveUDPMessage() throws IOException {
		MulticastSocket socket = new MulticastSocket(port);
		InetAddress group = InetAddress.getByName(hostname);
		socket.joinGroup(group);
		Message msg;
		while (true) {
			logger.info("Waiting for multicast message...");
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			socket.receive(packet);
			msg = MessageHandler.getMessageFrom(buffer);
//			String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
			logger.info("[Multicast UDP message received] >>" + msg.getProcessId());
			if ("OK".equals(msg)) {
				System.out.println("No more message. Exiting : " + msg);
				break;
			}
		}
		socket.leaveGroup(group);
		socket.close();
	}

	public void run() {
		try {
			receiveUDPMessage();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
