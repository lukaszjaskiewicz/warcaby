package core;

public class Board {
    private int[][] piecePlacement;
    private int numberWhitePieces;
    private int numberBlackPieces;

    public Board() {
        this.piecePlacement = new int[8][8];
    }

    public int[][] getPiecePlacement() {
        return piecePlacement;
    }

    public int getNumberWhitePieces() {
        return numberWhitePieces;
    }

    public int getNumberBlackPieces() {
        return numberBlackPieces;
    }

    public void setNumberWhitePieces(int number) {
        this.numberWhitePieces = number;
    }

    public void setNumberBlackPieces(int number) {
        this.numberBlackPieces = number;
    }

    public void setPiecePlacement(int [][] piecePlacement){this.piecePlacement = piecePlacement;}
}
