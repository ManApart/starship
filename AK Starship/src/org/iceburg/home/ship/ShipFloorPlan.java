package org.iceburg.home.ship;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.data.Data;
import org.iceburg.home.items.Item;
import org.iceburg.home.main.Constants;
import org.iceburg.home.main.Home;
import org.iceburg.home.main.StaticFunctions;
import org.iceburg.home.ship.systems.Doors;
import org.iceburg.home.ship.systems.Sensors;

public class ShipFloorPlan {
	// private String imageName = "textures/ship/blueprint.png";
	private BufferedImage bi, floorImg;
	private Tile[][] shipTiles;
	// private ArrayList<Tile> unBlockedTiles;
	// x and y are move values for shifting the floorplan around
	private int shiftX, shiftY;
	//is this floorlevel actually necessary?
	private int floorLevel;
	private Ship parentShip;
	private boolean calcImage;

	public ShipFloorPlan(Ship ship, int floorNumber, String imageName) {
		Image img = new ImageIcon(Home.resources.getClass().getResource(imageName)).getImage();
		bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
		bi.getGraphics().drawImage(img, 0, 0, null);
		floorLevel = floorNumber;
		parentShip = ship;
		shipTiles = Data.getTiles().parseBluePrint(ship, floorNumber, bi, this);
	}
	/**
	 * This constructer used when loading ShipFloorPlan Saves
	 */
	public ShipFloorPlan(Ship ship, int floorNumber) {
		floorLevel = floorNumber;
		parentShip = ship;
	}
	public void paintComponent(Graphics2D g, boolean showTarget) {
		// paint floorplan if the image is null or needs to be updated
		if (floorImg == null || calcImage) {
			calcImage();
		}
		g.drawImage(floorImg, null, 0, 0);
		// g.drawImage(floorImg, null, shiftX * Constants.shipSquare, shiftY *
		// Constants.shipSquare);
		// draw crew
		int max = getParentShip().getSensors().getMaxSensorLevel();
		if (max >= Sensors.crew) {
			boolean drawColor = true;
			if (max < Sensors.crewHealth) {
				drawColor = false;
			}
			for (int c = 0; c < getParentShip().crew.size(); c++) {
				CrewMan crew = getParentShip().crew.get(c);
				if (crew.floor == floorLevel) {
					crew.paint(g, shiftX * Constants.shipSquare, shiftY
							* Constants.shipSquare, drawColor);
				}
			}
		}
	}
	/**
	 * Tells the floorplan to recalculate it's image next time it is drawn.
	 */
	public void updateImage() {
		calcImage = true;
	}
	/**
	 * Calculates/ updates the tiles for this floorplan
	 */
	public void calcImage() {
		calcImage = false;
		int sensorLevel = getParentShip().getSensors().getMaxSensorLevel();
		// update sensor level for other draw functions
		// Home.getShip().getSensors().setMaxSensor(sensorLevel);
		// use a temp image to prevent screen tears (spelling? lol)
		BufferedImage img = new BufferedImage(Constants.viewScreenSize, Constants.viewScreenSize, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D gg = img.createGraphics();
		for (int i = 0; i < shipTiles.length; i++) {
			// don't draw columns that are out of the screen
			if ((i + getShiftX()) * Constants.shipSquare >= Constants.viewScreenSize
					|| (i + getShiftX()) * Constants.shipSquare < 0) {
				continue;
			}
			// draw tiles
			for (int j = 0; j < shipTiles[i].length; j++) {
				// don't tiles (rows) that are out of the screen
				if ((j + getShiftY()) * Constants.shipSquare < 0
						|| (j + getShiftY()) * Constants.shipSquare > Constants.viewScreenSize) {
					continue;
				}
				// TODO - transfer the offset to offsetting the whole photo
				// instead of each tile
				// then clean code so tile draws itself at its own x,y
				// shipTiles[i][j].paintComponent(gg, sensorLevel, i*
				// Constants.shipSquare,j* Constants.shipSquare);
				Tile temp = shipTiles[i][j];
//				if ( temp== null){
//					System.out.println("ShipFloorPlan: null Tile");
//				}
					temp.paintComponent(gg, sensorLevel, (int) ((i + shiftX) * Constants.shipSquare), (int) ((j + shiftY) * Constants.shipSquare));
//				}
			}
		}
		floorImg = img;
	}
	/**
	 * Set's this floorplan so that it has 1 row and 1 column of space tiles before and after the furthest ship tiles
	 */
	public void cropFloorPlan(){
		//find first and last tiles
		//init start and end tiles
		int x1 = -1, x2 = -1;
		int y1 = -1, y2 = -1;
		for (int i = 0; i < shipTiles.length; i++) {
			for (int j = 0; j < shipTiles[i].length; j++) {
				//if it's not a space tile
				if (!shipTiles[i][j].isSpaceTile()){
					//set first only first time
					if (x1 == -1){
						x1 = i;
					}
					//if we find a j value later that is smaller than y1, that should be our new start
					if (y1 == -1 || y1 > j){
						y1 = j;
					}
					//keep setting end value
					x2 = i;
					//if we find a j value later that is greater than y2, that should be our new end
					if (j > y2){
						y2 = j;
					}
				}
			}
		}
		//now we have the start and end values
		//if this is a new size, let's resize (add 2, for a 1 buffer on either side)
		//add 3 to our size, 1 on either side, and 1 to account for the 0 tile.
		int a = 3;
		if (!(shipTiles.length == x2-x1 + a && shipTiles[0].length == y2-y1 + a)){
			//create a new floorplan of proper size
			Tile[][] newPlan = new Tile[x2-x1+ a][y2-y1 + a];
			
			//copy only the rows/columns we want over, but use new x,y to copy to begenning of newplan (0 rows/columns should be null)
			//TODO -proper bounds!
			int x = 0, y =0;
			Tile temp = null;
			//start before first, do space, go till 1 after last
			for (int i = x1-1; i <= x2 + 1; i++) {
				y =0;
				for (int j = y1-1; j <= y2 + 1; j++) {
					//reset temp to null
					temp = null;
					//if shiptiles contains this tile transfer it
					if (i >=0 && j>=0 && i <shipTiles.length && j <shipTiles[i].length){
						temp = shipTiles[i][j];
					}
					//else turn any null tiles into space tiles
					if (temp == null){
						temp = Tile.spaceTile();
					}
					//update tile values
					newPlan[x][y] = temp;
					temp.setX(x);
					temp.setY(y);
					temp.setParentFloor(getFloorLevel());
					temp.setParentFloorPlan(this);
					//update actor position
					if (temp.getActor() != null){
						temp.getActor().setxPos(temp.getX());
						temp.getActor().setyPos(temp.getY());
					}
					y++;
				}
				x ++;
			}
			setShipTiles(newPlan);
		}

	}
	/**
	 * Returns the tile at the given x and y position
	 */
	public Tile getTileAt(int x, int y) {
		if (x >= 0 && y >= 0 && x < shipTiles.length && y < shipTiles[x].length) {
			return shipTiles[x][y];
		}
		return null;
	}
	/**
	 * Return random tile - could be null or space
	 */
	private Tile getRandomTile() {
		int x = StaticFunctions.randRange(0, getMaxX());
		int y = StaticFunctions.randRange(0, getMaxY());
		return shipTiles[x][y];
	}
	/**
	 * Return random tile - won't be null (unless failed) or space will stop
	 * looking after 500 tries
	 */
	public Tile getRandomShipTile() {
		Tile ret = null;
		int i = 0;
		while (ret == null || i > 500) {
			ret = getRandomTile();
		}
		return ret;
	}
	/**
	 * Return random, safe tile - won't be null (unless failed) or space will stop
	 * looking after 500 tries
	 */
	public Tile getRandomSafeShipTile() {
		Tile ret = null;
		int i = 0;
		while ((ret == null || (ret != null && !ret.isSafe()))|| i > 500) {
			ret = getRandomTile();
		}
		return ret;
	}
	/**
	 * Returns whether the tile at position x, y is on the edge of the floorplan
	 */
	public boolean isEdgeTile(int x, int y) {
		if (x == getMaxX() || y == getMaxY() || x == 0 || y == 0) {
			return true;
		}
		return false;
	}
	/**
	 * Returns whether the tile at position x, y is on the edge of the floorplan
	 */
	public boolean isEdgeTile(Tile tile) {
		if (tile.getX() == getMaxX() || tile.getY() == getMaxY() || tile.getX() == 0
				|| tile.getY() == 0) {
			return true;
		}
		return false;
	}
	// TODO - add crew to available tile
	public void addCrewToTiles(ArrayList<CrewMan> list) {
		// int j = 0;
		for (int i = 0; i < list.size(); i++) {
			this.shipTiles[list.get(i).getxPos()][list.get(i).getyPos()].setActor(list.get(i));
			// j =+ 1;
		}
	}
	/**
	 * Places the item at the goal, if possible
	 */
	public void placeTile(Tile goal, Item item){
		// check that the man is touching the tile and that the tile is valid
		if (item != null && goal != null
				&& (goal.getHealth() >= goal.getHealthTotal() || goal.isSpaceTile())) {
			// tile is space and crewman has floor or a floor with full health
			if ((item.isFloor() && goal.isSpaceTile()) || goal.getItem().isFloor()) {
				// if the floor doesn't match ship default store id of floor with tile
				// so when it's removed later the proper floor is displayed
				// if we're placing something over a floor, remember it's base id
				if (goal.getItem() != null && goal.getItem().isFloor()) {
					goal.setUnderItem(goal.getItem().getId());
				}
				goal.cloneUpdate(item.getItemType());
				goal.setItem(item);
				goal.connectToProperSystem();
				goal.updateImages();
				//only update on place in space
				if (goal.isSpaceTile()){
					cropFloorPlan();
				}
			}
		}
	}
	// Much thanks to Kevin Glass's tutorial at
	// http://www.cokeandcode.com/main/tutorials/path-finding/
	/**
	 * Finds the distance that each tile is from the target location
	 * 
	 * @param loc
	 * @return
	 */
	public static HashMap<Tile, Integer> findTileCosts(Tile tile) {
		ArrayList<Tile> closed = new ArrayList<Tile>();
		ArrayList<Tile> open = new ArrayList<Tile>();
		Ship ship = tile.getParentShip();
		int[] loc = tile.getLocation();
		HashMap<Tile, Integer> path = new HashMap<Tile, Integer>();
		// Loc[0] = floor, 1 = x, 2= y;
		// System.out.println("Finding path");
		ShipFloorPlan startFloor = ship.getFloorplans().get(loc[0]);
		closed.clear();
		int cost = 0;
		open.add(startFloor.getTileAt(loc[1], loc[2]));
		path.put(startFloor.getTileAt(loc[1], loc[2]), 0);
		while (open.size() > 0) {
			// move the current tile to the closed list
			Tile current = open.get(0);
			open.remove(current);
			closed.add(current);
			cost = path.get(current) + 1;
			// evaluate the neighbor squares
			ArrayList<Tile> tiles = current.findAOETilesFlat(1);
			for (int i = 0; i < tiles.size(); i++) {
				Tile testTile = tiles.get(i);
				if ((testTile.isSafe() || testTile.getTileColor().equals(Doors.systemMain))
						&& closed.contains(testTile) == false) {
					if (path.containsKey(testTile) == false || path.get(testTile) > cost) {
						path.put(testTile, cost);
						open.add(testTile);
					}
				}
			}
		}
		Home.crewPath = path;
		return path;
		// System.out.println("Found path");
	}
	// TODO remove this once travel updated
	public static HashMap<Tile, Integer> findTileCosts(Ship ship, int[] loc) {
		return findTileCosts(ship.getTileAt(loc));
	}
	/**
	 * Find's an edge tile at the input angle, relative to the center of the
	 * screen.
	 */
	public Tile findEdgeTileAtAngle(int angle) {
		int w = getShipTiles().length;
		int h = getShipTiles()[0].length;
		Tile centerTile = getTileAt(w / 2, h / 2);
		return findEdgeTileAtAngle(angle, centerTile);
	}
	/**
	 * Find's an edge tile at the input angle, relative to the source tile.
	 */
	public Tile findEdgeTileAtAngle(int angle, Tile centerTile) {
		// if centertile is null, use the center of the screen
		if (centerTile == null) {
			return findEdgeTileAtAngle(angle);
		}
		int x = 0;
		int y = 0;
		int l = 0;
		while (centerTile.getX() + x <= getMaxX() && centerTile.getX() + x >= 0
				&& centerTile.getY() + y <= getMaxY() && centerTile.getY() + y >= 0) {
			Tile tempTile = getTileAt(centerTile.getX() + x, centerTile.getY() + y);
			// if this is an edge tile, exit loop
			if (isEdgeTile(tempTile)) {
				return tempTile;
			}
			// draw the next 'pixel' of the line
			x = (int) (l * Math.cos(Math.toRadians(angle - 90)));
			y = (int) (l * Math.sin(Math.toRadians(angle - 90)));
			l += 1;
		}
		return null;
	}
	public Tile[][] getShipTiles() {
		return shipTiles;
	}
	public void setShipTiles(Tile[][] shipTiles) {
		this.shipTiles = shipTiles;
	}
	/**
	 * Find's the tile on this floorplan with the least health
	 */
	public Tile findLowestHealthTile() {
		Tile ret = null;
		int cost = 100;
		for (int i = 0; i < shipTiles.length; i++) {
			for (int j = 0; j < shipTiles[i].length; j++) {
				if (shipTiles[i][j].getHealth() < cost) {
					Tile temp = shipTiles[i][j];
					if (temp != null && !temp.isSpaceTile() && temp.hasSafeNeighbor()) {
						if (temp.getHealth() <= 0) {
							return temp;
						}
						ret = temp;
						cost = temp.getHealth();
					}
				}
			}
		}
		return ret;
	}
	// public ArrayList<Tile> getUnBlockedTiles() {
	// return unBlockedTiles;
	// }
	// public void setUnBlockedTiles(ArrayList<Tile> unBlockedTiles) {
	// this.unBlockedTiles = unBlockedTiles;
	// }
	public int getFloorLevel() {
		return floorLevel;
	}
	public void setFloorLevel(int floorLevel) {
		this.floorLevel = floorLevel;
	}
	/**
	 * Get the max X index
	 * 
	 * @return
	 */
	public int getMaxX() {
		return shipTiles.length - 1;
	}
	/**
	 * Get the max Y index
	 * 
	 * @return
	 */
	public int getMaxY() {
		return shipTiles[0].length - 1;
	}
	public Ship getParentShip() {
		return parentShip;
	}
	public void setParentShip(Ship parentShip) {
		this.parentShip = parentShip;
	}
	public int getShiftX() {
		return shiftX;
	}
	public void setShiftX(int shiftX) {
		this.shiftX = shiftX;
	}
	public void incShiftX(int amount) {
		setShiftX(getShiftX() + amount);
	}
	public void incShiftY(int amount) {
		setShiftY(getShiftY() + amount);
	}
	public int getShiftY() {
		return shiftY;
	}
	public void setShiftY(int shiftY) {
		this.shiftY = shiftY;
	}
}
