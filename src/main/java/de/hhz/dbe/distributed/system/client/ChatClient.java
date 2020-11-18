package de.hhz.dbe.distributed.system.client;
import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hhz.dbe.distributed.system.message.MessageReceiver;
import de.hhz.dbe.distributed.system.message.MessageSender;

public class ChatClient {
	private static Logger logger = LogManager.getLogger(ChatClient.class);
    public static void main(String args[]) throws Exception {
        String host = null;
        if (args.length < 1) {
            System.out.println("Usage: java ChatClient <server_hostname>");
            System.exit(0);
        } else {
            host = args[0];
        }
        DatagramSocket socket = new DatagramSocket();
        MessageReceiver r = new MessageReceiver(socket);
        MessageSender s = new MessageSender(socket, host);
        Thread rt = new Thread(r);
        Thread st = new Thread(s);
        rt.start(); st.start();
    }
}


