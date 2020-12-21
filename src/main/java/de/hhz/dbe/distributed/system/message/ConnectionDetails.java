package de.hhz.dbe.distributed.system.message;

import de.hhz.dbe.distributed.system.client.Participant;

public class ConnectionDetails extends MessageObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Participant participant;

	public ConnectionDetails(MessageType messageType, Participant participant) {
		super(messageType);
		this.participant = participant;
	}

	public Participant getParticipant() {
		return participant;
	}

	public void setParticipant(Participant participant) {
		this.participant = participant;
	}

}
