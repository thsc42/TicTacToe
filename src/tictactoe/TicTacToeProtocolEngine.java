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
            // serialize symbol
            switch (wantedSymbol) {
                case O: dos.writeInt(SYMBOL_0); break;
                case X: dos.writeInt(SYMBOL_X); break;
                default: throw new GameException("unknown symbol: " + wantedSymbol);
            }

        } catch (IOException e) {
            throw new GameException("could not serialize command", e);
        }

        return null; // !! TODO ??
    }

    private void deserializePick() throws GameException {
        DataInputStream dis = new DataInputStream(this.is);
        TicTacToePiece wantedSymbol = null;
        try {
            // read userName
            String userName = dis.readUTF();
            // read serialized symbol
            int symbolInt = dis.readInt();
            switch (symbolInt) {
                case SYMBOL_0: wantedSymbol = TicTacToePiece.O; break;
                case SYMBOL_X: wantedSymbol = TicTacToePiece.X; break;
                default: throw new GameException("unknown symbol: " + wantedSymbol);
            }

            this.gameEngine.pick(userName, wantedSymbol);
        } catch (IOException | StatusException e) {
            throw new GameException("could not deserialize command", e);
        }
    }

    @Override
    public boolean set(TicTacToePiece piece, TicTacToeBoardPosition position) throws GameException, StatusException {
        DataOutputStream dos = new DataOutputStream(this.os);

        try {
            // write method id
            dos.writeInt(METHOD_SET);
            // serialize symbol
            switch (piece) {
                case O: dos.writeInt(SYMBOL_0); break;
                case X: dos.writeInt(SYMBOL_X); break;
                default: throw new GameException("unknown symbol: " + piece);
            }
            dos.writeUTF(position.getSCoordinate());
            dos.writeInt(position.getICoordinate());

        } catch (IOException e) {
            throw new GameException("could not serialize command", e);
        }

        return false;
    }

    private void deserializeSet() throws GameException {
        DataInputStream dis = new DataInputStream(this.is);
        TicTacToePiece piece = null;
        try {
            // read serialized symbol
            int symbolInt = dis.readInt();
            switch (symbolInt) {
                case SYMBOL_0: piece = TicTacToePiece.O; break;
                case SYMBOL_X: piece = TicTacToePiece.X; break;
                default: throw new GameException("unknown symbol: " + piece);
            }
            // read s coordinate
            String sCoordinate = dis.readUTF();
            // read i coordinate
            int iCoordinate = dis.readInt();

            TicTacToeBoardPosition position = new TicTacToeBoardPosition(sCoordinate, iCoordinate);

            // call method
            this.gameEngine.set(piece, position);
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
