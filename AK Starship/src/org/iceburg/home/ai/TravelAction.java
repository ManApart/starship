package org.iceburg.home.ai;

import java.util.ArrayList;
import java.util.HashMap;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.main.Constants;
import org.iceburg.home.main.Home;
import org.iceburg.home.ship.ShipFloorPlan;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.ship.systems.Doors;
import org.iceburg.home.ui.MessageCenter;

//TODO - test/debug travel to new ship
//-test picking up items/ placing them - underitem work?
//A travelAction is one step, if there are multiple floors or ships, it will take multiple
//travel actions to reach the destination
public class TravelAction extends AIAction {
	public Tile goal, start;
	// Crewman's walking path
	public HashMap<Tile, Integer> crewPath;

	public TravelAction(AI parent, Tile goal, Tile start, CrewMan man) {
		super(parent);
		this.man = man;
		// this.endLoc = endLoc;
		this.goal = goal;
		this.start = start;
	}
	@Override
	public String toString() {
		return "Travel to " + goal.getName();
	}
	@Override
	public void startAction() {
		// fail if the goal doesn't exist, or we're already there,
		// or we don't start where we thought we would
		if (goal == null || start == null
				|| (start != null && !start.isTouching(man.getLocationTile()))) {
			// travel failed because no valid space
			Home.messagePanel.addMessage(getMan(), MessageCenter.typeDefaultResponse, false);
			endAction();
		} else if (start != goal) {
			choosePath(goal);
		} else {
			endAction();
		}
	}
	@Override
	public void updateAction() {
		takePathStep();
	}
	@Override
	public void endAction() {
		parent.endAction(this);
		// System.out.println("Travel ended");
	}
	public void failTravel() {
		Home.messagePanel.addMessage(getMan(), MessageCenter.typeCanTravel, false, goal.getName());
		endAction();
	}
	/**
	 * Assemble the list of instructions (TravelActions) for traveling to a
	 * specific location
	 */
	public static void assembleTravelAction(Tile goal, CrewMan man) {
		// this tile represents where the crewman is along his way
		Tile start = man.getLocationTile();
		assembleTravelAction(goal, man, start);
	}
	public static void assembleTravelAction(Tile goal, CrewMan man, Tile start) {
		Tile manGoal = goal;
		if (goal == null) {
			failAssembleTravel(man, goal);
		} else {
			// if the tile is blocked or unsafe, stand beside it
			if (!goal.isSafe() || goal.isBlocked()) {
				goal = goal.findSafeAdjacentTile(man.getLocationTile());
			}
			if (goal == null || (goal != null && !goal.isSafe())) {
				failAssembleTravel(man, goal);
				return;
			}
			// a temp goal/ the goal for each step
			Tile tempGoal = goal;
			// break the whole trip into single steps and add a travel action
			// for
			// each step
			// different ships- find air locks
			if (goal.getParentShip() != start.getParentShip()) {
				// can we get off of our ship?
				// look for air locks
				Tile airLock = findNearestAirLock(start);
				// couldn't find a way off of the ship
				if (airLock == null) {
					failAssembleTravel(man, tempGoal);
					return;
				}
				// grab the tile next to the air lock
				if (!airLock.isSafe()) {
					airLock = airLock.findSafeAdjacentTile(start);
				}
				if (airLock == null) {
					failAssembleTravel(man, tempGoal);
					return;
				}
				// if airlock on different floors, use turbo tubes to get there
				if (airLock.getFloor() != start.getFloor()) {
					// find turbo tube nearest to the crewman
					tempGoal = findNearestTurboTubeOnFloor(start);
					// couldn't find a path to the turbo tube
					if (tempGoal == null) {
						failAssembleTravel(man, goal);
						return;
					}
					// travel to the turbo tube from start
					man.addAIAction(new TravelAction(man.getAi(), tempGoal, start, man));
					// the crewman is now standing at the turbo tube
					start = tempGoal;
					// find the nearest turbo tube to the airlock
					tempGoal = findNearestTurboTubeOnFloor(airLock);
					// if no turbo tube on this floor, we're sunk
					if (tempGoal == null) {
						failAssembleTravel(man, goal);
						return;
					}
					// add the turbo tube transport action
					man.addAIAction(new TransportAction(man.getAi(), tempGoal, start, man));
					// the crewman is now at the recieveing turbo tube
					start = tempGoal;
				}
				// travel to the airlock
				man.addAIAction(new TravelAction(man.getAi(), airLock, start, man));
				// the man is now at the air lock
				start = airLock;
				// find the airlock on the other ship that is closest to the
				// goal
				tempGoal = findNearestAirLock(goal);
				// couldn't find a way onto the ship
				if (tempGoal == null) {
					failAssembleTravel(man, tempGoal);
					return;
				}
				if (!tempGoal.isSafe()) {
					tempGoal = tempGoal.findSafeAdjacentTile(start);
				}
				if (tempGoal == null) {
					failAssembleTravel(man, goal);
					return;
				}
				// use the airLock to get to the other ship
				man.addAIAction(new TransportAction(man.getAi(), tempGoal, airLock, man));
				start = tempGoal;
				if (start.getFloor() != goal.getFloor()) {
					tempGoal = findNearestTurboTubeOnFloor(start);
					// couldn't find a path to the turbo tube
					if (tempGoal == null) {
						failAssembleTravel(man, tempGoal);
						return;
					}
					// travel to the turbo tube from start
					man.addAIAction(new TravelAction(man.getAi(), tempGoal, start, man));
					// the crewman is now standing at the turbo tube
					start = tempGoal;
					// find the nearest turbo tube to the goal
					tempGoal = findNearestTurboTubeOnFloor(goal);
					// if no turbo tube on this floor, we're sunk
					if (tempGoal == null) {
						failAssembleTravel(man, goal);
						return;
					}
					// add the turbo tube transport action
					man.addAIAction(new TransportAction(man.getAi(), tempGoal, start, man));
					// the crewman is now at the recieveing turbo tube
					start = tempGoal;
				}
				// finally, travel to the final location
				man.addAIAction(new TravelAction(man.getAi(), goal, start, man));
			}
			// Same ship
			else {
				if (start.getFloor() != goal.getFloor()) {
					tempGoal = findNearestTurboTubeOnFloor(start);
					// couldn't find a path to the turbo tube
					if (tempGoal == null) {
						failAssembleTravel(man, tempGoal);
						return;
					}
					// travel to the turbo tube from start
					man.addAIAction(new TravelAction(man.getAi(), tempGoal, start, man));
					// the crewman is now standing at the turbo tube
					start = tempGoal;
					// find the nearest turbo tube to the goal
					tempGoal = findNearestTurboTubeOnFloor(goal);
					// if no turbo tube on this floor, we're sunk
					if (tempGoal == null) {
						failAssembleTravel(man, goal);
						return;
					}
					// add the turbo tube transport action
					man.addAIAction(new TransportAction(man.getAi(), tempGoal, start, man));
					// the crewman is now at the recieveing turbo tube
					start = tempGoal;
				}
				// finally, travel to the final location
				man.addAIAction(new TravelAction(man.getAi(), goal, start, man));
			}
			// man the final locaion
			// if (man.isPlayerControlledIgnoreCheat()){
			// man.addAIAction(new ManStationAction(man.getAi(), manGoal, man));
			// }
			// else{
			// man.addAIAction(new ManStationActionAuto(man.getAi(), manGoal,
			// man));
			// }
			// report that the planning was successful
			Home.messagePanel.addMessage(man, MessageCenter.typeCanTravel, true, goal.getName());
		}
	}
	private static void failAssembleTravel(CrewMan man, Tile goal) {
		String s = "location";
		if (goal != null) {
			s = goal.getName();
		}
		Home.messagePanel.addMessage(man, MessageCenter.typeCanTravel, false, s);
		man.getAi().clear();
	}
	// create a path from current location to this step's goal tile
	public void choosePath(Tile goal) {
		if (goal != null && goal.isSafe()) {
			crewPath = ShipFloorPlan.findTileCosts(goal);
		} else {
			failTravel();
			return;
		}
	}
	/**
	 * if the crewman has a target location, they take a step toward that
	 * location or if they don't have a path, they re-evaluate their path to
	 * their target
	 */
	public void takePathStep() {
		// isTraveling = true;
		// find the tile we're standing on
		ShipFloorPlan floorPlan = man.getParentShip().getFloorplans().get(man.getFloor());
		Tile current = floorPlan.getTileAt(man.getxPos(), man.getyPos());
		// teleport people out of walls
		if (current.isBlocked() && !current.isDoor()) {
			Tile temp = current.findSafeAdjacentTile();
			current.setActor(null);
			temp.setActor(man);
			crewPath.remove(current);
		}
		int cost = 0;
		// TODO - shouldn't need this check?
		if (crewPath != null && crewPath.containsKey(current)) {
			cost = crewPath.get(current);
		}
		// IF path is null, re-evalute path based on target.
		// TODO But only re-evaluate if not touching endLoc
		else if (crewPath == null) {
			// failTravel();
			return;
		}
		if (cost > 0) {
			Tile destTile = current;
			int addX = 0, addY = 0;
			// unman any previous station
			if (man.getCurrentTile() != null) {
				man.getCurrentTile().endManStation(man);
				man.setCurrentTile(null);
			}
			// evaluate the neighbor squares
			for (int x = -1; x < 2; x++) {
				for (int y = -1; y < 2; y++) {
					if (((x == 0) && (y == 0)) || x == y || x == -y) {
						continue;
					}
					int xVal = current.getX() + x;
					int yVal = current.getY() + y;
					Tile prospect = floorPlan.getTileAt(xVal, yVal);
					// find the neighbor with the lowest cost
					if (crewPath.containsKey(prospect) && crewPath.get(prospect) <= cost
					// && prospect.getActor() == null
					// and its not a wall (but could be a closed door)
							&& (prospect.isBlocked() == false || prospect.getTileColor().equals(Doors.systemMain))) {
						destTile = prospect;
						cost = crewPath.get(prospect);
						addX = x;
						addY = y;
					}
				}
			}
			// We're moving vertically, so center horizontal
			int inc = Math.max(Constants.shipSquare / 10, 0);
			if (addX == 0 && man.subX != Constants.shipSquare / 2) {
				if (man.subX > 0) {
					man.subX -= inc;
				} else if (man.subX < 0) {
					man.subX += inc;
				}
			} else if (addY == 0 && man.subY != Constants.shipSquare / 2) {
				if (man.subY > 0) {
					man.subY -= inc;
				} else if (man.subY < 0) {
					man.subY += inc;
				}
			}
			// update with subposition for smooth motion
			man.subX += addX * inc;
			man.subY += addY * inc;
			boolean moveTile = false;
			if (man.subX >= Constants.shipSquare / 2) {
				man.subX = (int) (-Constants.shipSquare / 2 + 1);
				moveTile = true;
			} else if (man.subX <= -Constants.shipSquare / 2) {
				man.subX = (int) (Constants.shipSquare / 2 - 1);
				moveTile = true;
			}
			if (man.subY >= Constants.shipSquare / 2) {
				man.subY = (int) (-Constants.shipSquare / 2 + 1);
				moveTile = true;
			} else if (man.subY <= -Constants.shipSquare / 2) {
				man.subY = (int) (Constants.shipSquare / 2 - 1);
				moveTile = true;
			}
			// move to a tile with a lower cost
			if (moveTile && (destTile.getActor() == null || destTile.getActor() == man)) {
				// System.out.println("move");
				current.setActor(null);
				destTile.setActor(man);
				crewPath.remove(current);
			}
			// move 'through' or 'around' other crewman
			else if ((!moveTile && destTile.getActor() != null)) {
				// System.out.println("TravelAction: Shove");
				Tile temp = current.findOppositeTile(destTile);
				if (temp != null) {
					current.setActor(null);
					temp.setActor(man);
					crewPath.remove(current);
				}
			}
		} else if (man.subX != 0 || man.subY != 0) {
			// System.out.println("Sub x not right!");
			if (man.subX > 0) {
				man.subX -= 1;
			} else if (man.subX < 0) {
				man.subX += 1;
			}
			if (man.subY > 0) {
				man.subY -= 1;
			} else if (man.subY < 0) {
				man.subY += 1;
			}
		}
		// Done Moving
		else if (man.getLocationTile().isTouching(goal)) {
			endAction();
		}
	}
	/**
	 * Return the nearest turbo tube (to the start tile) that is on the start
	 * tile's floor
	 */
	public static Tile findNearestTurboTubeOnFloor(Tile start) {
		ArrayList<Tile> list = start.getParentShip().getTurboTubes().getStationTilesOnFloor(start.getFloor());
		return findNearestTileInList(list, start);
	}
	/**
	 * Return the nearest turbo tube that is on the start tile's floor
	 */
	private static Tile findNearestAirLockOnFloor(Tile start) {
		ArrayList<Tile> list = start.getParentShip().getDoorSystem().getAirLocksOnFloor(start.getFloor());
		return findNearestTileInList(list, start);
	}
	/**
	 * Returns the nearest air lock, looking for an airlock on the start tile
	 * floor first
	 */
	public static Tile findNearestAirLock(Tile start) {
		// Tile retTile = findNearestAirLockOnFloor(start);
		// if (retTile != null){
		// return retTile;
		// }
		// else{
		ArrayList<Tile> list = start.getParentShip().getDoorSystem().getAirLocks();
		return findNearestTileInList(list, start);
		// }
	}
	public static Tile findNearestTileInList(ArrayList<Tile> list, Tile targetTile) {
		Tile retTile = null;
		Tile current = targetTile;
		// if we're currently at the proper tile, return it!
		if (list.contains(current)) {
			retTile = current;
			return retTile;
		} else {
			// for each of the sysTiles, note the cost to the goal
			int mainCost = -1;
			HashMap<Tile, Integer> path = ShipFloorPlan.findTileCosts(targetTile);
			for (int i = 0; i < list.size(); i++) {
				// this nice little check avoids a crash when clicking on a
				// non-safe tile a floor away
				int cost = mainCost + 1;
				if (path.get(current) != null) {
					cost = path.get(current);
				}
				// set first mainCost to cost
				if (mainCost == -1) {
					mainCost = cost;
				}
				if (cost <= mainCost) {
					retTile = list.get(i);
				}
			}
		}
		return retTile;
	}
	// public static Tile findNearestTileInList(ArrayList<Tile> list, Tile
	// targetTile){
	// Tile retTile = null;
	// Tile current = targetTile;
	// //if we're currently at the proper tile, return it!
	// if (list.contains(current)) {
	// retTile = current;
	// return retTile;
	// } else {
	// // for each of the sysTiles, note the cost to the goal
	// int mainCost = -1;
	// for (int i = 0; i < list.size(); i++) {
	//
	// HashMap<Tile, Integer> path = ShipFloorPlan.findTileCosts(list.get(i));
	// // this nice little check avoids a crash when clicking on a
	// // non-safe tile a floor away
	// int cost = mainCost + 1;
	// if (path.get(current) != null) {
	// cost = path.get(current);
	// }
	// // set first mainCost to cost
	// if (mainCost == -1) {
	// mainCost = cost;
	// }
	// if (cost <= mainCost) {
	// retTile = list.get(i);
	// }
	// }
	// }
	// return retTile;
	// }
	public HashMap<Tile, Integer> getCrewPath() {
		return man.getCrewPath();
	}
	// public void setCrewPath(HashMap<Tile, Integer> crewPath) {
	// //this.crewPath = crewPath;
	// //man.setCrewPath(crewPath);
	// }
	public Tile getGoal() {
		return goal;
	}
	public void setGoal(Tile goal) {
		this.goal = goal;
	}
	public Tile getStart() {
		return start;
	}
	public void setStart(Tile start) {
		this.start = start;
	}
}
