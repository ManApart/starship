package org.iceburg.home.items;

public class HelmItem extends Item {
	int turnSpeed;

	public int getTurnSpeed() {
		return turnSpeed;
	}
	public void setTurnSpeed(int turnSpeed) {
		this.turnSpeed = turnSpeed;
	}
	/**
	 * Return the current turn speed, based on damage, power level, and item
	 * turn speed
	 */
	@Override
	public int getEffectivenessLevel() {
		return (int) (getTurnSpeed() * getPowerCurrent() - getDamagePercent100());
	}
}
