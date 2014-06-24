package org.iceburg.home.persistance;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.iceburg.home.actors.Race;
import org.iceburg.home.main.Home;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RaceParser extends Parser {
	public ArrayList<Race> races;
	public HashMap<String, Font> fonts;

	public RaceParser() {
		races = new ArrayList<Race>();
		fonts = new HashMap<String, Font>();
		parseFile("xml/Races.xml");
	}
	@Override
	public void parse(Document doc) {
		NodeList nodeList = doc.getElementsByTagName("Race");
		Race race = new Race();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				race = new Race();
				Element eElement = (Element) node;
				// Attributes and bonuses and stuff
				race.setId(eElement.getAttribute("id"));
				race.setName(getTextValue(eElement, "raceName"));
				race.setDescription(getTextValue(eElement, "description"));
				race.setHealthTotal(getIntValue(eElement, "healthTotal"));
				race.setEnergyFields(getIntValue(eElement, "energyFields"));
				race.setNavigation(getIntValue(eElement, "navigation"));
				race.setAnalysis(getIntValue(eElement, "analysis"));
				race.setPowerDistribution(getIntValue(eElement, "powerDistribution"));
				// names:
				int l = eElement.getElementsByTagName("name").getLength();
				for (int j = 0; j < l; j++) {
					race.getNames().add(getTextValue(j, eElement, "name"));
				}
			}
			races.add(race);
		}
	}
	public Race findRace(String itemID) {
		for (int i = 0; i < races.size(); i++) {
			if (races.get(i).getId().equals(itemID)) {
				return races.get(i);
			}
		}
		return null;
	}
	public Font parseFont(String name) {
		// don't recreate font we already have
		if (fonts.containsKey(name)) {
			return fonts.get(name);
		}
		// create the font
		else {
			URL url = Home.resources.getClass().getResource(name);
			// File fontFile = new File(url.getFile());
			Font font = null;
			try {
				font = Font.createFont(Font.TRUETYPE_FONT, url.openStream());
			} catch (FontFormatException e) {
				// Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// Auto-generated catch block
				e.printStackTrace();
			}
			font = font.deriveFont(Font.PLAIN, 10);
			fonts.put(name, font);
			GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
			g.registerFont(font);
			return font;
		}
	}
}
