package tictactoe;

import java.io.*;

public class TicTacToeProtocolEngine implements TicTacToe {
    private final OutputStream os;
    private final InputStream is;
    private final TicTacToe gameEngine;

    public static final int METHOD_PICK = 0;
    public static final int METHOD_SET = 1;

    public static final int SYMBOL_0 = 0;
    public static final int SYMBOL_X = 1;

    public TicTacToeProtocolEngine(InputStream is, OutputStream os, TicTacToe gameEngine) {
        this.is = is;
        this.os = os;
        this.gameEngine = gameEngine;
    }

    @Override
    public TicTacToePiece pick(String userName, TicTacToePiece wantedSymbol) throws GameException, StatusException {
        DataOutputStream dos = new DataOutputStream(this.os);

        try {
            // write method id
            dos.writeInt(METHOD_PICK);
            // write user name
            dos.writeUTF(userName);
            // serialize piece symbol
            dos.writeInt(this.getIntValue4Piece(wantedSymbol));

            /*
            // read result
            DataInputStream dis = new DataInputStream(this.is);
            int symbolInt= dis.readInt();

            return this.getPieceFromIntValue(symbolInt);
             */
            return wantedSymbol; // TODO

        } catch (IOException e) {
            throw new GameException("could not serialize command", e);
        }
    }

    private void deserializePick() throws GameException {
        DataInputStream dis = new DataInputStream(this.is);
        TicTacToePiece wantedSymbol = null;
        try {
            // read userName
            String userName = dis.readUTF();
            // read serialized symbol
            int symbolInt = dis.readInt();
            // convert to symbol
            wantedSymbol = this.getPieceFromIntValue(symbolInt);

            TicTacToePiece piece = this.gameEngine.pick(userName, wantedSymbol);

            /*
            // write result
            DataOutputStream dos = new DataOutputStream(this.os);
            dos.writeInt(this.getIntValue4Piece(piece));
             */
        } catch (IOException | StatusException e) {
            throw new GameException("could not deserialize command", e);
        }
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

    @Override
    public boolean set(TicTacToePiece piece, TicTacToeBoardPosition position) throws GameException, StatusException {
        DataOutputStream dos = new DataOutputStream(this.os);

        try {
            // write method id
            dos.writeInt(METHOD_SET);
            // serialize symbol
            dos.writeInt(this.getIntValue4Piece(piece));
            // serialize position coordinates
            dos.writeUTF(position.getSCoordinate());
            dos.writeInt(position.getICoordinate());

        } catch (IOException e) {
            throw new GameException("could not serialize command", e);
        }

        return false;
    }

    private void deserializeSet() throws GameException {
        DataInputStream dis = new DataInputStream(this.is);
        try {
            // read serialized symbol
            int symbolInt = dis.readInt();
            // convert back to piece
            TicTacToePiece piece = this.getPieceFromIntValue(symbolInt);
            // read s coordinate
            String sCoordinate = dis.readUTF();
            // read i coordinate
            int iCoordinate = dis.readInt();

            TicTacToeBoardPosition position = new TicTacToeBoardPosition(sCoordinate, iCoordinate);

            // call method
            boolean won = this.gameEngine.set(piece, position);
        } catch (IOException | StatusException e) {
            throw new GameException("could not deserialize command", e);
        }
    }

    public void read() throws GameException {
        DataInputStream dis = new DataInputStream(this.is);

        // read method id
        try {
            int commandID = dis.readInt();
            switch (commandID) {
                case METHOD_PICK: this.deserializePick(); break;
                case METHOD_SET: this.deserializeSet(); break;
                default: throw new GameException("unknown method id: " + commandID);
            }
        } catch (IOException e) {
            throw new GameException("could not deserialize command", e);
        }
    }
}
