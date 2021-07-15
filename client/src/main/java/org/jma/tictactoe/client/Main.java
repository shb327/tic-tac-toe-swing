package org.jma.tictactoe.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {
    public static Stage stage;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Main.stage = stage;
        Parent root = FXMLLoader.load(Objects.requireNonNull(Main.class.getClassLoader().getResource("main.fxml")));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getClassLoader().getResource("style.css")).toExternalForm());

        stage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
        stage.setTitle("TicTacToe!");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }
}
