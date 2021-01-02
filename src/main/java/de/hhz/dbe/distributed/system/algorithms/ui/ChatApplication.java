package de.hhz.dbe.distributed.system.algorithms.ui;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ChatApplication  extends Application {

    @Override
    public void start(Stage stage) throws IOException {
//        String javaVersion = System.getProperty("java.version");
//        String javafxVersion = System.getProperty("javafx.version");
//        Label l = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/application.fxml"));
        Parent content = loader.load(); 
//        Scene scene = new Scene(new StackPane(l), 640, 480);
//        stage.setScene(scene);
//        stage.show();
        Scene scene = new Scene(content);

        // set up the stage
        stage.setTitle("Orbit the Camera Around an Object Sample");
        stage.setWidth(800);
        stage.setHeight(700);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}
