package org.iceburg.home.ai;

import org.iceburg.home.actors.CrewMan;

//AI Actions are instructions that can be stored and then followed in sequence
//when added to AI they take and store the information they need to run
//and then run when it's their turn
public class AIAction {
	AI parent;
	CrewMan man;
	int timer;

	public AIAction(AI parent) {
		this.parent = parent;
	}
	public void startAction() {
	}
	public void updateAction() {
	}
	public void endAction() {
		parent.endAction(this);
	}
	public AI getParent() {
		return parent;
	}
	public void setParent(AI parent) {
		this.parent = parent;
	}
	public CrewMan getMan() {
		return man;
	}
	public void setMan(CrewMan man) {
		this.man = man;
	}
	public int getTimer() {
		return timer;
	}
	public void setTimer(int timer) {
		this.timer = timer;
	}
	public void incTimer(int amount) {
		setTimer(getTimer() + amount);
	}
}
