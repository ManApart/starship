package org.iceburg.home.ship.systems;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.ai.HelmAction;
import org.iceburg.home.items.HelmItem;
import org.iceburg.home.items.Item;
import org.iceburg.home.main.Home;
import org.iceburg.home.main.StaticFunctions;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.story.BattleLocation;
import org.iceburg.home.ui.GameScreen;
import org.iceburg.home.ui.MessageCenter;
import org.iceburg.home.ui.PopUpBox;
import org.iceburg.home.ui.TilePane;
import org.iceburg.home.ui.UIMisc;

public class HelmSystem extends ShipSystem {
	public static Color systemMain = Color.decode("#8bc676");
	// directions. Fore is the front of the ship, or 0 degrees (aft is 180)
	public static int fore = 0, aft = 1, port = 2, starboard = 3, none = 4;
	// position of our ship compared to the enemy,
	// direction other ship is from ours (we want the enemy to be on what side
	// of us?)
	public int position, direction, angleToTarget;
	private JButton btnPosition, btnDirection, btnFore, btnAft, btnPort, btnStarboard,
			btnNone, btnAutoPilot, btnJumpWarp;
	private boolean adjustPosition, autoPilot;
	private PopUpBox pop;
	private Warp targetShip;

	public HelmSystem(Ship ship) {
		super(ship);
		this.tileTypes.add(systemMain);
		this.name = "Helm";
		this.position = none;
		this.direction = none;
		this.autoPilot = true;
	}
	@Override
	public void paintTileInfo(TilePane p, Item item) {
		UIMisc.addJLabel("Bearing: " + getWarp().getBearing()+ "\u00B0", p);
		if (hasTargetShip()){
			UIMisc.addJLabel("Target at: " + getTargetShip().getBearing()+ "\u00B0", p);
		}
		String s = MessageCenter.colorString("On", Color.green);
		if (!isAutoPilot()) {
			s = MessageCenter.colorString("Off", Color.red);
		}
		UIMisc.addJLabel("Auto " + s, p);
		if (getPosition() != none) {
			UIMisc.addJLabel("Aproach their: " + getPositionString(), p);
		}
		if (getDirection() != none) {
			UIMisc.addJLabel("Face them with: " + getDirectionString(), p);
		}
	}
	@Override
	public ArrayList<JButton> systemOptionOverride(ActionListener comp, Tile tile) {
		ArrayList<JButton> list = new ArrayList<JButton>();
		btnPosition = UIMisc.createJButton("Aproach", comp);
		btnDirection = UIMisc.createJButton("Face with", comp);
		String s = "Auto on";
		if (isAutoPilot()) {
			s = "Auto off";
		}
		btnAutoPilot = UIMisc.createJButton(s, comp);
		btnJumpWarp = UIMisc.createJButton("Jump to Warp", comp);
		if (isManned()) {
			list.add(btnPosition);
			list.add(btnDirection);
			list.add(btnAutoPilot);
			if ((Home.getPlayer().getQuest() != null && Home.getPlayer().getQuest().isCanJump() && tile.isOnline())) {
				list.add(btnJumpWarp);
			}
		}
		return list;
	}
	@Override
	public void getButtonPress(JButton button, Tile source) {
		// what are we setting?
		if (button.getText().equals(btnPosition.getText())) {
			adjustPosition = true;
			directionPopUp(source);
		} else if (button.getText().equals(btnDirection.getText())) {
			adjustPosition = false;
			directionPopUp(source);
			// What direction?
		} else if (button.getText().equals(btnAutoPilot.getText())) {
			setAutoPilot(!isAutoPilot());
			if (Home.getCurrentScreen() instanceof GameScreen) {
				TilePane pane = TilePane.containsTile(source);
				if (pane != null) {
					String s = MessageCenter.colorString("On", Color.green);
					if (!isAutoPilot()) {
						s = MessageCenter.colorString("Off", Color.red);
					}
					pane.updatePanel("Auto", "Auto " + s);
					pane.updatePane();
				}
			}
		} else if (button.getText().equals(btnJumpWarp.getText())) {
			if (source.isPowered()) {
				if (getParentShip().getCabins().withinCapacity()){
					boolean confirm = true;
					// if jump to warp on enemy ship, confirm that you want to
					// commendeer
					if (Home.getShip() != source.getParentShip()) {
						confirm = false;
						int response = JOptionPane.showConfirmDialog(null, "Take this ship and leave any crew on your old ship behind?", "Confirm", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION);
						if (response == 0) {
							confirm = true;
							Home.getPlayer().setPlayerShip(source.getParentShip());
						}
					}
					if (confirm) {
						Home.getPlayer().getQuest().setJumping(true);
						Home.getPlayer().setCurrentLocation(BattleLocation.spaceLocation());
						Home.getShip().getWarp().attemptMaxSpeed();
					}
				}
				else{
					Home.messagePanel.addMessage(getFirstManningCrew(), "We have too many crew! Dismiss a crewman or install a cabin.");
				}
			}
		} else if (button.getText().equals(btnFore.getText())) {
			removePopUpBox();
			// System.out.println("Fore");
			updatePosDir(fore, source);
		} else if (button.getText().equals(btnAft.getText())) {
			removePopUpBox();
			// System.out.println("Aft");
			updatePosDir(aft, source);
		} else if (button.getText().equals(btnPort.getText())) {
			removePopUpBox();
			// System.out.println("Port");
			updatePosDir(port, source);
		} else if (button.getText().equals(btnStarboard.getText())) {
			removePopUpBox();
			// System.out.println("Starboard");
			updatePosDir(starboard, source);
		} else if (button.getText().equals(btnNone.getText())) {
			removePopUpBox();
			// System.out.println("Don't care");
			updatePosDir(none, source);
		}
	}
	/**
	 * Update direction or position, based on our boolean we set earlier
	 */
	public void updatePosDir(int i, Tile source) {
		if (adjustPosition) {
			setPosition(i);
		} else {
			setDirection(i);
		}
		// update the tile pane
		updateTilePane(source);
	}
	@Override
	public void manSystem(CrewMan man) {
		// if the crewman is manning a console, give him the healing action
		if (man.getCurrentTile().getTileColor().equals(systemMain)) {
			man.addAIAction(new HelmAction(man, man.getAi(), man.getCurrentTile()));
		}
	}
	@Override
	public void manSystemAuto(CrewMan man) {
		Tile tile = man.getCurrentTile();
		if (tile.isPowered()) {
			// find best angle, find the direction of that angle
			int a = getParentShip().getWeapons().getBestWeaponArc();
			int d = angleToDirection(a - 90);
			// if weapon direction and desired direction don't match
			if (direction != d) {
				updatePosDir(d, tile);
			}
			manuver(man, null);
		} else {
			tile.getParentShip().getEngineering().attemptPowerSystem(tile.getItem());
		}
	}
	/**
	 * Add options to choose which direction
	 */
	public void directionPopUp(Tile source) {
		GameScreen s = Home.getCurrentScreen();
		ArrayList<JButton> buttons = new ArrayList<JButton>();
		btnFore = UIMisc.createJButton("Fore", s);
		btnAft = UIMisc.createJButton("Aft", s);
		btnPort = UIMisc.createJButton("Port", s);
		btnStarboard = UIMisc.createJButton("Starboard", s);
		btnNone = UIMisc.createJButton("Don't Care", s);
		buttons.add(btnFore);
		buttons.add(btnAft);
		buttons.add(btnPort);
		buttons.add(btnStarboard);
		buttons.add(btnNone);
		// get position
		Point p = Home.getCurrentScreen().getMousePosition();
		// if null, we need to grab position from tile
		if (p == null) {
			p = source.getPositionOnScreen();
		}
		pop = PopUpBox.createPopUpBox(Home.getCurrentScreen(), p, buttons);
	}
	/**
	 * Convert the input direction to a string
	 */
	public String directionToString(int d) {
		String s = "";
		if (d == fore) {
			s = "Fore";
		} else if (d == 1) {
			s = "Aft";
		} else if (d == 2) {
			s = "Port";
		} else if (d == 3) {
			s = "Starboard";
		} else {
			s = "Don't Care";
		}
		return s;
	}
	/**
	 * Convert the input direction to a Point that represents the minimum and
	 * maximum arc for this direction p.x = min angle, p.y = max angle Returns
	 * null if no valid direction is given
	 */
	public int directionToAngle(int d) {
		int p = -1;
		if (d == fore) {
			p = 0;
		} else if (d == aft) {
			p = 180;
		} else if (d == port) {
			p = 270;
		} else if (d == starboard) {
			p = 90;
		}
		return p;
	}
	/**
	 * Convert the input direction to a Point that represents the minimum and
	 * maximum arc for this direction p.x = min angle, p.y = max angle Returns
	 * null if no valid direction is given
	 */
	public Point directionToAngleRange(int d) {
		Point p = null;
		if (d == starboard) {
			p = new Point(45, 135);
		} else if (d == port) {
			p = new Point(225, 315);
		} else if (d == fore) {
			p = new Point(315, 45);
		} else if (d == aft) {
			p = new Point(135, 225);
		}
		return p;
	}
	/**
	 * Convert the input direction int to a 'direction' static
	 */
	public int angleToDirection(int p) {
		int d = -1;
		// go around clockwise
		if (p >= 315 || p < 45) {
			d = fore;
		} else if (p >= 45 && p < 135) {
			d = starboard;
		} else if (p >= 135 && p < 225) {
			d = aft;
		} else if (p >= 225 && p < 315) {
			d = port;
		}
		// this should never happen
		else {
			d = none;
		}
		return d;
	}
	/**
	 * Convert the input direction int to a 'direction' static Narrow uses
	 * smaller, centered arcs, excludes edge areas (instead of 90 degrees, uses
	 * 50 degrees)
	 */
	public int angleToDirectionNarrow(int p) {
		int d = -1;
		// go around clockwise
		if (p >= 335 || p < 25) {
			d = fore;
		} else if (p >= 65 && p < 115) {
			d = starboard;
		} else if (p >= 155 && p < 205) {
			d = aft;
		} else if (p >= 245 && p < 295) {
			d = port;
		}
		// this should never happen
		else {
			d = none;
		}
		return d;
	}
	public void removePopUpBox() {
		pop.setVisible(false);
		pop = null;
	}
	public int getPosition() {
		return position;
	}
	public String getPositionString() {
		return directionToString(getPosition());
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public int getDirection() {
		return direction;
	}
	public String getDirectionString() {
		return directionToString(getDirection());
	}
	public void setDirection(int direction) {
		this.direction = direction;
	}
	public void updateTilePane(Tile source) {
		// if we're looking at stations panel and this tile is selected
		if (Home.getCurrentScreen() instanceof GameScreen) {
			TilePane pane = TilePane.containsTile(source);
			if (pane != null) {
				pane.updatePane();
				// pane.updatePanel("Aproach their: ",
				// "<html>Aproach their: <br>"
				// + getPositionString() + "</html>");
				// pane.updatePanel("Face them with: ",
				// "<html>Face them with: <br>"
				// + getDirectionString() + "</html>");
			}
		}
	}
	/**
	 * This is run from the Helm AI action, and runs more often based on the
	 * Pilot's navigation level
	 */
	public void manuver(CrewMan pilot, HelmAction ai) {
		// only run if we're properly manned
		if (pilot.getCurrentTile().getItem() instanceof HelmItem) {
			// and both position and direction aren't none
			if (getPosition() != none || getDirection() != none) {
				// get the amount we can turn
				int amount = getTotalEffectivenessLevel();
				// int amount = ((HelmItem)
				// pilot.getMannedTile().getItem()).getEffectivenessLevel();
				// if we're not in proper position, work on that first
				if (!checkPosition()) {
					updatePosition(amount);
				} else {
					// we're in the right position, so halt the ship
					getShipTravel().attemptSpeed(0);
					if (!checkDirection()) {
						updateDirection(amount);
					}
				}
			}
		}
		// pilot no longer manning a console
		else if (ai != null) {
			ai.endAction();
		}
	}
	/**
	 * Are we in the position we'd like to be in?
	 * 
	 * @return
	 */
	public boolean checkPosition() {
		Warp enemy = getTargetShip();
		if (enemy != null) {
			// find angle to this ship from enemy, include enemy's bearing
			int a = StaticFunctions.within360(enemy.findAngleto(getShipTravel())
					- enemy.getBearing());
			// TODO - also check distance to desired point/ship
			// use narrow to get more in the middle of our direction
			if (getPosition() == none || (angleToDirectionNarrow(a) == getPosition())) {
				return true;
			}
			// there is a target, and we're not in position
			else {
				return false;
			}
		}
		// no target ship, so we're good
		return true;
	}
	/**
	 * Are we facing where we'd like to face?
	 * 
	 * @return
	 */
	public boolean checkDirection() {
		Warp enemy = getTargetShip();
		if (enemy != null) {
			int a = getShipTravel().getBearing();
			// int a =
			// StaticFunctions.within360(getShipTravel().findAngleto(enemy) -
			// getShipTravel().getBearing());
			// if(angleToDirection(a) == getDirection() || getDirection() ==
			// none){
			// direction bearing
			int desiredDirection = StaticFunctions.within360(getShipTravel().findAngleto(enemy)
					- directionToAngle(getDirection()));
			// we have a desired direction and we're not meeting it
			if (getDirection() != none && a != desiredDirection) {
				return false;
			}
			// we're good
			else {
				return true;
			}
		}
		// no target ship, so we're good
		return true;
	}
	public void updatePosition(int amount) {
		// find a target position
		Point t = findTargetPosition();
		// get angle to point
		int a = getShipTravel().findAngleto(t);
		int bearing = getShipTravel().getBearing();
		// if the point is 'in front of us', attempt to move forward at top
		// speed
		if (a > bearing - 5 && a < bearing + 5) {
			getShipTravel().attemptMaxSpeed();
			// getShipTravel().attemptWarp(getShipTravel().getMaxWarp());
		} else {
			// otherwise, turn to face the right bearing
			int test = a - bearing;
			// increase bearing
			if (test > 0) {
				// don't turn past our destination
				int nbear = Math.min(a, bearing + amount);
				getShipTravel().setBearing(nbear);
			}
			// decrease bearing
			else {
				int nbear = Math.max(a, bearing + amount);
				getShipTravel().setBearing(nbear);
			}
		}
	}
	public void updateDirection(int amount) {
		// get angle to enemy ship (plus desired angle)
		int a = StaticFunctions.within360(getShipTravel().findAngleto(getTargetShip())
				- directionToAngle(getDirection()));
		int bearing = getShipTravel().getBearing();
		// turn to face the right bearing
		int test = a - bearing;
		// take the smaller amount of distance - so we don't over correct
		// because of abs, we know value is positive, or clockwise
		amount = Math.min(Math.abs(test), Math.abs(amount));
		// decrease bearing if bearing is greater, or near 0 and angle near 360
		if (bearing > a || (bearing < 90 && a > 270)) {
			amount = amount * -1;
		}
		if (bearing > 270 && a < 90) {
			amount = Math.abs(amount);
		}
		amount = bearing + amount;
		getShipTravel().setBearing(StaticFunctions.within360(amount));
	}
	/**
	 * Return's a position
	 */
	public Point findTargetPosition() {
		Warp enemy = getTargetShip();
		// find heading based on bearing of enemy ship and desired position
		int angle = StaticFunctions.within360(enemy.getBearing()
				+ directionToAngle(getPosition()));
		double heading = Math.toRadians(angle);
		double ax = Math.sin(heading);
		double ay = Math.cos(heading);
		// distance
		float dx = (float) (ax * 5);
		float dy = (float) (ay * 5);
		// update position
		return new Point((int) (enemy.getXpos() + dx), (int) (enemy.getYpos() - dy));
	}
	/**
	 * Return's this system's ship
	 */
	public Warp getShipTravel() {
		return getParentShip().getWarp();
	}
	/**
	 * Return's the target's weapon ship
	 */
	public Warp getTargetShip() {
		return targetShip;
	}
	public void setTargetShip(Warp ship) {
		this.targetShip = ship;
		// update weapons
		if (ship != null) {
			getParentShip().getWeapons().setWeaponTargets(ship.getParentShip());
		} else {
			getParentShip().getWeapons().setWeaponTargets(null);
		}
	}
	public boolean hasTargetShip() {
		return getTargetShip() != null;
	}
	/**
	 * Returns the angle the target ship is to this ship (does not account for
	 * this ship's bearing)
	 */
	public int getAngleToTarget() {
		return angleToTarget;
	}
	public void setAngleToTarget(int angleToTarget) {
		this.angleToTarget = angleToTarget;
	}
	/**
	 * Return's if autopilot is on (returns false if jumping)
	 */
	public boolean isAutoPilot() {
		boolean jumping = false;
		if (Home.getPlayer().getQuest() != null && Home.getPlayer().getQuest().isJumping()){
			jumping = true;
		}
		return autoPilot && !jumping;
	}
	public void setAutoPilot(boolean autoPilot) {
		this.autoPilot = autoPilot;
	}
	private Warp getWarp(){
		return getParentShip().getWarp();
	}
}
