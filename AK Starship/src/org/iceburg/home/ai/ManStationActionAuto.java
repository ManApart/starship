package org.iceburg.home.ai;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.ship.Tile;

public class ManStationActionAuto extends AIAction {
	private Tile goal;

	public ManStationActionAuto(AI parent, Tile goal, CrewMan man) {
		super(parent);
		// this.parent = parent;
		this.goal = goal;
		this.man = man;
	}
	@Override
	public String toString() {
		return "Man " + goal.getName() + " action - auto";
	}
	@Override
	public void startAction() {
		// System.out.println("ManStation Auto: Start auto action");
		// if we've arrived at our station to be manned, let's man it
		if (goal != null && man.getLocationTile().isTouching(goal)) {
			// System.out.println("Man that station!");
			// TODO - update crew pane under set manned station
			man.setCurrentTile(goal);
			man.getCurrentTile().startManStation(man);
		} else {
			endAction();
		}
	}
	@Override
	public void updateAction() {
		if (timer < 10) {
			timer += 1;
		} else {
			// skip to the next action if we have one
			// if (getParent().hasNextAction() &&
			// getParent().getActions().get(1) instanceof TakeTileAction){
			// endAction();
			// return;
			// }
			// System.out.println("ManStation Auto: update auto action");
			timer = 0;
			if (goal != null && goal.hasParentSystem()) {
				goal.getParentSystem().manSystemAuto(man);
			}
		}
	}
}
