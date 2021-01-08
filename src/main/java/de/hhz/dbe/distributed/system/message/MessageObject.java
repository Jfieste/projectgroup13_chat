package de.hhz.dbe.distributed.system.message;

import java.io.Serializable;
import java.util.Vector;

public class MessageObject extends BaseMessage implements Serializable {
	Vector<Message> messgaes = new Vector<Message>();

	private static final long serialVersionUID = -9040479273965431406L;

	public MessageObject(MessageType messageType) {
		super(messageType);
	}

	public Vector<Message> getMessgaes() {
		return messgaes;
	}

	public void setMessgaes(Vector<Message> messgaes) {
		this.messgaes = messgaes;
	}
}
