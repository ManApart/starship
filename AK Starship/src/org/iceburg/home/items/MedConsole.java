package org.iceburg.home.items;

public class MedConsole extends Item {
	private int healingRate;

	int getHealingRate() {
		return healingRate;
	}
	public void setHealingRate(int healingRate) {
		this.healingRate = healingRate;
	}
	/**
	 * Return's the healing rate based on power level and damage
	 * 
	 * @return
	 */
	@Override
	public int getEffectivenessLevel() {
		return (int) (healingRate * getPowerCurrent() * getHealthPercent());
	}
}
