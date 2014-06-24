package org.iceburg.home.actors;

import java.awt.Color;
import java.awt.Graphics2D;

import org.iceburg.home.main.Constants;
import org.iceburg.home.main.StaticFunctions;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.ship.systems.Warp;

public class Missile extends Projectile {
	public Missile(Tile source) {
		super(source);
	}
	@Override
	public String toString() {
		return "Missile from " + getWeapon().getName();
	}
	@Override
	public void paint(Graphics2D g, int x, int y) {
		g.setColor(Color.black);
		int xd = (int) (xPos * Constants.shipSquare + subX + x);
		int yd = (int) (yPos * Constants.shipSquare + subY + y);
		int wd = (int) Constants.shipSquare;
		int[] xPoints = { xd, xd, xd + wd };
		int[] yPoints = { yd, yd + wd, yd + wd / 2 };
		g.setColor(Color.red);
		g.fillPolygon(xPoints, yPoints, xPoints.length);
	}
	/**
	 * Runs often, moving the projectile like creman's take step function or
	 * moveship function
	 */
	@Override
	public void update() {
		//heading should remain the same on the enemy ship
		double heading = Math.toRadians(getAngle());
		//but be adjusted based on its own ship's heading
		if (getParent() ==getWeapon().getParentTile().getParentShip()){
			heading = Math.toRadians(getAngle() - getParent().getWarp().getBearing() + 90);
		}
		// Set up rotate image
		// rotateImg = StaticFunctions.rotateImage(bi, bearing - 90);
		double ax = Math.sin(heading);
		double ay = Math.cos(heading);
		// distance
		float dx = (float) (ax * 1);
		float dy = (float) (ay * 1);
		// update sub position
		this.setSubX(subX += dx);
		this.setSubY(subY -= dy);
		boolean moveTile = false;
		// need to run movetile?
		if (subX >= Constants.shipSquare / 2) {
			subX = (int) (-Constants.shipSquare / 2 + 1);
			setXpos(xPos + 1);
			moveTile = true;
		} else if (subX <= -Constants.shipSquare / 2) {
			subX = (int) (Constants.shipSquare / 2 - 1);
			setXpos(xPos - 1);
			moveTile = true;
		}
		if (subY >= Constants.shipSquare / 2) {
			subY = (int) (-Constants.shipSquare / 2 + 1);
			setYpos(yPos + 1);
			moveTile = true;
		} else if (subY <= -Constants.shipSquare / 2) {
			subY = (int) (Constants.shipSquare / 2 - 1);
			setYpos(yPos - 1);
			moveTile = true;
		}
		if (moveTile) {
			enterNewTile(getXpos(), getYpos());
		}
	}
	/**
	 * Runs whenever a new tile is entered. If a tile is found at this location,
	 * it does damage to the tile If the new tile is out of bounds, the
	 * projectile is moved to the target ship, or removed altogether
	 */
	public void enterNewTile(int x, int y) {
		// if the projectile is on a valid floor
		if (parent.getFloorplans().size() > getFloor() && getFloor() >= 0) {
			Tile tile = parent.getFloorPlanAt(getFloor()).getTileAt(x, y);
			if (tile != null) {
				// if we hit a tile, do damage, otherwise just carry on
				if ((tile.hasShieldLayerOn() && tile.getParentShip() != getWeapon().getParentTile().getParentShip())
						|| (tile.getHealth() > 0 && (tile.isSolid() || tile.equals(getTargetTile())))) {
					doDamage(tile);
				}
			}
			// we've exited the ship
			else {
				// if the projectile is leaving the target ship (it missed or
				// went all the way through
				if (parent == getWeapon().getTargetShip()) {
					removeFromParent();
				}
				// the projectile just left the ship it launched from
				else {
					moveToShip(getWeapon().getTargetShip());
				}
			}
		}
	}
	/**
	 * Transfers the projectile to the targetship
	 * 
	 * @param newShip
	 *            - the target ship
	 */
	public void moveToShip(Ship newShip) {
		// System.out.println("Added to target ship!");
		// set proper floor if applicable
		if (getTargetTile() != null) {
			setFloor(getTargetTile().getFloor());
		}
		// find angle and center tile
		// int invAngle = newTravel.findAngleto(parent.getWarp()) +
		// newTravel.getBearing()- 90;
		Warp orginShip = getParent().getWarp();
		Warp newTravel = newShip.getWarp();
		setAngle(StaticFunctions.within360(orginShip.findAngleto(newTravel)
				- newTravel.getBearing()) + 90);
		int invAngle = StaticFunctions.within360(getAngle() + 180);
		Tile newTile = newShip.getFloorPlanAt(floor).findEdgeTileAtAngle(invAngle, getWeapon().getTargetTile());
		// adjust angles based on ship bearings
		// setAngle(angle + newTravel.getBearing() - 90);
		// actually make the swap
		removeFromParent();
		addToParent(newShip);
		this.setXpos(newTile.getX());
		this.setYpos(newTile.getY());
	}
}
