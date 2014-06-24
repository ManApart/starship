package org.iceburg.home.ship.systems;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.items.Item;
import org.iceburg.home.main.Home;
import org.iceburg.home.main.StaticFunctions;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.sound.Sound;
import org.iceburg.home.ui.GameScreen;
import org.iceburg.home.ui.PopUpBox;
import org.iceburg.home.ui.TilePane;

public abstract class ShipSystem {
	public static Color systemMain;
	Ship parentShip;
	ArrayList<Color> tileTypes;
	String name;
	ArrayList<Tile> stations;
	static Sound powerUpSound = new Sound("fx/power_up.wav");
	static Sound powerDownSound = new Sound("fx/power_down.wav");
	// used for updates
	int timer;

	public ShipSystem(Ship ship) {
		this.parentShip = ship;
		this.tileTypes = new ArrayList<Color>();
		this.stations = new ArrayList<Tile>();
	}
	@Override
	public String toString() {
		return name;
	}
	/**
	 * Get's overRidden by the system to provide contextual tile info
	 */
	public void paintTileInfo(TilePane p, Item item) {
	}
	/**
	 * Get's overRidden by the system to provide contextual tile info
	 */
	public void paintTileMultiInfo(TilePane p, Item item) {
	}
	/**
	 * Get's overRidden by the system to provide contextual tile info
	 */
	public void stateChanged(JSlider o, Tile tile) {
		System.out.println(getName() + " recieved slider event");
	}
	/**
	 * Get's overRidden by the system to provide contextual tile info
	 */
	public void stateChanged(JSpinner o, Tile tile) {
		System.out.println(getName() + " recieved Spinner event");
	}
	/**
	 * Get's overRidden by the system to provide contextual tile info
	 */
	public void textChanged(JTextField o, Tile tile) {
		System.out.println(getName() + " recieved TextField event");
	}
	/**
	 * Get's overRidden by the system. Used to have crew intellegently manage
	 * whatever system they're manning
	 */
	public void manSystemAuto(CrewMan man) {
		// System.out.println("ShipSystem: " + getName() +
		// " being auto manned");
	}
	/**
	 * Return the highest effectiveness level of all of this system's stations.
	 */
	public int getHighestEffectivenessLevel() {
		int max = 0;
		for (int i = 0; i < getStations().size(); i++) {
			int temp = getStations().get(i).getItem().getEffectivenessLevel();
			if (temp > max) {
				max = temp;
			}
		}
		return max;
	}
	/**
	 * Return the total effectiveness level (all of this system's stations
	 * combined).
	 */
	public int getTotalEffectivenessLevel() {
		int temp = 0;
		for (int i = 0; i < getStations().size(); i++) {
			temp += getStations().get(i).getItem().getEffectivenessLevel();
		}
		return temp;
	}
	public void installItem(Tile tile) {
		tile.setParentSystem(this);
		addAccessPoint(tile);
	}
	public void unInstallItem(Tile tile) {
		tile.setParentSystem(null);
		removeAccessPoint(tile);
	}
	/**
	 * Return's a popup box that adds system buttons to input buttons
	 */
	public PopUpBox createSystemOptionsPopUp(Component comp, Point click,
			ArrayList<JButton> list, Tile source) {
		// Home.messagePanel.addMessage("Add popup box to " + getName());
		createSystemOptions(comp, source, list);
		PopUpBox pop = new PopUpBox(list);
		pop.setTile(source);
		pop.show(comp, click.x, click.y);
		return pop;
	}
	/**
	 * Return's a list of system buttons added to input buttons
	 */
	public void createSystemOptions(Component comp, Tile tile, ArrayList<JButton> list) {
		// use this to move the cancel button to the last in the list
		JButton btnCancel = list.get(list.size() - 1);
		list.remove(list.size() - 1);
		list.addAll(systemOptionOverride((ActionListener) comp, tile));
		list.add(btnCancel);
	}
	/**
	 * Get's overRidden by the system to provide contextual power on events
	 */
	public void powerSystemOn(Tile station) {
		// Don't play power up sound for enemy ship, they probably were already
		// powered on
		if (station.isFriendly() && Home.getCurrentScreen() != null
				&& station == Home.getCurrentScreen().getCurrentTile()) {
			Home.getCurrentScreen().getCurrentTilePane().updatePane();
		}
		powerUpSound.play();
	}
	/**
	 * Get's overRidden by the system to provide contextual power off events
	 */
	public void powerSystemOff(Tile station) {
		if (Home.getCurrentScreen() != null
				&& station == Home.getCurrentScreen().getCurrentTile()) {
			Home.getCurrentScreen().getCurrentTilePane().updatePane();
		}
		powerDownSound.play();
	}
	/**
	 * Get's overRidden by the system to provide contextual menus
	 */
	public ArrayList<JButton> systemOptionOverride(ActionListener comp, Tile tile) {
		ArrayList<JButton> list = new ArrayList<JButton>();
		return list;
	}
	/**
	 * Get's overriden by the system to interpret button presses from system
	 * specific buttons
	 */
	public void getButtonPress(JButton button, Tile source) {
	}
	/**
	 * Runs when ship is updated for each system overriden by system specific
	 * methods
	 */
	public void updateSystem() {
	}
	/**
	 * Runs when a system gains another manning crewmember. May be overriden by
	 * system specific methods (like turbo tubes)
	 */
	public void manSystem(CrewMan man) {
	}
	/**
	 * Runs when a system goes from being manned to being unmanned May be
	 * overriden by system specific methods (like turbo tubes)
	 */
	public void unManSystem(CrewMan man) {
		if (Home.getCurrentScreen() instanceof GameScreen) {
			((GameScreen) Home.getCurrentScreen()).removePopUpBox();
		}
	}
	public Color getMainColor() {
		return tileTypes.get(0);
	}
	public Tile getFirstStation() {
		if (stations.size() > 0) {
			return stations.get(0);
		}
		return null;
	}
	public Item getFirstItem() {
		if (stations.size() > 0) {
			return stations.get(0).getItem();
		}
		return null;
	}
	/**
	 * Returns the stations in this ship system that have room to be manned (and
	 * aren't manned already)
	 */
	public ArrayList<Tile> getMannableStations() {
		ArrayList<Tile> ret = new ArrayList<Tile>();
		ArrayList<Tile> list = getStations();
		for (int i = 0; i < list.size(); i++) {
			if (!list.get(i).isManned() && list.get(i).hasSafeNeighbor()) {
				ret.add(list.get(i));
			}
		}
		return ret;
	}
	/**
	 * Return's the first station in this ship system that has room to be manned
	 * (and isn't manned already)
	 */
	public Tile getFirstMannableStation() {
		if (getMannableStations().size() == 0) {
			return null;
		} else {
			return getMannableStations().get(0);
		}
	}
	/**
	 * Return's a random station in this ship system that has room to be manned
	 * (and isn't manned already)
	 */
	public Tile getRandomMannableStation() {
		ArrayList<Tile> list = getMannableStations();
		if (list.size() == 0) {
			return null;
		}
		// find a random guy on the list
		int i = StaticFunctions.randRange(0, list.size() - 1);
		return list.get(i);
	}
	/**
	 * Returns all the tiles on the ship that match one of the system's tiletype
	 * colors
	 */
	public ArrayList<Tile> getStationTiles(Color tileType) {
		ArrayList<Tile> retTiles = new ArrayList<Tile>();
		for (int i = 0; i < stations.size(); i++) {
			if (stations.get(i).getTileColor().equals(tileType)) {
				retTiles.add(stations.get(i));
			}
		}
		return retTiles;
	}
	/**
	 * Return all the tiles on this floor
	 */
	public ArrayList<Tile> getStationTilesOnFloor(int floor) {
		ArrayList<Tile> retTiles = new ArrayList<Tile>();
		for (int i = 0; i < stations.size(); i++) {
			if (stations.get(i).getFloor() == floor) {
				retTiles.add(stations.get(i));
			}
		}
		return retTiles;
	}
	/**
	 * Find's the tile of this item if this item is part of the system
	 */
	public Tile findStationThatContains(Item item) {
		ArrayList<Item> items = getSystemItems();
		if (items.contains(item)) {
			ArrayList<Tile> list = getStations();
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getItem() == item) {
					return list.get(i);
				}
			}
		}
		return null;
	}
	/**
	 * Return's the station manned by this actor
	 */
	public Tile findStationMannedBy(CrewMan man) {
		for (int i = 0; i < stations.size(); i++) {
			if (stations.get(i).getMan() == man) {
				return stations.get(i);
			}
		}
		return null;
	}
	/**
	 * Return all the crew manning this subsystem
	 * 
	 * @param TileType
	 * @return
	 */
	public ArrayList<CrewMan> getManningCrew(Color subSystem) {
		ArrayList<CrewMan> ret = new ArrayList<CrewMan>();
		for (int i = 0; i < stations.size(); i++) {
			if (stations.get(i).getMan() != null
					&& stations.get(i).getTileColor().equals(subSystem)) {
				ret.add(stations.get(i).getMan());
			}
		}
		return ret;
	}
	public ArrayList<CrewMan> getManningCrew() {
		ArrayList<CrewMan> ret = new ArrayList<CrewMan>();
		for (int i = 0; i < stations.size(); i++) {
			if (stations.get(i).getMan() != null) {
				ret.add(stations.get(i).getMan());
			}
		}
		return ret;
	}
	public ArrayList<CrewMan> getManningCrewRandom(Color subSystem) {
		ArrayList<CrewMan> ret = new ArrayList<CrewMan>();
		ArrayList<CrewMan> list = getManningCrew(subSystem);
		while (list.size() > 0) {
			// find a random guy on the list
			int i = StaticFunctions.randRange(0, list.size() - 1);
			ret.add(list.get(i));
			list.remove(i);
		}
		return ret;
	}
	/**
	 * Return's all of the items of all of the system's stations
	 * 
	 */
	public ArrayList<Item> getSystemItems() {
		ArrayList<Tile> tiles = getStations();
		ArrayList<Item> items = new ArrayList<Item>();
		for (int i = 0; i < tiles.size(); i++) {
			Item item = tiles.get(i).getItem();
			// only include items that match this system type
			if (item != null && includesTileType(item.getItemType())) {
				items.add(item);
			}
			// if this tile doesn't contain an item, or contains an item for a
			// different system
			else {
				// uninstall it from this system
				// unInstallItem(tiles.get(i));
				// install it on its parent system if needed?
				System.out.println("ShipSystem: Item did not match proper system");
			}
		}
		return items;
	}
	/**
	 * Returns all the items on the ship that match one of the system's tiletype
	 * colors
	 */
	public ArrayList<Item> getSystemItems(Color tileType) {
		ArrayList<Item> retTiles = new ArrayList<Item>();
		for (int i = 0; i < stations.size(); i++) {
			if (stations.get(i).getTileColor().equals(tileType)) {
				retTiles.add(stations.get(i).getItem());
			}
		}
		return retTiles;
	}
	/**
	 * Returns all the items on the ship that match one of the system's tiletype
	 * colors, returns them in a random order
	 */
	public ArrayList<Item> getSystemItemsRandom(Color tileType) {
		ArrayList<Item> ret = new ArrayList<Item>();
		ArrayList<Item> list = getSystemItems(tileType);
		while (list.size() > 0) {
			// find a random guy on the list
			int i = StaticFunctions.randRange(0, list.size() - 1);
			ret.add(list.get(i));
			list.remove(i);
		}
		return ret;
	}
	public CrewMan getFirstManningCrew() {
		if (getStations().size() > 0) {
			for (int i = 0; i < getStations().size(); i++) {
				if (getStations().get(i).isManned()) {
					return getStations().get(i).getMan();
				}
			}
		}
		return null;
	}
	public boolean includesTileType(Color matchColor) {
		for (int i = 0; i < getTileTypes().size(); i++) {
			if (getTileTypes().get(i).equals(matchColor)) {
				return true;
			}
		}
		return false;
	}
	public boolean includesTile(Tile tile) {
		return includesTileType(tile.getTileColor());
	}
	public void addAccessPoint(Tile accesspoint) {
		stations.add(accesspoint);
	}
	public void removeAccessPoint(Tile accesspoint) {
		stations.remove(accesspoint);
	}
	/**
	 * Returns whether the system is manned (has at least 1 manned station/ 1
	 * manning crew)
	 */
	public boolean isManned() {
		for (int i = 0; i < stations.size(); i++) {
			if (stations.get(i).isManned()) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns whether the system's subsystem (specific color) is manned (has at
	 * least 1 manned station/ 1 manning crew)
	 */
	public boolean isSubSystemManned(Color type) {
		ArrayList<Tile> list = getStationTiles(type);
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).isManned()) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns whether the system is powered (has at least 1 powered station)
	 */
	public boolean isSystemPowered() {
		for (int i = 0; i < stations.size(); i++) {
			if (stations.get(i).isPowered()) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns true if this system has at least 1 station that is both powered
	 * and manned
	 */
	public boolean isSystemOnline() {
		return (isManned() && isSystemPowered());
	}
	public ArrayList<Color> getTileTypes() {
		return tileTypes;
	}
	public void setTileTypes(ArrayList<Color> tileTypes) {
		this.tileTypes = tileTypes;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Ship getParentShip() {
		return parentShip;
	}
	public void setParentShip(Ship parentShip) {
		this.parentShip = parentShip;
	}
	public ArrayList<Tile> getStations() {
		return stations;
	}
	public void setStations(ArrayList<Tile> stations) {
		this.stations = stations;
	}
}
