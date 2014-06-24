package org.iceburg.home.ship.systems;

import java.awt.Color;
import java.util.ArrayList;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.ai.HealAction;
import org.iceburg.home.items.Item;
import org.iceburg.home.items.MedBed;
import org.iceburg.home.items.MedConsole;
import org.iceburg.home.ship.Ship;

//when biobeds are manned, the manning crew is healed by the crewman standing next to the bed
public class MedBay extends ShipSystem {
	public static Color systemMain = Color.decode("#700000");
	public static Color colorMedBed = Color.decode("#ff0000");

	public MedBay(Ship ship) {
		super(ship);
		this.tileTypes.add(systemMain);
		this.tileTypes.add(colorMedBed);
		this.name = "Med Bay";
	}
	@Override
	public void manSystem(CrewMan man) {
		// if the crewman is manning a console, give him the healing action
		if (man.getCurrentTile().getTileColor().equals(systemMain)) {
			man.clearAI();
			man.addAIAction(new HealAction(man, man.getAi(), man.getCurrentTile()));
		}
	}
	/**
	 * This is run from the Heal AI action, and runs more often based on the
	 * doctor's heal level
	 */
	public void healCrew(CrewMan doctor, HealAction ai) {
		// get the amount we can heal each tick
		if (doctor.getCurrentTile().getItem() instanceof MedConsole) {
			int amount = ((MedConsole) doctor.getCurrentTile().getItem()).getEffectivenessLevel();
			// get the list of crewman to heal (who are manning bio beds)
			ArrayList<Item> list = getSystemItemsRandom(colorMedBed);
			if (list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					((MedBed) list.get(i)).healOccupant(amount);
				}
			}
		}
		// doc no longer manning a console
		else {
			ai.endAction();
		}
	}
}
