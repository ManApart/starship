package org.iceburg.home.ship.systems;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.ai.TakeTileAction;
import org.iceburg.home.ai.TravelAction;
import org.iceburg.home.items.CargoBay;
import org.iceburg.home.items.Item;
import org.iceburg.home.main.Home;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.ui.GameScreen;
import org.iceburg.home.ui.MessageCenter;
import org.iceburg.home.ui.TilePane;
import org.iceburg.home.ui.UIMisc;

public class CargoHold extends ShipSystem {
	public static Color systemMain = Color.decode("#b5774c");
	private JButton btnTake;
	private JButton btnStore;

	public CargoHold(Ship ship) {
		super(ship);
		this.tileTypes.add(systemMain);
		this.name = "Cargo Bay";
	}
	@Override
	public void paintTileInfo(TilePane p, Item item) {
		CargoBay w = (CargoBay) item;
		if (w.getStock() > 0) {
			UIMisc.addJLabel("Holding: " + w.getItem().getName() + " x" + w.getStock()
					+ "/" + w.getCapacity(), p);
		} else {
			UIMisc.addJLabel("Capacity: " + w.getCapacity(), p);
		}
	}
	@Override
	public void paintTileMultiInfo(TilePane p, Item item) {
		CargoBay w = (CargoBay) item;
		if (w.getStock() > 0) {
			UIMisc.addJLabel("" + MessageCenter.colorString("Holding: " + w.getItem().getName() + " x" + w.getStock()
					+ " /" + w.getCapacity(), Color.green), p);
		} else {
			UIMisc.addJLabel("Capacity: " + w.getCapacity(), p);
		}
	}
	@Override
	public ArrayList<JButton> systemOptionOverride(ActionListener comp, Tile tile) {
		ArrayList<JButton> list = new ArrayList<JButton>();
		btnTake = UIMisc.createJButton("Take item", comp);
		if (hasCargoItem(tile)) {
			btnTake.setText("Take " + getCargoItem(tile));
			list.add(btnTake);
		}
		CrewMan man = Home.getCurrentScreen().getCurrentCrewMan();
		btnStore = UIMisc.createJButton("Store item", comp);
		if (man != null && man.hasItem()) {
			btnStore.setText("Store " + man.getItem());
			list.add(btnStore);
		}
		return list;
	}
	@Override
	public void getButtonPress(JButton button, Tile source) {
		// do a text match as the tilepane and popup versions of this button may
		// become desynced
		if ((button.getText().equals(btnTake.getText()))
				|| (button.getText().equals(btnStore.getText()))) {
			CrewMan currentCrewMan = Home.getCurrentScreen().getCurrentCrewMan();
			currentCrewMan.getAi().clear();
			// Tile nearTile = source.findTouchingTile();
			TravelAction.assembleTravelAction(source, currentCrewMan);
			// take or store the item based on button
			if (button.getText().equals(btnTake.getText())) {
				currentCrewMan.addAIAction(new TakeTileAction(currentCrewMan.getAi(), currentCrewMan, source, true));
			} else {
				currentCrewMan.addAIAction(new TakeTileAction(currentCrewMan.getAi(), currentCrewMan, source, false));
			}
		}
	}
	/**
	 * Returns this cargo bay's first item
	 * 
	 * @param tile
	 * @return
	 */
	public Item getCargoItem(Tile tile) {
		if (tile.hasItem() && tile.getItem().getItemType().equals(systemMain)) {
			CargoBay cb = (CargoBay) tile.getItem();
			if (cb.hasItem()) {
				return cb.getItem();
			}
		}
		return null;
	}
	/**
	 * Returns the cargohold's first item
	 * 
	 * @param tile
	 * @return
	 */
	public Item getFirstCargoItem() {
		for (int i=0; i<getStations().size(); i++){
			Tile tile = getStations().get(i);
			if (tile.hasItem() && tile.getItem().getItemType().equals(systemMain)) {
				CargoBay cb = (CargoBay) tile.getItem();
				if (cb.hasItem()) {
					return cb.getItem();
				}
			}
		}
		return null;
	}
	/**
	 * Remove the input item from its cargobay
	 */
	public void removeCargoItem(Item item){
		for (int i=0; i<getStations().size(); i++){
			Tile tile = getStations().get(i);
			if (tile.hasItem() && tile.getItem().getItemType().equals(systemMain)) {
				CargoBay cb = (CargoBay) tile.getItem();
				if (cb.hasItem() && cb.getItem() == item) {
					 cb.removeItem(item);
					 return;
				}
			}
		}
	}
	/**
	 * Return's whether this cargo bay has an item
	 * 
	 * @param tile
	 * @return
	 */
	public boolean hasCargoItem(Tile tile) {
		return getCargoItem(tile) != null;
	}
	/**
	 * Retrieve an item from this cargo bay if the creman is near, and has room
	 * Reduces the stock
	 */
	public static boolean attemptTakeItem(CrewMan man, Tile tile) {
		if (tile != null && tile.getParentSystem().includesTile(tile)
				&& man.isTouching(man.getLocation(), tile.getLocation())) {
			CargoBay cb = (CargoBay) tile.getItem();
			Item item = cb.getItem();
			if (item != null) {
				// The crewman has room for the item,
				// or can swap items (neither item is a floor)
				if (man.getItem() == null) {
					man.setItem(item);
					if (!Home.creativeMode) {
						cb.removeItem(item);
					}
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Store an item in the cargo bay if the crewman has a valid item and stock
	 * isn't full
	 */
	public static boolean attemptStoreItem(CrewMan man, Tile tile) {
		// we have a valid tile, the man is nearby, and has an item to store
		if (tile != null && tile.getParentSystem().includesTile(tile) && man.hasItem()
				&& man.isTouching(man.getLocation(), tile.getLocation())) {
			CargoBay cb = (CargoBay) tile.getItem();
			// we have room to store the item
			if (cb.hasRoom()) {
				// the man's item is compatable with the cargo
				if (!cb.hasItem()
						|| (cb.hasItem() && cb.getItem().isSameBaseItem(man.getItem())))
					// only remove crew item if we made a successful transfer
					if (cb.addItem(man.getItem())) {
						man.setItem(null);
					}
				// if we're looking at stations panel and this tile is selected
				if (Home.getCurrentScreen() instanceof GameScreen) {
					TilePane pane = TilePane.containsTile(tile);
					if (pane != null) {
						pane.updatePane();
					}
				}
				return true;
			}
		}
		return false;
	}
	public ArrayList<CargoBay> getAllCargoBays() {
		ArrayList<CargoBay> ret = new ArrayList<CargoBay>();
		ArrayList<Tile> list = getStations();
		for (int i = 0; i < list.size(); i++) {
			ret.add((CargoBay) list.get(i).getItem());
		}
		return ret;
	}
	/**
	 * Return's a cargobay that contains this type of item and has room to add
	 * stock return's null if unsuccessful
	 */
	public CargoBay findVacantCargoBayWith(Item item) {
		ArrayList<CargoBay> bays = getAllCargoBays();
		for (int i = 0; i < bays.size(); i++) {
			CargoBay bay = bays.get(i);
			if (bay.hasRoom() && (bay.hasItem() && bay.getItem().isSameBaseItem(item))) {
				return bay;
			}
		}
		return null;
	}
	/**
	 * Return's a cargobay that contains this type of item 
	 * return's null if unsuccessful
	 */
	public CargoBay findCargoBayWith(Item item) {
		ArrayList<CargoBay> bays = getAllCargoBays();
		for (int i = 0; i < bays.size(); i++) {
			CargoBay bay = bays.get(i);
			if ((bay.hasItem() && bay.getItem().isSameBaseItem(item))) {
				return bay;
			}
		}
		return null;
	}
	/**
	 * Find's a cargobay that has room for this item, first looking at bays that
	 * already have an item of this type, then looking at empty bays as well
	 */
	public CargoBay findVacantCargoBay(Item item) {
		CargoBay ret = findVacantCargoBayWith(item);
		if (ret != null) {
			return ret;
		} else {
			ArrayList<CargoBay> bays = getAllCargoBays();
			for (int i = 0; i < bays.size(); i++) {
				CargoBay bay = bays.get(i);
				if (bay.hasRoom()
						&& (!bay.hasItem() || (bay.hasItem() && bay.getItem().isSameBaseItem(item)))) {
					return bay;
				}
			}
		}
		return null;
	}
	// Assemble the steps to loot a tile, remember the AIActions are
	// instructions that are carried out later!
	public void assembleLootAI(CrewMan man, Tile clickTile) {
		man.getAi().clear();
		// travel to the tile and take it
		TravelAction.assembleTravelAction(clickTile, man);
		man.addAIAction(new TakeTileAction(man.getAi(), man, clickTile, true));
		// find a place to store it and then store it
		CargoBay bay = findVacantCargoBay(clickTile.getItem());
		if (bay != null) {
			Tile bayTile = bay.getParentTile();
			// nearTile = clickTile.findTouchingTile();
			TravelAction.assembleTravelAction(bayTile, man, clickTile);
			man.addAIAction(new TakeTileAction(man.getAi(), man, bayTile, false));
		} else {
			Home.messagePanel.addMessage(man, "No room to store this " + clickTile.getName()
					+ ", sir!");
		}
	}
}
