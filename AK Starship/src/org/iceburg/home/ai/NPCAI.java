package org.iceburg.home.ai;

import java.util.ArrayList;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.ai.priorities.AtmosphereGenPriority;
import org.iceburg.home.ai.priorities.HelmPriority;
import org.iceburg.home.ai.priorities.Priority;
import org.iceburg.home.ai.priorities.RepairGenPriority;
import org.iceburg.home.ai.priorities.ShieldsPriority;
import org.iceburg.home.ai.priorities.WeaponsPriority;
import org.iceburg.home.main.Home;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.ship.systems.HelmSystem;
import org.iceburg.home.ship.systems.LifeSupport;
import org.iceburg.home.ship.systems.Weapons;

//This is the AI of the opponent to the player who controls enemy crew
public class NPCAI {
	int timer;
	Ship parent;
	ArrayList<CrewMan> crew;
	// priorities
	AtmosphereGenPriority atmo;
	RepairGenPriority repair;
	WeaponsPriority weap;
	HelmPriority helm;
	ShieldsPriority shields;

	// sets our priorities, should be run every time the NPC takes charge of a
	// ship
	public NPCAI(Ship s) {
		this.parent = s;
		atmo = new AtmosphereGenPriority(this, s);
		repair = new RepairGenPriority(this, s);
		weap = new WeaponsPriority(this, s);
		helm = new HelmPriority(this, s);
		shields = new ShieldsPriority(this, s);
	}
	/**
	 * Updates the AI
	 */
	public void update() {
		if (Home.processAI) {
			// run once a second
			if (timer < 100) {
				timer += 1;
			} else {
				timer = 0;
				// System.out.println("NPCAI: Update AI");
				// clear in route status
				resetPriorities();
				// find our crew and re-evaluate in-route status
				findCrew();
				// only update if we have crew
				if (hasCrew()) {
					// System.out.println("NPCAI: Update AI Crew");
					checkPriority(atmo);
					checkPriority(helm);
					checkPriority(weap);
					checkPriority(shields);
					checkPriority(repair);
					// if we still have crew left over, send them on laps
					if (hasCrew()) {
						testLaps();
					}
				}
			}
		}
	}
	/**
	 * Check's this priority, clearing members that meet the priority, and
	 * asigning members if needbe
	 */
	public void checkPriority(Priority p) {
		p.clearMembers();
		if (hasCrew() && !p.isMet()) {
			p.meetPriority(getFirstMan());
			removeFirstMan();
		}
	}
	/**
	 * Resets all priorities to unmet
	 */
	public void resetPriorities() {
		atmo.setInRoute(false);
		repair.setInRoute(false);
		weap.setInRoute(false);
		helm.setInRoute(false);
	}
	/**
	 * Find all the crew we're responsible for, ignores traveling crew, but if
	 * they're traveling toward a priority, sets the priorty to inroute
	 */
	public void findCrew() {
		ArrayList<Ship> ships = Home.getBattleLoc().getShips();
		setCrew(new ArrayList<CrewMan>());
		for (int i = 0; i < ships.size(); i++) {
			for (int j = 0; j < ships.get(i).getCrew().size(); j++) {
				CrewMan man = ships.get(i).getCrew().get(j);
				// Crewman isn't player controlled
				if (!man.isPlayerControlledIgnoreCheat()) {
					// if walking see if he meets a priority and set that to
					// enroute
					if ((man.getAi().getCurrentAction() instanceof TravelAction)) {
						checkPrioritiesInRoute(man);
					}
					// otherwise add to our list
					else {
						getCrew().add(man);
					}
				}
			}
		}
	}
	/**
	 * Check's each priority to see if a crewman in route to this tile is in
	 * route to meeting a priority
	 */
	public void checkPrioritiesInRoute(CrewMan man) {
		Tile tile = ((TravelAction) man.getAi().getCurrentAction()).getGoal();
		if (tile.getTileColor().equals(LifeSupport.systemMain)) {
			atmo.setInRoute(true);
		} else if (tile.isTouching(parent.getFloorPlanAt(0).findLowestHealthTile())) {
			repair.setInRoute(true);
		} else if (tile.getTileColor().equals(Weapons.systemMain)
				|| tile.getTileColor().equals(Weapons.colorWeaponsMissile)) {
			weap.setInRoute(true);
		} else if (tile.getTileColor().equals(HelmSystem.systemMain)) {
			helm.setInRoute(true);
		}
		// else{
		// getCrew().add(man);
		// }
	}
	/**
	 * Return's wether we have have crew we're responsible for
	 */
	public boolean hasCrew() {
		return getCrew().size() > 0;
	}
	/**
	 * Test Function: Make the crewman go back and forth between helm and warp
	 */
	public void testLaps() {
		for (int i = 0; i < getCrew().size(); i++) {
			doLaps(getCrew().get(i));
		}
	}
	/**
	 * Test: Make the crewman go back and forth between helm and warp
	 */
	public void doLaps(CrewMan man) {
		// if man has no current ai or is manning something other than the Helm
		if ((man.getCurrentTile() == null || (man.getCurrentTile() != null && !man.getCurrentTile().getTileColor().equals(HelmSystem.systemMain)))) {
			Tile tile = man.getParentShip().getHelm().getRandomMannableStation();
			man.getAi().addTravelAction(tile);
		}
		// else if he's manning the helm, have him man warp
		if (man.getCurrentTile() != null
				&& man.getCurrentTile().getTileColor().equals(HelmSystem.systemMain)) {
			Tile tile = man.getParentShip().getWarp().getRandomMannableStation();
			man.getAi().addTravelAction(tile);
		}
	}
	public ArrayList<CrewMan> getCrew() {
		return crew;
	}
	public CrewMan getFirstMan() {
		if (getCrew().size() > 0) {
			return getCrew().get(0);
		} else
			return null;
	}
	public void removeFirstMan() {
		getCrew().remove(0);
	}
	public void setCrew(ArrayList<CrewMan> crew) {
		this.crew = crew;
	}
}
