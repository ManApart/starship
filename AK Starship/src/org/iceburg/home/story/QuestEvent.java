package org.iceburg.home.story;

import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.items.CargoBay;
import org.iceburg.home.items.Item;
import org.iceburg.home.main.Home;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.ship.systems.CargoHold;
import org.iceburg.home.ship.systems.Comms;
import org.iceburg.home.sound.Sound;
import org.iceburg.home.story.questActions.QuestAction;

public class QuestEvent implements Serializable {
	Quest parent;
	BattleLocation loc;
	//vars ending in F store the data for failed events (see step method)
	String id, unlock, newQuest, sound, unlockF, newQuestF;
	ArrayList<CrewMan> crew, crewF;
	ArrayList<Item> items, itemsF;
	ArrayList<String> preText, postText, arriveText, postTextW, arriveTextW, postTextF, arriveTextF;
	int step, distance;
	public static int timeFactor = 10, distanceFactor = 100;
	long time, waitTime;
	boolean locDistance, locTime, eventSuccess;
	QuestAction action;

	public QuestEvent(String id) {
		this.id = id;
		this.crew = new ArrayList<CrewMan>();
		this.items = new ArrayList<Item>();
		this.preText = new ArrayList<String>();
		this.arriveText = new ArrayList<String>();
		this.postText = new ArrayList<String>();
		this.crewF = new ArrayList<CrewMan>();
		this.itemsF = new ArrayList<Item>();
		this.arriveTextF = new ArrayList<String>();
		this.arriveTextW = new ArrayList<String>();
		this.postTextF = new ArrayList<String>();
		this.postTextW = new ArrayList<String>();
		this.step = 0;
	}
	// @Override
	// public String toString(){
	// return getId();
	// }
	public BattleLocation getLoc() {
		return loc;
	}
	public void setLoc(BattleLocation loc) {
		this.loc = loc;
	}


	public ArrayList<String> getPreText() {
		return preText;
	}
	public void setPreText(ArrayList<String> preText) {
		this.preText = preText;
	}
	public ArrayList<String> getPostText() {
		return postText;
	}
	public void setPostText(ArrayList<String> postText) {
		this.postText = postText;
	}
	
	public ArrayList<String> getArriveText() {
		return arriveText;
	}
	public void setArriveText(ArrayList<String> arriveText) {
		this.arriveText = arriveText;
	}
	public String getUnlock() {
		return unlock;
	}
	public void setUnlock(String unlock) {
		this.unlock = unlock;
	}
	public ArrayList<CrewMan> getCrew() {
		return crew;
	}
	public void setCrew(ArrayList<CrewMan> crew) {
		this.crew = crew;
	}
	public ArrayList<Item> getItems() {
		return items;
	}
	public void setItems(ArrayList<Item> items) {
		this.items = items;
	}
	public Quest getParent() {
		return parent;
	}
	public void setParent(Quest parent) {
		this.parent = parent;
	}
	public int getStep() {
		return step;
	}
	public void setStep(int step) {
		this.step = step;
	}
	
	public long getWaitTime() {
		return waitTime;
	}
	public void setWaitTime(long waitTime) {
		this.waitTime = waitTime;
	}
	
	public String getSound() {
		return sound;
	}
	public void setSound(String sound) {
		this.sound = sound;
	}
	
	public String getUnlockF() {
		return unlockF;
	}
	public void setUnlockF(String unlockF) {
		this.unlockF = unlockF;
	}
	public String getNewQuestF() {
		return newQuestF;
	}
	public void setNewQuestF(String newQuestF) {
		this.newQuestF = newQuestF;
	}
	
	public ArrayList<CrewMan> getCrewF() {
		return crewF;
	}
	public void setCrewF(ArrayList<CrewMan> crewF) {
		this.crewF = crewF;
	}
	public ArrayList<Item> getItemsF() {
		return itemsF;
	}
	public void setItemsF(ArrayList<Item> itemsF) {
		this.itemsF = itemsF;
	}
	
	
	public QuestAction getAction() {
		return action;
	}
	public void setAction(QuestAction action) {
		this.action = action;
	}
	public ArrayList<String> getPostTextF() {
		return postTextF;
	}
	public void setPostTextF(ArrayList<String> postTextF) {
		this.postTextF = postTextF;
	}
	public ArrayList<String> getArriveTextF() {
		return arriveTextF;
	}
	public void setArriveTextF(ArrayList<String> arriveTextF) {
		this.arriveTextF = arriveTextF;
	}
	/**
	 * Start the series of sub events within this event
	 */
	public void start(Quest q) {
		System.out.println("Starting event " + getId());
		this.parent = q;	
		// run event
		step();
		
		
	}
	/**
	 * Marks the event to go to the next step on next quest update
	 */
	public void incStep() {
		setStep(getStep() + 1);
		parent.setAdvanceStep(true);
	}
	/**
	 * Run the next step in the event
	 */
	public void step() {
		if (getStep() == 0) {
			//Pre text
			if (getPreText().size() > 0) {
				for (int i =0; i< getPreText().size(); i++){
					JOptionPane.showMessageDialog(Home.getCurrentScreen(), getPreText().get(i), "", JOptionPane.DEFAULT_OPTION);
				}
			}
			incStep();
//		else if (getStep() == 1) {
//			//sound
//			if (getSound() != null){
//				Sound s = new Sound(getSound());
//				s.play();
//			}
//			incStep();
		}else if (getStep() == 1) {
			incStep();
			
		}else if (getStep() == 2) {
			//gen location, enable warp
			if (getLoc() != null) {
				if (hasDistance()) {
					if (hasTime()) {
						// display time remaining
					}
					// display distance remaining
					getParent().setCanJump(true);
					// inc on arrival (in setdist < 0)
				}
				// no distance means an instant change
				else {
					//Home.getPlayer().setCurrentLocation(getLoc());
					//setStep(1);
					incStep();
				}
			}
			// no loc in this event, skip ahead
			else {
				incStep();
			}
		}else if (getStep() == 3) {
			// we have a battle loc, and have arrived to it
			if (getLoc() != null){
				getParent().setJumping(false);
				setEventSuccess(true);
				Home.getPlayer().setCurrentLocation(getLoc());
				if (hasTime()) {
					setHasTime(false);
					if (getTime() < 0) {
						setEventSuccess(false);
					}
				}
			}
			incStep();
			
		} else if (getStep() == 4) {
			//arrive text
			if (isEventSuccess()){
				if (getArriveTextW().size() > 0) {
					for (int i =0; i< getArriveTextW().size(); i++){
						JOptionPane.showMessageDialog(Home.getCurrentScreen(), getArriveTextW().get(i), "", JOptionPane.DEFAULT_OPTION);
					}
				}
			}
			else{
				if (getArriveTextF().size() > 0) {
					for (int i =0; i< getArriveTextF().size(); i++){
						JOptionPane.showMessageDialog(Home.getCurrentScreen(), getArriveTextF().get(i), "", JOptionPane.DEFAULT_OPTION);
					}
				}
			}
			if (getArriveText().size() > 0) {
				for (int i =0; i< getArriveText().size(); i++){
					JOptionPane.showMessageDialog(Home.getCurrentScreen(), getArriveText().get(i), "", JOptionPane.DEFAULT_OPTION);
				}
			}
			incStep();
		} else if (getStep() == 5) {
			//sound
			if (getSound() != null){
				Sound s = new Sound(getSound());
				s.play();
			}
			incStep();
		} else if (getStep() == 6) {
			//step waits for battle to end
			if (getLoc() != null){
				//if we're not in battle go to next step
				Home.getPlayer().getCurrentLocation().checkBattleStatus();
				//otherwise wait for last enemy crewman's death, which triggers check again
			}
			//no battle loc, so skip step
			else{
				incStep();
			}
		} else if (getStep() == 7) {
			//post text
			if (isEventSuccess()){
				if (getPostTextW().size() > 0) {
					for (int i =0; i< getPostTextW().size(); i++){
						JOptionPane.showMessageDialog(Home.getCurrentScreen(), getPostTextW().get(i), "", JOptionPane.DEFAULT_OPTION);
					}
				}
			}else{
				if (getPostTextF().size() > 0) {
					for (int i =0; i< getPostTextF().size(); i++){
						JOptionPane.showMessageDialog(Home.getCurrentScreen(), getPostTextF().get(i), "", JOptionPane.DEFAULT_OPTION);
					}
				}
			}
			if (getPostText().size() > 0) {
				for (int i =0; i< getPostText().size(); i++){
					JOptionPane.showMessageDialog(Home.getCurrentScreen(), getPostText().get(i), "", JOptionPane.DEFAULT_OPTION);
				}
			}
			incStep();
		} else if (getStep() == 8) {
			//unlock ship
			if (isEventSuccess()){
				if (getUnlock() != null) {
					Home.getProfile().unlockShip(getUnlock());
				}
			} else{
				if (getUnlockF() != null) {
					Home.getProfile().unlockShip(getUnlockF());
				}
			}
			incStep();
		} else if (getStep() == 9) {
			//add crew
			if (isEventSuccess()){
				if (getCrew().size() > 0) {
					// add the crew to the ship (TODO - make dismiss option)
					for (int i = 0; i < getCrew().size(); i++) {
						Home.getShip().addCrewMan(getCrew().get(i), null);
					}
				}
			} else{
				if (getCrewF().size() > 0) {
					// add the crew to the ship (TODO - make dismiss option)
					for (int i = 0; i < getCrewF().size(); i++) {
						Home.getShip().addCrewMan(getCrewF().get(i), null);
					}
				}
			}
			incStep();
		} else if (getStep() == 10) {
			//add items
			if (isEventSuccess()){
				if (getItems().size() > 0) {
					CargoHold ch = Home.getShip().getCargo();
					for (int i = 0; i < getItems().size(); i++) {
						CargoBay cb = ch.findVacantCargoBay(getItems().get(i));
						// keep going until we fail to add an item
						if (!(cb != null && cb.addItem(getItems().get(i)))) {
							break;
						}
					}
				}
			} else{
				if (getItemsF().size() > 0) {
					CargoHold ch = Home.getShip().getCargo();
					for (int i = 0; i < getItemsF().size(); i++) {
						CargoBay cb = ch.findVacantCargoBay(getItemsF().get(i));
						// keep goign until we fail to add an item
						if (!(cb != null && cb.addItem(getItemsF().get(i)))) {
							break;
						}
					}
				}
			}
			incStep();
		} else if (getStep() == 11) {
			//add new quest
			//System.out.println("Quest Event: Step next quest");
			if (isEventSuccess()){
				if (getNewQuest() != null){
					Home.getPlayer().setQuest(Home.resources.getQuests().parseQuest(getNewQuest()));
				}
				else{
					incStep();
				}
			}else{
				if (getNewQuestF() != null){
					Home.getPlayer().setQuest(Home.resources.getQuests().parseQuest(getNewQuestF()));
				}
				else{
					incStep();
				}
			}

		}
		// end the event, start the next one in the quest
		else {
			getParent().nextEvent();
		}
	}
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public long getDistance() {
		return distance;
	}
	public long getDistanceDisplay() {
		return getDistance() / distanceFactor + 1;
	}
	public void setDistance(int distance) {
		this.distance = distance;
		// we've arrived
		//only inc step once: only if we haven't arrived at the new loc yet
//		if (distance < 0 ) {
		if (distance < 0 && hasDistance()) {
			setHasDistance(false);
			incStep();
		}
		Tile temp = Home.getCurrentScreen().getCurrentTilePane().getTile();
		if (temp != null && temp.getTileColor().equals(Comms.systemMain)) {
			Home.getCurrentScreen().getCurrentTilePane().updatePanel("Distance:", "Distance: "
					+ getDistanceDisplay());
		}
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
		Tile temp = Home.getCurrentScreen().getCurrentTilePane().getTile();
		if (temp != null && temp.getTileColor().equals(Comms.systemMain)) {
			Home.getCurrentScreen().getCurrentTilePane().updatePanel("Time left: ", "Time left: "
					+ Comms.timeToString(getTime()));
		}
	}
	public boolean hasDistance() {
		return locDistance;
	}
	public void setHasDistance(boolean locDistance) {
		this.locDistance = locDistance;
	}
	public boolean hasTime() {
		return locTime;
	}
	public void setHasTime(boolean locTime) {
		this.locTime = locTime;
	}
	
	public boolean isEventSuccess() {
		return eventSuccess;
	}
	public void setEventSuccess(boolean eventSuccess) {
		this.eventSuccess = eventSuccess;
	}
	public String getNewQuest() {
		return newQuest;
	}
	public void setNewQuest(String newQuest) {
		this.newQuest = newQuest;
	}
	public ArrayList<String> getPostTextW() {
		return postTextW;
	}
	public void setPostTextW(ArrayList<String> postTextW) {
		this.postTextW = postTextW;
	}
	public ArrayList<String> getArriveTextW() {
		return arriveTextW;
	}
	public void setArriveTextW(ArrayList<String> arriveTextW) {
		this.arriveTextW = arriveTextW;
	}
	
}
