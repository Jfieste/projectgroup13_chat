package de.hhz.dbe.distributed.system.multicast;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MulticastSender  {
	private static Logger logger = LogManager.getLogger(MulticastSender.class);
    private String hostname;
    private int port ;
    MulticastSocket sock ;
    public MulticastSender(String hostname, int port) {
    	this.port= port;
        this.hostname = hostname;
    }
    /**
     * 
     * @param buf The message to send
     * @throws Exception
     */
    public void sendMessage( byte buf[]) throws Exception {
        sock =  new MulticastSocket(port);
        logger.info(String.format("Send a Multicast message to: %s with port: %s",hostname, port));
        InetAddress address = InetAddress.getByName(hostname);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        sock.send(packet);
        sock.close();
    }
    
    /**
     * close the Socket
     */
    public void closeSocket() {
    	if (sock !=  null) {
			sock.close();
		}
    }
}