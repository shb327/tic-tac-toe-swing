package org.jma.tictactoe.server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

class Server {
    private AtomicInteger playerIdCounter = new AtomicInteger(1);
    private AtomicInteger gameIdCounter = new AtomicInteger(1);

    private Executor executor = Executors.newCachedThreadPool();
    private Map<Integer, Player> players = new ConcurrentHashMap<>();
    private Map<Integer, Game> games = new ConcurrentHashMap<>();
    private Queue<Player> gameQueue = new LinkedList<>();
    private Broadcaster broadcaster;
    private final int port;

    Server(int port, int broadcasterPort) {
        this.port = port;
        this.broadcaster = new Broadcaster(broadcasterPort);
    }

    @SuppressWarnings("InfiniteLoopStatement")
    void start() {
            try {
                broadcaster.start();
                ServerSocket serverSocket = new ServerSocket(port);
                System.out.println("Server is started on port: " + port);
                while(true) {
                    try {
                        Socket socket = serverSocket.accept();
                        Player player = new Player(playerIdCounter.getAndIncrement(), socket);
                        players.put(player.getId(), player);
                        PlayerHandler playerHandler = new PlayerHandler(player, this);
                        playerHandler.start();
                        System.out.printf("%s has connected!\n", player.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                System.err.println("unable to open port");
                throw new RuntimeException(e);
            }
    }

    Executor getExecutor() {
        return executor;
    }

    Map<Integer, Player> getPlayers() {
        return players;
    }

    Queue<Player> getGameQueue() {
        return gameQueue;
    }

    Map<Integer, Game> getGames() {
        return games;
    }

    AtomicInteger getGameIdCounter() {
        return gameIdCounter;
    }

    Broadcaster getBroadcaster() {
        return broadcaster;
    }
}
