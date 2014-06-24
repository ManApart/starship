package org.iceburg.home.persistance;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.iceburg.home.main.Home;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.story.BattleLocation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BattleLocationParser extends Parser {
	ArrayList<String> names;
	ArrayList<String> backgrounds;
	ArrayList<String> overlay1s;
	ArrayList<String> stars;
	ArrayList<String> planets;
	ArrayList<String> overlay2s;
	ArrayList<String> songs;

	public BattleLocationParser() {
		names = new ArrayList<String>();
		backgrounds = new ArrayList<String>();
		overlay1s = new ArrayList<String>();
		stars = new ArrayList<String>();
		planets = new ArrayList<String>();
		overlay2s = new ArrayList<String>();
		songs = new ArrayList<String>();
		parseBattleLocations();
	}
	private void parseBattleLocations() {
		InputStream starFile = Home.resources.getClass().getResourceAsStream("xml/BattleLocations.xml");
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(starFile);
			doc.getDocumentElement().normalize();
			// System.out.println("Root element :" +
			// doc.getDocumentElement().getNodeName());
			NodeList nList = doc.getElementsByTagName("Location");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				// System.out.println("\nCurrent Element :" +
				// nNode.getNodeName());
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					// loc.setId(eElement.getAttribute("id"));
					// TODO - grab ALL of the files in a parant directory
					// instead of by xml line
					// String s =
					// Home.resources.getClass().getResource("textures/world/stars/SC_A.png").getFile();
					// File fileLoc = new File(s);
					// ImageIcon ii = new ImageIcon(s);
					// // fileLoc = fileLoc.getAbsoluteFile();
					// // String[] fileList = fileLoc.list();
					// // File[] fileList = fileLoc.listFiles();
					// boolean test = fileLoc.exists();
					// test = fileLoc.getAbsoluteFile().exists();
					// test = fileLoc.getCanonicalFile().exists();
					// test = fileLoc.isDirectory();
					// test = fileLoc.canRead();
					// test = fileLoc.isFile();
					// File[] fileList = fileLoc.listFiles(new FilenameFilter()
					// {
					// public boolean accept(File dir, String name) {
					// return (!name.toLowerCase().endsWith(".txt"));
					// }
					// });
					for (int i = 0; i < eElement.getElementsByTagName("name").getLength(); i++) {
						names.add(getTextValue(i, eElement, "name"));
					}
					for (int i = 0; i < eElement.getElementsByTagName("background").getLength(); i++) {
						backgrounds.add(getTextValue(i, eElement, "background"));
					}
					for (int i = 0; i < eElement.getElementsByTagName("overlay1").getLength(); i++) {
						overlay1s.add(getTextValue(i, eElement, "overlay1"));
					}
					for (int i = 0; i < eElement.getElementsByTagName("star").getLength(); i++) {
						stars.add(getTextValue(i, eElement, "star"));
					}
					for (int i = 0; i < eElement.getElementsByTagName("planet").getLength(); i++) {
						planets.add(getTextValue(i, eElement, "planet"));
					}
					for (int i = 0; i < eElement.getElementsByTagName("overlay2").getLength(); i++) {
						overlay2s.add(getTextValue(i, eElement, "overlay2"));
					}
					for (int i = 0; i < eElement.getElementsByTagName("song").getLength(); i++) {
						songs.add(getTextValue(i, eElement, "song"));
					}
					// System.out.println("Parsed Battle Locations");
				}
				// battleLocations.add(loc);
			}
			// System.out.println(nList.getLength() +
			// " star classes processed");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error parsing xml/StarClasses.xml");
		}
	}
	/**
	 * Create's a Battle location, overriding any vars with new ones provided by
	 * the input Element
	 */
	public static BattleLocation parseBattleLocation(Element e) {
		BattleLocation b = new BattleLocation();
		if (elementExists(e, "name")) {
			b.setName(getTextValue(e, "name"));
		}
		if (elementExists(e, "background")) {
			b.setBackgroundText((getTextValue(e, "background")));
		}
		if (elementExists(e, "overlay1")) {
			b.setOverlay1Text(getTextValue(e, "overlay1"));
		}
		if (elementExists(e, "star")) {
			b.setStarText(getTextValue(e, "star"));
		}
		if (elementExists(e, "planet")) {
			b.setPlanetText(getTextValue(e, "planet"));
		}
		if (elementExists(e, "overlay2")) {
			b.setOverlay1Text(getTextValue(e, "overlay2"));
		}
		if (elementExists(e, "song")) {
			b.setSongText(getTextValue(e, "song"));
		}
		// TODO -parse and add ships
		if (elementExists(e, "ship")) {
			Element el = (Element) e.getElementsByTagName("ship").item(0);
			Ship sp = Ship.shipComplete(getTextAttributeValue(el, "id"), 15, 15);
			b.addShip(sp);
		}
		// System.out.println("Parsed Battle Location");
		return b;
	}
	public ArrayList<String> getNames() {
		return names;
	}
	public ArrayList<String> getBackgrounds() {
		return backgrounds;
	}
	public ArrayList<String> getOverlay1s() {
		return overlay1s;
	}
	public ArrayList<String> getStars() {
		return stars;
	}
	public ArrayList<String> getPlanets() {
		return planets;
	}
	public ArrayList<String> getOverlay2s() {
		return overlay2s;
	}
	public ArrayList<String> getSongs() {
		return songs;
	}
	public void setSongs(ArrayList<String> songs) {
		this.songs = songs;
	}
}
