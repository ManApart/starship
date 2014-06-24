package org.iceburg.home.ai.priorities;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.ai.NPCAI;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.ship.Tile;

public class HelmPriority extends Priority {
	public HelmPriority(NPCAI p, Ship s) {
		super(p, s);
	}
	@Override
	public boolean isMet() {
		if (!ship.getHelm().isManned()) {
			// find direction of best arc
			int a = ship.getWeapons().getBestWeaponArc();
			int d = ship.getHelm().angleToDirection(a);
			// compare the direction of best arc with actual ship direction
			int shipD = ship.getHelm().angleToDirection(ship.getHelm().getAngleToTarget());
			// if they don't match, send someone to the helm
			if (d != shipD) {
				return false;
			}
		}
		return true;
	}
	@Override
	public void meetPriority(CrewMan man) {
		Tile tile = ship.getHelm().getRandomMannableStation();
		man.getAi().addTravelAction(tile);
	}
	@Override
	public void clearMembers() {
		getCrew().remove(ship.getHelm().getFirstManningCrew());
	}
}
