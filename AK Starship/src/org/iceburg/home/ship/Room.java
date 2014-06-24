package org.iceburg.home.ship;

import java.util.ArrayList;

public class Room {
	ArrayList<Tile> roomTiles;

	public Room() {
		// this.roomNumber = roomNumber;
		roomTiles = new ArrayList<Tile>();
	}
	@Override
	public String toString() {
		return roomTiles.size() + " Tiles, starting with: " + getFirstTile();
	}
	/**
	 * Builds a room based on this source tile
	 */
	public static Room buildRoom(Tile startTile) {
		Room room = new Room();
		// don't go any further unless we have a non-null start tile!
		if (startTile != null) {
			ArrayList<Tile> closed = new ArrayList<Tile>();
			ArrayList<Tile> open = new ArrayList<Tile>();
			open.add(startTile);
			room.addTile(startTile);
			while (open.size() > 0) {
				// move the current tile to the closed list
				Tile current = open.get(0);
				open.remove(current);
				closed.add(current);
				// evaluate the neighbor squares
				ArrayList<Tile> tiles = current.findAOETilesFlat(1);
				for (int i = 0; i < tiles.size(); i++) {
					// old: tiles.get(i).isSafe()
					Tile testTile = tiles.get(i);
					if (testTile != null && testTile.isFlowTile()
							&& closed.contains(testTile) == false) {
						if (room.roomTiles.contains(testTile) == false) {
							room.addTile(testTile);
							// add the tile to the list of tiles we're searching
							// for, unless, it's a space tile
							if (testTile.isFlowTile()) {
								open.add(testTile);
							} else {
								closed.add(testTile);
							}
						}
					}
				}
			}
		}
		return room;
	}
	/**
	 * Combines this room with another room
	 */
	public void combineRooms(Tile source) {
		Room otherRoom = source.getParentRoom();
		// for goodness sakes don't add a room to itself!
		if (otherRoom != this) {
			for (int i = 0; i < otherRoom.getTiles().size(); i++) {
				Tile tile = otherRoom.getTiles().get(i);
				addTile(tile);
			}
			// add the doorway to the room
			addTile(source);
			removeRoomFromShip(otherRoom);
		}
	}
	/**
	 * Breaks this room into two seperate rooms
	 */
	public void seperateRooms(Tile tile1, Tile tile2) {
		addRoomtoShip(buildRoom(tile1));
		addRoomtoShip(buildRoom(tile2));
		removeRoomFromShip(this);
	}
	/**
	 * Returns the tile in this room with the lowest air pressure, or returns
	 * null if all tiles are the same pressure or returns null if the greatest
	 * pressure tile
	 * 
	 * @return
	 */
	public Tile findLowPressure() {
		Tile retTile = getFirstTile();
		Tile sample = retTile;
		int air = sample.getAirLevel();
		int maxAir = air;
		// start with the second tile and look at each tile's air
		for (int i = 1; i < getTiles().size(); i++) {
			sample = getTileAt(i);
			// if the sample has less air than the rettile, make this the new
			// source
			if (sample.getAirLevel() < air) {
				retTile = sample;
				air = sample.getAirLevel();
			}
			// if the sample has more air than max air, increase max air
			else if (sample.getAirLevel() > maxAir) {
				maxAir = sample.getAirLevel();
			}
		}
		// nullify low pressure ( making room stir not operate)
		// if difference in air is less than 10
		// or if the rettile is already full
		if (maxAir - air < 10 || retTile.getAirLevel() == 100) {
			retTile = null;
		}
		return retTile;
	}
	public Tile getTileAt(int i) {
		return getTiles().get(i);
	}
	public Tile getFirstTile() {
		return getTiles().get(0);
	}
	public Tile getLastTile() {
		return getTiles().get(getTiles().size() - 1);
	}
	public ArrayList<Tile> getTiles() {
		return roomTiles;
	}
	public void setTiles(ArrayList<Tile> tiles) {
		this.roomTiles = tiles;
	}
	/**
	 * Adds the tile to the room, setting the room as the tile's parentroom
	 */
	public void addTile(Tile tile) {
		getTiles().add(tile);
		tile.setParentRoom(this);
	}
	public Ship getParentShip() {
		return getFirstTile().getParentShip();
	}
	public void removeRoomFromShip(Room room) {
		getParentShip().getRooms().remove(room);
	}
	public void addRoomtoShip(Room room) {
		getParentShip().getRooms().add(room);
	}
}
