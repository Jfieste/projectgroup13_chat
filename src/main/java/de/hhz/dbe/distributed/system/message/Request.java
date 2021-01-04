package de.hhz.dbe.distributed.system.message;

import java.io.Serializable;

public class Request extends BaseMessage implements Serializable {

  private static final long serialVersionUID = -9040479273965431406L;
  private int messageId;

  public Request(int messageId) {
	  super(MessageType.REQUEST_LOST_MESSAGE);
	  this.messageId = messageId; }

  public int getMessageId() { return messageId; }

  public void setMessageId(int messageId) { this.messageId = messageId; }
}
