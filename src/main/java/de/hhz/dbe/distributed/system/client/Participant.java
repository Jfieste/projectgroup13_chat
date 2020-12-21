package de.hhz.dbe.distributed.system.client;

import java.io.Serializable;
import java.net.InetAddress;

public class Participant implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String addr;
	private int port;
	private int id;

	public Participant(String address, int port, int id) {
		this.addr = address;
		this.port = port;
		this.id = id;
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
