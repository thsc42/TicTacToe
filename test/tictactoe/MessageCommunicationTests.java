package tictactoe;

import net.sharksystem.asap.apps.mock.ASAPSessionMock;
import network.TCPStream;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;


public class MessageCommunicationTests {
    private static final String ALICE = "Alice";
    private static final String BOB = "Bob";
    private static final long TEST_THREAD_SLEEP_DURATION = 1000;

    //@Test - TODO work on it
    public void test1() throws InterruptedException, IOException, GameException, StatusException {
        // create Alice's game engine
        TicTacToeImpl aliceGameEngine = new TicTacToeImpl(ALICE);

        // asap session mock
        ASAPSessionMock aliceASAPSessionMock = new ASAPSessionMock();

        // create real protocol engine on Alice's side with asap mock
        TicTacToeASAPProtocolEngine aliceASAPProtocolEngine =
                new TicTacToeASAPProtocolEngine(aliceGameEngine, ALICE, aliceASAPSessionMock);

        aliceGameEngine.setProtocolEngine(aliceASAPProtocolEngine);

        // create Bob's game engine
        TicTacToeImpl bobGameEngine = new TicTacToeImpl(BOB);

        // asap session mock
        ASAPSessionMock bobASAPSessionMock = new ASAPSessionMock();

        // create real protocol engine on Bob's side with asap mock
        TicTacToeASAPProtocolEngine bobTicTacToeASAPProtocolEngine =
                new TicTacToeASAPProtocolEngine(bobGameEngine, BOB, bobASAPSessionMock);

        bobGameEngine.setProtocolEngine(bobTicTacToeASAPProtocolEngine);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                       launch protocol engine                                           //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // give protocol engines streams and launch
        /*
        aliceTicTacToeTCPProtocolEngine.handleConnection(aliceSide.getInputStream(), aliceSide.getOutputStream());
        bobTicTacToeTCPProtocolEngine.handleConnection(bobSide.getInputStream(), bobSide.getOutputStream());
         */

        // give it a moment - important stop this test thread - to threads must be launched
        System.out.println("give threads a moment to be launched");
        Thread.sleep(TEST_THREAD_SLEEP_DURATION);

        ////////////////////// run scenario
        this.runTestFullGame(aliceGameEngine, bobGameEngine);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                             tidy up                                                    //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

        /*
        aliceTicTacToeTCPProtocolEngine.close();
        bobTicTacToeTCPProtocolEngine.close();
         */

        // stop test thread to allow operating system to close sockets
        Thread.sleep(TEST_THREAD_SLEEP_DURATION);

        // Thread.sleep(Long.MAX_VALUE); // debugging


    }

    public void runTestFullGame(TicTacToeImpl aliceGameEngine, TicTacToeImpl bobGameEngine)
            throws GameException, StatusException, IOException, InterruptedException {

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
    }
}
