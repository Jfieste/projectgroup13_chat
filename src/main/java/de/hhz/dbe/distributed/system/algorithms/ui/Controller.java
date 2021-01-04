package de.hhz.dbe.distributed.system.algorithms.ui;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import de.hhz.dbe.distributed.system.client.Client;
import de.hhz.dbe.distributed.system.message.BaseMessage;
import de.hhz.dbe.distributed.system.message.MessageObject;
import de.hhz.dbe.distributed.system.message.MessageType;
import de.hhz.dbe.distributed.system.message.Payload;
import de.hhz.dbe.distributed.system.utils.LoadProperties;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

public class Controller {

	@FXML
	private MenuItem enterGroup;

	@FXML
	private MenuItem leaveGroup;

	@FXML
	private MenuItem editName;

	@FXML
	private MenuItem helpMenu;

	@FXML
	private TextArea textArea;

	@FXML
	private TextField textField;

	@FXML
	private Label autorName;

	@FXML
	private Button sendMessage;

	private Client chatClient;
	private BaseMessage conMsg;
	private Payload payload;
	private String userName;
	TextInputDialog dialog = new TextInputDialog("");

	@FXML
	void onEditName(ActionEvent event) {

	}

	@FXML
	void onEnterGroup(ActionEvent event) {
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Your name: " + result.get());
	}


	@FXML
	void onHelp(ActionEvent event) {

	}

	@FXML
	void onLeaveGroup(ActionEvent event) {
		chatClient.leaveGroup();
	}

	@FXML
	void onSend(ActionEvent event) {
		payload = new Payload();
		String text = textField.getText();
		payload.setAuthor(userName);
		payload.setText(text);
		try {
			chatClient.sendMessage(payload);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		Properties prop;

		try {
			prop = new LoadProperties().readProperties();
			String multicast = prop.getProperty("MULTICAST_GROUP");
			int port = Integer.parseInt(prop.getProperty("MULTICAST_PORT"));
			chatClient = new Client(multicast, port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
