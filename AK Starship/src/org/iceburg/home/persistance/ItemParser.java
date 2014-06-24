package org.iceburg.home.persistance;

import java.awt.Color;
import java.util.ArrayList;

import org.iceburg.home.items.ACGenerator;
import org.iceburg.home.items.ACVent;
import org.iceburg.home.items.Cabin;
import org.iceburg.home.items.CargoBay;
import org.iceburg.home.items.Door;
import org.iceburg.home.items.HelmItem;
import org.iceburg.home.items.Item;
import org.iceburg.home.items.MedBed;
import org.iceburg.home.items.MedConsole;
import org.iceburg.home.items.Sensor;
import org.iceburg.home.items.Shield;
import org.iceburg.home.items.WarpGenerator;
import org.iceburg.home.items.Weapon;
import org.iceburg.home.ship.systems.Cabins;
import org.iceburg.home.ship.systems.CargoHold;
import org.iceburg.home.ship.systems.Doors;
import org.iceburg.home.ship.systems.HelmSystem;
import org.iceburg.home.ship.systems.LifeSupport;
import org.iceburg.home.ship.systems.MedBay;
import org.iceburg.home.ship.systems.Sensors;
import org.iceburg.home.ship.systems.Shields;
import org.iceburg.home.ship.systems.Warp;
import org.iceburg.home.ship.systems.Weapons;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ItemParser extends Parser {
	public ArrayList<Item> items;

	// public ArrayList <Tile> tiles;
	public ItemParser() {
		// tiles = new ArrayList<Tile>();
		items = new ArrayList<Item>();
		// parseTiles();
		parseFile("xml/Items.xml");
	}
	@Override
	public void parse(Document doc) {
		NodeList nodeList = doc.getElementsByTagName("Item");
		Item item = new Item();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) node;
				item = parseItem(e);
			}
			items.add(item);
		}
	}
	public Item findItem(String itemID) {
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).getId().equals(itemID)) {
				return items.get(i);
			}
		}
		return null;
	}
	/**
	 * Parses and returns the item at element e
	 */
	public static Item parseItem(Element e) {
		Item item = new Item();
		// set stuff we know we should get
		item.setId(e.getAttribute("id"));
		item.setName(getTextValue(e, "name"));
		// item.setTechLevel(getIntValue(eElement, "techLevel"));
		item.setItemType(Color.decode(getTextValue(e, "itemType")));
		if (elementExists(e, "image")) {
			item.setImagePath(getTextValue(e, "image"));
		}
		item.setHealthTotal(getIntValue(e, "health"));
		item.setHealth(item.getHealthTotal());
		item.setMaxPower(getIntValue(e, "powerMax"));
		item.setDescription(getTextValue(e, "description"));
		item = parseItemTypes(item, e);
		return item;
	}
	public static Item parseItemTypes(Item item, Element eElement) {
		if (item.getItemType().equals(Weapons.colorWeaponsMissile)
				|| item.getItemType().equals(Weapons.systemMain)) {
			return parseWeapon(item, eElement);
		} else if (item.getItemType().equals(Doors.systemMain)) {
			return parseDoor(item, eElement);
		} else if (item.getItemType().equals(Shields.systemMain)) {
			return parseShield(item, eElement);
		} else if (item.getItemType().equals(MedBay.colorMedBed)) {
			return parseMedBed(item, eElement);
		} else if (item.getItemType().equals(MedBay.systemMain)) {
			return parseMedConsole(item, eElement);
		} else if (item.getItemType().equals(HelmSystem.systemMain)) {
			return parseHelm(item, eElement);
		} else if (item.getItemType().equals(Warp.systemMain)) {
			return parseWarpGen(item, eElement);
		} else if (item.getItemType().equals(LifeSupport.systemMain)) {
			return parseAC(item, eElement);
		} else if (item.getItemType().equals(LifeSupport.colorACVent)) {
			return parseACVent(item, eElement);
		} else if (item.getItemType().equals(Cabins.systemMain)) {
			return parseCabin(item, eElement);
		} else if (item.getItemType().equals(CargoHold.systemMain)) {
			return parseCargo(item, eElement);
		} else if (item.getItemType().equals(Sensors.systemMain)) {
			return parseSensors(item, eElement);
		}
		return item;
	}
	public static Item parseWeapon(Item item, Element eElement) {
		Weapon w = new Weapon();
		w.cloneItemBase(item);
		w.setMaxArc(getIntValue(eElement, "arc"));
		w.setDamage(getIntValue(eElement, "damage"));
		w.setMissileHealth(getIntValue(eElement, "missleHealth"));
		if (w.getItemType().equals(Weapons.systemMain)) {
			// w.setFrequency(getIntValue(eElement, "frequency"));
		} else {
			w.setAOE(getIntValue(eElement, "AOE"));
			w.setReloadTime(getIntValue(eElement, "reloadTime"));
		}
		return w;
	}
	public static Item parseShield(Item item, Element eElement) {
		Shield s = new Shield();
		s.cloneItemBase(item);
		s.setAOE(getIntValue(eElement, "AOE"));
		// s.setFrequency(getIntValue(eElement, "frequency"));
		return s;
	}
	public static Item parseMedBed(Item item, Element eElement) {
		MedBed s = new MedBed();
		s.cloneItemBase(item);
		s.setMaxCrewHealth(getIntValue(eElement, "crewHealth"));
		return s;
	}
	public static Item parseMedConsole(Item item, Element eElement) {
		MedConsole s = new MedConsole();
		s.cloneItemBase(item);
		s.setHealingRate(getIntValue(eElement, "healingRate"));
		return s;
	}
	public static Item parseDoor(Item item, Element eElement) {
		Door s = new Door();
		s.cloneItemBase(item);
		s.setOpenImagePath(getTextValue(eElement, "openImage"));
		return s;
	}
	public static Item parseHelm(Item item, Element eElement) {
		HelmItem s = new HelmItem();
		s.cloneItemBase(item);
		s.setTurnSpeed(getIntValue(eElement, "turnSpeed"));
		return s;
	}
	public static Item parseWarpGen(Item item, Element eElement) {
		WarpGenerator s = new WarpGenerator();
		s.cloneItemBase(item);
		s.setSpeed(getIntValue(eElement, "speed"));
		return s;
	}
	public static Item parseAC(Item item, Element eElement) {
		ACGenerator s = new ACGenerator();
		s.cloneItemBase(item);
		s.setCapacity(getIntValue(eElement, "capacity"));
		return s;
	}
	public static Item parseACVent(Item item, Element eElement) {
		ACVent s = new ACVent();
		s.cloneItemBase(item);
		s.setDrainRate(getIntValue(eElement, "drainRate"));
		return s;
	}
	public static Cabin parseCabin(Item item, Element eElement) {
		Cabin s = new Cabin();
		s.cloneItemBase(item);
		s.setMaxCrew(getIntValue(eElement, "maxCrew"));
		return s;
	}
	public static Item parseSensors(Item item, Element eElement) {
		Sensor s = new Sensor();
		s.cloneItemBase(item);
		s.setLevel(getIntValue(eElement, "level"));
		return s;
	}
	public static Item parseCargo(Item item, Element eElement) {
		CargoBay s = new CargoBay();
		s.cloneItemBase(item);
		s.setCapacity(getIntValue(eElement, "capacity"));
		return s;
	}
}
