package de.hhz.dbe.distributed.system.message;

public enum MessageType {

	HEARTBEAT(), 
	JOIN_MESSAGE(),
	CONNECTION_LOST(),
	MASTER_ELECTED(),
	NEW_MASTER_ELECTED(),
	MASTER_IN_ELECTION(),
	SERVER_RESPONSE(),
	REQUEST_MESSAGES(),
	REQUESTED_MESSAGES(),
	START_ELECTION(),
	REQUEST_LOST_MESSAGE(),
	CHAT_MESSAGE();

}
