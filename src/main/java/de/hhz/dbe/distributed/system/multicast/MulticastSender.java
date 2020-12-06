package de.hhz.dbe.distributed.system.multicast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MulticastSender  implements Runnable {
	private static Logger logger = LogManager.getLogger(MulticastSender.class);
    private String hostname;
    private int port ;
    public MulticastSender(int port, String hostnme) throws IOException {
    	this.port= port;
        hostname = hostnme;
    }
    public void sendMessage( byte buf[]) throws Exception {
        MulticastSocket sock =  new MulticastSocket(port);
        InetAddress address = InetAddress.getByName(hostname);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        sock.send(packet);
    }
    public void run() {
//        boolean connected = false;
//        do {
//            try {
//                sendMessage("GREETINGS");
//                connected = true;
//            } catch (Exception e) {
//                
//            }
//        } while (!connected);
//        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//        while (true) {
//            try {
//                while (!in.ready()) {
//                    Thread.sleep(100);
//                }
//                sendMessage(in.readLine());
//            } catch(Exception e) {
//                System.err.println(e);
//            }
//        }
    }
}