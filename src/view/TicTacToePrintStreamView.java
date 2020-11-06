package view;

import tictactoe.TicTacToePiece;

import java.io.IOException;
import java.io.PrintStream;

public class TicTacToePrintStreamView implements PrintStreamView {
    private final TicTacToePiece[][] board;

    public TicTacToePrintStreamView(TicTacToePiece[][] board) {
        this.board = board;
    }

    @Override
    public void print(PrintStream ps) throws IOException {
        for(int v = 2; v > -1; v--) {
            ps.print(v + " ");
            for(int h = 0; h < 3; h++) {
                TicTacToePiece piece = this.board[h][v];
                if(piece == null) { System.out.print(" - "); }
                else {
                    switch (piece) {
                        case O: ps.print(" O "); break;
                        case X: ps.print(" X "); break;
                    }
                }
            }
            ps.print("\n");
        }
        ps.println("   A  B  C");
    }
}
