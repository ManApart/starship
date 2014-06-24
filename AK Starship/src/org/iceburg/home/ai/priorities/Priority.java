package org.iceburg.home.ai.priorities;

import java.util.ArrayList;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.ai.NPCAI;
import org.iceburg.home.ship.Ship;

public abstract class Priority {
	boolean inRoute;
	Ship ship;
	NPCAI parent;

	public Priority(NPCAI p, Ship s) {
		this.parent = p;
		this.ship = s;
	}
	/**
	 * Overriden by children, return's whether this priority is currently beeing
	 * met
	 */
	public boolean isMet() {
		return true;
	}
	/**
	 * Overriden by children, return's whether this priority's secondary check
	 * is currently being met Secondary checks may be used lower in the priority
	 * list. For example, the weapons isMet may check to see that at least 1
	 * weapon is on. It's secondary check may see if all weapons are on.
	 */
	public boolean isSecondaryMet() {
		return true;
	}
	public boolean isInRoute() {
		return inRoute;
	}
	public void setInRoute(boolean inRoute) {
		this.inRoute = inRoute;
	}
	/**
	 * Overriden by children, running sets a crewman to meeting this priorities
	 * condition
	 */
	public void meetPriority(CrewMan man) {
	}
	/**
	 * Overridden. Clears any members that meet this priority from the NPC's
	 * crew list
	 */
	public void clearMembers() {
	}
	public ArrayList<CrewMan> getCrew() {
		return parent.getCrew();
	}
}
