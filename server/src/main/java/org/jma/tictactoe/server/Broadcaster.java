package org.jma.tictactoe.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

class Broadcaster {
    private DatagramSocket socket;
    private int port;

    Broadcaster(int port) {
        this.port = port;
    }

    void start() throws SocketException {
        socket = new DatagramSocket();
        socket.setBroadcast(true);
        System.out.println("Broadcasting on " + port);

    }

    void send(byte[] data) throws IOException {
        DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName("255.255.255.255"), port);
        socket.send(packet);
    }
}
