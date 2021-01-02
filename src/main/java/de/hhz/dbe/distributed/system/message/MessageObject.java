package de.hhz.dbe.distributed.system.message;

import java.io.Serializable;

public class MessageObject  extends BaseMessage implements Serializable {

	public MessageObject(MessageType messageType) {
		super(messageType);
	}
	private static final long serialVersionUID = -9040479273965431406L;
	private int messageId;



}
