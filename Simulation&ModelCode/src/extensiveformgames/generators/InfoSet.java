package extensiveformgames.generators;

public class InfoSet {

	private String[] actions;
	private int count;
	
	public InfoSet(){
		actions = new String[4];
		count = 0;
	}
	
	public InfoSet(String[] actions){
		this.actions = actions.clone();
		count = 0;
	}
	
	public void increaseCount(){
		count++;
	}
	
	public void decreaseCount(){
		count--;
	}

	public String[] getActions() {
		return actions;
	}

	public int getCount() {
		return count;
	}
}
