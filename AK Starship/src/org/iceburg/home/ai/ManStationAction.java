package org.iceburg.home.ai;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.ship.Tile;

public class ManStationAction extends AIAction {
	private Tile goal;

	public ManStationAction(AI parent, Tile goal, CrewMan man) {
		super(parent);
		// this.parent = parent;
		this.goal = goal;
		this.man = man;
	}
	@Override
	public String toString() {
		return "Man " + goal.getName() + " action";
	}
	@Override
	public void startAction() {
		// if we've arrived at our station to be manned, let's man it
		if (goal != null && man.getLocationTile().isTouching(goal)) {
			// System.out.println("Man that station!");
			// TODO - update crew pane under set manned station
			man.setCurrentTile(goal);
			man.getCurrentTile().startManStation(man);
		}
		endAction();
	}
}
