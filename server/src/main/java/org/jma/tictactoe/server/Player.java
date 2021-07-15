package org.jma.tictactoe.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Player {
    private int id;
    private Socket socket;
    private DataInputStream inputFromUser;
    private DataOutputStream outputToUser;

    Player(int id, Socket socket) throws IOException {
        this.id = id;
        this.socket = socket;
        outputToUser = new DataOutputStream(socket.getOutputStream());
        inputFromUser = new DataInputStream(socket.getInputStream());
    }

    int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    Socket getSocket() {
        return socket;
    }

    DataInputStream getInputFromUser() {
        return inputFromUser;
    }

    DataOutputStream getOutputToUser() {
        return outputToUser;
    }

    @Override
    public String toString() {
        return String.format("Player #%d (%s)", id, socket.getInetAddress().toString());
    }
}
