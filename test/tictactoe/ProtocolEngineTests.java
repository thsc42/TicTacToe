package tictactoe;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ProtocolEngineTests {
    public static final String ALICE = "Alice";

    private TicTacToe getTTTEngine(InputStream is, OutputStream os, TicTacToe gameEngine) {
        return new TicTacToeProtocolEngine(is, os, gameEngine);
    }

    @Test
    public void pickTest1() throws GameException, StatusException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TicTacToe tttProtocolSender = this.getTTTEngine(null, baos, null);
        TicTacToePiece aliceSymbol = tttProtocolSender.pick(ALICE, TicTacToePiece.O);

        // simulate network
        byte[] serializedBytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(serializedBytes);

        TicTacToeReadTester tttReceiver = new TicTacToeReadTester();
        TicTacToe tttProtocolReceiver = this.getTTTEngine(bais, null, tttReceiver);

        // TODO
        TicTacToeProtocolEngine tttEngine = (TicTacToeProtocolEngine) tttProtocolReceiver;
        tttEngine.read();

        Assert.assertTrue(tttReceiver.lastCallPick);
        Assert.assertTrue(tttReceiver.userName.equalsIgnoreCase(ALICE));
        Assert.assertEquals(TicTacToePiece.O, tttReceiver.piece);
    }

    @Test
    public void setTest1() throws GameException, StatusException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TicTacToe tttProtocolSender = this.getTTTEngine(null, baos, null);

        TicTacToeBoardPosition position = new TicTacToeBoardPosition("A", 0);
        tttProtocolSender.set(TicTacToePiece.O, position);

        // simulate network
        byte[] serializedBytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(serializedBytes);

        TicTacToeReadTester tttReceiver = new TicTacToeReadTester();
        TicTacToe tttProtocolReceiver = this.getTTTEngine(bais, null, tttReceiver);

        // TODO
        TicTacToeProtocolEngine tttEngine = (TicTacToeProtocolEngine) tttProtocolReceiver;
        tttEngine.read();

        Assert.assertTrue(tttReceiver.lastCallSet);
        Assert.assertEquals(TicTacToePiece.O, tttReceiver.piece);
        Assert.assertTrue(tttReceiver.position.getSCoordinate().equalsIgnoreCase("A"));
        Assert.assertEquals(0, tttReceiver.position.getICoordinate());
    }

    private class TicTacToeReadTester implements TicTacToe {
        private boolean lastCallPick = false;
        private boolean lastCallSet = false;

        String userName = null;
        TicTacToePiece piece;
        TicTacToeBoardPosition position;

        @Override
        public TicTacToePiece pick(String userName, TicTacToePiece wantedSymbol) throws GameException, StatusException {
            this.lastCallSet = false;
            this.lastCallPick = true;
            this.userName = userName;
            this.piece = wantedSymbol;

            return wantedSymbol;
        }

        @Override
        public boolean set(TicTacToePiece piece, TicTacToeBoardPosition position) throws GameException, StatusException {
            this.lastCallPick = false;
            this.lastCallSet = true;
            this.position = position;
            this.piece = piece;

            return false;
        }
    }
}
