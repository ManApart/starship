package org.iceburg.home.actors;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.HashMap;

import org.iceburg.home.ai.AI;
import org.iceburg.home.ai.AIAction;
import org.iceburg.home.items.CargoBay;
import org.iceburg.home.items.Item;
import org.iceburg.home.main.Constants;
import org.iceburg.home.main.Home;
import org.iceburg.home.main.StaticFunctions;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.ship.ShipFloorPlan;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.ship.systems.Comms;
import org.iceburg.home.ship.systems.Engineering;
import org.iceburg.home.ship.systems.HelmSystem;
import org.iceburg.home.ship.systems.MedBay;
import org.iceburg.home.ship.systems.Sensors;
import org.iceburg.home.ship.systems.Shields;
import org.iceburg.home.ship.systems.Warp;
import org.iceburg.home.ship.systems.Weapons;
import org.iceburg.home.sound.Sound;
import org.iceburg.home.ui.CrewPane;
import org.iceburg.home.ui.GameScreen;
import org.iceburg.home.ui.TilePane;
import org.iceburg.home.ui.UIMisc;

public class CrewMan {
	public static Color Science = Color.decode("#000092"),
			Engineer = Color.decode("#cd5400"), Security = Color.decode("#ff0008"),
			Command = Color.decode("#cdff00");
	public static int breathTotal = 100;
	// Use this map to see how much experience each level up needs
	private HashMap<Integer, Integer> levelEXPMap;
	// station skills x= level, y=skill
	private Point energyFields, navigation, analysis, powerDistribution;
	Color division;
	Race race;
	// Attributes
	int healthCurrent, healthTotal;
	String name;
	// // Crewman's walking path
	private HashMap<Tile, Integer> crewPath;
	public int xPos, yPos, subX, subY, floor, breath, breatheCount;
//	private int timer;
	private Ship parentShip;
	private Tile currentTile;
	private boolean playerControlled;
	// endLoc = place where player clicked, manLoc = location if location is
	// mannable, targetLoc = adjacant to manLoc
	// loc 0= floor, 1=x, 2 = y
	// public int[] endLoc, manLoc, targetLoc;
	private Item item;
	private AI ai;
	private static Sound dieSound = new Sound("fx/Ouch 1.wav");
	private static Sound dieSound2 = new Sound("fx/Ouch 2.wav");
	private static Sound alertSound = new Sound("fx/Hey.wav");

	public CrewMan(Color division) {
		// if (race == null) {
		// race = "race_human";
		// }
		this.division = division;
		// TODO - race parser!
		this.race = Home.resources.getRaces().findRace("race_human");
		// this.playerControlled = playerControlled;
		this.ai = new AI();
		generateStats();
	}
	@Override
	public String toString() {
		return getName();
	}
	public void generateStats() {
		generateStats(2);
	}
	public void generateStats(int base) {
		// update with a name from the race sheet
		this.name = race.getRandName();
		// create skills
		this.setHealthTotal(race.getHealthTotal());
		this.setHealthCurrent(getHealthTotal());
		// create education
		this.energyFields = new Point(StaticFunctions.randRange(1, base
				+ race.getEnergyFields()), 0);
		this.navigation = new Point(StaticFunctions.randRange(1, base
				+ race.getNavigation()), 0);
		this.analysis = new Point(StaticFunctions.randRange(1, base + race.getAnalysis()), 0);
		this.powerDistribution = new Point(StaticFunctions.randRange(1, base
				+ race.getPowerDistribution()), 0);
		this.breath = breathTotal;
	}
	public void paint(Graphics g, int x, int y, boolean drawDivisionColor) {
		g.setColor(Color.black);
		int xd = xPos * Constants.shipSquare + subX + x;
		int yd = yPos * Constants.shipSquare + subY + y;
		if (xd < Constants.viewScreenSize && yd < Constants.viewScreenSize && xd > 0
				&& yd > 0) {
			int wd = (int) Constants.shipSquare;
			g.fillOval(xd, yd, wd, wd);
			if (drawDivisionColor) {
				g.setColor(this.division);
			} else {
				g.setColor(Color.gray);
			}
			g.fillOval(xd + 1, yd + 1, wd - 2, wd - 2);
		}
	}
	public String getDivisionTitle() {
		String string = "";
		if (getDivision() == Command) {
			string = "Command";
		} else if (getDivision() == Security) {
			string = "Security";
		} else if (getDivision() == Science) {
			string = "Science";
		} else if (getDivision() == Engineer) {
			string = "Engineer";
		}
		return string;
	}
	/**
	 * Update the crewman
	 */
	public void updateCrewMan() {
//		if (timer < 1){
//			timer += 1;
//		}
//		else{
//			timer = 0;
		getAi().think();
		breathe();
//		}
		
	}
	/**
	 * Function for killing crewmen
	 */
	public void die() {
		String s = "Crewman ";
		if (getParentShip() != Home.getShip()) {
			s = "Enemy ";
		}
		else{
			int i = StaticFunctions.randRange(0, 1);
			if (i == 0){
				dieSound.play();
			}
			else{
				dieSound2.play();
			}
		}
		if (getCurrentTile() != null) {
			if (getCurrentTile().hasParentSystem()) {
				getCurrentTile().getParentSystem().unManSystem(this);
			}
			getCurrentTile().removeMan();
		}
		getParentShip().getCrew().remove(this);
		getLocationTile().removeMan();
		// if we're looking at stations panel and this crew is selected
		if (Home.getCurrentScreen() instanceof GameScreen) {
			CrewPane pane = CrewPane.containsMan(this);
			if (pane != null) {
				pane.updatePane();
			}
			TilePane paneT = TilePane.containsTile(getCurrentTile());
			if (paneT != null) {
				TilePane.createPanel();
			}
		}
		setCurrentTile(null);
		// if an enemy died, see if we're still in a battle
		if (!isPlayerControlledIgnoreCheat() || Home.creativeMode) {
			Home.getPlayer().getCurrentLocation().checkBattleStatus();
		}
		Home.messagePanel.addMessage(s + getName() + " has died!");
	}
	/**
	 * Returns the skill that matches this color
	 */
	public int getSkillFor(Color type) {
		// Energy Fields
		if (type.equals(Shields.systemMain) || type.equals(Warp.systemMain)) {
			return getEnergyFieldsLvl();
		}
		// Analysis
		else if (type.equals(MedBay.systemMain) || type.equals(Sensors.systemMain)
				|| type.equals(Comms.systemMain)) {
			return getAnalysisLvl();
		}
		// Power Distribution
		else if (type.equals(Engineering.systemMain) || type.equals(Weapons.systemMain)) {
			return getPowerDistributionLvl();
		}
		// Navigation
		else if (type.equals(HelmSystem.systemMain)
				|| type.equals(Weapons.colorWeaponsMissile)) {
			return getNavigationLvl();
		}
		// default return
		return 0;
	}
	/**
	 * Returns the skill that matches this tile
	 */
	public int getSkillFor(Tile tile) {
		if (tile != null && tile.getTileColor() != null) {
			return getSkillFor(tile.getTileColor());
		} else {
			return 5;
		}
	}
	/**
	 * Returns the skill that matches this item
	 */
	public int getSkillFor(Item item) {
		return getSkillFor(item.getItemType());
	}
	/**
	 * tests if this tile is touching to another tile,
	 * 
	 * @param other
	 * @return
	 */
	public boolean isTouching(int[] current, int[] other) {
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
	 * The crewman breaths. If there is air, increase breath towards max, if
	 * not, decrease breath if out of breath, decrease health
	 */
	public void breathe() {
		breatheCount += 1;
		if (breatheCount > 10) {
			breatheCount = 0;
			// System.out.println(getName() + " is breathing");
			// use up oxygen needed for breathe
			incBreath(-1);
			// how much breath can we breathe in?
			int needed = breathTotal - getBreath();
			// if we need more than the tile has, take what the tile has
			if (needed > getLocationTile().getAirLevel()) {
				needed = getLocationTile().getAirLevel();
			}
			// breathe in
			incBreath(needed);
			getLocationTile().incAirLevel(-needed);
			// take damage if out of breath
			if (getBreath() < 0) {
				incHealthCurrent(getBreath());
				setBreath(0);
			}
		}
	}
	public int[] getLocation() {
		return new int[] { floor, getxPos(), getyPos() };
	}
	public int getxPos() {
		return xPos;
	}
	public void setxPos(int xPos) {
		this.xPos = xPos;
	}
	public int getyPos() {
		return yPos;
	}
	public void setyPos(int yPos) {
		this.yPos = yPos;
	}
	public int getFloor() {
		return floor;
	}
	public void setFloor(int floor) {
		this.floor = floor;
	}
	public Tile getLocationTile() {
		ShipFloorPlan floorPlan = getParentShip().shipFloorplans.get(floor);
		return floorPlan.getTileAt(xPos, yPos);
	}
	public Tile getCurrentTile() {
		return currentTile;
	}
	public String getMannedStationName() {
		if (currentTile != null && currentTile.isSystemTile()) {
			return currentTile.getName();
		}
		return "None";
	}
	public void setCurrentTile(Tile mannedStation) {
		this.currentTile = mannedStation;
		// if we're looking at stations panel and this tile is selected
		if (Home.getCurrentScreen() instanceof GameScreen) {
			CrewPane pane = CrewPane.containsMan(this);
			if (pane != null) {
				pane.updatePane();
			}
		}
	}
	public Ship getParentShip() {
		return parentShip;
	}
	/**
	 * Change's this crewman's parent ship, removing him from the old
	 * parentship's crew list and adding him to the new parentship's crewlist
	 */
	public void setParentShip(Ship parentShip) {
		if (getParentShip() != null) {
			getParentShip().getCrew().remove(this);
		}
		this.parentShip = parentShip;
		if (getParentShip() != null) {
			if (getParentShip().getCrew().contains(this) == false) {
				getParentShip().getCrew().add(this);
			}
		}
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getHealthCurrent() {
		return healthCurrent;
	}
	public void setHealthCurrent(int healthCurrent) {
		//if we're dropping below 20 health, play alert
		if (isPlayerControlled() && getHealthCurrent() >= 50 && healthCurrent < 50){
			Home.messagePanel.addMessage(this, "I need a medic!");
			alertSound.play();
		}
		this.healthCurrent = healthCurrent;
		if (getHealthCurrent() <= 0) {
			this.healthCurrent = 0;
			die();
		}
		// if health is increased above max, update health max
		else if (getHealthCurrent() > getHealthTotal()) {
			setHealthTotal(getHealthCurrent());
		}
		// if we're looking at stations panel and this crew is selected
		if (Home.getCurrentScreen() instanceof GameScreen) {
			CrewPane pane = CrewPane.containsMan(this);
			if (pane != null) {
				pane.updateProgressBar(UIMisc.health, this);
			}
		}
	}
	/**
	 * Increases health by this amount up to total health
	 */
	public void incHealthCurrent(int amount) {
		int val = amount + getHealthCurrent();
		if (val > getHealthTotal()) {
			setHealthCurrent(getHealthTotal());
		} else {
			setHealthCurrent(val);
		}
	}
	public int getHealthTotal() {
		return healthTotal;
	}
	public void setHealthTotal(int healthTotal) {
		this.healthTotal = healthTotal;
	}
	public HashMap<Tile, Integer> getCrewPath() {
		return crewPath;
	}
	public void setCrewPath(HashMap<Tile, Integer> crewPath) {
		this.crewPath = crewPath;
	}
	public int getSkillLVL(Point skill) {
		return skill.x;
	}
	public int getSkillEXP(Point skill) {
		return skill.y;
	}
	public void setSkillLVL(Point skill, int amount) {
		skill.x = amount;
	}
	public void incSkillLVL(Point skill, int amount) {
		skill.x += amount;
	}
	public void decSkillLVL(Point skill, int amount) {
		skill.x -= amount;
	}
	public void setSkillEXP(Point skill, int amount) {
		skill.y = amount;
	}
	public void incSkillEXP(Point skill, int amount) {
		skill.y += amount;
	}
	public void decSkillEXP(Point skill, int amount) {
		skill.y -= amount;
	}
	public Point getEnergyFields() {
		return energyFields;
	}
	public int getEnergyFieldsLvl() {
		return energyFields.x;
	}
	public void setEnergyFields(int amount) {
		setEnergyFields(new Point(amount, 0));
	}
	public void setEnergyFields(Point energyFields) {
		this.energyFields = energyFields;
	}
	public Point getNavigation() {
		return navigation;
	}
	public int getNavigationLvl() {
		return navigation.x;
	}
	public void setNavigation(int amount) {
		setNavigation(new Point(amount, 0));
	}
	public void setNavigation(Point navigation) {
		this.navigation = navigation;
	}
	public Point getAnalysis() {
		return analysis;
	}
	public int getAnalysisLvl() {
		return analysis.x;
	}
	public void setAnalysis(int amount) {
		setAnalysis(new Point(amount, 0));
	}
	public void setAnalysis(Point analysis) {
		this.analysis = analysis;
	}
	public Point getPowerDistribution() {
		return powerDistribution;
	}
	public int getPowerDistributionLvl() {
		return powerDistribution.x;
	}
	public void setPowerDistribution(int powerDistribution) {
		setPowerDistribution(new Point(powerDistribution, 0));
	}
	public void setPowerDistribution(Point powerDistribution) {
		this.powerDistribution = powerDistribution;
	}
	public Color getDivision() {
		return division;
	}
	public void setDivision(Color division) {
		this.division = division;
	}
	public int getBreath() {
		return breath;
	}
	public void setBreath(int breath) {
		this.breath = breath;
		// if we're looking at stations panel and this crew is selected
		if (Home.getCurrentScreen() instanceof GameScreen) {
			CrewPane pane = CrewPane.containsMan(this);
			if (pane != null) {
				pane.updateProgressBar(UIMisc.breath, this);
			}
		}
	}
	/**
	 * Increases breath up to total breath by this amount
	 */
	public void incBreath(int amount) {
		setBreath(getBreath() + amount);
		if (this.breath > breathTotal) {
			setBreath(breathTotal);
		}
	}
	public int getBreathTotal() {
		return breathTotal;
	}
	/**
	 * Return's player controlled, ignoreing if Home.creativemode is on or not
	 * 
	 * @return
	 */
	public boolean isPlayerControlledIgnoreCheat() {
		return playerControlled;
	}
	public boolean isPlayerControlled() {
		// allPlayerControlled let's player control all crewmen
		if (Home.creativeMode) {
			return true;
		}
		return playerControlled;
	}
	public void setPlayerControlled(boolean playerControlled) {
		this.playerControlled = playerControlled;
	}
	public Item getItem() {
		return item;
	}
	public void setItem(Item item) {
		this.item = item;
	}
	public boolean hasItem() {
		return getItem() != null;
	}
	/**
	 * Check to see if the man can pick up the item, picking it up if possible
	 * Return's true if the crewman was able to pick up this item
	 * 
	 * @param item
	 * @return
	 */
	public boolean attemptPickUpItem(Tile tile) {
		// check that the man is touching the tile
//		if (tile != null && tile.getHealth() <= 0
//				&& isTouching(getLocation(), tile.getLocation())) {
		if (tile != null && (!Home.getBattleLoc().isInBattle()
				|| isTouching(getLocation(), tile.getLocation()))) {
			Item item = tile.getItem();
			if (item != null) {
				// The crewman has room for the item,
				// or can swap items (neither item is a floor)
				if (getItem() == null
						|| (!tile.getItem().isFloor() && !getItem().isFloor())) {
					pickUpItem(tile);
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Actually pick up the item (This is run by attemptPickUpItem)
	 * 
	 * @param tile
	 */
	private void pickUpItem(Tile tile) {
		// temp store the creman's item if it exists
		Item tempItem = getItem();
		// add the tile item to the player
		setItem(tile.getItem());
		tile.removeTileItem(tempItem);
		// if the item is a floor, replace it with space
//		if (tile.getItem().isFloor()) {
//			//if crewman is standing here, push him to the side
//			if (tile.getActor() != null){
//				CrewMan m = tile.getActor();
//				Tile shove = tile.findNearestVacantTile();
//				shove.setActor(m);
//				tile.setActor(null);
//			}
//			tile.setAirLevel(0);
//			tile.cloneUpdate(Color.black);
//			tile.removeItem();
//			// System.out.println("Replaced tile with space");
//		}
//		// otherwise replace it with the crewman's item
//		else if (tempItem != null) {
//			// set tile to appropriate tile type
//			tile.cloneUpdate(tempItem.getItemType());
//			// set the new tile's item to the temp item
//			tile.setItem(tempItem);
//			// install to new system
//			tile.connectToProperSystem();
//		}
//		// otherwise replace it with a floor based on the ship's tile set
//		else {
//			// set tile to appropriate tile type
//			tile.cloneUpdate(Color.white);
//			// update item based on tile set or under item id
//			String id = tile.getUnderItem();
//			if (id != null && !id.equals("")) {
//				tile.setItem(Home.resources.getItems().findItem(id));
//			} else {
//				tile.setItem(tile.findDefaultItem(tile.getParentShip().getTileSet()));
//			}
//			tile.connectToProperSystem();
//		}
//		tile.updateImages();
	}
	/**
	 * Check to see if the man can place their item, placing it up if possible
	 * Return's true if the crewman was able to place this item
	 * 
	 * @param item
	 * @return
	 */
	public boolean attemptPlaceItem(Tile tile) {
		//set the item to crewman's item, or cargo item
		Item item = getItem();
		if (item == null && getParentShip().getCargo().isManned()){
			if (Home.getCurrentScreen().getCurrentTile() != null){
				Item ci = Home.getCurrentScreen().getCurrentTile().getItem();
				if (ci instanceof CargoBay){
					item = ((CargoBay) ci).getItem();
				}
			}
			if (item == null){
				item = getParentShip().getCargo().getFirstCargoItem();
			}
			
				
		}
		// check that the man is touching the tile and that the tile is valid
			//or is out of battle
		if (!Home.getBattleLoc().isInBattle()
				|| isTouching(getLocation(), tile.getLocation())) {
			tile.getParentFloorPlan().placeTile(tile, item);
			// Creative mode lets you place unlimited tiles
			if (!Home.creativeMode) {
				//lower crewman or cargo item
				if (getItem() != null){
					setItem(null);
				}
				else {
					getParentShip().getCargo().removeCargoItem(item);		
				}
			}
			
		}
		return false;
	}
	/**
	 * Return's this man's AI, setting the AI's parent to this man
	 */
	public AI getAi() {
		if (ai != null && ai.getMan() == null) {
			ai.setMan(this);
		}
		return ai;
	}
	public void addAIAction(AIAction action) {
		getAi().addAction(action);
	}
	public AIAction getCurrentAction() {
		return getAi().getActions().get(0);
	}
	public void clearAI() {
		getAi().clear();
	}
	/**
	 * Take's the string name of a division and return's that division's color
	 * if bad string, return's command division
	 */
	public static Color divisionStringToColor(String s) {
		if (s.equals("Engineer")) {
			return Engineer;
		} else if (s.equals("Science")) {
			return Science;
		} else if (s.equals("Security")) {
			return Security;
		} else {
			return Command;
		}
	}
	public static Color randDivision() {
		int i = StaticFunctions.randRange(1, 4);
		if (i == 1) {
			return Engineer;
		} else if (i == 2) {
			return Science;
		} else if (i == 3) {
			return Security;
		} else {
			return Command;
		}
	}
}
