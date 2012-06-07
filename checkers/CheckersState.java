package checkers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CheckersState{

  // Resulting board and board value when the move described above is made
  private int[][] board;
  //private int     value;
  
  // Possible board configurations that can result from the current board.
  private List<CheckersState> legalMoves;

  // Constants
  static final int
    // General
    ROWS        = 8,
    COLS        = 4,
    OUTOFBOUNDS = -1,
    // State
    EMPTY       = 0,
    RED         = 1,
    WHITE       = 2,
    KINGRED     = 3,
    KINGWHITE   = 4,
    // Neighbors
    UPLEFT      = 1,
    UPRIGHT     = 2,
    DOWNRIGHT   = 3,
    DOWNLEFT    = 4;
  
  public CheckersState(int[][] boardState) {
    if (boardState.length != ROWS || boardState[0].length != COLS) {
      System.out.println("CheckersState: Board must be " + ROWS + " by " + COLS);
      System.exit(1);
    }
    
    board = boardState; // board 0-7, 0-7
    legalMoves = new ArrayList<CheckersState>();
//    this.evalFunc = evalFunc;
  }
  
  /*
   * ================================= MOVES =================================
   */
  
  public boolean expand(int RW) {
    boolean jump     = false;
    boolean gameOver = false;
    
    outerloop:
    for (int row = 0; row < ROWS; row++)
      for (int col = 0; col < COLS; col++)
        if (board[row][col] == (1 + RW) || board[row][col] == (3 + RW)) {
          jump = exploitMoves(row, col, RW, jump);
          if (jump) break outerloop;
        }
    if (jump == false) {
      if (!legalMoves.isEmpty()) gameOver = false;
      else gameOver = true;
    }
    return gameOver;
  }
  
  private boolean exploitMoves(int row, int col, int RW, boolean jump) {
    int[] nbrCoords = new int[2];

    if (board[row][col] == (3 + RW)) {
      for (int diag = 0; diag < 2; diag++) {
        nbrCoords = neighbor(row, col, diag, RW, 0);
        if (nbrCoords[0] != OUTOFBOUNDS) {
          jump = checkMove(row, col, nbrCoords[0], nbrCoords[1], diag, jump, RW, 0);
        }
      }
    }
    for (int diag = 0; diag < 2; diag++) {
      if (board[row][col] != 0) { // TODO: added this to stop a piece with 2 jump choices from jumping both
        nbrCoords = neighbor(row, col, diag, RW, 1);
        if (nbrCoords[0] != OUTOFBOUNDS) {
          jump = checkMove(row, col, nbrCoords[0], nbrCoords[1], diag, jump, RW, 1);
        }
      }
    }
    return jump;
  }
  
  private boolean checkMove(int fromRow, int fromCol, int toRow, int toCol, 
      int diag, boolean jump, int RW, int FB) {
    int nbrCoords[] = new int [2];
    
    if (board[toRow][toCol] == EMPTY && jump == false) {
      listUpdate(fromRow, fromCol, toRow, toCol, simulateMove(fromRow, fromCol, 
          toRow, toCol, RW), RW);
    }
    else if ((board[toRow][toCol] == 2 - RW || board[toRow][toCol] == 4 - RW)) {
      nbrCoords = neighbor(toRow, toCol, diag, RW, FB);
      if(nbrCoords[0] != OUTOFBOUNDS && board[nbrCoords[0]][nbrCoords[1]] == EMPTY) {
        if (jump == false) {
          jump = true;
          legalMoves.clear();
        }
        makeJump(fromRow, fromCol, toRow, toCol, nbrCoords[0], nbrCoords[1], 
            jump, RW, FB);
      }
    }
    
    return jump;
  }
  
  private int[][] simulateMove(int fromRow, int fromCol, int toRow, int toCol, int RW) {
    int[][] boardCopy = new int[ROWS][COLS];
    for(int row = 0; row < ROWS; row++) boardCopy[row] = board[row].clone();   
    
    boardCopy[fromRow][fromCol] = 0;
    if (toRow == (0 + 7 * ( 1 - RW)) && board[fromRow][fromCol] == 1 + RW)
      boardCopy[toRow][toCol] = 3 + RW;
    else
      boardCopy[toRow][toCol] = board[fromRow][fromCol];
    return boardCopy;
  }
  
  private boolean makeJump(int fromRow, int fromCol, int skipRow, int skipCol, 
      int toRow, int toCol, boolean jump, int RW, int FB) {
    int[][] origBoard = new int[ROWS][COLS];
    for(int row = 0; row < ROWS; row++) origBoard[row] = board[row].clone();
    int piece     = board[fromRow][fromCol];
    int promotion = 0;
    
    board[fromRow][fromCol] = EMPTY;
    board[skipRow][skipCol] = EMPTY;
    if(toRow == (0 + 7 * (1 - RW)) && piece == 1 + RW) { // King promotion.
      promotion = 1;
      board[toRow][toCol] = 3 + RW;
    }
    else { // No promotion, remain regular piece.
      board[toRow][toCol] = piece;
    }
    
    if (promotion != 1) exploitMoves(toRow, toCol, RW, jump);
    
    int[][] newBoard = new int[ROWS][COLS];
    for(int row = 0; row < ROWS; row++) newBoard[row] = board[row].clone();
    board = origBoard;
    legalMoves.add(new CheckersState(newBoard));
    newBoard = null;
    origBoard = null;
    return jump;
  }
  
  private void listUpdate(int fromRow, int fromCol, int toRow, int toCol, 
      int[][] newBoard, int RW) {
    CheckersState newState = new CheckersState(newBoard);
    legalMoves.add(newState);
    newState = null;
  }

  public int evaluate(int player) {
    return defense(player);
    //return random();
  }
  
  private int[] neighbor(int row, int col, int diag, int RW, int FB) {
    int neighbor = (RW + FB) % 2; // 1 == red fw, white bw; 0 == red bw, white fw
    int[] coords = new int[2];
    
    if (diag == 0) 
      coords = (neighbor == 1)? neighborList(row, col, DOWNRIGHT) : 
        neighborList(row, col, UPLEFT);
    else if (diag == 1)
      coords = (neighbor == 1)? neighborList(row, col, DOWNLEFT) : 
        neighborList(row, col, UPRIGHT);
    return coords;
  }
  
  private int[] neighborList(int row, int col, int neighbor) {
    int[] nbrCoords = {OUTOFBOUNDS, OUTOFBOUNDS};
    
    switch (neighbor) {
    case UPLEFT:
      if ((row - 1) >= 0) {
        if (row % 2 == 0) {
          nbrCoords[0] = row - 1;
          nbrCoords[1] = col; // moving downleft from even row
        }
        else if (row % 2 == 1 && (col - 1) >= 0) {
          nbrCoords[0] = row - 1;
          nbrCoords[1] = col - 1; // from odd row
        }
      }
      break;
    case UPRIGHT: 
      if ((row - 1) >= 0) {
        if (row % 2 == 0 && (col + 1) < COLS) {
          nbrCoords[0] = row - 1;
          nbrCoords[1] = col + 1; // moving downright from even row
        }
        else if (row % 2 == 1) {
          nbrCoords[0] = row - 1;
          nbrCoords[1] = col; // from odd row
        }
      }
      break;
    case DOWNRIGHT: 
      if ((row + 1) < ROWS) {
        if (row % 2 == 0 && (col + 1) < COLS) {
          nbrCoords[0] = row + 1;
          nbrCoords[1] = col + 1; // moving downright from even row
        }
        else if (row % 2 == 1) {
          nbrCoords[0] = row + 1;
          nbrCoords[1] = col; // from odd row
        }
      }
      break;
    case DOWNLEFT:
      if ((row + 1) < ROWS) {
        if (row % 2 == 0) {
          nbrCoords[0] = row + 1;
          nbrCoords[1] = col; // moving downleft from even row
        }
        else if (row % 2 == 1 && (col - 1) >= 0) {
          nbrCoords[0] = row + 1;
          nbrCoords[1] = col - 1; // from odd row
        }
      }
      break;
    }
    
    return nbrCoords;
  }
  
  
  /*
   * ========================= EVALUATION FUNCTIONS =========================
   */
    
  /*
   * m123456 and lower
   */
  
  private int m12(int player) {
    return totalPieces(player) + 2 * kingPieces(player);
  }
  
  private int m34(int player) {
    return defense(player) + 1 * defVsKings(player);
  }
  
  private int m56(int player) {
    return sideDefense(player) + 2 * dynamicPos(player);
  }
  
  private int m3456(int player) {
    return m56(player) + 1 * m34(player);
  }
  
  private int m123456(int player) {
    return m12(player) + 4 * m3456(player);
  } 
  
  /*
   * 0: Random
   */
  
  private int random() {
    int num = 0 + (int)(Math.random() * ((30 - 0) + 1));
    return num;
  }
    
  /*
   * 1: Single Pieces Count
   */
  private int totalPieces(int player) {
    int currPlyrPieces = 0;
    int oppPlyrPieces  = 0;
    for (int row = 0; row < ROWS; row++) {
      for (int col = 0; col < COLS ; col++) {
        if (board[row][col] != EMPTY ) { // not empty
          if (board[row][col] % 2 == 1) { // red piece
            if (player + 1 == RED) currPlyrPieces++; // player is red
            else if (player + 1 == WHITE) oppPlyrPieces++; // player is white
          }
          if (board[row][col] % 2 == 0) { // white piece
            if (player + 1 == RED) oppPlyrPieces++; // player is red
            else if (player + 1 == WHITE) currPlyrPieces++; // player is white
          }
        }
      }
    }
    return (currPlyrPieces - oppPlyrPieces);
  }
  
  /*
   * 2: King Pieces Count
   */
  private int kingPieces(int player) {
    int currPlyrKings = 0;
    int oppPlyrKings  = 0;
    for (int row = 0; row < ROWS; row++) {
      for (int col = 0; col < COLS ; col++) {
        if (board[row][col] != EMPTY ) { // not empty
          if (board[row][col] == KINGRED) { // king is red
            if (player + 1 == RED) currPlyrKings++; // player is red
            else if (player + 1 == WHITE) oppPlyrKings++; // player is white
          }
          else if (board[row][col] == KINGWHITE) { // king is white
            if (player + 1 == WHITE) currPlyrKings++; // player is white
            else if (player + 1 == RED) oppPlyrKings++;  // player is red
          }
        }
      }
    }
    return (oppPlyrKings - currPlyrKings);
  }
  
  /*
   * 3: Defense
   */
  private int defense(int player) {
    int defense = 0;
    for (int row = 0; row < ROWS; row++) {
      for (int col = 0; col < COLS ; col++) {
        if (board[row][col] != EMPTY) { // check if cell is empty
          if ((player + 1) == RED && board[row][col] % 2 == 1) { // player is red
            int[] nbrCoords = neighborList(row, col, UPLEFT);
            if (nbrCoords[0] != -1 && board[nbrCoords[0]][nbrCoords[1]] % 2 == 1) 
              defense++;
            nbrCoords = neighborList(row, col, UPRIGHT); 
            if (nbrCoords[0] != -1 && board[nbrCoords[0]][nbrCoords[1]] % 2 == 1) 
              defense++;
          }
          else if ((player + 1) == WHITE && board[row][col] % 2 == 0) { // player is white
            int[] nbrCoords = neighborList(row, col, DOWNLEFT);
            if (nbrCoords[0] != -1 && 
                board[nbrCoords[0]][nbrCoords[1]] != EMPTY &&
                board[nbrCoords[0]][nbrCoords[1]] % 2 == 0) defense++;
            nbrCoords = neighborList(row, col, DOWNRIGHT); 
            if (nbrCoords[0] != -1 && 
                board[nbrCoords[0]][nbrCoords[1]] != EMPTY &&
                board[nbrCoords[0]][nbrCoords[1]] % 2 == 0) defense++;
          }
        }// end check if cell empty
      }
    }
    return defense;
  }
  
  /*
   * 4: Defense Against Kings
   */
  private int defVsKings(int player) {
    int defense  = 0;
    int numKings = 0;
    for (int row = 0; row < ROWS; row++) {
      for (int col = 0; col < COLS ; col++) {
        if (board[row][col] != EMPTY) { // check if empty cell
          if ((player + 1) == RED) { // player is red
            if (board[row][col] % 2 == 1) {
              int[] nbrCoords = neighborList(row, col, UPLEFT);
              if (nbrCoords[0] != -1 && 
                  board[nbrCoords[0]][nbrCoords[1]] % 2 == 1) 
                defense++;
              nbrCoords = neighborList(row, col, UPRIGHT); 
              if (nbrCoords[0] != -1 && 
                  board[nbrCoords[0]][nbrCoords[1]] % 2 == 1) 
                defense++;
            }
            else if (board[row][col] == KINGWHITE) {
              numKings++; // piece is white king
            }
          }
          else if ((player + 1) == WHITE) {
            if (board[row][col] % 2 == 0) {
              int[] nbrCoords = neighborList(row, col, DOWNLEFT);
              if (nbrCoords[0] != -1 && board[nbrCoords[0]][nbrCoords[1]] != EMPTY && 
                  board[nbrCoords[0]][nbrCoords[1]] % 2 == 0) 
                defense++;
              nbrCoords = neighborList(row, col, DOWNRIGHT); 
              if (nbrCoords[0] != -1 && board[nbrCoords[0]][nbrCoords[1]] != EMPTY && 
                  board[nbrCoords[0]][nbrCoords[1]] % 2 == 0) 
                defense++;
            }
            else if (board[row][col] == KINGRED){
              numKings++;
            }
          }
        } // end check if empty cell
      }
    }
    return numKings * defense;
  }
  
  /*
   * 5: Dynamic Defense on Sides
   */
  private int sideDefense(int player) {
    int defense = 0;
    for (int row = 0; row < ROWS; row++) {
      for (int col = 0; col < COLS; col++) {
        if (board[row][col] != EMPTY  && 
            ((row % 2 == 0 && col == 3) || (row % 2 == 1 && col == 0))) { // first or last column
          if (player + 1 == RED && board[row][col] % 2 == 1) 
            defense += (col + 1);
          else if (player + 1 == WHITE && board[row][col] % 2 == 0) 
            defense += (col + 1);
        }
      }
    }
    return defense;
  }
  
  /*
   * 6: Dynamic Position
   */
  private int dynamicPos(int player) {
    int numPossibleMoves = 0;
    for (int row = 0; row < ROWS; row++) {
      for (int col = 0; col < COLS ; col++) {
        if (board[row][col] != EMPTY) { // check if empty cell
          if (board[row][col] % 2 == 1 && (player + 1) == RED) { // red player
            if (board[row][col] == KINGRED) { // red king, check backwards moves
              if (isPossibleMove(row, col, UPLEFT)) numPossibleMoves++;
              if (isPossibleMove(row, col, UPRIGHT)) numPossibleMoves++;         
            }
            if (isPossibleMove(row, col, DOWNLEFT)) numPossibleMoves++;   
            if (isPossibleMove(row, col, DOWNRIGHT)) numPossibleMoves++;   
          }
          else if (board[row][col] % 2 == 0 && (player + 1) == WHITE) { // white player
            if (board[row][col] == KINGWHITE) {// white king, check backwards moves
              if (isPossibleMove(row, col, DOWNLEFT)) numPossibleMoves++;   
              if (isPossibleMove(row, col, DOWNRIGHT)) numPossibleMoves++;              
            }
            if (isPossibleMove(row, col, UPLEFT)) numPossibleMoves++;
            if (isPossibleMove(row, col, UPRIGHT)) numPossibleMoves++; 
          }
        } // end empty cell check
      } // end for col
    } // end for row
    return numPossibleMoves;
  }
  
  // helper method for 6: Dynamic position
  private boolean isPossibleMove(int row, int col, int direction) {
    int[] nbrCoords = neighborList(row, col, direction);
    if (nbrCoords[0] != -1 && board[nbrCoords[0]][nbrCoords[1]] == EMPTY) 
      return true;
    else 
      return false;
  }
   
  /* 
   * 7: Offense Single Pieces
   */
  private int offense(int player) {
    int currPlyrOff = 0;
    int oppPlyrOff  = 0;
    for (int row = 0; row < ROWS; row++) {
      for (int col = 0; col < COLS; col++) {
        if (board[row][col] != EMPTY) { // check for empty cell 
          if (board[row][col] == RED) { // red piece
            if (player + 1 == RED) currPlyrOff += row; // player is red
            if (player + 1 == WHITE) oppPlyrOff += (8 - row); // player is white
          }
          else if (board[row][col] == WHITE) { // white piece
            if (player + 1 == RED) oppPlyrOff += row; // player is red
            if (player + 1 == WHITE) currPlyrOff += (8 - row); // player is white
          }
        } // end empty cell check
      } // end for col
    } // end for row
    return currPlyrOff - oppPlyrOff;
  }
  
  
  /*
   * ========================== SETTERS AND GETTERS ==========================
   */
  
//  public void setValue(int value) {
//    this.value = value;
//  }
//  
//  public int getValue() {
//    return value;
//  }
  
  public List<CheckersState> getLegalMoves() {
    return legalMoves;
  }
  
  public boolean hasLegalMoves() {
    return !legalMoves.isEmpty();
  }
    
  public String toString() {
    StringBuffer str = new StringBuffer();
    String piece = "-";
    
    str.append("  Col | 0\t1\t2\t3\t4\t5\t6\t7");
    str.append("\n------+------------------------------------------"
        + "----------------------\n");
    for (int row = 0; row < board.length; row ++) {
      str.append("Row " + (row) + " |\t");
      for (int col = 0; col < board[row].length; col ++) {
        if (row % 2 == 0) str.append("--\t");
        switch(board[row][col]) {
        case EMPTY:
          piece = "--";
          break;
        case RED:
          piece = "R ";
          break;
        case WHITE:
          piece = "W ";
          break;
        case KINGRED:
          piece = "RK";
          break;
        case KINGWHITE:
          piece = "WK";
          break;
        }
        str.append(piece + "\t");
        if (row % 2 == 1) str.append("--\t");
      }
      str.append("\n");
    }
    str.append("\n");
    return str.toString();
  }
  
}
