package org.iceburg.home.ship.systems;

import java.awt.Color;
import java.util.ArrayList;

import org.iceburg.home.items.Cabin;
import org.iceburg.home.items.Item;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.ui.TilePane;
import org.iceburg.home.ui.UIMisc;

public class Cabins extends ShipSystem {
	public static Color systemMain = Color.decode("#5323a5");

	public Cabins(Ship ship) {
		super(ship);
		this.tileTypes.add(systemMain);
		this.name = "Cabins";
	}
	@Override
	public void paintTileInfo(TilePane p, Item item) {
		Cabin w = (Cabin) item;
		UIMisc.addJLabel("Cabin Capacity: " + w.getMaxCrew(), p);
		UIMisc.addJLabel("Ship Capacity: " + getCrewCount() + "/" + getCrewCapcity(), p);
	}
	public ArrayList<Cabin> getCabins() {
		ArrayList<Cabin> ret = new ArrayList<Cabin>();
		ArrayList<Item> list = getSystemItems();
		for (int i = 0; i < list.size(); i++) {
			ret.add((Cabin) list.get(i));
		}
		return ret;
	}
	/**
	 * 
	 * @return the total amount of crew we can have on the ship
	 */
	public int getCrewCapcity() {
		ArrayList<Cabin> cabins = getCabins();
		int ret = 0;
		if (cabins.size() > 0) {
			for (int i = 0; i < cabins.size(); i++) {
				ret += cabins.get(i).getMaxCrew();
			}
		}
		return ret;
	}
	/**
	 * 
	 * @return how many crew we have on the ship
	 */
	public int getCrewCount() {
		return getParentShip().getCrew().size();
	}
	/**
	 * 
	 * @return number of crew we can add before we're full (may return negative)
	 */
	public int getCrewVacancy() {
//		int i = getCrewCapcity();
//		int j = getCrewCount();
		return getCrewCapcity() - getCrewCount();
	}
	/**
	 * 
	 * @return whether we're at full crew capacity
	 */
	public boolean isFull() {
		return getCrewVacancy() <= 0;
	}
	/**
	 * 
	 * @return whether we have enough cabins for all the crew
	 */
	public boolean withinCapacity() {
		return getCrewVacancy() >= 0;
	}
}
