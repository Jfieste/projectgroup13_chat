package de.hhz.dbe.distributed.system.message;

public class ConnectionMessage extends MessageObject {
	
	private static final long serialVersionUID = 1L;
	private String message;
	public ConnectionMessage(MessageType messageType, String message) {
		super(messageType);
		this.message = message;
	}
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
