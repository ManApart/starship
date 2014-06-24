package org.iceburg.home.ship.systems;

import java.awt.Color;
import java.util.ArrayList;

import org.iceburg.home.items.Item;
import org.iceburg.home.main.Home;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.ui.GameScreen;
import org.iceburg.home.ui.TilePane;
import org.iceburg.home.ui.UIMisc;

public class Engineering extends ShipSystem {
	public static Color systemMain = Color.decode("#983da5");
	private int powerTotal, powerUsed;
	ArrayList<Item> poweredItems;

	public Engineering(Ship ship) {
		super(ship);
		this.tileTypes.add(systemMain);
		this.name = "Engineering";
		this.poweredItems = new ArrayList<Item>();
		// System.out.println("eng");
	}
	/**
	 * 
	 */
	public void powerEngine(Item item, int level) {
		int amount = level - item.getPowerCurrent();
		item.setCurrentPower(level);
		// increase/ decrease power by amount changed
		bumpPowerTotal(amount* 4);
		// if we're looking at stations panel and this tile is selected
		if (Home.getCurrentScreen() instanceof GameScreen) {
			TilePane pane = TilePane.containsTile(item.getParentTile());
			if (pane != null) {
				Home.getCurrentScreen().getCurrentTilePane().updatePane();
			}
		}
	}
	/**
	 * Powers all the engines
	 */
	public void powerAllEngines() {
		ArrayList<Tile> tiles = getStations();
		for (int i = 0; i < tiles.size(); i++) {
			Item item = tiles.get(i).getItem();
			powerEngine(item, item.getMaxPower());
		}
	}
	/**
	 * powers all of the items that are the same subsystem as the input system
	 * returns true if there was enough power to fully power every subsystem
	 * station
	 */
	public boolean attemptPowerSubSystem(Item source) {
		boolean on = source.getPowerCurrent() == 0;
		// get all the tiles of this item's type
		if (source.getParentTile().hasParentSystem()){
			ArrayList<Tile> tiles = source.getParentTile().getParentSystem().getStationTiles(source.getItemType());
			for (int i = 0; i < tiles.size(); i++) {
				Item item = tiles.get(i).getItem();
				// turning systems on or off?
				if (on) {
					attemptPowerSystem(item);
					// don't keep trying once we've hit full power use
					if (!Home.creativeMode && getPowerUsed() >= getPowerTotal()) {
						Home.messagePanel.addMessage(source.getParentTile().getParentSystem().getFirstManningCrew(), "She's already giving her all she's got, Captain!");
						return false;
					}
				} else {
					attemptPowerSystem(item, 0);
				}
			}
			return true;
		}
		else{
			System.out.println("Engineering: " + source + " has no parent system.");
			return false;
		}
	}
	/**
	 * Power the system's item if there is enough power in the engines Powers to
	 * max, or a given level
	 **/
	public boolean attemptPowerSystem(Item item) {
		return attemptPowerSystem(item, item.getMaxPower());
	}
	/**
	 * Power the system's item if there is enough power in the engines Powers to
	 * max, or a given level
	 **/
	public boolean attemptPowerSystem(Item item, int level) {
		if (item.getItemType().equals(systemMain)) {
			powerEngine(item, level);
			return true;
		}
		// power system tile
		else {
			int amount = level - item.getPowerCurrent();
			// always works if we're in creative mode
			// if we have power to power the system, or subtracting doesn't make
			// us negative
			if (Home.creativeMode || (getPowerUsed() + amount >= 0
			// the system can not have enough power, if we're reducing the power
			// level
					&& (amount + getPowerUsed() <= getPowerTotal() || amount < 0))) {
				bumpPowerUsed(amount);
				item.bumpCurrentPower(amount);
				trackPoweredItems(item);
				return true;
			}
		}
		Home.messagePanel.addMessage(item.getParentTile().getParentSystem().getFirstManningCrew(), "Not enough power, sir!");
		return false;
	}
	/**
	 * Checks to make sure there is enough power to power all the systems,
	 * powering off the latest systems as necessary
	 */
	public void checkPowerLevel() {
		int diff = getPowerUsed() - getPowerTotal();
		// if we need more power than we have
		while (diff > 0) {
			Item item = getLatestPoweredItem();
			// find how much power we can take from the latest item
			int take = Math.min(diff, item.getPowerCurrent());
			// set the item's power level based on the take
			int level = item.getPowerCurrent() - take;
			// powering off a system automatically removes it from the list
			attemptPowerSystem(item, level);
			diff -= take;
		}
	}
	/**
	 * If the item's power level is positive, this adds the item to the powered
	 * items list. If the power level is 0, it removes it.
	 */
	public void trackPoweredItems(Item item) {
		if (item.getPowerCurrent() > 0) {
			if (!poweredItems.contains(item)) {
				poweredItems.add(item);
			}
		} else {
			if (poweredItems.contains(item)) {
				poweredItems.remove(item);
			}
		}
	}
	@Override
	public void paintTileInfo(TilePane p, Item item) {
		UIMisc.addJLabel("Total power: " + powerUsed + "/" + powerTotal, p);
	}
	/**
	 * Run on ship complete- auto turns on power
	 */
	public void autoPowerUp() {
		ArrayList<Item> items = getSystemItems(systemMain);
		if (items.size() > 0) {
			powerAllEngines();
		}
	}
	// Home.messagePanel.addMessage(getFirstManningCrew(),
	// MessageCenter.typeEngineerTileType, tile.hasParentSystem(),
	// tile.getName(), sysName);
	// Home.messagePanel.addMessage(tile.getDescription(),
	// getFirstManningCrew());
	public int getPowerTotal() {
		return powerTotal;
	}
	public int getPowerUsed() {
		return powerUsed;
	}
	public void setPowerUsed(int powerUsed) {
		this.powerUsed = powerUsed;
	}
	public void setPowerTotal(int powerTotal) {
		this.powerTotal = powerTotal;
	}
	/**
	 * Changes the power total by this amount
	 */
	public void bumpPowerTotal(int amount) {
		setPowerTotal(powerTotal + amount);
		// if power decreased, check powerlevel
		if (!Home.creativeMode && amount < 0) {
			checkPowerLevel();
		}
	}
	/**
	 * Changes the power used by this amount
	 */
	public void bumpPowerUsed(int amount) {
		setPowerUsed(powerUsed + amount);
	}
	public Item getLatestPoweredItem() {
		return poweredItems.get(poweredItems.size() - 1);
	}
}
