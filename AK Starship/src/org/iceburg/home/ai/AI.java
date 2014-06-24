package org.iceburg.home.ai;

import java.util.ArrayList;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.items.CargoBay;
import org.iceburg.home.main.Home;
import org.iceburg.home.main.StaticFunctions;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.ship.systems.CargoHold;
import org.iceburg.home.sound.Sound;

public class AI {
	ArrayList<AIAction> actions;
	// man is set when this ai is 'gotten' from it's parent man
	CrewMan man;
	boolean thinking;
	private static Sound orderSound = new Sound("fx/Yes 1.wav");
	private static Sound orderSound2 = new Sound("fx/Yes 2.wav");
	

	public AI() {
		this.actions = new ArrayList<AIAction>();
	}
	@Override
	public String toString() {
		return "AI ";
	}
	/**
	 * Update the current ai package (as some packages use updates)
	 */
	public void think() {
		if (hasAction()) {
			//run every other cycle
			if (thinking){
				getCurrentAction().updateAction();
			}
			thinking = !thinking;
		}
	}
	public void endAction(AIAction action) {
		actions.remove(action);
		nextAction();
	}
	public void nextAction() {
		if (actions.size() > 0) {
			actions.get(0).startAction();
		}
	}
	public ArrayList<AIAction> getActions() {
		return actions;
	}
	public void setActions(ArrayList<AIAction> actions) {
		this.actions = actions;
	}
	public void addAction(AIAction action) {
		getActions().add(action);
		// if this is the first action, start thinking!
		if (getActions().size() == 1) {
			nextAction();
		}
	}
	public AIAction getCurrentAction() {
		if (getActions().size() > 0) {
			return getActions().get(0);
		}
		return null;
	}
	/**
	 * Return's whether this ai has at least 1 action to perform
	 * 
	 * @return
	 */
	public boolean hasAction() {
		return getActions().size() > 0;
	}
	/**
	 * Return's whether this ai has another action to preform
	 * 
	 * @return
	 */
	public boolean hasNextAction() {
		return getActions().size() > 1;
	}
	/**
	 * Clear's this AI's task list
	 */
	public void clear() {
		setActions(new ArrayList<AIAction>());
	}
	// Action methods
	public void addRepairAction(Tile tile) {
		clear();
		TravelAction.assembleTravelAction(tile, getMan());
		getMan().addAIAction(new RepairAction(getMan().getAi(), tile, getMan(), true));
		playConfirm();
	}
	public void addBreakAction(Tile tile) {
		clear();
		TravelAction.assembleTravelAction(tile, getMan());
		getMan().addAIAction(new RepairAction(getMan().getAi(), tile, getMan(), false));
		playConfirm();
	}
	public void addLootAction(Tile tile) {
		CargoHold hold = getMan().getParentShip().getCargo();
		if (!Home.getBattleLoc().isInBattle() && hold.isManned()){
			
			// find a place to store it and then store it
			CargoBay bay = hold.findVacantCargoBay(tile.getItem());
			if (bay != null){
				bay.addItem(tile.getItem());
				tile.removeTileItem(null);
			}
			
		}
		else{
			getMan().getParentShip().getCargo().assembleLootAI(getMan(), tile);
			playConfirm();
		}
	}
	public void addTakeAction(Tile tile) {
		clear();
		if (tile.getParentShip() != Home.getShip() || Home.getBattleLoc().isInBattle()){
			TravelAction.assembleTravelAction(tile.findSafeAdjacentTileHollow(), getMan());
		}
		getMan().addAIAction(new TakeTileAction(getMan().getAi(), getMan(), tile, true));
		playConfirm();
	}
	public void addPlaceAction(Tile tile) {
		//if all is right, we don't need to leave our post
		if (tile.getParentShip() != Home.getShip() || Home.getBattleLoc().isInBattle() || !tile.getParentShip().getCargo().isManned()){
			clear();
		}
		//need to travel to the spot if in battle, or on enemy ship
		if (tile.getParentShip() != Home.getShip() || Home.getBattleLoc().isInBattle()){
			TravelAction.assembleTravelAction(tile.findSafeAdjacentTileHollow(), getMan());
			playConfirm();
		}
		//if cargo manned, insta place
		if (tile.getParentShip().getCargo().isManned()){
			getMan().attemptPlaceItem(tile);
		}
		else{
			getMan().addAIAction(new TakeTileAction(getMan().getAi(), getMan(), tile, false));
		}
	}
	public void addTravelAction(Tile tile) {
		clear();
		TravelAction.assembleTravelAction(tile, getMan());
		// man the final locaion
		if (man.isPlayerControlledIgnoreCheat()) {
			man.addAIAction(new ManStationAction(man.getAi(), tile, man));
		} else {
			man.addAIAction(new ManStationActionAuto(man.getAi(), tile, man));
		}
		playConfirm();
	}
	public void playConfirm(){
		if (getMan().isPlayerControlledIgnoreCheat()){
			int i = StaticFunctions.randRange(0, 1);
			if (i == 0){
				orderSound.play();
			}
			else{
				orderSound2.play();
			}
		}
	}
	public CrewMan getMan() {
		return man;
	}
	public void setMan(CrewMan man) {
		this.man = man;
	}
}
