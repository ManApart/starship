package org.iceburg.home.ship.systems;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JSlider;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.items.Item;
import org.iceburg.home.items.Shield;
import org.iceburg.home.main.Home;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.ui.TilePane;
import org.iceburg.home.ui.UIMisc;

public class Shields extends ShipSystem {
	public static Color systemMain = Color.decode("#9dfffe");
	public static Color shieldShade = Color.decode("#9dfffe");
	JSlider jsFrequency;

	// public HashMap<Tile, Item> shieldMap;
	public Shields(Ship ship) {
		super(ship);
		this.tileTypes.add(systemMain);
		// this.shieldMap = new HashMap<Tile, Item>();
		this.name = "Shields";
		shieldShade = new Color(systemMain.getRed(), systemMain.getGreen(), systemMain.getBlue(), 200);
	}
	@Override
	public void paintTileInfo(TilePane p, Item item) {
		Shield w = (Shield) item;
		int sLevel = getParentShip().getSensors().getMaxSensorLevel();
		if (sLevel >= Sensors.shieldFreq) {
			UIMisc.addEnergyBar(item.getParentTile(), null, p);
		}
		if (isManned() && !item.isPowered()) {
			jsFrequency = UIMisc.addSlider("Shield Frequency:", p, 5, w.getFrequency());
		} else if (sLevel >= Sensors.shieldFreq) {
			UIMisc.addJLabel("Shield Frequency: " + w.getFrequency(), p);
		}
	}
	@Override
	public void paintTileMultiInfo(TilePane p, Item item) {
		Shield w = (Shield) item;
		UIMisc.addEnergyBar(item.getParentTile(), null, p);
		if (isManned()) {
			jsFrequency = UIMisc.addSlider("Shield Frequency:", p, 5, w.getFrequency());
		}
	}
	@Override
	public void updateSystem() {
		// Let's not have the shields
		if (timer > 10) {
			timer = 0;
			ArrayList<Item> list = getSystemItems();
			for (int i = 0; i < list.size(); i++) {
				((Shield) list.get(i)).update();
			}
		} else {
			timer += 1;
		}
	}
	@Override
	public void powerSystemOn(Tile station) {
		// System.out.println("Shields Powered");
		Shield s = (Shield) station.getItem();
		// if (s.getBufferHealth() > 0){
		ArrayList<Tile> list = s.getShieldedTiles();
		// get shield tiles
		ArrayList<Tile> tiles = station.findAOETiles3dCircleHollow(s.getAOE());
		// test if shield is compatible - for each tile
		for (int i = 0; i < tiles.size(); i++) {
			Tile tile = tiles.get(i);
			ArrayList<Shield> otherShields = tile.getShields();
			for (int j = 0; j < otherShields.size(); j++) {
				Shield os = otherShields.get(j);
				if (os.getFrequency() != s.getFrequency()) {
					Home.messagePanel.addMessage("Incompatable frequencies!");
					s.setCurrentPower(0);
					return;
				}
			}
		}
		// add shield layers
		for (int i = 0; i < tiles.size(); i++) {
			tiles.get(i).addShieldLayer(s);
			// add to the shielded tiles list
			list.add(tiles.get(i));
		}
		if (Home.getCurrentScreen() != null
				&& station == Home.getCurrentScreen().getCurrentTile()) {
			Home.getCurrentScreen().getCurrentTilePane().updatePane();
		}
		// TODO - update image to show shields on per tile basis?
		Home.getCurrentScreen().updateCurrentFloorPlanImage();
		powerUpSound.play();
		// }
	}
	@Override
	public void powerSystemOff(Tile station) {
		Shield s = (Shield) station.getItem();
		// if (s.getBufferHealth() <= 0){
		// get shield tiles
		ArrayList<Tile> tiles = s.getShieldedTiles();
		// old way could take longer and be less accurate
		// ArrayList<Tile> tiles =
		// station.findAOETiles3dCircleHollow(s.getAOE());
		// remove this shield layer
		for (int i = 0; i < tiles.size(); i++) {
			tiles.get(i).removeShieldLayer(s);
		}
		if (Home.getCurrentScreen() != null
				&& station == Home.getCurrentScreen().getCurrentTile()) {
			Home.getCurrentScreen().getCurrentTilePane().updatePane();
		}
		Home.getCurrentScreen().updateCurrentFloorPlanImage();
		powerDownSound.play();
		// }
	}
	@Override
	public void stateChanged(JSlider o, Tile tile) {
		if (o == jsFrequency) {
			// System.out.println("slide value: " + slide.getValue());
			// if frequncy change fails, change the slider back
			if (!attemptFrequencyChange(getShieldFrom(tile), o.getValue())) {
				o.setValue(getShieldFrom(tile).getFrequency());
			}
		}
	}
	@Override
	public void manSystemAuto(CrewMan man) {
		Tile tile = man.getCurrentTile();
		if (tile.isPowered()) {
		} else if (tile.getHealth() > 10) {
			// make all frequencies match
			((Shield) tile.getItem()).setFrequency(((Shield) getFirstItem()).getFrequency());
			tile.getParentShip().getEngineering().attemptPowerSystem(tile.getItem());
		}
	}
	public boolean attemptFrequencyChange(Shield s, int freq) {
		// look at all of the shielded tiles and return false if any of them
		// have a frequency other than newFreq
		if (compatibleFrequenciesAll(s, freq)) {
			s.setFrequency(freq);
			return true;
		}
		return false;
	}
	/**
	 * Return's whether this tile's shield overlays are the same frequency as
	 * the input frequency
	 */
	private boolean compatibleFrequencies(Shield s, int freq, Tile tile) {
		ArrayList<Shield> otherShields = tile.getShields();
		if (otherShields.size() > 0) {
			for (int j = 0; j < otherShields.size(); j++) {
				Shield os = otherShields.get(j);
				// not compatible if os isn't the target frequency and isn't the
				// source shield
				if (os.getFrequency() != freq && os != s) {
					Home.messagePanel.addMessage("Incompatable frequencies!");
					return false;
				}
			}
		}
		return true;
	}
	/**
	 * Takes a shield and check's all of it's shielded tiles for compatability
	 * with the new frequency
	 */
	public boolean compatibleFrequenciesAll(Shield s, int freq) {
		// Shield s = getShieldFrom(tile);
		ArrayList<Tile> tiles = s.getShieldedTiles();
		// test if shield is compatible - for each tile
		if (tiles.size() > 0) {
			for (int i = 0; i < tiles.size(); i++) {
				if (!compatibleFrequencies(s, freq, tiles.get(i))) {
					return false;
				}
			}
		}
		return true;
	}
	public Shield getShieldFrom(Tile tile) {
		return (Shield) tile.getItem();
	}
}
