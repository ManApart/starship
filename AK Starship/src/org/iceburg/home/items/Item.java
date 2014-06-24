package org.iceburg.home.items;

import java.awt.Color;
import java.awt.Image;
import java.util.ArrayList;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.data.Data;
import org.iceburg.home.main.StaticFunctions;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.ship.systems.Cabins;
import org.iceburg.home.ship.systems.CargoHold;
import org.iceburg.home.ship.systems.Doors;
import org.iceburg.home.ship.systems.HelmSystem;
import org.iceburg.home.ship.systems.LifeSupport;
import org.iceburg.home.ship.systems.MedBay;
import org.iceburg.home.ship.systems.Sensors;
import org.iceburg.home.ship.systems.Shields;
import org.iceburg.home.ship.systems.Warp;
import org.iceburg.home.ship.systems.Weapons;
import org.iceburg.home.sound.Sound;
import org.iceburg.home.ui.TilePane;
import org.iceburg.home.ui.UIMisc;

public class Item {
	String id, name, description, imagePath;
	Color itemType;
	// current power will equal 0 or powerReq, unless it's part of a system
	// where power can be adjusted
	int health, healthTotal, maxPower, currentPower;
	Image image;
	Tile parentTile;
	private static Sound breakSound = new Sound("fx/break.wav");

	public Item() {
	}
	@Override
	public String toString() {
		return name;
	}
	// getters and setters
	public Item findItemByID(String id) {
		ArrayList<Item> list = Data.getItems().items;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getId().equals(id)) {
				return list.get(i);
			}
		}
		return null;
	}
	/**
	 * Return's true if both items have the same base item (the same id)
	 */
	public boolean isSameBaseItem(Item other) {
		if (other == null){
//			System.out.println("");
			return false;
		}
		return (getId().equals(other.getId()));
	}
	/**
	 * Makes a new item that is a clone of the origional item (paramater item)
	 * 
	 * @param origional
	 */
	public static Item cloneItem(Item origional) {
		// clone unique stuff
		if (origional.getItemType().equals(Weapons.colorWeaponsMissile)
				|| origional.getItemType().equals(Weapons.systemMain)) {
			return cloneWeapon((Weapon) origional);
		} else if (origional.getItemType().equals(Doors.systemMain)) {
			return cloneDoor((Door) origional);
		} else if (origional.getItemType().equals(Shields.systemMain)) {
			return cloneShield((Shield) origional);
		} else if (origional.getItemType().equals(MedBay.colorMedBed)) {
			return cloneMedBed((MedBed) origional);
		} else if (origional.getItemType().equals(MedBay.systemMain)) {
			return cloneMedConsole((MedConsole) origional);
		} else if (origional.getItemType().equals(HelmSystem.systemMain)) {
			return cloneHelm((HelmItem) origional);
		} else if (origional.getItemType().equals(Warp.systemMain)) {
			return cloneWarpGen((WarpGenerator) origional);
		} else if (origional.getItemType().equals(LifeSupport.systemMain)) {
			return cloneAC((ACGenerator) origional);
		} else if (origional.getItemType().equals(LifeSupport.colorACVent)) {
			return cloneACVent((ACVent) origional);
		} else if (origional.getItemType().equals(Cabins.systemMain)) {
			return cloneCabin((Cabin) origional);
		} else if (origional.getItemType().equals(CargoHold.systemMain)) {
			return cloneCargo((CargoBay) origional);
		} else if (origional.getItemType().equals(Sensors.systemMain)) {
			return cloneSensor((Sensor) origional);
		} else {
			Item item = new Item();
			item.cloneItemBase(origional);
			return item;
		}
	}
	public static Item cloneWeapon(Weapon origional) {
		Weapon w = new Weapon();
		w.cloneItemBase(origional);
		w.setMaxArc(origional.getMaxArc());
		w.setDamage(origional.getDamage());
		w.setStartArc(90 - w.getMaxArc() / 2);
		if (w.getItemType().equals(Weapons.systemMain)) {
			w.setFrequency(StaticFunctions.randRange(0, 5));
			// w.setFrequency(origional.getFrequency());
		} else {
			w.setAOE(origional.getAOE());
			w.setReloadTime(origional.getReloadTime());
		}
		return w;
	}
	public static Item cloneShield(Shield origional) {
		Shield s = new Shield();
		s.cloneItemBase(origional);
		s.setAOE(origional.getAOE());
		s.setFrequency(StaticFunctions.randRange(0, 5));
		// s.setFrequency(origional.getFrequency());
		return s;
	}
	public static Item cloneMedBed(MedBed origional) {
		MedBed s = new MedBed();
		s.cloneItemBase(origional);
		s.setMaxCrewHealth(origional.getMaxCrewHealth());
		return s;
	}
	public static Item cloneMedConsole(MedConsole origional) {
		MedConsole s = new MedConsole();
		s.cloneItemBase(origional);
		s.setHealingRate(origional.getHealingRate());
		return s;
	}
	public static Item cloneHelm(HelmItem origional) {
		HelmItem s = new HelmItem();
		s.cloneItemBase(origional);
		s.setTurnSpeed(origional.getTurnSpeed());
		return s;
	}
	public static Item cloneWarpGen(WarpGenerator origional) {
		WarpGenerator s = new WarpGenerator();
		s.cloneItemBase(origional);
		s.setSpeed(origional.getSpeed());
		return s;
	}
	public static Item cloneAC(ACGenerator origional) {
		ACGenerator s = new ACGenerator();
		s.cloneItemBase(origional);
		s.setCapacity(origional.getCapacity());
		return s;
	}
	public static Item cloneACVent(ACVent origional) {
		ACVent s = new ACVent();
		s.cloneItemBase(origional);
		s.setDrainRate(origional.getDrainRate());
		return s;
	}
	public static Item cloneDoor(Door origional) {
		Door s = new Door();
		s.cloneItemBase(origional);
		s.setOpenImagePath(origional.getOpenImagePath());
		return s;
	}
	public static Item cloneCabin(Cabin origional) {
		Cabin s = new Cabin();
		s.cloneItemBase(origional);
		s.setMaxCrew(origional.getMaxCrew());
		return s;
	}
	public static Item cloneCargo(CargoBay origional) {
		CargoBay s = new CargoBay();
		s.cloneItemBase(origional);
		s.setCapacity(origional.getCapacity());
		return s;
	}
	public static Item cloneSensor(Sensor origional) {
		Sensor s = new Sensor();
		s.cloneItemBase(origional);
		s.setLevel(origional.getLevel());
		return s;
	}
	/**
	 * Makes this item a clone of the origional item (paramater item)
	 * 
	 * @param origional
	 */
	public void cloneItemBase(Item origional) {
		if (origional == null) {
			System.out.println("");
		}
		this.id = origional.getId();
		this.name = origional.getName();
		this.description = origional.getDescription();
		this.itemType = origional.getItemType();
		// this.techLevel = origional.getTechLevel();
		this.health = origional.getHealth();
		this.healthTotal = origional.getHealthTotal();
		this.maxPower = origional.getMaxPower();
		this.imagePath = origional.getImagePath();
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Color getItemType() {
		return itemType;
	}
	public void setItemType(Color itemType) {
		this.itemType = itemType;
	}
	// public int getTechLevel() {
	// return techLevel;
	// }
	// public void setTechLevel(int techLevel) {
	// this.techLevel = techLevel;
	// }
	public int getHealth() {
		return health;
	}
	// TODO - probably want to trigger an event here that update's the station's
	// status?
	public void setHealth(int health) {
		if (health < 0) {
			health = 0;
		}
		// if we're going from blocked to unblocked, update rooms
		if ((health <= 0 && getHealth() > 0) || (health > 0 && getHealth() <= 0)) {
			this.health = health;
			if (getParentTile() != null && getParentTile().getParentShip() != null) {
				getParentTile().getParentShip().updateShipRooms(getParentTile());
			}
		} else {
			this.health = health;
		}
		// play the dying sound; unpower the system
		if (health == 0) {
			breakSound.play();
			setCurrentPower(0);
		}
		// if we're looking at stations panel and this tile is selected
		TilePane pane = TilePane.containsTile(getParentTile());
		if (pane != null) {
			if (pane.getTile() == getParentTile()) {
				pane.updateProgressBar(UIMisc.health, getParentTile());
			} else {
				pane.updateProgressBar(UIMisc.hit, pane.getTile().getWeapon().getTargetTile());
			}
			
		}

	}
	/**
	 * @return the % of health this item has <br>
	 *         Ex: item has 90/100 health, returns 0.9
	 */
	public double getHealthPercent() {
		return ((double) getHealth()) / getHealthTotal();
	}
	/**
	 * @return the % of health this item has * 10 <br>
	 *         Ex: item has 90/100 health, returns 9
	 */
	public int getHealthPercent10() {
		return (int) (getHealthPercent() * 10);
	}
	/**
	 * @return the % of health this item has * 100 <br>
	 *         Ex: item has 90/100 health, returns 90
	 */
	public int getHealthPercent100() {
		return (int) (getHealthPercent() * 100);
	}
	/**
	 * @return the % this item is damaged <br>
	 *         Ex: item has 90/100 health, returns 0.1
	 */
	public double getDamagePercent() {
		// double i= 1-getHealthPercent();
		return 1 - getHealthPercent();
	}
	/**
	 * @return the % this item is damaged, in ints by 10 <br>
	 *         Ex: item has 90/100 health, returns 1
	 */
	public int getDamagePercent10() {
		// int i= (int) Math.floor(getDamagePercent()*10);
		return (int) Math.floor(getDamagePercent() * 10);
	}
	/**
	 * @return the % this item is damaged, in ints by 100 <br>
	 *         Ex: item has 90/100 health, returns 10
	 */
	public int getDamagePercent100() {
		// int i= (int) Math.floor(getDamagePercent()*100);
		return (int) Math.floor(getDamagePercent() * 100);
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getMaxPower() {
		return maxPower;
	}
	public void setMaxPower(int powerReq) {
		this.maxPower = powerReq;
	}
	public int getHealthTotal() {
		return healthTotal;
	}
	public void setHealthTotal(int healthTotal) {
		this.healthTotal = healthTotal;
	}
	public String getImagePath() {
		return imagePath;
	}
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	public int getPowerCurrent() {
		return currentPower;
	}
	/**
	 * 
	 * @return The current effectiveness rate (usually power level, less percent
	 *         damaged, + Manning Man's skill level) May be ovverriden by items
	 */
	public int getEffectivenessLevel() {
		return (int) (getPowerCurrent() * getHealthPercent() + getManningLvl());
	}
	/**
	 * Return the skill of the crewman manning this station.
	 * @return
	 */
	public int getManningLvl(){
		if (getParentTile().isManned()){
			CrewMan man = getParentTile().getMan();
			return man.getSkillFor(getItemType());
		}
		return 0;
	}
	public boolean isPowered() {
		return getPowerCurrent() > 0;
	}
	public void setCurrentPower(int currentPower) {
		// can't power on a broken system
		if (getHealth() <= 0) {
			currentPower = 0;
		}
		if (getPowerCurrent() == 0 && currentPower > 0) {
			this.currentPower = currentPower;
			getParentTile().getParentSystem().powerSystemOn(getParentTile());
		} else if (getPowerCurrent() > 0 && currentPower == 0) {
			this.currentPower = currentPower;
			getParentTile().getParentSystem().powerSystemOff(getParentTile());
		} else {
			this.currentPower = currentPower;
		}
	}
	public void bumpCurrentPower(int amount) {
		setCurrentPower(getPowerCurrent() + amount);
	}
	public Image getImage() {
		return image;
	}
	public void setImage(Image image) {
		this.image = image;
	}
	public Tile getParentTile() {
		return parentTile;
	}
	public void setParentTile(Tile parentTile) {
		this.parentTile = parentTile;
	}
	public boolean isFloor() {
		return getItemType().equals(Color.decode("#ffffff"));
	}
	/**
	 * Finds and updates the pane that contains this item's parent tile
	 */
	public void updatePane(){
		if (getParentTile() != null){
			getParentTile().updatePane();
		}
	}
}
