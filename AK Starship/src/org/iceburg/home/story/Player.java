package org.iceburg.home.story;

import org.iceburg.home.main.Home;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.ui.GameScreen;

//this is read and written as the player save
public class Player {
	Ship playerShip;
	BattleLocation currentLocation;
	Quest quest;

	public Player() {
		this(null);
	}
	public Player(Ship s) {
		this.playerShip = s;
		s.makeCrewFriendly();
		// set a default location in case quest hasn't loaded
		// TODO - should do some unique location, like a home base
//		 setCurrentLocation(BattleLocation.spaceLocation());
		// TODO This sets a default quest, should be replaced by a main screen
		// allowing the player to choose a quest
//		this.quest = Home.resources.getQuests().parseQuest("testQuest");
		this.quest = Home.resources.getQuests().parseQuest("tutorialQuest");

	}
	public Ship getPlayerShip() {
		return playerShip;
	}
	public void setPlayerShip(Ship playerShip) {
		this.playerShip = playerShip;
	}
	public BattleLocation getCurrentLocation() {
		return currentLocation;
	}
	public void setCurrentLocation(BattleLocation currentLocation) {
		// parse images before setting as location
		currentLocation.genImages();
		Home.getPlayer().getPlayerShip().getHelm().setTargetShip(null);
		this.currentLocation = currentLocation;
		this.currentLocation.addShip(getPlayerShip());
		Home.getPlayer().getPlayerShip().getWarp().setSpeed(0);
		if (Home.getPlayer().getQuest() != null){
			Home.getPlayer().getQuest().setCanJump(false);
		}
	}
	public Quest getQuest() {
		return quest;
	}
	public void setQuest(Quest quest) {
		if (this.quest != null){
			Home.messagePanel.addMessage("Finished quest: " + this.quest.getName());
		}
		this.quest = quest;
		if (quest != null){
			this.quest.nextEvent();
		}
	}
	/**
	 * Runs when all the player's crew is dead
	 */
	public void gameOver(){
		Home.messagePanel.addMessage("Game Over!");
		Home.getCurrentScreen().setCurrentView(GameScreen.viewComputer);
	}

	
	
}
