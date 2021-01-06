package de.hhz.dbe.distributed.system.algorithms.ui;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hhz.dbe.distributed.system.client.Client;
import de.hhz.dbe.distributed.system.client.Participant;
import de.hhz.dbe.distributed.system.message.BaseMessage;
import de.hhz.dbe.distributed.system.message.Message;
import de.hhz.dbe.distributed.system.message.MessageObject;
import de.hhz.dbe.distributed.system.message.MessageProcessorIF;
import de.hhz.dbe.distributed.system.message.MessageType;
import de.hhz.dbe.distributed.system.message.Payload;
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
import javafx.scene.control.TextArea;
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
		System.out.println("Your name: " + autorName);
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
		Thread rt = new Thread(receiver);
		rt.start();
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
				Payload payload = chatMessage.getPayload();
				Platform.runLater(new Runnable() {
					
					public void run() {
						// TODO Auto-generated method stub
						listView.getItems().add(String.format("%s: %s", chatMessage.getPayload().getAuthor(),
								chatMessage.getPayload().getText()));
						
					}
				});
				logger.info(String.format("Receive message from type %s %s", payload.getAuthor(), payload.getText()));
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
