package de.hhz.dbe.distributed.system.message;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageReceiver implements Runnable {
	private static Logger logger = LogManager.getLogger(MessageReceiver.class);
    DatagramSocket sock;
    byte buf[];
    public MessageReceiver(DatagramSocket s) {
        sock = s;
        buf = new byte[1024];
    }
    public void run() {
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                sock.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                logger.info(received);
            } catch(Exception e) {
                System.err.println(e);
            }
        }
    }
}
