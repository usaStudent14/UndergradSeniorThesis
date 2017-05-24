package control;

import model.Loc;

import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Batch program for multiple simulation executions. Calls {@link SimDriver.remoteNewSim} to
 * generate simulations, passing it series of grid sizes and player start locations. Writes
 * the resultant data to .xls file.
 * @author Alex Henderson
 *
 */
public class DataGenerator {

	static StringBuilder testString = new StringBuilder();
	static int gameID = 0;
	
	public static void main(String[] args) {
		String outputDirectory = "C:\\Users\\Alex\\Code\\SeniorThesis\\data\\";

		testString.append("Game type\tGrid Size\tRobot 1 Start Position\tRobot 2 Start Position\tRobot 1 Model\tRobot 2 Model" +
				"\tTarget Locations\tRobot 1 Path\tRobot 2 Path\tRobot 1 Score\tRobot 2 Score\n");
		
//		runSimSet(2, true, true);
//		runSimSet(2, false, false);
//		runSimSet(2, true, false);
//		runSimSet(3, true, true);
//		runSimSet(3, false, false);
//		runSimSet(3, true, false);
//		runSimSet(4, true, true);
//		runSimSet(4, false, false);
//		runSimSet(4, true, false);
//		runSimSet(5, true, true);
//		runSimSet(5, false, false);
		runSimSet(5, true, false);
		datOutput(outputDirectory+"4_14_Test2.xls", testString.toString());
		
	}
	
	/**
	 * Generates an exhaustive list of player start location pairs, bounded
	 * by the given grid size.
	 * @param gridSize - length/height of game grid
	 * @return a list of player start location pairs, covering the entire grid
	 */
	protected static ArrayList<Loc[]> generateStarts(int gridSize){
		ArrayList<Loc[]> starts = new ArrayList<Loc[]>();
		for(int s1x = 0, s1y = 0; s1x<gridSize && s1y<gridSize; s1x++){
			for(int s2x = gridSize-1, s2y = gridSize-1; s2x>=0 && s2y>=0; s2x--){
				if(s1x!=s2x || s1y!=s2y){
					Loc[] a = {new Loc(s1x,s1y), new Loc(s2x,s2y)};
					starts.add(a);
				}
				if(s2x==0){s2x=gridSize; s2y--;}
			}// end inner loop
			if(s1x==gridSize-1){s1x=-1; s1y++;}
		}
		return starts;
	}

	/**
	 * Runs a set of simulations for a given grid size and all possible
	 * start location combinations
	 * @param gridSize - length/height of game grid
	 */
	protected static void runSimSet(int gridSize, boolean r1Mode, boolean r2Mode){
		ArrayList<Loc[]> startPos = generateStarts(gridSize);
		for(int i = 0; i < startPos.size(); i++){
			// Run for variant 1
			System.out.println("Running: Grid " + gridSize + ", R1 " + startPos.get(i)[0].toString() + " "
					+ r1Mode + " R2 " + startPos.get(i)[1].toString() + " " + r2Mode);
			SimDriver.remoteNewSim(0, gridSize, startPos.get(i)[0], startPos.get(i)[1], r1Mode, r2Mode);
			// Add game type to output string
			testString.append("0\t");
		
			quickRun(gridSize, startPos, i);
			
			// Run for variant 2
			// TODO: uncomment once both variants working
			//SimDriver.remoteNewSim(1, gridSize, startPos.get(i)[0], startPos.get(i)[1], true, true);
			//testString.append("1\t");
					
			//quickRun(gridSize, startPos, i);
		}
	}
	
	/**
	 * Runs a single simulation and records the results.
	 * @param gridSize - length/height of game grid
	 * @param startPos - start location pair for both players
	 * @param i - current simulation ID
	 */
	protected static void quickRun(int gridSize, ArrayList<Loc[]> startPos, int i){
		// Add grid size to parameter strings
		testString.append(gridSize+"\t");
		// Add current robot starts to parameter strings
		testString.append(startPos.get(i)[0].getX()+", "+startPos.get(i)[0].getY()+"\t");// R1
		testString.append(startPos.get(i)[1].getX()+", "+startPos.get(i)[1].getY()+"\t");// R2
		testString.append(SimDriver.getR1().getMode() + "\t");
		testString.append(SimDriver.getR2().getMode() + "\t");
		
		
		// Add new target set to database
		for(int t = 0;t<SimDriver.getTargets().size();t++){
			testString.append("(" + SimDriver.getTargets().get(t).getX()+", "+SimDriver.getTargets().get(t).getY()+") ");
		}
		testString.append("\t");
		int timeout = 0;
//		System.out.println("Beginning Simulation");
		long start = System.currentTimeMillis();
		
		for(; SimDriver.isSimRunning() && timeout<Math.pow(gridSize, 4); timeout++){
			SimDriver.run();
		}
		
		long time = (System.currentTimeMillis() - start)/1000;
//		System.out.println("Simulation done: " + time + " seconds");
		int r1Score;
		int r2Score;
		if(timeout>=Math.pow(gridSize, 4)){
			r1Score = -1;
			r2Score = -1;
		}else{
			// Add sim score to result strings (targets hit or -1 if sim timed out)
			r1Score = SimDriver.getR1().getScore();
			r2Score = SimDriver.getR2().getScore();
		}
		
		testString.append(SimDriver.getR1().getPath().toString() + "\t");
		testString.append(SimDriver.getR2().getPath().toString() + "\t");
		testString.append(r1Score + "\t");
		testString.append(r2Score + "\n");
		
		// Increment game Id variable
		gameID += 1;
	}
	
	/**
	 * Writes recorded data for all simulations to an .xls file.
	 * @param filename - name of .xls file
	 * @param data - simulations results output string
	 */
	protected static void datOutput(String filename, String data){
		try {
			FileWriter out = new FileWriter(filename);
			out.write(data);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
