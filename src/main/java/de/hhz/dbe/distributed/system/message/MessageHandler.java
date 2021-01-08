package de.hhz.dbe.distributed.system.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

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
	public static BaseMessage getMessageFrom(byte[] buffer) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
		ObjectInputStream in = new ObjectInputStream(bis);

		BaseMessage message = null;
		try {
			message = (BaseMessage) in.readObject();
		} catch (ClassNotFoundException e) {
			// e.printStackTrace();
		} catch (EOFException eo) {

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
	public static Request getRequestMessageFrom(byte[] buffer) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
		ObjectInputStream in = new ObjectInputStream(bis);

		Request request = null;
		try {
			request = (Request) in.readObject();
		} catch (ClassNotFoundException e) {
			// e.printStackTrace();
		}
		in.close();
		return request;
	}

	/**
	 * retransmit messages
	 * 
	 * @param ip
	 * @param repPortClient
	 * @param messageId
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Message requestMessage(String ip, int repPortClient, BaseMessage request)
			throws IOException, ClassNotFoundException {
//		Request request = new Request(messageId);
		BaseMessage message = null;
		Socket socket = new Socket(ip, repPortClient);
		ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
		os.writeObject(request);
		// read response from leader
		ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
		message = (Message) is.readObject();
		is.close();
		os.close();
		socket.close();

		return (Message) message;
	}

	/**
	 * retransmit list of messages
	 * 
	 * @param ip
	 * @param repPortClient
	 * @param messageId
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public static Vector<Message> requestListOfMessages(String ip, int repPortClient, BaseMessage request)
			throws IOException, ClassNotFoundException {
		Socket socket = new Socket(ip, repPortClient);
		ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
		os.writeObject(request);
		// read response from leader
		ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
		Vector<Message> message = (Vector<Message>) is.readObject();
		is.close();
		os.close();
		socket.close();

		return (Vector<Message>) message;
	}
}
