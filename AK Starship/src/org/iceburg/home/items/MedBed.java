package org.iceburg.home.items;

import org.iceburg.home.actors.CrewMan;

public class MedBed extends Item {
	// max crew health is the amount this bed can heal to (possibly increasing
	// crew health above their old health
	public int maxCrewHealth;

	public int getMaxCrewHealth() {
		return maxCrewHealth;
	}
	/**
	 * The max we can heal someone to
	 */
	@Override
	public int getEffectivenessLevel() {
		return (int) (getMaxCrewHealth() * getHealthPercent());
	}
	public void setMaxCrewHealth(int maxCrewHealth) {
		this.maxCrewHealth = maxCrewHealth;
	}
	/**
	 * if this bed has an occupant, heal them the specified amount (if the bed
	 * is fully powered) heals up to the max health (which is affected by how
	 * damaged the bed is)
	 */
	public void healOccupant(int amount) {
		CrewMan man = getParentTile().getMan();
		if (man != null && getPowerCurrent() == getMaxPower()) {
			int newHealth = man.getHealthCurrent() + amount;
			newHealth = Math.min(newHealth, getEffectivenessLevel());
			man.setHealthCurrent(newHealth);
		}
	}
}
