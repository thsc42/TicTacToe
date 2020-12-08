package tictactoe;

class SetCommand {
    private final TicTacToePiece piece;
    private final TicTacToeBoardPosition position;

    public SetCommand(TicTacToePiece piece, TicTacToeBoardPosition position) {
        this.piece = piece;
        this.position = position;
    }

    TicTacToePiece getPiece() { return this.piece; }

    TicTacToeBoardPosition getPosition() { return this.position; }
}
