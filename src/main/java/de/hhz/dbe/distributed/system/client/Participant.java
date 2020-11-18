package de.hhz.dbe.distributed.system.client;

import java.net.InetAddress;

public class Participant {
	InetAddress addr;
	int port;

	Participant(InetAddress a, int p) {
		addr = a;
		port = p;
	}
}
