package org.iceburg.home.ship.systems;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JSlider;

import org.iceburg.home.items.HelmItem;
import org.iceburg.home.items.Item;
import org.iceburg.home.items.WarpGenerator;
import org.iceburg.home.main.Constants;
import org.iceburg.home.main.Home;
import org.iceburg.home.main.StaticFunctions;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.ui.MessageCenter;
import org.iceburg.home.ui.TilePane;
import org.iceburg.home.ui.UIMisc;

public class Warp extends ShipSystem {
	public static Color systemMain = Color.decode("#29d025");
	// TODO - this class needs major overhaul to work with warp engines
	float xPos, yPos;
	// Max turn is 0-10;
	int distance, bearing, width, height, angle;
	double speed;
	int[] starDate;
	private Image rotateImg;
	private BufferedImage bi;
	private boolean autoPilot;
	JSlider jsSpeed;

	public Warp(Ship parentShip) {
		super(parentShip);
		this.tileTypes.add(systemMain);
		this.name = "Warp";
		// this.setStarDate(Constants.origionalDate);
		int[] orig = Constants.origionalDate;
		this.setStarDate(new int[] { orig[0], orig[1], orig[2], orig[3], orig[4], orig[5], orig[6] });
		// So location[2] would give 7,
		// 1.74.75.55");
		// this.setMaxWarp(2);
		// this.setMaxSpeed(Constants.warpFactors[9]);
		// this.setMaxTurn(10);
		// StaticFunctions.findBlockAt(getLocation()).getShips().add(parentShip);
	}
	/**
	 * Init's the warp so that our ship has an x, y, and image (taken from the
	 * parentShip ship) Run once the ship's image has been parsed
	 */
	public void init() {
		bi = new BufferedImage(parentShip.getImg().getWidth(null), parentShip.getImg().getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
		rotateImg = bi;
		// Draw the image on to the buffered image
		Graphics2D bGr = bi.createGraphics();
		bGr.drawImage(parentShip.getImg(), 0, 0, null);
		bGr.dispose();
		width = parentShip.getImg().getWidth(null);
		height = parentShip.getImg().getHeight(null);
		this.setXpos(StaticFunctions.randRange(0, 500));
		this.setYpos(StaticFunctions.randRange(0, 500));
		this.setBearing(90);
	}
	public void paintShip(Graphics2D g, int locX, int locY) {
		g.drawImage(this.rotateImg, locX, locY, null);
	}
	@Override
	public void paintTileInfo(TilePane p, Item item) {
		if (isManned() && isSystemPowered()) {
			jsSpeed = UIMisc.addSlider("Speed", p, (int) getMaxSpeed(), (int) getSpeed());
		}
		if (isSystemPowered()) {
			UIMisc.addJLabel("Total Power: " + getTotalEffectivenessLevel(), p);
		} else {
			UIMisc.addJLabel("Powered " + MessageCenter.colorString("off", Color.red), p);
		}
	}
	@Override
	public void stateChanged(JSlider o, Tile tile) {
		if (o == jsSpeed) {
			// System.out.println("slide value: " + slide.getValue());
			setSpeed(o.getValue());
			// o.setValue((int) getSpeed());
		}
	}
	// TODO - calculate max warp of ship
	// update the ship.travel's location (Block)
	public void moveShip() {
		// Find percent moved in either direction
		double heading = Math.toRadians(bearing);
		// Set up rotate image
		rotateImg = StaticFunctions.rotateImage(bi, bearing - 90);
		double ax = Math.sin(heading);
		double ay = Math.cos(heading);
		// System.out.println("X:"+ ax + "  Y: "+ ay);
		// distance
		// TODO - replaced time factor with 1
		float dx = (float) (ax * getSpeed());
		float dy = (float) (ay * getSpeed());
		// System.out.println("dX:"+ dx + " dY:"+ dy+ " Distance:" + distance);
		// update position
		this.setXpos(xPos += dx);
		this.setYpos(yPos -= dy);
		if (dx != 0 || dy != 0) {
			// System.out.println("X:"+ xPos + " Y:"+ yPos+ " Distance:" +
			// distance + " Time Factor:" + timeFactor);
			checkLocation();
			// checkCollision();
		}
		if (parentShip.hasSystem(Weapons.systemMain)
				&& parentShip.getHelm().getTargetShip() != null) {
			getParentShip().getHelm().setAngleToTarget(findAngleto(parentShip.getHelm().getTargetShip()));
		}
		if (Home.getCurrentScreen().getCurrentTile() != null && Home.getCurrentScreen().getCurrentTile().getItem() instanceof HelmItem){
			Home.getCurrentScreen().getCurrentTilePane().updatePanel("Bearing: ", "Bearing: " + getBearing()+ "\u00B0");
			if (getHelm().getTargetShip() != null){
				Home.getCurrentScreen().getCurrentTilePane().updatePanel("Target at: ", "Target at: " + getHelm().getTargetShip().getBearing()+ "\u00B0");
			}
		}
	}
	/**
	 * Used when physically colliding with objects such as stars. Needs tuning
	 * cause it's cheap now.
	 * 
	 * @return
	 */
	public Rectangle getBounds() {
		return new Rectangle((int) getXpos(), (int) getYpos(), 1, 1);
	}
	/**
	 * Used to register collision with mouse clicks in a non-scrolling
	 * enviornment
	 * 
	 * @param mapScale
	 * @return
	 */
	public Rectangle getBoundsVisual(int mapScale) {
		return new Rectangle((int) getXpos() * mapScale - getImage().getWidth(null) / 2, (int) getYpos()
				* mapScale - getImage().getWidth(null) / 2, rotateImg.getHeight(null), rotateImg.getWidth(null));
	}
	/**
	 * Used to register collision with mouse clicks in a scrolling enviornment
	 * 
	 * @param mapScale
	 *            - replaced with 1
	 * @return
	 */
	public Rectangle getBoundsVisualScroll(double locX, double locY) {
		Warp homeShip = Home.getShip().getWarp();
		int shipX = (int) (((getXpos() - homeShip.getXpos()) * 1) + locX);
		int shipY = (int) (((getYpos() - homeShip.getYpos()) * 1) + locY);
		return new Rectangle((int) shipX - getImage().getWidth(null) / 2, (int) shipY
				- getImage().getWidth(null) / 2, rotateImg.getHeight(null), rotateImg.getWidth(null));
	}
	// Use this so ships don't get too far from each other
	// check to see if we're still in the same block etc - if not, wrap to other
	// side
	public void checkLocation() {
		int width = Constants.arenaSize / 2;
		int height = Constants.arenaSize / 2;
		// int width = Home.getBattleLoc().getBackground().getWidth(null)/2;
		// int height = Home.getBattleLoc().getBackground().getWidth(null)/2;
		if (getXpos() > width) {
			// System.out.println("Right side");
			setXpos(-width);
		} else if (getXpos() < -width) {
			// System.out.println("Left side");
			setXpos(width - 1);
		}
		if (getYpos() > height) {
			// System.out.println("Bottom side");
			setYpos(-height);
		} else if (getYpos() < -height) {
			// System.out.println("Top side");
			setYpos(height - 1);
		}
		// Update ship arcs
		if (Home.getBattleLoc() != null) {
			Home.getBattleLoc().updateShipArcs();
		}
	}
	// public void attemptWarp(int warpFactor) {
	// int wf;
	// if (warpFactor > 0) {
	// if (warpFactor <= getMaxWarp()) {
	// wf = warpFactor;
	// } else {
	// wf = getMaxWarp();
	// }
	// setWarp(wf);
	// setSpeed(Constants.warpFactors[wf - 2]);
	// } else {
	// attemptSpeed(0);
	// }
	// }
	public void attemptMaxSpeed() {
		attemptSpeed(getMaxSpeed());
	}
	public void attemptSpeed(double speed) {
		if (speed <= getMaxSpeed()) {
			if (speed < 0) {
				speed = 0;
			}
		} else {
			speed = getMaxSpeed();
		}
		setSpeed(speed);
		if (Home.getCurrentScreen().getCurrentTile() != null
				&& Home.getCurrentScreen().getCurrentTile().getItem() instanceof WarpGenerator) {
			if (Home.getShip().getWarp().jsSpeed != null) {
				Home.getShip().getWarp().jsSpeed.setValue((int) speed);
			}
		}
		// setWarp(findClosestWarp(spd));
	}
	// public static int findClosestWarp(double speed) {
	// int i = Constants.warpFactors.length - 1;
	// int wf = 0;
	// while (i >= 0) {
	// if (Constants.warpFactors[i] < speed) {
	// // wf = Constants.warpFactors[i];
	// return i + 1;
	// }
	// i--;
	// }
	// return wf;
	// }
	public int getDaysPast() {
		int years = starDate[0] - Constants.origionalDate[0];
		int months = starDate[1] - Constants.origionalDate[1];
		int days = starDate[2] - Constants.origionalDate[2];
		return (days + months * 30 + years * 12 * 30);
	}
	public int getMinPast() {
		int years = starDate[0] - Constants.origionalDate[0];
		int months = starDate[1] - Constants.origionalDate[1];
		int days = starDate[2] - Constants.origionalDate[2];
		int hours = starDate[3] - Constants.origionalDate[3];
		int min = starDate[4] - Constants.origionalDate[4];
		return (min + hours * 60 + days * 60 * 24 + months * 60 * 24 * 30 + years * 60
				* 24 * 30 * 12);
	}
	public void updateTime(int miliseconds) {
		// TODO - replaced time factor with 1
		int update = (int) (miliseconds * 1);
		// Stardate[0] = year, [1] = month, [2] = day, 3 = hour, 4 = min, 5 =
		// sec, 6 =milisec
		if (update >= 1000) {
			getStarDate()[5] = getStarDate()[5] + (update / 1000);
		}
		getStarDate()[6] = getStarDate()[6] + (update % 1000);
		// mili
		while (getStarDate()[6] > 1000) {
			getStarDate()[6] = getStarDate()[6] - 1000;
			getStarDate()[5] = getStarDate()[5] + 1;
		}
		// second
		while (getStarDate()[5] > 60) {
			getStarDate()[5] = getStarDate()[5] - 60;
			getStarDate()[4] = getStarDate()[4] + 1;
			// minute
			if (getStarDate()[4] > 60) {
				getStarDate()[4] = getStarDate()[4] - 60;
				getStarDate()[3] = getStarDate()[3] + 1;
				// hour
				if (getStarDate()[3] > 24) {
					getStarDate()[3] = getStarDate()[3] - 24;
					getStarDate()[2] = getStarDate()[2] + 1;
					// day
					if (getStarDate()[2] > 30) {
						getStarDate()[2] = getStarDate()[2] - 30;
						getStarDate()[1] = getStarDate()[1] + 1;
						// month
						if (getStarDate()[1] > 12) {
							getStarDate()[1] = getStarDate()[1] - 12;
							getStarDate()[0] = getStarDate()[0] + 1;
						}
					}
				}
			}
		}
	}
	/**
	 * Finds the angle to the target ship
	 * 
	 * @param ship
	 *            - target ship
	 * @return
	 */
	// TODO - is this working properly? Need to test
	public int findAngleto(Warp ship) {
		int i = 0;
		double dx = ship.getXpos() - getXpos();
		double dy = (ship.getYpos() - getYpos());
		if (dx < 0) {
			i += 180;
		}
		double rads = Math.atan(dy / dx);
		double angle = 90 + i + Math.toDegrees(rads);
		return (int) angle;
	}
	/**
	 * Finds the angle from this ship to the target point
	 */
	public int findAngleto(Point p) {
		int i = 0;
		double dx = p.x - getXpos();
		double dy = (p.y - getYpos());
		if (dx < 0) {
			i += 180;
		}
		double rads = Math.atan(dy / dx);
		double angle = 90 + i + Math.toDegrees(rads);
		return (int) angle;
	}
	/**
	 * Finds the angle from point 1 to point 2
	 */
	public int findAngleBetween(Point p1, Point p2) {
		int i = 0;
		double dx = p1.x - p2.x;
		double dy = p1.y - p2.y;
		if (dx < 0) {
			i += 180;
		}
		double rads = Math.atan(dy / dx);
		double angle = 90 + i + Math.toDegrees(rads);
		return (int) angle;
	}
	public double getDistanceTo(Warp ship) {
		double dx = ship.getXpos() - getXpos();
		double dy = (ship.getYpos() - getYpos());
		double cs = dx * dx + dy * dy;
		return Math.sqrt(cs);
	}
	/**
	 * Return the total effectiveness level (all of this system's stations
	 * combined).
	 */
	@Override
	public int getTotalEffectivenessLevel() {
		int temp = 0;
		for (int i = 0; i < getStations().size(); i++) {
			temp += getStations().get(i).getItem().getEffectivenessLevel();
		}
		return temp;
	}
	// Getters and Setters
	// public int getMaxWarp() {
	// return maxWarp;
	// }
	// public void setMaxWarp(int maxWarp) {
	// this.maxWarp = maxWarp;
	// }
	// public void setWarp(int warp) {
	// this.warp = warp;
	// }
	// public int getWarp() {
	// return warp;
	// }
	public float getXpos() {
		return xPos;
	}
	public void setXpos(float xPos) {
		this.xPos = xPos;
	}
	public float getYpos() {
		return yPos;
	}
	public void setYpos(float yPos) {
		this.yPos = yPos;
	}
	public int getBearing() {
		return bearing;
	}
	public void setBearing(int bearing) {
		if (Home.getBattleLoc() != null) {
			Home.getBattleLoc().updateShipArcs();
		}
		// int i = StaticFunctions.within360(bearing);
		this.bearing = StaticFunctions.within360(bearing);
	}
	public double getSpeed() {
		return speed;
	}
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	public double getMaxSpeed() {
		if (Home.creativeMode) {
			return 40;
		}
		return getTotalEffectivenessLevel();
	}
	public BufferedImage getBuffImage() {
		return bi;
	}
	public Image getImage() {
		return parentShip.getImg();
	}
	public int[] getStarDate() {
		return starDate;
	}
	public void setStarDate(int[] starDate) {
		this.starDate = starDate;
	}
	public boolean isAutoPilot() {
		return autoPilot;
	}
	public void setAutoPilot(boolean autoPilot) {
		this.autoPilot = autoPilot;
	}
	public int getMaxTurn() {
//		int i = getParentShip().getHelm().getTotalEffectivenessLevel();
		if (Home.creativeMode){
			return 10;
		}
		return getParentShip().getHelm().getTotalEffectivenessLevel();
	}
	// public void setMaxTurn(int maxTurn) {
	// this.maxTurn = maxTurn;
	// }
	public HelmSystem getHelm(){
		return getParentShip().getHelm();
	}
}
