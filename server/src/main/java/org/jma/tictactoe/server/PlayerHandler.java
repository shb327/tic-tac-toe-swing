package org.jma.tictactoe.server;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

class PlayerHandler {
    private final Player player;
    private final Server server;

    PlayerHandler(Player player, Server server) {
        this.player = player;
        this.server = server;
    }

    void start() {
        server.getExecutor().execute(() -> {
            while (!player.getSocket().isClosed()) {
                try {
                    String command = player.getInputFromUser().readUTF();
                    System.out.printf("%s: %s\n", player.toString(), command);
                    switch (command) {
                        case "PLAY":
                            Player poll = server.getGameQueue().poll();
                            if (poll != null) {
                                startGame(poll, player);
                            } else {
                                server.getGameQueue().add(player);
                            }
                            break;
                        case "LOGOUT":
                            logout();
                            break;
                        case "LIST":
                            player.getOutputToUser().writeUTF("LIST");
                            player.getOutputToUser().writeInt(server.getPlayers().size());
                            for (Player p : server.getPlayers().values()) {
                                player.getOutputToUser().writeInt(p.getId());
                                player.getOutputToUser().writeUTF(p.getSocket().getInetAddress().toString());
                            }
                            player.getOutputToUser().flush();
                            break;
                        case "SET":
                            byte x = player.getInputFromUser().readByte();
                            byte y = player.getInputFromUser().readByte();
                            int gameId = player.getInputFromUser().readInt();
                            setMark(player, x, y, gameId);
                            break;
                        default:
                            System.err.printf("%s has entered invalid command '%s'\n", player.toString(), command);
                    }
                } catch (IOException e) {
                    logout();
                }
            }
        });
    }


    private void logout() {
        System.out.printf("%s has disconnected\n", player.toString());
        try {
            if (!player.getSocket().isClosed()) {
                player.getSocket().close();
            }
            server.getGameQueue().remove(player);
            server.getPlayers().remove(player.getId());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    private void startGame(Player p1, Player p2) throws IOException {
        Game game = new Game(server.getGameIdCounter().getAndIncrement(), p1, p2);
        server.getGames().put(game.getId(), game);
        sendNewGame(game);
    }

    private void setMark(Player player, byte x, byte y, int gameId) throws IOException {
        Game game = server.getGames().get(gameId);
        game.setState("PLAYING");
        if (game != null) {
            if (game.getTurnId() == player.getId() && game.getMap()[y][x] == 0) {
                if (game.getTurnId() == game.getPlayer1().getId()) {
                    game.getMap()[y][x] = 1;
                    game.setTurnId(game.getPlayer2().getId());
                } else {
                    game.getMap()[y][x] = 2;
                    game.setTurnId(game.getPlayer1().getId());
                }
                if (checkCondition(game, (byte) 1)) {
                    game.setState("END " + game.getPlayer1().getId());
                    server.getGames().remove(gameId);
                } else if (checkCondition(game, (byte) 2)) {
                    game.setState("END " + game.getPlayer2().getId());
                    server.getGames().remove(gameId);
                } else if (isFull(game.getMap())) {
                    game.setState("END 0");
                    server.getGames().remove(gameId);
                }
                sendPlayersUpdate(game);
            }
        } else {
            System.err.println("game not found");
        }
    }

    private boolean checkCondition(Game game, byte player) {
        for (int i = 0; i < 3; i++) {
            if (game.getMap()[i][0] == player && game.getMap()[i][1] == player && game.getMap()[i][2] == player) {
                return true;
            }
            if (game.getMap()[0][i] == player && game.getMap()[1][i] == player && game.getMap()[2][i] == player) {
                return true;
            }
        }
        return (game.getMap()[0][0] == player && game.getMap()[1][1] == player && game.getMap()[2][2] == player) ||
                (game.getMap()[2][0] == player && game.getMap()[1][1] == player && game.getMap()[0][2] == player);
    }

    private boolean isFull(byte[][] map) {
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                if (map[x][y] == 0) return false;
            }
        }
        return true;
    }

    private void sendPlayersUpdate(Game game) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        out.writeUTF("UPDATE");
        out.writeInt(game.getId());
        out.writeUTF(game.getState());
        out.writeInt(game.getPlayer1().getId());
        out.writeInt(game.getPlayer2().getId());
        out.writeInt(game.getTurnId());
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                out.writeByte(game.getMap()[x][y]);
            }
        }
        byte[] data = baos.toByteArray();
        sendUpdate(game.getPlayer1(), data);
        sendUpdate(game.getPlayer2(), data);
        server.getBroadcaster().send(data);
    }

    private void sendUpdate(Player player, byte[] data) throws IOException {
        player.getOutputToUser().write(data);
        player.getOutputToUser().flush();
    }

    private void sendNewGame(Game game) throws IOException {
        game.getPlayer1().getOutputToUser().writeUTF("NEWGAME");
        game.getPlayer1().getOutputToUser().writeInt(game.getId());
        game.getPlayer1().getOutputToUser().writeInt(game.getPlayer1().getId());
        game.getPlayer1().getOutputToUser().writeInt(game.getPlayer2().getId());
        game.getPlayer1().getOutputToUser().writeInt(game.getTurnId());
        game.getPlayer1().getOutputToUser().flush();

        game.getPlayer2().getOutputToUser().writeUTF("NEWGAME");
        game.getPlayer2().getOutputToUser().writeInt(game.getId());
        game.getPlayer2().getOutputToUser().writeInt(game.getPlayer2().getId());
        game.getPlayer2().getOutputToUser().writeInt(game.getPlayer1().getId());
        game.getPlayer2().getOutputToUser().writeInt(game.getTurnId());
        game.getPlayer2().getOutputToUser().flush();
    }

    private void sendStrings(Player player, String... messages) throws IOException {
        for (String message : messages) {
            player.getOutputToUser().writeUTF(message);
        }
        player.getOutputToUser().flush();
    }
}

