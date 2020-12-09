package tictactoe;

import java.io.*;

import static tictactoe.TicTacToeTCPProtocolEngine.METHOD_SET;

public class TicTacToeProtocolEngine {
    public static final int SYMBOL_0 = 0;
    public static final int SYMBOL_X = 1;

    void serializeSet(TicTacToePiece piece, TicTacToeBoardPosition position, OutputStream os) throws GameException {
        DataOutputStream dos = new DataOutputStream(os);

        // write method id
        try {
            dos.writeInt(METHOD_SET);
            // serialize symbol
            dos.writeInt(this.getIntValue4Piece(piece));
            // serialize position coordinates
            dos.writeUTF(position.getSCoordinate());
            dos.writeInt(position.getICoordinate());
        } catch (IOException e) {
            throw new GameException(e.getLocalizedMessage());
        }
    }

    SetCommand deserializeSet(InputStream is) throws GameException, IOException {
        DataInputStream dis = new DataInputStream(is);
        // read serialized symbol
        int symbolInt = dis.readInt();
        // convert back to piece
        TicTacToePiece piece = this.getPieceFromIntValue(symbolInt);
        // read s coordinate
        String sCoordinate = dis.readUTF();
        // read i coordinate
        int iCoordinate = dis.readInt();

        TicTacToeBoardPosition position = new TicTacToeBoardPosition(sCoordinate, iCoordinate);

        // call method - but no need to keep result - it isn't sent back.
        return new SetCommand(piece, position);
    }

    private TicTacToePiece getPieceFromIntValue(int symbolInt) throws GameException {
        switch (symbolInt) {
            case SYMBOL_0: return TicTacToePiece.O;
            case SYMBOL_X: return TicTacToePiece.X;
            default: throw new GameException("unknown symbol: " + symbolInt);
        }
    }

    private int getIntValue4Piece(TicTacToePiece piece) throws GameException {
        switch (piece) {
            case O: return SYMBOL_0;
            case X: return SYMBOL_X;
            default: throw new GameException("unknown symbol: " + piece);
        }
    }
}
