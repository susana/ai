package checkers;

public class GameTree {
  
  private CheckersState root;
  private int           maxDepth;
  private int           maxPlayer;
  
  public GameTree(CheckersState checkers, int maxDepth, int maxPlayer) {
    root           = checkers;
    this.maxDepth  = maxDepth;
    this.maxPlayer = maxPlayer;
  }
  
  public void setRoot(CheckersState gameState) {
    root = gameState;
  }


  private int minimax(CheckersState root, int player, int depth) {
    if (depth == maxDepth) {
      System.out.println("LEAF\tUP\tDepth " + depth + "\tValue " + root.evaluate(player));
      return root.evaluate(player);
    }
    int alpha = Integer.MIN_VALUE;
    if (!root.hasLegalMoves()) root.expand(player);
    for(CheckersState child : root.getLegalMoves()) {
      alpha = Math.max(alpha, -1 * minimax(child, (player == 0)? 1 : 0, depth+1));
    }
    
    return alpha;
  }
  
  private int alphabeta(CheckersState root, int depth, int alpha, int beta, int player) {
    if (depth == maxDepth) {
      //System.out.println("LEAF\tUP\tDepth " + depth + "\tValue " + root.evaluate(player));
      return root.evaluate(player);
    }
    if (!root.hasLegalMoves()) root.expand(player);
    if (player == maxPlayer) {
      for(CheckersState child : root.getLegalMoves()){
        alpha = Math.max(alpha, alphabeta(child, depth + 1, alpha, beta, (player == 0)? 1 : 0));
        if (beta <= alpha) break;
      }
      return alpha;
    }
    else {
      for(CheckersState child : root.getLegalMoves()){
        beta = Math.min(beta, alphabeta(child, depth + 1, alpha, beta, (player == 0)? 1 : 0));
        if (beta <= alpha) break;
      }
      return beta;
    }
    
  }
  
  public CheckersState getMove(int player) {
    int depth = 0;
    int bestVal = Integer.MIN_VALUE;
    int tempVal = Integer.MIN_VALUE;
    CheckersState bestMove = null;
    this.root.expand(player);
    for (CheckersState child : root.getLegalMoves()) {
      tempVal = alphabeta(root, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, player);
      System.out.println(tempVal);
      if (tempVal > bestVal) {
        bestVal = tempVal;
        bestMove = child;
      }
    }
    System.out.println(bestVal);
    return bestMove;
  }
}
