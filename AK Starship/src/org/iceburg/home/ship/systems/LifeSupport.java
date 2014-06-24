package org.iceburg.home.ship.systems;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.items.ACGenerator;
import org.iceburg.home.items.ACVent;
import org.iceburg.home.items.Item;
import org.iceburg.home.items.Weapon;
import org.iceburg.home.main.StaticFunctions;
import org.iceburg.home.ship.Room;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.ui.MessageCenter;
import org.iceburg.home.ui.TilePane;
import org.iceburg.home.ui.UIMisc;

public class LifeSupport extends ShipSystem {
	// Weapons, the main color, is also the beam weapon
	public static Color systemMain = Color.decode("#aa76c6");
	public static Color colorACVent = Color.decode("#8476c6");
	public int airCapacity, airCurrent;
	// vacumMap is used for determaning the distance from a vacume
	// room map is reused for low/high pressure between rooms
	public HashMap<Tile, Integer> vacumMap, roomMap;
	public JButton btnOpen;

	// amount of air produced = each station*item production amount
	// vent can give out up to item amount
	public LifeSupport(Ship ship) {
		super(ship);
		this.tileTypes.add(systemMain);
		this.tileTypes.add(colorACVent);
		this.name = "Life Support";
	}
	@Override
	public String toString() {
		return getName() + ", Air: " + getAirCurrent() + "/" + getAirTotal();
	}
	@Override
	public void updateSystem() {
		// System.out.println(" Update ls");
		if (timer < 10) {
			timer += 1;
			// stagger pot stirring and venting
			if (timer == 2) {
				runVacuum();
			} else if (timer == 5) {
				stirThePots();
			}
		} else {
			timer = 0;
			updateGens();
			updateVents();
		}
	}
	@Override
	public void paintTileInfo(TilePane p, Item item) {
		if (item instanceof ACGenerator) {
			ACGenerator w = (ACGenerator) item;
			UIMisc.addJLabel("Air Reserves: " + w.getCurrentStock() + "/"
					+ w.getCurrentCapacity(), p);
		}
		else if (item instanceof ACVent) {
			ACVent w = (ACVent) item;
			String s = MessageCenter.colorString("Closed", Color.red);
			if (w.isOpen()){
				s = MessageCenter.colorString("Open", Color.green);
			}
			UIMisc.addJLabel(s, p);
		}
	}
	@Override
	public void paintTileMultiInfo(TilePane p, Item item) {
		if (item instanceof ACVent) {
			ACVent w = (ACVent) item;
			String s = MessageCenter.colorString("Closed", Color.red);
			if (w.isOpen()){
				s = MessageCenter.colorString("Open", Color.green);
			}
			UIMisc.addJLabel(s, p);
		}
	}
	@Override
	public ArrayList<JButton> systemOptionOverride(ActionListener comp, Tile tile) {
		ArrayList<JButton> list = new ArrayList<JButton>();
		if (tile.getItem() instanceof ACVent){
			String s = "Close";
			if (btnOpen == null) {
				btnOpen = UIMisc.createJButton(s, comp);
			}
			if (!((ACVent) tile.getItem()).isOpen()) {
				btnOpen.setText("Open");
			} else {
				btnOpen.setText(s);
			}
			if (isManned()) {
				list.add(btnOpen);
			}
		}
		return list;
	}
	@Override
	public void getButtonPress(JButton button, Tile source) {
		// System.out.println("weapons button pressed");
		if (button == btnOpen) {
			if (source.getItem() instanceof ACVent){
				((ACVent) source.getItem()).setOpen(!((ACVent) source.getItem()).isOpen());
			}
		}
	}
	public void updateGens() {
		ArrayList<ACGenerator> list = getGenerators();
		// reset these values to be reset by the generators
		setAirCapacity(0);
		setAirCurrent(0);
		for (int i = 0; i < list.size(); i++) {
			list.get(i).updateGen();
		}
	}
	public void updateVents() {
		ArrayList<ACVent> list = getVentsRandom();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) != null) {
				list.get(i).updateVent();
			}
		}
	}
	/**
	 * Returns a list of 'holes' where vacumes start (Space tiles adjacent to
	 * open ship tiles, and crewmen)
	 */
	public ArrayList<Tile> findVacumStartTiles(int floor) {
		ArrayList<Tile> startTiles = new ArrayList<Tile>();
		Tile[][] tiles = getParentShip().getFloorplans().get(floor).getShipTiles();
		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[x].length; y++) {
				// find all of the space tiles that are adjacent to air,
				Tile tile = tiles[x][y];
				if (tile.isSpaceAdjacentToFlow()) {
					startTiles.add(tile);
				}
			}
		}
		// add crewman to the 'holes' list -disabled for more realistic air flow
//		ArrayList<CrewMan> crew = getParentShip().getCrew();
//		for (int i = 0; i < crew.size(); i++) {
//			startTiles.add(crew.get(i).getLocationTile());
//		}
		return startTiles;
	}
	/**
	 * Finds the distance from a space hole, creating a path for the vacuum to
	 * follow
	 */
	public HashMap<Integer, ArrayList<Tile>> findVacumePath(ArrayList<Tile> startTiles) {
		vacumMap = new HashMap<Tile, Integer>();
		// recheck for each of the startTiles so that we have the right costs
		for (int t = 0; t < startTiles.size(); t++) {
			// the space tile we start from
			Tile startTile = startTiles.get(t);
			// don't go any further unless we have a non-null start tile!
			if (startTile != null) {
				ArrayList<Tile> closed = new ArrayList<Tile>();
				ArrayList<Tile> open = new ArrayList<Tile>();
				int cost = 0;
				open.add(startTile);
				vacumMap.put(startTile, cost);
				while (open.size() > 0) {
					// move the current tile to the closed list
					Tile current = open.get(0);
					open.remove(current);
					closed.add(current);
					// used to be just cost += 1;
					cost = vacumMap.get(current) + 1;
					// evaluate the neighbor squares
					ArrayList<Tile> tiles = current.findAOETilesFlat(1);
					for (int i = 0; i < tiles.size(); i++) {
						// old: tiles.get(i).isSafe()
						Tile testTile = tiles.get(i);
						if (testTile != null && testTile.isSolid() == false
								&& closed.contains(testTile) == false) {
							if (vacumMap.containsKey(testTile) == false
									|| vacumMap.get(testTile) > cost) {
								vacumMap.put(testTile, cost);
								// add the tile to the list of tiles we're
								// searching for, unless, it's a space tile
								if (!testTile.isSpaceTile()) {
									open.add(testTile);
								}
								// else, ignore the space tile
								else {
									closed.add(testTile);
								}
							}
						}
					}
				}
			}
		}
		// sort them by distance and return them
		return sortPath(vacumMap);
		// System.out.println("Found path");
	}
	/**
	 * Finds the distance from a low pressure tile, creating a path for the
	 * vacuum to follow Used for room stirs
	 */
	public HashMap<Integer, ArrayList<Tile>> findStirPath(Tile startTile) {
		// don't go any further unless we have a non-null start tile!
		if (startTile != null) {
			roomMap = new HashMap<Tile, Integer>();
			ArrayList<Tile> closed = new ArrayList<Tile>();
			ArrayList<Tile> open = new ArrayList<Tile>();
			int cost = 0;
			open.add(startTile);
			roomMap.put(startTile, cost);
			while (open.size() > 0) {
				// move the current tile to the closed list
				Tile current = open.get(0);
				open.remove(current);
				closed.add(current);
				cost += 1;
				// evaluate the neighbor squares
				ArrayList<Tile> tiles = current.findAOETilesFlat(1);
				for (int i = 0; i < tiles.size(); i++) {
					// old: tiles.get(i).isSafe()
					Tile testTile = tiles.get(i);
					if (testTile != null && testTile.isSolid() == false
							&& closed.contains(testTile) == false) {
						if (roomMap.containsKey(testTile) == false
								|| roomMap.get(testTile) > cost) {
							roomMap.put(testTile, cost);
							// add the tile to the list of tiles we're searching
							// for, unless, it's a space tile
							if (!testTile.isSpaceTile()) {
								open.add(testTile);
							}
							// else, ignore the space tile
							else {
								closed.add(testTile);
							}
						}
					}
				}
			}
		}
		// sort them by distance and return them
		return sortPath(roomMap);
		// System.out.println("Found path");
	}
	/**
	 * Convert's a hashmap tile-int to a hashmap sorted by int with a list of
	 * tiles for each int Doesn't properly sort duplicates, so don't give this
	 * duplicate tiles
	 */
	public HashMap<Integer, ArrayList<Tile>> sortPath(HashMap<Tile, Integer> map) {
		HashMap<Integer, ArrayList<Tile>> ret = new HashMap<Integer, ArrayList<Tile>>();
		for (Tile tile : map.keySet()) {
			int key = map.get(tile);
			// make sure the list for this key isn't empty
			if (ret.get(key) == null) {
				ret.put(key, new ArrayList<Tile>());
			}
			ret.get(key).add(tile);
		}
		return ret;
	}
	/**
	 * Travels along the vacume path, pulling air out of the ship
	 */
	public void pullAir(HashMap<Integer, ArrayList<Tile>> path, HashMap<Tile, Integer> map) {
		// for each distance on the path
		for (int dist : path.keySet()) {
			ArrayList<Tile> list = path.get(dist);
			// grab each tile
			for (int i = 0; i < list.size(); i++) {
				// and take air
				list.get(i).grabAir(map);
			}
		}
	}
	/**
	 * Even's out air within rooms (based on doors)
	 */
	public void stirThePots() {
		// for each room on the ship
		ArrayList<Room> rooms = getParentShip().getRooms();
		for (int i = 0; i < rooms.size(); i++) {
			// Tile tile = getParentShip().getRooms().get(i).getFirstTile();
			// if the first tile of the room is contained within the vacume map,
			// ignore it
			// if (!(vacumMap != null && vacumMap.containsKey(tile))){
			// else, within each room, find the lowest air level- this is the
			// start tile
			Tile startTile = rooms.get(i).findLowPressure();
			// if lowest value < highest value, average air towards the low
			// pressure
			if (startTile != null) {
				HashMap<Integer, ArrayList<Tile>> path = findStirPath(startTile);
				if (!path.isEmpty()) {
					// pull air towards space, starting with the nearest tile
					pullAir(path, roomMap);
				}
			}
			// }
		}
	}
	/**
	 * Even's air out as it moves towards holes/ vacuums
	 */
	public void runVacuum() {
		// for each floor
		for (int f = 0; f < getParentShip().getFloorplans().size(); f++) {
			// find the 'holes'
			ArrayList<Tile> startTiles = findVacumStartTiles(f);
			// if we have holes
			if (startTiles.size() > 0) {
				// create a 'crew path' finding the distance from each start
				// tile
				HashMap<Integer, ArrayList<Tile>> path = findVacumePath(startTiles);
				if (!path.isEmpty()) {
					// pull air towards space, starting with the nearest tile
					pullAir(path, vacumMap);
				}
			}
		}
	}
	/**
	 * Powers other generators with the 'same' item
	 */
	public void powerSimiliarGens(ACGenerator engine) {
		ArrayList<ACGenerator> items = getGenerators();
		Engineering engines = getParentShip().getEngineering();
		for (int i = 0; i < items.size(); i++) {
			Item item = items.get(i);
			if (engine.getId().equals(item.getId())) {
				engines.attemptPowerSystem(items.get(i));
			}
		}
	}
	/**
	 * Run on ship complete- auto turns on life support so people don't die
	 */
	public void powerOnLifeSupport() {
		ArrayList<ACGenerator> items = getGenerators();
		if (items.size() > 0) {
			powerSimiliarGens(items.get(0));
		}
	}
	/**
	 * Changes the air capacity by this amount
	 */
	public void incAirCapacity(int amount) {
		setAirCapacity(airCapacity + amount);
	}
	/**
	 * Changes the current amount of air by this amount
	 */
	public void incAirCurrent(int amount) {
		int newAir = getAirCurrent() + amount;
		// if (newAir > getAirTotal()){
		// newAir = getAirTotal();
		// }
		// else if (newAir < 0){
		// newAir = 0;
		// }
		setAirCurrent(newAir);
		// if we're subtracting air, we need to remove it from it's generator
		if (amount < 0) {
			int i = 0;
			// while amount is negative, search the generators
			ArrayList<ACGenerator> list = getGenerators();
			while (amount < 0 && i < list.size()) {
				ACGenerator gen = list.get(i);
				// how much can we take from this generator?
				// amount is negative, make it positive so we can compare
				int take = Math.min(gen.getCurrentStock(), -amount);
				// subtract take from the current stock
				gen.incCurrentStock(-take);
				// reduce the amount we need by the take amount
				amount = amount + take;
				i++;
			}
		}
	}
	/**
	 * Return AC Generators
	 */
	public ArrayList<ACGenerator> getGenerators() {
		ArrayList<ACGenerator> ret = new ArrayList<ACGenerator>();
		ArrayList<Item> list = getSystemItems(LifeSupport.systemMain);
		for (int i = 0; i < list.size(); i++) {
			ret.add((ACGenerator) list.get(i));
		}
		return ret;
	}
	/**
	 * Return AC Vents, shuffling their order for even air distribution
	 */
	public ArrayList<ACVent> getVentsRandom() {
		// this has been refacterd to ship system, but is a faster method
		ArrayList<ACVent> ret = new ArrayList<ACVent>();
		ArrayList<Item> list = getSystemItems(LifeSupport.colorACVent);
		while (list.size() > 0) {
			// find a random guy on the list
			int i = StaticFunctions.randRange(0, list.size() - 1);
			ret.add((ACVent) list.get(i));
			list.remove(i);
		}
		return ret;
		// ArrayList<ACVent> ret = new ArrayList<ACVent>();
		// ArrayList<Item> list = getSystemItems(LifeSupport.colorACVent);
		// for (int i = 0; i < list.size(); i++) {
		// ret.add((ACVent) list.get(i));
		// }
		// return ret;
	}
	/**
	 * Return AC Vents in the same order every time
	 */
	public ArrayList<ACVent> getVents() {
		ArrayList<ACVent> ret = new ArrayList<ACVent>();
		ArrayList<Item> list = getSystemItems(LifeSupport.colorACVent);
		for (int i = 0; i < list.size(); i++) {
			ret.add((ACVent) list.get(i));
		}
		return ret;
	}
	public int getAirTotal() {
		return airCapacity;
	}
	public void setAirCapacity(int airTotal) {
		this.airCapacity = airTotal;
	}
	public int getAirCurrent() {
		return airCurrent;
	}
	public void setAirCurrent(int airCurrent) {
		this.airCurrent = airCurrent;
	}
	public HashMap<Tile, Integer> getVacumMap() {
		return vacumMap;
	}
	public void setVacumMap(HashMap<Tile, Integer> vacumMap) {
		this.vacumMap = vacumMap;
	}
	
}
