package org.iceburg.home.ai.priorities;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.ai.NPCAI;
import org.iceburg.home.ship.Ship;

public class ShieldsPriority extends Priority {
	public ShieldsPriority(NPCAI p, Ship s) {
		super(p, s);
	}
	@Override
	public boolean isMet() {
		// Tile tile = ship.getShields().getRandomMannableStation();
		// if (tile.isPowered()){
		// return true;
		// }
		// return false;
		for (int i = 0; i < ship.getShields().getStations().size(); i++) {
			if (!ship.getShields().getStations().get(i).isPowered()) {
				return false;
			}
		}
		return true;
	}
	@Override
	public void meetPriority(CrewMan man) {
		for (int i = 0; i < ship.getShields().getStations().size(); i++) {
			if (!ship.getShields().getStations().get(i).isPowered()) {
				man.getAi().addTravelAction(ship.getShields().getStations().get(i));
				return;
			}
		}
	}
	@Override
	public void clearMembers() {
		// ArrayList<CrewMan> list = ship.getWeapons().getManningCrew();
		// for (int i=0; i< list.size(); i++){
		// getCrew().remove(list.get(i));
		// }
	}
}
