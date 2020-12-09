package tictactoe;

import network.GameSessionEstablishedListener;
import network.ProtocolEngine;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static tictactoe.TicTacToeTCPProtocolEngine.METHOD_SET;

public abstract class TicTacToeProtocolEngine implements TicTacToe {
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

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                         oracle creation listener                                      //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////

    private List<GameSessionEstablishedListener> sessionCreatedListenerList = new ArrayList<>();

    public void subscribeGameSessionEstablishedListener(GameSessionEstablishedListener ocListener) {
        this.sessionCreatedListenerList.add(ocListener);
    }

    public void unsubscribeGameSessionEstablishedListener(GameSessionEstablishedListener ocListener) {
        this.sessionCreatedListenerList.remove(ocListener);
    }

    void notifyGamesSessionEstablished(boolean oracle, String partnerName) {
        // call listener
        if (this.sessionCreatedListenerList != null && !this.sessionCreatedListenerList.isEmpty()) {
            for (GameSessionEstablishedListener oclistener : this.sessionCreatedListenerList) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1); // block a moment to let read thread start - just in case
                        } catch (InterruptedException e) {
                            // will not happen
                        }
                        oclistener.gameSessionEstablished(oracle, partnerName);
                    }
                }).start();
            }
        }
    }
}
