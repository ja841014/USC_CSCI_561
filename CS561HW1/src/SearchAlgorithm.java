import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
//always be given a valid input.txt file 
//When the wagon moves vertically or horizontally (so north, south, east, or west), the movement cost is will be either 1 for BFS or 10 for UCS and A. 
//When the wagon moves diagonally (so northeast, northwest, southeast, or southwest), the movement cost is either 1 for BFS or 14 for UCS and A. 
// Ｘ往東正 Ｙ往南正
public class SearchAlgorithm {
	private final static int DIRECTION[][] = {{-1,-1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
	private static int COLS;
	private static int ROWS;
	public static void main(String[] args) throws IOException {
		File file = new File("/Users/laicunhao/eclipse-workspace/CS561HW1/src/test2.txt");
		File outFile = new File("output.txt");
		FileWriter fileWriter = new FileWriter(outFile);
		Scanner scanner = new Scanner(file);
		String algorithm = scanner.next();
		COLS = Integer.parseInt(scanner.next());
		ROWS = Integer.parseInt(scanner.next());
		int questionX = Integer.parseInt(scanner.next());
		int questionY = Integer.parseInt(scanner.next());
		// do change to program Xaxis and Y axis
		int[] start = {questionY, questionX};
		int maxRockHeight = Integer.parseInt(scanner.next());
		int numEndGoal = Integer.parseInt(scanner.next());
		
		int [][] MAP = new int[ROWS][COLS];
		int [][][] parentMap = new int[ROWS][COLS][2];
		List<String> endLocs = new ArrayList<>();
		
		storeEndLoc(numEndGoal, scanner, endLocs);
		buildMap(ROWS, COLS, MAP, scanner);
		System.out.println(algorithm.equals("BFS"));
		if(algorithm.equals("BFS")) {
			BFS(MAP, parentMap, endLocs, start, maxRockHeight, fileWriter);
		}
		else if(algorithm.equals("UCS")) {
			UCS(MAP, parentMap, endLocs, start, maxRockHeight,fileWriter);
		}
		else if(algorithm.equals("A*")) {
			AstartSearch(MAP, parentMap, endLocs, start, maxRockHeight, fileWriter);
		}
		
		int a = 10;
		System.out.println(a);
	
	}
	
	/*
	 * A star search
	 * */
	private static void AstartSearch(int[][] MAP, int[][][] parentMap, List<String> endLocs, int[] startPtr, int maxRockHeight, FileWriter fileWriter) throws IOException {
		PriorityQueue<Node> pq = new PriorityQueue<>((a, b) -> a.cost - b.cost);
		
		for(int goal = 0; goal < endLocs.size(); goal++) {
			
			Set<String> visited = new HashSet<>();
			String[] tmpEndPoint = endLocs.get(goal).split(",");
			int[] endPoint = new int[] {Integer.parseInt(tmpEndPoint[0]), Integer.parseInt(tmpEndPoint[1])};
			Node startNode = new Node(startPtr, 0 + findStraightLineDistance(startPtr, endPoint), new int[] {-1,-1} );
			pq.offer(startNode);
			
			// this statement may wrong
			while(!pq.isEmpty() ) {

				Node curNode = pq.poll();
				// only when we poll out we mark it visited
				String visitNode = curNode.loc[0] + "," + curNode.loc[1];
				if(visited.contains(visitNode)) {
					continue;
				}

				// put the parent loc into it
				parentMap[curNode.loc[0]][curNode.loc[1]] = curNode.parent;
				
				visited.add(visitNode);
				
				for(int[] dir : DIRECTION) {
					int x = curNode.loc[0] + dir[0];
					int y = curNode.loc[1] + dir[1];
					if(isValidCell(x, y) && isValidClimb(MAP, x, y, curNode.loc, maxRockHeight) && !visited.contains(x + "," + y)) {
						
						Node nextNode;
						// if there is x or y == 0, it means they should be  one direction in N E W S
						if(dir[0] == 0 || dir[1] == 0) {
							nextNode = new Node(new int[] {x, y}, 10 + heightDifferenceAndMud(MAP, x, y, curNode.loc) + curNode.cost, new int[] {curNode.loc[0], curNode.loc[1]});
						}
						else {
							nextNode = new Node(new int[] {x, y}, 14 + heightDifferenceAndMud(MAP, x, y, curNode.loc) + curNode.cost, new int[] {curNode.loc[0], curNode.loc[1]});
						}
						pq.offer(nextNode);
		
					}
				}
			}
			if(!visited.contains(endLocs.get(goal))) {
				fileWriter.write("FAIL");
			}
			else {
				writeOutPath(fileWriter, parentMap, endLocs.get(goal), startPtr);
			}
			if(goal != endLocs.size() - 1) {
				fileWriter.write("\n");
			}
			
		}
		fileWriter.close();
		
		
	}
	/*
	 * Only used for A* search.
	 * We calculate height difference + Mud level and return it.
	 * */
	private static int heightDifferenceAndMud(int[][] MAP, int x, int y, int[] curr) {
		int oldNodeHeight = 0;
		int newNodeHeight = 0;
		int newNodeMud = 0;
		if(MAP[curr[0]][curr[1]] < 0 ) {
			oldNodeHeight = Math.abs(MAP[curr[0]][curr[1]]);
		}
		
		
		if(MAP[x][y] < 0) {
			newNodeHeight = Math.abs(MAP[x][y]);
		}
		else {
			newNodeMud = MAP[x][y];
		}
		// return height difference
		return Math.abs(newNodeHeight - oldNodeHeight) + newNodeMud;
	}
	
	/**
	 * UCS Algorithm
	 * */
	private static void UCS(int[][] MAP, int[][][] parentMap, List<String> endLocs, int[] startPtr, int maxRockHeight, FileWriter fileWriter) throws IOException {
		PriorityQueue<Node> pq = new PriorityQueue<>((a, b) -> a.cost - b.cost);
		Set<String> visited = new HashSet<>();
		int cntRemainGoal = endLocs.size();
		pq.offer(new Node(startPtr, 0, new int[] {-1,-1}));
		// this statement may wrong
		while(!pq.isEmpty() || cntRemainGoal == 0) {
			if(cntRemainGoal == 0) {
				break;
			}

			Node curNode = pq.poll();
			// only when we poll out we mark it visited
			String visitNode = curNode.loc[0] + "," + curNode.loc[1];
			if(visited.contains(visitNode)) {
				continue;
			}
			if(endLocs.contains(visitNode)) {
				cntRemainGoal--;
			}
			// put the parent loc into it
			parentMap[curNode.loc[0]][curNode.loc[1]] = curNode.parent;
			
			visited.add(visitNode);
			
			for(int[] dir : DIRECTION) {
				int x = curNode.loc[0] + dir[0];
				int y = curNode.loc[1] + dir[1];
				if(isValidCell(x, y) && isValidClimb(MAP, x, y, curNode.loc, maxRockHeight) && !visited.contains(x + "," + y)) {
					
					Node nextNode;
					// if there is x or y == 0, it means they should be  one direction in N E W S
					if(dir[0] == 0 || dir[1] == 0) {
						nextNode = new Node(new int[] {x, y}, 10 + curNode.cost, new int[] {curNode.loc[0], curNode.loc[1]});
					}
					else {
						nextNode = new Node(new int[] {x, y}, 14 + curNode.cost, new int[] {curNode.loc[0], curNode.loc[1]});
					}
					pq.offer(nextNode);
	
				}
			}
		}
		for(int i = 0; i < endLocs.size(); i++) {
			if(!visited.contains(endLocs.get(i))) {
				fileWriter.write("FAIL");
			}
			else {
				writeOutPath(fileWriter, parentMap, endLocs.get(i), startPtr);
			}
			// Not sure what is the chnge line character \n \r
			if(i != endLocs.size() - 1) {
				fileWriter.write("\n");
			}
		}
		fileWriter.close();
		
	}
	
	
	/**
	 * BFS Algorithm
	 * */
	private static void BFS(int[][] MAP, int[][][] parentMap, List<String> endLocs, int[] startPtr, int maxRockHeight, FileWriter fileWriter) throws IOException {
		Queue<int[]> queue = new LinkedList<>();
		Set<String> visited = new HashSet<>();
		int cntRemainGoal = endLocs.size();
		queue.offer(startPtr);
		visited.add(startPtr[0] + "," + startPtr[1]);
		// this statement may wrong
		while(!queue.isEmpty() || cntRemainGoal == 0) {
			// all targets are already reached
			if(cntRemainGoal == 0) {
				break;
			}
			int size = queue.size();
			for(int i = 0; i < size; i++) {
				int[] currNode = queue.poll();
				
				for(int[] dir : DIRECTION) {
					int x = currNode[0] + dir[0];
					int y = currNode[1] + dir[1];
					// the cell not exceed the board and can climb and not visited
					if(isValidCell(x, y) && isValidClimb(MAP, x, y, currNode, maxRockHeight) && !visited.contains(x + "," + y)) {
						// x, y is new node, we put parent node info(tmp) into parentMap
						parentMap[x][y] = currNode;
						queue.offer(new int[]{x, y});
						
						String nextStr = x + "," + y;
						visited.add(nextStr);
						if(endLocs.contains(nextStr)) {
							cntRemainGoal--;
						}
						
					}
				}
				
			}
		}
		
		for(int i = 0; i < endLocs.size(); i++) {
			if(!visited.contains(endLocs.get(i))) {
				fileWriter.write("FAIL");
			}
			else {
				writeOutPath(fileWriter, parentMap, endLocs.get(i), startPtr);
			}
			// Not sure what is the chnge line character \n \r
			if(i != endLocs.size() - 1) {
				fileWriter.write("\n");
			}
		}
		fileWriter.close();
	}
	/**
	 * This function is used to write the Path into file by using stack
	 * */
	private static void writeOutPath(FileWriter fileWriter, int[][][] parentMap, String endLoc, int[] startPtr) throws IOException {
		// TODO Auto-generated method stub
		Stack<String> stack = new Stack<>();
		String[] loc = endLoc.split(",");
		int x = Integer.parseInt(loc[0]);
		int y = Integer.parseInt(loc[1]);
		// need to change because the question is suck
		stack.add(y + "," + x);
		while(x != startPtr[0] || y != startPtr[1]) {
			int[] parent = parentMap[x][y];
			x = parent[0];
			y = parent[1];
			// need to change because the question is suck
			stack.add(y + "," + x);
		}
		// write on the file
		int size = stack.size();
		for(int i = 0; i < size; i++) {
			String tmpString = stack.pop();
			System.out.print(tmpString);
			fileWriter.write(tmpString);
			// if it is last point we break out.  we do not want add a extra whitespace
			if(i == size - 1) {
				break;
			}
			fileWriter.write(" ");
		}
		
	}
	
	/**
	 * This function is to check whether the height difference is bigger than maxRockHeight or not.
	 * */
	private static boolean isValidClimb(int[][] MAP, int x, int y, int[] curr, int maxRockHeight) {
		int oldNode = 0;
		int newNode = 0;
		if(MAP[curr[0]][curr[1]] < 0 ) {
			oldNode = Math.abs(MAP[curr[0]][curr[1]]);
		}
		
		if(MAP[x][y] < 0) {
			newNode = Math.abs(MAP[x][y]);
		}
		// we can climb this cell
		if(Math.abs(oldNode - newNode) <= maxRockHeight) {
			return true;
		}
		return false;
	}
	/**
	 * This function is used to put those end locations which read form file and format as "x,y" into ArrayList 
	 * */
	private static void storeEndLoc(int numEndGoal, Scanner scanner, List<String> endLocs) {
		for(int i = 0; i < numEndGoal; i++) {
			String tmp = "";
			// need to change because question is suck
			String x = scanner.next();
			String y = scanner.next();
			tmp = y + "," + x;
			endLocs.add(tmp);
		}
	}
	/**
	 * This function build up the Map
	 * */
	private static void buildMap(int rows, int cols, int [][] MAP, Scanner scanner) {
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < cols; j++) {
				MAP[i][j] = Integer.parseInt(scanner.next());
			}
		}
	}
	/**
	 * This function check the cell is not exceed the border
	 * */
	private static boolean isValidCell(int x, int y) {
		if(x >= 0 && y >= 0 && x < ROWS && y < COLS) {
			return true;
		}
		return false;
	}
	
	private static int findStraightLineDistance(int[] from, int[] goal) {
		int xsquare =(from[0] - goal[0]) * (from[0] - goal[0]);
		int ysquare = (from[1] - goal[1]) * (from[1] - goal[1]);
		return (int) Math.sqrt( xsquare + ysquare );
		
	}
	/**
	 * Node class
	 * */
	public static class Node {
		int[] loc;
		int[] parent;
		int cost;
		public Node(int[] loc, int cost, int[] parent) {
			this.loc = loc;
			this.cost = cost;
			this.parent = parent;
		}
	}
	
}
