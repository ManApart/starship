package org.iceburg.home.actors;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import org.iceburg.home.main.Constants;
import org.iceburg.home.main.StaticFunctions;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.ship.systems.Warp;

public class Laser extends Projectile {
	boolean doOnce;
	Tile entryTile, hitTile;
	int updateProj;

	public Laser(Tile source) {
		super(source);
		doOnce = false;
	}
	@Override
	public String toString() {
		return "Laser from " + getWeapon().getName();
	}
	@Override
	public synchronized void paint(Graphics2D g, int x, int y) {
		if (isPainted()) {
			g.setColor(Color.red);
			int s = Constants.shipSquare;
			g.setStroke(new BasicStroke(s / 2));
			g.drawLine(x + entryTile.getX() * s, y + entryTile.getY() * s, x
					+ hitTile.getX() * s, y + hitTile.getY() * s);
			g.drawOval(x + hitTile.getX() * s, y + hitTile.getY() * s, s / 4, s / 4);
		}
	}
	public boolean isPainted() {
		return (entryTile != null && hitTile != null && getWeapon().isWithinArc());
		// && !hitTile.isEdgeTile() && (getTargetTile() == null ||
		// (getTargetTile() != null && getTargetTile().getHealth() > 0)));
	}
	public void init() {
		// add it to the other ship
		Ship ship = getWeapon().getTargetShip();
		removeFromParent();
		addToParent(ship);
		// adjust angles based on ship bearings
		// initial angle
		// setAngle(angle + getWeapon().getTargetShip().getWarp().getBearing() -
		// 90);
		// // correct for the other ship
		// setAngle(angle - getParent().getWarp().getBearing() + 90);
		doOnce = true;
	}
	/**
	 * Runs often, moving the projectile like creman's take step function or
	 * moveship function
	 */
	@Override
	public void update() {
		// if we're no longer firing the laser, delete it
		if (!getWeapon().isFiring()) {
			removeFromParent();
			getWeapon().removeProjectile(this);
		}
		// if projectile isn't in arc, hide it
		if (!getWeapon().isWithinArc()) {
			hitTile = entryTile;
		}
		if (doOnce == false) {
			init();
		} else if (updateProj < 40) {
			updateProj += 1;
		} else {
			updateProj = 0;
			if (getParent() != null && getWeapon().isFiring()
					&& getWeapon().isWithinArc()) {
				// establish initial angle and location
				Warp newTravel = getParent().getWarp();
				Warp orginShip = getWeapon().getParentSystem().getParentShip().getWarp();
				setAngle(StaticFunctions.within360(orginShip.findAngleto(newTravel)
						- newTravel.getBearing()) + 90);
				int invAngle = StaticFunctions.within360(getAngle() + 180);
				entryTile = getParent().getFloorPlanAt(floor).findEdgeTileAtAngle(invAngle, getWeapon().getTargetTile());
				setXpos(entryTile.getX());
				setYpos(entryTile.getY());
				setSubX(entryTile.getX());
				setSubY(entryTile.getY());
				// set proper floor if applicable
				if (getTargetTile() != null) {
					setFloor(getTargetTile().getFloor());
				}
				double heading = Math.toRadians(getAngle());
				// TODO Set up rotate image
				// rotateImg = StaticFunctions.rotateImage(bi, bearing - 90);
				boolean hitTarget = false;
				double ax = Math.sin(heading);
				double ay = Math.cos(heading);
				// now repeatedly move the laser forward until it hits something
				// and does damage
				int i = 0;
				while (hitTarget == false) {
					i++;
					// sub position used as double, rounded to 'normal' position
					// and compared to pos.
					// basically sub positions are continually updated and
					// normal pos is used to store an old value
					// until the 'sub' has changed by a whole int.
					// update subposition
					this.setSubX(subX += ax);
					this.setSubY(subY -= ay);
					// if the sub #'s are a full int greater or less than the
					// old int position, update/ move tile
					if ((int) getSubX() > getXpos() || (int) getSubX() < getXpos()
							|| (int) getSubY() > getYpos() || (int) getSubY() < getYpos()) {
						// update pos to subpos (so that further checks tell us
						// if we've moved a tile
						setXpos((int) getSubX());
						setYpos((int) getSubY());
						// do a tile check to see if we should do damage
						if (parent.getFloorplans().size() > getFloor() && getFloor() >= 0) {
							hitTile = parent.getFloorPlanAt(getFloor()).getTileAt(getXpos(), getYpos());
							if (hitTile != null) {
								// if we hit a tile, do damage, otherwise just
								// carry on
								// if we hit a layer of shields and don't have
								// the same frequency
								if ((hitTile.hasShieldLayerOn()
										&& hitTile.getParentShip() != getWeapon().getParentTile().getParentShip() && !(getWeapon().getFrequency() == hitTile.getShields().get(0).getFrequency()))
										|| (getParent() == getWeapon().getTargetShip() && (hitTile.isSolid() || (hitTile == getTargetTile() && getTargetTile().getHealth() > 0)))) {
									hitTarget = true;
									doDamage(hitTile);
								}
							}
							// else we've exited the ship, we'll delete the
							// laser on update if no longer fireing.
							else {
								hitTarget = true;
								// hitTile =
								// getParent().getFloorPlanAt(floor).findEdgeTileAtAngle(angle,
								// entryTile);
								hitTile = parent.getFloorPlanAt(getFloor()).getTileAt((int) (getXpos() - ax), (int) (getYpos() - ay));
							}
						}
					}
				}
			}
		}
	}
}
