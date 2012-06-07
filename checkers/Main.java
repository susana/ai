package checkers;

public class Main {
  
  static final int
    TOTALMOVES  = 100,
    DIM         = 8,
    RED         = 0,
    WHITE       = 1;
  
  // takes in 8*8 board and turns it into a 8*4 board
  public static int[][] createCompactBoard(int[][] originalBoard) {
    int     newCol   = 0;
    int[][] newBoard = new int[8][4];
    
    for (int row = 0; row < 8; row++) {
      for (int col = 0; col < 8; col ++) {
        if ((row + col) % 2 == 1) {
          newBoard[row][newCol] = originalBoard[row][col];
          newCol++;
        }
      }
      newCol = 0;
    }
    originalBoard = null;
    return newBoard;
  }
  
  public static void singleMove(int[][] initialState, int maxDepth) {    
    CheckersState gameState = new CheckersState(initialState);
    System.out.println(gameState.toString());
    GameTree tree = new GameTree(gameState, maxDepth, RED);
    gameState = tree.getMove(RED);
    System.out.println(gameState.toString());
    
  }

  public static void main(String args[]) {    
    int maxDepth = 10;
    int[][] board = {
        // columns 0  1  2  3  4  5  6  7
        new int[] {0, 1, 0, 1, 0, 1, 0, 1}, // row 0
        new int[] {1, 0, 1, 0, 1, 0, 1, 0}, // row 1
        new int[] {0, 1, 0, 1, 0, 1, 0, 1}, // row 2
        new int[] {0, 0, 0, 0, 0, 0, 0, 0}, // row 3
        new int[] {0, 0, 0, 0, 0, 0, 0, 0}, // row 4
        new int[] {2, 0, 2, 0, 2, 0, 2, 0}, // row 5
        new int[] {0, 2, 0, 2, 0, 2, 0, 2}, // row 6
        new int[] {2, 0, 2, 0, 2, 0, 2, 0}  // row 7
    };
    
    int[][] newBoard = createCompactBoard(board);
    board = null;
    
    singleMove(newBoard, maxDepth);
  }
}
