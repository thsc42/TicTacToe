package tictactoe;

public interface TicTacToe {
    /**
     * Pick a symbol
     * @param userName user name
     * @param wantedSymbol user asks for this symbol. It can be a race condition
     * @return selected symbol
     * @throws GameException both symbols are already taken - it is at least the third attempt in a two player game
     * @throws StatusException can only be called if games hasn't started yet.
     */
    TicTacToePiece pick(String userName, TicTacToePiece wantedSymbol)
            throws GameException, StatusException;


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
