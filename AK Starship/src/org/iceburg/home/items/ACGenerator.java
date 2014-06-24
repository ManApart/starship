package org.iceburg.home.items;

import org.iceburg.home.ship.systems.LifeSupport;

public class ACGenerator extends Item {
	public int capacity, currentStock;

	// @Override
	// public String toString(){
	// return getName() + ", Air: " +getCurrentStock() + "/" +
	// getCurrentCapacity();
	// }
	public void updateGen() {
		int produced = 0;
		// produce air if powered
		if (getParentTile().isPowered()) {
			produced = getEffectivenessLevel();
		}
		// add the air to this generator's stock
		incCurrentStock(produced);
		// account for stock that exceeds capacity
		if (getCurrentStock() > getCurrentCapacity()) {
			setCurrentStock(getCurrentCapacity());
		} else if (getCurrentStock() < 0 || getCurrentCapacity() <= 0) {
			setCurrentStock(0);
		}
		// update system
		incSystemCapacity(getCurrentCapacity());
		incSystemAir(getCurrentStock());
	}
	/**
	 * Returns the max amount of air produced each update
	 */
	@Override
	public int getEffectivenessLevel() {
		return getPowerCurrent() * 1;
	}
	/**
	 * 
	 */
	public int getCurrentCapacity() {
		return (int) (getCapacity() * getHealthPercent());
	}
	public int getCapacity() {
		return capacity;
	}
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	public int getCurrentStock() {
		return currentStock;
	}
	public void setCurrentStock(int currentStock) {
		this.currentStock = currentStock;
	}
	public void incCurrentStock(int amount) {
		this.setCurrentStock(amount + currentStock);
	}
	/**
	 * add's this generator's air to the system's air
	 */
	public void incSystemAir(int amount) {
		((LifeSupport) getParentTile().getParentSystem()).incAirCurrent(amount);
	}
	/**
	 * add's this generator's capacity to the system's capacity
	 */
	public void incSystemCapacity(int amount) {
		((LifeSupport) getParentTile().getParentSystem()).incAirCapacity(amount);
	}
}
