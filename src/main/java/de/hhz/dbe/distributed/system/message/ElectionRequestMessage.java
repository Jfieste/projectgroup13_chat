package de.hhz.dbe.distributed.system.message;

import java.util.UUID;

public class ElectionRequestMessage extends MessageObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ElectionRequestMessage(MessageType messageType) {
		super(messageType);
	}
}
