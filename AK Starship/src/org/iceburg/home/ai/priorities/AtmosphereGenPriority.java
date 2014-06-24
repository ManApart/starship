package org.iceburg.home.ai.priorities;

import java.util.ArrayList;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.ai.NPCAI;
import org.iceburg.home.items.ACGenerator;
import org.iceburg.home.ship.Ship;

public class AtmosphereGenPriority extends Priority {
	public AtmosphereGenPriority(NPCAI p, Ship s) {
		super(p, s);
	}
	@Override
	public boolean isMet() {
		ArrayList<ACGenerator> list = ship.getLifeSupport().getGenerators();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getHealth() < 10) {
				return false;
			}
		}
		return true;
	}
	@Override
	public void meetPriority(CrewMan man) {
		ArrayList<ACGenerator> list = ship.getLifeSupport().getGenerators();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getHealth() < 10) {
				man.getAi().addRepairAction(list.get(i).getParentTile());
				return;
			}
		}
	}
	// TODO - update clear method?
	// @Override
	// public void clearMembers() {
	// int i = getCrew().size();
	// while ( i>0){
	// i -= 1;
	// if (getCrew().get(i).getAi().getCurrentAction() instanceof RepairAction
	// && getCrew().get(i).getCurrentTile().getHealth() <
	// getCrew().get(i).getCurrentTile().getHealthTotal()){
	// getCrew().remove(i);
	// }
	// }
	// }
}
