import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
//always be given a valid input.txt file 
//When the wagon moves vertically or horizontally (so north, south, east, or west), the movement cost is will be either 1 for BFS or 10 for UCS and A. 
//When the wagon moves diagonally (so northeast, northwest, southeast, or southwest), the movement cost is either 1 for BFS or 14 for UCS and A. 
// Ｘ往東正 Ｙ往南正
public class SearchAlgorithm {
	private final static int DIRECTION[][] = {{-1,-1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
	private static int COLS;
	private static int ROWS;
	public static void main(String[] args) throws IOException {
		
//		File outFile = new File("outputTotal.txt");
//		FileWriter fileWriter = new FileWriter(outFile);
//		for(int i = 0; i < 30; i++) {
		//   /Users/laicunhao/eclipse-workspace/tets/input/input"+i+".txt"
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
		List<String> endLocs = new ArrayList<>();
		
		storeEndLoc(numEndGoal, scanner, endLocs);
		buildMap(ROWS, COLS, MAP, scanner);
		
		if(algorithm.equals("BFS")) {
			BFS(MAP, endLocs, start, maxRockHeight, fileWriter);
		}
		else if(algorithm.equals("UCS")) {
			UCS(MAP, endLocs, start, maxRockHeight,fileWriter);
		}
		else if(algorithm.equals("A*")) {
			AstartSearch(MAP, endLocs, start, maxRockHeight, fileWriter);
		}
//		fileWriter.write("\n");
//		}
//		fileWriter.close();
//		int a = 10;
//		System.out.println(a);
	
	}
	
	/*
	 * A star search
	 * */
	private static void AstartSearch(int[][] MAP, List<String> endLocs, int[] startPtr, int maxRockHeight, FileWriter fileWriter) throws IOException {
		
		
		Map<String, Node> endGoalEntry = new HashMap<>();
		
		for(int goal = 0; goal < endLocs.size(); goal++) {
			
			PriorityQueue<Node> pq = new PriorityQueue<>((a, b) -> a.cost - b.cost);
			Map<String, Node> inQueue = new HashMap<>();
			
			Set<String> visited = new HashSet<>();
			
			String[] tmpEndPoint = endLocs.get(goal).split(",");
			int[] endPoint = new int[] {Integer.parseInt(tmpEndPoint[0]), Integer.parseInt(tmpEndPoint[1])};
			
			int NodeStraightLine = findStraightLineDistance(startPtr, endPoint);
			
			// important!! the cost function is wrong
			Node startNode = new Node(startPtr, 0, null, NodeStraightLine);
			
			pq.offer(startNode);
			inQueue.put(startPtr[0] + "," + startPtr[1], null);
			// this statement may wrong
			while(!pq.isEmpty() && !visited.contains(endLocs.get(goal)) ) {

				Node curNode = pq.poll();
				inQueue.remove(curNode.loc[0] + "," + curNode.loc[1]);
				
				// only when we poll out we mark it visited
				String visitNode = curNode.loc[0] + "," + curNode.loc[1];
				if(visited.contains(visitNode)) {
					continue;
				}
				if(endLocs.contains(visitNode)) {
					endGoalEntry.put(visitNode, curNode);
				}
				// put the parent loc into it				
				visited.add(visitNode);
				
				for(int[] dir : DIRECTION) {
					int x = curNode.loc[0] + dir[0];
					int y = curNode.loc[1] + dir[1];
					if(isValidCell(x, y) && isValidClimb(MAP, x, y, curNode.loc, maxRockHeight) && !visited.contains(x + "," + y)) {
						
						Node nextNode;
						int[] nextNodeLoc = new int[] {x, y};
						int nextNodeStraightLine = findStraightLineDistance(nextNodeLoc, endPoint);
						// if there is x or y == 0, it means they should be  one direction in N E W S
						// important!! the cost function is wrong
						if(dir[0] == 0 || dir[1] == 0) {
							nextNode = new Node(nextNodeLoc, 10 + heightDifferenceAndMud(MAP, x, y, curNode.loc) + curNode.cost - curNode.straightLineDist + nextNodeStraightLine, curNode, nextNodeStraightLine );
						}
						else {
							nextNode = new Node(nextNodeLoc, 14 + heightDifferenceAndMud(MAP, x, y, curNode.loc) + curNode.cost - curNode.straightLineDist + nextNodeStraightLine, curNode, nextNodeStraightLine );
						}
						
						String checkNode = nextNode.loc[0] + "," + nextNode.loc[1];
						if( inQueue.containsKey(checkNode) ) {
							if(inQueue.get(checkNode).cost > nextNode.cost) {
								pq.remove(inQueue.get(checkNode));
							}
							else {
								continue;
							}						
						}	
						pq.offer(nextNode);
						inQueue.put(checkNode, nextNode);	
						
					}
				}
			}
			// check whether we reach the end goal
			
			writeOutpathOrFail(visited, endLocs, fileWriter, endGoalEntry, goal, startPtr);
			
		}
		fileWriter.close();
		
		
	}
	
	/**
	 * UCS Algorithm
	 * */
	private static void UCS(int[][] MAP, List<String> endLocs, int[] startPtr, int maxRockHeight, FileWriter fileWriter) throws IOException {
		PriorityQueue<Node> pq = new PriorityQueue<>((a, b) -> a.cost - b.cost);
		Map<String, Node> inQueue = new HashMap<>();
		
		Map<String, Node> endGoalEntry = new HashMap<>();
		
		Set<String> visited = new HashSet<>();
		int cntRemainGoal = endLocs.size();
		
		pq.offer(new Node(startPtr, 0, null));
		inQueue.put(startPtr[0] + "," + startPtr[1], null);
		// this statement may wrong // !pq.isEmpty()
		while( !pq.isEmpty() && cntRemainGoal != 0) {
			if(cntRemainGoal == 0) {
				break;
			}

			Node curNode = pq.poll();
			inQueue.remove(curNode.loc[0] + "," + curNode.loc[1]);
			// only when we poll out we mark it visited
			String visitNode = curNode.loc[0] + "," + curNode.loc[1];
			if(visited.contains(visitNode)) {
				continue;
			}
			if(endLocs.contains(visitNode)) {
				endGoalEntry.put(visitNode, curNode);
				cntRemainGoal--;
			}
			// put the parent loc into it
//			parentMap[curNode.loc[0]][curNode.loc[1]] = curNode.parent;
			
			visited.add(visitNode);
			
			for(int[] dir : DIRECTION) {
				int x = curNode.loc[0] + dir[0];
				int y = curNode.loc[1] + dir[1];
				
				
				if(isValidCell(x, y) && isValidClimb(MAP, x, y, curNode.loc, maxRockHeight) && !visited.contains(x + "," + y) ) {
					
					Node nextNode;
					// if there is x or y == 0, it means they should be  one direction in N E W S
					if(dir[0] == 0 || dir[1] == 0) {
						nextNode = new Node(new int[] {x, y}, 10 + curNode.cost, curNode);
					}
					else {
						nextNode = new Node(new int[] {x, y}, 14 + curNode.cost, curNode);
					}
					
					String checkNode = nextNode.loc[0] + "," + nextNode.loc[1];
					if( inQueue.containsKey(checkNode) ) {
						if(inQueue.get(checkNode).cost > nextNode.cost) {
							pq.remove(inQueue.get(checkNode));
						}
						else {
							continue;
						}						
					}
					
					pq.offer(nextNode);
					inQueue.put(checkNode, nextNode);
				}
			}
		}
		
		for(int i = 0; i < endLocs.size(); i++) {
			writeOutpathOrFail(visited, endLocs, fileWriter, endGoalEntry, i,  startPtr);
		}
		fileWriter.close();
		
	}
	
	
	/**
	 * BFS Algorithm not finish the path Map bug
	 * */
	private static void BFS(int[][] MAP, List<String> endLocs, int[] startPtr, int maxRockHeight, FileWriter fileWriter) throws IOException {
		Queue<Node> queue = new LinkedList<>();
		Set<String> visited = new HashSet<>();
		
		Map<String, Node> endGoalEntry = new HashMap<>();
		
		int cntRemainGoal = endLocs.size();
		Node start = new Node(startPtr, 0, null);
		queue.offer(start);
		visited.add(startPtr[0] + "," + startPtr[1]);
		// this statement may wrong
		while(!queue.isEmpty() && cntRemainGoal != 0) {
			// all targets are already reached
			if(cntRemainGoal == 0) {
				break;
			}
			int size = queue.size();
			for(int i = 0; i < size; i++) {
				Node currNode = queue.poll();
				
				for(int[] dir : DIRECTION) {
					int x = currNode.loc[0] + dir[0];
					int y = currNode.loc[1] + dir[1];
					// the cell not exceed the board and can climb and not visited
					if(isValidCell(x, y) && isValidClimb(MAP, x, y, currNode.loc, maxRockHeight) && !visited.contains(x + "," + y)) {
						// x, y is new node, we put parent node info(tmp) into parentMap
//						parentMap[x][y] = currNode;
						
						Node nextNode = new Node(new int[]{x, y}, 0, currNode);
						queue.offer(nextNode);
						
						String nextStr = x + "," + y;
						visited.add(nextStr);
						if(endLocs.contains(nextStr)) {
							endGoalEntry.put(nextStr, nextNode);
							cntRemainGoal--;
						}
						
					}
				}
				
			}
		}
		
		for(int i = 0; i < endLocs.size(); i++) {
			writeOutpathOrFail(visited, endLocs, fileWriter, endGoalEntry, i,  startPtr);
		}
		fileWriter.close();
	}
	
	private static void writeOutpathOrFail(Set<String> visited, List<String> endLocs, FileWriter fileWriter, Map<String, Node> endGoalEntry, int index, int[] startPtr) throws IOException {
		if(!visited.contains(endLocs.get(index))) {
			fileWriter.write("FAIL");
		}
		else {
			writeOutPath(fileWriter, endGoalEntry.get(endLocs.get(index)), startPtr);
		}
		// Not sure what is the chnge line character \n \r
		if(index != endLocs.size() - 1) {
			fileWriter.write("\n");
		}
	}
	/**
	 * This function is used to write the Path into file by using stack
	 * */
	private static void writeOutPath(FileWriter fileWriter, Node endLoc, int[] startPtr) throws IOException {
		// TODO Auto-generated method stub
		Stack<String> stack = new Stack<>();
		int x = endLoc.loc[0];
		int y = endLoc.loc[1];
		// need to change because the question is suck
		stack.add(y + "," + x);
		
		for(Node node = endLoc;  node.parent != null; node = node.parent) {
			x = node.parent.loc[0];
			y = node.parent.loc[1];
			stack.add(y + "," + x);
		}
		// write on the file
		int size = stack.size();
		for(int i = 0; i < size; i++) {
			String tmpString = stack.pop();
			fileWriter.write(tmpString);
			// if it is last point we break out.  we do not want add a extra whitespace
			if(i == size - 1) {
				break;
			}
			fileWriter.write(" ");
		}
		
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
	 * This function is to check whether the height difference is bigger than maxRockHeight or not.
	 * */
	private static boolean isValidClimb(int[][] MAP, int x, int y, int[] curr, int maxRockHeight) {
		int oldNode = 0;
		int newNode = 0;
		if(MAP[curr[0]][curr[1]] < 0 ) {
			oldNode = MAP[curr[0]][curr[1]];
		}
		
		if(MAP[x][y] < 0) {
			newNode = MAP[x][y];
		}
		// we can climb this cell
		if(Math.abs(oldNode - newNode) <= maxRockHeight) {
			return true;
		}
		return false;
	}
	/**
	 * This function is used to put these end locations which read form file and format as "x,y" into ArrayList 
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
	 * This function build up the Map from the input text
	 * */
	private static void buildMap(int rows, int cols, int [][] MAP, Scanner scanner) {
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < cols; j++) {
				MAP[i][j] = Integer.parseInt(scanner.next());
			}
		}
	}
	/**
	 * This function check the cell whether it is exceed the border or not.
	 * return boolean
	 * */
	private static boolean isValidCell(int x, int y) {
		if(x >= 0 && y >= 0 && x < ROWS && y < COLS) {
			return true;
		}
		return false;
	}
	/**
	 * Only for  A* Algorithm's admissible heuristic function
	 * This function calculate the straight line distance form one cell to another cell.
	 * return integer
	 * */
	private static int findStraightLineDistance(int[] from, int[] goal) {
		int xsquare =(from[0] - goal[0]) * (from[0] - goal[0]) * 100;
		int ysquare = (from[1] - goal[1]) * (from[1] - goal[1]) * 100;
		return (int) Math.sqrt( xsquare + ysquare );
		
	}
	/**
	 * Node class
	 * */
	public static class Node {
		int[] loc;
		Node parent;
		int cost;
		// for A star search properties
		int straightLineDist;
		
		public Node(int[] loc, int cost, Node parent) {
			this.loc = loc;
			this.cost = cost;
			this.parent = parent;
		}
		public Node(int[] loc, int cost, Node parent, int straightLineDist) {
			this.loc = loc;
			this.cost = cost;
			this.parent = parent;
			this.straightLineDist = straightLineDist;
		}
	}
	
}
