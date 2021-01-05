package de.hhz.dbe.distributed.system.testfield;

import java.io.IOException;
import java.util.Properties;

import de.hhz.dbe.distributed.system.client.Client;
import de.hhz.dbe.distributed.system.message.BaseMessage;
import de.hhz.dbe.distributed.system.message.Message;
import de.hhz.dbe.distributed.system.message.MessageObject;
import de.hhz.dbe.distributed.system.message.MessageType;
import de.hhz.dbe.distributed.system.message.Payload;
import de.hhz.dbe.distributed.system.message.VectorClock;
import de.hhz.dbe.distributed.system.utils.LoadProperties;

public class ChatClient {

	public static void main(String[] args) {
//		System.setProperty("java.net.preferIPv4Stack" , "true");
//		try {
//			Properties prop = new LoadProperties().readProperties();
//			String multicast = prop.getProperty("MULTICAST_GROUP");
//			int port = Integer.parseInt(prop.getProperty("MULTICAST_PORT"));
//			BaseMessage conMsg = new MessageObject(MessageType.JOIN_MESSAGE);
////			String tcpAdd = prop.getProperty("TCP_ADDRESS");
////			int tcpPort = Integer.parseInt(prop.getProperty("TCP_PORT_SERVER_1"));
//			Client c = new Client(multicast, port);
//			try {
//				c.joinGroup(conMsg);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			c.startConnection();
//			
//			c.sendMessage(new Payload());
////			c.stopConnection();
//			
////			c.readMessage();
//		} catch (IOException e) {
//			System.out.println("somthing");
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

}
