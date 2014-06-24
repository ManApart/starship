package org.iceburg.home.persistance;

import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.actors.Race;
import org.iceburg.home.items.Item;
import org.iceburg.home.main.Home;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.ship.ShipFloorPlan;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.ship.systems.Warp;
import org.iceburg.home.story.BattleLocation;
import org.iceburg.home.story.Player;
import org.iceburg.home.story.Quest;
import org.iceburg.home.story.QuestEvent;

//this class converts the Player class into a writtable file, back and forth
//includes subclasses that are serializable versions of game objects
public class PlayerSave implements Serializable {
	private QuestSave quest;
	private BattleLocSave currentLocation;
	private ShipSave playerShip;

	// Save classes
	public static class BattleLocSave implements Serializable {
		public BattleLocSave(){
			ships = new ArrayList<ShipSave>();
		}
		String id, name;
		ArrayList<ShipSave> ships;
		String backgroundText, overlay1Text, starText, planetText, overlay2Text,
				songText;
		Point starPoint, planetPoint, overlay2Point;
		boolean inBattle;
	}

	public static class ShipSave implements Serializable {
		private ArrayList<ShipFloorPlanSave> shipFloorplans;
		private ArrayList<CrewManSave> crew;
		private String id;
		private WarpSave warpSave;
	}

	//TODO Fill out based on Warp - figure out how to deal with image
	public static class WarpSave implements Serializable {
		float xPos, yPos;
		// Max turn is 0-10;
		int distance, bearing, width, height, angle;
		double speed;
		int[] starDate;
		//try wrapping with imageicons
		private ImageIcon rotateImg, bi;
		private boolean autoPilot;
	}

	public static class CrewManSave implements Serializable {
		private Point energyFields, navigation, analysis, powerDistribution;
		Color division;
		Race race;
		// Attributes
		int healthCurrent, healthTotal;
		//position of actor, manned tiles
		int x, y, xm, ym;
		String name;
		private int floor, breath;
//		private ShipSave parentShip;
		// find tilesave of parent shipsave
//		private TileSave mannedTile, actorTile;
		private boolean playerControlled;
		private ItemSave item;
		// don't save AI, on deserialize, have man adj tile
	}

	public static class ShipFloorPlanSave implements Serializable {
		private TileSave[][] shipTiles;
		private int floorLevel;
	}

	public static class TileSave implements Serializable {
		private String name, underItem;
		private int airLevel;
		private Color tileType;
		private ItemSave item;
		private int parentFloor;
	}

	public static class ItemSave implements Serializable {
		String id;
		int health, currentPower;
//		TileSave parentTile;
	}
	public static class QuestSave implements Serializable {
		int progress, timer;
		boolean advanceStep;
		boolean canJump, jumping;
		String id, title;
		ArrayList<String> events;
		QuestEventSave currentEvent;
	}
	public static class QuestEventSave implements Serializable {
		Quest parent;
		BattleLocSave loc;
		String id, unlock, newQuest, sound, unlockF, newQuestF;
		ArrayList<CrewManSave> crew, crewF;
		ArrayList<ItemSave> items, itemsF;
		ArrayList<String> preText, postText, arriveText, postTextW, arriveTextW, postTextF, arriveTextF;
		int step, distance;
		public static int timeFactor = 10, distanceFactor = 100;
		long time, waitTime;
		boolean locDistance, locTime, eventSuccess;
	}
	public static PlayerSave serializePlayer(Player p) {
		if (p == null) {
			return null;
		}
		PlayerSave ps = new PlayerSave();
		//TODO -fix this
//		ps.quest = p.getQuest();
		ps.playerShip = serializeShip(p.getPlayerShip());
		ps.currentLocation = serializeLocation(p.getCurrentLocation());
		return ps;
	}
	public static Player deserializePlayer(PlayerSave ps) {
		if (ps == null) {
			return null;
		}
		Player p = new Player();
		p.setQuest(deserializeQuest(ps.quest));
		p.setCurrentLocation(deserializeLocation(ps.currentLocation));
		p.setPlayerShip(deserializeShip(ps.playerShip));
		// add player ship to current loc
		p.getCurrentLocation().addShip(p.getPlayerShip());
		return p;
	}
	public static BattleLocSave serializeLocation(BattleLocation b) {
		BattleLocSave bs = new BattleLocSave();
		// strings
		bs.id = b.getId();
		bs.name = b.getName();
		bs.backgroundText = b.getBackgroundText();
		bs.overlay1Text = b.getOverlay1Text();
		bs.starText = b.getStarText();
		bs.planetText = b.getPlanetText();
		bs.overlay2Text = b.getOverlay2Text();
		bs.songText = b.getSongText();
		bs.inBattle = b.isInBattle();
		// Ships
		for (int i = 0; i < b.getShips().size(); i++) {
			// do not add player ship to battle location
			if (b.getShips().get(i) != Home.getShip()) {
				bs.ships.add(serializeShip(b.getShips().get(i)));
			}
		}
		return bs;
	}
	public static BattleLocation deserializeLocation(BattleLocSave bs) {
		BattleLocation b = new BattleLocation();
		b.setId(bs.id);
		b.setName(bs.name);
		b.setBackgroundText(bs.backgroundText);
		b.setOverlay1Text(bs.overlay1Text);
		b.setStarText(bs.starText);
		b.setOverlay2Text(bs.overlay2Text);
		b.setSongText(bs.songText);
		b.setInBattle(bs.inBattle);
		// Ships
		for (int i = 0; i < bs.ships.size(); i++) {
			b.getShips().add(deserializeShip(bs.ships.get(i)));
		}
		return b;
	}
	//TODO - complete
	public static ShipSave serializeShip(Ship s) {
		ShipSave ss = new ShipSave();
//		ss.shipFloorplans = serialize
		return ss;
	}
	//TODO - complete
	public static Ship deserializeShip(ShipSave ss) {
		Ship s = new Ship();
		return s;
	}
	//TODO - complete
	public static WarpSave serializeWarp(Warp s) {
		WarpSave ss = new WarpSave();
		return ss;
	}
	//TODO - complete
	public static Warp deserializeWarp(WarpSave ss, Ship ship) {
		Warp s = new Warp(ship);
		return s;
	}
	//TODO - complete
	public static QuestSave serializeQuest(Quest q) {
		QuestSave qs = new QuestSave();
		return qs;
	}
	//TODO - complete
	public static Quest deserializeQuest(QuestSave qs) {
		Quest q = new Quest(qs.id, qs.title);
		return q;
	}
	//TODO - complete
	public static QuestEventSave serializeQuestEvent(QuestEvent q) {
		QuestEventSave qs = new QuestEventSave();
		return qs;
	}
	//TODO - complete
	public static QuestEvent deserializeQuestEvent(QuestEventSave qs) {
		QuestEvent q = new QuestEvent(qs.id);
		return q;
	}
	public static CrewManSave serializeCrewman(CrewMan q) {
		CrewManSave qs = new CrewManSave();
		qs.name = q.getName();
		qs.energyFields = q.getEnergyFields();
		qs.navigation = q.getNavigation();
		qs.analysis = q.getAnalysis();
		qs.powerDistribution = q.getPowerDistribution();
		qs.division = q.getDivision();
		qs.healthCurrent = q.getHealthCurrent();
		qs.healthTotal = q.getHealthTotal();
		qs.floor = q.getFloor();
		qs.breath = q.getBreath();
		qs.x = q.getLocationTile().getX();
		qs.y = q.getLocationTile().getY();
		qs.xm = q.getCurrentTile().getX();
		qs.ym = q.getCurrentTile().getY();
		qs.playerControlled = q.isPlayerControlled();
		qs.item = serializeItem(q.getItem());
		return qs;
	}
	public static CrewMan deserializeCrewman(CrewManSave qs, Ship s) {
		CrewMan q = new CrewMan(qs.division);
		q.setName(qs.name);
		q.setEnergyFields(qs.energyFields);
		q.setNavigation(qs.navigation);
		q.setAnalysis(qs.analysis);
		q.setPowerDistribution(qs.powerDistribution);
		q.setDivision(qs.division);
		q.setHealthCurrent(qs.healthCurrent);
		q.setHealthTotal(qs.healthTotal);
		q.setFloor(qs.floor);
		q.setBreath(qs.breath);
		q.setPlayerControlled(qs.playerControlled);
		q.setItem(deserializeItem(qs.item, null));
		q.setxPos(qs.x);
		q.setyPos(qs.y);
		ShipFloorPlan floorPlan = s.shipFloorplans.get(q.getFloor());
		q.setCurrentTile(floorPlan.getTileAt(qs.xm, qs.ym));
		s.getTileAt(new int[]{qs.floor, qs.x, qs.y}).setActor(q);
		s.getTileAt(new int[]{qs.floor, qs.xm, qs.ym}).setMan(q);
		q.setParentShip(s);
		return q;
	}
	public static ItemSave serializeItem(Item q) {
		if (q == null){
			return null;
		}
		ItemSave qs = new ItemSave();
		qs.id = q.getId();
		qs.health = q.getHealth();
		qs.currentPower = q.getPowerCurrent();
		return qs;
	}
	public static Item deserializeItem(ItemSave qs, Tile t) {
		if (qs == null){
			return null;
		}
		Item q = Home.resources.items.findItem(qs.id);
		q.setHealth(qs.health);
		q.setCurrentPower(qs.currentPower);
		if (t != null){
			q.setParentTile(t);
		}
		return q;
	}
	public static TileSave serializeTile(Tile q) {
		TileSave qs = new TileSave();
		qs.name = q.getName();
		qs.underItem = q.getUnderItem();
		qs.airLevel = q.getAirLevel();
		qs.tileType = q.getTileColor();
		qs.item = serializeItem(q.getItem());
		return qs;
	}
	public static Tile deserializeTile(TileSave qs, int x, int y) {
		Tile q = new Tile();
		q.setName(qs.name);
		q.setUnderItem(qs.underItem);
		q.setAirLevel(qs.airLevel);
		q.setTileColor(qs.tileType);
		q.setItem(deserializeItem(qs.item, q));
		q.setX(x);
		q.setY(y);
		return q;
	}
	/**
	 * Serialize an array of Tiles
	 */
	public static TileSave[][] serializeTiles(Tile[][] q) {
		TileSave[][] qs = new TileSave[q.length][q[0].length];
		for (int i=0; i< q.length; i++){
			for (int j=0; j< q[0].length; j++){
				qs[i][j] = serializeTile(q[i][j]);
			}
		}
		return qs;
	}
	/**
	 * Deserialize an array of Tiles
	 */
	public static Tile[][] deserializeTiles(TileSave[][] qs, ShipFloorPlan fp) {
		Tile[][] q = new Tile[qs.length][qs[0].length];
		for (int i=0; i< qs.length; i++){
			for (int j=0; j< qs[0].length; j++){
				q[i][j] = deserializeTile(qs[i][j], i, j);
				q[i][j].setParentFloorPlan(fp);
			}
		}
		return q;
	}
	public static ShipFloorPlanSave serializeShipFloorplan(ShipFloorPlan q) {
		ShipFloorPlanSave qs = new ShipFloorPlanSave();
		return qs;
	}
	public static ShipFloorPlan deserializeShipFloorplan(ShipFloorPlanSave qs, Ship ship) {
		ShipFloorPlan q = new ShipFloorPlan(ship, qs.floorLevel);
		q.setShipTiles(deserializeTiles(qs.shipTiles, q));
		return q;
	}
	
}
