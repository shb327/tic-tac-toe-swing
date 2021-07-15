package org.jma.tictactoe.server;

class Game {
    private int id;
    private Player player1;
    private Player player2;
    private int turnId;
    private String state;
    private byte[][] map = new byte[3][3];

    Game(int id, Player player1, Player player2) {
        this.id = id;
        this.player1 = player1;
        this.player2 = player2;
        this.state = "STARTED";
        this.turnId = player1.getId();
    }

    int getId() {
        return id;
    }

    Player getPlayer1() {
        return player1;
    }

    Player getPlayer2() {
        return player2;
    }

    int getTurnId() {
        return turnId;
    }

    String getState() {
        return state;
    }

    byte[][] getMap() {
        return map;
    }

    void setTurnId(int turnId) {
        this.turnId = turnId;
    }

    void setState(String state) {
        this.state = state;
    }
}
