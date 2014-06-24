package org.iceburg.home.ai;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.main.Home;
import org.iceburg.home.ship.Tile;

public class HealAction extends AIAction {
	Tile tile;

	public HealAction(CrewMan man, AI parent, Tile tile) {
		super(parent);
		this.man = man;
		this.tile = tile;
	}
	@Override
	public String toString() {
		return "Heal action";
	}
	@Override
	public void startAction() {
		Home.messagePanel.addMessage(man, "Running Sickbay, sir");
	}
	@Override
	public void updateAction() {
		if (getTimer() <= 100) {
			incTimer(man.getAnalysisLvl());
		} else {
			setTimer(0);
			if (tile != null && man.isTouching(man.getLocation(), tile.getLocation())) {
				man.getParentShip().getMedbay().healCrew(man, this);
			} else {
				endAction();
			}
		}
	}
}
