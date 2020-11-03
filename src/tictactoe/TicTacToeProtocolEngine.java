package tictactoe;

import network.ProtocolEngine;

import java.io.*;
import java.util.Random;

public class TicTacToeProtocolEngine implements TicTacToe, Runnable, ProtocolEngine {
    private static final String DEFAULT_NAME = "anonymousProtocolEngine";
    private String name;
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
    private boolean oracle;
    private boolean oracleSet = false;

    /**
     * constructor has an additional name - helps debugging.
     * @param gameEngine
     * @param name
     */
    TicTacToeProtocolEngine(TicTacToe gameEngine, String name) {
        this.gameEngine = gameEngine;
        this.name = name;
    }

    public TicTacToeProtocolEngine(TicTacToe gameEngine) {
        this(gameEngine, DEFAULT_NAME);
    }

    @Override
    public TicTacToePiece pick(String userName, TicTacToePiece wantedSymbol)
            throws GameException, StatusException {

        this.log("send pick message to other side");
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
                this.log("pick thread back - results arrived");
            }

            // remember - we are not waiting any longer
            this.pickWaitThread = null;

            return this.pickResult;

        } catch (IOException e) {
            throw new GameException("could not serialize command", e);
        }
    }

    private void deserializeResultPick() throws GameException {
        this.log("deserialize received pick result message");
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
        this.log("deserialize received pick message");
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
            this.log("going to send return value");
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
        this.log("send set message to other side");
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
        this.log("deserialize received set message");
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

    boolean read() throws GameException {
        this.log("Protocol Engine: read from input stream");
        DataInputStream dis = new DataInputStream(this.is);

        // read method id
        try {
            int commandID = dis.readInt();
            switch (commandID) {
                case METHOD_PICK: this.deserializePick(); return true;
                case METHOD_SET: this.deserializeSet(); return true;
                case RESULT_PICK: this.deserializeResultPick(); return true;
                default: this.log("unknown method, throw exception id == " + commandID); return false;

            }
        } catch (IOException e) {
            this.log("IOException caught - most probably connection close - stop thread / stop engine");
            try {
                this.close();
            } catch (IOException ioException) {
                // ignore
            }
            return false;
        }
    }

    @Override
    public void run() {
        this.log("Protocol Engine started - flip a coin");
        long seed = this.hashCode() * System.currentTimeMillis();
        Random random = new Random(seed);

        int localInt = 0, remoteInt = 0;
        try {
            do {
                localInt = random.nextInt();
                this.log("flip and take number " + localInt);
                DataOutputStream dos = new DataOutputStream(this.os);
                dos.writeInt(localInt);
                DataInputStream dis = new DataInputStream(this.is);
                remoteInt = dis.readInt();
            } while(localInt == remoteInt);

        } catch (IOException e) {
            e.printStackTrace();
        }

        this.oracle = localInt < remoteInt;
        this.log("Flipped a coin and got an oracle == " + this.oracle);
        this.oracleSet = true;

        try {
            boolean again = true;
            while(again) {
                    again = this.read();
            }
        } catch (GameException e) {
            this.logError("exception called in protocol engine thread - fatal and stop");
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

    @Override
    public void close() throws IOException {
        if(this.os != null) { this.os.close();}
        if(this.is != null) { this.is.close();}
    }

    @Override
    public boolean getOracle() throws StatusException {
        this.log("asked for an oracle - return " + this.oracle);
        return this.oracle;
    }

    private String produceLogString(String message) {
        StringBuilder sb = new StringBuilder();
        if(this.name != null) {
            sb.append(this.name);
            sb.append(": ");
        }

        sb.append(message);

        return sb.toString();
    }

    private void log(String message) {
        System.out.println(this.produceLogString(message));
    }

    private void logError(String message) {
        System.err.println(this.produceLogString(message));
    }
}
