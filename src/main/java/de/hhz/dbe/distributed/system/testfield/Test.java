package de.hhz.dbe.distributed.system.testfield;

import java.io.IOException;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hhz.dbe.distributed.system.message.BaseMessage;
import de.hhz.dbe.distributed.system.message.MessageProcessorIF;
import de.hhz.dbe.distributed.system.multicast.MulticastReceiver;

public class Test {
	private static Logger logger = LogManager.getLogger(Test.class);

	public static void main(String[] args) {
//		MessageProcessorIF messageProcessor = new MessageProcessorIF() {
//
//			public void processMessage(BaseMessage msg) {
//				logger.info(String.format("Receive message from type %s", msg.getMessageType()));
//				switch (msg.getMessageType()) {
//				case JOIN_MESSAGE:
//					logger.info(String.format("Receive message from type 1 %s", msg.getMessageType()));
//					break;
//				case CONNECTION_DETAIL:
//					logger.info(String.format("Receive message from type 2 %s", msg.getMessageType()));
//					break;
//				default:
//					break;
//				}
//
//			}
//		};
//		MulticastReceiver receiver;
//		try {
//			receiver = new MulticastReceiver("224.0.0.3", 8888, messageProcessor);
//			Thread receiverThread = new Thread(receiver);
//			receiverThread.start();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();
		System.out.println((id1.compareTo(id2)));
//			System.out.println(id1+"  work   "+id2);
//		UUID UUID_1 
//        = UUID 
//              .fromString( 
//                  "5fc03087-d265-11e7-b8c6-83e29cd24f4c"); 
//
//    UUID UUID_2 
//        = UUID 
//              .fromString( 
//                  "58e0a7d7-eebc-11d8-9669-0800200c9a66"); 
//    System.out.println("Comparison Value: "
//            + UUID_1.compareTo(UUID_2));
	}
}
