package de.hhz.dbe.distributed.system.message;

import java.io.Serializable;

public abstract class MessageObject implements Serializable {
	MessageType messageType;

	MessageObject(MessageType messageType) {
		this.messageType = messageType;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setType(MessageType messageType) {
		this.messageType = messageType;
	}
}
