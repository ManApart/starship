package org.iceburg.home.ship;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.items.Door;
import org.iceburg.home.items.Item;
import org.iceburg.home.items.Shield;
import org.iceburg.home.items.Weapon;
import org.iceburg.home.main.Constants;
import org.iceburg.home.main.Home;
import org.iceburg.home.main.StaticFunctions;
import org.iceburg.home.ship.systems.Doors;
import org.iceburg.home.ship.systems.Sensors;
import org.iceburg.home.ship.systems.Shields;
import org.iceburg.home.ship.systems.ShipSystem;
import org.iceburg.home.ship.systems.Weapons;
import org.iceburg.home.ui.GameScreen;
import org.iceburg.home.ui.TilePane;

//TODO - room tiles expand and floodfill the room
public class Tile {
	// under item is the item id of the item under this tile's item, for
	// placeing and taking items
	private String name, underItem;
	private boolean blocked;
	private int x, y, airLevel;
	private Color tileType, altColor;
	// TODO The actor and man should be combined, but in general actor is
	// uesed for location while man is used for manning stations.
	// Combining them led to turbo tube failure, so back up before trying again
	private CrewMan man, actor;
	// item is the item it contains- the weapon, wall type, resource, or flora
	private Item item;
	private ArrayList<Shield> shields;
	private ShipSystem parentSystem;
	private int parentFloor;
	private ShipFloorPlan parentFloorPlan;
	private Room parentRoom;

	public Tile() {
		// stuff that should be copied during clone
		this.name = "Null!";
		this.blocked = false;
		this.airLevel = 0;
		this.tileType = Color.decode("#e3152c");
		this.actor = null;
		// stuff that need not be copied during clone
		this.shields = new ArrayList<Shield>();
	}
	public void cloneTile(Tile origional) {
		this.name = origional.getName();
		this.blocked = origional.isBlocked();
		this.airLevel = origional.getAirLevel();
		this.tileType = origional.getTileColor();
		this.actor = origional.getActor();
		// setParentSystem(origional.getParentSystem());
	}
	/**
	 * Like cloneTile, except runs after initialization. Used when crew
	 * take/place tiles. it does not update the actor or air level, also updates
	 * rooms
	 */
	public void cloneUpdate(Color baseColor) {
		Tile origional = Home.resources.tiles.getBaseTileFromColor(baseColor);
		setName(origional.getName());
		setBlocked(origional.isBlocked());
		setTileColor(origional.getTileColor());
		// uninstall the old system before setting to the new
		if (hasParentSystem()) {
			getParentSystem().unInstallItem(this);
		}
		// does not install the tile on the system.
	}
	/**
	 * Finds the proper system and connects this tile to it
	 */
	public void connectToProperSystem(Ship ship) {
		// Connect the tile to the proper system
		for (int t = 0; t < ship.getShipSystems().size(); t++) {
			ShipSystem sys = ship.shipSystems.get(t);
			if (sys.getTileTypes().size() > 0 && sys.includesTileType(getTileColor())) {
				sys.installItem(this);
				break;
			}
		}
	}
	public void connectToProperSystem() {
		connectToProperSystem(getParentShip());
	}
	public void paintComponent(Graphics2D g, int sensorLevel, int x, int y) {
		int cs = Constants.shipSquare;
		// draw alt color
		if (this.getAltColor() != null && Home.displayAlt) {
			g.setColor(getAltColor());
			g.fillRect(x, y, (int) cs, (int) cs);
		}
		// draw tile/image, based on sensor level
		else {
			Sensors.drawTile(g, this, x, y, sensorLevel);
		}
		// draw shields if needed
		if (hasShieldLayerOn()) {
			g.setColor(Shields.shieldShade);
			g.fillRect(x, y, (int) cs, (int) cs);
		}
		// draw low air
		if (sensorLevel >= Sensors.buildName && getAirLevel() < 50 && isFlowTile()) {
			int a = (50 - getAirLevel()) * 4;
			g.setColor(new Color(255, 0, 0, a));
			g.fillRect(x, y, (int) cs, (int) cs);
		}
		// debug, crew path
		if (Home.displayCrew && Home.crewPath != null) {
			g.setColor(Color.black);
			g.setFont(new Font("Helvetica", Font.BOLD, 7));
			if (Home.crewPath.containsKey(this)) {
				int cost = Home.crewPath.get(this);
				g.drawString("" + cost, x, y + cs / 2);
			}
		}
		// debug, draw air level
		else if (Home.displayAir) {
			if (getTileColor().equals(Color.white)) {
				g.setColor(Color.black);
			} else {
				g.setColor(Color.white);
			}
			g.setFont(new Font("Helvetica", Font.BOLD, 7));
			if (getAirLevel() > 0) {
				g.drawString("" + getAirLevel(), x, y + cs / 2);
			}
		}
		if (Home.displayGrid){
			g.setColor(Color.black);
			g.drawRect(x, y, cs, cs);
			g.setColor(Color.white);
			g.drawRect(x+1, y+1, cs-1, cs-1);
			if (getX() >= 0) {
				g.drawString("" + getY(), 0, y + cs);
			}
			if (getY() >= 0) {
				g.drawString("" + getX(),  x, cs);
			}
		}
	}
	/**
	 * Draw this tile if we have an image for it, otherwise color it default
	 * color
	 */
	public void drawTileImage(Graphics2D g, int x, int y) {
		if (getItem() != null && getItem().getImage() != null) {
			Image img = getItem().getImage();
			if (!isBlocked() && getItem() instanceof Door) {
				img = ((Door) getItem()).getOpenImage();
			} else if (getItem() instanceof Weapon) {
				img = ((Weapon) getItem()).getImageRotate();
			}
			g.drawImage(img, x, y, Constants.shipSquare, Constants.shipSquare, null);
		}
		// draw default color
		else {
			g.setColor(getTileColor());
			g.fill3DRect(x, y, (int) Constants.shipSquare, (int) Constants.shipSquare, true);
		}
	}
	@Override
	public String toString() {
		return name + ", f:" + parentFloor + ", x:" + x + ", y:" + y;
	}
	
	public static Tile spaceTile(){
		Tile ret = new Tile();
		ret.cloneTile(Home.resources.getTiles().getBaseTileFromColor(Color.black));
		return ret;
	}
	/**
	 * Set's the tile's item's draw image based on the image path and the tile's
	 * relation to other tiles
	 */
	// TODO - add to crew install function
	public void setItemImage() {
		Item item = getItem();
		Image baseImage = null;
		Image baseImageOpen = null;
		if (item != null && item.getImagePath() != null) {
			Door door = null;
			if (item instanceof Door) {
				door = (Door) item;
			}
			// same item for all tiles of type, need to use clones and not
			// repeat root
			try {
				baseImage = new ImageIcon(Home.resources.getClass().getResource(item.getImagePath())).getImage();
				if (door != null) {
					baseImageOpen = new ImageIcon(Home.resources.getClass().getResource(door.getOpenImagePath())).getImage();
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error parsing image of " + item.getId());
			}
			// if the item is a weapon, just grab the image (and set as rotate
			// img as well)
			if (item.getItemType().equals(Weapons.systemMain)
					|| item.getItemType().equals(Weapons.colorWeaponsMissile)) {
				item.setImage(baseImage);
				((Weapon) item).updateRotateImage();
			}
			// item.setImage(baseImage);
			else {
				// setup
				// the size of each sub-tile
				int size = baseImage.getWidth(null) / 3;
				BufferedImage newImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
				Graphics g = newImage.createGraphics();
				// calculate which sub image we need to retrieve
				// figure out what tiles surround this guy
				ShipFloorPlan floor = getParentFloorPlan();
				int x = getX();
				int y = getY();
				int tiles = 0;
				int corner = 0;
				int corner2 = 0;
				boolean isCorner = false;
				// is the tile to this compass direction the same as this one?
				boolean n = false;
				boolean s = false;
				boolean e = false;
				boolean w = false;
				int width = 0;
				int height = 0;
				Tile tile = floor.getTileAt(x, y - 1);
				if (tile != null && tile.getItem() != null
						&& item.isSameBaseItem(tile.getItem())) {
					tiles += 1;
					corner2 = 0;
					n = true;
				} else {
					corner2 += 1;
				}
				tile = floor.getTileAt(x + 1, y);
				if (tile != null && tile.getItem() != null
						&& item.isSameBaseItem(tile.getItem())) {
					tiles += 1;
					corner = 0;
					e = true;
				} else {
					corner += 1;
				}
				if (corner + corner2 == 2) {
					isCorner = true;
				}
				tile = floor.getTileAt(x, y + 1);
				if (tile != null && tile.getItem() != null
						&& item.isSameBaseItem(tile.getItem())) {
					tiles += 1;
					corner = 0;
					s = true;
				} else {
					corner += 1;
				}
				if (corner == 2) {
					isCorner = true;
				}
				tile = floor.getTileAt(x - 1, y);
				if (tile != null && tile.getItem() != null
						&& item.isSameBaseItem(tile.getItem())) {
					corner = 0;
					tiles += 1;
					w = true;
				} else {
					corner += 1;
				}
				if (corner + corner2 == 2) {
					isCorner = true;
				}
				// retrieve that sub image
				// tiles of the same type surround this guy
				int rotate = 0;
				if (tiles == 4) {
					// g.drawImage(baseImage, 0, 0, null);
					width = 0;
					height = 0;
				}
				// single line
				else if (tiles == 3) {
					// g.drawImage(baseImage, -size, 0, null);
					width = -size;
					height = 0;
					if (e == false) {
						rotate = 90;
					} else if (s == false) {
						rotate = 180;
					} else if (w == false) {
						rotate = 270;
					}
				}
				// Corner
				else if (tiles == 2 && isCorner == true) {
					// g.drawImage(baseImage, -size * 2, 0, null);
					width = -size * 2;
					height = 0;
					// TODO
					if (e == false && s == false) {
						rotate = 90;
					} else if (s == false && w == false) {
						rotate = 180;
					} else if (w == false && n == false) {
						rotate = 270;
					}
				}
				// two parrallel lines
				else if (tiles == 2) {
					// g.drawImage(baseImage, 0, -size, null);
					width = 0;
					height = -size;
					if (e == false) {
						rotate = 90;
					}
				}
				// u shape
				else if (tiles == 1) {
					// g.drawImage(baseImage, -size, -size, null);
					width = -size;
					height = -size;
					if (n == true) {
						rotate = 90;
					} else if (e == true) {
						rotate = 180;
					} else if (s == true) {
						rotate = 270;
					}
				}
				// No tiles like this one surround this tile
				else if (tiles == 0) {
					// g.drawImage(baseImage, -size * 2, -size, null);
					width = -size * 2;
					height = -size;
				}
				// now draw the image
				g.drawImage(baseImage, width, height, null);
				// item.setImage(newImage);
				item.setImage(StaticFunctions.rotateImage(newImage, rotate));
				// add open image to door
				if (door != null) {
					g.drawImage(baseImageOpen, width, height, null);
					door.setOpenImage(StaticFunctions.rotateImage(newImage, rotate));
				}
				g.dispose();
			}
		}
	}
	/**
	 * Finds the tile at x dist
	 * 
	 * @param retTiles
	 * @param floorPlan
	 * @param x
	 *            - horizontal area
	 * @param y
	 *            - vertical area
	 * @param xDist
	 *            -horizontal adjustment (for diagnols)
	 * @param yDist
	 *            -vertical adjustment (for diagnols)
	 */
	public void addTiletoList(ArrayList<Tile> retTiles, int x, int y, int xDist, int yDist) {
		int xVal = getX() + x + xDist;
		int yVal = getY() + y + yDist;
		if (xVal >= 0 && yVal >= 0 && xVal < getParentFloorPlan().getShipTiles().length
				&& yVal < getParentFloorPlan().getShipTiles()[0].length) {
			Tile prospect = getParentFloorPlan().getTileAt(xVal, yVal);
			// if not in the list, add the non-empty tile
			if (!retTiles.contains(prospect) && prospect != null) {
				retTiles.add(prospect);
			}
		}
	}
	/**
	 * Finds an adjacent tile that is safe and not manned
	 * 
	 * @return
	 */
	public Tile findNearestVacantTile() {
		Tile retTile = null;
		Tile current = this;
		ArrayList<Tile> open = new ArrayList<Tile>();
		ArrayList<Tile> closed = new ArrayList<Tile>();
		open.add(current);
		while (open.size() > 0 && retTile == null) {
			// move the current tile to the closed list
			current = open.get(0);
			open.remove(current);
			closed.add(current);
			// evaluate the neighbor squares
			ArrayList<Tile> tiles = current.findAOETilesFlat(1);
			for (int i = 0; i < tiles.size(); i++) {
				if (tiles.get(i).isSafe() && closed.contains(tiles.get(i)) == false) {
					open.add(tiles.get(i));
					if (tiles.get(i).isVacant()) {
						retTile = tiles.get(i);
						return retTile;
					}
				}
			}
		}
		return retTile;
	}
	/**
	 * Return's the safe tile adjacant to this tile, that is nearest to input
	 * man
	 * 
	 */
	public Tile findSafeAdjacentTile(Tile start) {
		// Tile start = man.getLocationTile();
		Tile retTile = null;
		HashMap<Tile, Integer> crewPath = start.getParentFloorPlan().findTileCosts(start);
		ArrayList<Tile> tiles = findAOETilesFlat(1);
		int cost = -1;
		for (int i = 0; i < tiles.size(); i++) {
			Tile temp = tiles.get(i);
			// only worry about safe tiles
			if (temp.isSafe()) {
				if (crewPath.containsKey(temp)) {
					// set ret if cost isn't inited, or if it is smaller than
					// the rettile cost
					if (crewPath.get(temp) < cost || cost == -1) {
						retTile = temp;
						// update cost
						cost = crewPath.get(temp);
					}
				}
				// even if we don't have a proper crew path, return a safe tile
				else if (retTile == null) {
					retTile = temp;
				}
			}
		}
		return retTile;
	}
	/**
	 * Finds an adjacent tile that is safe to walk on This is faster than
	 * inputing a start tile and finding the closest
	 */
	public Tile findSafeAdjacentTile() {
		Tile retTile = null;
		// the new clean way TODO finish others
		ArrayList<Tile> tiles = findAOETilesFlat(1);
		for (int i = 0; i < tiles.size(); i++) {
			if (tiles.get(i).isSafe()) {
				retTile = tiles.get(i);
			}
		}
		return retTile;
	}
	/**
	 * Returns the tile that is opposite this tile (with the othertile being in
	 * the middle)
	 */
	public Tile findOppositeTile(Tile otherTile) {
		int x = getX() - otherTile.getX();
		int y = getY() - otherTile.getY();
		x = -x;
		y = -y;
		return getParentFloorPlan().getTileAt(otherTile.getX() + x, otherTile.getY() + y);
	}
	/**
	 * Finds an adjacent tile that is safe to walk on Don't include the center
	 * tile
	 */
	public Tile findSafeAdjacentTileHollow() {
		Tile retTile = null;
		// the new clean way TODO finish others
		ArrayList<Tile> tiles = findAOETilesFlatHollow(1);
		for (int i = 0; i < tiles.size(); i++) {
			if (tiles.get(i).isSafe()) {
				retTile = tiles.get(i);
			}
		}
		return retTile;
	}
	/**
	 * Finds an adjacent tile that is safe to walk on does not include this tile
	 * 
	 * @param loc
	 * @return
	 */
	public Tile findTouchingTile() {
		Tile retTile = null;
		// the new clean way TODO finish others
		ArrayList<Tile> tiles = findAOETilesFlat(1);
		for (int i = 0; i < tiles.size(); i++) {
			if (tiles.get(i).isSafe() && tiles.get(i) != this) {
				retTile = tiles.get(i);
			}
		}
		return retTile;
	}
	/**
	 * Updates this tile's image, as well as the images of the tile surrounding
	 * it
	 */
	public void updateImages() {
		ArrayList<Tile> tiles = findAOETilesFlat(1);
		for (int i = 0; i < tiles.size(); i++) {
			tiles.get(i).setItemImage();
		}
	}
	/**
	 * Returns all tiles on this floor that surround the given tile in the given
	 * area
	 * 
	 * @param floorPlan
	 *            - this tile's floorplan
	 * @param area
	 *            - the area of effect: 0= just this tile, 2 = two tiles in each
	 *            direction, plus 1 tile diagnols
	 * @return
	 */
	public ArrayList<Tile> findAOETilesFlat(int area) {
		ArrayList<Tile> retTiles = new ArrayList<Tile>();
		for (int x = -area; x <= area; x++) {
			for (int y = -area; y <= area; y++) {
				// grab the center tile, but grab reduced diagnols
				int xDist = 0;
				int yDist = 0;
				if (x != 0 && (x == y || x == -y)) {
					if (x == y) {
						if (x > 0) {
							xDist = -1;
							yDist = -1;
						} else {
							xDist = 1;
							yDist = 1;
						}
					} else if (x == -y) {
						if (x > 0) {
							xDist = -1;
							yDist = 1;
						} else {
							xDist = 1;
							yDist = -1;
						}
					}
				}
				addTiletoList(retTiles, x, y, xDist, yDist);
			}
		}
		return retTiles;
	}
	/**
	 * Returns the outer ring of tiles on this floor that surround the given
	 * tile in the given area
	 * 
	 * @param floorPlan
	 *            - this tile's floorplan
	 * @param area
	 *            - the area of effect: 0= just this tile, 2 = two tiles in each
	 *            direction, plus 1 tile diagnols
	 * @return
	 */
	public ArrayList<Tile> findAOETilesFlatHollow(int area) {
		ArrayList<Tile> retTiles = new ArrayList<Tile>();
		for (int x = -area; x <= area; x++) {
			int y = Math.abs(Math.abs(x) - area);
			addTiletoList(retTiles, x, y, 0, 0);
			y = -y;
			addTiletoList(retTiles, x, y, 0, 0);
		}
		return retTiles;
	}
	/**
	 * Returns a square instead of a diamond
	 */
	public ArrayList<Tile> findAOETilesFlatSquareHollow(int area) {
		ArrayList<Tile> retTiles = new ArrayList<Tile>();
		for (int x = -area; x <= area; x++) {
			int y = 0;
			int xDist = 0;
			int yDist = 0;
			// the verticle lines of the square
			if (x == -area || x == area) {
				for (y = -area; y <= area; y++) {
					addTiletoList(retTiles, x, y, xDist, yDist);
				}
			}
			// the horizontal lines of the square
			else {
				y = area;
				addTiletoList(retTiles, x, y, xDist, yDist);
				y = -y;
				addTiletoList(retTiles, x, y, xDist, yDist);
			}
		}
		return retTiles;
	}
	/**
	 * Returns a circle instead of a diamond
	 */
	public ArrayList<Tile> findAOETilesFlatCircleHollow(int area) {
		ArrayList<Tile> retTiles = new ArrayList<Tile>();
		int i, x1, y1;
		for (i = 0; i < 360; i++) {
			x1 = (int) (area * Math.cos(i * Math.PI / 180));
			y1 = (int) (area * Math.sin(i * Math.PI / 180));
			addTiletoList(retTiles, x1, y1, 0, 0);
		}
		return retTiles;
	}
	// Optimized in new method
	// public ArrayList<Tile> findAOETilesFlatHollow(ShipFloorPlan floorPlan,
	// int area) {
	// //find the outer ring
	// ArrayList<Tile> retTiles = findAOETilesFlat(floorPlan, area);
	// //find the inner area
	// ArrayList<Tile> negTiles = findAOETilesFlat(floorPlan, area-1);
	// //subtract inner area
	// for (int i =0; i < negTiles.size(); i++){
	// retTiles.remove(negTiles.get(i));
	// }
	// return retTiles;
	// }
	/**
	 * Returns the tiles that surround the hit tile
	 * 
	 * @param floorPlan
	 *            - this tile's floorplan
	 * @param area
	 *            - the area of effect: 0= just this tile, 2 = two tiles in each
	 *            direction, plus 1 tile diagnols. For each floor above or below
	 *            the origional tile, the area is reduced 1
	 * @return
	 */
	public ArrayList<Tile> findAOETiles3d(int area) {
		ArrayList<Tile> retTiles = new ArrayList<Tile>();
		Ship ship = this.getParentShip();
		int origFloor = this.getFloor();
		// For each floor within the AOE
		for (int f = -area + origFloor; f <= area + origFloor; f++) {
			// if the floor exists
			if (ship == null || (ship != null && ship.getFloorplans() == null)){
				System.out.println("Tile: floorplans null");
			}
			if (f < ship.getFloorplans().size() && f >= 0) {
				ShipFloorPlan floorPlan = ship.getFloorplans().get(f);
				// get the aoe for this floor; area - distance from origfloor
				int localArea = area - Math.abs(origFloor - f);
				// new clean way
				retTiles.addAll(findAOETilesFlat(localArea));
			}
		}
		return retTiles;
	}
	/**
	 * Returns the outer ring of tiles that surround the hit tile
	 * 
	 * @param floorPlan
	 *            - this tile's floorplan
	 * @param area
	 *            - the area of effect: 0= just this tile, 2 = two tiles in each
	 *            direction, plus 1 tile diagnols. For each floor above or below
	 *            the origional tile, the area is reduced 1
	 * @return
	 */
	public ArrayList<Tile> findAOETiles3dHollow(int area) {
		ArrayList<Tile> retTiles = new ArrayList<Tile>();
		int origFloor = this.getFloor();
		Ship ship = getParentShip();
		// For each floor within the AOE
		for (int f = -area + origFloor; f <= area + origFloor; f++) {
			// if the floor exists
			if (f < ship.getFloorplans().size() && f >= 0) {
				// get the aoe for this floor; area - distance from origfloor
				int localArea = area - Math.abs(origFloor - f);
				retTiles.addAll(findAOETilesFlatHollow(localArea));
			}
		}
		return retTiles;
	}
	public ArrayList<Tile> findAOETiles3dSquareHollow(int area) {
		ArrayList<Tile> retTiles = new ArrayList<Tile>();
		int origFloor = this.getFloor();
		Ship ship = getParentShip();
		// For each floor within the AOE
		for (int f = -area + origFloor; f <= area + origFloor; f++) {
			// if the floor exists
			if (f < ship.getFloorplans().size() && f >= 0) {
				// get the aoe for this floor; area - distance from origfloor
				int localArea = area - Math.abs(origFloor - f);
				retTiles.addAll(findAOETilesFlatSquareHollow(localArea));
			}
		}
		return retTiles;
	}
	public ArrayList<Tile> findAOETiles3dCircleHollow(int area) {
		ArrayList<Tile> retTiles = new ArrayList<Tile>();
		int origFloor = this.getFloor();
		Ship ship = getParentShip();
		// For each floor within the AOE
		for (int f = -area + origFloor; f <= area + origFloor; f++) {
			// if the floor exists
			if (f < ship.getFloorplans().size() && f >= 0) {
				// get the aoe for this floor; area - distance from origfloor
				int localArea = area - Math.abs(origFloor - f);
				Tile tile = ship.getTileAt(new int[] { f, getX(), getY() });
				retTiles.addAll(tile.findAOETilesFlatCircleHollow(localArea));
			}
		}
		return retTiles;
	}
	/**
	 * Grab's air from any surounding tiles that has more air
	 */
	public void grabAir(HashMap<Tile, Integer> path) {
		ArrayList<Tile> tiles = findAOETilesFlat(1);
		for (int i = 0; i < tiles.size(); i++) {
			// the tile with more air that we're taking from
			Tile newTile = tiles.get(i);
			// remove walls from the equation
			if (newTile != null
					&& newTile.isFlowTile()
					// take from tiles with more air that are more distant from
					// the source
					&& newTile.getAirLevel() > getAirLevel() && path.containsKey(newTile)
					&& path.get(newTile) > path.get(this)) {
				// set airlevels to an average
				int total = (getAirLevel() + newTile.getAirLevel());
				int avg = total / 2;
				// if it's an odd number, add the remainder to new tile
				int remainder = total % 2;
				int take1 = avg + remainder;
				// At this point the constant flow method is not working, so as
				// air pressure
				// lowers, so does drain rate. Oh well. :/
				// constant flow is 10, or if the tile has less then 10, all of
				// the tile's air
				int constantFlow = Math.min(total, 10);
				// set take 1 to constant flow if constant flow is greater
				// take1 = Math.max(constantFlow, take1);
				// take2 is whatever is left over
				int take2 = total - take1;
				// adjust air level for our tile (who is grabbing air)
				setAirLevel(take1);
				// adjust air level for the dowor tile
				newTile.setAirLevel(take2);
				// remove tile so air cannot be grabbed back?
				// path.put(newTile, -1);
			}
		}
	}
	/**
	 * Returns whether this tile is a space tile that is touching a tile that
	 * contains (flowing/flowable) air
	 */
	public boolean isSpaceAdjacentToAir() {
		if (getTileColor().equals(Color.black)) {
			ArrayList<Tile> list = findAOETiles3d(1);
			for (int i = 0; i < list.size(); i++) {
				Tile testTile = list.get(i);
				if (testTile.getAirLevel() > 0 && !testTile.isSolid()) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Returns whether this tile is a space tile that is touching a tile that
	 * contains (flowing/flowable) air (but may not currently have any air. Aka
	 * a non-solid, non space tile)
	 */
	public boolean isSpaceAdjacentToFlow() {
		if (getTileColor().equals(Color.black)) {
			ArrayList<Tile> list = findAOETiles3d(1);
			for (int i = 0; i < list.size(); i++) {
				Tile testTile = list.get(i);
				if (!testTile.isSpaceTile() && !testTile.isSolid()) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Returns whether this tile is a space tile that is touching a non-space/ship tile 
	 */
	public boolean isSpaceAdjacentToShipTile() {
		if (getTileColor().equals(Color.black)) {
			ArrayList<Tile> list = findAOETiles3d(1);
			for (int i = 0; i < list.size(); i++) {
				Tile testTile = list.get(i);
				if (!testTile.isSpaceTile()) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Returns whether this tile is touching a space tile
	 */
	public boolean isTouchingSpace() {
		ArrayList<Tile> list = findAOETiles3d(1);
		for (int i = 0; i < list.size(); i++) {
			Tile testTile = list.get(i);
			if (testTile.isSpaceTile()) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns whether this tile has no air but is touching a tile that contains
	 * air
	 */
	public boolean needsAir() {
		if (getAirLevel() < 1) {
			ArrayList<Tile> list = findAOETiles3d(1);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getAirLevel() > 0) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Returns whether the station is manned by a friendly crewman
	 */
	public boolean isManned() {
		return (getMan() != null);
		// return (getMan() != null && getMan().isPlayerControlled());
	}
	/**
	 * Returns whether the station is manned by this crewman
	 */
	public boolean isMannedBy(CrewMan man) {
		return (this.man == man);
	}
	/**
	 * Returns true if this station is both powered and manned
	 */
	public boolean isOnline() {
		return (isManned() && isPowered());
	}
	/**
	 * Returns true if this tile is on a ship without enemies
	 */
	public boolean isFriendly() {
		return !getParentShip().hasEnemyCrewOnboard();
	}
	public boolean isWeapon() {
		return getTileColor().equals(Weapons.systemMain)
				|| getTileColor().equals(Weapons.colorWeaponsMissile);
	}
	public boolean isDoor() {
		return getTileColor().equals(Doors.systemMain);
	}
	public boolean isOpenDoor() {
		return isDoor() && !isSolid();
	}
	/**
	 * Set's this tile's man to the crewman, manning systems and updating the
	 * crewman's position if necessary
	 * 
	 * @param manSystem
	 *            - if true work to man a system, if false just worry about
	 *            setting position
	 */
	public void setMan(CrewMan man) {
		if (hasParentSystem()) {
			if (man != null) {
				// if (getParentSystem().isManned() == false && man != null) {
				getParentSystem().manSystem(man);
			} else if (getParentSystem().isManned() && man == null) {
				getParentSystem().unManSystem(man);
			}
		}
		this.man = man;
	}
	public void setActor(CrewMan man) {
		this.actor = man;
		if (man != null) {
			man.setxPos(this.getX());
			man.setyPos(this.getY());
			man.setFloor(getFloor());
		}
	}
	public void removeMan() {
		this.man = null;
		this.actor = null;
	}
	public void addShieldLayer(Shield shield) {
		if (shields.contains(shield) == false) {
			shields.add(shield);
		}
	}
	public void removeShieldLayer(Item shield) {
		shields.remove(shield);
	}
	public int getShieldLayerFrequency() {
		return shields.get(0).getFrequency();
	}
	public boolean hasShieldLayerOn() {
		return (shields.size() > 0);
	}
	public int[] tileToLoc() {
		return new int[] { getFloor(), getX(), getY() };
	}
	public void startManStation(CrewMan man) {
		// getParentSystem().manSystem(man);
		setMan(man);
		// if we're looking at stations panel and this tile is selected
		if (Home.getCurrentScreen() instanceof GameScreen) {
			TilePane pane = TilePane.containsTile(this);
			if (pane != null) {
				TilePane.createPanel();
			}
		}
	}
	public void endManStation(CrewMan man) {
		// getParentSystem().unManSystem(man);
		setMan(null);
		// if (this == Home.getStationsPanel().getCurrentTile()){
		// Home.getStationsPanel().getCurrentTilePane().updatePane();
		// }
	}
	public int getFloor() {
		return parentFloor;
	}
	public void setParentFloor(int parentFloor) {
		this.parentFloor = parentFloor;
	}

	public class ConsoleTile extends Tile {
		private Tile remoteStation;
	}

	public class DoorTile extends Tile {
		private boolean locked;
	}

	public Ship getParentShip() {
		if (getParentFloorPlan() != null) {
			return getParentFloorPlan().getParentShip();
		}
		return null;
	}
	public ShipFloorPlan getParentFloorPlan() {
		return parentFloorPlan;
	}
	public void setParentFloorPlan(ShipFloorPlan parentFloorPlan) {
		this.parentFloorPlan = parentFloorPlan;
	}
	public boolean hasParentSystem() {
		if (getParentSystem() != null) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * is this tile safe?
	 * 
	 * @returns safe if the tile is not blocked and is not a space tile
	 */
	public boolean isSafe() {
		if (!isBlocked() && !isSpaceTile()) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * Does this tile have a safe neigbor?
	 * 
	 */
	public boolean hasSafeNeighbor() {
		for (int x = -1; x <= 1; x++) {
			int y = Math.abs(Math.abs(x) - 1);
			Tile t1 = getParentFloorPlan().getTileAt(getX() + x, getY() + y);
			y = -y;
			Tile t2 = getParentFloorPlan().getTileAt(getX() + x, getY() + y);
			if ((t1 != null && t1.isSafe()) || (t2 != null && t2.isSafe())) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Is this tile safe and doesn't already have a crewman?
	 * 
	 * @return
	 */
	public boolean isVacant() {
		if (isBlocked() == false && getAirLevel() > 0 && getActor() == null) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * Is this tile touching the other tile?
	 * 
	 * @param otherTile
	 * @return
	 */
	public boolean isTouching(Tile otherTile) {
		if (otherTile == null) {
			return false;
		}
		int[] current = getLocation();
		int[] other = otherTile.getLocation();
		if (current[0] == other[0]) {
			for (int x = -1; x < 2; x++) {
				for (int y = -1; y < 2; y++) {
					if ((x == y && x != 0) || (x == -y && x != 0)) {
						continue;
					}
					if (other[1] == current[1] + x && other[2] == current[2] + y) {
						return true;
					}
				}
			}
		}
		return false;
	}
	/**
	 * Is this tile empty?
	 * 
	 * @returns true if the tile has no air and is unblocked, or has no item or
	 *          has no health
	 */
	public boolean isEmpty() {
		if ((isBlocked() == false && getAirLevel() == 0)
		// || getItem()== null
		// || getHealthCurrent() <= 0
		) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * Is this a space tile?
	 */
	public boolean isSpaceTile() {
		return getTileColor().equals(Color.black);
	}
	/**
	 * Return's whether this tile is a wall or a floor
	 */
	public boolean isBuildTile() {
		return getTileColor().equals(Color.decode("#ffffff"))
				|| getTileColor().equals(Color.decode("#8b8b8b"));
	}
	/**
	 * Return's whether this tile is system tile (niether wall, nor floor, nor
	 * space)
	 */
	public boolean isSystemTile() {
		return !isBuildTile() && !isSpaceTile();
	}
	/**
	 * Returns whether this tile is on the edge of its floorplan
	 */
	public boolean isEdgeTile() {
		return getParentFloorPlan().isEdgeTile(this);
	}
	// Todo - return clone item, not origional
	public Item findDefaultItem(String tileSet) {
		String itemID = null;
		TileSet set = Home.resources.getTiles().findTileSet(tileSet);
		if (set != null){
			itemID = set.getItemFromColor((getTileColor()));
		}
//		String itemID = Home.resources.getTiles().getTileSets().get(tileSet).get(getTileColor());
		if (itemID != null) {
			Item newItem = Item.cloneItem(Home.resources.getItems().findItem(itemID));
			return newItem;
		}
		return null;
	}
	public String getDescription() {
		if (item != null) {
			return item.getDescription();
		}
		return "";
	}
	public ShipSystem getParentSystem() {
		return parentSystem;
	}
	public void setParentSystem(ShipSystem parentSystem) {
		if (isSystemTile() && parentSystem == null) {
			System.out.println("null par sys");
		}
		this.parentSystem = parentSystem;
		// parentSystem.installItem(this);
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public String getName() {
		if (hasItem()) {
			return item.getName();
		} else {
			return name;
		}
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isBlocked() {
		return blocked;
	}
	/**
	 * @returns whether the tile is blocked and has greater than 0 health
	 */
	public boolean isSolid() {
		return isBlocked() && getHealth() > 0;
	}
	/**
	 * Return's whether this is a ship tile (non space) that can allow air to
	 * flow through it (non solid)
	 * 
	 * @return
	 */
	public boolean isFlowTile() {
		return !isSolid() && !isSpaceTile();
	}
	// TODO Crewman properly setting this blocked?
	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
		// update ship rooms
		if (getParentShip() != null) {
			getParentShip().updateShipRooms(this);
		}
		// update flooplan pic
		if (getParentFloorPlan() != null) {
			getParentFloorPlan().updateImage();
		}
	}
	public int getAirLevel() {
		return airLevel;
	}
	/**
	 * Only sets air level for non- space tiles
	 */
	public void setAirLevel(int airLevel) {
		if (!getTileColor().equals(Color.black)) {
			this.airLevel = airLevel;
		}
		// update flooplan pic
		if (getParentFloorPlan() != null && (getAirLevel() < 52 || Home.displayAir)) {
			getParentFloorPlan().updateImage();
		}
	}
	public void incAirLevel(int amount) {
		this.setAirLevel(getAirLevel() + amount);
	}
	public int getHealthTotal() {
		if (item != null) {
			return item.getHealthTotal();
		} else {
			return 0;
		}
	}
	public int getHealth() {
		if (item != null) {
			return item.getHealth();
		} else {
			return 0;
		}
	}
	/**
	 * Does this tile have at least no health?
	 */
	public boolean isBroken() {
		return getHealth() <= 0;
	}
	public void setHealth(int health) {
		if (health < 0) {
			health = 0;
		}
		if (getItem() != null) {
			boolean kill = false;
			//random chance that when hitting a completely broken tile it will disolve
			if (getHealth() ==0 && health == 0 && StaticFunctions.randRange(0, 1) == 1){
				kill = true;
			}
			item.setHealth(health);
			if (kill){
				cloneUpdate(Color.black);
				removeTileItem(null);
				updateImages();
			}
		
		}
	}
	/**
	 * Return's this tile's position on the screen
	 */
	public Point getPositionOnScreen() {
		return new Point(x * Constants.shipSquare + GameScreen.borderSide, y
				* Constants.shipSquare + GameScreen.borderTop);
	}
	public Color getTileColor() {
		return tileType;
	}
	public void setTileColor(Color tileColor) {
		this.tileType = tileColor;
	}
	public Item getItem() {
		return item;
	}
	public boolean setItem(Item item) {
		if (item != null) {
			if (item.getItemType().equals(getTileColor())) {
				this.item = item;
				item.setParentTile(this);
				return true;
			}
		}
		return false;
	}
	/**
	 * Use removeTileItem, as that is the safer function
	 */
	private void removeItem() {
		if (hasParentSystem()) {
			getParentSystem().unInstallItem(this);
		}
		item.setParentTile(null);
		this.item = null;
	}
	/**
	 * Safely remove's this tile's item and properly updates the tile
	 * @param tempItem - optional item to swap with the old item
	 */
	public void removeTileItem(Item tempItem){
		if (getItem().isFloor()) {
			//if crewman is standing here, push him to the side
			if (getActor() != null){
				CrewMan m = getActor();
				Tile shove = findNearestVacantTile();
				shove.setActor(m);
				setActor(null);
			}
			setAirLevel(0);
			cloneUpdate(Color.black);
			removeItem();
			//crop the floorplan
			getParentFloorPlan().cropFloorPlan();
			// System.out.println("Replaced tile with space");
		}
		// otherwise replace it with the crewman's item
		else if (tempItem != null) {
			// set tile to appropriate tile type
			cloneUpdate(tempItem.getItemType());
			// set the new tile's item to the temp item
			setItem(tempItem);
			// install to new system
			connectToProperSystem();
		}
		// otherwise replace it with a floor based on the ship's tile set
		else {
			// set tile to appropriate tile type
			cloneUpdate(Color.white);
			setAirLevel(1);
			// update item based on tile set or under item id
			String id = getUnderItem();
			if (id != null && !id.equals("")) {
				setItem(Home.resources.getItems().findItem(id));
			} else {
				setItem(findDefaultItem(getParentShip().getTileSet()));
			}
			connectToProperSystem();
		}
		updateImages();
	}
	public ArrayList<Shield> getShields() {
		return shields;
	}
	public void setShields(ArrayList<Shield> shields) {
		this.shields = shields;
	}
	public boolean hasItem() {
		if (item != null) {
			return true;
		}
		return false;
	}
	public boolean isPowered() {
		return (getItem() != null && getItem().getPowerCurrent() > 0);
	}
	public CrewMan getMan() {
		return man;
	}
	/**
	 * Actor and Man are duplicates (see todo by definition) Actor is used more
	 * for location stuff. I think it stores the actor that is actually standing in this tile
	 * as oppossed to the one who is 'manning it' (possibly from an adjacent tile)
	 */
	public CrewMan getActor() {
		return actor;
	}
	public Color getAltColor() {
		return altColor;
	}
	public void setAltColor(Color altColor) {
		this.altColor = altColor;
	}
	/**
	 * Removes this tile's alternate color if it matches the input paramater
	 */
	public void removeAltColor(Color altColor) {
		if (this.altColor != null && this.altColor.equals(altColor)) {
			this.altColor = null;
		}
	}
	public Weapon getWeapon() {
		return ((Weapon) getItem());
	}
	public Room getParentRoom() {
		return parentRoom;
	}
	public boolean hasParentRoom() {
		return (getParentRoom() != null);
	}
	public void setParentRoom(Room parentRoom) {
		this.parentRoom = parentRoom;
	}
	public int[] getLocation() {
		return new int[] { parentFloor, getX(), getY() };
	}
	public String getUnderItem() {
		return underItem;
	}
	public void setUnderItem(String underItem) {
		this.underItem = underItem;
	}
	/**
	 * Finds and upateds the pane that contians this tile
	 */
	public void updatePane(){
		TilePane pane = TilePane.containsTile(this);
		if (pane != null){
			pane.updatePane();
		}
	}
}
