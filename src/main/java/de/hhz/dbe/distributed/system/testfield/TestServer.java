package de.hhz.dbe.distributed.system.testfield;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hhz.dbe.distributed.system.server.Server;

public class TestServer {
	private static Logger logger = LogManager.getLogger(TestServer.class);
	public static void main(String[] args) {
		logger.info("Running at port" + 4002);
		try {
			new Server(4002).start();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
