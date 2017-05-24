package reenforcementlearning.model;

import static reenforcementlearning.model.MyGridGame.*;

import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.core.state.UnknownKeyException;

public class MyGGoal implements ObjectInstance, MutableState {
	public int x;
	public int y;
	public int touchedByPlayer;
	
	protected String name;

	public MyGGoal() {
	}

	private static final List<Object> keys = Arrays.<Object>asList(VAR_X, VAR_Y, VAR_PTG);

	public MyGGoal(int x, int y, String name) {
		this.x = x;
		this.y = y;
		this.touchedByPlayer = 0;
		this.name = name;

	}
	
	public MyGGoal(int x, int y, int ptg, String name){
		this.x = x;
		this.y = y;
		this.touchedByPlayer = ptg;
		this.name = name;
	}

	@Override
	public String className() {
		return CLASS_GOAL;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new MyGGoal(this.x, this.y, this.touchedByPlayer, objectName);
	}

	@Override
	public MutableState set(Object variableKey, Object value) {
		int i = StateUtilities.stringOrNumber(value).intValue();

		if(variableKey.equals(VAR_PTG)){
			if(i==0)
				this.touchedByPlayer=0;
			else if(this.touchedByPlayer!=0 && this.touchedByPlayer!=i)
				this.touchedByPlayer = 3;
			else
				this.touchedByPlayer = i;
		}
		else if(variableKey.equals(VAR_X)){
			this.x = i;
		}
		else if(variableKey.equals(VAR_Y)){
			this.y = i;
		}
		else if(variableKey.equals(VAR_GT)){
		}
		else{
			throw new UnknownKeyException(variableKey);
		}

		return this;
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

	@Override
	public Object get(Object variableKey) {
		if(variableKey.equals(VAR_PTG)){
			return touchedByPlayer;
		}
		if(variableKey.equals(VAR_X)){
			return x;
		}
		else if(variableKey.equals(VAR_Y)){
			return y;
		}
		else if(variableKey.equals(VAR_GT)){
			return 0;
		}
		else{
			throw new UnknownKeyException(variableKey);
		}
	}

	@Override
	public State copy() {
		return new MyGGoal(this.x, this.y, this.touchedByPlayer, this.name);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return OOStateUtilities.objectInstanceToString(this);
	}
}
