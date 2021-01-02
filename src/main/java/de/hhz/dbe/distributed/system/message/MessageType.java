package de.hhz.dbe.distributed.system.message;

public enum MessageType {

	HEARTBEAT("heartbeat"), 
	JOIN_MESSAGE("join group"),
	LEAVEMESSAGE("leave chat"), 
	CONNECTION_LOST("LOST"),
	MASTER_ELECTED("master elected"),
	MASTER_IN_ELECTION("master in election"),
	SERVER_RESPONSE("response"),
	CONNECTION_DETAIL("connection details"),
	START_ELECTION("start election"),
	ELECTION_REQUEST("election request"),
	REQUEST_LOST_MESSAGE("Request Message"),
	RESPONSE_LOST_MESSAGE("Request Message"),
	CHAT_MESSAGE("chat message");

	String typeContent;

	MessageType(String typeContent) {
		this.typeContent = typeContent;
	}

	public String getTypeContent() {
		return typeContent;
	}
}
