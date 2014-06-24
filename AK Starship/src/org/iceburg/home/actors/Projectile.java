package org.iceburg.home.actors;

import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;

import org.iceburg.home.items.Weapon;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.ui.TilePane;
import org.iceburg.home.ui.UIMisc;

public abstract class Projectile {
	Weapon weapon;
	Ship parent;
	int xPos, yPos, floor, angle, currentHealth;
	double subX, subY;
	Image rotateImg; // will be used to add a rotated image for the projectile
	// where does the projectile start at on the other ship?
	Tile awayEdgeTile;

	/**
	 * Create's a projectile that leave's this ship
	 */
	public Projectile(Tile source) {
		this.weapon = source.getWeapon();
		this.parent = source.getParentShip();
		this.currentHealth = getWeapon().getMissileHealth();
		this.angle = parent.getWarp().findAngleto(this.weapon.getTargetShip().getWarp());
		this.xPos = source.getX();
		this.yPos = source.getY();
		this.floor = source.getFloor();
	}
	/**
	 * Gets overridden by missile or laser - how to paint
	 */
	public void paint(Graphics2D g, int x, int y) {
	}
	/**
	 * Runs often, moving the projectile like creman's take step function or
	 * moveship function Gets overriden
	 */
	public void update() {
	}
	/**
	 * Does damage to this tile, exploding if necessary
	 */
	public void doDamage(Tile tile) {
		// System.out.println("done damage");
		// if missile, explode at 0 health, set to 0 health if target tile
		if (tile == getTargetTile()) {
			setHealthCurrent(0);
		}
		// do damage to shields layer
		if (tile.hasShieldLayerOn()) {
			// //but only if it's not a laser with the same frequency
			// && !(getWeapon().isLaser() && getWeapon().getFrequency() ==
			// tile.getShields().get(0).getFrequency())){
			for (int i = 0; i < tile.getShields().size(); i++) {
				tile.getShields().get(i).doDamage(this);
			}
		}
		// if projectile strong enough to pierce the wall/item and keep going
		if (tile.getHealth() < getCurrentHealth()) {
			setHealthCurrent(currentHealth - tile.getHealth());
			tile.setHealth(0);
		}
		// do damage to this tile
		else {
			ArrayList<Tile> tiles = tile.findAOETiles3d(getWeapon().getAOE());
			// spillover = extra damage carried over to the next tile
			int spillover = getCurrentHealth();
			for (int i = 0; i < tiles.size(); i++) {
				Tile tempTile = tiles.get(i);
				if (spillover < 0) {
					spillover = 0;
				}
				//if crewman, do damage to them first, crewman ignore spillover
				if (tempTile.getActor() != null){
					int temp = tempTile.getActor().getHealthCurrent() - getDamage();
					if (temp < 0) {
						tempTile.getActor().setHealthCurrent(0);
					} else {
						tempTile.getActor().setHealthCurrent(temp);
					}
					
				}
				//now do damage to tile
				int temp = tempTile.getHealth() - getDamage() - spillover;
				if (temp < 0) {
					// add the extra damage to the spillover
					spillover = -temp;
					tempTile.setHealth(0);
				} else {
					tempTile.setHealth(temp);
				}
			}
		}
		// setCurrentTile(null);
		// update stations panel
		Tile curTile = TilePane.getFirstActiveTile();
		// if stations panel is looking at weapons
		if (curTile != null && curTile.getItem() instanceof Weapon) {
			TilePane pane = TilePane.containsTile(getWeapon().getParentTile());
			// and we found a pane for this projectile's weapon
			if (pane != null) {
				pane.updateProgressBar(UIMisc.hit, tile);
			}
		}
		// An idea to update all weapons hit tiles, but currently doesn't work
		// needs a way to know if each weapon has the same hit tile as the
		// projectile
		// Tile curTile = TilePane.getFirstActiveTile();
		// // if stations panel is looking at weapons
		// if (curTile != null && curTile.getItem() instanceof Weapon) {
		// ArrayList<TilePane> list = Home.getCurrentScreen().getActiveTiles();
		// //if this is a weapon, update any other weaopns that have the same
		// target
		// for (int i=0; i< list.size(); i++){
		// TilePane pane = list.get(i);
		// Weapon w = pane.getTile().getWeapon();
		// //
		// if (w == getWeapon()){
		// pane.updateProgressBar(UIMisc.hit,
		// pane.getTile().getWeapon().getTargetTile());
		// }
		// }
		// }
		// remove the projectile if health == 0
		if (getCurrentHealth() <= 0 && this instanceof Missile) {
			removeFromParent();
			weapon.removeProjectile(this);
		}
	}
	public void removeFromParent() {
		parent.getProjectiles().remove(this);
		if (parent != weapon.getParentTile().getParentShip()) {
			weapon.removeProjectile(this);
		}
		this.parent = null;
	}
	public void addToParent(Ship parent) {
		setParent(parent);
		parent.getProjectiles().add(this);
	}
	/**
	 * Returns the tile the weapon was targeting when it fired this projectile
	 * 
	 * @return
	 */
	public Tile getTargetTile() {
		return weapon.getTargetTile();
	}
	public Weapon getWeapon() {
		return weapon;
	}
	public int getDamage() {
		return getWeapon().getWeaponDamage();
	}
	public void setWeapon(Weapon weapon) {
		this.weapon = weapon;
	}
	public int getXpos() {
		return xPos;
	}
	public void setXpos(int xPos) {
		this.xPos = xPos;
	}
	public int getYpos() {
		return yPos;
	}
	public void setYpos(int yPos) {
		this.yPos = yPos;
	}
	public double getSubX() {
		return subX;
	}
	public void setSubX(double subX) {
		this.subX = subX;
	}
	public double getSubY() {
		return subY;
	}
	public void setSubY(double subY) {
		this.subY = subY;
	}
	public int getFloor() {
		return floor;
	}
	public void setFloor(int floor) {
		this.floor = floor;
	}
	public int getAngle() {
		return angle;
	}
	public void setAngle(int angle) {
		this.angle = angle;
	}
	public synchronized Ship getParent() {
		return parent;
	}
	public synchronized void setParent(Ship parentShip) {
		this.parent = parentShip;
	}
	public int getCurrentHealth() {
		return currentHealth;
	}
	public void setHealthCurrent(int currentHealth) {
		this.currentHealth = currentHealth;
	}
	public int getTotalHealth() {
		return getWeapon().getMissileHealth();
	}
	public Image getRotateImg() {
		return rotateImg;
	}
	public void setRotateImg(Image rotateImg) {
		this.rotateImg = rotateImg;
	}
}
