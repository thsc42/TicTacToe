package tictactoe;

import java.util.HashMap;

public class TicTacToeImpl implements TicTacToe, TicTacToeDebugHelper {
    private Status status = Status.START;
    HashMap<TicTacToePiece, String> player = new HashMap<>();

    @Override
    synchronized public TicTacToePiece pick(String userName, TicTacToePiece wantedSymbol)
            throws GameException, StatusException {
        if (this.status != Status.START && this.status != Status.ONE_PICKED) {
            throw new StatusException("pick call but wrong status");
        }

        System.out.println("userName == " + userName + " | symbol == " + wantedSymbol);

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
            System.out.println(userName + " already got a symbol " + takenSymbol);

            if(takenSymbol == wantedSymbol) {
                System.out.println(userName + " wanted symbol is takenSymbol nothing to do here");
                return wantedSymbol;
            }

            System.out.println(userName + " want's to change symbol ");
            // had a change of heart - can it be changed?
            if(this.player.get(wantedSymbol) == null) { // yes - can change
                this.player.remove(takenSymbol);
                this.player.put(wantedSymbol, userName);
                System.out.println(userName + " changed symbol from " + takenSymbol + " to " + wantedSymbol);
                return wantedSymbol;
            } else { // cannot change - other symbol is already taken - live with your previous choice
                System.out.println(userName + " cannot change symbol will take: " + takenSymbol);
                return takenSymbol;
            }
        } else { // no - no symbol taken yet
            System.out.println(userName + " has no symbol yet: " + wantedSymbol);
            // wanted symbol available?
            if(this.player.get(wantedSymbol) == null) { // yes - symbol available
                System.out.println(userName + " wanted symbol still available: " + wantedSymbol);
                this.player.put(wantedSymbol, userName);
                this.changeStatusAfterPickedSymbol();
                return wantedSymbol;
            } else { // not - wanted symbol already taken
                System.out.println(userName + " wanted symbol already taken: " + wantedSymbol);
                TicTacToePiece otherSymbol = wantedSymbol == TicTacToePiece.O ? TicTacToePiece.X : TicTacToePiece.O;
                this.player.put(otherSymbol, userName);
                this.changeStatusAfterPickedSymbol();
                System.out.println(userName + "taken instead " + otherSymbol);
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
    public boolean set(TicTacToePiece piece, TicTacToeBoardPosition position)
            throws GameException, StatusException {

        if(this.status != Status.ACTIVE_O && this.status != Status.ACTIVE_X) {
            throw new StatusException("set called but wrong status");
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

        boolean ended = this.hasWon(piece);

        if(ended) {
            this.status = Status.ENDED;
        } else {
            this.status = this.status == Status.ACTIVE_O ? Status.ACTIVE_X : Status.ACTIVE_O;
        }

        return ended;
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
    //                                             debug helper                                             //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void printBoard() {
        for(int v = 2; v > -1; v--) {
            System.out.print(v + " ");
            for(int h = 0; h < 3; h++) {
                TicTacToePiece piece = this.board[h][v];
                if(piece == null) { System.out.print("   "); }
                else {
                    switch (piece) {
                        case O: System.out.print(" O "); break;
                        case X: System.out.print(" X "); break;
                    }
                }
            }
            System.out.print("\n");
        }
        System.out.println("   A  B  C");
    }
}
