package de.hhz.dbe.distributed.system.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public class MessageHandler {

	/**
	 * Converts an object in a stream of bytes
	 *
	 * @param message
	 * @return
	 * @throws IOException
	 */
	public static byte[] getByteFrom(Object obj) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = new ObjectOutputStream(bos);

		out.writeObject(obj);
		out.close();
		return bos.toByteArray();
	}

	/**
	 * Converts a stream of bytes in a Message object
	 *
	 * @param buffer
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static MessageObject getMessageFrom(byte[] buffer) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
		ObjectInputStream in = new ObjectInputStream(bis);

		MessageObject message = null;
		try {
			message = (MessageObject) in.readObject();
		} catch (ClassNotFoundException e) {
			// e.printStackTrace();
		}
		in.close();
		return message;
	}

	/**
	 *
	 * @param buffer
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static RequestMessage getRequestMessageFrom(byte[] buffer) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
		ObjectInputStream in = new ObjectInputStream(bis);

		RequestMessage request = null;
		try {
			request = (RequestMessage) in.readObject();
		} catch (ClassNotFoundException e) {
			// e.printStackTrace();
		}
		in.close();
		return request;
	}
}
