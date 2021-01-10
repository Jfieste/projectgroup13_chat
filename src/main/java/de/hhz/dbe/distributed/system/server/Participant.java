package de.hhz.dbe.distributed.system.server;

import java.io.Serializable;
import java.util.UUID;

public class Participant implements Serializable, Comparable<Participant> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String addr;
	private int port;
	private UUID id;
	private boolean isServerComponent;

	public Participant(String address, int port, UUID id, boolean isServerComponent) {
		this.addr = address;
		this.port = port;
		this.id = id;
		this.isServerComponent = isServerComponent;

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

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public int compareTo(Participant o) {
		return getId().compareTo(o.getId());
	}

	public boolean isServerComponent() {
		return isServerComponent;
	}

	public void setServerComponent(boolean isServerComponent) {
		this.isServerComponent = isServerComponent;
	}

}
