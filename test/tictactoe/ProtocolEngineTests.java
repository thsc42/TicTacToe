package tictactoe;

import network.ProtocolEngine;
import network.TCPStream;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;

public class ProtocolEngineTests {
    public static final String ALICE = "Alice";
    public static final int PORTNUMBER = 9999;
    private static final String BOB = "Bob";

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

    /**
     * Test protocol engine alone but over a network with implemented threads. No race conditions are tested here.
     * Can be done in an integration test.
     * @throws GameException
     * @throws StatusException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void pickNetworkTest() throws GameException, StatusException, IOException, InterruptedException {
        // there are players in this test: Alice and Bob

        // create Alice's game engine tester
        TicTacToeReadTester aliceGameEngineTester = new TicTacToeReadTester();
        // create real protocol engine on Alice's side
        TicTacToeProtocolEngine aliceTicTacToeProtocolEngine = new TicTacToeProtocolEngine(aliceGameEngineTester);

        // make it clear - this is a protocol engine
        ProtocolEngine aliceProtocolEngine = aliceTicTacToeProtocolEngine;
        // make it clear - it also supports the game engine interface
        TicTacToe aliceGameEngineSide = aliceTicTacToeProtocolEngine;

        // create Bob's game engine tester
        TicTacToeReadTester bobGameEngineTester = new TicTacToeReadTester();
        // create real protocol engine on Bob's side
        ProtocolEngine bobProtocolEngine = new TicTacToeProtocolEngine(bobGameEngineTester);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                           setup tcp                                                    //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // this stream plays TCP server role during connection establishment
        TCPStream aliceSide = new TCPStream(PORTNUMBER, true, "aliceSide");
        // this stream plays TCP client role during connection establishment
        TCPStream bobSide = new TCPStream(PORTNUMBER, false, "bobSide");
        // start both stream
        aliceSide.start(); bobSide.start();
        // wait until TCP connection is established
        aliceSide.waitForConnection(); bobSide.waitForConnection();

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                       launch protocol engine                                           //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // give protocol engines streams and launch
        aliceProtocolEngine.handleConnection(aliceSide.getInputStream(), aliceSide.getOutputStream());
        bobProtocolEngine.handleConnection(bobSide.getInputStream(), bobSide.getOutputStream());

        // give it a moment - important stop this test thread - to threads must be launched
        System.out.println("give threads a moment to be launched");
        Thread.sleep(1000);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                             run scenario                                               //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // connection is established here - pick thread waits for results
        TicTacToePiece alicePickResult = aliceGameEngineSide.pick(ALICE, TicTacToePiece.O);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                             test results                                               //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Alice got here symbol
        Assert.assertEquals(TicTacToePiece.O, alicePickResult);
        // pick("Alice", O) arrived on Bob's side
        Assert.assertTrue(bobGameEngineTester.lastCallPick);
        Assert.assertTrue(bobGameEngineTester.userName.equalsIgnoreCase(ALICE));
        Assert.assertEquals(TicTacToePiece.O, bobGameEngineTester.piece);
    }

    @Test
    public void pickIntegrationGameProtocolEngine() throws GameException, StatusException, IOException, InterruptedException {
        // there are players in this test: Alice and Bob

        // create Alice's game engine ! no tester any longer
        TicTacToe aliceGameEngine = new TicTacToeImpl();
        // create real protocol engine on Alice's side
        TicTacToeProtocolEngine aliceProtocolEngine = new TicTacToeProtocolEngine(aliceGameEngine);

        // create Bob's game engine tester
        TicTacToe bobGameEngine = new TicTacToeImpl();
        // create real protocol engine on Bob's side
        TicTacToeProtocolEngine bobProtocolEngine = new TicTacToeProtocolEngine(bobGameEngine);

        // setup tcp connection
        // this stream plays TCP server role during connection establishment
        TCPStream aliceSide = new TCPStream(PORTNUMBER, true, "aliceSide");
        // this stream plays TCP client role during connection establishment
        TCPStream bobSide = new TCPStream(PORTNUMBER, false, "bobSide");
        // start both stream
        aliceSide.start(); bobSide.start();
        // wait until TCP connection is established
        aliceSide.waitForConnection(); bobSide.waitForConnection();

        // give protocol engines streams and launch
        aliceProtocolEngine.handleConnection(aliceSide.getInputStream(), aliceSide.getOutputStream());
        bobProtocolEngine.handleConnection(bobSide.getInputStream(), bobSide.getOutputStream());

        // give it a moment - important stop this test thread - to threads must be launched
        System.out.println("give threads a moment to be launched");
        Thread.sleep(1000);

        // connection is established here - pick thread waits for results
        TicTacToePiece alicePickResult = aliceProtocolEngine.pick(ALICE, TicTacToePiece.O);
        TicTacToePiece bobPickResult = bobProtocolEngine.pick(BOB, TicTacToePiece.O);

        Assert.assertTrue(alicePickResult != bobPickResult);
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
