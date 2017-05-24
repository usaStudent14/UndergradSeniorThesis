package model;

import java.util.ArrayList;
import java.util.List;

public class Robot{

	// Instance variables
	private int ID;
	private Loc currentPos;
	private int gridSize;
	private ArrayList<Loc> targets;// Usually a given set of locations to hit for success
	private boolean mode;// Indicates robot behavior: true->GameSolver, false->LearningAlg
	private ArrayList<Loc> path;// Holds all locations visited by robot
	
	// methods
	public Robot(int id, Loc start, List<Loc> list, int gs) {
		ID = id;
		currentPos = start;
		gridSize = gs;
		targets = new ArrayList<Loc>();
		for(Loc t: list){
			targets.add(new Loc(t));
		}
		mode = true;
		path = new ArrayList<Loc>();
		path.add(start);
	}
	
	public boolean step(Loc next, boolean goodMove, int gameVar){
		if(!goodMove){// Choose different move
			next = noCollide(next);
		}
		currentPos = next;
		path.add(currentPos);
		// Check if on target
		if(targets.contains(currentPos)){
			if(targets.get(targets.indexOf(currentPos)).targHitBy()==0)
				targets.get(targets.indexOf(currentPos)).setTargHit(ID); // Target hit by this robot
			else if(gameVar==1 && targets.get(targets.indexOf(currentPos)).targHitBy()!=ID)
				targets.get(targets.indexOf(currentPos)).setTargHit(3); // Target hit by both robots
			// Check if done
			return checkDone();
		}
		return false;
	}
	
	/**
	 * Takes default move selected by main algorithm, checks
	 * for possible collision with opponent and, if collision
	 * is possible, returns a location on the opposite axis that
	 * is on the board.
	 * @param def - default move selected by main algorithm
	 * @return move that will not result in a collision or breach
	 * of board boundary
	 */
	public Loc noCollide(Loc blocked){
		Loc safeMove = currentPos;
		int xDif = blocked.getX()-currentPos.getX();
		int yDif = blocked.getY()-currentPos.getY();
		safeMove = new Loc(currentPos.getX()+yDif, currentPos.getY()+xDif);
		if(!safeMove.onBoard(gridSize))
			safeMove = new Loc(currentPos.getX()-yDif, currentPos.getY()-xDif);
		return safeMove;
	}

//	/**
//	 * Returns closest element from a list to a given
//	 * position.
//	 * @param p - position from which to search
//	 * @param t - set of locations to search
//	 * @return - closest location in t to p
//	 */
//	private Loc getClosestLoc(Loc p, ArrayList<Loc> t){
//		int minDist = 10000;
//		Loc closest = t.get(0);
//		for(Loc targ : t){
//			if(targ.isTargHit())
//				continue;
//			int xdist = targ.getX()-p.getX();
//			int ydist = targ.getY()-p.getY();
//			int dist = Math.abs(xdist) + Math.abs(ydist);
//			if(dist>0 && dist<minDist){
//				minDist = dist;
//				closest = targ;
//			}
//		}
//		return closest;
//	}

	private boolean checkDone(){
		if(getScore()>=targets.size())
			return true;
		else
			return false;
	}

	public void switchMode(boolean mode){
		this.mode = mode;
	}
	
	public int getID(){
		return ID;
	}
	
	public int getScore(){
		int hit = 0;
		for(Loc t: targets){
			if (t.targHitBy()==ID)
				hit++;
		}
		return hit;
	}
	
	public boolean getMode(){
		return mode;
	}
	
	public Loc getCurrentPos() {
		return currentPos;
	}

	public ArrayList<Loc> getTargets() {
		return targets;
	}
	
	public ArrayList<Loc> getPath() {
		return path;
	}

}// end Robot