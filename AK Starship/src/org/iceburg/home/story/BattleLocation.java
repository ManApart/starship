package org.iceburg.home.story;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import org.iceburg.home.main.Constants;
import org.iceburg.home.main.Home;
import org.iceburg.home.main.StaticFunctions;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.ship.systems.Warp;
import org.iceburg.home.ship.systems.Weapons;
import org.iceburg.home.sound.Sound;

public class BattleLocation {
	String id, name;
	ArrayList<Ship> ships;
	// TODO Images: Background, Overlay 1, Star, planet, Overlay 2
	Image background, overlay1, star, planet, overlay2;
	String backgroundText, overlay1Text, starText, planetText, overlay2Text, songText;
	// Load paths at startup and only load images as needed
	// points for single graphic locations
	Point starPoint, planetPoint, overlay2Point;
	// scroll rates
	Double backgroundRate, overlay1Rate, starRate, planetRate, overlay2Rate,
			backgroundRateY, overlay1RateY, starRateY, planetRateY, overlay2RateY;
	Sound song;
	private boolean inBattle;

	// Constructor
	public BattleLocation() {
		this.setShips(new ArrayList<Ship>());
		// this.inBattle = true;
		// Gen random strings
		// names
		ArrayList<String> list = Home.resources.getBattleLocations().getNames();
		String s = list.get(StaticFunctions.randRange(0, list.size() - 1));
		setName(s);
		// music
		list = Home.resources.getBattleLocations().getSongs();
		s = list.get(StaticFunctions.randRange(0, list.size() - 1));
		setSongText(s);
		// background
		list = Home.resources.getBattleLocations().getBackgrounds();
		s = list.get(StaticFunctions.randRange(0, list.size() - 1));
		setBackgroundText(s);
		// overlay1
		list = Home.resources.getBattleLocations().getOverlay1s();
		s = list.get(StaticFunctions.randRange(0, list.size() - 1));
		setOverlay1Text(s);
		// star
		list = Home.resources.getBattleLocations().getStars();
		s = list.get(StaticFunctions.randRange(0, list.size() - 1));
		setStarText(s);
		// planet
		list = Home.resources.getBattleLocations().getPlanets();
		s = list.get(StaticFunctions.randRange(0, list.size() - 1));
		setPlanetText(s);
		// overlay2
		list = Home.resources.getBattleLocations().getOverlay2s();
		s = list.get(StaticFunctions.randRange(0, list.size() - 1));
		setOverlay2Text(s);
	}
	@Override
	public String toString() {
		return getName();
	}
	/**
	 * Check's to see if we're in combat
	 */
	public void checkBattleStatus() {
		setInBattle(false);
		//returns to false so long as there is at least 1 friendly crew
		boolean gameover = true;
		for (int i = 0; i < ships.size(); i++) {
			for (int j = 0; j < ships.get(i).getCrew().size(); j++) {
				if (!ships.get(i).getCrew().get(j).isPlayerControlledIgnoreCheat()) {
					setInBattle(true);
				}
				else{
					gameover = false;
				}
			}
		}
		// if no longer in battle, advance quest event step
		if (!isInBattle()) {
			if (Home.getPlayer().getQuest() != null){
				Home.getPlayer().getQuest().getCurrentEvent().incStep();
			}
		}
		if (gameover) {
			Home.getPlayer().gameOver();
			
		}
	}
	public boolean isInBattle() {
		return inBattle;
	}
	public void setInBattle(boolean inBattle) {
		this.inBattle = inBattle;
	}
	/**
	 * Returns the warp space battle location from data and parses the
	 * background images
	 */
	// fill out specifics for space, could improve quite a bit to make look
	// nicer
	public static BattleLocation spaceLocation() {
		BattleLocation loc = new BattleLocation();
		loc.setName("Warp Space");
		loc.setBackgroundText("textures/world/star fields/Starfield.jpg");
		loc.setOverlay1Text("textures/world/star fields close/Big Stars.png");
		loc.setStarText("textures/world/Empty.png");
		loc.setPlanetText("textures/world/Empty.png");
		loc.setOverlay2Text("textures/world/Empty.png");
		return loc;
	}
	/**
	 * Generate's this location's images/song from its strings
	 */
	public void genImages() {
		// song
		String s = getSongText();
		setSong(new Sound(s));
		try {
			// background
			s = getBackgroundText();
			setBackground(new ImageIcon(Home.resources.getClass().getResource(s)).getImage());
			backgroundRate = (0.0 + getBackground().getWidth(null)) / Constants.arenaSize;
			backgroundRateY = (0.0 + getBackground().getHeight(null))
					/ Constants.arenaSize;
			// overlay1
			s = getOverlay1Text();
			setOverlay1(new ImageIcon(Home.resources.getClass().getResource(s)).getImage());
			overlay1Rate = (0.0 + getOverlay1().getWidth(null)) / Constants.arenaSize;
			overlay1RateY = (0.0 + getOverlay1().getHeight(null)) / Constants.arenaSize;
			// make sure rate increases as we get closer
			double tempRate = overlay1Rate;
			int i = 2;
			while (tempRate <= backgroundRate) {
				tempRate = overlay1Rate * i;
				i++;
			}
			overlay1Rate = tempRate;
			tempRate = overlay1RateY;
			i = 2;
			while (tempRate <= backgroundRateY) {
				tempRate = overlay1RateY * i;
				i++;
			}
			overlay1RateY = tempRate;
			// star
			s = getStarText();
			setStar(new ImageIcon(Home.resources.getClass().getResource(s)).getImage());
			starPoint = new Point();
			starPoint.x = StaticFunctions.randRange(0, (Constants.arenaSize - getStar().getWidth(null)) / 2);
			starPoint.y = StaticFunctions.randRange(0, (Constants.arenaSize - getStar().getHeight(null)) / 2);
			starRate = (0.0 + getStar().getWidth(null)) / Constants.arenaSize;
			starRateY = (0.0 + getStar().getHeight(null)) / Constants.arenaSize;
			// starPoint.x = 2500;
			// starPoint.y = 10;
			// make sure rate increases as we get closer
			tempRate = starRate;
			i = 2;
			while (tempRate <= overlay1Rate) {
				tempRate = starRate * i;
				i++;
			}
			starRate = tempRate;
			// now for the y
			tempRate = starRateY;
			i = 2;
			while (tempRate <= overlay1RateY) {
				tempRate = starRateY * i;
				i++;
			}
			starRateY = tempRate;
			// planet
			s = getPlanetText();
			setPlanet(new ImageIcon(Home.resources.getClass().getResource(s)).getImage());
			planetPoint = new Point();
			planetPoint.x = StaticFunctions.randRange(0, (Constants.arenaSize - getPlanet().getWidth(null)) / 2);
			planetPoint.y = StaticFunctions.randRange(0, (Constants.arenaSize - getPlanet().getHeight(null)) / 2);
			planetRate = (0.0 + getPlanet().getWidth(null)) / Constants.arenaSize;
			planetRateY = (0.0 + getPlanet().getHeight(null)) / Constants.arenaSize;
			// make sure rate increases as we get closer
			tempRate = planetRate;
			i = 2;
			while (tempRate <= starRate) {
				tempRate = planetRate * i;
				i++;
			}
			planetRate = tempRate;
			tempRate = planetRateY;
			i = 2;
			while (tempRate <= starRateY) {
				tempRate = planetRateY * i;
				i++;
			}
			planetRateY = tempRate;
			// planetPoint.x = 5700;
			// planetPoint.y = 10;
			// overlay2
			s = getOverlay2Text();
			setOverlay2(new ImageIcon(Home.resources.getClass().getResource(s)).getImage());
			overlay2Point = new Point();
			overlay2Point.x = StaticFunctions.randRange(0, (Constants.arenaSize - getOverlay2().getWidth(null)) / 2);
			overlay2Point.y = StaticFunctions.randRange(0, (Constants.arenaSize - getOverlay2().getHeight(null)) / 2);
			overlay2Rate = (0.0 + getOverlay2().getWidth(null)) / Constants.arenaSize;
			overlay2RateY = (0.0 + getOverlay2().getHeight(null)) / Constants.arenaSize;
			// make sure rate increases as we get closer
			tempRate = overlay2Rate;
			i = 2;
			while (tempRate <= planetRate) {
				tempRate = overlay2Rate * i;
				i++;
			}
			overlay2Rate = tempRate;
			tempRate = overlay2RateY;
			i = 2;
			while (tempRate <= planetRateY) {
				tempRate = overlay2RateY * i;
				i++;
			}
			overlay2RateY = tempRate;
			// loc.overlay2Point.x = 10;
			// loc.overlay2Point.y = 10;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error parsing background");
		}
		// TODO - this plays song - comment out for no background track
		 Home.getSoundManager().playSong(getSong());
	}
	public void paintWOScroll(Graphics2D g) {
		Warp homeShip = Home.getShip().getWarp();
		// get the center of the screen
		int locX = (int) (Constants.viewScreenSize - homeShip.getImage().getWidth(null)) / 2;
		int locY = (int) (Constants.viewScreenSize - homeShip.getImage().getHeight(null)) / 2;
		g.setColor(Color.black);
		// TODO - change to screen size
		g.fillRect(0, 0, 500, 500);
		// Scroll rates need to be a multiple of picture size/arena size
		// draw background
		if (getBackground() != null) {
			drawScrollingWrappedImage(g, getBackground(), backgroundRate, backgroundRateY);
		}
		if (getOverlay1() != null) {
			drawScrollingWrappedImage(g, getOverlay1(), overlay1Rate, overlay1RateY);
		}
		if (getStar() != null) {
			drawScrollingImage(g, getStar(), getStarPoint(), starRate, starRateY);
		}
		if (getPlanet() != null) {
			drawScrollingImage(g, getPlanet(), getPlanetPoint(), planetRate, planetRateY);
		}
		// draw ships
		if (ships.size() > 0) {
			for (int s = 0; s < ships.size(); s++) {
				Warp ship = ships.get(s).getWarp();
				if (ship == homeShip) {
					ship.paintShip(g, locX, locY);
				} else {
					// get distance from player ship
					int dx = (int) (ship.getXpos() - homeShip.getXpos());
					int dy = (int) (ship.getYpos() - homeShip.getYpos());
					ship.paintShip(g, dx + locX, dy + locX);
				}
			}
		}
		// Draw final Overlay
		if (getOverlay2() != null) {
			drawScrollingImage(g, getOverlay2(), getOverlay2Point(), overlay2Rate, overlay2RateY);
		}
	}
	/**
	 * Draw's an image that scrolls, but is not wrapped. Only 1 of the image is
	 * drawn at a time
	 * 
	 * @param g
	 *            - graphics to draw to
	 * @param image
	 *            - image drawn
	 * @param orgin
	 *            - location the image should be drawn to
	 * @param scrollX
	 *            - rate at which it scrolls
	 */
	public void drawScrollingImage(Graphics2D g, Image image, Point origin,
			double scrollX, double scrollY) {
		// int scrollRate = 1;
		Warp homeShip = Home.getShip().getWarp();
		int dx = (int) ((scrollX * (origin.x - homeShip.getXpos())));
		int dy = (int) (scrollY * (origin.y - homeShip.getYpos()));
		// Draw Image
		g.drawImage(image, dx, dy, null);
		// if just 1 screen from orgin, draw objects in old location as well
		if (homeShip.getXpos() <= (Constants.viewScreenSize - Constants.arenaSize / 2
				* scrollX)
				|| homeShip.getYpos() <= (Constants.viewScreenSize * scrollY)) {
			// draw horizontal guy
			if (homeShip.getXpos() <= (Constants.viewScreenSize - Constants.arenaSize / 2
					* scrollX)) {
				// changed to dt so it doesn't mess up the vertical check
				int dt = (int) ((dx - Constants.arenaSize * scrollX));
				g.drawImage(image, dt, dy, null);
			}
			// draw vertical guy
			if (homeShip.getYpos() <= (Constants.viewScreenSize - Constants.arenaSize / 2
					* scrollY)) {
				// System.out.println("draw vert");
				dy = (int) ((dy - Constants.arenaSize * scrollY));
				g.drawImage(image, dx, dy, null);
			}
			// This is for when you have a planet above and below, and they're
			// at the edge.
			// one planet automatically gets another, but the second planet
			// needs a new doppleganger for fade in
			// yeah, that didn't make any sense to me either....
			if (homeShip.getXpos() <= (Constants.viewScreenSize - Constants.arenaSize / 2
					* scrollX)
					&& homeShip.getYpos() >= (-Constants.viewScreenSize + Constants.arenaSize
							/ 2 * scrollY)) {
				dx = (int) ((dx - Constants.arenaSize * scrollX));
				// dy was changed above. We know this because this is only true
				// if above was true
				g.drawImage(image, dx, dy, null);
			}
			// g.drawImage(image, dx , dy , null);
		}
	}
	/**
	 * Draws an image that is wrapped - when the edge of the image is reached,
	 * it is drawn again
	 * 
	 * @param g
	 *            -graphics to draw to
	 * @param image
	 *            - the image drawn
	 * @param scrollX
	 *            - the speed at which it scrolls
	 */
	public void drawScrollingWrappedImage(Graphics2D g, Image image, double scrollX,
			double scrollY) {
		Warp homeShip = Home.getShip().getWarp();
		int width = image.getWidth(null);
		int height = image.getHeight(null);
		int w = 0;
		int h = 0;
		int dx = (int) (-scrollX * homeShip.getXpos() - (Constants.viewScreenSize - homeShip.getImage().getWidth(null)) / 2);
		int dy = (int) (-scrollY * homeShip.getYpos() - (Constants.viewScreenSize - homeShip.getImage().getHeight(null)) / 2);
		dx = dx % width;
		dy = dy % height;
		// Draw initial Image
		g.drawImage(image, (int) (dx), (int) (dy), null);
		// Draw horizontal image
		if (dx < 0) {
			g.drawImage(image, dx + width, dy, null);
			w = 1;
		} else if (dx > 0) {
			g.drawImage(image, dx - width, dy, null);
			w = -1;
		}
		// Draw vertical image
		if (dy < 0) {
			g.drawImage(image, dx, dy + height, null);
			h = 1;
		} else if (dy > 0) {
			g.drawImage(image, dx, dy - height, null);
			h = -1;
		}
		// Draw diagnal image
		if (w != 0 || h != 0) {
			g.drawImage(image, dx + width * w, dy + height * h, null);
		}
	}
	/**
	 * Updates all the ships at this location
	 */
	public void updateShips() {
		for (int i = 0; i < getShips().size(); i++) {
			getShips().get(i).updateShip();
		}
	}
	/**
	 * Updates all this location's ships' arcs
	 */
	public void updateShipArcs() {
		for (int i = 0; i < getShips().size(); i++) {
			Ship s = getShips().get(i);
			if (s.hasSystem(Weapons.systemMain)) {
				// System.out.println("BattleLoc: Updating arcs for "+ s);
				s.getWeapons().evaluateWeaponArcs();
			}
		}
	}
	public void addShip(Ship ship) {
		// update targets
		if (ship != Home.getShip() && Home.getPlayer().getCurrentLocation() == this) {
			ship.getHelm().setTargetShip(Home.getShip().getWarp());
			Home.getShip().getHelm().setTargetShip(ship.getWarp());
		}
		// we're adding the player ship, if there is another ship here, set
		// targets
		else if (getShips().size() > 0) {
			Ship s = getShips().get(0);
			ship.getHelm().setTargetShip(s.getWarp());
			s.getHelm().setTargetShip(ship.getWarp());
		}
		getShips().add(ship);
//		checkBattleStatus();
	}
	public void removeShip(Ship ship) {
		if (getShips().contains(ship)) {
			getShips().remove(ship);
		}
	}
	/**
	 * If the player's ship isn't alone, set target ships
	 */
	public void addEnemyShipToTarget() {
	}
	public ArrayList<Ship> getShips() {
		return ships;
	}
	public void setShips(ArrayList<Ship> ships) {
		this.ships = ships;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Image getBackground() {
		return background;
	}
	public void setBackground(Image background) {
		this.background = background;
	}
	public Image getOverlay1() {
		return overlay1;
	}
	public void setOverlay1(Image overlay1) {
		this.overlay1 = overlay1;
	}
	public Image getStar() {
		return star;
	}
	public void setStar(Image star) {
		this.star = star;
	}
	public Image getPlanet() {
		return planet;
	}
	public void setPlanet(Image planet) {
		this.planet = planet;
	}
	public Image getOverlay2() {
		return overlay2;
	}
	public void setOverlay2(Image overlay2) {
		this.overlay2 = overlay2;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Point getStarPoint() {
		return starPoint;
	}
	public void setStarPoint(Point starPoint) {
		this.starPoint = starPoint;
	}
	public Point getPlanetPoint() {
		return planetPoint;
	}
	public void setPlanetPoint(Point planetPoint) {
		this.planetPoint = planetPoint;
	}
	public Point getOverlay2Point() {
		return overlay2Point;
	}
	public void setOverlay2Point(Point overlay2Point) {
		this.overlay2Point = overlay2Point;
	}
	public Sound getSong() {
		return song;
	}
	public void setSong(Sound song) {
		this.song = song;
	}
	public String getBackgroundText() {
		return backgroundText;
	}
	public void setBackgroundText(String backgroundText) {
		this.backgroundText = backgroundText;
	}
	public String getOverlay1Text() {
		return overlay1Text;
	}
	public void setOverlay1Text(String overlay1Text) {
		this.overlay1Text = overlay1Text;
	}
	public String getStarText() {
		return starText;
	}
	public void setStarText(String starText) {
		this.starText = starText;
	}
	public String getPlanetText() {
		return planetText;
	}
	public void setPlanetText(String planetText) {
		this.planetText = planetText;
	}
	public String getOverlay2Text() {
		return overlay2Text;
	}
	public void setOverlay2Text(String overlay2Text) {
		this.overlay2Text = overlay2Text;
	}
	public String getSongText() {
		return songText;
	}
	public void setSongText(String songText) {
		this.songText = songText;
	}
}
