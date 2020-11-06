package tictactoe;

public interface TicTacToe {
    int DEFAULT_PORT = 6907;
    /**
     * set a piece on the board
     * @param piece to be placed on board
     * @param position
     * @return true if won, false otherwise
     * @throws GameException postion outside board - or position not empty
     * @throws StatusException not in status play
     */
    boolean set(TicTacToePiece piece, TicTacToeBoardPosition position)
            throws GameException, StatusException;
}
