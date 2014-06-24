package org.iceburg.home.persistance;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.iceburg.home.main.PlayerProfile;
import org.iceburg.home.story.Player;

public class PlayerParser {
//	private static String saveLoc = "c:\\Temp\\";

	/**
	 * Return the path to the saves (currently same folder as holds jar
	 */
	public static String getSavePath() {
		String path = PlayerParser.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String decodedPath = null;
		try {
			// - add a saves folder?
			decodedPath = URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return decodedPath;
	}
	//TODO - make all of these return boolean so we know if save/load was successful.
	/**
	 * Take a player, convert it to a playersave, and write it to the Save.sav
	 * file
	 */
	public static void writePlayerSave(Player p) {
		PlayerSave ps = PlayerSave.serializePlayer(p);
		try {
			FileOutputStream fout = new FileOutputStream(getSavePath() + "Save.sav");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(ps);
		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Read a playersave from the Profile.sav file, convert it to a player
	 * object, and return it
	 */
	public static Player readPlayerSave() {
		PlayerSave ps = null;
		try {
			FileInputStream fin = new FileInputStream(getSavePath() + "Save.sav");
			ObjectInputStream ois = new ObjectInputStream(fin);
			ps = (PlayerSave) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return PlayerSave.deserializePlayer(ps);
	}
	/**
	 * Write a player profile to the Profile.sav file
	 */
	public static void writePlayerProfile(PlayerProfile p) {
		try {
			FileOutputStream fout = new FileOutputStream(getSavePath() + "Profile.sav");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(p);
		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Read a player profile from the Profile.sav file
	 */
	public static PlayerProfile readPlayerProfile() {
		PlayerProfile p = null;
		try {
			FileInputStream fin = new FileInputStream(getSavePath() + "Profile.sav");
			ObjectInputStream ois = new ObjectInputStream(fin);
			p = (PlayerProfile) ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return p;
	}
}
