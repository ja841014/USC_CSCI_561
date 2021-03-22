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
//////
// [not finished] 
// 怎麼判斷勝負
// 改座標體系
// eval function
public class minmax {
	
	public static void main(String[] args) throws IOException {
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
		
		// 這裡很怪 我是不是應該都要先走 => myturn = true
		if(myColor.equals("BLACK")) {
			opponentColor = "WHITE";
		}
		else {
			opponentColor = "BLACK";
		}
		
//		long startTime = System.currentTimeMillis();
		Board resultBoard = minimax(9, true, Integer.MIN_VALUE, Integer.MAX_VALUE, myColor, opponentColor, board);
//		long endTime = System.currentTimeMillis(); //获取结束时间
		
		
		Board testwhite = board;
		Board testblack = board;
		int turn = 1;
//		///////TEST/////
//		while(testwhite.endGame() == false && testblack.endGame() == false ) {
//			System.out.println("turn: " + turn);
//			//黑先
//			testwhite = minimax(9, true, Integer.MIN_VALUE, Integer.MAX_VALUE, "BLACK", "WHITE", testblack);
//			testwhite.print();
//			System.out.println();
//			testblack = minimax(3, true, Integer.MIN_VALUE, Integer.MAX_VALUE, "WHITE", "BLACK", testwhite);
//			testblack.print();
//			System.out.println();
//			turn++;
//		}
		///////TEST/////
		
//		System.out.println("TIME：" + (endTime - startTime)+ "msec"); 
//		
		List<String> path =  resultBoard.map.get(resultBoard.endNode).paths;
		String movingType = moveType(path);
		writeOutFile(fileWriter, path, movingType);
//		
//		for(String s:  path) {
//			System.out.println(s);
//		}
		
		
		
	}
	
	private static void writeOutFile(FileWriter fileWriter, List<String> path, String movingType) throws IOException {
		Map<String, String> dictMap = new HashMap<>();
		dictMap.put("0", "a");
		dictMap.put("1", "b");
		dictMap.put("2", "c");
		dictMap.put("3", "d");
		dictMap.put("4", "e");
		dictMap.put("5", "f");
		dictMap.put("6", "g");
		dictMap.put("7", "h");
		// move
		if(movingType.equals("E")) {
			outputMove(fileWriter, movingType, path, dictMap);
		}
		else {
			outputJump(fileWriter, movingType, path, dictMap);
		}
		fileWriter.close();
	}
	
	private static void outputJump(FileWriter fileWriter, String movingType, List<String> path, Map<String, String> dictMap) throws IOException {
		
		List<String[]> intData = processData(path);
		for(int i = 1; i < intData.size(); i++) {
			fileWriter.write(movingType);
			fileWriter.write(" ");
			String first1 = intData.get(i - 1)[0];
			String second1 = intData.get(i - 1)[1];
			fileWriter.write(transform(first1, second1, dictMap));
			fileWriter.write(" ");
			String first2 = intData.get(i)[0];
			String second2 = intData.get(i)[1];
			fileWriter.write(transform(first2, second2, dictMap));
			
			
			if(i == intData.size() - 1) {
				break;
			}
			fileWriter.write("\n");
		}
	}

	
	
	private static void outputMove(FileWriter fileWriter, String movingType, List<String> path, Map<String, String> dictMap) throws IOException {
		fileWriter.write(movingType);
		fileWriter.write(" ");
		List<String[]> intData = processData(path);
		for(int i = 0; i < intData.size(); i++) {
			String first = intData.get(i)[0];
			String second = intData.get(i)[1];
			fileWriter.write(transform(first, second, dictMap));
			if(i == intData.size() - 1) {
				break;
			}
			fileWriter.write(" ");
		}
	}
	
	
	private static String transform(String row, String col, Map<String, String> dictMap) {
		String ans = "";
		ans = dictMap.get(col);
		int tmp = (Integer.parseInt(row) - 8) * -1;
		ans = ans + tmp;
		return ans;
	}
	
	private static List<String[]> processData(List<String> path) {
		List<String[]> ls = new ArrayList<>();
		for(int i = 0; i < path.size(); i++) {
			String[] s1 = path.get(i).split(",");
			ls.add(s1);
		}
		return ls;
	}
	
	
	private static String moveType(List<String> path) {
		String[] s1 = path.get(0).split(",");
		String[] s2 = path.get(1).split(",");
		int diff = Math.abs( Integer.parseInt(s1[0]) - Integer.parseInt(s2[0]) );
		// Move Type
		if(diff == 1) {
			return "E";
		}
		// Jump Type
		else {
			return "J";
		}
	}
	
	private static Board minimax(int depth, boolean myTurn, int alpha, int beta, String myColor, String opponentColor, Board board) {
		if(depth == 0) {
//			board.statsFunc();
			
			board.eval(myColor);		
//			System.out.println("EVAL ORG:"+ board.eval);
//			System.out.println("curBoard.blackLs.size()" + board.blackLs.size());
//			System.out.println("curBoard.whiteLs.size()" + board.whiteLs.size());
//			System.out.println("board.blackKing:"+ board.blackKing);
//			System.out.println("board.whiteKing:"+ board.whiteKing);
//			board.print();
//			System.out.println();
			return board;
		}
		
		if(myTurn) {
			int maxEval = Integer.MIN_VALUE;
			// 這裡需要改 bestMove == board 嗎？？？？   要 必續傳
			Board bestMove= board;

			List<Node> ls = determineColor(board, myTurn, myColor, opponentColor);
			
			List<Board> allMoves = getAllMoves(board, ls);
			// null check => check if "board" is the end of the Game
			if(allMoves.isEmpty()) {
				bestMove.eval(myColor);
//				System.out.println("EVAL:"+ board.eval);
//				System.out.println("curBoard.blackLs.size()" + board.blackLs.size());
//				System.out.println("curBoard.whiteLs.size()" + board.whiteLs.size());
//				System.out.println("board.blackKing:"+ board.blackKing);
//				System.out.println("board.whiteKing:"+ board.whiteKing);
//				board.print();
//				System.out.println();
				return bestMove;
			}
			
			for(Board curBoard: allMoves) {
				Board tmp = minimax(depth - 1, false, alpha, beta, myColor, opponentColor, curBoard);
				
				maxEval = Math.max(maxEval, tmp.eval);
				/////////
				// a b //
				if(maxEval >= beta) {
					// NOT SURE return tmp or curBoard
					return tmp;
				}
				else {
					alpha = Math.max(alpha, maxEval);
				}
				////////
				
				if(maxEval == tmp.eval) {
					bestMove = curBoard;
				}
				
			}
			return bestMove;
		}
		else {
			int minEval = Integer.MAX_VALUE;
			Board bestMove= board;
			List<Node> ls = determineColor(board, myTurn, myColor, opponentColor);
			
			List<Board> allMoves = getAllMoves(board, ls);
			// null check => check if "board" is the end of the Game
			if(allMoves.isEmpty()) {
				bestMove.eval(myColor);
//				System.out.println("EVAL:"+ board.eval);
//				System.out.println("curBoard.blackLs.size()" + board.blackLs.size());
//				System.out.println("curBoard.whiteLs.size()" + board.whiteLs.size());
//				System.out.println("board.blackKing:"+ board.blackKing);
//				System.out.println("board.whiteKing:"+ board.whiteKing);
//				board.print();
//				System.out.println();
				return bestMove;
			}
			
			for(Board curBoard: allMoves) {
				Board tmp = minimax(depth - 1, true, alpha, beta, myColor, opponentColor, curBoard);
				
				
				minEval = Math.min(minEval, tmp.eval);
				// a b //
				if(minEval <= alpha) {
					// NOT SURE return tmp or curBoard
					return tmp;
				}
				else {
					beta = Math.min(beta, minEval);
				}
				///////
				
				
				if(minEval == tmp.eval) {
					bestMove = curBoard;
				}
			}
			return bestMove;
		}
	}
	
	private static List<Node> determineColor(Board board, boolean myTurn, String myColor, String opponentColor) {
		List<Node> ls;
		
		if(myTurn) {
			if(myColor.equals("WHITE")) {
				ls = board.whiteLs;
			}
			else {
				ls = board.blackLs;
			}
		}
		else {
			if(opponentColor.equals("WHITE")) {
				ls = board.whiteLs;
			}
			else {
				ls = board.blackLs;
			}
		}
		
		return ls;
	}
	
	
	private static List<Board> getAllMoves(Board board, List<Node> ls) {
		
		List<Board> jumpMoveBoards = new ArrayList<>();
		List<Board> goMoveBoards = new ArrayList<>();
		// go through every Node in our side node
		for (Node curNode: ls) {
			List<Board> nextMoveBoards = new ArrayList<>();
			// get this node all valid moves
			nextMoveBoards.addAll(validMoves(board, curNode));
			// not empty
			if(nextMoveBoards.isEmpty() == false) {
				if(nextMoveBoards.get(0).mode.equals("jump") ) {
					jumpMoveBoards.addAll(nextMoveBoards);
				}
				else {
					goMoveBoards.addAll(nextMoveBoards);
				}
			}

		}
		if(jumpMoveBoards.isEmpty() == false) {
			return jumpMoveBoards;
		}
		return goMoveBoards;
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
			if(tmpLeftJump.isEmpty() == false) {
				pathLeft.remove(pathLeft.size() - 1);
				jumpOverLeft.remove(jumpOverLeft.size() - 1);
			}
			List<Board> tmpRightJump = goJump(color,1, 1,isKing, curNode.row, curNode.col, curboard, jumpOverRight, pathRight);
			if(tmpRightJump.isEmpty() == false) {
				pathRight.remove(pathRight.size() - 1);
				jumpOverRight.remove(jumpOverRight.size() - 1);
			}
			
			////
			if(tmpLeftJump.isEmpty()&& tmpRightJump.isEmpty()) {
				List<Board> tmpLeft = go(color, 1, -1, isKing, curNode.row, curNode.col, curboard, jumpOverLeft, pathLeft);
				List<Board> tmpRight = go(color, 1, 1, isKing, curNode.row, curNode.col, curboard, jumpOverRight, pathRight);
				validPosBoards.addAll(tmpLeft);

				validPosBoards.addAll(tmpRight);
			}
			else {
				validPosBoards.addAll(tmpLeftJump);

				validPosBoards.addAll(tmpRightJump);
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
			if(tmpLeftJump.isEmpty() == false) {
				pathLeft.remove(pathLeft.size() - 1);
				jumpOverLeft.remove(jumpOverLeft.size() - 1);
			}
			List<Board> tmpRightJump = goJump(color, -1, 1,isKing, curNode.row, curNode.col, curboard, jumpOverRight, pathRight);
			if(tmpRightJump.isEmpty() == false) {
				pathRight.remove(pathRight.size() - 1);
				jumpOverRight.remove(jumpOverRight.size() - 1);
			}
			if(tmpLeftJump.isEmpty() && tmpRightJump.isEmpty()) {
				List<Board> tmpLeft = go(color, -1, -1, isKing, curNode.row, curNode.col, curboard, jumpOverLeft, pathLeft);
				List<Board> tmpRight = go(color, -1, 1, isKing, curNode.row, curNode.col, curboard, jumpOverRight, pathRight);
				validPosBoards.addAll(tmpLeft);

				validPosBoards.addAll(tmpRight);
			}
			else {
				validPosBoards.addAll(tmpLeftJump);

				validPosBoards.addAll(tmpRightJump);
			}
		}
		else if(isKing) {
			List<String> pathUpLeft = new ArrayList<>();
			List<String> pathDownLeft = new ArrayList<>();
			List<String> pathUpRight = new ArrayList<>();
			List<String> pathDownRight = new ArrayList<>();
			pathUpLeft.add(curPos);
			pathDownLeft.add(curPos);
			pathUpRight.add(curPos);
			pathDownRight.add(curPos);

			List<Board> tmpUpLeftJump = goJumpKing(color, -1, -1, isKing, curNode.row, curNode.col, curboard, new ArrayList<>(), pathUpLeft);
			if(tmpUpLeftJump.isEmpty() == false) {
				pathUpLeft.remove(pathUpLeft.size() - 1);
			}
			List<Board> tmpUpRightJump = goJumpKing(color, -1, 1,isKing, curNode.row, curNode.col, curboard, new ArrayList<>(), pathUpRight);
			if(tmpUpRightJump.isEmpty() == false) {
				pathUpRight.remove(pathUpRight.size() - 1);
			}
			List<Board> tmpDownLeftJump = goJumpKing(color,1, -1, isKing, curNode.row, curNode.col, curboard, new ArrayList<>(), pathDownLeft);
			if(tmpDownLeftJump.isEmpty() == false) {
				pathDownLeft.remove(pathDownLeft.size() - 1);
			}
			List<Board> tmpDownRightJump = goJumpKing(color,1, 1,isKing, curNode.row, curNode.col, curboard, new ArrayList<>(), pathDownRight);
			if(tmpDownRightJump.isEmpty() == false) {
				pathDownRight.remove(pathDownRight.size() - 1);
			}
			
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
			// 若遇到連續旗子 不一定是敵軍！！！！ 或 卡牆 都不能走
			if( isValidPos( row + 2 * rowScalar, col + 2 * colScalar) == false || isAPieceThere(curboard, row + 2 * rowScalar, col + 2 * colScalar, path.get(0), path) == true ) {
				return result;
			}
			// 確定可以吃一顆旗子
			else {
				if(jumpOver.contains(nextStep)) {
					return result;
				}
				// 把所有被跳得點存起來,之後board清除掉
				jumpOver.add(nextStep);
				
				int nnRow = row + 2 * rowScalar;
				int nnCol = col + 2 * colScalar;
				
				path.add(nnRow + "," + nnCol);
				List<Board> tmpLeft = new ArrayList<>();
				List<Board> tmpRight = new ArrayList<>();
				
				if(color.equals("BLACK") && isKing == false) {
					
					tmpLeft = goJump(color, 1, -1, isKing, nnRow, nnCol, curboard, jumpOver, path);
					if(tmpLeft.isEmpty() == false) {
						path.remove(path.size() - 1);
						jumpOver.remove(jumpOver.size() - 1);
					}
					
					tmpRight = goJump(color, 1, 1, isKing, nnRow, nnCol, curboard, jumpOver, path);
					if(tmpRight.isEmpty() == false) {
						path.remove(path.size() - 1);
						jumpOver.remove(jumpOver.size() - 1);
					}
					
				}
				else if(color.equals("WHITE") && isKing == false) {
					
					tmpLeft = goJump(color, -1, -1, isKing, nnRow, nnCol, curboard, jumpOver, path);
					if(tmpLeft.isEmpty() == false) {
						path.remove(path.size() - 1);
						jumpOver.remove(jumpOver.size() - 1);
					}
					
					tmpRight = goJump(color,-1, 1, isKing, nnRow, nnCol, curboard, jumpOver, path);
					if(tmpRight.isEmpty() == false) {
						path.remove(path.size() - 1);
						jumpOver.remove(jumpOver.size() - 1);
					}
				}
				
				// 這個可能不需要
//				path.remove(path.size() - 1);
				
				if(tmpLeft.isEmpty() && tmpRight.isEmpty()) {
//					path.add(nnRow + "," + nnCol);
					Board newBoard = new Board();
					setUpNewBoard(newBoard, curboard.map, path.get(0), path.get(path.size() - 1), nnRow, nnCol, jumpOver, path, "jump");
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
			// 若遇到連續敵軍(不一定是敵軍 有可能是 友軍 那一樣不能跳) 或 卡牆 都不能走
			if( isValidPos( row + 2 * rowScalar, col + 2 * colScalar) == false || isAPieceThere(curboard, row + 2 * rowScalar, col + 2 * colScalar, path.get(0), path) == true ) {
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
				if(tmpDownLeft.isEmpty() == false) {
					path.remove(path.size() - 1);
					jumpOver.remove(jumpOver.size() - 1);
				}
				tmpDownRight = goJumpKing(color, 1, 1, isKing, nnRow, nnCol, curboard, jumpOver, path);
				if(tmpDownRight.isEmpty() == false) {
					path.remove(path.size() - 1);
					jumpOver.remove(jumpOver.size() - 1);
				}
				tmpUpLeft = goJumpKing(color, -1, -1, isKing, nnRow, nnCol, curboard, jumpOver, path);
				if(tmpUpLeft.isEmpty() == false) {
					path.remove(path.size() - 1);
					jumpOver.remove(jumpOver.size() - 1);
				}
				tmpUpRight = goJumpKing(color,-1, 1, isKing, nnRow, nnCol, curboard, jumpOver, path);
				
				if(tmpUpRight.isEmpty() == false) {
					path.remove(path.size() - 1);
					jumpOver.remove(jumpOver.size() - 1);
				}
				// 這個可能不需要
//				path.remove(path.size() - 1);
//				jumpOver.remove(jumpOver.size() - 1);
				
				if(tmpDownLeft.isEmpty() && tmpDownRight.isEmpty() && tmpUpLeft.isEmpty() && tmpUpRight.isEmpty() ) {
//					path.add(nnRow + "," + nnCol);
//					jumpOver.add(nextStep);
					Board newBoard = new Board();
					setUpNewBoard(newBoard, curboard.map, path.get(0), path.get(path.size() - 1), nnRow, nnCol, jumpOver, path, "jump");
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
			setUpNewBoard(newBoard, curboard.map, curStep, nextStep, nextRow, nextCol, null, path, "go");
			result.add(newBoard);
			return result;
		}
		return result;
	}	
		
	
	
	
	/**
	 * moving the node then create new board view
	 * */
	private static void setUpNewBoard(Board newBoard, Map<String, Node> preMap, String fromPos, String toPos, int toPosRow, int toPosCol, List<String> removeOpenent, List<String> path, String mode ) {
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
		newBoard.mode = mode;
		
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
		
		// 先移除舊的再新增新的可避免 (3,3)出發後來又回到(3,3)的狀況
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

				newBoard.whiteLs.add(newNode);
			}
			else {

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
	 * Make sure there do not have any piece AND THAT Location not equals to ORIGINAL
	 * */
	private static boolean isAPieceThere(Board board, int row, int col, String org, List<String> path) {
		String key = row + "," + col;
		// 要加這句嗎 && key.equals(org) == true 要加 因為他的確可以跳回原點
		if(board.map.containsKey(key) == true  ) {
			if(key.equals(org)) {
				return false;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * determine whether the next step will face the enemy or not
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
		
		String mode = "";
		
		String endNode;
		
		// test
		public boolean endGame() {
			if(blackLs.isEmpty() || whiteLs.isEmpty()) {
				return true;
			}
			return false;
		}
		//
		
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
		
		public int eval(String color) {
//			statsFunc();
//			int blackNor = blackLs.size() - blackKing;
//			int whiteNor = whiteLs.size() - whiteKing;
			int blackVal = 0;
			int whiteVal = 0;
			
			for(Node blackNode: blackLs) {
				if(blackNode.isKing == true) {
					blackVal = blackVal + 15;
					blackKing++;
				}
				else {
					blackVal = blackVal + 5 + blackNode.row;
				}
			}
			
			for(Node whiteNode : whiteLs) {
				if(whiteNode.isKing == true) {
					whiteVal = whiteVal + 15;
					whiteKing++;
				}
				else {
					whiteVal = whiteVal + 5 + (7 - whiteNode.row);
				}
			}
			
			if(color.equals("WHITE")) {
				eval = whiteVal - blackVal;
			}
			else {
				eval = blackVal - whiteVal;
			}
			
			
			return eval;
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
