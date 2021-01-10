package de.hhz.dbe.distributed.system.ui;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
import de.hhz.dbe.distributed.system.message.Request;
import de.hhz.dbe.distributed.system.message.VectorClock;
import de.hhz.dbe.distributed.system.multicast.MulticastReceiver;
import de.hhz.dbe.distributed.system.multicast.MulticastSender;
import de.hhz.dbe.distributed.system.utils.LoadProperties;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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
	private BaseMessage conMsg = new MessageObject(MessageType.JOIN_MESSAGE);
	private String userName;
	private MulticastSender sender;
	private MulticastReceiver receiver;
	private TextInputDialog dialog = new TextInputDialog("");
	private Properties prop;
	private String multicast;
	private int port;
	private VectorClock vectorClock;
	private int retries = 3;
	private int count = 0;
	private Thread receiverThread;

	@FXML
	void onEditName(ActionEvent event) {

	}

	@FXML
	void onEnterGroup(ActionEvent event) {
		final MessageObject reqMsg = new MessageObject(MessageType.REQUEST_MESSAGES);
		chatClient = new Client(sender, receiver);
		receiverThread.start();
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
			Platform.runLater(new Runnable() {
				public void run() {

					final ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

					exec.schedule(new Runnable() {
						public void run() {
							try {
								MessageObject m = (MessageObject) MessageHandler.requestMessage(getServerIp(),
										getServerPort(), reqMsg);
								for (int i = 0; i < m.getMessgaes().size(); i++) {
									vectorClock = VectorClock.mergeClocks(causalityHandling(m.getMessgaes().get(i)),
											vectorClock);
								}
								textField.setEditable(true);
							} catch (Exception e) {
								logger.debug("Error transmitting message: " + e.getMessage());
							}

						}
					}, 5, TimeUnit.SECONDS);

				}
			});

		} catch (Exception e) {
			logger.debug("Error joining the group: " + e.getMessage());
		}
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
			e.printStackTrace();
		}
	}

	@FXML
	void onSend(ActionEvent event) {

		final Payload payload = new Payload();
		String text = textField.getText();
		payload.setAuthor(userName);
		payload.setText(text);
		Platform.runLater(new Runnable() {

			public void run() {

				boolean reDo = true;
				while (reDo) {
					try {
						chatClient.startConnection(getServerIp(), getServerPort());
						MessageObject msg = chatClient.sendMessage(payload);
						if (msg == null) {
							reDo = false;
							textField.clear();
						} else if (msg.getMessageType() == MessageType.NEW_MASTER_ELECTED) {
							try {
								chatClient.joinGroup(conMsg);
							} catch (Exception e1) {
								logger.error(String.format("Couldn´t contact the leader"));
							}
						}
					} catch (IOException e) {
						logger.info(String.format("Retrying to contact leader"));
						if (++count == retries) {
							try {
								chatClient.joinGroup(conMsg);
							} catch (Exception e1) {
								logger.error(String.format("Couldn´t contact the leader"));
							}
						} else if (count > 4) {
							logger.error("No server is avalaible");
							reDo = false;
							Alert alert = new Alert(Alert.AlertType.ERROR);
							alert.setTitle("Error");
							alert.setHeaderText("Connection error");
							alert.setContentText("No server is avalaible");
							alert.showAndWait();
						}
					} catch (InterruptedException e) {
						logger.debug("Interrupted error: " + e.getMessage());
					} catch (ClassNotFoundException e) {
						logger.debug("Class not found: " + e.getMessage());
					}

				}
			}
		});
	}

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		logger.info("Start Chat application...");
		vectorClock = new VectorClock();
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
			receiverThread = new Thread(receiver);
		} catch (IOException e) {
			logger.debug("Error initializing the MulticastReceiver: " + e.getMessage());
		}
		textField.setEditable(false);
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

				} catch (Exception e) {
					logger.error(String.format("Somthing went wrong sending connection details: %s", e));
				}
				break;
			case CHAT_MESSAGE:
				final Message chatMessage = (Message) message;
				Platform.runLater(new Runnable() {
					public void run() {
						Payload payload = chatMessage.getPayload();

						logger.info(String.format("Receive message from type %s %s", payload.getAuthor(),
								payload.getText()));

						try {
							VectorClock piggybackedVectorClock = causalityHandling(chatMessage);
							// merge local and received vector clocks
							vectorClock = VectorClock.mergeClocks(piggybackedVectorClock, vectorClock);
						} catch (UnknownHostException e) {
							logger.info("UnknownHostException in Controller: " + e.getMessage());
						} catch (IOException e) {
							logger.info("IOException in Controller " + e);
						} catch (ClassNotFoundException e) {
							logger.info("ClassNotFoundException in Controller " + e);
						}
					}

				});
				break;
			default:
				break;
			}
		}
	};

	private VectorClock causalityHandling(final Message chatMessage) throws IOException, ClassNotFoundException {
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

				String pattern = "EEEEE dd-MM-yy HH:mm:ss";
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
				// if vector clocks do not agree, then pull messages
				for (int messageId = seen + 1; messageId < sent; messageId++) {
					logger.info("Lost messages " + messageId + " from " + process);
					Request req = new Request(messageId);
					Message lostMes = (Message) MessageHandler.requestMessage(getServerIp(), getServerPort(), req);
					listView.getItems().add(String.format("%s =>   %s  ||   %s", lostMes.getPayload().getAuthor(),
							lostMes.getPayload().getText(), simpleDateFormat.format(lostMes.getReceiveDate())));
				}
				listView.getItems().add(String.format("%s =>    %s  ||   %s", chatMessage.getPayload().getAuthor(),
						chatMessage.getPayload().getText(), simpleDateFormat.format(chatMessage.getReceiveDate())));

			}
		}
		return piggybackedVectorClock;
	}

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
