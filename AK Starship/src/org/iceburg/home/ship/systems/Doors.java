package org.iceburg.home.ship.systems;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;

import org.iceburg.home.main.Home;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.sound.Sound;
import org.iceburg.home.ui.MessageCenter;
import org.iceburg.home.ui.UIMisc;

public class Doors extends ShipSystem {
	public static Color systemMain = Color.decode("#69452c");
	public HashMap<Tile, Boolean> closedMap;
	JButton btnOpen;
	static Sound doorSound = new Sound("fx/door.wav");

	public Doors(Ship ship) {
		super(ship);
		this.tileTypes.add(systemMain);
		this.name = "Doors";
		this.closedMap = new HashMap<Tile, Boolean>();
	}
	@Override
	public ArrayList<JButton> systemOptionOverride(ActionListener comp, Tile tile) {
		ArrayList<JButton> list = new ArrayList<JButton>();
		String s = "Close";
		if (btnOpen == null) {
			btnOpen = UIMisc.createJButton(s, comp);
		}
		if (tile.isBlocked()) {
			btnOpen.setText("Open");
		} else {
			btnOpen.setText(s);
		}
		if (isManned()) {
			list.add(btnOpen);
		}
		return list;
	}
	@Override
	public void getButtonPress(JButton button, Tile source) {
		// System.out.println("weapons button pressed");
		if (button == btnOpen) {
			source.setBlocked(!source.isBlocked());
			doorSound.play();
		}
	}
	public void getDoorInfo(int floor, Tile tile) {
		if (isManned() && getStations().contains(tile)) {
			Home.messagePanel.addMessage(getFirstManningCrew(), MessageCenter.typeSecurityDoorInfo, tile.isBlocked(), tile.getName());
			// Home.messagePanel.addMessage("This " + tile.getName()
			// +" is shut: " + tile.isBlocked());
		}
	}
	public void toggleDoorOpen(int floor, Tile tile) {
		if (isManned() && getStations().contains(tile)) {
			tile.setBlocked(!tile.isBlocked());
			Home.messagePanel.addMessage(getFirstManningCrew(), MessageCenter.typeSecurityDoorSetClose, tile.isBlocked(), tile.getName());
			// Home.messagePanel.addMessage("Now is shut: " + tile.isBlocked());
		}
	}
	public ArrayList<Tile> getDoors() {
		return getStationTiles(systemMain);
	}
	/**
	 * Return's all of this ship's air locks
	 * 
	 * @return
	 */
	public ArrayList<Tile> getAirLocks() {
		ArrayList<Tile> airLocks = new ArrayList<Tile>();
		for (int i = 0; i < getStations().size(); i++) {
			if (getStations().get(i).isTouchingSpace()) {
				airLocks.add(getStations().get(i));
			}
		}
		return airLocks;
	}
	/**
	 * Return's all of this ship's air locks on this floor
	 * 
	 * @return
	 */
	public ArrayList<Tile> getAirLocksOnFloor(int floor) {
		ArrayList<Tile> airLocks = new ArrayList<Tile>();
		ArrayList<Tile> list = getStationTilesOnFloor(floor);
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).isTouchingSpace()) {
				airLocks.add(list.get(i));
			}
		}
		return airLocks;
	}
}
