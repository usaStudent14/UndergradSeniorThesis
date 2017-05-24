package model;

public class Loc implements Cloneable{

	// Instance variables
	private final int X;
	private final int Y;
	private int hitBy;// 0-not hit; 1-hit by robot 1; 2-hit by robot 2; 3-hit by both
	
	// Methods
	public Loc(int x, int y) {
		X = x;
		Y = y;
		hitBy = 0;
	}
	// Copy Constructor
	public Loc(Loc o){
		X = o.getX();
		Y = o.getY();
		hitBy = o.targHitBy();
	}
	public boolean equals(Object ob){
	    if (ob == null) return false;
	    if (ob.getClass() != getClass()) return false;
	    Loc other = (Loc)ob;
	    if (X!=other.X) return false;
	    if (Y!=other.Y) return false;
	    return true;
	}

	public Object clone(){
		return new Loc(X, Y);
		
	}
	public int getX() {
		return X;
	}

	public int getY() {
		return Y;
	}
	
	public int targHitBy(){
		return hitBy;
	}
	
	public void setTargHit(int i){
		hitBy = i;
	}
	
	public boolean onBoard(int gs){
		return X>=0 && X<gs && Y>=0 && Y<gs;
	}
	
	public String toString(){
		return "(" + X + ", " + Y + ")";
	}
	
}// end Loc
