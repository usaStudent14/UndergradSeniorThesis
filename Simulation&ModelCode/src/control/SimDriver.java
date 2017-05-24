package control;

import extensiveformgames.solvers.CounterFactualRegretSolver;
import extensiveformgames.generators.GameTreeEnumerator;
import reenforcementlearning.model.MATrainer;
import reenforcementlearning.model.MyGridGame;

import model.Loc;
import model.Robot;

import extensiveformgames.games.Game;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.stochasticgames.agents.interfacing.singleagent.LearningAgentToSGAgentInterface;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import burlap.mdp.stochasticgames.model.JointRewardFunction;
import burlap.mdp.stochasticgames.oo.OOSGDomain;
import burlap.statehashing.simple.SimpleHashableStateFactory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import ui.*;

/**
 * Driver class for simulations. Execute this class's {@link main} method to open a GUI for simulation initialization.
 * <p>
 * To initialize a simulation from another class, call the {@link remoteNewSim} method.
 * @author Alex Henderson
 *
 */

public class SimDriver {

	static Robot r1;
	static Robot r2;
	static SimWindow mainDisplay;
	static int simGridSize;
	static int gameVariant;
	static boolean simRunning = false;
	static ArrayList<Loc> targets;
	private static CounterFactualRegretSolver gameSolver;
	private static OOSGDomain domain;
	private static ArrayList<LearningAgentToSGAgentInterface> learningAgents = new ArrayList<LearningAgentToSGAgentInterface>();
	
	static String gameFolder = System.getProperty("user.dir") + "/enumGameFiles/";
	
	public static void main(String[] args){
		mainDisplay = initUI();
	}

	
	public static void remoteNewSim(int gridSize, Loc r1s, Loc r2s, boolean r1Mode, 
			boolean r2Mode, ArrayList<Loc> targs){
		// Null several static variables
				targets = null;
				gameSolver = null;
				domain = null;
				learningAgents = null;
				// Initialize game variant
				gameVariant = 0;
				// Initialize grid
				simGridSize = gridSize;
				// Initialize target list
				targets = targs;
				// Initialize robots
				r1 = initRobot(r1s, 1);
				r2 = initRobot(r2s, 2); 
				// Set modes
				r1.switchMode(r1Mode);
				r2.switchMode(r2Mode);
				// If one or both robots are set to CFR solver
				if(r1Mode || r2Mode){
//					System.out.println("Initializing CFR");
//					long start = System.currentTimeMillis();
					gameSolver = initGameSolver(r1Mode, r2Mode, r1s, r2s);
//					long time = (System.currentTimeMillis() - start)/1000;
//					System.out.println("CFR Initialized: " + time + " seconds");
				}
				// If one or both robots are set to RL solver
				if(!r1Mode || !r2Mode){
//					System.out.println("Training learning agents");
//					long start = System.currentTimeMillis();
					learningAgents = initLearningAgents(!r1Mode, !r2Mode, r1s, r2s);
//					long time = (System.currentTimeMillis() - start)/1000;
//					System.out.println("Agents trained: " + time + " seconds");
				}
					
				simRunning = true;
	}
	
	/**
	 * Initializes simulation remotely. Does not generate GUI.
	 * @param var - game rule variant
	 * @param gridSize - length/height of game grid
	 * @param r1s - location of robot 1
	 * @param r2s - location of robot 2
	 * @param r1Mode - robot 1 mode
	 * @param r2Mode - robot 2 mode
	 */
	public static void remoteNewSim(int var, int gridSize, Loc r1s, Loc r2s, 
			boolean r1Mode, boolean r2Mode){
		// Null several static variables
		targets = null;
		gameSolver = null;
		domain = null;
		learningAgents = null;
		// Initialize game variant
		gameVariant = var;
		// Initialize grid
		simGridSize = gridSize;
		// Initialize target list
		targets = initTargs(simGridSize, r1s, r2s);
		// Initialize robots
		r1 = initRobot(r1s, 1);
		r2 = initRobot(r2s, 2); 
		// Set modes
		r1.switchMode(r1Mode);
		r2.switchMode(r2Mode);
		// If one or both robots are set to CFR solver
		if(r1Mode || r2Mode){
//			System.out.println("Initializing CFR");
//			long start = System.currentTimeMillis();
			gameSolver = initGameSolver(r1Mode, r2Mode, r1s, r2s);
//			long time = (System.currentTimeMillis() - start)/1000;
//			System.out.println("CFR Initialized: " + time + " seconds");
		}
		// If one or both robots are set to RL solver
		if(!r1Mode || !r2Mode){
//			System.out.println("Training learning agents");
//			long start = System.currentTimeMillis();
			learningAgents = initLearningAgents(!r1Mode, !r2Mode, r1s, r2s);
//			long time = (System.currentTimeMillis() - start)/1000;
//			System.out.println("Agents trained: " + time + " seconds");
		}
			
		simRunning = true;
	}
	
	// Initializes new simulation with input from GUI
	private static void newSim(int var, int gridSize, Loc r1s, Loc r2s,
			boolean r1Mode, boolean r2Mode){
		// Null several static variables
		targets = null;
		gameSolver = null;
		domain = null;
		learningAgents = null;
		// Initialize game variant
		gameVariant = var;
		// Initialize grid
		simGridSize = gridSize;
		// Initialize target list
		targets = initTargs(simGridSize, r1s, r2s);
		// Initialize robots
		r1 = initRobot(r1s, 1);
		r2 = initRobot(r2s, 2); 
		// Set modes
		r1.switchMode(r1Mode);
		r2.switchMode(r2Mode);
		// If one or both robots are set to CFR solver
		if(r1Mode || r2Mode)
			gameSolver = initGameSolver(r1Mode, r2Mode, r1s, r2s);
		if(!r1Mode || !r2Mode)
			learningAgents = initLearningAgents(!r1Mode, !r2Mode, r1s, r2s);
			
		// Set up GUI parameters
		mainDisplay.getGridView().setGridSize(simGridSize);
		mainDisplay.getGridView().setTargetList(targets);
		// Initial GUI update
		update();
		
		simRunning = true;
	}
	
	/**
	 * Advances simulation forward by one discrete timestep. Agents each choose and execute an 
	 * action based on their selected solution mode. Updates the game state and GUI display (if
	 * applicable)
	 */
	public static void run() {
		Loc r1Move;
		Loc r2Move;
		// Find next step based on robot mode
		if(r1.getMode())
			r1Move = gameSolverNextStep(1, r1.getCurrentPos(), r2.getCurrentPos());
		else
			r1Move = learningAgentNextStep(0, r1.getCurrentPos(), r2.getCurrentPos());
		if(r2.getMode())
			r2Move = gameSolverNextStep(2, r2.getCurrentPos(), r1.getCurrentPos());
		else
			r2Move = learningAgentNextStep(1, r1.getCurrentPos(), r2.getCurrentPos());
		
		
		// Inform any learning agents of all moves
		if(!r1.getMode() || ! r2.getMode()){
			JointAction ja = new JointAction();
			ja.setAction(1, null);
			State s = MyGridGame.getCustomGameState(simGridSize, r1Move, r2Move, targets);
			JointRewardFunction rf = new MyGridGame.MyGJointRewardFunction(domain, -1, 100, false);
			double[] jointReward = rf.reward(null, ja, s);
	
			for(SGAgent a : learningAgents){
				a.observeOutcome(null, null, jointReward, s, false);// TODO: keep an eye on always sending false for terminal state
			}
		}
		
		boolean r1Done;
		boolean r2Done;
		// Handle contention
		if(r1Move.equals(r2Move)){
			// Currently implemented a random chooser.
			Random r = new Random();
			r.setSeed(System.currentTimeMillis());
			int choice = r.nextInt(2);
			r1Done = r1.step(r1Move, choice==0, gameVariant);// TODO: Implement both game variants with target lists
			r2Done = r2.step(r2Move, choice==1, gameVariant);
		}else{// No contention
			r1Done = r1.step(r1Move, true, gameVariant);
			r2Done = r2.step(r2Move, true, gameVariant);
		}
		if(r1Done || r2Done){// One robot has completed goal
			// Tell any learning agents game is done
			MyGridGame.resetGoals(MyGridGame.getCustomGameState(simGridSize, r1.getCurrentPos(), r2.getCurrentPos(), targets));
			for(int i = 0; learningAgents !=null && i < learningAgents.size(); i++){
				learningAgents.get(i).gameTerminated();
			}
			simRunning=false;
		}
		update();
	}
	
	public static Loc[] getMoves(){
		Loc[] moves = new Loc[2];
		// Find next step based on robot mode
		if(r1.getMode())
			moves[0] = gameSolverNextStep(1, r1.getCurrentPos(), r2.getCurrentPos());
		else
			moves[0] = learningAgentNextStep(0, r1.getCurrentPos(), r2.getCurrentPos());
		if(r2.getMode())
			moves[1] = gameSolverNextStep(2, r2.getCurrentPos(), r1.getCurrentPos());
		else
			moves[1] = learningAgentNextStep(1, r1.getCurrentPos(), r2.getCurrentPos());
		
		// Handle contention
		if(moves[0].equals(moves[1])){
			// Currently implemented a random chooser.
			Random r = new Random();
			r.setSeed(System.currentTimeMillis());
			int choice = r.nextInt(2);
			if(choice==1)
				moves[0] = r1.noCollide(moves[0]);
			else
				moves[1] = r2.noCollide(moves[1]);
		}
		return moves;
	}
	
	public static void runMoves(Loc r1Move, Loc r2Move){
		// Inform any learning agents of all moves
		if(!r1.getMode() || ! r2.getMode()){
			JointAction ja = new JointAction();
			ja.setAction(1, null);
			State s = MyGridGame.getCustomGameState(simGridSize, r1Move, r2Move, targets);
			JointRewardFunction rf = new MyGridGame.MyGJointRewardFunction(domain, -1, 100, false);
			double[] jointReward = rf.reward(null, ja, s);
	
			for(SGAgent a : learningAgents){
				a.observeOutcome(null, null, jointReward, s, false);// TODO: keep an eye on always sending false for terminal state
			}
		}
		
		boolean r1Done;
		boolean r2Done;
		r1Done = r1.step(r1Move, true, gameVariant);
		r2Done = r2.step(r2Move, true, gameVariant);
		if(r1Done || r2Done){// One robot has completed goal
			// Tell any learning agents game is done
			MyGridGame.resetGoals(MyGridGame.getCustomGameState(simGridSize, r1.getCurrentPos(), r2.getCurrentPos(), targets));
			for(int i = 0; learningAgents !=null && i < learningAgents.size(); i++){
				learningAgents.get(i).gameTerminated();
			}
			simRunning=false;
		}
		update();
	}
	
	public static Robot getR1() {
		return r1;
	}

	public static Robot getR2() {
		return r2;
	}

	public static SimWindow getMainDisplay() {
		return mainDisplay;
	}

	public static int getSimGridSize() {
		return simGridSize;
	}

	public static boolean isSimRunning() {
		return simRunning;
	}
	
	public static ArrayList<Loc> getTargets() {
		return targets;
	}

	private static Robot initRobot(Loc start, int ID){
		return new Robot(ID, start, targets, simGridSize);
	}

	// Initializes a random list of target locations bounded by the 
	// current grid size.
	private static ArrayList<Loc> initTargs(int gs, Loc r1s, Loc r2s){
		ArrayList<Loc> targs = new ArrayList<Loc>();
		Random rand = new Random();
		int targNum = 1 + rand.nextInt(gs/2);

		targNum += (targNum+1)%2;// Guarantee number is odd (no ties)
		for(int i = 0; i < targNum; i++){
			//Random randX = new Random(System.currentTimeMillis());
			int x = rand.nextInt(gs);
			//Random randY = new Random(System.nanoTime());
			int y = rand.nextInt(gs);
			// Prevent dupes and targets on robot start positions
			Loc temp = new Loc(x,y);
			if(!targs.contains(temp) && !r1s.equals(temp) && !r2s.equals(temp)){
				targs.add(temp);
			}else{
				i--;
			}
		}
		return targs;
	}
	
	// Initializes a CounterFactualRegretSolver object from a generated 
	// extensive-form game tree file
	private static CounterFactualRegretSolver initGameSolver(boolean r1Mode, boolean r2Mode, Loc r1s, Loc r2s){
		// Initialize game solver
		String gameFileName = "Test_" + simGridSize + "-Grid_" + r1Mode + "_" + r2Mode + "_" + r1s.getX() + "," + r1s.getY() + "_" 
				+ r2s.getX() + "," + r2s.getY() + ".txt";
//		System.out.println("Enumerating Game tree");
//		long start = System.nanoTime();
		GameTreeEnumerator fileMaker = new GameTreeEnumerator(gameFolder + gameFileName);
//		double time = (System.nanoTime()-start)/100000;
//		System.out.println("Game tree enumerated: " + time);
		try {
			fileMaker.generateFile(r1s, r2s, simGridSize, targets);
		} catch (IOException e) {
			System.out.println("There was a problem writing to file " + gameFileName + "\n");
		}
		
		Game testGame = new Game();
//		start = System.nanoTime();
//		System.out.println("Creating game");
		testGame.createGameFromFileZerosumPackageFormat(gameFolder + gameFileName);
//		time = (System.nanoTime()-start)/100000;
//		System.out.println("Game created: " + time);
		
//		start = System.nanoTime();
//		System.out.println("Solving game");
		CounterFactualRegretSolver solver = new CounterFactualRegretSolver(testGame);
		solver.solveGame(20);
//		time = (System.nanoTime()-start)/100000;
//		System.out.println("Game Solved: " + time);
		
		return solver;
	}
	
	// Selects next action based on generated CounterFactualRegretSolver 
	// for the specified player
	private static Loc gameSolverNextStep(int player, Loc currentPos, Loc oppPosition){
		// Id actions at current position
		String[] actions = GameTreeEnumerator.idActionsAtNode(simGridSize, currentPos, oppPosition);
		
		// Identify info set of current node
		int i;
		for(i = 0; i < gameSolver.getGame().getNumInformationSets(player); i++){
			// Get info set list from gameSolver.game
			if(GameTreeEnumerator.equalActionSet(actions, ((Game)gameSolver.getGame()).getActionsAtInformationSet(player, i)))
				break;
		}
		double[] actionProb;
		// If missing an information set, randomize over actions with equal weight
		if(i >= gameSolver.getGame().getNumInformationSets(player)){			
			actionProb = new double[actions.length];
			for(int x = 0; x < actions.length; x++) 
				actionProb[x] = 1.0 / actions.length;
		}else
			// Get matching strategy profile entry
			actionProb = gameSolver.getStrategyProfile()[player][i];		
			
		// Randomize action
		Random rand = new Random();
		rand.setSeed(System.nanoTime());
		double prob = rand.nextDouble();
		int a;
		for(a = 0; a < actionProb.length; a++){
			prob -= actionProb[a];
			if(prob <= 0)
				break;
		}
		// Return new location
		if(i >= gameSolver.getGame().getNumInformationSets(player))
			return GameTreeEnumerator.actionToLoc(currentPos, actions[a]);
		else
			return GameTreeEnumerator.actionToLoc(currentPos, ((Game)gameSolver.getGame()).getActionsAtInformationSet(player, i)[a].getName());
	}
	
	private static ArrayList<LearningAgentToSGAgentInterface> initLearningAgents(boolean r1LA, boolean r2LA, Loc r1s, Loc r2s) {
		ArrayList<LearningAgentToSGAgentInterface> agents = new ArrayList<LearningAgentToSGAgentInterface>();
		MyGridGame gridGame = new MyGridGame();
		
		domain = gridGame.generateDomain();
		SGAgentType at = MyGridGame.getStandardGridGameAgentType(domain);

		// Initialize agents
		if(r1LA){
			LearningAgentToSGAgentInterface l1 = new LearningAgentToSGAgentInterface(domain, new QLearning(null, 0.99, new SimpleHashableStateFactory(), 0, 0.1), "agent0", at);
			l1.gameStarting(null, 0);
			agents.add(l1);
			
		}
		if(r2LA){
			LearningAgentToSGAgentInterface l2 = new LearningAgentToSGAgentInterface(domain, new QLearning(null, 0.99, new SimpleHashableStateFactory(), 0, 0.1), "agent1", at);
			l2.gameStarting(null, 1);
			agents.add(l2);		
		}
		// Train agents
		MATrainer t = new MATrainer();
		t.runTrainingSet(domain, MyGridGame.getCustomGameState(simGridSize, r1s, r2s, targets), agents, 2);
		
		return agents;
	}
	
	// Returns a location selected by a stochastic learning agent interface
	// defined in the BURLAP project
	private static Loc learningAgentNextStep(int id, Loc r1s, Loc r2s){
		//JointModel worldModel = domain.getJointActionModel();
		Loc currentPos = null;
		if(id==0) 
			currentPos = r1s;
		else 
			currentPos = r2s;
		
		State currentState = MyGridGame.getCustomGameState(simGridSize, r1s, r2s, targets);
		JointAction ja = new JointAction();

		int index;
		if(learningAgents.size()>1)
			index = id;
		else
			index = 0;
		
		Loc step = new Loc(0,0);
		
		do{
			// Get action from learning agent
			ja.setAction(index, learningAgents.get(index).action(currentState));
			
//			System.out.println(ja);
//			// Sample action outcome from world model
//			State sp = worldModel.sample(currentState, ja);
//			
//			// Get this agent from list to fetch its location
//			GGAgent a = (GGAgent) ((OOState) sp).object("agent"+id);//objectsOfClass(MyGridGame.CLASS_AGENT).get(id-1);
//			
			switch(ja.getActions().get(index).actionName()){
				case MyGridGame.ACTION_NORTH:step = new Loc(currentPos.getX(), currentPos.getY()+1);
				break;
				case MyGridGame.ACTION_SOUTH:step = new Loc(currentPos.getX(), currentPos.getY()-1);
				break;
				case MyGridGame.ACTION_EAST:step = new Loc(currentPos.getX()+1, currentPos.getY());
				break;
				case MyGridGame.ACTION_WEST:step = new Loc(currentPos.getX()-1, currentPos.getY());
				break;
			}
			
		// Make sure chosen action keeps agent on grid
		}while(step.getX()>=simGridSize||step.getY()>=simGridSize||step.getX()<0||step.getY()<0);

		return step;
	}
	
	
	// Updates the GUI display (if applicable)
	private static void update() {
		if(mainDisplay!=null){ // May not be initialized if running simDriver remotely
			StringBuilder sb = new StringBuilder();
			sb.append("Targets: ");
			for(Loc t: targets){
				sb.append("\n"+t.toString());
			}
			sb.append("\n\nRobot 1: "+r1.getCurrentPos().toString());
			sb.append("\nRobot 2: "+r2.getCurrentPos().toString());
			sb.append("\n\nRobot 1 Score: "+r1.getScore());
			sb.append("\nRobot 2 Score: "+r2.getScore());
			mainDisplay.updateData(sb.toString());
			mainDisplay.getGridView().updateData(r1.getCurrentPos(), r2.getCurrentPos());
		}
	}
	
	// Performs input sanitation on generated InputWindow object
	private static void checkInput(InputWindow iw, int gameVar, int gridSize, Loc r1S, Loc r2S, boolean r1Mode, boolean r2Mode){
		int r1X = r1S.getX();
		int r1Y = r1S.getY();
		int r2X = r2S.getX();
		int r2Y = r2S.getY();
		if (gridSize <= 0){
			iw.addErrorLabel("Grid size must be a positive, non-zero integer");
		}else if(r1X<0 || r1Y<0 || r1X>=gridSize || r1Y>=gridSize){
			iw.addErrorLabel("Robot 1 position is out of specified grid bounds");
		}else if(r2X<0 || r2Y<0 || r2X>=gridSize || r2Y>=gridSize){
			iw.addErrorLabel("Robot 2 position is out of specified grid bounds");
		}else if(r1X==r2X && r1Y==r2Y){
			iw.addErrorLabel("Robots must have different starting locations");
		}else{
			iw.dispose();
			newSim(gameVar, gridSize, r1S, r2S, r1Mode, r2Mode);
		}
	}
	
	// Initializes a GUI window. Listeners are added here to better fit
	// an MVC model
	private static SimWindow initUI(){
		SimWindow sw = new SimWindow();
		sw.setVisible(true);
		
		// Add listeners
		sw.addNewBtnListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(!simRunning){
					InputWindow iw = new InputWindow();
					iw.addSubmitListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							iw.addErrorLabel("");
							int gameVar = iw.getGameVar();
							int inSize = iw.getInputSize();
							Loc inR1 = iw.getRob1Start();
							Loc inR2 = iw.getRob2Start();
							boolean in1M = iw.get1Mode();
							boolean in2M = iw.get2Mode();
							checkInput(iw, gameVar, inSize, inR1, inR2, in1M, in2M);
						}
					});
					iw.setLocationRelativeTo(mainDisplay);
					iw.setVisible(true);
				}// end if
			}
		});
		sw.addNextBtnListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0){
				if(!simRunning)return;
				run();
			}
		});
		sw.addFinBtnListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0){
				if(!simRunning)return;
				while(simRunning){
					run();
				}
			}
		});
		sw.addStopBtnListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0){
				simRunning = false;
			}
		});
		return sw;
	}
	
	
}// end Driver
