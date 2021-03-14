import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

// black open the game, black always on the top
// 8*8 12棋子
// normal piece can only move forward (upper left/ upper right)
// king piece can move forward and backward (upper left/ upper right/ down left/ down right)
public class minmax {
	
	public static void main(String[] args) throws IOException, CloneNotSupportedException {
		File outFile = new File("output.txt");
		FileWriter fileWriter = new FileWriter(outFile);
		File file = new File("/Users/laicunhao/eclipse-workspace/CS561HW2/src/input.txt");
		Scanner scanner = new Scanner(file);
		// SINGLE OR GAME
		String mode = scanner.next();
		// BLACK or WHITE
		String myColor = scanner.next();
		float remainTime = Float.parseFloat(scanner.next());
		// Build up the board information 
		Board board = new Board();
		buildMap(board, scanner);
		
		// Board newBoard = new Board();
		// newBoard.setMap(assignNewBoard(newBoard, board.map));
		
		
		// Get all our side piece
		List<Node> ls;
		String opponentColor = "";
		boolean myTurn;
		if(myColor.equals("BLACK")) {
			opponentColor = "WHITE";
			myTurn = true;
		}
		else {
			myTurn = false;
			opponentColor = "BLACK";
		}
				
		Board resultBoard = minimax(2, myTurn, myColor, opponentColor, board);
		List<String> path =  resultBoard.map.get(resultBoard.endNode).paths;
		for(String s:  path) {
			System.out.println(s);
		}		
		
		
	}
	
	private static Board minimax(int depth, boolean myTurn, String myColor, String opponentColor, Board board) {
		if(depth == 0) {
			board.statsFunc();
			board.eval();
			return board;
		}
		if(myTurn) {
			int maxEval = Integer.MIN_VALUE;
			Board bestMove= null;
			List<Node> ls = determineColor(board, myColor, opponentColor);
			
			for(Board curBoard: getAllMoves(board, ls)) {
				Board tmp = minimax(depth - 1, false, myColor, opponentColor, curBoard);
				maxEval = Math.max(maxEval, tmp.eval);
				if(maxEval == tmp.eval) {
					bestMove = curBoard;
				}
			}
			return bestMove;
		}
		else {
			int minEval = Integer.MAX_VALUE;
			Board bestMove= null;
			List<Node> ls = determineColor(board, myColor, opponentColor);
			
			for(Board curBoard: getAllMoves(board, ls)) {
				Board tmp = minimax(depth - 1, true, myColor, opponentColor, curBoard);
				minEval = Math.min(minEval, tmp.eval);
				if(minEval == tmp.eval) {
					bestMove = curBoard;
				}
			}
			return bestMove;
		}
	}
	
	private static List<Node> determineColor(Board board, String myColor, String opponentColor) {
		List<Node> ls;
		if(myColor.equals("WHITE")) {
			ls = board.whiteLs;
		}
		else {
			ls = board.blackLs;
		}
		return ls;
	}
	
	
	private static List<Board> getAllMoves(Board board, List<Node> ls) {
		List<Board> nextMoveBoards = new ArrayList<>();
		// go through every Node in our side node
		for (Node curNode: ls) {
			// get this node all valid moves
			nextMoveBoards.addAll(validMoves(board, curNode));
//			System.out.println(curNode.row + "," + curNode.col);
//			for(Board b : nextMoveBoards) {
//				b.print();
//				System.out.println();
//			}
		}
		
		return nextMoveBoards;
	}
	
	
	/**
	 * valid moves of one node
	 * */
	private static List<Board> validMoves(Board curboard, Node curNode) {
		List<Board> validPosBoards = new ArrayList<>();
		String color = curNode.color;
		String curPos = curNode.row + "," + curNode.col;
		boolean isKing = curNode.isKing;
		if(color.equals("BLACK") && isKing == false) {
			List<String> jumpOverLeft = new ArrayList<>();
			List<String> pathLeft = new ArrayList<>();
			List<String> jumpOverRight = new ArrayList<>();
			List<String> pathRight = new ArrayList<>();
			pathLeft.add(curPos);
			pathRight.add(curPos);
			
			List<Board> tmpLeftJump = goJump(color,1, -1, isKing, curNode.row, curNode.col, curboard, jumpOverLeft, pathLeft);
			List<Board> tmpRightJump = goJump(color,1, 1,isKing, curNode.row, curNode.col, curboard, jumpOverRight, pathRight);
			if(tmpLeftJump.isEmpty()&& tmpRightJump.isEmpty()) {
				List<Board> tmpLeft = go(color, 1, -1, isKing, curNode.row, curNode.col, curboard, jumpOverLeft, pathLeft);
				List<Board> tmpRight = go(color, 1, 1, isKing, curNode.row, curNode.col, curboard, jumpOverRight, pathRight);
//				if(tmpLeft != null) {
					validPosBoards.addAll(tmpLeft);
//				}
//				if(tmpRight != null) {
					validPosBoards.addAll(tmpRight);
//				}
			}
			else {
//				if(tmpLeftJump != null) {
					validPosBoards.addAll(tmpLeftJump);
//				}
//				if(tmpRightJump != null) {
					validPosBoards.addAll(tmpRightJump);
//				}
			}
						
		}
		else if(color.equals("WHITE") && isKing == false) {
			List<String> jumpOverLeft = new ArrayList<>();
			List<String> pathLeft = new ArrayList<>();
			List<String> jumpOverRight = new ArrayList<>();
			List<String> pathRight = new ArrayList<>();
			pathLeft.add(curPos);
			pathRight.add(curPos);

			List<Board> tmpLeftJump = goJump(color, -1, -1, isKing, curNode.row, curNode.col, curboard, jumpOverLeft, pathLeft);
			List<Board> tmpRightJump = goJump(color, -1, 1,isKing, curNode.row, curNode.col, curboard, jumpOverRight, pathRight);
			if(tmpLeftJump.isEmpty() && tmpRightJump.isEmpty()) {
				List<Board> tmpLeft = go(color, -1, -1, isKing, curNode.row, curNode.col, curboard, jumpOverLeft, pathLeft);
				List<Board> tmpRight = go(color, -1, 1, isKing, curNode.row, curNode.col, curboard, jumpOverRight, pathRight);
//				if(tmpLeft != null) {
					validPosBoards.addAll(tmpLeft);
//				}
//				if(tmpRight != null) {
					validPosBoards.addAll(tmpRight);
//				}
			}
			else {
//				if(tmpLeftJump != null) {
					validPosBoards.addAll(tmpLeftJump);
//				}
//				if(tmpRightJump != null) {
					validPosBoards.addAll(tmpRightJump);
//				}
			}
		}
		else if(isKing) {
//			List<String> jumpOverLeft = new ArrayList<>();
			List<String> pathUpLeft = new ArrayList<>();
			List<String> pathDownLeft = new ArrayList<>();
//			List<String> jumpOverRight = new ArrayList<>();
			List<String> pathUpRight = new ArrayList<>();
			List<String> pathDownRight = new ArrayList<>();
			pathUpLeft.add(curPos);
			pathDownLeft.add(curPos);
			pathUpRight.add(curPos);
			pathDownRight.add(curPos);

			List<Board> tmpUpLeftJump = goJumpKing(color, -1, -1, isKing, curNode.row, curNode.col, curboard, new ArrayList<>(), pathUpLeft);
			List<Board> tmpUpRightJump = goJumpKing(color, -1, 1,isKing, curNode.row, curNode.col, curboard, new ArrayList<>(), pathUpRight);
			List<Board> tmpDownLeftJump = goJumpKing(color,1, -1, isKing, curNode.row, curNode.col, curboard, new ArrayList<>(), pathDownLeft);
			List<Board> tmpDownRightJump = goJumpKing(color,1, 1,isKing, curNode.row, curNode.col, curboard, new ArrayList<>(), pathDownRight);
			if(tmpUpLeftJump.isEmpty() && tmpUpRightJump.isEmpty() && tmpDownLeftJump.isEmpty() && tmpDownRightJump.isEmpty()) {
				List<Board> tmpUpLeft = go(color, -1, -1, isKing, curNode.row, curNode.col, curboard, new ArrayList<>(), pathUpLeft);
				List<Board> tmpUpRight = go(color, -1, 1, isKing, curNode.row, curNode.col, curboard, new ArrayList<>(), pathUpRight);
				List<Board> tmpDownLeft = go(color, 1, -1, isKing, curNode.row, curNode.col, curboard, new ArrayList<>(), pathDownLeft);
				List<Board> tmpDownRight = go(color, 1, 1, isKing, curNode.row, curNode.col, curboard, new ArrayList<>(), pathDownRight);
				validPosBoards.addAll(tmpUpLeft);
				validPosBoards.addAll(tmpUpRight);
				validPosBoards.addAll(tmpDownLeft);
				validPosBoards.addAll(tmpDownRight);
			}
			else {
				validPosBoards.addAll(tmpUpLeftJump);
				validPosBoards.addAll(tmpUpRightJump);
				validPosBoards.addAll(tmpDownLeftJump);
				validPosBoards.addAll(tmpDownRightJump);
			}
		}
		
		
		return validPosBoards;
	}
	// 黑色左是+1 -1 黑色又是 +1 +1 白色左是 -1 -1 白色右是 -1 +1
	private static List<Board> goJump(String color, int rowScalar, int colScalar, boolean isKing, int row, int col, Board curboard,  List<String> jumpOver, List<String> path) {
		List<Board> result = new ArrayList<>();
	 
		int nextStepRow = row + rowScalar;
		int nextStepCol = col + colScalar;
		String nextStep = nextStepRow + "," + nextStepCol;
		
		// first, check whether there is something can eat
		if( isValidPos(nextStepRow, nextStepCol) && isMeetEnemy(curboard, nextStepRow, nextStepCol, isKing, color) ) {
			// 若遇到連續敵軍 或 卡牆 都不能走
			if( isValidPos( row + 2 * rowScalar, col + 2 * colScalar) == false || isMeetEnemy(curboard, row + 2 * rowScalar, col + 2 * colScalar, isKing, color) == true ) {
				return result;
			}
			// 確定可以吃一顆旗子
			else {
				if(jumpOver.contains(nextStep)) {
					return result;
				}
				// 還有一個判斷式要判斷是不是走到底要變身ＫＩＮＧ
				// 把所有被跳得點存起來,之後board清除掉
				jumpOver.add(nextStep);
				
				int nnRow = row + 2 * rowScalar;
				int nnCol = col + 2 * colScalar;
				
				path.add(nnRow + "," + nnCol);
				List<Board> tmpLeft = new ArrayList<>();
				List<Board> tmpRight = new ArrayList<>();
				
				if(color.equals("BLACK") && isKing == false) {
					tmpLeft = goJump(color, 1, -1, isKing, nnRow, nnCol, curboard, jumpOver, path);
					tmpRight = goJump(color, 1, 1, isKing, nnRow, nnCol, curboard, jumpOver, path);
				}
				else if(color.equals("WHITE") && isKing == false) {
					tmpLeft = goJump(color, -1, -1, isKing, nnRow, nnCol, curboard, jumpOver, path);
					tmpRight = goJump(color,-1, 1, isKing, nnRow, nnCol, curboard, jumpOver, path);
				}
				
				// 這個可能不需要
				path.remove(path.size() - 1);
				
				if(tmpLeft.isEmpty() && tmpRight.isEmpty()) {
					path.add(nnRow + "," + nnCol);
					Board newBoard = new Board();
					setUpNewBoard(newBoard, curboard.map, path.get(0), path.get(path.size() - 1), nnRow, nnCol, jumpOver, path);
					result.add(newBoard);
					return result;
					
				}
				else {
					result.addAll(tmpLeft);
					result.addAll(tmpRight);
					return result;
				}
			}
		}		
		return result;
	}
	
	private static List<Board> goJumpKing(String color, int rowScalar, int colScalar, boolean isKing, int row, int col, Board curboard,  List<String> jumpOver, List<String> path) {
		List<Board> result = new ArrayList<>();
		 
		int nextStepRow = row + rowScalar;
		int nextStepCol = col + colScalar;
		String nextStep = nextStepRow + "," + nextStepCol;
		
		// first, check whether there is something can eat
		if( isValidPos(nextStepRow, nextStepCol) && isMeetEnemy(curboard, nextStepRow, nextStepCol, isKing, color) ) {
			// 若遇到連續敵軍 或 卡牆 都不能走
			if( isValidPos( row + 2 * rowScalar, col + 2 * colScalar) == false || isMeetEnemy(curboard, row + 2 * rowScalar, col + 2 * colScalar, isKing, color) == true ) {
				return result;
			}
			// 確定可以吃一顆旗子
			else {
				if(jumpOver.contains(nextStep)) {
					return result;
				}
				// 還有一個判斷式要判斷是不是走到底要變身ＫＩＮＧ
				// 把所有被跳得點存起來,之後board清除掉
				jumpOver.add(nextStep);
				
				int nnRow = row + 2 * rowScalar;
				int nnCol = col + 2 * colScalar;
				
				path.add(nnRow + "," + nnCol);
				List<Board> tmpUpLeft = new ArrayList<>();
				List<Board> tmpUpRight = new ArrayList<>();
				List<Board> tmpDownLeft = new ArrayList<>();
				List<Board> tmpDownRight = new ArrayList<>();
				
				
				tmpDownLeft = goJumpKing(color, 1, -1, isKing, nnRow, nnCol, curboard, jumpOver, path);
				tmpDownRight = goJumpKing(color, 1, 1, isKing, nnRow, nnCol, curboard, jumpOver, path);
				tmpUpLeft = goJumpKing(color, -1, -1, isKing, nnRow, nnCol, curboard, jumpOver, path);
				tmpUpRight = goJumpKing(color,-1, 1, isKing, nnRow, nnCol, curboard, jumpOver, path);
				
				
				// 這個可能不需要
				path.remove(path.size() - 1);
				
				if(tmpDownLeft.isEmpty() && tmpDownRight.isEmpty() && tmpUpLeft.isEmpty() && tmpUpRight.isEmpty() ) {
					path.add(nnRow + "," + nnCol);
					Board newBoard = new Board();
					setUpNewBoard(newBoard, curboard.map, path.get(0), path.get(path.size() - 1), nnRow, nnCol, jumpOver, path);
					result.add(newBoard);
					return result;
					
				}
				else {
					result.addAll(tmpDownLeft);
					result.addAll(tmpDownRight);
					result.addAll(tmpUpLeft);
					result.addAll(tmpUpRight);
					return result;
				}
			}
		}		
		return result;
	}
	
	
	// 黑色左是+1 -1 黑色又是 +1 +1 白色左是 -1 -1 白色右是 -1 +1
	private static List<Board> go(String color, int rowScalar, int colScalar, boolean isKing, int row, int col, Board curboard, List<String> jumpOver, List<String> path) {
		List<Board> result = new ArrayList<>();
		String curStep = row + "," + col;
		int nextRow = row + rowScalar;
		int nextCol = col + colScalar;
		String nextStep = nextRow + "," + nextCol;
		if( isValidPos(nextRow, nextCol) && curboard.map.containsKey(nextStep) == false  ) {
			// change map
			path.add(nextStep);
			Board newBoard = new Board();
			setUpNewBoard(newBoard, curboard.map, curStep, nextStep, nextRow, nextCol, null, path);
			result.add(newBoard);
			return result;
		}
		return result;
	}	
		
	/**
	 * refator
	 * */
	private static List<Board> goLeft(String color, boolean isKing, int row, int col, Board curboard, boolean continuousJump, List<String> jumpOver, List<String> path) {
//		List<Board> result = new ArrayList<>();
// 
//		String curStep = row + "," + col;
//		if(color.equals("BLACK")) {
//			int nextStepRow = row + 1;
//			int nextStepCol = col - 1;
//			String nextStep = nextStepRow + "," + nextStepCol;
//			
//			// first, check whether there is something can eat
//			if( isValidPos(row + 1, col - 1) ) {
//				if(isMeetEnemy(curboard, row + 1, col - 1, isKing, color) ) {
//					// 若遇到連續敵軍 或 卡牆 都不能走
//					if( isValidPos( row + 2, col - 2) == false|| isMeetEnemy(curboard, row + 2, col - 2, isKing, color) == true ) {
//						return null;
//					}
//					// 確定可以吃一顆旗子
//					else {
//						
//						// 還有一個判斷式要判斷是不是走到底要變身ＫＩＮＧ
//						// 把所有被跳得點存起來,之後board清除掉
//						jumpOver.add(nextStep);
//						
//						int nnRow = row + 2;
//						int nnCol = col - 2;
//						
//						path.add(nnRow + "," + nnCol);
//						List<Board> tmpLeft = goLeft(color, isKing, row + 2, col - 2, curboard, true, jumpOver, path);
//						
//						List<Board> tmpRight = goRight(color, isKing, row + 2, col - 2, curboard, true, jumpOver, path);
//						// 這個可能不需要
//						path.remove(path.size() - 1);
//						
//						if(tmpLeft == null && tmpRight == null) {
//							path.add(nnRow + "," + nnCol);
//							Board newBoard = new Board();
//							setUpNewBoard(newBoard, curboard.map, path.get(0), path.get(path.size() - 1), nnRow, nnCol, jumpOver, path);
//							result.add(newBoard);
//							return result;
//							
//						}
//						else {
//							result.addAll(tmpLeft);
//							result.addAll(tmpRight);
//							return result;
//						}
//					}
//				}
//				// simple step out, the nextStep is valid and empty position
//				else if(curboard.map.containsKey(nextStep) == false && continuousJump == false ) {
//					// change map
//					path.add(nextStep);
//					Board newBoard = new Board();
//					setUpNewBoard(newBoard, curboard.map, curStep, nextStep, nextStepRow, nextStepCol, null, path);
//					result.add(newBoard);
//					return result;
//				}
//				else {
//					return null;
//				}
//			}
//			else {
//				return null;
//			}
//		}
//		// color = WHITE
//		else {
//			int nextStepRow = row - 1;
//			int nextStepCol = col - 1;
//			String nextStep = nextStepRow + "," + nextStepCol;
//			
//			// first, check whether there is something can eat
//			if( isValidPos(row - 1, col - 1) ) {
//				if(isMeetEnemy(curboard, row - 1, col - 1, isKing, color) ) {
//					// 若遇到連續敵軍 或 卡牆 都不能走
//					if( isValidPos( row - 2, col - 2) || isMeetEnemy(curboard, row - 2, col - 2, isKing, color) ) {
//						return null;
//					}
//					// 確定可以吃一顆旗子
//					else {
//						// 把所有被跳得點存起來,之後board清除掉
//						jumpOver.add(nextStep);
//						
//						int nnRow = row - 2;
//						int nnCol = col - 2;
//						
//						path.add(nnRow + "," + nnCol);
//						List<Board> tmpLeft = goLeft(color, isKing, row - 2, col - 2, curboard, true, jumpOver, path);
//						
//						List<Board> tmpRight = goRight(color, isKing, row - 2, col - 2, curboard, true, jumpOver, path);
//						// 這個可能不需要
//						path.remove(path.size() - 1);
//						
//						if(tmpLeft == null && tmpRight == null) {
//							path.add(nnRow + "," + nnCol);
//							Board newBoard = new Board();
//							setUpNewBoard(newBoard, curboard.map, path.get(0), path.get(path.size() - 1), nnRow, nnCol, jumpOver, path);
//							result.add(newBoard);
//							return result;
//							
//						}
//						else {
//							if(tmpLeft != null) {
//								result.addAll(tmpLeft);
//							}
//							if(tmpRight != null) {
//								result.addAll(tmpRight);
//							}
//							return result;
//						}
//					}
//				}
//				// simple step out, the nextStep is valid and empty position
//				else if(curboard.map.containsKey(nextStep) == false && continuousJump == false ) {
//					// change map
//					path.add(nextStep);
//					Board newBoard = new Board();
//					setUpNewBoard(newBoard, curboard.map, curStep, nextStep, nextStepRow, nextStepCol, null, path);
//					result.add(newBoard);
//					return result;
//				}
//				else {
//					return null;
//				}
//			}
//			else {
//				return null;
//			}
//		}
		return null;
	}
	
	
	/**
	 * refator
	 * */
	private static List<Board> goRight(String color, boolean isKing, int row, int col, Board curboard, boolean continuousJump, List<String> jumpOver, List<String> path) {
//		List<Board> result = new ArrayList<>();
////		int row = curNode.row;
////		int col = curNode.col;
//		String curStep = row + "," + col;
//		if(color.equals("BLACK")) {
//			int nextStepRow = row + 1;
//			int nextStepCol = col + 1;
//			String nextStep = nextStepRow + "," + nextStepCol;
//			
//			// first, check whether there is something can eat
//			if( isValidPos(row + 1, col + 1) ) {
//				if(isMeetEnemy(curboard, row + 1, col + 1, isKing, color) ) {
//					// 若遇到連續敵軍 或 卡牆 都不能走
//					if( isValidPos( row + 2, col + 2) == false || isMeetEnemy(curboard, row + 2, col + 2, isKing, color) == true ) {
//						return null;
//					}
//					// 確定可以吃一顆旗子
//					else {
//						// 把所有被跳得點存起來,之後board清除掉
//						jumpOver.add(nextStep);
//						
//						int nnRow = row + 2;
//						int nnCol = col + 2;
//						
//						path.add(nnRow + "," + nnCol);
//						List<Board> tmpLeft = goLeft(color, isKing, row + 2, col + 2, curboard, true, jumpOver, path);
//						
//						List<Board> tmpRight = goRight(color, isKing, row + 2, col + 2, curboard, true, jumpOver, path);
//						// 這個可能不需要
//						path.remove(path.size() - 1);
//						
//						if(tmpLeft == null && tmpRight == null) {
//							path.add(nnRow + "," + nnCol);
//							Board newBoard = new Board();
//							setUpNewBoard(newBoard, curboard.map, path.get(0), path.get(path.size() - 1), nnRow, nnCol, jumpOver, path);
//							result.add(newBoard);
//							return result;
//							
//						}
//						else {
//							if(tmpLeft != null) {
//								result.addAll(tmpLeft);
//							}
//							if(tmpRight != null) {
//								result.addAll(tmpRight);
//							}
//							
//							
//							return result;
//						}
//					}
//				}
//				// simple step out, the nextStep is valid and empty position
//				else if(curboard.map.containsKey(nextStep) == false && continuousJump == false ) {
//					// change map
//					path.add(nextStep);
//					Board newBoard = new Board();
//					setUpNewBoard(newBoard, curboard.map, curStep, nextStep, nextStepRow, nextStepCol, null, path);
//					result.add(newBoard);
//					return result;
//				}
//				else {
//					return null;
//				}
//			}
//			else {
//				return null;
//			}
//		}
//		// color = WHITE
//		else {
//			int nextStepRow = row - 1;
//			int nextStepCol = col + 1;
//			String nextStep = nextStepRow + "," + nextStepCol;
//			
//			// first, check whether there is something can eat
//			if( isValidPos(row - 1, col + 1) ) {
//				if(isMeetEnemy(curboard, row - 1, col + 1, isKing, color) ) {
//					// 若遇到連續敵軍 或 卡牆 都不能走
//					if( isValidPos( row - 2, col + 2) || isMeetEnemy(curboard, row - 2, col + 2, isKing, color) ) {
//						return null;
//					}
//					// 確定可以吃一顆旗子
//					else {
//						// 把所有被跳得點存起來,之後board清除掉
//						jumpOver.add(nextStep);
//						
//						int nnRow = row - 2;
//						int nnCol = col + 2;
//						
//						path.add(nnRow + "," + nnCol);
//						List<Board> tmpLeft = goLeft(color, isKing, row - 2, col + 2, curboard, true, jumpOver, path);
//						
//						List<Board> tmpRight = goRight(color, isKing, row - 2, col + 2, curboard, true, jumpOver, path);
//						// 這個可能不需要
//						path.remove(path.size() - 1);
//						
//						if(tmpLeft == null && tmpRight == null) {
//							path.add(nnRow + "," + nnCol);
//							Board newBoard = new Board();
//							setUpNewBoard(newBoard, curboard.map, path.get(0), path.get(path.size() - 1), nnRow, nnCol, jumpOver, path);
//							result.add(newBoard);
//							return result;
//							
//						}
//						else {
//							result.addAll(tmpLeft);
//							result.addAll(tmpRight);
//							return result;
//						}
//					}
//				}
//				// simple step out, the nextStep is valid and empty position
//				else if(curboard.map.containsKey(nextStep) == false && continuousJump == false ) {
//					// change map
//					path.add(nextStep);
//					Board newBoard = new Board();
//					setUpNewBoard(newBoard, curboard.map, curStep, nextStep, nextStepRow, nextStepCol, null, path);
//					result.add(newBoard);
//					return result;
//				}
//				else {
//					return null;
//				}
//			}
//			else {
//				return null;
//			}
//		}
		return null;
	}
	
	
	
	/**
	 * moving the node then create new board view
	 * */
	private static void setUpNewBoard(Board newBoard, Map<String, Node> preMap, String fromPos, String toPos, int toPosRow, int toPosCol, List<String> removeOpenent, List<String> path ) {
		// Deep copy 一份 newMap
		newBoard.setMap(assignNewMap(newBoard, preMap));
		// 改棋子的位置, 取得”始動“的棋子
		Node movingNode = newBoard.map.get(fromPos);
		String color = movingNode.getColor();
		// 改座標
		movingNode.col = toPosCol;
		movingNode.row = toPosRow;
		
		// if the node reach the king row then change to king
		if(movingNode.isKing == false) {
			isValidKingThenChange(color, movingNode);
		}
		
		newBoard.belongNode = fromPos;
		newBoard.endNode = toPos;
		
		// 可能不需要path 但要remove element
		// [important] path 會不會被洗掉！！！！！！！！！！
		// 記錄從哪裡來 paths裡至少都會有兩個element
		for(String p : path) {
			movingNode.paths.add(p);
		}
		
		if(color.equals("BLACK") && toPosRow == 7) {
			movingNode.setIsKing(true);
		}
		else if(color.equals("WHITE") && toPosRow == 0) {
			movingNode.setIsKing(true);
		}
		
		newBoard.map.remove(fromPos);
		newBoard.map.put(toPos, movingNode);
		
		
		// removeOpenent 有可能是null
		if(removeOpenent != null) {
			for(String oppnent : removeOpenent) {
				Node removeNode = newBoard.map.get(oppnent);
				newBoard.map.remove(oppnent);
				if(color.equals("BLACK")) {
					newBoard.whiteLs.remove(removeNode);
				}
				else {
					newBoard.blackLs.remove(removeNode);
				}
			}
		}
		
		// do we need to create a new map that store the node which removed by us?
	}
	
	private static void isValidKingThenChange(String color, Node node) {
		if(color.equals("BLACK") && node.row == 7) {
			node.isKing = true;
		}
		else if(color.equals("WHITE") && node.row == 0) {
			node.isKing = true;
		}
	}
	
	
	/**
	 * Deep copy the Node Map
	 * */
	private static Map<String, Node> assignNewMap(Board newBoard, Map<String, Node> table) {
		Map<String, Node> newTable = new HashMap<>();
		for(Map.Entry<String, Node> entries: table.entrySet()) {
			int row = entries.getValue().row;
			int col = entries.getValue().col;
			Node newNode = new Node(row, col);
			newNode.color = entries.getValue().getColor();
			newNode.isKing = entries.getValue().getIsKing();
			if(newNode.color.equals("WHITE")) {
//				if(newNode.isKing == true) {
//					newBoard.whiteKing++;
//				}
//				else {
//					newBoard.whiteNorPiece++;
//				}
				newBoard.whiteLs.add(newNode);
			}
			else {
//				if(newNode.isKing == true) {
//					newBoard.blackKing++;
//				}
//				else {
//					newBoard.blackNorPiece++;
//				}
				newBoard.blackLs.add(newNode);
			}
			
			newTable.put(entries.getKey(), newNode);
		}
		return newTable;
	}
	
	/**
	 * Only determine whether the posistion is in the board
	 * */
	private static boolean isValidPos(int row, int col) {
		if(row < 8 && row >= 0 && col < 8 && col >= 0 ) {
			return true;
		}
		return false;
	}
	/**
	 * determine whether the next step will face the enemy
	 * */
	private static boolean isMeetEnemy(Board board, int row, int col, boolean isKing, String nodeColor) {
		String key = row + "," + col;
		if(nodeColor.equals("BLACK")) {
			if(board.map.containsKey(key) == true && board.map.get(key).color.equals("WHITE")  ) {
				return true;
			}
		}
		else {
			if(board.map.containsKey(key) == true && board.map.get(key).color.equals("BLACK")) {
				return true;
			}
		}
		
		return false;
	}
	
	public static void buildMap(Board board, Scanner scan) {
		Map<String, Node> map = new HashMap<>();
		for(int i = 0; i < 8; i++) {
			String curLine = scan.next();
			for(int j = 0; j < 8; j++) {
				char curChar = curLine.charAt(j);
				Node node = new Node(i, j);
				if(curChar == 'b' || curChar == 'B') {
					if(curChar == 'B') {
//						board.blackKing++;
						node.setColor("BLACK");
						node.setIsKing(true);
						
					}
					else {
						node.setColor("BLACK");
						node.setIsKing(false);
//						board.blackNorPiece++;
					}
					board.blackLs.add(node);
					map.put(i + "," + j, node);
				}
				else if(curChar == 'w' || curChar == 'W') {
					if(curChar == 'W') {
						node.setColor("WHITE");
						node.setIsKing(true);
//						board.whiteKing++;
					}
					else {
						node.setColor("WHITE");
						node.setIsKing(false);
//						board.whiteNorPiece++;
					}
					board.whiteLs.add(node);
					map.put(i + "," + j, node);
				}
				
				
			}
		}
		board.setMap(map);
	}
	
	
	public static class Node {
		int row;
		int col;
		private String color;
		private boolean isKing;
		List<String> paths;
		public Node(int row, int col) {
			this.row = row;
			this.col = col;
			this.color = "NULL";
			paths = new ArrayList<>();
		}
		public void setColor(String color) {
			this.color = color;
		}
		public String getColor() {
			return color;
		}
		public void setIsKing(boolean isKing) {
			this.isKing = isKing;
		}
		public boolean getIsKing() {
			return isKing;
		}

	}
	
	
	//	Store all BOARD information
	public static class Board {
		int blackKing = 0;
		int whiteKing = 0;
		int eval = 0;
		List<Node> blackLs;
		List<Node> whiteLs;
		Map<String, Node> map;
		// belong node mught not be helpful
		String belongNode;
		
		String endNode;
		
		
		public Board() {
			blackLs = new ArrayList<>();
			whiteLs = new ArrayList<>();
			map = new HashMap<>();
			belongNode = null;
			endNode = null;
		}
		
		public void statsFunc() {
			for(Node blackNode: blackLs) {
				if(blackNode.isKing == true) {
					blackKing++;
				}
			}
			for(Node whiteNode: whiteLs) {
				if(whiteNode.isKing == true) {
					whiteKing++;
				}
			}
		}
		
		public void eval() {
			int blackNor = blackLs.size() - blackKing;
			int whiteNor = whiteLs.size() - whiteKing;
			eval = blackNor - whiteNor + (blackNor * 2 - whiteNor * 2);
		}
		
		public List<Node> getBlackLs() {
			
			return blackLs;
		}
		public List<Node> getWhiteLs() {
			return whiteLs;
		}
		
		public void setMap(Map<String, Node>  m) {
			map = m;
		}
		
		public void print() {
			for(int i = 0; i < 8; i++) {
				for(int j = 0; j < 8; j++) {
					String pos = i + "," + j;
					if(map.containsKey(pos)) {
						Node node = map.get(pos);
						if(node.color.equals("BLACK")) {
							if(node.isKing == true) {
								System.out.print("B");
							}
							else {
								System.out.print("b");
							}
						}
						else {
							if(node.isKing == true) {
								System.out.print("W");
							}
							else {
								System.out.print("w");
							}
						}
					}
					else {
						System.out.print(".");
					}
				}
				System.out.println();
			}
		}
		
		
		// need a delete method.
		
	}
}
