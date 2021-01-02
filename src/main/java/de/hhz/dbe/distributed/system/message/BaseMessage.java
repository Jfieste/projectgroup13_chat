package de.hhz.dbe.distributed.system.message;

import java.io.Serializable;

import de.hhz.dbe.distributed.system.client.Participant;

public abstract class BaseMessage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private MessageType messageType;
	private Participant participant;
	private Participant neighbor;
	public BaseMessage(MessageType messageType) {
		this.messageType = messageType;
	}

	public Participant getParticipant() {
		return participant;
	}

	public void setParticipant(Participant participant) {
		this.participant = participant;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setType(MessageType messageType) {
		this.messageType = messageType;
	}

	public Participant getNeighbor() {
		return neighbor;
	}

	public void setNeighbors(Participant neighbor) {
		this.neighbor = neighbor;
	}
	
}
