package org.iceburg.home.ai.priorities;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.ai.NPCAI;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.ship.Tile;

public class WeaponsPriority extends Priority {
	public WeaponsPriority(NPCAI p, Ship s) {
		super(p, s);
	}
	@Override
	public boolean isMet() {
		Tile tile = ship.getWeapons().getRandomMannableStation();
		if (tile != null){
			Tile target = tile.getWeapon().getTargetTile();
			if (tile.isPowered() && target != null && target.getHealth() > 0) {
				// old, is good for 1 weapon, but would like to man multiple
				// if (ship.getWeapons().isManned()){
				return true;
			}
		}
		return false;
	}
	@Override
	public void meetPriority(CrewMan man) {
		Tile tile = ship.getWeapons().getRandomMannableStation();
		man.getAi().addTravelAction(tile);
	}
	@Override
	public void clearMembers() {
		// ArrayList<CrewMan> list = ship.getWeapons().getManningCrew();
		// for (int i=0; i< list.size(); i++){
		// getCrew().remove(list.get(i));
		// }
	}
}
