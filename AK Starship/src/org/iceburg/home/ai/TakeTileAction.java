package org.iceburg.home.ai;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.main.Home;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.ship.systems.CargoHold;
import org.iceburg.home.ui.CrewPane;
import org.iceburg.home.ui.GameScreen;

public class TakeTileAction extends AIAction {
	Tile targetTile;
	boolean take;

	/**
	 * Take's or places an item
	 */
	public TakeTileAction(AI parent, CrewMan man, Tile tile, boolean take) {
		super(parent);
		this.man = man;
		this.targetTile = tile;
		this.take = take;
	}
	@Override
	public String toString() {
		if (take) {
			return "Take " + targetTile.getName() + " action";
		} else {
			return "Place " + targetTile.getName() + " action";
		}
	}
	@Override
	public void startAction() {
		// if (targetTile != null){
		// cargohold behavior
		if (targetTile.getHealth() > 0
				&& targetTile.getTileColor().equals(CargoHold.systemMain)) {
			if (take) {
				CargoHold.attemptTakeItem(getMan(), targetTile);
			} else {
				CargoHold.attemptStoreItem(getMan(), targetTile);
			}
		}
		// normal grab / place
		else {
			if (take) {
				man.attemptPickUpItem(targetTile);
			} else {
				man.attemptPlaceItem(targetTile);
			}
		}
		// }
		endAction();
	}
	@Override
	public void endAction() {
		// if we're looking at stations panel and this crew is selected
		if (Home.getCurrentScreen() instanceof GameScreen) {
			CrewPane pane = CrewPane.containsMan(man);
			if (pane != null) {
				pane.updatePane();
			}
		}
		parent.endAction(this);
	}
}
