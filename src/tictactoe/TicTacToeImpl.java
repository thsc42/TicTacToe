package tictactoe;

import network.GameSessionEstablishedListener;
import view.PrintStreamView;
import view.TicTacToePrintStreamView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TicTacToeImpl implements TicTacToe, TicTacToeLocalBoard, GameSessionEstablishedListener {
    private static final String DEFAULT_PLAYERNAME = "anonPlayer";
//    private static final long WAITING_PERIOD_TO_AVOID_DEADLOCK = 1000;
    private final String localPlayerName;
    private String remotePlayerName;
    private Status status = Status.START;
    HashMap<TicTacToePiece, String> player = new HashMap<>();
    private TicTacToeProtocolEngine protocolEngine;
    private TicTacToePiece localSymbol;
    private TicTacToePiece remoteSymbol;
    private boolean localWon;

    public TicTacToeImpl(String localPlayerName) {
        this.localPlayerName = localPlayerName;
    }

    /**
     * @deprecated for simple testing purpose only
     */
    TicTacToeImpl() {
        this(DEFAULT_PLAYERNAME);
    }

    /**
     * produce print stream view - TODO discuss - not a perfect solution, though
     * @return
     */
    public PrintStreamView getPrintStreamView() {
        return new TicTacToePrintStreamView(this.board);
    }

    @Override
    public TicTacToePiece pick(String userName, TicTacToePiece wantedSymbol)
            throws GameException, StatusException {

        if (this.status != Status.START && this.status != Status.ONE_PICKED) {
            throw new StatusException("pick call but wrong status");
        }

        System.out.println(this.localPlayerName + ": " + "userName == " + userName + " | symbol == " + wantedSymbol);

        TicTacToePiece takenSymbol = null;
        // already taken a symbol?
        takenSymbol = this.getTakenSymbol(userName, TicTacToePiece.O);
        if(takenSymbol == null) {
            takenSymbol = this.getTakenSymbol(userName, TicTacToePiece.X);
        }

        // it this user number 2+ ?
        if(takenSymbol == null && this.player.values().size() == 2) {
            throw new GameException("both symbols taken but not from " + userName);
        }

        // user already got symbol?
        if(takenSymbol != null) { // yes - user got a symbol
            // wanted one?
            System.out.println(this.localPlayerName + ": " + userName + " already got a symbol " + takenSymbol);

            if(takenSymbol == wantedSymbol) {
                System.out.println(this.localPlayerName + ": " + userName + " wanted symbol is takenSymbol nothing to do here");
                return wantedSymbol;
            }

            System.out.println(this.localPlayerName + ": " + userName + " want's to change symbol ");
            // had a change of heart - can it be changed?
            if(this.player.get(wantedSymbol) == null) { // yes - can change
                this.player.remove(takenSymbol);
                this.player.put(wantedSymbol, userName);
                System.out.println(this.localPlayerName + ": " + userName + " changed symbol from " + takenSymbol + " to " + wantedSymbol);
                return wantedSymbol;
            } else { // cannot change - other symbol is already taken - live with your previous choice
                System.out.println(this.localPlayerName + ": " + userName + " cannot change symbol will take: " + takenSymbol);
                return takenSymbol;
            }
        } else { // no - no symbol taken yet
            System.out.println(this.localPlayerName + ": " + userName + " has no symbol yet: " + wantedSymbol);
            // wanted symbol available?
            if(this.player.get(wantedSymbol) == null) { // yes - symbol available
                System.out.println(this.localPlayerName + ": " + userName + " wanted symbol still available: " + wantedSymbol);
                this.player.put(wantedSymbol, userName);
                this.changeStatusAfterPickedSymbol();
                return wantedSymbol;
            } else { // not - wanted symbol already taken
                System.out.println(this.localPlayerName + ": " + userName + " wanted symbol already taken: " + wantedSymbol);
                TicTacToePiece otherSymbol = wantedSymbol == TicTacToePiece.O ? TicTacToePiece.X : TicTacToePiece.O;
                this.player.put(otherSymbol, userName);
                this.changeStatusAfterPickedSymbol();
                System.out.println(this.localPlayerName + ": " + userName + "taken instead " + otherSymbol);
                return otherSymbol;
            }
        }
    }

    private void changeStatusAfterPickedSymbol() {
        this.status = this.status == Status.START ? Status.ONE_PICKED : Status.ACTIVE_O;
    }

    private TicTacToePiece getTakenSymbol(String userName, TicTacToePiece piece) {
        String name = this.player.get(piece);
        if(name != null && name.equalsIgnoreCase(userName)) {
            return piece;
        }
        return null;
    }

    private TicTacToePiece[][] board = new TicTacToePiece[3][3]; // horizontal / vertical


    @Override
    public boolean set(TicTacToeBoardPosition position) throws GameException, StatusException {
        return this.set(this.localSymbol, position);
    }

    @Override
    public boolean set(TicTacToePiece piece, TicTacToeBoardPosition position)
            throws GameException, StatusException {

        if(this.status != Status.ACTIVE_O && this.status != Status.ACTIVE_X) {
            throw new StatusException("set called but wrong status");
        }

        if(piece == null) {
            throw new GameException("piece must not be null");
        }

        if( (piece == TicTacToePiece.O) && this.status == Status.ACTIVE_X)
            throw new StatusException("not your turn but X");

        if( (piece == TicTacToePiece.X) && this.status == Status.ACTIVE_O)
            throw new StatusException("not your turn but O");

        int horizontal = this.sCoordinate2Int(position.getSCoordinate());
        int vertical = this.checkIntCoordinate(position.getICoordinate());

        if(this.board[horizontal][vertical] != null) {
            throw new GameException("position already occupied");
        }

        this.board[horizontal][vertical] = piece;

        boolean hasWon = this.hasWon(piece);

        if(hasWon) {
            System.out.println(this.localPlayerName + ": set " + piece  + " - has won");
            this.status = Status.ENDED;
            if(this.localSymbol == piece) this.localWon = true;
        } else {
            this.status = this.status == Status.ACTIVE_O ? Status.ACTIVE_X : Status.ACTIVE_O;
            System.out.println(this.localPlayerName + ": set " + piece  + " - not won, new status " + this.status);
        }

        // tell other side - if local call (test of null if for some unit tests)
        if(this.localSymbol == piece && this.protocolEngine != null) {
            this.protocolEngine.set(piece, position);
        } else {
            // remote call
            this.notifyBoardChanged();
        }

        return hasWon;
    }

    private boolean hasWon(TicTacToePiece piece) {
        for(int start = 0; start < 3; start++) {
            // vertical row
            if(this.threeInARow(piece, 0, start, 1, 0)) return true;
            // horizontal row
            if(this.threeInARow(piece, start, 0, 0, 1))  return true;
        }

        // diagonal row 1
        if(this.threeDiagonal(piece, 0, 0, 1, 1)) return true;

        // diagonal row 2
        if(this.threeDiagonal(piece, 0, 2, 1, -1)) return true;

        return false;
    }

    private boolean threeDiagonal(TicTacToePiece piece, int horizontal, int vertical,
                                  int horizontalIncrement, int verticalIncrement) {

        for(int rounds = 0; rounds < 2; rounds++) {
            if(this.board[horizontal][vertical] != piece) return false;
            horizontal += horizontalIncrement;
            vertical += verticalIncrement;
        }

        return true;
    }

    private boolean threeInARow(TicTacToePiece piece, int horizontalStart, int verticalStart,
                                int horizontalIncrement, int verticalIncrement) {

        if(this.board[horizontalStart][verticalStart] != piece) return false;

        int h = horizontalStart;
        for(int hRoundCounter = 0; hRoundCounter < 3; hRoundCounter++) {
            int v= verticalStart;
            for(int vRoundCounter = 0; vRoundCounter < 3; vRoundCounter++) {
                if(this.board[h][v] != piece) return false;
                v += verticalIncrement;
            }
            h += horizontalIncrement;
        }

        return true; // line is not broken
    }



    private int sCoordinate2Int(String cCoordinate) throws GameException {
        switch (cCoordinate) {
            case "A": return 0;
            case "B": return 1;
            case "C": return 2;
        }

        throw new GameException("coordinate outside the board");
    }

    private int checkIntCoordinate(int iCoordinate) throws GameException {
        if(iCoordinate < 0 || iCoordinate > 2) {
            throw new GameException("coordinate outside the board");
        }
        return iCoordinate;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                       constructor helper                                             //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setProtocolEngine(TicTacToeProtocolEngine protocolEngine) {
        this.protocolEngine = protocolEngine;
        this.protocolEngine.subscribeGameSessionEstablishedListener(this);
    }

    @Override
    public Status getStatus() {
        return this.status;
    }

    @Override
    public boolean isActive() {
        if(this.localSymbol == null) return false;

        return (
            (this.getStatus() == Status.ACTIVE_O && this.localSymbol == TicTacToePiece.O)
            ||
            (this.getStatus() == Status.ACTIVE_X && this.localSymbol == TicTacToePiece.X));
    }

    @Override
    public boolean hasWon() {
        return this.status == Status.ENDED && this.localWon;
    }

    @Override
    public boolean hasLost() {
        return this.status == Status.ENDED && !this.localWon;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                            observed                                                 //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    private List<LocalBoardChangeListener> boardChangeListenerList = new ArrayList<>();
    @Override
    public void subscribeChangeListener(LocalBoardChangeListener changeListener) {
        this.boardChangeListenerList.add(changeListener);
    }

    private void notifyBoardChanged() {
        // are there any listeners ?
        if(this.boardChangeListenerList == null || this.boardChangeListenerList.isEmpty()) return;

        // yes - there are - create a thread and inform them
        (new Thread(new Runnable() {
            @Override
            public void run() {
                for(LocalBoardChangeListener listener : TicTacToeImpl.this.boardChangeListenerList) {
                    listener.changed();
                }
            }
        })).start();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                            listener                                                 //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void gameSessionEstablished(boolean oracle, String partnerName) {
        System.out.println(this.localPlayerName + ": gameSessionEstablished with " + partnerName + " | " + oracle);

        this.localSymbol = oracle ? TicTacToePiece.O : TicTacToePiece.X;
        this.remoteSymbol = this.localSymbol == TicTacToePiece.O ? TicTacToePiece.X : TicTacToePiece.O;
        this.remotePlayerName = partnerName;

        // O always starts
        this.status = Status.ACTIVE_O;
    }
}
