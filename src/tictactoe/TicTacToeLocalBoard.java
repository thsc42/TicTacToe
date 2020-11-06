package tictactoe;

import javax.swing.event.ChangeListener;

public interface TicTacToeLocalBoard extends TicTacToe {

    /**
     * Pick a symbol - changed semantic during development process - it is only a local call.
     * Symbols are chosen based on a negotiation protocol
     * @param userName user name
     * @param wantedSymbol user asks for this symbol. It can be a race condition
     * @return selected symbol
     * @throws GameException both symbols are already taken - it is at least the third attempt in a two player game
     * @throws StatusException can only be called if games hasn't started yet.
     */
    TicTacToePiece pick(String userName, TicTacToePiece wantedSymbol)
            throws GameException, StatusException;

    boolean set(TicTacToeBoardPosition position) throws GameException, StatusException;

    /**
     *
     * @return game status
     */
    Status getStatus();

    /**
     * @return if active - can set a piece, false otherwise
     */
    boolean isActive();

    /**
     * @return true if won, false otherwise
     */
    boolean hasWon();

    /**
     * @return true if lost, false otherwise
     */
    boolean hasLost();

    /**
     * Subscribe for changes
     * @param changeListener
     */
    void subscribeChangeListener(LocalBoardChangeListener changeListener);
}
