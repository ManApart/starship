package org.iceburg.home.ship;

import java.awt.Color;
import java.util.HashMap;

import org.iceburg.home.main.Home;

public class TileSet {
	String id, base;
	HashMap<Color, String> key;
	boolean assembled;
	
	public TileSet(String id){
		this.id = id;
		this.key = new HashMap<Color, String>();
		this.assembled = false;
	}
	@Override
	public String toString() {
		return getId();
	}
	/**
	 * Assembles this tileset's base tileset (and any of it's bases and so on)
	 * and then ovverrides any of the baseses keys with its own keys
	 */
	public void assembleTileSet(){
		//only run if not already assembled
		if (!assembled){
			//find the base
			TileSet base = Home.resources.getTiles().findTileSet(getBase());
			//if it's not null, assemble the base
			if (base != null){
				base.assembleTileSet();
				HashMap<Color, String> tempKey = new HashMap<Color, String>();
				//store our values in the temp key
				tempKey.putAll(getKey());
				//store all the base tiles in our keyset
				getKey().putAll(base.getKey());
				//now override the base with this tileset
				getKey().putAll(tempKey);
			}
			assembled = true;
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public HashMap<Color, String> getKey() {
		return key;
	}

	private void setKey(HashMap<Color, String> key) {
		this.key = key;
	}
	/**
	 * Get's the id mapped to this color.
	 * If this tileset has not yet been assembled, this method assembles the set
	 */
	public String getItemFromColor(Color c){
		if (!assembled){
			assembleTileSet();
		}
		return getKey().get(c);
		
	}
//	/**
//	 * Set a color to item map
//	 */
//	public void setItemColor(Color c, String item){
//		getKey().put(c, item);
//	}
	
	
	
}
