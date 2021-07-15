package org.jma.tictactoe.client;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

public class Controller {
    private static final String PATTERN = "^([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}):([0-9]{1,5})$";
    public Button connectButton;
    public Button listButton;
    public ProgressIndicator connectIndicator;
    public TextField ipTextField;
    public Label player1;
    public Label player2;

    public ImageView img11;
    public ImageView img12;
    public ImageView img13;
    public ImageView img21;
    public ImageView img22;
    public ImageView img23;
    public ImageView img31;
    public ImageView img32;
    public ImageView img33;
    public ImageView field;
    public AnchorPane statusLabels;
    private Thread socketThread;
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private int gameId;
    private int turn;
    private int self;

    public void initialize() {
        field.setImage(new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("field.png"))));
        img11.setOnMouseClicked((e) -> onCellClicked(0, 0));
        img12.setOnMouseClicked((e) -> onCellClicked(0, 1));
        img13.setOnMouseClicked((e) -> onCellClicked(0, 2));
        img21.setOnMouseClicked((e) -> onCellClicked(1, 0));
        img22.setOnMouseClicked((e) -> onCellClicked(1, 1));
        img23.setOnMouseClicked((e) -> onCellClicked(1, 2));
        img31.setOnMouseClicked((e) -> onCellClicked(2, 0));
        img32.setOnMouseClicked((e) -> onCellClicked(2, 1));
        img33.setOnMouseClicked((e) -> onCellClicked(2, 2));
        connectButton.setOnAction(e -> {
            if (!ipTextField.getText().isEmpty()) {
                connect(ipTextField.getText());
            }
        });
        listButton.setOnAction(e -> {
            requestList();
        });
    }

    private void requestList() {
        try {
            output.writeUTF("LIST");
            output.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void onCellClicked(int x, int y) {
        if (turn == self && socket != null) {
            try {
                output.writeUTF("SET");
                output.writeByte(x);
                output.writeByte(y);
                output.writeInt(gameId);
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void connect(String addr) {
        if (!addr.matches(PATTERN)) return;
        connectButton.setDisable(true);
        String[] split = addr.split(":");
        try {
            socket = new Socket(split[0], Integer.parseInt(split[1]));
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            socketThread = new Thread(() -> {
                while (!socket.isClosed()) {
                    try {
                        String command = input.readUTF();
                        System.out.println(command);
                        switch (command) {
                            case "LIST":
                                showList();
                                break;
                            case "NEWGAME":
                                startGame(input.readInt(), input.readInt(), input.readInt(), input.readInt());
                                break;
                            case "UPDATE":
                                int gameId = input.readInt();
                                String state = input.readUTF();
                                int p1 = input.readInt();
                                int p2 = input.readInt();
                                int turnId = input.readInt();
                                byte[][] map = new byte[3][3];
                                for (int x = 0; x < 3; x++) {
                                    for (int y = 0; y < 3; y++) {
                                        map[x][y] = input.readByte();
                                    }
                                }
                                update(gameId, state, p1, p2, turnId, map);
                                break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        try {
                            output.writeUTF("DISCONNECT");
                            output.close();
                        } catch (IOException ignored) {

                        }
                        System.out.println("disconnect");
                        System.exit(1);
                    }
                }
            });
            socketThread.start();
            listButton.setDisable(false);
            output.writeUTF("PLAY");
            output.flush();
            connectIndicator.setProgress(-1);
            connectIndicator.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showList() throws IOException {
        int count = input.readInt();
        ObservableList<GameInfo> games = FXCollections.observableArrayList();
        for (int i = 0; i < count; i++) {
            games.add(new GameInfo(input.readInt(), input.readUTF()));
        }

        TableView<GameInfo> table = new TableView<>(games);
        TableColumn<GameInfo, Integer> idColumn = new TableColumn<>("Id");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        table.getColumns().add(idColumn);

        TableColumn<GameInfo, Integer> addrColumn = new TableColumn<>("Address");
        addrColumn.setCellValueFactory(new PropertyValueFactory<>("details"));
        table.getColumns().add(addrColumn);
        table.setPrefHeight(200);
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("List of players: ");
            alert.setTitle("List");
            alert.getDialogPane().setContent(table);
            alert.showAndWait();
        });
    }

    private void update(int gameId, String state, int p1, int p2, int turnId, byte[][] map) {
        Platform.runLater(() -> {
            try {
                if (this.gameId == gameId) {
                    this.turn = turnId;
                    for (int x = 0; x < 3; x++) {
                        for (int y = 0; y < 3; y++) {
                            setValue(x, y, map[y][x]);
                        }
                    }
                    if (state.matches("END(\\s\\d+)?")) {
                        int win = Integer.parseInt(state.split(" ")[1]);
                        if (win == self) {
                            showModal("YOU WON!", "giphy.gif");
                        } else if (win == 0) {
                            showModal("DRAW", "giphy3.gif");
                        } else {
                            showModal("YOU LOST!", "giphy2.gif");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void showModal(String text, String gif) throws IOException {
        Platform.runLater(() -> {
            Pane dialog = null;
            try {
                dialog = (Pane) FXMLLoader.load(getClass().getClassLoader().getResource("dialog.fxml"));
                ((ImageView) dialog.getChildren().get(1)).setImage(
                        new Image(gif));
                ((Label) ((Pane) dialog.getChildren().get(0)).getChildren().get(0)).setText(text);
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initOwner(Main.stage);
                stage.setScene(new Scene(dialog));
                stage.setTitle("Result");
                stage.sizeToScene();
                stage.showAndWait();
                socket.close();
                socketThread.interrupt();
                Platform.exit();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }

    private void startGame(int gameId, int self, int opponent, int turn) {
        this.self = self;
        this.gameId = gameId;
        this.turn = turn;
        Platform.runLater(() -> {
            player1.setText("Player " + self);
            player2.setText("Player " + opponent);
            connectIndicator.setVisible(false);
            statusLabels.setVisible(true);
        });
    }

    private void setValue(int x, int y, byte value) {
        if (value == 0) return;
        if (x == 0 && y == 0) {
            img11.setImage(new Image(
                    Objects.requireNonNull(getClass().getClassLoader()
                                                   .getResourceAsStream((value == 1 ? "cross" : "circle") + ".png"))));
        }
        if (x == 0 && y == 1) {
            img12.setImage(new Image(
                    Objects.requireNonNull(getClass().getClassLoader()
                                                   .getResourceAsStream((value == 1 ? "cross" : "circle") + ".png"))));
        }
        if (x == 0 && y == 2) {
            img13.setImage(new Image(
                    Objects.requireNonNull(getClass().getClassLoader()
                                                   .getResourceAsStream((value == 1 ? "cross" : "circle") + ".png"))));
        }
        if (x == 1 && y == 0) {
            img21.setImage(new Image(
                    Objects.requireNonNull(getClass().getClassLoader()
                                                   .getResourceAsStream((value == 1 ? "cross" : "circle") + ".png"))));
        }
        if (x == 1 && y == 1) {
            img22.setImage(new Image(
                    Objects.requireNonNull(getClass().getClassLoader()
                                                   .getResourceAsStream((value == 1 ? "cross" : "circle") + ".png"))));
        }
        if (x == 1 && y == 2) {
            img23.setImage(new Image(
                    Objects.requireNonNull(getClass().getClassLoader()
                                                   .getResourceAsStream((value == 1 ? "cross" : "circle") + ".png"))));
        }
        if (x == 2 && y == 0) {
            img31.setImage(new Image(
                    Objects.requireNonNull(getClass().getClassLoader()
                                                   .getResourceAsStream((value == 1 ? "cross" : "circle") + ".png"))));
        }
        if (x == 2 && y == 1) {
            img32.setImage(new Image(
                    Objects.requireNonNull(getClass().getClassLoader()
                                                   .getResourceAsStream((value == 1 ? "cross" : "circle") + ".png"))));
        }
        if (x == 2 && y == 2) {
            img33.setImage(new Image(
                    Objects.requireNonNull(getClass().getClassLoader()
                                                   .getResourceAsStream((value == 1 ? "cross" : "circle") + ".png"))));
        }


    }

    public static class GameInfo {
        private SimpleIntegerProperty id;
        private SimpleStringProperty details;

        GameInfo(int id, String details) {
            this.id = new SimpleIntegerProperty(id);
            this.details = new SimpleStringProperty(details);
        }

        public SimpleIntegerProperty idProperty() {
            return id;
        }

        public SimpleStringProperty detailsProperty() {
            return details;
        }
    }
}

