package de.hhz.dbe.distributed.system.message;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.Vector;

public class Message extends MessageObject implements Serializable, Comparable<Message> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String processId;
	private Long messageId;
	private Payload payload;
	private VectorClock vectorClock;
	private Date receiveDate;

	public Message(String processId, long messageId, Payload payload, VectorClock vectorClock) throws IOException {
		super(MessageType.CHAT_MESSAGE);
		this.processId = processId;
		this.messageId = messageId;
		this.payload = payload;
		this.vectorClock = vectorClock;
		receiveDate = new Date();
	}

	public String getProcessId() {
		return processId;
	}

	public Payload getPayload() {
		return payload;
	}

	public Long getMessageId() {
		return messageId;
	}

	public VectorClock getPiggybackedVectorClock() {
		return vectorClock;
	}

	public Date getReceiveDate() {
		return receiveDate;
	}

	public void setReceiveDate(Date receiveDate) {
		this.receiveDate = receiveDate;
	}

	public String toString() {
		return messageId + " by " + processId + " " + getPiggybackedVectorClock().toString();
	}

	public int compareTo(Message message) {
		Set<String> s1 = vectorClock.getVector().keySet();
		Set<String> s2 = message.getPiggybackedVectorClock().getVector().keySet();
		Vector<String> keys = unionSet(s1, s2);

		int v1, v2;
		int result =5;

		int i = 0;
		for (String id : keys) {
			v1 = vectorClock.get(id);
			v2 = message.getPiggybackedVectorClock().get(id);

			if (v1 < v2) {
				if (i == 0 || result == -2) {
					result = -2;
				} else {
					if (result == -1 || result == 0) {
						result =-1;
					} else {
						result = 5;
						break;
					}
				}
			} else if (v1 > v2) {
				if (i == 0 || result == 2) {
					result = 1;
				} else {
					if (result ==1|| result == 0) {
						result = 1;
					} else {
						result = 5;
						break;
					}
				}
			} else {
				if (i == 0 || result == 0) {
					result = 0;
				} else {
					if (result == -1 || result == -2) {
						result =-1;
					} else if (result == 1 || result == 2) {
						result = 1;
					}
				}
			}
			i++;
		}

		if (result == 5) {
			result = receiveDate.compareTo(message.getReceiveDate());
		}

		return 0;
	}

	private Vector<String> unionSet(Set<String> s1, Set<String> s2) {
		Vector<String> union = new Vector<String>();
		union.addAll(s1);
		for (String s : s2) {
			if (!union.contains(s)) {
				union.add(s);
			}
		}
		return union;
	}

}
