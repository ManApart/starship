package org.iceburg.home.ship.systems;

import java.awt.Color;

import org.iceburg.home.ship.Ship;

public class TurboTube extends ShipSystem {
	public static Color systemMain = Color.decode("#ea5e00");

	public TurboTube(Ship ship) {
		super(ship);
		this.tileTypes.add(systemMain);
		this.name = "TurboTubes";
	}
}
