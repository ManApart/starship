package org.iceburg.home.items;

import java.util.ArrayList;

import org.iceburg.home.actors.Projectile;
import org.iceburg.home.main.StaticFunctions;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.ship.systems.Shields;
import org.iceburg.home.ui.TilePane;
import org.iceburg.home.ui.UIMisc;

public class Shield extends Item {
	int AOE, frequency, bufferHealth;
	ArrayList<Tile> shieldedTiles;
	int timer;

	public Shield() {
		shieldedTiles = new ArrayList<Tile>();
		bufferHealth = 0;
		frequency = StaticFunctions.randRange(0, 5);
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
		// Home.messagePanel.addMessage("Frequency set to: " + frequency);
	}
	public ArrayList<Tile> getShieldedTiles() {
		return shieldedTiles;
	}
	public void setShieldedTiles(ArrayList<Tile> shieldedTiles) {
		this.shieldedTiles = shieldedTiles;
	}
	/**
	 * The max health of the shield buffer
	 * 
	 * @return
	 */
	public int getBufferMax() {
		return getHealthTotal() / 10;
	}
	public int getBufferHealth() {
		return bufferHealth;
	}
	public void setBufferHealth(int bufferHealth) {
		// if we're going from shields to no shields, update the shield layers
		// if ((bufferHealth <= 0 && getBufferHealth() > 0)){
		// this.bufferHealth = bufferHealth;
		// //getParentSystem().powerSystemOff(getParentTile());
		// //or from no shields to shields
		// }else if(bufferHealth > 0 && getBufferHealth() <= 0) {
		// this.bufferHealth = bufferHealth;
		// //getParentSystem().powerSystemOn(getParentTile());
		// //otherwise just update buffer
		// } else{
		this.bufferHealth = bufferHealth;
		// }
		updateTilePane();
	}
	// TODO - this needs tested
	/**
	 * The Shield absorbs the damage, inflicting any extra damage onto the
	 * generator
	 */
	public void doDamage(Projectile p) {
		// total damage weapon inflicts
		int damageTotal = p.getCurrentHealth() + p.getDamage();
		// if the buffer can take the hit, just chagne the buffer
		if (getBufferHealth() > damageTotal) {
			setBufferHealth(getBufferHealth() - damageTotal);
		}
		// otherwise subtract the damage the buffer can take from total damage
		// set buffer to 0
		// damage the shield health with the rest of the damage
		else {
			damageTotal -= getBufferHealth();
			setBufferHealth(0);
			setHealth(getHealth() - damageTotal);
		}
		updateTilePane();
	}
	/**
	 * 
	 */
	public void update() {
		// if powered off, lower buffer
		if (getEffectivenessLevel() == 0) {
			setBufferHealth(0);
		} else if (timer < 10) {
			timer += getEffectivenessLevel();
		} else {
			timer = 0;
			rechargeBuffer();
		}
	}
	/**
	 * buffer recharge rate is based on current powerlevel*damage level
	 */
	public void rechargeBuffer() {
		if (getBufferHealth() < getBufferMax()) {
			int amount = 1;
			amount = Math.min(getBufferMax(), getBufferHealth() + amount);
			setBufferHealth(amount);
		}
	}
	public void updateTilePane() {
		// if stations panel is looking at this shield
		TilePane pane = TilePane.containsTile(getParentTile());
		if (pane != null) {
			pane.updateProgressBar(UIMisc.energy, getParentTile());
		}
	}
	public Shields getParentSystem() {
		return (Shields) getParentTile().getParentSystem();
	}
	/**
	 * get
	 */
	@Override
	public int getEffectivenessLevel() {
		int d = getDamagePercent10();
		int i = (getPowerCurrent() - getDamagePercent10());
		return Math.max(0, i);
	}
}
