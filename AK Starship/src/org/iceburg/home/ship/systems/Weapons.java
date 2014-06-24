package org.iceburg.home.ship.systems;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.actors.Laser;
import org.iceburg.home.actors.Missile;
import org.iceburg.home.items.Item;
import org.iceburg.home.items.Weapon;
import org.iceburg.home.main.Home;
import org.iceburg.home.main.StaticFunctions;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.ui.TilePane;
import org.iceburg.home.ui.UIMisc;

public class Weapons extends ShipSystem {
	// Weapons, the main color, is also the beam weapon
	public static Color systemMain = Color.decode("#ff3238");
	public static Color colorWeaponsMissile = Color.decode("#ff5c38");
	// public Ship targetShip;
	// TODO - currentfloor?
	int currentFloor = 0;
	private JButton btnFire, btnAutoFire, btnClearTarget;
	// Use spinner
	JSpinner jtaRotate;
	JSlider jsFrequency;

	public Weapons(Ship ship) {
		super(ship);
		this.tileTypes.add(systemMain);
		this.tileTypes.add(colorWeaponsMissile);
		this.name = "Weapons";
	}
	@Override
	public void paintTileInfo(TilePane p, Item item) {
		Weapon w = (Weapon) item;
		int sLevel = getParentShip().getSensors().getMaxSensorLevel();
		UIMisc.addJLabel("" + w.printFiring(), p);
		// UIMisc.addJLabel("Max arc: " + w.getMaxArc(), p);
		UIMisc.addJLabel("Damage: " + w.getDamage(), p);
		if (w.getItemType().equals(Weapons.systemMain)) {
			if (isManned() && !item.isPowered()) {
				jsFrequency = UIMisc.addSlider("Frequency:", p, 5, w.getFrequency());
			} else if (sLevel >= Sensors.shieldFreq) {
				UIMisc.addJLabel("Frequency: " + w.getFrequency(), p);
			}
		} else {
			UIMisc.addJLabel("Area of Effect: " + w.getAOE(), p);
		}
		String s = "Unknown";
		if (w.getTargetTile() != null && w.getTargetTile().getParentShip().getSensors().getMaxSensorLevel() > Sensors.systemName) {
			s = w.getTargetTile().getName();
		}

		UIMisc.addJLabel("Target Tile: " + s, p);
		
		UIMisc.addTileHealthBar(w.getTargetTile(), "Hit: ", p);
		if (Home.creativeMode
				|| (item.getParentTile().isManned() && item.getHealth() <= 0)) {
			jtaRotate = UIMisc.addSpinner("Weapon At: " + w.getArc()
					+ "\u00B0 \n Set to:", w.getArc(), p);
		} else {
			UIMisc.addJLabel("Weapon At: " + w.getArc() + "\u00B0", p);
		}
		if (w.isMissile()) {
			UIMisc.addLoadBar(w, UIMisc.loadTime, p);
		}
	}
	@Override
	public void paintTileMultiInfo(TilePane p, Item item) {
		Weapon w = (Weapon) item;
		String s = "Empty";
		if (w.getTargetTile() != null) {
			s = w.getTargetTile().getName();
		}
		UIMisc.addJLabel("Target Tile: " + s, p);
		UIMisc.addTileHealthBar(w.getTargetTile(), "Hit: ", p);
	}
	@Override
	public ArrayList<JButton> systemOptionOverride(ActionListener comp, Tile tile) {
		ArrayList<JButton> list = new ArrayList<JButton>();
		String s = "";
		// TODO - if isfiring (has projectile) change button to stop (unless is
		// missile and auto is off)
		s = "Fire";
		if (tile.getWeapon().isFiring()) {
			s = "Cease Fire";
		}
		btnFire = UIMisc.createJButton(s, comp);
		// online and within arc, and not firing
		if (Home.creativeMode || (tile != null && (tile.isPowered() && isManned()))) {
			list.add(btnFire);
		}
		btnClearTarget = UIMisc.createJButton("Clear Target", comp);
		if (Home.creativeMode
				|| (tile != null && tile.isPowered() && isManned() && tile.getWeapon().getTargetTile() != null)) {
			list.add(btnClearTarget);
		}
		btnAutoFire = UIMisc.createJButton("AutoFire", comp);
		if (isManned() && tile.getWeapon().isMissile()
				&& !btnFire.getText().equals("Cease Fire")) {
			list.add(btnAutoFire);
		}
		return list;
	}
	@Override
	public void getButtonPress(JButton button, Tile source) {
		Weapon w = source.getWeapon();
		// System.out.println("weapons button pressed");
		// do a text match as the tilepane and popup versions of this button may
		// become desynced
		if (button.getText().equals(btnFire.getText())) {
			// System.out.println("fire");
			// if isfiring, we want to cease it
			if (w.isFiring()) {
				w.setFiring(false);
				w.ceaseFire();
			} else {
				fireWeapon(source);
			}
		} else if (button.getText().equals(btnAutoFire.getText())) {
			w.setFiring(!w.isFiring());
		} else if (button.getText().equals(btnClearTarget.getText())) {
			w.setTargetTile(null);
		}
	}
	@Override
	public void stateChanged(JSpinner o, Tile tile) {
		// System.out.println(getName() + " recieved Spinner event: " +
		// o.getValue());
		int i = StaticFunctions.within360((int) o.getValue());
		tile.getWeapon().setStartArcByMid(i);
		o.setValue(i);
	}
	/**
	 * Equipping a weapon calculates it's weapon arcs etc.
	 */
	@Override
	public void installItem(Tile tile) {
		tile.setParentSystem(this);
		addAccessPoint(tile);
		if (tile.getItem() != null) {
			Weapon w = ((Weapon) tile.getItem());
			// w.setParentSystem(this);
			w.setStartArc(90 - w.getMaxArc() / 2);
			w.findWeaponArc();
		}
	}
	@Override
	public void powerSystemOn(Tile station) {
		Weapon w = station.getWeapon();
		if (w.getTargetShip() != null && w.getTargetShip().getWarp() != null) {
			station.getWeapon().evaluateWeaponArc();
			w.addArcOverlay();
			// System.out.println("Weapons: Target");
		} else {
			w.findWeaponArc();
			System.out.println("Weapons: No Target");
		}
	}
	@Override
	public void powerSystemOff(Tile station) {
		if (Home.getCurrentScreen() != null
				&& station == Home.getCurrentScreen().getCurrentTile()) {
			Home.getCurrentScreen().getCurrentTilePane().updatePane();
		}
		station.getWeapon().removeArcOverlay();
		powerDownSound.play();
	}
	@Override
	public void updateSystem() {
		// update all of the projectiles
		for (int i = 0; i < getStations().size(); i++) {
			getStations().get(i).getWeapon().updateProjectiles();
		}
		if (timer < 30) {
			timer += 1;
		} else {
			timer = 0;
			// update all of the weapons
			for (int i = 0; i < getStations().size(); i++) {
				getStations().get(i).getWeapon().update();
			}
		}
	}
	@Override
	public void stateChanged(JSlider o, Tile tile) {
		if (o == jsFrequency) {
			// System.out.println("slide value: " + slide.getValue());
			tile.getWeapon().setFrequency(o.getValue());
			o.setValue(tile.getWeapon().getFrequency());
		}
	}
	@Override
	public void manSystemAuto(CrewMan man) {
		Tile tile = man.getCurrentTile();
		if (tile.isPowered()) {
			Weapon w = tile.getWeapon();
			if (!w.isFiring()) {
				fireWeapon(tile);
				w.setFiring(true);
			}
			// if target is null/ broken, set one at random
			else if (w.getTargetTile() == null || w.getTargetTile().getHealth() == 0) {
				w.setTargetTile(w.getTargetShip().getRandomFloorPlan().getRandomShipTile());
			}
		} else if (tile.getHealth() > 10) {
			tile.getParentShip().getEngineering().attemptPowerSystem(tile.getItem());
		}
	}
	/**
	 * Sets every weapon (that doesn't already have a target to target this new
	 * ship
	 */
	public void setWeaponTargets(Ship target) {
		ArrayList<Weapon> list = getAllWeapons();
		for (int i = 0; i < list.size(); i++) {
			if (!list.get(i).hasTargetShip()) {
				list.get(i).setTargetShip(target);
			}
		}
	}
	public ArrayList<Weapon> getAllWeapons() {
		ArrayList<Weapon> ret = new ArrayList<Weapon>();
		ArrayList<Item> list = getSystemItems();
		for (int i = 0; i < list.size(); i++) {
			ret.add((Weapon) list.get(i));
		}
		return ret;
	}
	public ArrayList<Weapon> getBeamWeapons() {
		ArrayList<Weapon> ret = new ArrayList<Weapon>();
		ArrayList<Item> list = getSystemItems();
		for (int i = 0; i < list.size(); i++) {
			Weapon w = (Weapon) list.get(i);
			if (w.getItemType().equals(Weapons.systemMain)) {
				ret.add(w);
			}
		}
		return ret;
	}
	public ArrayList<Weapon> getMissileWeapons() {
		ArrayList<Weapon> ret = new ArrayList<Weapon>();
		ArrayList<Item> list = getSystemItems();
		for (int i = 0; i < list.size(); i++) {
			Weapon w = (Weapon) list.get(i);
			if (w.getItemType().equals(Weapons.colorWeaponsMissile)) {
				ret.add(w);
			}
		}
		return ret;
	}
	public Weapon getFirstWeapon() {
		return getAllWeapons().get(0);
	}
	public void evaluateWeaponArcs() {
		// Home.messagePanel.addMessage("Evaluating Arcs");
		ArrayList<Weapon> stations = getAllWeapons();
		for (int i = 0; i < stations.size(); i++) {
			stations.get(i).evaluateWeaponArc();
		}
		// Home.messagePanel.addMessage("Evaluated Arcs");
	}
	/**
	 * Spawns a projectile aimed at the target
	 */
	public void fireWeapon(Tile source) {
		// check to see if we 'can' fire the weapon:
		Weapon weapon = source.getWeapon();
		if (weapon.isWithinArc() && weapon.isPowered()) {
			Tile target = weapon.getTargetTile();
			target = calcAccuracy(target);
			if (weapon.isLaser()) {
				weapon.setFiring(true);
				Laser l = new Laser(source);
				weapon.addProjectile(l);
				getParentShip().getProjectiles().add(l);
				weapon.updateEdgeTile();
				// else missile
			} else {
				if (weapon.getReloadProgress() < weapon.getTotalReloadTime()) {
					weapon.setReloading(true);
				} else {
					weapon.setReloading(false);
					weapon.setReloadProgress(0);
					Missile m = new Missile(source);
					weapon.addProjectile(m);
					getParentShip().getProjectiles().add(m);
				}
			}
		}
	}
	/**
	 * Averages all the weapon arcs that are powered and returns that average
	 * angle Meant to get the angle where the ship has the most weapons pointed
	 */
	public int getBestWeaponArc() {
		// get all the arcs
		ArrayList<Point> arcList = new ArrayList<Point>();
		for (int i = 0; i < getAllWeapons().size(); i++) {
			if (getAllWeapons().get(i).isPowered()) {
				arcList.addAll(getAllWeapons().get(i).getArcs());
			}
		}
		if (arcList.size() > 0) {
			// calc average
			int x = 0;
			int y = 0;
			for (int i = 0; i < arcList.size(); i++) {
				x += arcList.get(i).x;
				y += arcList.get(i).y;
			}
			x = x / arcList.size();
			y = y / arcList.size();
			// System.out.println("Weapons: Avg Arc: " + (x+y)/2);
			return (x + y) / 2;
		} else
			return 90;
	}
	/**
	 * Adjusts the target tile based on accuracy factors (weapons, crew skill,
	 * etc)
	 */
	// TODO
	public Tile calcAccuracy(Tile target) {
		return target;
	}
	public void setTargeted() {
	}
	public int getCurrentFloor() {
		return currentFloor;
	}
	public void setCurrentFloor(int currentFloor) {
		this.currentFloor = currentFloor;
	}
	public Sensors getSensors(){
		return getParentShip().getSensors();
		
	}
}
