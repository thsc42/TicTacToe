package tictactoe;

import network.ProtocolEngine;

import java.io.*;

public class TicTacToeProtocolEngine implements TicTacToe, Runnable, ProtocolEngine {
    private OutputStream os;
    private InputStream is;
    private final TicTacToe gameEngine;

    public static final int METHOD_PICK = 0;
    public static final int METHOD_SET = 1;
    public static final int RESULT_PICK = 2;

    public static final int SYMBOL_0 = 0;
    public static final int SYMBOL_X = 1;

    private Thread protocolThread = null;
    private Thread pickWaitThread = null;
    private TicTacToePiece pickResult;

    /**
     * @deprecated
     * @param is
     * @param os
     * @param gameEngine
     */
    public TicTacToeProtocolEngine(InputStream is, OutputStream os, TicTacToe gameEngine) {
        this.is = is;
        this.os = os;
        this.gameEngine = gameEngine;
    }

    public TicTacToeProtocolEngine(TicTacToe gameEngine) {
        this.gameEngine = gameEngine;
    }

    @Override
    public TicTacToePiece pick(String userName, TicTacToePiece wantedSymbol) throws GameException, StatusException {
        System.out.println("send pick message to other side");
        DataOutputStream dos = new DataOutputStream(this.os);

        try {
            // write method id
            dos.writeInt(METHOD_PICK);
            // write user name
            dos.writeUTF(userName);
            // serialize piece symbol
            dos.writeInt(this.getIntValue4Piece(wantedSymbol));


            // looks good but is not.
            /*
            // read result
            System.out.println("wait for return value");
            DataInputStream dis = new DataInputStream(this.is);
            int symbolInt= dis.readInt();
            System.out.println("back from reading");

            return this.getPieceFromIntValue(symbolInt);
             */

            try {
                this.pickWaitThread = Thread.currentThread();
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                // interrupted
                System.out.println("pick thread back - results arrived");
            }

            // remember - we are not waiting any longer
            this.pickWaitThread = null;

            return this.pickResult;

        } catch (IOException e) {
            throw new GameException("could not serialize command", e);
        }
    }

    private void deserializeResultPick() throws GameException {
        System.out.println("deserialize received pick result message");
        DataInputStream dis = new DataInputStream(this.is);
        TicTacToePiece wantedSymbol = null;
        try {
            // read serialized symbol
            int symbolInt = dis.readInt();
            // convert to symbol
            this.pickResult = this.getPieceFromIntValue(symbolInt);

            // wake up thread
            this.pickWaitThread.interrupt();
        } catch (IOException e) {
            throw new GameException("could not deserialize command", e);
        }
    }

    private void deserializePick() throws GameException {
        System.out.println("deserialize received pick message");
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

            // write result
            System.out.println("going to send return value");
            DataOutputStream dos = new DataOutputStream(this.os);
            dos.writeInt(RESULT_PICK);
            dos.writeInt(this.getIntValue4Piece(piece));
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
    public boolean set(TicTacToePiece piece, TicTacToeBoardPosition position) throws GameException {
        System.out.println("send set message to other side");
        DataOutputStream dos = new DataOutputStream(this.os);

        try {
            // write method id
            dos.writeInt(METHOD_SET);
            // serialize symbol
            dos.writeInt(this.getIntValue4Piece(piece));
            // serialize position coordinates
            dos.writeUTF(position.getSCoordinate());
            dos.writeInt(position.getICoordinate());

            // no change here - no need to transmit result

        } catch (IOException e) {
            throw new GameException("could not serialize command", e);
        }

        return false;
    }

    private void deserializeSet() throws GameException {
        System.out.println("deserialize received set message");
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

            // call method - but no need to keep result - it isn't sent back.
            this.gameEngine.set(piece, position);

        } catch (IOException | StatusException e) {
            throw new GameException("could not deserialize command", e);
        }
    }

    public void read() throws GameException {
        System.out.println("Protocol Engine: read from input stream");
        DataInputStream dis = new DataInputStream(this.is);

        // read method id
        try {
            int commandID = dis.readInt();
            switch (commandID) {
                case METHOD_PICK: this.deserializePick(); break;
                case METHOD_SET: this.deserializeSet(); break;
                case RESULT_PICK: this.deserializeResultPick(); break;
                default: throw new GameException("unknown method id: " + commandID);
            }
        } catch (IOException e) {
            throw new GameException("could not deserialize command", e);
        }
    }

    @Override
    public void run() {
        System.out.println("Protocol Engine started - read");

        try {
            while(true) {
                    this.read();
            }
        } catch (GameException e) {
            System.err.println("exception called in protocol engine thread - fatal and stop");
            e.printStackTrace();
            // leave while - end thread
        }
    }

    @Override
    public void handleConnection(InputStream is, OutputStream os) throws IOException {
        this.is = is;
        this.os = os;

        this.protocolThread = new Thread(this);
        this.protocolThread.start();
    }
}
