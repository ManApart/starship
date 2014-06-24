package org.iceburg.home.items;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.main.Home;
import org.iceburg.home.ship.systems.Sensors;

public class Sensor extends Item {
	private int level;
	@Override
	public void setCurrentPower(int currentPower) {
		// update draw image
		if (getPowerCurrent() != currentPower) {
			updateImage();
		}
		// update system power on/off if needed
		if (getPowerCurrent() == 0 && currentPower > 0) {
			getParentTile().getParentSystem().powerSystemOn(getParentTile());
		} else if (getPowerCurrent() > 0 && currentPower == 0) {
			getParentTile().getParentSystem().powerSystemOff(getParentTile());
		}
		// update crewman info if we're crossing over the crewHealth threshold
		boolean updateCrew = false;
		if ((getEffectivenessLevel() >= Sensors.crewHealth && getEffectivenessLevelAtPower(currentPower) < Sensors.crewHealth)
				|| (getEffectivenessLevel() < Sensors.crewHealth && getEffectivenessLevelAtPower(currentPower) >= Sensors.crewHealth)) {
			// but only if the crewman is an enemy
			CrewMan man = Home.getCurrentScreen().getCurrentCrewMan();
			if (!man.isPlayerControlled()) {
				updateCrew = true;
				// System.out.println("Update");
			}
		}
		this.currentPower = currentPower;
		if (updateCrew) {
			Home.getCurrentScreen().getCurrentCrewPane().updatePane();
		}
	}
	@Override
	public void setHealth(int health) {
		super.setHealth(health);
		if (getHealth() != health) {
			updateImage();
		}
	}
	/**
	 * Updates the viewscreen image if we need to (Because our sensors have
	 * changed effectiveness and we're not looking at our own ship)
	 */
	private void updateImage() {
		// if this sensor belongs to the player ship, and we're not looking at
		// player ship
		if (getParentTile().getParentShip() == Home.getShip()
				&& Home.getShip() != Home.getCurrentScreen().getCurrentShip()) {
			Home.getCurrentScreen().updateCurrentFloorPlanImage();
		}
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	
	@Override
	public int getEffectivenessLevel() {
		return (int) (getPowerCurrent() * getLevel()* getHealthPercent());
	}
	/**
	 * What would our effectiveness level be if we were at input power?
	 */
	public int getEffectivenessLevelAtPower(int power) {
		return (int) (power * getLevel()* getHealthPercent());
	}
	
}
