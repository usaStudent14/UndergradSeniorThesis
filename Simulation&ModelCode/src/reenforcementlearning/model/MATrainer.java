package reenforcementlearning.model;
import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.stochasticgames.GameEpisode;
import burlap.behavior.stochasticgames.agents.interfacing.singleagent.LearningAgentToSGAgentInterface;
import burlap.debugtools.DPrint;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.model.JointRewardFunction;
import burlap.mdp.stochasticgames.oo.OOSGDomain;
import burlap.mdp.stochasticgames.world.World;
import burlap.statehashing.simple.SimpleHashableStateFactory;

/**
 * Example code showing the usage of CoCo-Q and Correlated-Q operators with planning (VI) and learning (Q-learning),
 * and using single agent learning algorithms on two-player Grid Games (a multi-agent stochastic game).
 * From main, comment/uncomment the example method you want to run.
 * @author James MacGlashan.
 */
public class MATrainer {

	public void runTrainingSet(OOSGDomain domain, State s, ArrayList<LearningAgentToSGAgentInterface> agents, int an){

		JointRewardFunction rf = new MyGridGame.MyGJointRewardFunction(domain, -1, 100, false);
		TerminalFunction tf = new MyGridGame.MyGTerminalFunction(domain);

		World w = new World(domain, rf, tf, s);
		int i = 0;
		for(; i < agents.size(); i++){
			w.join(agents.get(i));
		}
		for(int j = i; j < an; j++){
			w.join(new LearningAgentToSGAgentInterface(domain, 
					new QLearning(null, 0.99, new SimpleHashableStateFactory(), 0, 0.1), 
					"testagent"+String.valueOf(j), 
					agents.get(0).agentType()));
		}

		//don't have the world print out debug info (comment out if you want to see it!)
		DPrint.toggleCode(w.getDebugId(), false);

		int ngames = 1000;
		List<GameEpisode> gas = new ArrayList<GameEpisode>(ngames);
		for(int g = 0; g < ngames; g++){
			MyGridGame.resetGoals(w.getCurrentWorldState());
			GameEpisode ga = w.runGame();
			gas.add(ga);
		}
		
//		Visualizer v = GGVisualizer.getVisualizer(9, 9);
//		new GameSequenceVisualizer(v, domain, gas);
	}
	
}