package org.iceburg.home.items;

import java.util.ArrayList;

import org.iceburg.home.main.Home;
import org.iceburg.home.ship.systems.CargoHold;
import org.iceburg.home.ui.TilePane;

public class CargoBay extends Item {
	private ArrayList<Item> items;
	private int capacity;

	public CargoBay() {
		items = new ArrayList<Item>();
	}
	/**
	 * Return the amount of items held by this cargobay
	 * 
	 * @return
	 */
	public int getStock() {
		if (getItems().size() > 0) {
			return getItems().size();
		}
		return 0;
	}
	/**
	 * Return this cargo's total capicity (number of items it can store)
	 * 
	 * @return
	 */
	public int getCapacity() {
		return this.capacity;
	}
	/**
	 * Return's whether this cargohold has room for another item
	 * 
	 * @return
	 */
	public boolean hasRoom() {
		return getStock() < getCapacity();
	}
	public ArrayList<Item> getItems() {
		return items;
	}
	/**
	 * Return the first item stored in this cargo bay
	 */
	public Item getItem() {
		if (getItems().size() > 0) {
			return getItems().get(0);
		}
		return null;
	}
	public boolean hasItem() {
		return getItems().size() > 0;
	}
	/**
	 * Attempts to add an item to this cargo bay will fail if the item is not
	 * the same type (id) as already stored itesm of if the bay is full
	 * 
	 * @param item
	 */
	public boolean addItem(Item item) {
		// boolean test =getItem() != null && getItem().isSameBaseItem(item);
		if (getStock() < getCapacity()
				&& ((getItem() != null && getItem().isSameBaseItem(item) || getItem() == null))) {
			getItems().add(item);
			updatePane();
			return true;
		}
		updatePane();
		return false;
		
	}
	/**
	 * Remove the item from the array
	 */
	public void removeItem(Item item) {
		getItems().remove(item);
		//if last item in this bay, switch to next bay with these items
		if (getItems().size() < 1){
			if (Home.getCurrentScreen().getCurrentTilePane() != null && Home.getCurrentScreen().getCurrentTilePane().getTile() != null
					&& Home.getCurrentScreen().getCurrentTilePane().getTile().getTileColor().equals(CargoHold.systemMain)){
				CargoBay cb = getParentTile().getParentShip().getCargo().findCargoBayWith(item);
				if (cb != null){
					Home.getCurrentScreen().setCurrentTile(cb.getParentTile());
				}
			}
			
		}
		updatePane();
	}
	public void setItems(ArrayList<Item> items) {
		this.items = items;
	}
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	

}
