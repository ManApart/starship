package org.iceburg.home.items;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.iceburg.home.actors.Laser;
import org.iceburg.home.actors.Projectile;
import org.iceburg.home.main.Constants;
import org.iceburg.home.main.Home;
import org.iceburg.home.main.StaticFunctions;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.ship.ShipFloorPlan;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.ship.systems.Warp;
import org.iceburg.home.ship.systems.Weapons;
import org.iceburg.home.ui.GameScreen;
import org.iceburg.home.ui.MessageCenter;
import org.iceburg.home.ui.TilePane;
import org.iceburg.home.ui.UIMisc;

//When an item (weapon) is equipped to a system, it is converted into a weapon
public class Weapon extends Item {
	// charge time is the number of ticks to charge the weapon, rechargetime =
	// if not null, needs an ammo to fire. - should look at item
	// firing arc -TODO change to arraylist x is min, y is max...
	// int angleMax, angleMin;
	private ArrayList<Point> arcs;
	private ArrayList<Projectile> projectiles;
	private ArrayList<Tile> yellows, greens, cyans;
	private boolean withinArc, isFiring, reloading;
	// Weapons parentSystem;
	private Tile targetTile, edgeTile;
	// common
	private int startArc, maxArcRange, damage, missileHealth;
	// laser
	private int frequency;
	// missile
	private int AOE, reloadTime, reloadProgress;
	public static int reloadFactor = 100;
	public Ship targetShip;
	private Image imageRotate;

	@Override
	public String toString() {
		return getName();
	}
	public Weapon() {
		setProjectiles(new ArrayList<Projectile>());
		frequency = StaticFunctions.randRange(0, 5);
	}
	public void paintLaser(Graphics2D g, int x, int y) {
		if (getProjectiles().size() > 0 && ((Laser) getProjectiles().get(0)).isPainted()) {
			// if (isFiring() && isWithinArc() && edgeTile != null &&
			// (getTargetTile() == null || (getTargetTile() != null &&
			// getTargetTile().getHealth() > 0))) {
			g.setColor(Color.red);
			int s = Constants.shipSquare;
			g.setStroke(new BasicStroke(s / 2));
			g.drawLine(x + getParentTile().getX() * s + s / 2, y + getParentTile().getY()
					* s + s / 2, x + edgeTile.getX() * s + s, y + edgeTile.getY() * s + s);
		}
	}
	@Override
	public void setCurrentPower(int currentPower) {
		super.setCurrentPower(currentPower);
		// if we've lost power, stop firing
		if (getEffectivenessLevel() < 1) {
			ceaseFire();
		}
	}
	/**
	 * Clear's projectiles and stops the weapon from firing more until isFiring
	 * is turned back on
	 */
	public void ceaseFire() {
		setFiring(false);
		setReloading(false);
	}
	/**
	 * Update's the weapon, firing a new projectile (Currently only for missile
	 * weapons) TODO - in future transfer all projectiles to be updated this way
	 * (See updateProjectiles in Ship)
	 */
	public void update() {
		if (isMissile()) {
			// update reload progress, but only if powered (which also means we
			// have some health)
			if (getReloadProgress() < getTotalReloadTime() && getPowerCurrent() > 0) {
				setReloadProgress(getReloadProgress() + getHealthPercent10()
						+ getPowerCurrent());
				if (Home.creativeMode){
					setReloadProgress(getTotalReloadTime());
				}
			}
			if (isFiring() || isReloading()) {
				if (getEffectivenessLevel() > 0) {
					getParentSystem().fireWeapon(getParentTile());
				}
			}
		} else if (isLaser()) {
			if (isFiring() && getProjectiles().size() < 1) {
				fireWeapon();
			}
		}
	}
	public void updateProjectiles() {
		for (int i = getProjectiles().size() - 1; i >= 0; i--) {
			getProjectiles().get(i).update();
		}
	}
	public void fireWeapon() {
		getParentSystem().fireWeapon(getParentTile());
	}
	/**
	 * Adds the yellow,green,blue weapon cones
	 */
	public void addArcOverlay() {
		if (yellows != null) {
			for (int i = 0; i < yellows.size(); i++) {
				if (yellows.get(i) != null) {
					yellows.get(i).setAltColor(Color.yellow);
				}
			}
		}
		if (greens != null) {
			for (int i = 0; i < greens.size(); i++) {
				if (greens.get(i) != null) {
					greens.get(i).setAltColor(Color.green);
				}
			}
		}
		if (cyans != null) {
			for (int i = 0; i < cyans.size(); i++) {
				if (cyans.get(i) != null) {
					cyans.get(i).setAltColor(Color.cyan);
				}
			}
		}
		if (Home.getCurrentScreen() != null) {
			Home.getCurrentScreen().updateCurrentFloorPlanImage();
		}
	}
	/**
	 * Remove the yellow,green,blue weapon cones
	 */
	public void removeArcOverlay() {
		if (yellows != null) {
			for (int i = 0; i < yellows.size(); i++) {
				if (yellows.get(i) != null) {
					yellows.get(i).removeAltColor(Color.yellow);
				}
			}
		}
		if (greens != null) {
			for (int i = 0; i < greens.size(); i++) {
				if (greens.get(i) != null) {
					greens.get(i).removeAltColor(Color.green);
				}
			}
		}
		if (cyans != null) {
			for (int i = 0; i < cyans.size(); i++) {
				if (cyans.get(i) != null) {
					cyans.get(i).removeAltColor(Color.cyan);
				}
			}
		}
		if (Home.getCurrentScreen() != null) {
			Home.getCurrentScreen().updateCurrentFloorPlanImage();
		}
	}
	/**
	 * Finds the weapon's clear shooting arcs
	 */
	public void findWeaponArc() {
		Tile tile = getParentTile();
		// clear the old visual
		removeArcOverlay();
		// reset the color lists
		yellows = new ArrayList<Tile>();
		greens = new ArrayList<Tile>();
		cyans = new ArrayList<Tile>();
		int i = 0, tempMin, x, y, l;
		boolean blocked = false;
		ShipFloorPlan floor = tile.getParentFloorPlan();
		// clear any old values
		setArcs(new ArrayList<Point>());
		tempMin = startArc;
		// for each degree 1-360
		int max = startArc + maxArcRange;
		i = startArc;
		while (i <= max) {
			// for(i = 0; i < 360; i ++){
			l = 1;
			x = 0;
			y = 0;
			Tile tempTile = null; // for making sure the getTile at isn't null
			// while the line is inside the floorplan
			while (tile.getX() + x <= floor.getMaxX() && tile.getX() + x >= 0
					&& tile.getY() + y <= floor.getMaxY() && tile.getY() + y >= 0) {
				// if tile is blocked
				tempTile = floor.getTileAt(tile.getX() + x, tile.getY() + y);
				if (!tempTile.isEmpty() && tempTile != tile) {
					// System.out.println("add cyan");
					cyans.add(tempTile);
					tempTile.setAltColor(Color.cyan);
					blocked = true;
					break;
				} else {
					// TODO - comment this out when done testing!!!!
					if (!tile.equals(floor.getTileAt(tile.getX() + x, tile.getY() + y))) {
						tempTile = floor.getTileAt(tile.getX() + x, tile.getY() + y);
						if (tempTile != null) {
							yellows.add(tempTile);
							tempTile.setAltColor(Color.yellow);
						}
					}
					// if this is an edge tile, exit loop
					if (floor.isEdgeTile(tile.getX() + x, tile.getY() + y)) {
						// COloring, can comment line out
						tempTile = floor.getTileAt(tile.getX() + x, tile.getY() + y);
						greens.add(tempTile);
						tempTile.setAltColor(Color.green);
						break;
					}
				}
				// draw the next 'pixel' of the line
				x = (int) (l * Math.cos(Math.toRadians(i - 90)));
				y = (int) (l * Math.sin(Math.toRadians(i - 90)));
				l += 1;
			}
			// We record the arc if blocked, or if we reached the end
			if (blocked || i == max) {
				// don't record angle if only done 1 step and it's blocked
				if (!(i == tempMin + 1)) {
					addArc(tempMin, i);
				}
				// reset to unblocked, and continue search
				tempMin = i;
				blocked = false;
			}
			i++;
		}
		Home.getCurrentScreen().updateCurrentFloorPlanImage();
	}
	/**
	 * Evaluates whether the target ship is within any of this weapon's arcs
	 */
	public void evaluateWeaponArc() {
		if (getEffectivenessLevel() > 0 && targetShip != null
				&& targetShip.getWarp() != null) {
			boolean withinTemp = false;
			// Ship s = getParentSystem().getParentShip();
			int angleToTarget = getParentSystem().getParentShip().getHelm().getAngleToTarget();
			// int angleToTarget =
			// getParentSystem().getParentShip().getTravel().getAngleToTarget();
			if (getArcs() == null) {
				findWeaponArc();
			}
			if (getArcs().size() > 0 && getTargetShip() != null) {
				Warp thisShip = getParentSystem().getParentShip().getWarp();
				for (int i = 0; i < getArcs().size(); i++) {
					int newMin = StaticFunctions.within360(thisShip.getBearing() - 90
							+ getArcMin(i));
					int newMax = StaticFunctions.within360(thisShip.getBearing() - 90
							+ getArcMax(i));
					if (angleToTarget > newMin && angleToTarget < newMax) {
						withinTemp = true;
					}
				}
			}
			// we're within the arc
			if (withinTemp) {
				// we were outside the arc, so change to be outside
				if (!isWithinArc()) {
					Home.messagePanel.addMessage("Target is now within " + getName()
							+ "'s arc.");
					setWithinArc(true);
				}
			}
			// we're outside the arc
			else {
				// we were in the arc, so change to be outside
				if (isWithinArc()) {
					Home.messagePanel.addMessage("Target is now outside " + getName()
							+ "'s arc.");
					setWithinArc(false);
				}
			}
			updateEdgeTile();
		}
	}
	// /**
	// * Return's the first projectile owned by this weapon that is in the
	// target
	// * ship area
	// */
	// public Projectile getProjectile() {
	// ArrayList<Projectile> list = getTargetShip().getProjectiles();
	// for (int i = 0; i < list.size(); i++) {
	// if (list.get(i).getWeapon() == this) {
	// return list.get(i);
	// }
	// }
	// return null;
	// }
	public ArrayList<Point> getArcs() {
		return arcs;
	}
	/**
	 * This weapons set of arcs
	 */
	public void setArcs(ArrayList<Point> arcs) {
		this.arcs = arcs;
	}
	/**
	 * The angle this arc starts at
	 */
	public int getArcMin(int arc) {
		return getArcs().get(arc).x;
	}
	/**
	 * The angle this arc ends at
	 */
	public int getArcMax(int arc) {
		return getArcs().get(arc).y;
	}
	/**
	 * The number of degrees this arc covers (length of arc
	 */
	public int getArcRange(int arc) {
		return getArcMax(arc) - getArcMin(arc);
	}
	public void addArc(int min, int max) {
		getArcs().add(new Point(min, max));
	}
	public boolean isWithinArc() {
		return withinArc;
	}
	public void setWithinArc(boolean withinArc) {
		this.withinArc = withinArc;
	}
	public Weapons getParentSystem() {
		return (Weapons) getParentTile().getParentSystem();
	}
	public Tile getTargetTile() {
		return targetTile;
	}
	public void setTargetTile(Tile targetTile) {
		this.targetTile = targetTile;
		// update this weapon's target ship to match the targeted tile
		if (targetTile != null) {
			setTargetShip(targetTile.getParentShip());
		}
		// if we're looking at stations panel and this tile is selected
		TilePane pane = TilePane.containsTile(getParentTile());
		if (pane != null) {
			String s = "Empty";
			if (getTargetTile() != null) {
				s = getTargetTile().getName();
			}
			pane.updatePanel("Target Tile: ", "Target Tile: " + s);
		}
	}
	public int getStartArc() {
		return startArc;
	}
	/**
	 * Return's the middle of the arc (start arc + half of arc width)
	 */
	public int getArc() {
		return startArc + getMaxArc() / 2;
	}
	/**
	 * Set the start arc given a mid arc (given arc - half of arc width)
	 */
	public void setStartArcByMid(int arc) {
		setStartArc(arc - getMaxArc() / 2);
	}
	public void setStartArc(int startArc) {
		this.startArc = startArc;
		if (getParentTile() != null && getParentTile().getParentFloorPlan() != null) {
			findWeaponArc();
		}
		updateRotateImage();
	}
	public void updateRotateImage() {
		if (getImage() != null) {
			int size = getImage().getWidth(null);
			BufferedImage newImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
			Graphics g = newImage.createGraphics();
			g.drawImage(getImage(), 0, 0, null);
			setImageRotate(StaticFunctions.rotateImage(newImage, startArc));
		}
	}
	/**
	 * Return's the max width of the arc
	 * 
	 * @return
	 */
	public int getMaxArc() {
		return maxArcRange;
	}
	public void setMaxArc(int maxArc) {
		this.maxArcRange = maxArc;
	}
	public int getDamage() {
		return damage;
	}
	/**
	 * Return calculated weapon damaged (based on whether missile or beam)
	 * Includes factors like current power and health percent
	 */
	public int getWeaponDamage() {
		if (isMissile()) {
			return (getDamage());
			// return (getPowerCurrent()*getDamage());
		}
		// else beam
		else {
			return (int) (getPowerCurrent() * getDamage() * getHealthPercent());
		}
	}
	public void setDamage(int damage) {
		this.damage = damage;
	}
	public int getAOE() {
		return AOE;
	}
	public void setAOE(int aOE) {
		AOE = aOE;
	}
	public int getFrequency() {
		return frequency;
	}
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
	public boolean hasParentSystem() {
		return getParentSystem() != null;
	}
	public int getMissileHealth() {
		return missileHealth;
	}
	public void setMissileHealth(int missileHealth) {
		this.missileHealth = missileHealth;
	}
	/**
	 * Return's true if this item is a laser and is firing or is a missile and
	 * is set to autofire
	 */
	public boolean isFiring() {
		return isFiring;
	}
	public void setFiring(boolean firingLaser) {
		this.isFiring = firingLaser;
		if (Home.getCurrentScreen() instanceof GameScreen) {
			TilePane pane = TilePane.containsTile(getParentTile());
			if (pane != null) {
				pane.updatePane();
			}
		}
	}
	/**
	 * Prints a string appropriate to whether this weapon is firing/is
	 * missile/laser
	 */
	public String printFiring() {
		if (isLaser()) {
			if (isFiring()) {
				return MessageCenter.colorString("Firing Beam", Color.red);
			} else {
				return "Beam Off";
			}
		}
		// Missile
		else {
			if (isFiring()) {
				return "Auto Fire " + MessageCenter.colorString("On", Color.red);
			} else {
				return "Auto Fire Off";
			}
		}
	}
	public boolean isLaser() {
		return getItemType().equals(Weapons.systemMain);
	}
	public boolean isMissile() {
		return getItemType().equals(Weapons.colorWeaponsMissile);
	}
	public Tile getEdgeTile() {
		return edgeTile;
	}
	public void setEdgeTile(Tile edgeTile) {
		this.edgeTile = edgeTile;
	}
	public void updateEdgeTile() {
		Tile source = getParentTile();
		Warp travel = source.getParentShip().getWarp();
		int angle = travel.findAngleto(getTargetShip().getWarp());
		angle = StaticFunctions.within360(angle - travel.getBearing() + 90);
		Tile tile = source.getParentFloorPlan().findEdgeTileAtAngle(angle, source);
		setEdgeTile(tile);
	}
	public Ship getTargetShip() {
		return targetShip;
	}
	public void setTargetShip(Ship targetShip) {
		this.targetShip = targetShip;
	}
	/**
	 * Does this weapon have a target ship? If we have a target tile, but no
	 * target ship, updates target ship
	 */
	public boolean hasTargetShip() {
		if (getTargetShip() == null) {
			if (getTargetTile() != null) {
				setTargetShip(getTargetTile().getParentShip());
				return true;
			} else {
				return false;
			}
		}
		return true;
	}
	public Image getImageRotate() {
		return imageRotate;
	}
	public void setImageRotate(Image imageRotate) {
		this.imageRotate = imageRotate;
	}
	/**
	 * Get this weapon's delay factor for reloading
	 */
	public int getReloadTime() {
		return reloadTime;
	}
	/**
	 * Get the total time it takes this weapon to reload
	 */
	public int getTotalReloadTime() {
		return getReloadTime() * reloadFactor;
	}
	public void setReloadTime(int reloadTime) {
		this.reloadTime = reloadTime;
	}
	public ArrayList<Projectile> getProjectiles() {
		return projectiles;
	}
	private void setProjectiles(ArrayList<Projectile> projectiles) {
		this.projectiles = projectiles;
	}
	public void addProjectile(Projectile p) {
		getProjectiles().add(p);
	}
	public void removeProjectile(Projectile p) {
		getProjectiles().remove(p);
	}
	public void removeAllProjectiles() {
		for (int i = getProjectiles().size() - 1; i >= 0; i--) {
			getProjectiles().remove(i);
		}
	}
	public int getReloadProgress() {
		return reloadProgress;
	}
	public void setReloadProgress(int reloadProgress) {
		this.reloadProgress = reloadProgress;
		// if we're looking at stations panel and this tile is selected
		if (Home.getCurrentScreen() instanceof GameScreen) {
			TilePane pane = TilePane.containsTile(getParentTile());
			if (pane != null) {
				pane.updateProgressBar(UIMisc.loadTime, getParentTile());
			}
		}
	}
	public boolean isReloading() {
		return reloading;
	}
	public void setReloading(boolean reloading) {
		this.reloading = reloading;
	}
}
