package extensiveformgames.generators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import extensiveformgames.games.Game.Action;
import model.Loc;

/**
 * Enumerates a game tree given an initial start state. Writes the resultant
 * tree to a file in the ZeroSumFormat specified in
 * https://github.com/ChrKroer/ExtensiveFormGames
 * 
 * @author Alex Henderson
 *
 */
public class GameTreeEnumerator {

	private String fileName;
	private InfoSet[][] infoSets;
	private int numChanceNodes;
	private int numPlayerNodes;
	private int numTerminalNodes;
	private int workingID;
	private int chanceCounter;
	private int mainCounter;

	public GameTreeEnumerator(String gameFileName) {
		fileName = gameFileName;
		numChanceNodes = 0;
		numPlayerNodes = 0;
		numTerminalNodes = 0;
		workingID = 0;
		chanceCounter = 0;
		mainCounter = 0;
		infoSets = new InfoSet[2][];
	}

	/**
	 * Generates game tree from given initial state data. Writes tree to file
	 * using ZeroSumFormat
	 * 
	 * @param r1s
	 *            - robot 1 start location
	 * @param r2s
	 *            - robot 2 start location
	 * @param gridSize
	 *            - length/height of game grid
	 * @param targets
	 *            - list of target locations
	 * @throws IOException
	 */
	public void generateFile(Loc r1s, Loc r2s, int gridSize, ArrayList<Loc> targets) throws IOException {
		FileWriter ofStream = new FileWriter(fileName);
		BufferedWriter writer = new BufferedWriter(ofStream);
		String[] treeArray = new String[(int) Math.pow(gridSize, 6)];
		treeArray = createNode(treeArray, "/", 1, r1s, r2s, targets, gridSize);
		mainCounter = numChanceNodes;
		treeArray = idNodes(treeArray.clone(), 0);
		String tree = new String();
		for (int i = 0; i < (numChanceNodes + numPlayerNodes + numTerminalNodes); i++) {
			tree = tree + treeArray[i] + "\n";
		}

		String output = "'Test: Grid size-" + gridSize + ", robots-" + r1s.toString() + ", " + r2s.toString() + "' "
				+ numChanceNodes + " " + numPlayerNodes + " " + numTerminalNodes + " 0 "
				+ (numChanceNodes + numPlayerNodes + numTerminalNodes - 1) + " 0 " + infoSets[0].length + " "
				+ infoSets[1].length + "\n" + tree;
		writer.write(output, 0, output.length());
		writer.close();
	}

	// Recursive function that populates game tree.
	private String[] createNode(String[] tree, String nodeName, int player, Loc r1, Loc r2, ArrayList<Loc> targets,
			int gridSize) {
		if (workingID >= tree.length)
			tree = increaseBuffer(tree.clone());
		// Check if current player on a target****************
		if (targets.contains(r1))
			targets.get(targets.indexOf(r1)).setTargHit(1);
		if (targets.contains(r2))
			targets.get(targets.indexOf(r2)).setTargHit(2);

		// Check end condition********************************
		// Check if player 1 to ensure each history includes both player moves
		// If end condition met or reached branch length limit, create terminal node
		// NOTE: Second half of condition specifies max allowed branch limit; if
		// set too large, program generates max heap space exception
		if (player == 1 && (checkDone(targets) || nodeName.length() >= 9)) { 
			// Increment numTerminalNodes
			numTerminalNodes++;
			// Calculate payoff & return
			tree[workingID] = "T '" + nodeName + "' " + payoff(targets);
			return tree;
		}
		// Identify possible actions***************************
		String[] actions;
		if (player == 1)
			actions = idActionsAtNode(gridSize, r1, r2);
		else
			actions = idActionsAtNode(gridSize, r2, r1);

		// Check for matching info set************************
		int index = matchInfoSet(actions, player - 1);
		if (index >= 0)
			infoSets[player - 1][index].increaseCount();
		else {
			// Create new info set
			addInfoSet(actions, player - 1);
		}

		// Create player node*********************************
		numPlayerNodes++;
		String pNode = "P '" + nodeName + "' " + (player - 1) + " 0 " + actions.length;
		int pID = workingID;
		// For each possible action***************************************
		for (int a = 0; a < actions.length; a++) {
			workingID++;
			int childId = workingID;
			pNode = pNode + " '" + actions[a] + "' " + childId;
			String currentAct = actions[a];
			String oppAct = nodeName.charAt(nodeName.length() - 1) + "";

			// If current player==2 and r2 + action == r1*****************
			if (player == 2 && actionToLoc(r2, currentAct).equals(actionToLoc(r1, oppAct))) {
				if (childId >= tree.length)
					tree = increaseBuffer(tree.clone());
				numChanceNodes++;
				// Create chance node*************************************
				workingID++;
				
				tree[childId] = "C '" + nodeName + currentAct + "' 2 '1' " + workingID + " 1 ";
				tree = createNode(tree.clone(), nodeName + "1", 1, actionToLoc(r1, oppAct), r2, cloneLocList(targets),
						gridSize);
				tree = pruneBranch(tree.clone(), workingID, 0);
				workingID++;
				tree[childId] = tree[childId] + "'2' " + workingID + " 1";
				tree = createNode(tree.clone(), nodeName + "2", 1, r1, actionToLoc(r2, currentAct),
						cloneLocList(targets), gridSize);
				tree = pruneBranch(tree.clone(), workingID, 1);
			} else {
				// Update positions***************************************
				Loc r1New = r1;
				Loc r2New = r2;
				if (player == 2) {
					r1New = actionToLoc(r1, oppAct);
					r2New = actionToLoc(r2, currentAct);
				}
				// call createNode****************************************
				tree = createNode(tree.clone(), nodeName + currentAct, ((player % 2) + 1), r1New, r2New,
						cloneLocList(targets), gridSize);
			}
			 // Branch Pruning
			tree = pruneBranch(tree.clone(), childId, (player % 2));
		}
		// Return new Node
		tree[pID] = pNode;
		return tree;
	}

	private String[] increaseBuffer(String[] tree) {
		String[] bigTree = new String[(int) (tree.length * 1.5)];
		for (int i = 0; i < tree.length; i++) {
			bigTree[i] = tree[i];
		}
		return bigTree.clone();
	}

	// Determines whether all targets have been hit on this
	// branch
	private boolean checkDone(ArrayList<Loc> targets) {
		int hit = 0;
		for (Loc t : targets) {
			if (t.targHitBy() > 0)
				hit++;
		}
		return hit == targets.size();
	}

	// Calculates payoff for game
	private int payoff(ArrayList<Loc> targets) {
		int payoff = 0;
		for (Loc t : targets) {
			if (t.targHitBy() == 1)
				payoff++;
			else if (t.targHitBy() == 2)
				payoff--;
		}
		return payoff;
	}

	// Identifies if an information set for the set of actions
	// exists
	private int matchInfoSet(String[] actions, int player) {
		if (infoSets[player] == null)
			return -1;

		for (int i = 0; i < infoSets[player].length; i++) {
			if (equalActionSet(actions, infoSets[player][i].getActions()))
				return i;
		}
		return -1;
	}

	// Adds new information set to master list
	private void addInfoSet(String[] actions, int player) {
		if (infoSets[player] == null) {
			infoSets[player] = new InfoSet[1];
			infoSets[player][0] = new InfoSet(actions);
			infoSets[player][0].increaseCount();
			return;
		}
		InfoSet[] infoSetList = new InfoSet[infoSets[player].length + 1];
		for (int i = 0; i < infoSets[player].length; i++) {
			infoSetList[i] = infoSets[player][i];
		}
		infoSetList[infoSetList.length - 1] = new InfoSet(actions);
		infoSetList[infoSetList.length - 1].increaseCount();
		infoSets[player] = infoSetList.clone();
	}

	private String[] pruneBranch(String[] tree, int id, int player){
		String[] tokens = tree[id].split(" ");
		int actLen = 0;
		int actIndex = 0;
		int actDelta = 0;
		if(tokens[0].equals("C")){
			actLen = new Integer(tokens[2]);
			actIndex = 4;
			actDelta = 3;
		}else if(tokens[0].equals("P")){
			actLen = new Integer(tokens[4]);
			actIndex = 6;
			actDelta = 2;
		}else
			return tree;
		String[] act = new String[actLen];
		int scoreSum = 0;
		
		for (int n = 0, i = actIndex; n < actLen; n++, i += actDelta) {
			act[n] = tokens[i-1].substring(1, 2);
			int childId = new Integer(tokens[i]);
			String[] childTok = tree[childId].split(" ");
			if(childTok[0].equals("T"))
				scoreSum += Math.abs(new Integer(childTok[2]));
			else 
				return tree;
		}// end loop
		
		if(scoreSum==0){
			numTerminalNodes -= (actLen-1);
			if(tokens[0].equals("P")){
				numPlayerNodes--;
				int index = matchInfoSet(act, player);
				if (index >= 0)
					infoSets[player][index].decreaseCount();
				if(infoSets[player][index].getCount()==0)
					pruneInfoSet(player, index);
			}else
				numChanceNodes--;
			tree[id] = "T " + tokens[1] + " 0";
			workingID = id;
		}
		return tree;
	}
	
	private void pruneInfoSet(int player, int index){
		InfoSet[] infoSetList = new InfoSet[infoSets[player].length - 1];
		for (int i = 0, j = 0; i < index; i++, j++) {
			if(i==index){
				j--;
				continue;
			}
			infoSetList[j] = infoSets[player][i];
		}
		infoSets[player] = infoSetList.clone();
	}
	
	// Deep copy of list of Loc objects
	private ArrayList<Loc> cloneLocList(ArrayList<Loc> l1) {
		ArrayList<Loc> l2 = new ArrayList<Loc>();
		for (Loc t : l1) {
			l2.add(new Loc(t));
		}
		return l2;
	}

	private String[] idNodes(String[] tree, int id) {
		char type = tree[id].charAt(0);
		if (type == 'P') {
			String[] tokens = tree[id].split(" ");
			tokens[0] = String.valueOf(mainCounter);
			mainCounter++;
			int actLen = new Integer(tokens[4]);
			String[] act = new String[actLen];
			for (int n = 0, i = 6; n < actLen; n++, i += 2) {
				int childId = new Integer(tokens[i]);
				act[n] = tokens[i-1].substring(1, 2);
				tree = idNodes(tree.clone(), childId);
				tokens[i] = tree[childId].split(" ")[0];
			}
			int index = matchInfoSet(act, new Integer(tokens[2]));
			tokens[3] = String.valueOf(index);
			tree[id] = tokens[0];
			for (int s = 1; s < tokens.length; s++) {
				tree[id] = tree[id] + " " + tokens[s];
			}
		} else if (type == 'T') {
			tree[id] = mainCounter + tree[id].substring(1);
			mainCounter++;
		} else if (type == 'C') {
			String[] tokens = tree[id].split(" ");
			tokens[0] = String.valueOf(chanceCounter);
			chanceCounter++;

			int leftId = new Integer(tokens[4]);
			tree = idNodes(tree.clone(), leftId);
			tokens[4] = tree[leftId].split(" ")[0];

			int rightId = new Integer(tokens[7]);
			tree = idNodes(tree.clone(), rightId);
			tokens[7] = tree[rightId].split(" ")[0];

			tree[id] = tokens[0];
			for (int s = 1; s < tokens.length; s++) {
				tree[id] = tree[id] + " " + tokens[s];
			}
		}

		return tree;
	}
	
	/**
	 * Calculates and returns all possible moves for current game node.
	 * 
	 * @param gridSize
	 *            - size of grid
	 * @param currentPos
	 *            - current position of player for which to calculate move set
	 * @param oppPosition
	 *            - current position of opposite player
	 * @return Array of String action names
	 */
	public static String[] idActionsAtNode(int gridSize, Loc currentPos, Loc oppPosition) {
		ArrayList<String> actions = new ArrayList<String>();
		// Check up
		if (currentPos.getY() > 0 && !oppPosition.equals(new Loc(currentPos.getX(), currentPos.getY() - 1)))
			actions.add("U");
		// Check down
		if (currentPos.getY() < gridSize - 1 && !oppPosition.equals(new Loc(currentPos.getX(), currentPos.getY() + 1)))
			actions.add("D");
		// Check left
		if (currentPos.getX() > 0 && !oppPosition.equals(new Loc(currentPos.getX() - 1, currentPos.getY())))
			actions.add("L");
		// Check right
		if (currentPos.getX() < gridSize - 1 && !oppPosition.equals(new Loc(currentPos.getX() + 1, currentPos.getY())))
			actions.add("R");

		return actions.toArray(new String[actions.size()]);
	}

	/**
	 * Returns the location resulting from performing the given action from the
	 * given location.
	 * 
	 * @param pos
	 *            - current position of player
	 * @param a
	 *            - name of action
	 * @return new player location after performing action
	 */
	public static Loc actionToLoc(Loc pos, String a) {
		switch (a) {
		case "U":
			return new Loc(pos.getX(), pos.getY() - 1);
		case "D":
			return new Loc(pos.getX(), pos.getY() + 1);
		case "L":
			return new Loc(pos.getX() - 1, pos.getY());
		case "R":
			return new Loc(pos.getX() + 1, pos.getY());
		default:
			return pos;
		}
	}

	/**
	 * Returns true if the two actions sets contain identical actions.
	 * 
	 * @param a1
	 *            - First list of actions as Strings
	 * @param a2
	 *            - Second list of actions as
	 *            {@link extensiveformgames.games.Game.Action}
	 * @return true if both lists contain identical actions, false otherwise
	 */
	public static boolean equalActionSet(String[] a1, Action[] a2) {
		String[] a2String = new String[a2.length];
		for (int i = 0; i < a2.length; i++) {
			a2String[i] = a2[i].getName();
		}
		return equalActionSet(a1, a2String);
	}

	/**
	 * Returns true if the two actions sets contain identical actions.
	 * 
	 * @param a1
	 *            - First list of actions
	 * @param a2
	 *            - Second list of actions
	 * @return true if both lists contain identical actions, false otherwise
	 */
	public static boolean equalActionSet(String[] a1, String[] a2) {
		List<String> l1 = new ArrayList<String>(Arrays.asList(a1));
		List<String> l2 = new ArrayList<String>(Arrays.asList(a2));

		Collections.sort(l1);
		Collections.sort(l2);
		return l1.equals(l2);
	}

}
