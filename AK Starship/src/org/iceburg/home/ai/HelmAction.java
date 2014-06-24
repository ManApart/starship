package org.iceburg.home.ai;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.main.Home;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.ship.systems.HelmSystem;

public class HelmAction extends AIAction {
	Tile tile;

	public HelmAction(CrewMan man, AI parent, Tile tile) {
		super(parent);
		this.man = man;
		this.tile = tile;
	}
	@Override
	public String toString() {
		return "Helm action";
	}
	@Override
	public void startAction() {
		Home.messagePanel.addMessage(man, "Piloting the ship now, sir");
	}
	@Override
	public void updateAction() {
		if (getTimer() <= 10) {
			incTimer(man.getNavigationLvl());
		} else {
			setTimer(0);
			// only update if autopilot is on
			if (tile != null && ((HelmSystem) tile.getParentSystem()).isAutoPilot()) {
				if (man.isTouching(man.getLocation(), tile.getLocation())) {
					man.getParentShip().getHelm().manuver(man, this);
				} else {
					endAction();
				}
			}
		}
	}
}
