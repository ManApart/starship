package org.iceburg.home.data;

import org.iceburg.home.persistance.BattleLocationParser;
import org.iceburg.home.persistance.ItemParser;
import org.iceburg.home.persistance.QuestParser;
import org.iceburg.home.persistance.RaceParser;
import org.iceburg.home.persistance.ShipParser;
import org.iceburg.home.persistance.TileParser;

//Class for getting resources
public class Data {
	public static BattleLocationParser battleLocations;
	public static TileParser tiles;
	public static ShipParser structures;
	public static ItemParser items;
	public static RaceParser races;
	public static QuestParser quests;

	public void parseData() {
		battleLocations = new BattleLocationParser();
		tiles = new TileParser();
		structures = new ShipParser();
		items = new ItemParser();
		races = new RaceParser();
		quests = new QuestParser();
	}
	public static BattleLocationParser getBattleLocations() {
		return battleLocations;
	}
	public static TileParser getTiles() {
		return tiles;
	}
	public static ShipParser getStructures() {
		return structures;
	}
	public static ItemParser getItems() {
		return items;
	}
	public static RaceParser getRaces() {
		return races;
	}
	public static QuestParser getQuests() {
		return quests;
	}
}
