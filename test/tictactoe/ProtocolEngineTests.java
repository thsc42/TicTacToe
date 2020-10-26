package tictactoe;

import network.TCPStream;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;

public class ProtocolEngineTests {
    public static final String ALICE = "Alice";
    public static final int PORTNUMBER = 9999;

    private TicTacToe getTTTEngine(InputStream is, OutputStream os, TicTacToe gameEngine) {
        return new TicTacToeProtocolEngine(is, os, gameEngine);
    }

    //@Test
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
    public void pickTestWithResults1() throws GameException, StatusException, IOException, InterruptedException {
        // there are players in this test: Alice and Bob

        // create Alice's game engine tester
        TicTacToeReadTester aliceGameEngineTester = new TicTacToeReadTester();
        // create real protocol engine on Alice's side
        TicTacToeProtocolEngine aliceProtocolEngine = new TicTacToeProtocolEngine(aliceGameEngineTester);

        // create Bob's game engine tester
        TicTacToeReadTester bobGameEngineTester = new TicTacToeReadTester();
        // create real protocol engine on Bob's side
        TicTacToeProtocolEngine bobProtocolEngine = new TicTacToeProtocolEngine(bobGameEngineTester);

        // setup tcp connection

        // this stream plays TCP server role during connection establishment
        TCPStream aliceSide = new TCPStream(PORTNUMBER, true, "aliceSide");
        // this stream plays TCP client role during connection establishment
        TCPStream bobSide = new TCPStream(PORTNUMBER, false, "bobSide");

        // start both stream
        aliceSide.start();
        bobSide.start();

        // wait until TCP connection is established
        aliceSide.waitForConnection();
        bobSide.waitForConnection();

        // give protocol engines streams and launch
        aliceProtocolEngine.handleConnection(aliceSide.getInputStream(), aliceSide.getOutputStream());
        bobProtocolEngine.handleConnection(bobSide.getInputStream(), bobSide.getOutputStream());

        // give it a moment - important stop this test thread - to threads must be launched
        System.out.println("give threads a moment to be launched");
        Thread.sleep(1000);

        // connection is established here - pick thread waits for results
        TicTacToePiece alicePickResult = aliceProtocolEngine.pick(ALICE, TicTacToePiece.O);
        Assert.assertEquals(TicTacToePiece.O, alicePickResult);

        // a) something arrived on Bob's side
        Assert.assertTrue(bobGameEngineTester.lastCallPick);
        Assert.assertTrue(bobGameEngineTester.userName.equalsIgnoreCase(ALICE));
        Assert.assertEquals(TicTacToePiece.O, bobGameEngineTester.piece);
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
