package de.hhz.dbe.distributed.system.client;

import java.net.InetAddress;

public class Participant {
	InetAddress addr;
	int port;

	Participant(InetAddress a, int p) {
		addr = a;
		port = p;
	}

	public InetAddress getAddr() {
		return addr;
	}

	public void setAddr(InetAddress addr) {
		this.addr = addr;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
}
