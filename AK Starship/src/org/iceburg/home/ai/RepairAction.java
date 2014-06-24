package org.iceburg.home.ai;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.main.Home;
import org.iceburg.home.ship.Tile;

public class RepairAction extends AIAction {
	Tile tile;
	boolean positive;
	int inc;

	public RepairAction(AI parent, Tile tile, CrewMan man, boolean positive) {
		super(parent);
		this.man = man;
		this.tile = tile;
		this.positive = positive;
		this.inc = man.getSkillFor(tile);
	}
	@Override
	public String toString() {
		return "Repair " + tile.getName() + " action";
	}
	@Override
	public void updateAction() {
		if (getTimer() <= 100) {
			incTimer(inc);
		} else {
			setTimer(0);
			if (tile != null && man.isTouching(man.getLocation(), tile.getLocation())) {
				if (!tile.isPowered()) {
					if (positive && tile.getHealth() < tile.getHealthTotal()) {
						//if on own ship out of battle, make instant
						if (tile.getParentShip() == Home.getShip() && !Home.getBattleLoc().isInBattle()){
							tile.setHealth(tile.getHealthTotal());
						}else{
							tile.setHealth(tile.getHealth() + 1);
						}
					} else if (!positive && tile.getHealth() > 0) {
						//if on own ship out of battle, make instant
						if (!Home.getBattleLoc().isInBattle()){
							tile.setHealth(0);
						}else{
							tile.setHealth(tile.getHealth() - 1);
						}
					}
				}
			} else {
				endAction();
			}
		}
	}
}
