package org.iceburg.home.story;

import java.io.Serializable;
import java.util.ArrayList;

import org.iceburg.home.main.Home;
import org.iceburg.home.persistance.QuestParser;

public class Quest implements Serializable {
	// quest progress, distance to next event, time till next attack
	// dist/time will only display if the next event has a timer/distance
	int progress, timer;
	// whether the next event has a distance and time, we should update, the
	// player arrived in time
	boolean advanceStep;
	// can jump means we have a location to travel to, isjumping means we're
	// moving through hyperspace
	boolean canJump, jumping;
	String id, title;
	ArrayList<String> events;
	QuestEvent currentEvent;

	public Quest(String id, String title) {
		this.id = id;
		this.title = title;
		this.events = new ArrayList<String>();
		this.progress = -1;
	}
	public int getProgress() {
		return progress;
	}
	public void setProgress(int progress) {
		this.progress = progress;
	}
	public void incProgress() {
		setProgress(getProgress() + 1);
	}
	public String getName() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public ArrayList<String> getEvents() {
		return events;
	}
	public String getEventN(int n) {
		return getEvents().get(n);
	}
	public void setEvents(ArrayList<String> events) {
		this.events = events;
	}
	public QuestEvent getCurrentEvent() {
		return currentEvent;
	}
	public void setCurrentEvent(QuestEvent currentEvent) {
		this.currentEvent = currentEvent;
	}
	public boolean isAdvanceStep() {
		return advanceStep;
	}
	public void setAdvanceStep(boolean advanceStep) {
		this.advanceStep = advanceStep;
	}
	public boolean isCanJump() {
		return canJump;
	}
	public void setCanJump(boolean canJump) {
		this.canJump = canJump;
		Home.getCurrentScreen().getCurrentTilePane().updatePane();
	}
	public boolean isJumping() {
		return jumping;
	}
	public void setJumping(boolean jumping) {
		this.jumping = jumping;
	}
	public void update() {
		if (timer < 10) {
			timer += 1;
		}
		// runs every 1/10 a second
		else {
			timer = 0;
			if (isJumping()) {
				getCurrentEvent().setDistance((int) (getCurrentEvent().getDistance() - Home.getShip().getWarp().getSpeed()));
			}
			if (getCurrentEvent() == null) {
				System.out.println("Quest: Event null");
				nextEvent();
				
			}
			if (getCurrentEvent() != null && getCurrentEvent().hasTime()) {
				getCurrentEvent().setTime(getCurrentEvent().getTime() - 1);
			}
			if (isAdvanceStep()) {
				setAdvanceStep(false);
				step();
			}
		}
	}
	/**
	 * Runs this quest's next event, if there is one
	 */
	public void nextEvent() {
		incProgress();
		// announce quest on start
		if (getProgress() == 0) {
			Home.messagePanel.addMessage("Started quest: " + getName());
		}
		// only run the next step in the quest if there actually is a next step
		if (getProgress() < getEvents().size()) {
			QuestEvent qe = QuestParser.parseEvent(getEventN(getProgress()), this);
			setCurrentEvent(qe);
			// if we have an event, run it, if not, start the next one
			if (getCurrentEvent() != null) {
				getCurrentEvent().start(this);
			} else {
				nextEvent();
			}
		} else {
			Home.getPlayer().setQuest(null);
		}
	}
	/**
	 * Steps the current event
	 */
	public void step() {
		getCurrentEvent().step();
	}
}
