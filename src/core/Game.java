package core;

import java.util.*;

public class Game {
    private int id;
    private Board board;
    private int[] playersId;
    private Vector<String> chat;
    private boolean ongoing;

    public Game(int id, int playerId1, int playerId2) {
        this.id = id;
        this.playersId = new int[2];
        playersId[0] = playerId1;
        playersId[1] = playerId2;
        this.chat = new Vector<String>();
        this.board = new Board();
    }

    public void movePiece(int[] coordinatesBefore, int[] coordinatesAfter) {

    }

    public int getId() {
        return id;
    }

    public int[] getPlayersId() {
        return playersId;
    }

    public boolean getOngoing() {
        return ongoing;
    }

    public Vector<String> getChat() {
        return chat;
    }

    public void setBoard(Board board) { this.board = board; }

    public Board getBoard() { return board; }
}
