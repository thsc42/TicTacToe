package tictactoe;

import network.TCPStream;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;

public class ProtocolEngineTests {
    public static final String ALICE = "Alice";
    public static final int PORTNUMBER = 5555;
    private static final String BOB = "Bob";
    private static int port = 0;
    public static final long TEST_THREAD_SLEEP_DURATION = 1000;

    private TicTacToe getTTTEngine(InputStream is, OutputStream os, TicTacToe gameEngine) throws IOException {
        TicTacToeProtocolEngine ticTacToeProtocolEngine = new TicTacToeProtocolEngine(gameEngine);
        ticTacToeProtocolEngine.handleConnection(is, os);
        return ticTacToeProtocolEngine;
    }

    /*
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
     */

    private int getPortNumber() {
        if(ProtocolEngineTests.port == 0) {
            ProtocolEngineTests.port = PORTNUMBER;
        } else {
            ProtocolEngineTests.port++;
        }

        System.out.println("use portnumber " + ProtocolEngineTests.port);
        return ProtocolEngineTests.port;
    }

    //@Test
    /* does not work any longer after step3 introducing multi-threading
    public void pickTest1() throws GameException, StatusException, IOException {
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
     */

    /*
    @Test
    public void setTest1() throws GameException, StatusException, IOException {
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
     */

    /*
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
        int port = this.getPortNumber();
        // this stream plays TCP server role during connection establishment
        TCPStream aliceSide = new TCPStream(port, true, "aliceSide");
        // this stream plays TCP client role during connection establishment
        TCPStream bobSide = new TCPStream(port, false, "bobSide");
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
        Thread.sleep(TEST_THREAD_SLEEP_DURATION);

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

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                             tidy up                                                    //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

        aliceProtocolEngine.close();
        bobProtocolEngine.close();

        // stop test thread to allow operating system to close sockets
        Thread.sleep(TEST_THREAD_SLEEP_DURATION);
    }
    */

    /**
     * Test protocol engine alone but over a network with implemented threads. No race conditions are tested here.
     * Can be done in an integration test.
     * @throws GameException
     * @throws StatusException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void integrationTest1() throws GameException, StatusException, IOException, InterruptedException {
        // there are players in this test: Alice and Bob

        // create Alice's game engine
        TicTacToeImpl aliceGameEngine = new TicTacToeImpl(ALICE);
        // create real protocol engine on Alice's side
        TicTacToeProtocolEngine aliceTicTacToeProtocolEngine =
                new TicTacToeProtocolEngine(aliceGameEngine, ALICE);

        aliceGameEngine.setProtocolEngine(aliceTicTacToeProtocolEngine);

        // create Bob's game engine
        TicTacToeImpl bobGameEngine = new TicTacToeImpl(BOB);
        // create real protocol engine on Bob's side
        TicTacToeProtocolEngine bobTicTacToeProtocolEngine =
                new TicTacToeProtocolEngine(bobGameEngine, BOB);

        bobGameEngine.setProtocolEngine(bobTicTacToeProtocolEngine);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                           setup tcp                                                    //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        int port = this.getPortNumber();
        // this stream plays TCP server role during connection establishment
        TCPStream aliceSide = new TCPStream(port, true, "aliceSide");
        // this stream plays TCP client role during connection establishment
        TCPStream bobSide = new TCPStream(port, false, "bobSide");
        // start both stream
        aliceSide.start(); bobSide.start();
        // wait until TCP connection is established
        aliceSide.waitForConnection(); bobSide.waitForConnection();

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                       launch protocol engine                                           //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // give protocol engines streams and launch
        aliceTicTacToeProtocolEngine.handleConnection(aliceSide.getInputStream(), aliceSide.getOutputStream());
        bobTicTacToeProtocolEngine.handleConnection(bobSide.getInputStream(), bobSide.getOutputStream());

        // give it a moment - important stop this test thread - to threads must be launched
        System.out.println("give threads a moment to be launched");
        Thread.sleep(TEST_THREAD_SLEEP_DURATION);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                             test results                                               //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // pieces must not be same
        Assert.assertTrue(aliceGameEngine.getStatus() == bobGameEngine.getStatus());

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                             tidy up                                                    //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

        aliceTicTacToeProtocolEngine.close();
        bobTicTacToeProtocolEngine.close();

        // stop test thread to allow operating system to close sockets
        Thread.sleep(TEST_THREAD_SLEEP_DURATION);

        // Thread.sleep(Long.MAX_VALUE); // debugging
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
    public void integrationTestFullGame() throws GameException, StatusException, IOException, InterruptedException {
        // there are players in this test: Alice and Bob

        // create Alice's game engine
        TicTacToeImpl aliceGameEngine = new TicTacToeImpl(ALICE);
        // create real protocol engine on Alice's side
        TicTacToeProtocolEngine aliceTicTacToeProtocolEngine =
                new TicTacToeProtocolEngine(aliceGameEngine, ALICE);

        aliceGameEngine.setProtocolEngine(aliceTicTacToeProtocolEngine);

        // create Bob's game engine
        TicTacToeImpl bobGameEngine = new TicTacToeImpl(BOB);
        // create real protocol engine on Bob's side
        TicTacToeProtocolEngine bobTicTacToeProtocolEngine =
                new TicTacToeProtocolEngine(bobGameEngine, BOB);

        bobGameEngine.setProtocolEngine(bobTicTacToeProtocolEngine);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                           setup tcp                                                    //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        int port = this.getPortNumber();
        // this stream plays TCP server role during connection establishment
        TCPStream aliceSide = new TCPStream(port, true, "aliceSide");
        // this stream plays TCP client role during connection establishment
        TCPStream bobSide = new TCPStream(port, false, "bobSide");
        // start both stream
        aliceSide.start(); bobSide.start();
        // wait until TCP connection is established
        aliceSide.waitForConnection(); bobSide.waitForConnection();

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                       launch protocol engine                                           //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // give protocol engines streams and launch
        aliceTicTacToeProtocolEngine.handleConnection(aliceSide.getInputStream(), aliceSide.getOutputStream());
        bobTicTacToeProtocolEngine.handleConnection(bobSide.getInputStream(), bobSide.getOutputStream());

        // give it a moment - important stop this test thread - to threads must be launched
        System.out.println("give threads a moment to be launched");
        Thread.sleep(TEST_THREAD_SLEEP_DURATION);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                             run scenario                                               //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

        TicTacToeLocalBoard playerFirst = aliceGameEngine.isActive() ? aliceGameEngine : bobGameEngine;
        TicTacToeLocalBoard playerSecond = aliceGameEngine.isActive() ? bobGameEngine : aliceGameEngine;

        TicTacToeBoardPosition position =
        new TicTacToeBoardPosition("C", 0);
        Assert.assertFalse(playerFirst.set(TicTacToePiece.O, position));
        Thread.sleep(TEST_THREAD_SLEEP_DURATION);

        position = new TicTacToeBoardPosition("C", 1);
        Assert.assertFalse(playerSecond.set(TicTacToePiece.X, position));
        Thread.sleep(TEST_THREAD_SLEEP_DURATION);

        position = new TicTacToeBoardPosition("B", 1);
        Assert.assertFalse(playerFirst.set(TicTacToePiece.O, position));
        Thread.sleep(TEST_THREAD_SLEEP_DURATION);

        position = new TicTacToeBoardPosition("B", 0);
        Assert.assertFalse(playerSecond.set(TicTacToePiece.X, position));
        Thread.sleep(TEST_THREAD_SLEEP_DURATION);

        position = new TicTacToeBoardPosition("A", 2);
        Assert.assertTrue(playerFirst.set(TicTacToePiece.O, position));
        Thread.sleep(TEST_THREAD_SLEEP_DURATION);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                             test results                                               //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

        Assert.assertTrue(playerFirst.hasWon());
        Assert.assertTrue(playerSecond.hasLost());

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                             tidy up                                                    //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

        aliceTicTacToeProtocolEngine.close();
        bobTicTacToeProtocolEngine.close();

        // stop test thread to allow operating system to close sockets
        Thread.sleep(TEST_THREAD_SLEEP_DURATION);

        // Thread.sleep(Long.MAX_VALUE); // debugging
    }
}
