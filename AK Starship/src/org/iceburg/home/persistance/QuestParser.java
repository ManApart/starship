package org.iceburg.home.persistance;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.main.Home;
import org.iceburg.home.ship.systems.Comms;
import org.iceburg.home.story.Quest;
import org.iceburg.home.story.QuestEvent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class QuestParser extends Parser {
	public ArrayList<Quest> quests;

	public QuestParser() {
		quests = new ArrayList<Quest>();
		parseQuests();
	}
	public ArrayList<Quest> getQuests() {
		return quests;
	}
	public void setQuests(ArrayList<Quest> quests) {
		this.quests = quests;
	}
	/**
	 * Return's a list of the available quests (Parses their title's and ids,
	 * but leave's their event lists null) (When a quest is selected it should
	 * be parsed by id to get all of its events)
	 */
	public void parseQuests() {
		InputStream starFile = Home.resources.getClass().getResourceAsStream("xml/Quests.xml");
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(starFile);
			doc.getDocumentElement().normalize();
			NodeList shipList = doc.getElementsByTagName("Quest");
			Element eElement = null;
			for (int i = 0; i < shipList.getLength(); i++) {
				eElement = (Element) shipList.item(i);
				String id = eElement.getAttribute("id");
				String title = getTextValue(eElement, "title");
				getQuests().add(new Quest(id, title));
			}
			// System.out.println(nList.getLength() +
			// " star classes processed");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error parsing quest titles in xml/Structures.xml");
		}
	}
	/**
	 * Create's a Quest from the given id and then populates it's events
	 */
	public Quest parseQuest(String questID) {
		Quest q = getQuestByID(questID);
		if (q == null){
			System.out.println("Quest Parser: No quest by that name found!");
		}
		q = parseQuest(q);
		return q;
	}
	/**
	 * Parses a Quest's id in order to populate it's events
	 */
	public static Quest parseQuest(Quest q) {
		InputStream starFile = Home.resources.getClass().getResourceAsStream("xml/Quests.xml");
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(starFile);
			doc.getDocumentElement().normalize();
			NodeList shipList = doc.getElementsByTagName("Quest");
			Element eElement = null;
			// Find our quest
			for (int i = 0; i < shipList.getLength(); i++) {
				eElement = (Element) shipList.item(i);
				String id = eElement.getAttribute("id");
				if (q.getId().equals(id)) {
					break;
				}
			}
			for (int i = 0; i < eElement.getElementsByTagName("event").getLength(); i++) {
				q.getEvents().add(getTextValue(i, eElement, "event"));
				// TODO - use tilesets to add items to default tiles here
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error parsing quest " + q.getName()
					+ " in xml/Quests.xml");
		}
		return q;
	}
	/**
	 * Parse the event and take the appropriate course of action
	 */
	public static QuestEvent parseEvent(String id, Quest parent) {
		InputStream starFile = Home.resources.getClass().getResourceAsStream("xml/Events.xml");
		QuestEvent qe = new QuestEvent(id);
		// System.out.println("Parseing event " + id);
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(starFile);
			doc.getDocumentElement().normalize();
			NodeList shipList = doc.getElementsByTagName("Event");
			Element e = null;
			// Find our event
			for (int i = 0; i < shipList.getLength(); i++) {
				e = (Element) shipList.item(i);
				String eid = e.getAttribute("id");
				// this guy exists and has no true/false,
				if (id.equals(eid)) {
					break;
				}
			}
			// if has a success requirement, let us know we need to check
			// and what true/false we need to see from quest
			if (attributeExists(e, "success")) {
				
			}
			// parse location (include's possible ship additions)
			if (elementExists(e, "location")) {
				Element el = (Element) e.getElementsByTagName("location").item(0);
				qe.setLoc(BattleLocationParser.parseBattleLocation(el));
				// now add dist and time attributes if they exist
				if (attributeExists(el, "dist")) {
					qe.setHasDistance(true);
					qe.setDistance(QuestEvent.distanceFactor
							* getIntAttributeValue(el, "dist"));
					if (attributeExists(el, "time")) {
						qe.setHasTime(true);
						qe.setTime(Comms.parseTime(getTextAttributeValue(el, "time")));
					}
				}
			}
			// parse text
			if (elementExists(e, "preText")) {
				for (int i = 0; i < e.getElementsByTagName("preText").getLength(); i++) {
					qe.getPreText().add(getTextValue(i, e, "preText"));	
				}
			}
			if (elementExists(e, "arriveText")) {
				for (int i = 0; i < e.getElementsByTagName("arriveText").getLength(); i++) {
					qe.getArriveText().add(getTextValue(i, e, "arriveText"));	
				}
			}
			if (elementExists(e, "arriveTextW")) {
				for (int i = 0; i < e.getElementsByTagName("arriveTextW").getLength(); i++) {
					qe.getArriveTextW().add(getTextValue(i, e, "arriveTextW"));	
				}
			}
			if (elementExists(e, "arriveTextF")) {
				for (int i = 0; i < e.getElementsByTagName("arriveTextF").getLength(); i++) {
					qe.getArriveTextF().add(getTextValue(i, e, "arriveTextF"));	
				}
			}
			if (elementExists(e, "postText")) {
				for (int i = 0; i < e.getElementsByTagName("postText").getLength(); i++) {
					qe.getPostText().add(getTextValue(i, e, "postText"));	
				}
			}
			if (elementExists(e, "postTextW")) {
				for (int i = 0; i < e.getElementsByTagName("postTextW").getLength(); i++) {
					qe.getPostTextW().add(getTextValue(i, e, "postTextW"));	
				}
			}
			if (elementExists(e, "postTextF")) {
				for (int i = 0; i < e.getElementsByTagName("postTextF").getLength(); i++) {
					qe.getPostTextF().add(getTextValue(i, e, "postTextF"));	
				}
			}
		
			// parse add Crew
			if (elementExists(e, "addCrew")) {
				Element n = (Element) e.getElementsByTagName("addCrew").item(0);
				for (int i = 0; i < n.getElementsByTagName("crew").getLength(); i++) {
					Element el = (Element) n.getElementsByTagName("crew").item(i);
					CrewMan c = ShipParser.parseCrewMan(el);
					c.setPlayerControlled(true);
					qe.getCrew().add(c);
				}
				//fail crew
				for (int i = 0; i < n.getElementsByTagName("crewF").getLength(); i++) {
					Element el = (Element) n.getElementsByTagName("crewF").item(i);
					CrewMan c = ShipParser.parseCrewMan(el);
					c.setPlayerControlled(true);
					qe.getCrewF().add(c);
				}
			}
			// parse add Item
			if (elementExists(e, "addItem")) {
				Element n = (Element) e.getElementsByTagName("addItem").item(0);
				for (int i = 0; i < n.getElementsByTagName("item").getLength(); i++) {
					qe.getItems().add(Home.resources.getItems().findItem(getTextValue(i, n, "item")));
				}
				for (int i = 0; i < n.getElementsByTagName("itemF").getLength(); i++) {
					qe.getItemsF().add(Home.resources.getItems().findItem(getTextValue(i, n, "itemF")));
				}
			}
			if (elementExists(e, "addItem")) {
				Element n = (Element) e.getElementsByTagName("addItem").item(0);
				for (int i = 0; i < n.getElementsByTagName("item").getLength(); i++) {
					// Element el = (Element)
					// n.getElementsByTagName("item").item(i);
					qe.getItems().add(Home.resources.getItems().findItem(getTextValue(i, n, "item")));
				}
			}
			// parse unlock
			if (elementExists(e, "unlock")) {
				qe.setUnlock(getTextValue(e, "unlock"));
			}
			if (elementExists(e, "unlockF")) {
				qe.setUnlock(getTextValue(e, "unlockF"));
			}
			// parse new Quest
			if (elementExists(e, "quest")) {
				qe.setNewQuest(getTextValue(e, "quest"));
			}
			if (elementExists(e, "questF")) {
				qe.setNewQuest(getTextValue(e, "questF"));
			}
			// parse wait - not used
			if (elementExists(e, "wait")) {
				qe.setWaitTime(Comms.parseTime(getTextValue(e, "wait")));
			}
			// parse wait
			if (elementExists(e, "sound")) {
				qe.setSound(getTextValue(e, "sound"));
			}
			if (elementExists(e, "soundF")) {
				qe.setSound(getTextValue(e, "soundF"));
			}
			return qe;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error parsing event " + id + " in xml/Quests.xml");
			return qe;
		}
	}
	/**
	 * Return's the quest that matches this id, if it exists
	 */
	public Quest getQuestByID(String id) {
		for (int i = 0; i < getQuests().size(); i++) {
			if (getQuests().get(i).getId().equals(id)) {
				return getQuests().get(i);
			}
		}
		return null;
	}
}
