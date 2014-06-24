package org.iceburg.home.main;

import java.io.Serializable;
import java.util.ArrayList;

public class PlayerProfile implements Serializable {
	ArrayList<String> unlockedShips;

	public PlayerProfile() {
		unlockedShips = new ArrayList<String>();
		unlockShip("startShip");
	}
	public void unlockShip(String id) {
		getUnlockedShips().add(id);
	}
	public ArrayList<String> getUnlockedShips() {
		return unlockedShips;
	}
	private void setUnlockedShips(ArrayList<String> unlockedShips) {
		this.unlockedShips = unlockedShips;
	}
}
