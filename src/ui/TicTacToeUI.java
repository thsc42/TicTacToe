package ui;

import network.TCPStream;
import network.TCPStreamCreatedListener;
import tictactoe.TicTacToe;
import tictactoe.TicTacToeImpl;
import tictactoe.TicTacToeProtocolEngine;

import java.io.*;
import java.util.StringTokenizer;

public class TicTacToeUI implements TCPStreamCreatedListener {
    private static final String PRINT = "print";
    private static final String EXIT = "exit";
    private static final String CONNECT = "connect";
    private static final String OPEN = "open";
    private final PrintStream outStream;
    private final BufferedReader inBufferedReader;
    private final String playerName;
    private final TicTacToeImpl gameEngine;
    private TCPStream tcpStream;

    public static void main(String[] args) throws IOException {
        System.out.println("Welcome to TicTacToe version 0.1");

        if(args.length < 1) {
            System.err.println("need playerName as parameter");
        }

        System.out.println("Welcome " + args[0]);
        System.out.println("Let's play a game");

        TicTacToeUI userCmd = new TicTacToeUI(args[0], System.out, System.in);

        userCmd.printUsage();
        userCmd.runCommandLoop();
    }

    public TicTacToeUI(String playerName, PrintStream os, InputStream is) throws IOException {
        this.playerName = playerName;
        this.outStream = os;
        this.inBufferedReader = new BufferedReader(new InputStreamReader(is));

        this.gameEngine = new TicTacToeImpl(playerName);
    }


    @Override
    public void streamCreated(TCPStream stream) {
        // connection established - setup protocol engine

        TicTacToeProtocolEngine protocolEngine = new TicTacToeProtocolEngine(this.gameEngine);
        this.gameEngine.setProtocolEngine(protocolEngine);

        try {
            protocolEngine.handleConnection(stream.getInputStream(), stream.getOutputStream());
        } catch (IOException e) {
            System.err.println("cannot get streams from tcpStream - fatal, give up: " + e.getLocalizedMessage());
            System.exit(1);
        }
    }

    public void printUsage() {
        StringBuilder b = new StringBuilder();

        b.append("\n");
        b.append("\n");
        b.append("valid commands:");
        b.append("\n");
        b.append(CONNECT);
        b.append(".. connect as tcp client");
        b.append(OPEN);
        b.append(".. open port become tcp server");
        b.append("\n");
        b.append(PRINT);
        b.append(".. print board");
        b.append("\n");
        b.append(EXIT);
        b.append(".. exit");

        this.outStream.println(b.toString());
    }

    public void runCommandLoop() {
        boolean again = true;

        while(again) {
            boolean rememberCommand = true;
            String cmdLineString = null;

            try {
                // read user input
                cmdLineString = inBufferedReader.readLine();

                // finish that loop if less than nothing came in
                if(cmdLineString == null) break;

                // trim whitespaces on both sides
                cmdLineString = cmdLineString.trim();

                // extract command
                int spaceIndex = cmdLineString.indexOf(' ');
                spaceIndex = spaceIndex != -1 ? spaceIndex : cmdLineString.length();

                // got command string
                String commandString = cmdLineString.substring(0, spaceIndex);

                // extract parameters string - can be empty
                String parameterString = cmdLineString.substring(spaceIndex);
                parameterString = parameterString.trim();

                // start command loop
                switch(commandString) {
                    case PRINT:
                        this.doPrint(); break;
                    case CONNECT:
                        this.doConnect(parameterString); break;
                    case OPEN:
                        this.doOpen(); break;
                    case "q": // convenience
                    case EXIT:
                        again = false; break; // end loop

                    default: this.outStream.println("unknown command:" + cmdLineString);
                        this.printUsage();
                        rememberCommand = false;
                        break;
                }
            } catch (IOException ex) {
                this.outStream.println("cannot read from input stream");
                System.exit(0);
            } catch (RuntimeException ex) {
                this.outStream.println("problems: " + ex.getLocalizedMessage());
            }
        }
    }

    // don' repeat yourself
    private boolean alreadyConnected() {
        if(this.tcpStream != null) {
            System.err.println("connection already established or connection attempt in progress");
            return true;
        }

        return false;
    }

    private void doOpen() {
        if(this.alreadyConnected()) return;

        this.tcpStream = new TCPStream(TicTacToe.DEFAULT_PORT, false, this.playerName);
        this.tcpStream.start();
    }

    private void doConnect(String parameterString) {
        if(this.alreadyConnected()) return;

        StringTokenizer st = new StringTokenizer(parameterString);

        this.tcpStream = new TCPStream(TicTacToe.DEFAULT_PORT, false, this.playerName);
        this.tcpStream.setRemoteEngine(st.nextToken());
        this.tcpStream.start();
    }

    private void doPrint() {

    }
}
