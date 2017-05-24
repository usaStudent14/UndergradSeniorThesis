package reenforcementlearning.model;

import java.util.ArrayList;
import java.util.List;

import burlap.domain.stochasticgames.gridgame.GridGame;
import burlap.domain.stochasticgames.gridgame.GridGameStandardMechanics;
import burlap.domain.stochasticgames.gridgame.state.GGAgent;
import burlap.domain.stochasticgames.gridgame.state.GGWall;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.oo.state.generic.GenericOOState;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.oo.OOSGDomain;
import model.Loc;

public class MyGridGame extends GridGame{

	/**
	 * A constant indicating which players have touched a goal
	 */
	public static final String VAR_PTG = "playerTouchGoal";

	/**
	 * A constant for the name of a propositional function that evaluates whether an agent has touched all goals
	 */
	public static final String PF_HAS_M_SC = "hasMaxScore";
	
	/**
	 * A constant for the name of a propositional function that evaluates whether an agent has touched a goals
	 */
	public static final String PF_IN_G = "inGoal";

	@Override
	public OOSGDomain generateDomain() {
		
		OOSGDomain domain = new OOSGDomain();
		
		
		domain.addStateClass(CLASS_AGENT, GGAgent.class)
				.addStateClass(CLASS_GOAL, MyGGoal.class)
				.addStateClass(CLASS_DIM_H_WALL, GGWall.GGHorizontalWall.class)
				.addStateClass(CLASS_DIM_V_WALL, GGWall.GGVerticalWall.class);
		

		domain.addActionType(new UniversalActionType(ACTION_NORTH))
				.addActionType(new UniversalActionType(ACTION_SOUTH))
				.addActionType(new UniversalActionType(ACTION_EAST))
				.addActionType(new UniversalActionType(ACTION_WEST));
		
		domain.setJointActionModel(new GridGameStandardMechanics(domain, this.semiWallProb));
				
		OODomain.Helper.addPfsToDomain(domain, new AgentMaxScore(PF_HAS_M_SC), new AgentInGoal(PF_IN_G));
		
		return domain;
	}


	public static State getCustomGameState(int gridSize, Loc r1, Loc r2, ArrayList<Loc> goals){
		ObjectInstance[] state = new ObjectInstance[goals.size()+2];
		
		state[0] = new GGAgent(r1.getX(), r1.getY(), 0, "agent0");
		state[1] = new GGAgent(r2.getX(), r2.getY(), 1, "agent1");

		for(int i = 0; i < goals.size(); i++){
			state[i+2] = new MyGGoal(goals.get(i).getX(), goals.get(i).getY(), goals.get(i).targHitBy(), "g"+String.valueOf(i));
		}
		
		GenericOOState s = new GenericOOState(state);
		
		setBoundaryWalls(s, gridSize, gridSize);

		return s;
	}
	
	public static void resetGoals(State s){
		List<ObjectInstance> goals = ((OOState) s).objectsOfClass(CLASS_GOAL);
		for(ObjectInstance g : goals){
			((MutableState) g).set(VAR_PTG, 0);
		}
	}
	
	static class AgentInGoal extends PropositionalFunction{
		
		public AgentInGoal(String name){
			super(name, new String[]{CLASS_AGENT, CLASS_GOAL});
		}
		
		@Override
		public boolean isTrue(OOState s, String... params){
			ObjectInstance agent = s.object(params[0]);
			ObjectInstance goal = s.object(params[1]);
			
			int ax = (Integer)agent.get(VAR_X);
			int ay = (Integer)agent.get(VAR_Y);
			int apn = (Integer)agent.get(VAR_PN)+1;
			
			int gx = (Integer)goal.get(VAR_X);
			int gy = (Integer)goal.get(VAR_Y);
			int hit = (Integer)goal.get(VAR_PTG);
			
			if(gx == ax && gy == ay && hit != apn && hit != 3){
				((MutableState)goal).set(VAR_PTG, apn);
				return true;
			}
			
			return false;
		}
	}
	
	
	static class AgentMaxScore extends PropositionalFunction{

		/**
		 * Initializes with the given name and domain and is set to evaluate on agent objects
		 * @param name the name of the propositional function
		 */
		public AgentMaxScore(String name) {
			super(name, new String[]{CLASS_AGENT});
		}

		@Override
		public boolean isTrue(OOState s, String... params) {
			
			ObjectInstance agent = s.object(params[0]);
			int apn = (Integer)agent.get(VAR_PN)+1;
			
			int score = 0;
			//find all universal goals
			List <ObjectInstance> goals = s.objectsOfClass(CLASS_GOAL);
			for(ObjectInstance g : goals){
				if((Integer)g.get(VAR_PTG)==apn || (Integer)g.get(VAR_PTG)==3)
					score++;
			}
			if(score==goals.size())
				return true;
			return false;
		}

	}
	
	/**
	 * Specifies goal rewards and default rewards for agents. Defaults rewards to 0 reward everywhere except transition to unviersal or personal goals which return a reward 1.
	 * @author James MacGlashan
	 *
	 */
	public static class MyGJointRewardFunction extends GGJointRewardFunction {
		PropositionalFunction agentInGoal;
		double goalReward = 1.;

		public MyGJointRewardFunction(OODomain ggDomain){
			super(ggDomain);
			agentInGoal = ggDomain.propFunction(MyGridGame.PF_IN_G);
		}
		
		/**
		 * Initializes for a given domain, step cost reward and goal reward.
		 * @param ggDomain the domain
		 * @param stepCost the reward returned for all transitions except transtions to goal locations
		 * @param goalReward the reward returned for transitioning to a personal or universal goal
		 * @param noopIncursStepCost if true, then noop actions also incur the stepCost reward; if false, then noops always return 0 reward.
		 */
		public MyGJointRewardFunction(OODomain ggDomain, double stepCost, double goalReward, boolean noopIncursStepCost){
			super(ggDomain, stepCost, goalReward, noopIncursStepCost);
			agentInGoal = ggDomain.propFunction(MyGridGame.PF_IN_G);
		}
		
		@Override
		public double[] reward(State s, JointAction ja, State sp) {

			OOState osp = (OOState)sp;

			double [] rewards = new double[ja.size()];
			
			//get all agents and initialize reward to default
			List <ObjectInstance> obs = osp.objectsOfClass(GridGame.CLASS_AGENT);
			for(ObjectInstance o : obs){
				int aid = ((GGAgent)o).player;
				rewards[aid] = this.defaultCost(aid, ja);
			}
			
			//check for any agents that reached a goal location and give them a goal reward if they did
			List<GroundedProp> upgps = agentInGoal.allGroundings((OOState)sp);
			for(GroundedProp gp : upgps){
				String agentName = gp.params[0];
				if(gp.isTrue(osp)){
					int aid = ((GGAgent)((OOState) sp).object(agentName)).player;
					rewards[aid] = goalReward;
				}
			}	
			
			return rewards;
		}

	}
	
	
	public static class MyGTerminalFunction implements TerminalFunction {
		PropositionalFunction agentHasMaxScore;
		//PropositionalFunction agentInGoal;
		
		public MyGTerminalFunction(OODomain ggDomain) {
			agentHasMaxScore = ggDomain.propFunction(MyGridGame.PF_HAS_M_SC);
			//agentInGoal = ggDomain.propFunction(MyGridGame.PF_IN_G);
		}
		
		@Override
		public boolean isTerminal(State s){
	
			List<GroundedProp> msps = agentHasMaxScore.allGroundings((OOState)s);
			//List<GroundedProp> igps = agentInGoal.allGroundings((OOState)s);
			
//			for(GroundedProp ig : igps){
//				ig.isTrue((OOState) s);
//			}
						
			for(GroundedProp msp : msps){
				if(msp.isTrue((OOState)s)){
					return true;
				}
			}
			
			return false;
		}
		
	}
}
