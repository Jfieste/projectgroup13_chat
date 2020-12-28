package de.hhz.dbe.distributed.system.message;

import java.util.UUID;

public class MasterElected extends MessageObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MasterElected(MessageType messageType) {
		super(messageType);
	}
}
