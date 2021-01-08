package de.hhz.dbe.distributed.system.message;

public enum MessageType {

	HEARTBEAT(), 
	JOIN_MESSAGE(),
	LEAVEMESSAGE(), 
	CONNECTION_LOST(),
	MASTER_ELECTED(),
	MASTER_IN_ELECTION(),
	SERVER_RESPONSE(),
	REQUEST_MESSAGES(),
	START_ELECTION(),
	ELECTION_REQUEST(),
	REQUEST_LOST_MESSAGE(),
	CHAT_MESSAGE();

}
