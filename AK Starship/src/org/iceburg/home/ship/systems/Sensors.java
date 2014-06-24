package org.iceburg.home.ship.systems;

import java.awt.Color;
import java.awt.Graphics2D;

import org.iceburg.home.main.Constants;
import org.iceburg.home.main.Home;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.ship.Tile;

public class Sensors extends ShipSystem {
	public static Color systemMain = Color.decode("#1f3da5");
	// int maxSensor;
	public static int allGray = 0, floorVsWall = 1, systemVsBuild = 2, systemColor = 3,
			buildName = 4, systemName = 5, crew = 6, buildHealth = 7, systemHealth = 8,
			crewHealth = 9, shieldFreq = 10;

	public Sensors(Ship ship) {
		super(ship);
		this.tileTypes.add(systemMain);
		this.name = "Sensors";
	}
	/**
	 * Return's max sensors if we're looking at our own ship, or the our sensor
	 * level if we're looking at a different ship
	 */
	public int getMaxSensorLevel() {
		if (Home.creativeMode || getParentShip() == Home.getShip()
				|| !Home.getBattleLoc().isInBattle()) {
			return 10;
		} else {
			return Home.getShip().getSensors().getHighestEffectivenessLevel();
		}
	}
	/**
	 * Draw the input tile based on the input sensor level Moved here from tile
	 * to reduce size of tile code and make this more clear sense it's a sensor
	 * sort of thing
	 */
	public static void drawTile(Graphics2D g, Tile tile, int x, int y, int level) {
		// if above systemcolor level, draw all
		if (tile.isSystemTile()) {
			if (level >= systemName) {
				tile.drawTileImage(g, x, y);
			} else if (level >= systemColor) {
				g.setColor(tile.getTileColor());
				g.fillRect(x, y, Constants.shipSquare, Constants.shipSquare);
			} else if (level == systemVsBuild) {
				g.setColor(Color.blue);
				g.fillRect(x, y, (int) Constants.shipSquare, (int) Constants.shipSquare);
			} else if (level == floorVsWall) {
				if (tile.isBlocked()) {
					g.setColor(Color.decode("#8b8b8b"));
				} else {
					g.setColor(Color.decode("#ffffff"));
				}
				g.fillRect(x, y, (int) Constants.shipSquare, (int) Constants.shipSquare);
			} else {
				g.setColor(Color.decode("#8b8b8b"));
				g.fillRect(x, y, (int) Constants.shipSquare, (int) Constants.shipSquare);
			}
		} else if (tile.isBuildTile()) {
			if (level >= buildName) {
				tile.drawTileImage(g, x, y);
			} else if (level >= systemColor) {
				g.setColor(tile.getTileColor());
				g.fillRect(x, y, Constants.shipSquare, Constants.shipSquare);
			} else if (level >= floorVsWall) {
				g.setColor(tile.getTileColor());
				g.fillRect(x, y, (int) Constants.shipSquare, (int) Constants.shipSquare);
			} else {
				g.setColor(Color.decode("#8b8b8b"));
				g.fillRect(x, y, (int) Constants.shipSquare, (int) Constants.shipSquare);
			}
			// don't draw space, that way we can see projectiles below
			// } else {
			// if (tile.isSpaceTile())
			// g.setColor(Color.black);
			// g.fillRect(x, y, (int) Constants.shipSquare, (int)
			// Constants.shipSquare);
		}
	}
}
