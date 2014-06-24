package org.iceburg.home.ai;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.ship.Tile;

public class TransportAction extends AIAction {
	private Tile targetTile, source;

	public TransportAction(AI parent, Tile targetTile, Tile source, CrewMan man) {
		super(parent);
		this.man = man;
		this.targetTile = targetTile;
		this.source = source;
	}
	public CrewMan getMan() {
		return man;
	}
	public void setMan(CrewMan man) {
		this.man = man;
	}
	@Override
	public String toString() {
		return "Transport " + man + " to " + targetTile.getName();
	}
	@Override
	public void startAction() {
		// Only transport the man if he is touching the target tube and has a
		// valid place to go
		if (targetTile != null && source.isTouching(man.getLocationTile())) {
			man.getLocationTile().setActor(null);
			targetTile.setActor(man);
			if (targetTile.getParentShip() != man.getParentShip()) {
				man.setParentShip(targetTile.getParentShip());
			}
		}
		endAction();
	}
	@Override
	public void updateAction() {
	}
	@Override
	public void endAction() {
		parent.endAction(this);
	}
}
