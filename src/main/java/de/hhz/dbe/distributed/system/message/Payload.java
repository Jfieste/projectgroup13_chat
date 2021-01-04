package de.hhz.dbe.distributed.system.message;

import java.io.Serializable;
import java.util.Date;

public class Payload extends BaseMessage implements Serializable {

	private static final long serialVersionUID = 8554705838734687076L;
	private Date date;
	private String author;

	private String text;
	private int menberID;

	public Payload() {
		super(MessageType.CHAT_MESSAGE);
		this.date = new Date();
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

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setText(String text) {
		this.text = text;
	}
}
