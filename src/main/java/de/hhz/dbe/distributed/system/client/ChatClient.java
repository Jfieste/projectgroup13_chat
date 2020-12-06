package de.hhz.dbe.distributed.system.client;

import de.hhz.dbe.distributed.system.multicast.MulticastReceiver;

public class ChatClient {

	final static String INET_ADDR = "224.0.0.3";
	final static int PORT = 8888;

	public static void main(String args[]) throws Exception {
		String host = "";
		if (args.length < 1) {
			System.out.println("Usage: java ChatClient <server_hostname>");
			System.exit(0);
		} else {
			host = args[0];
		}

		MulticastReceiver r = new MulticastReceiver(PORT, INET_ADDR);
		Thread rt = new Thread(r);
		rt.start();
	}

}
