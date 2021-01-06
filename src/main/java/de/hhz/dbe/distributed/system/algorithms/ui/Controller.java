package de.hhz.dbe.distributed.system.algorithms.ui;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hhz.dbe.distributed.system.client.Client;
import de.hhz.dbe.distributed.system.client.Participant;
import de.hhz.dbe.distributed.system.message.BaseMessage;
import de.hhz.dbe.distributed.system.message.Message;
import de.hhz.dbe.distributed.system.message.MessageHandler;
import de.hhz.dbe.distributed.system.message.MessageObject;
import de.hhz.dbe.distributed.system.message.MessageProcessorIF;
import de.hhz.dbe.distributed.system.message.MessageType;
import de.hhz.dbe.distributed.system.message.Payload;
import de.hhz.dbe.distributed.system.message.VectorClock;
import de.hhz.dbe.distributed.system.multicast.MulticastReceiver;
import de.hhz.dbe.distributed.system.multicast.MulticastSender;
import de.hhz.dbe.distributed.system.utils.LoadProperties;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

public class Controller {
	private static Logger logger = LogManager.getLogger(Controller.class);
	@FXML
	private MenuItem enterGroup;

	@FXML
	private MenuItem leaveGroup;

	@FXML
	private MenuItem editName;

	@FXML
	private MenuItem helpMenu;

	@FXML
	private ListView<String> listView;

	@FXML
	private TextField textField;

	@FXML
	private Label autorName;

	@FXML
	private Button sendMessage;
	private String serverIp = null;

	private int serverPort = 0;
	private Client chatClient;
	private BaseMessage conMsg;
	final CountDownLatch latch = new CountDownLatch(5);
	private String userName;
	private MulticastSender sender;
	private MulticastReceiver receiver;
	TextInputDialog dialog = new TextInputDialog("");
	Properties prop;
	String multicast;
	int port;
	private VectorClock vectorClock;

	@FXML
	void onEditName(ActionEvent event) {

	}

	@FXML
	void onEnterGroup(ActionEvent event) {
		chatClient = new Client(sender, receiver);
		conMsg = new MessageObject(MessageType.JOIN_MESSAGE);
		dialog.setTitle("Chat Name");
		dialog.setHeaderText("Enter your username for the chat room");
		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()) {
			userName = result.get();
		}
		autorName.setText(userName);
		try {
			chatClient.joinGroup(conMsg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Thread rt = new Thread(receiver);
		rt.start();
	}

	@FXML
	void onHelp(ActionEvent event) {

	}

	@FXML
	void onLeaveGroup(ActionEvent event) {
		chatClient.leaveGroup();
		try {
			chatClient.stopConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML
	void onSend(ActionEvent event) {

		Payload payload = new Payload();
		String text = textField.getText();
		payload.setAuthor(userName);
		payload.setText(text);
		try {
			chatClient.startConnection(getServerIp(), getServerPort());
			chatClient.sendMessage(payload);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		textField.clear();
	}

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		vectorClock = new VectorClock();
		System.out.println("Staring");
		try {
			prop = new LoadProperties().readProperties();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		multicast = prop.getProperty("MULTICAST_GROUP");
		port = Integer.parseInt(prop.getProperty("MULTICAST_PORT"));
		sender = new MulticastSender(multicast, port);
		try {
			receiver = new MulticastReceiver(multicast, port, messageProcessor);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	MessageProcessorIF messageProcessor = new MessageProcessorIF() {
		public void processMessage(BaseMessage message) {
			logger.info(String.format("Receive message from type %s", message.getMessageType()));
			switch (message.getMessageType()) {
			case SERVER_RESPONSE:
				logger.info(String.format("Connection details %s: %s", message.getParticipant().getAddr(),
						message.getParticipant().getPort()));

				try {
					Participant participant = ((MessageObject) message).getParticipant();
					setServerIp(participant.getAddr());
					setServerPort(participant.getPort());
//					chatClient.setServerIp(serverIp);
//					chatClient.setServerPort(serverPort);

				} catch (Exception e) {
					logger.error(String.format("Somthing went wrong sending connection details: %s", e));
				}
				break;
			case CHAT_MESSAGE:
				final Message chatMessage = (Message) message;
				// push first received message
				Platform.runLater(new Runnable() {
//
					public void run() {

				Payload payload = chatMessage.getPayload();

				logger.info(String.format("Receive message from type %s %s", payload.getAuthor(), payload.getText()));

				try {
					String process;
					int seen, sent;
					// check if the sender is different from current process
					logger.info("Received message " + chatMessage.toString());

					VectorClock piggybackedVectorClock = chatMessage.getPiggybackedVectorClock();
					Iterator<String> it = piggybackedVectorClock.getProcessIds().iterator();

					// compare local and received vector clock
					while (it.hasNext()) {
						process = it.next();
						seen = vectorClock.get(process);
						sent = piggybackedVectorClock.get(process);
						if (seen <= sent - 1) {
							// if vector clocks do not agree, then pull messages
							logger.info("Lost messages " + (seen + 1) + " to " + (sent - 1) + " from " + process);
							for (int messageId = seen + 1; messageId < sent; messageId++) {
								Message lostMes = MessageHandler.requestMessage(getServerIp(), getServerPort(),
										messageId);
								listView.getItems().add(String.format("%s: %s %s", lostMes.getPayload().getAuthor(),
										lostMes.getPayload().getText(), lostMes.getReceiveDate()));
							}
							listView.getItems().add(String.format("%s: %s %s", chatMessage.getPayload().getAuthor(),
									chatMessage.getPayload().getText(), chatMessage.getReceiveDate()));

						}
					}
					// merge local and received vector clocks
					vectorClock = VectorClock.mergeClocks(piggybackedVectorClock, vectorClock);
				} catch (UnknownHostException e) {
					logger.info("UnknownHostException in CausalHandler Thread: " + e.getMessage());
				} catch (IOException e) {
					logger.info("IOException in CausalHandler Thread " + e);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					}
				});
				break;
			case RESPONSE_LOST_MESSAGE:
				break;
			default:
				break;
			}
		}
	};

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

}
