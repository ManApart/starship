package org.iceburg.home.persistance;

import java.io.InputStream;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.main.Home;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.ship.ShipFloorPlan;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ShipParser extends Parser {
	public ShipParser() {
	}
	public static Ship parseShip(String shipName) {
		// Thanks to Stack overflow, samples, and Mkyong:
		// http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
		Ship ship = new Ship();
		InputStream starFile = Home.resources.getClass().getResourceAsStream("xml/Ships.xml");
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(starFile);
			doc.getDocumentElement().normalize();
			NodeList shipList = doc.getElementsByTagName("Ship");
			Element eElement = null;
			for (int i = 0; i < shipList.getLength(); i++) {
				eElement = (Element) shipList.item(i);
				String id = eElement.getAttribute("id");
				if (shipName.equals(id)) {
					break;
				}
			}
			ship.setTitle(getTextValue(eElement, "title"));
			ship.setTileSet(getTextValue(eElement, "tileSet"));
			ship.setShipFloorplans(new ArrayList<ShipFloorPlan>());
			ship.setImg(new ImageIcon(Home.resources.getClass().getResource(getTextValue(eElement, "image"))).getImage());
			for (int i = 0; i < eElement.getElementsByTagName("floorPlan").getLength(); i++) {
				ship.getFloorplans().add(new ShipFloorPlan(ship, i, getTextValue(i, eElement, "floorPlan")));
				// TODO - use tilesets to add items to default tiles here
			}
			// add any default crew if necessary
			for (int i = 0; i < eElement.getElementsByTagName("crew").getLength(); i++) {
				ship.getCrew().add(parseCrewMan((Element) eElement.getElementsByTagName("crew").item(i)));
			}
			// System.out.println("Num Floors: "+ ship.getShipLayout().size());
			return ship;
			// System.out.println(nList.getLength() +
			// " star classes processed");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error parsing ship " + shipName
					+ " in xml/Structures.xml");
		} finally {
			return ship;
		}
	}
	/**
	 * Create's a crewman, overriding any stats with new ones provided by the
	 * input Element
	 */
	public static CrewMan parseCrewMan(Element e) {
		// System.out.println("Parse crewman");
		CrewMan c = new CrewMan(CrewMan.randDivision());
		if (elementExists(e, "name")) {
			c.setName(getTextValue(e, "name"));
		}
		// divisions
		if (elementExists(e, "division")) {
			c.setDivision(CrewMan.divisionStringToColor(getTextValue(e, "division")));
		}
		// for random gen of stats
		if (elementExists(e, "base")) {
			c.generateStats(getIntValue(e, "base"));
		}
		// skills
		if (elementExists(e, "energyFields")) {
			c.setEnergyFields(getIntValue(e, "energyFields"));
		}
		if (elementExists(e, "navigation")) {
			c.setNavigation(getIntValue(e, "navigation"));
		}
		if (elementExists(e, "analysis")) {
			c.setAnalysis(getIntValue(e, "analysis"));
		}
		if (elementExists(e, "powerDistribution")) {
			c.setPowerDistribution(getIntValue(e, "powerDistribution"));
		}
		if (elementExists(e, "healthTotal")) {
			c.setHealthTotal(getIntValue(e, "healthTotal"));
		}
		return c;
	}
}
