package de.hhz.dbe.distributed.system.message;

public enum MessageType {

	HEARTBEAT("heartbeat"), 
	JOIN_MESSAGE("join group"),
	LEAVEMESSAGE("leave chat"), 
	MASTER_ELECTED("master elected"),
	SERVER_RESPONSE("response"),
	CHAT_MESSAGE("chat message");

	String typeContent;

	MessageType(String typeContent) {
		this.typeContent = typeContent;
	}

	public String getTypeContent() {
		return typeContent;
	}
}
