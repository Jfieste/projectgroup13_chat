package de.hhz.dbe.distributed.system.message;

import java.io.Serializable;

public class RequestMessage implements Serializable {

	private static final long serialVersionUID = -9040479273965431406L;
	private int messageId;

	public RequestMessage(int messageId) {
		this.messageId = messageId;
	}

	public int getMessageId() {
		return messageId;
	}

	public void setMessageId(int messageId) {
		this.messageId = messageId;
	}
}
