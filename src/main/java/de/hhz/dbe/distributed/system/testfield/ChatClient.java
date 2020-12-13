package de.hhz.dbe.distributed.system.testfield;

import java.io.IOException;
import java.util.Properties;

import de.hhz.dbe.distributed.system.client.Client;
import de.hhz.dbe.distributed.system.message.Message;
import de.hhz.dbe.distributed.system.message.MessageProcessorIF;
import de.hhz.dbe.distributed.system.message.MessageType;
import de.hhz.dbe.distributed.system.message.Payload;
import de.hhz.dbe.distributed.system.message.VectorClock;
import de.hhz.dbe.distributed.system.multicast.MulticastReceiver;
import de.hhz.dbe.distributed.system.utils.LoadProperties;

public class ChatClient {

	public static void main(String[] args) {
	
		try {
			Properties prop = new LoadProperties().readProperties();
			String multicast = prop.getProperty("MULTICAST_GROUP");
			int port = Integer.parseInt(prop.getProperty("MULTICAST_PORT"));
			String tcpAdd = prop.getProperty("TCP_ADDRESS");
			int tcpPort = Integer.parseInt(prop.getProperty("TCP_PORT"));
			Client c = new Client(tcpAdd, tcpPort, multicast, port, new MessageProcessorIF() {
				
				public void processMessage(MessageType messageType) {
					// TODO Auto-generated method stub
					
				}
			});
			c.startConnection();
			c.sendMessage(new Message(MessageType.CHAT_MESSAGE,"test", 1, new Payload("Eric", "Hi"), new VectorClock()));
			c.stopConnection();
			c.listenToMessage();
		} catch (IOException e) {
			System.out.println("somthing");
			e.printStackTrace();
		}
	}

}
