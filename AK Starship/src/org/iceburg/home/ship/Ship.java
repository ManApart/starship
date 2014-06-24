package org.iceburg.home.ship;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.actors.Projectile;
import org.iceburg.home.ai.ManStationAction;
import org.iceburg.home.ai.NPCAI;
import org.iceburg.home.items.Weapon;
import org.iceburg.home.main.Constants;
import org.iceburg.home.main.Home;
import org.iceburg.home.main.StaticFunctions;
import org.iceburg.home.persistance.ShipParser;
import org.iceburg.home.ship.systems.Cabins;
import org.iceburg.home.ship.systems.CargoHold;
import org.iceburg.home.ship.systems.Comms;
import org.iceburg.home.ship.systems.Doors;
import org.iceburg.home.ship.systems.Engineering;
import org.iceburg.home.ship.systems.HelmSystem;
import org.iceburg.home.ship.systems.LifeSupport;
import org.iceburg.home.ship.systems.MedBay;
import org.iceburg.home.ship.systems.Sensors;
import org.iceburg.home.ship.systems.Shields;
import org.iceburg.home.ship.systems.ShipSystem;
import org.iceburg.home.ship.systems.TurboTube;
import org.iceburg.home.ship.systems.Warp;
import org.iceburg.home.ship.systems.Weapons;

public class Ship {
	public ArrayList<ShipFloorPlan> shipFloorplans;
	public ArrayList<CrewMan> crew;
	public String title, tileSet;
	public ArrayList<ShipSystem> shipSystems;
	// private static int ventTime;
	private Image img;
	public BufferedImage floorImg;
	private ArrayList<Projectile> projectiles;
	private ArrayList<Room> rooms;
	private String id;
	private NPCAI captain;

	public Ship() {
		createSystems();
		projectiles = new ArrayList<Projectile>();
		crew = new ArrayList<CrewMan>();
	}
	/**
	 * Create's a fully functional ship by parsing the id and placing at x, y
	 */
	public static Ship shipComplete(String id, int x, int y) {
		Ship ship = ShipParser.parseShip(id);
		ship.id = id;
		ship.addDefaultItems(ship.getTileSet());
		ship.getWarp().init();
		ship.setItemImages();
		ship.findShipRooms();
		ship.addCrewMen();
		ship.getWeapons().evaluateWeaponArcs();
		ship.getEngineering().autoPowerUp();
		ship.getLifeSupport().powerOnLifeSupport();
		ship.captain = new NPCAI(ship);
		// Ship's default to targeting the player ship
		if (Home.getShip() != null && Home.getShip() != ship) {
			ship.getHelm().setTargetShip(Home.getShip().getWarp());
		}
		// ship.getEngineering().attemptPowerSubSystem(ship.getShields().getFirstStation().getItem());
		return ship;
	}
	public void paintShip(Graphics2D gg, int floor) {
		if (getFloorplans().size() > floor) {
			// creating a new image clips all of the tiles, crew, and
			// projectiles to the viewscreen size
			BufferedImage img = new BufferedImage(Constants.viewScreenSize, Constants.viewScreenSize, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = img.createGraphics();
			ShipFloorPlan floorplan = shipFloorplans.get(floor);
			// draw projectiles from other floors under the floorplan
			ArrayList<Projectile> topProjectiles = new ArrayList<Projectile>();
			if (getProjectiles() != null) {
				for (int p = 0; p < getProjectiles().size(); p++) {
					Projectile proj = getProjectiles().get(p);
					if (proj.getFloor() != floor) {
						proj.paint(g, floorplan.getShiftX() * Constants.shipSquare, floorplan.getShiftY()
								* Constants.shipSquare);
					}
					// draw later
					else {
						topProjectiles.add(proj);
					}
				}
			}
			// paint floorplan
			floorplan.paintComponent(g, false);
			// paint lasers
			ArrayList<Weapon> lasers = getWeapons().getBeamWeapons();
			for (int i = 0; i < lasers.size(); i++) {
				Weapon laser = lasers.get(i);
				laser.paintLaser(g, floorplan.getShiftX() * Constants.shipSquare, floorplan.getShiftY()
						* Constants.shipSquare);
			}
			// paint projectiles
			for (int p = 0; p < topProjectiles.size(); p++) {
				Projectile proj = topProjectiles.get(p);
				proj.paint(g, floorplan.getShiftX() * Constants.shipSquare, floorplan.getShiftY()
						* Constants.shipSquare);
			}
			gg.drawImage(img, 0, 0, null);
		}
	}
	@Override
	public String toString() {
		return id;
	}
	public void updateShip() {
		getWarp().moveShip();
		getWarp().updateTime(10);
		updateCrew();
		updateSystems();
		if (this != Home.getShip()) {
			captain.update();
		}
		// updateProjectiles();
	}
	public void updateSystems() {
		for (int i = 0; i < getShipSystems().size(); i++) {
			getShipSystems().get(i).updateSystem();
		}
	}
	//TODO - calc image based on floorplan
//	/**
//	 * Calculates the image to be displayed in external systems
//	 * @return
//	 */
//	public BufferedImage calcExternalImage(){
//		
//	}

	/**
	 * Uses the ship's doors to find 'rooms' (area's where air can flow)
	 */
	public void findShipRooms() {
		rooms = new ArrayList<Room>();
		// for each door on the ship
		ArrayList<Tile> doorList = getDoorSystem().getDoors();
		if (doorList.size() > 0) {
			for (int i = 0; i < doorList.size(); i++) {
				ArrayList<Tile> tiles = doorList.get(i).findAOETilesFlat(1);
				// check the surrounding 'flow' tiles
				for (int j = 0; j < tiles.size(); j++) {
					// if the flow tile is contianed within the vacume map, or
					// another room map, ignore it
					Tile sample = tiles.get(j);
					if (sample != null && sample.isFlowTile()
							&& sample.getParentRoom() == null) {
						// build a new room with this tile
						rooms.add(Room.buildRoom(sample));
					}
				}
			}
		}
		// testRoomFindResults();
	}
	/**
	 * Tests the findShipRooms function by looking at every flow tile on the
	 * ship and finding the ones that aren't asigned a room
	 */
	public void testRoomFindResults() {
		// just do floor 1
		Tile[][] tiles = getFloorplans().get(0).getShipTiles();
		ArrayList<Tile> rejects = new ArrayList<Tile>();
		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[x].length; y++) {
				Tile tile = tiles[x][y];
				if (tile.isFlowTile() && !tile.hasParentRoom()) {
					rejects.add(tile);
				}
			}
		}
		if (rejects.size() > 0) {
			System.out.println("System produced +" + rejects.size() + "rejects. :(");
		} else {
			System.out.println("No rejects! :D");
		}
	}
	/**
	 * Update's the ship's room plan by combining or seperating rooms based on a
	 * change in the solidity of the source tile
	 */
	public void updateShipRooms(Tile source) {
		// find a tile in either room
		ArrayList<Tile> tiles = source.findAOETilesFlat(1);
		Tile tile1 = null;
		Tile tile2 = null;
		for (int i = 0; i < tiles.size(); i++) {
			Tile sample = tiles.get(i);
			if (sample.isFlowTile() && !sample.isDoor()) {
				if (tile1 == null) {
					tile1 = sample;
				} else if (tile2 == null) {
					tile2 = sample;
				}
			}
		}
		if (tile1 != null && tile1.hasParentRoom() && tile2 != null
				&& tile2.hasParentRoom()) {
			if (source.isSolid()) {
				// seperate into two rooms
				tile1.getParentRoom().seperateRooms(tile1, tile2);
			} else {
				// combine rooms
				tile1.getParentRoom().combineRooms(tile2);
			}
		}
		// System.out.println("rooms updated");
	}
	/**
	 * Make's this ship's entire crew player controlled (used when creating a
	 * player)
	 */
	public void makeCrewFriendly() {
		for (int i = 0; i < crew.size(); i++) {
			crew.get(i).setPlayerControlled(true);
		}
	}
	public void addCrewMen() {
		for (int i = 0; i < crew.size(); i++) {
			addCrewMan(crew.get(i), null);
		}
	}
	/**
	 * Adds a crewman to the ship, optionally adding him near a certain tile
	 * (Otherwise adding him near a station based on his division
	 * 
	 * @param crewMan
	 * @param goalTile
	 */
	public void addCrewMan(CrewMan crewMan, Tile goalTile) {
		// get the tile by division
		if (goalTile == null) {
			if (crewMan.getDivision().equals(CrewMan.Command)) {
				goalTile = findShipSystem(HelmSystem.systemMain).getFirstStation();
			} else if (crewMan.getDivision().equals(CrewMan.Science)) {
				goalTile = (findShipSystem(Shields.systemMain)).getFirstStation();
			} else if (crewMan.getDivision().equals(CrewMan.Security)) {
				goalTile = getWeapons().getFirstStation();
			} else {
				goalTile = getEngineering().getFirstStation();
			}
		}
		// add the crewman to the nearest safe, unmanned tile
		if (goalTile != null){

			Tile nearTile = goalTile.findNearestVacantTile();
			nearTile.setActor(crewMan);
			crewMan.setParentShip(this);
			crewMan.addAIAction(new ManStationAction(crewMan.getAi(), goalTile, crewMan));
		}
		else{
			Tile safeTile = getRandomFloorPlan().getRandomSafeShipTile();
			safeTile.setActor(crewMan);
			crewMan.setParentShip(this);
		}
	}
	public boolean containsCrewMan(CrewMan man) {
		return crew.contains(man);
	}
	// TODO is giving the first manning crew a null state instead of giving it
	// the crewman
	public void manAdjacentStation(CrewMan crewMan) {
		ArrayList<Tile> tiles = crewMan.getLocationTile().findAOETilesFlat(1);
		for (int i = 0; i < tiles.size(); i++) {
			if (tiles.get(i).hasParentSystem()
					&& tiles.get(i).getParentSystem().isManned() == false) {
				crewMan.setCurrentTile(tiles.get(i));
				tiles.get(i).startManStation(null);
				break;
			}
		}
	}
	/**
	 * Finds the system of that type (by color)
	 * 
	 * @param systemType
	 * @return
	 */
	public ShipSystem findShipSystem(Color systemType) {
		ShipSystem system = null;
		for (int i = 0; i < shipSystems.size(); i++) {
			if (shipSystems.get(i).includesTileType(systemType)) {
				system = shipSystems.get(i);
			}
		}
		return system;
	}
	// Stuff for quickly finding systems
	/**
	 * Return's this ship's engineering system
	 */
	public Engineering getEngineering() {
		return (Engineering) findShipSystem(Engineering.systemMain);
	}
	/**
	 * Return's this ship's weapons system
	 */
	public Weapons getWeapons() {
		return (Weapons) findShipSystem(Weapons.systemMain);
	}
	/**
	 * Return's this ship's weapons system
	 */
	public Warp getWarp() {
		return (Warp) findShipSystem(Warp.systemMain);
	}
	/**
	 * Return shields system
	 */
	public Shields getShields() {
		return ((Shields) findShipSystem(Shields.systemMain));
	}
	/**
	 * Return security system
	 */
	public Doors getDoorSystem() {
		return ((Doors) findShipSystem(Doors.systemMain));
	}
	/**
	 * Return LifeSupport system
	 */
	public LifeSupport getLifeSupport() {
		return ((LifeSupport) findShipSystem(LifeSupport.systemMain));
	}
	/**
	 * Return Cabins system
	 */
	public Cabins getCabins() {
		return ((Cabins) findShipSystem(Cabins.systemMain));
	}
	/**
	 * Return Helm system
	 */
	public HelmSystem getHelm() {
		return ((HelmSystem) findShipSystem(HelmSystem.systemMain));
	}
	/**
	 * Return Cargo system
	 */
	public CargoHold getCargo() {
		return ((CargoHold) findShipSystem(CargoHold.systemMain));
	}
	/**
	 * Return Medbay system
	 */
	public MedBay getMedbay() {
		return ((MedBay) findShipSystem(MedBay.systemMain));
	}
	/**
	 * Return Turbo Tube system
	 */
	public TurboTube getTurboTubes() {
		return ((TurboTube) findShipSystem(TurboTube.systemMain));
	}
	/**
	 * Return Sensors system
	 */
	public Sensors getSensors() {
		return ((Sensors) findShipSystem(Sensors.systemMain));
	}
	/**
	 * Return Comms system
	 */
	public Comms getComms() {
		return ((Comms) findShipSystem(Comms.systemMain));
	}
	/**
	 * Returns whether the ship has that system
	 * 
	 * @param systemType
	 * @return
	 */
	public boolean hasSystem(Color systemType) {
		if (findShipSystem(systemType) != null) {
			return true;
		}
		return false;
	}
	public void updateCrew() {
		for (int i = 0; i < crew.size(); i++) {
			crew.get(i).updateCrewMan();
		}
	}
	public void createSystems() {
		shipSystems = new ArrayList<ShipSystem>();
		shipSystems.add(new TurboTube(this));
		shipSystems.add(new HelmSystem(this));
		shipSystems.add(new Weapons(this));
		shipSystems.add(new Engineering(this));
		shipSystems.add(new CargoHold(this));
		shipSystems.add(new MedBay(this));
		shipSystems.add(new Doors(this));
		shipSystems.add(new LifeSupport(this));
		shipSystems.add(new Shields(this));
		shipSystems.add(new Cabins(this));
		shipSystems.add(new Sensors(this));
		shipSystems.add(new Comms(this));
		shipSystems.add(new Warp(this));
		// update warp later once we have a parsed ship image
	}
	/**
	 * Is this tile within attack range (vulnerable)? Tests to see if an
	 * adjacent tile to the sides, above or below is empty
	 * 
	 * @param tile
	 *            - the tile to examine
	 * @return
	 */
	public boolean isTileVulnerable(Tile tile) {
		ArrayList<Tile> tiles = tile.findAOETiles3d(1);
		for (int i = 0; i < tiles.size(); i++) {
			if (tiles.get(i).isEmpty()) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Return's whether this ship has a hostile crewmen aboard
	 * 
	 * @return
	 */
	public boolean hasHostiles() {
		for (int i = 0; i < getCrew().size(); i++) {
			if (!getCrew().get(i).isPlayerControlled()) {
				return true;
			}
		}
		return false;
	}
	public void addDefaultItems(String tileSet) {
		for (int f = 0; f < getFloorplans().size(); f++) {
			Tile[][] tiles = getFloorplans().get(f).getShipTiles();
			for (int x = 0; x < tiles.length; x++) {
				for (int y = 0; y < tiles[x].length; y++) {
					tiles[x][y].setItem(tiles[x][y].findDefaultItem(tileSet));
				}
			}
		}
	}
	public void setItemImages() {
		for (int f = 0; f < getFloorplans().size(); f++) {
			Tile[][] tiles = getFloorplans().get(f).getShipTiles();
			for (int x = 0; x < tiles.length; x++) {
				for (int y = 0; y < tiles[x].length; y++) {
					tiles[x][y].setItemImage();
				}
			}
		}
	}
	public ShipFloorPlan getFloorPlanAt(int floor) {
		return getFloorplans().get(floor);
	}
	/**
	 * Return a random floorplan
	 */
	public ShipFloorPlan getRandomFloorPlan() {
		int i = getFloorplans().size();
		return getFloorplans().get(StaticFunctions.randRange(0, i - 1));
	}
	public Tile getTileAt(int[] loc) {
		return getFloorPlanAt(loc[0]).getTileAt(loc[1], loc[2]);
	}
	public ArrayList<ShipSystem> getShipSystems() {
		return shipSystems;
	}
	public void setShipSystems(ArrayList<ShipSystem> shipSystems) {
		this.shipSystems = shipSystems;
	}
	public ArrayList<ShipFloorPlan> getFloorplans() {
		return shipFloorplans;
	}
	public void setShipFloorplans(ArrayList<ShipFloorPlan> shipLayout) {
		this.shipFloorplans = shipLayout;
	}
	public ArrayList<CrewMan> getCrew() {
		return crew;
	}
	/**
	 * Return the number of playercontrolled crew onboard this ship
	 */
	public int numFriendlyCrew() {
		int numCrew = 0;
		for (int i = 0; i < crew.size(); i++) {
			if (crew.get(i).isPlayerControlled()) {
				numCrew += 1;
			}
		}
		return numCrew;
	}
	/**
	 * Return the number of enemy crew onboard this ship
	 */
	public int numFoeCrew() {
		int numCrew = 0;
		for (int i = 0; i < crew.size(); i++) {
			if (!crew.get(i).isPlayerControlled()) {
				numCrew += 1;
			}
		}
		return numCrew;
	}
	/**
	 * Return's whether this ship has any enemy crew on board
	 */
	public boolean hasEnemyCrewOnboard() {
		return numFoeCrew() > 0;
	}
	public void setCrew(ArrayList<CrewMan> crew) {
		this.crew = crew;
	}
	public String getTileSet() {
		return tileSet;
	}
	public void setTileSet(String tileSet) {
		this.tileSet = tileSet;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Image getImg() {
		return img;
	}
	public void setImg(Image img) {
		this.img = img;
	}
	public ArrayList<Projectile> getProjectiles() {
		return projectiles;
	}
	public void setProjectiles(ArrayList<Projectile> projectiles) {
		this.projectiles = projectiles;
	}
	// /**
	// * Return all of the projectiles of the weapons of this ship
	// */
	// public ArrayList<Projectile> getProjectiles() {
	// ArrayList<Projectile> list = new ArrayList<Projectile>();
	// for (int i =0; i< getWeapons().getStations().size(); i++){
	// if (getWeapons().getStations().get(i).getWeapon().getProjectiles().size()
	// > 0){
	// list.addAll(getWeapons().getStations().get(i).getWeapon().getProjectiles());
	// }
	// }
	// return list;
	// }
	public ArrayList<Room> getRooms() {
		return rooms;
	}
}
