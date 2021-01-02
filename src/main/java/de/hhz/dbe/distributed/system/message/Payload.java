package de.hhz.dbe.distributed.system.message;

import java.io.Serializable;
import java.util.Date;

public class Payload extends BaseMessage implements Serializable {

	private static final long serialVersionUID = 8554705838734687076L;
	private Date date;
	private String author;
	private String text;
	private int menberID;
	public Payload(String author, String text) {
		super(MessageType.CHAT_MESSAGE);
		this.date = new Date();
		this.author = author;
		this.text = text;
	}

	public Date getDate() {
		return date;
	}

	public String getAuthor() {
		return author;
	}

	public String getText() {
		return text;
	}

	public int getMenberID() {
		return menberID;
	}

	public void setMenberID(int menberID) {
		this.menberID = menberID;
	}
}
