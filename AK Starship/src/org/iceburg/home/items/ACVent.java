package org.iceburg.home.items;

import java.util.ArrayList;

import org.iceburg.home.ship.Tile;
import org.iceburg.home.ship.systems.LifeSupport;
import org.iceburg.home.ui.TilePane;
import org.iceburg.home.ui.UIMisc;

;
public class ACVent extends Item {
	public boolean open;
	// TODO set up drainrate parser
	int drainRate;

	public ACVent() {
		open = true;
	}
	@Override
	public String toString() {
		return getName() + ", Air: " + getParentTile().getAirLevel();
	}
	@Override
	public void setHealth(int health) {
		super.setHealth(health);
		if (getHealth() == 0){
			setOpen(true);
		}
	}
	public void updateVent() {
		if (open || getHealth() <= 0) {
			ArrayList<Tile> list = getParentTile().findAOETilesFlat(1);
			for (int i = 0; i < list.size(); i++) {
				Tile tile = list.get(i);
				// only non solid tiles
				if (tile.isFlowTile() && tile.getAirLevel() < 100) {
					// how much air can we vent in?
					int taken = Math.min(getCurrentDrainRate(), getAirAvailable());
					// don't over fill tiles
					int needed = 100 - tile.getAirLevel();
					taken = Math.min(taken, needed);
					// vent in the air
					if (taken > 0) {
						incAirAvailable(-taken);
						tile.incAirLevel(taken);
					}
				}
			}
		}
	}
	/**
	 * Returns the amount that this vent drains to each surrounding tile when
	 * the vent is open
	 */
	public int getCurrentDrainRate() {
		return getDrainRate()*2;
	}
	/**
	 * Amount of air available/ stored in the life support system
	 */
	public int getAirAvailable() {
		return ((LifeSupport) getParentTile().getParentSystem()).getAirCurrent();
	}
	public void incAirAvailable(int amount) {
		((LifeSupport) getParentTile().getParentSystem()).incAirCurrent(amount);
	}
	public int getDrainRate() {
		return drainRate;
	}
	public void setDrainRate(int drainRate) {
		this.drainRate = drainRate;
	}
	public boolean isOpen() {
		return open;
	}
	public void setOpen(boolean open) {
		//at 0 health, always open
		if (getHealth() <= 0){
			open = true;
		}
		this.open = open;
		TilePane pane = TilePane.containsTile(getParentTile());
		if (pane != null) {
			pane.updatePane();
		}
	}
	
}
