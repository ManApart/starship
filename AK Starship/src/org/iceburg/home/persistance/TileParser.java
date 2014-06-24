package org.iceburg.home.persistance;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import org.iceburg.home.ship.Ship;
import org.iceburg.home.ship.ShipFloorPlan;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.ship.TileSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TileParser extends Parser {
	ArrayList<Tile> tiles;
	// TilesetID: Tile,Item
	ArrayList<TileSet> tileSets;
//	HashMap<String, HashMap<Color, String>> tileSets;
	boolean parsingTileSets;

	public TileParser() {
		parsingTileSets = false;
		tiles = new ArrayList<Tile>();
		parseFile("xml/Tiles.xml");
		parsingTileSets = true;
		tileSets = new ArrayList<TileSet>();
//		tileSets = new HashMap<String, HashMap<Color, String>>();
		parseFile("xml/TileSets.xml");
	}
	@Override
	public void parse(Document doc) {
		// parse tiles
		if (parsingTileSets == false) {
			NodeList nList = doc.getElementsByTagName("Tile");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Tile tile = new Tile();
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					tile.setName(getTextValue(eElement, "name"));
					tile.setBlocked(getBoolValue(eElement, "blocked"));
					tile.setAirLevel(getIntValue(eElement, "airLevel"));
					tile.setTileColor(Color.decode(getTextValue(eElement, "tileColor")));
				}
				tiles.add(tile);
			}
		}
		// parse tile sets
		else {
			// get tilesets
			NodeList nList = doc.getElementsByTagName("tileSet");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					String id = eElement.getAttribute("id");
					// add new hashmap if needed
//					if (tileSets.get(tileSet) == null) {
//						tileSets.put(tileSet, new HashMap<Color, String>());
//					}
					TileSet set = new TileSet(id);
					if (elementExists(eElement, "base")) {
						set.setBase((getTextValue(eElement, "base")));
					}
					// store each color-item pair in the proper hashmap
					for (int i = 0; i < eElement.getElementsByTagName("item").getLength(); i++) {
						set.getKey().put(Color.decode(getTextAttributeValue(i, eElement, "item")), getTextValue(i, eElement, "item"));
//						tileSets.get(tileSet).put(Color.decode(getTextAttributeValue(i, eElement, "item")), getTextValue(i, eElement, "item"));
					}
					tileSets.add(set);
				}
			}
		}
	}
	public Tile[][] parseBluePrint(Ship ship, int floorNumber, BufferedImage image,
			ShipFloorPlan fp) {
		int w = image.getWidth(null);
		int h = image.getHeight(null);
		Tile[][] blueprint = new Tile[w][h];
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				Color imgColor = new Color(image.getRGB(i, j));
				blueprint[i][j] = getTileFromColor(ship, floorNumber, imgColor, i, j);
				blueprint[i][j].setParentFloorPlan(fp);
			}
		}
		return blueprint;
	}
	// public ArrayList<Tile> getTileSet(String tileSetName) {
	// return tileSets.get(tileSetName);
	// }
	/**
	 * Read's the blueprint of a ship's floorplan and gives the correct tile
	 * 
	 */
	public Tile getTileFromColor(Ship ship, int floorPlan, Color color, int x, int y) {
		// ArrayList<Tile> tiles = getTileSet(ship.getTileSet());
		Tile tile = new Tile();
		int i = 0;
		int imageInt = color.getRGB();
		for (i = 0; i < tiles.size(); i++) {
			int tileInt = tiles.get(i).getTileColor().getRGB();
			if (imageInt == tileInt) {
				tile.cloneTile(tiles.get(i));
				tile.setX(x);
				tile.setY(y);
				tile.setParentFloor(floorPlan);
				tile.connectToProperSystem(ship);
				return tile;
			}
		}
		return tile;
	}
	/**
	 * Return's the base tile of this color
	 * 
	 * @param type
	 * @return
	 */
	public Tile getBaseTileFromColor(Color type) {
		for (int i = 0; i < tiles.size(); i++) {
			int tileInt = tiles.get(i).getTileColor().getRGB();
			int imageInt = type.getRGB();
			if (imageInt == tileInt) {
				return tiles.get(i);
			}
		}
		return null;
	}
	public void parseTileSets(String file) {
	}
	public ArrayList<Tile> getTiles() {
		return tiles;
	}
	public void setTiles(ArrayList<Tile> tiles) {
		this.tiles = tiles;
	}
//	public HashMap<String, HashMap<Color, String>> getTileSets() {
//		return tileSets;
//	}
//	public void setTileSets(HashMap<String, HashMap<Color, String>> tileSets) {
//		this.tileSets = tileSets;
//	}
	/**
	 * Finds and returns the tileset with this id, or null if none found
	 */
	public TileSet findTileSet(String id){
			for (int i=0; i < getTileSets().size(); i++){
				if (getTileSets().get(i).getId().equals(id)){
					return getTileSets().get(i);
				}
			}
		
		return null;
	}
	public ArrayList<TileSet> getTileSets() {
		return tileSets;
	}
	public void setTileSets(ArrayList<TileSet> tileSets) {
		this.tileSets = tileSets;
	}
}
