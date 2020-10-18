package tictactoe;

public class GameException extends Exception {
    public GameException() { super(); }
    public GameException(String message) { super(message); }
    public GameException(String message, Throwable t) { super(message, t); }
}
