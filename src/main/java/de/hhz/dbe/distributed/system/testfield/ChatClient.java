package de.hhz.dbe.distributed.system.testfield;

import java.io.IOException;

import de.hhz.dbe.distributed.system.client.Client;
import de.hhz.dbe.distributed.system.message.Message;
import de.hhz.dbe.distributed.system.message.Payload;
import de.hhz.dbe.distributed.system.message.VectorClock;
import de.hhz.dbe.distributed.system.multicast.MulticastReceiver;

public class ChatClient {

	final static String INET_ADDR = "224.0.0.3";
	final static int PORT = 8888;

	public static void main(String[] args) {
		try {
			Client c = new Client("192.168.56.1", 4001, INET_ADDR, PORT);
			c.startConnection();
			c.sendMessage(new Message("test", 1, new Payload("Eric", "Hi"), new VectorClock()));
			c.stopConnection();
			c.listenToMessage();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("somthing");
			e.printStackTrace();
		}
	}

}
