package org.iceburg.home.ai.priorities;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.ai.NPCAI;
import org.iceburg.home.ai.RepairAction;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.ship.Tile;

public class RepairGenPriority extends Priority {
	public RepairGenPriority(NPCAI p, Ship s) {
		super(p, s);
	}
	@Override
	public boolean isMet() {
		if (isInRoute()) {
			return true;
		}
		Tile temp = ship.getFloorPlanAt(0).findLowestHealthTile();
		if (temp == null) {
			return true;
		}
		// remove our repairman from the list
		else if (temp.isManned()) {
			parent.getCrew().remove(temp.getMan());
			return true;
		}
		return false;
	}
	@Override
	public void meetPriority(CrewMan man) {
		// if not already repairing something
		// if ((man.getAi().getCurrentAction() instanceof RepairAction) &&
		// man.getCurrentTile().getHealth() <
		// man.getCurrentTile().getHealthTotal()){
		//
		// }else{
		Tile tile = ship.getFloorPlanAt(0).findLowestHealthTile();
		man.getAi().addRepairAction(tile);
		// }
	}
	@Override
	public void clearMembers() {
		int i = getCrew().size();
		while (i > 0) {
			i -= 1;
			if (getCrew().get(i).getAi().getCurrentAction() instanceof RepairAction){
				Tile tile = getCrew().get(i).getCurrentTile();
				if( tile != null && tile.getHealth() <tile.getHealthTotal()) {
					getCrew().remove(i);
				}
			}
		}
	}
}
