package de.hhz.dbe.distributed.system.serverapp;

import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hhz.dbe.distributed.system.server.Server;
import de.hhz.dbe.distributed.system.utils.LoadProperties;

public class ServerApplication {
	private static Logger logger = LogManager.getLogger(ServerApplication.class);

	public static void main(String[] args) {
		try {
			Properties prop = new LoadProperties().readProperties();
			String multicast = prop.getProperty("MULTICAST_GROUP");
			int multicastPort = Integer.parseInt(prop.getProperty("MULTICAST_PORT"));
			int tcpPort = 0;
			if (args.length > 0) {
				tcpPort = Integer.parseInt(args[0]);
			} else {
				logger.error("Add starting the sever. Please the port as programm arguments");
				System.exit(404);
			}

			new Server(tcpPort, multicast, multicastPort).start();
		} catch (IOException e) {
			logger.error("Server already runing at the same port");
		}
	}

}
