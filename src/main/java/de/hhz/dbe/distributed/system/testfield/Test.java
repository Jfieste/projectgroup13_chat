package de.hhz.dbe.distributed.system.testfield;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hhz.dbe.distributed.system.message.MessageObject;
import de.hhz.dbe.distributed.system.message.MessageProcessorIF;
import de.hhz.dbe.distributed.system.multicast.MulticastReceiver;

public class Test {
	private static Logger logger = LogManager.getLogger(Test.class);

	public static void main(String[] args) {
		MessageProcessorIF messageProcessor = new MessageProcessorIF() {

			public void processMessage(MessageObject msg) {
				logger.info(String.format("Receive message from type %s", msg.getMessageType()));
				switch (msg.getMessageType()) {
				case JOIN_MESSAGE:
					logger.info(String.format("Receive message from type 1 %s", msg.getMessageType()));
					break;
				case CONNECTION_DETAIL:
					logger.info(String.format("Receive message from type 2 %s", msg.getMessageType()));
					break;
				default:
					break;
				}

			}
		};
		MulticastReceiver receiver;
		try {
			receiver = new MulticastReceiver("224.0.0.3", 8888, messageProcessor);
			Thread receiverThread = new Thread(receiver);
			receiverThread.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
