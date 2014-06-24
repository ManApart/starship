package org.iceburg.home.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.main.Constants;
import org.iceburg.home.main.Home;
import org.iceburg.home.main.StaticFunctions;
import org.iceburg.home.persistance.DialogueParser;

public class MessageCenter extends JPanel {
	// the bank of strings to pull messages from
	HashMap<Integer, ArrayList<String>> stringMap;
	// the messages in the message center
	ArrayList<JLabel> messages;
	int height, scrollHeight;
	static int heightInc = 14;
	// Static message types
	/**
	 * Question: Can do?
	 */
	public static int typeDefaultResponse = 0;
	/**
	 * Question: System has parent? Strings: tileName, parentSystemName
	 */
	public static int typeEngineerTileType = 2;
	/**
	 * Question: Tile is targetable Strings: tileName
	 */
	public static int typeWeaponsTargetable = 4;
	/**
	 * Question: Weapon is equipped Strings: tileName, weaponName
	 */
	public static int typeEquipWeapon = 6;
	/**
	 * Question: Get info about door Strings: tileName; bool: isblocked
	 */
	public static int typeSecurityDoorInfo = 8;
	/**
	 * Question: Close/open a door Strings: tileName; bool: isblocked now
	 */
	public static int typeSecurityDoorSetClose = 10;
	/**
	 * Can travel to tile: tileName, bool: success?
	 */
	public static int typeCanTravel = 12;

	public MessageCenter() {
		this.messages = new ArrayList<JLabel>();
		this.stringMap = DialogueParser.parseDialogue();
		// this.stringMap = new HashMap<Integer, ArrayList<String>>();
		this.setPreferredSize(new Dimension(Constants.messageCenterArea.width, Constants.windowSize.height));
		// TODO - set layout so that labels don't squash in the middle
		// TODO - parse XML messages
	}
	/**
	 * Creates a message center properly formatted to the JScrollPane input
	 * 
	 * @param scroll
	 */
	public MessageCenter(JScrollPane scroll) {
		this.messages = new ArrayList<JLabel>();
		this.stringMap = DialogueParser.parseDialogue();
		// this.stringMap = new HashMap<Integer, ArrayList<String>>();
		this.setPreferredSize(new Dimension(Constants.messageCenterArea.width, Constants.windowSize.height));
		// TODO - set layout so that labels don't squash in the middle
		this.setBackground(Color.black);
		this.setLayout(null);
		scroll = new JScrollPane(this);
		scroll.setBounds(Constants.messageCenterArea);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		Home.setMessageScroll(scroll);
	}
	// TODO - this function is useless at the moment...
	public void refreshMessageCenter() {
		// clear the components
		this.removeAll();
		for (int i = 0; i < messages.size(); i++) {
			// messages.get(i)
		}
	}
	// TODO - double check that this works
	public void removeMessages(int amount) {
		this.removeAll();
		if (messages.size() > 0) {
			while (amount > 0) {
				messages.remove(0);
				amount -= 1;
			}
			// move the remaining messages up
			height = 0;
			for (int i = 0; i < messages.size(); i++) {
				messages.get(i).setBounds(10, height, Constants.messageCenterArea.width, 20);
				add(messages.get(i));
				height += heightInc;
			}
			scrollHeight = heightInc + height - Constants.messageCenterArea.height;
		}
	}
	public String wordWrap(String s) {
		StringBuilder sb = new StringBuilder(s);
		int i = 0;
		while (i + 20 < sb.length() && (i = sb.lastIndexOf(" ", i + 20)) != -1) {
			sb.replace(i, i + 1, "\n");
		}
		return sb.toString();
	}
	public int getStringHeight(JLabel m) {
		int width = m.getFontMetrics(getFont()).stringWidth(m.getText());
		int lines = 1 + width / (Constants.messageCenterArea.width);
		return lines * 14;
	}
	/**
	 * Adds the simplest form of message
	 * 
	 * @param s
	 */
	public void addMessage(String s) {
		if (messages.size() + 1 > getHeight() / heightInc) {
			removeMessages(1);
		}
		JLabel message = new JLabel(s);
		message.setForeground(Color.white);
		message.setBounds(10, height, Constants.messageCenterArea.width - 50, getStringHeight(message));
		// message.setBounds(10, height, Constants.messageCenterArea.width- 50,
		// message.getPreferredSize().height);
		messages.add(message);
		add(message);
		// height += heightInc;
		height += message.getHeight();
		// TODO only update scroll position when screen full
		if (height - scrollHeight >= Constants.messageCenterArea.height) {
			scrollHeight = heightInc + height - Constants.messageCenterArea.height;
		}
		Home.getMessageScroll().getVerticalScrollBar().setValue(scrollHeight);
	}
	public static String colorString(String s, Color c) {
		String colHex = "color=\'" + "#" + Integer.toHexString(c.getRGB()).substring(2)
				+ "\'>";
		return "<html><font " + colHex + s + "</font></html>";
	}
	/**
	 * Add's a message from a crewman or the system
	 */
	public void addMessage(CrewMan man, String s) {
		// make sure that we don't display null strings, or strings from enemies
		if (s != null && !s.equals("")
				&& (man == null || (man != null && man.isPlayerControlled()))) {
			String retString = "";
			if (man == null) {
				retString = "System: " + s;
			} else if (man.isPlayerControlled()) {
				Color col = man.getDivision();
				String colHex = "color=\'" + "#"
						+ Integer.toHexString(col.getRGB()).substring(2) + "\'>";
				retString = "<html><font " + colHex + man.getDivisionTitle() + " "
						+ man.getName() + ": " + "</font>" + s + "</html>";
			}
			addMessage(retString);
		}
	}
	/**
	 * Adds a message based on the message type does not take special variables
	 * 
	 * @param man
	 *            - crewman speaker
	 * @param type
	 *            - type of message (choose from statics)
	 */
	public void addMessage(CrewMan man, int type) {
		String retString = getRandStringFromData(type);
		addMessage(man, retString);
	}
	/**
	 * Adds a positive or negative message based on the message type
	 * 
	 * @param man
	 *            - crewman speaker
	 * @param type
	 *            - type of message (choose from statics)
	 * @param bool
	 *            - the bool to accompany this message
	 */
	public void addMessage(CrewMan man, int type, boolean bool) {
		// negative responses are 1 higher in the hashmap
		if (bool == false) {
			type += 1;
		}
		addMessage(man, type);
	}
	/**
	 * Adds a positive or negative message based on the message type +
	 * incorporates 1 string variable
	 */
	public void addMessage(CrewMan man, int type, boolean bool, String varString1) {
		// negative responses are 1 higher in the hashmap
		if (bool == false) {
			type += 1;
		}
		String retString = feedVariables(getRandStringFromData(type), varString1);
		addMessage(man, retString);
	}
	/**
	 * Adds a positive or negative message based on the message type +
	 * incorporates 1 string variable
	 */
	public void addMessage(CrewMan man, int type, boolean bool, String varString1,
			String varString2) {
		// negative responses are 1 higher in the hashmap
		if (bool == false) {
			type += 1;
		}
		String retString = feedVariables(getRandStringFromData(type), varString1, varString2);
		addMessage(man, retString);
	}
	/**
	 * Returns a random string from the proper situation/type list
	 * 
	 * @param type
	 *            - the EXACT type of situation you want to pull from (Boolean
	 *            should have already been resolved)
	 * @return
	 */
	public String getRandStringFromData(int type) {
		ArrayList<String> list = stringMap.get(type);
		return list.get(StaticFunctions.randRange(0, list.size() - 1));
	}
	/**
	 * Takes a pre written text and replaces vars in the message text
	 * 
	 * @param message
	 *            - the input message
	 * @param varBool1
	 *            -3 -up to three bool variables to replace the text with
	 * @param varInt1
	 *            -3 -up to three int variables to replace the text with
	 * @param varString1
	 *            -3 -up to three string variables to replace the text with
	 * @return
	 */
	public String feedVariables(String message, boolean varBool1, boolean varBool2,
			boolean varBool3, int varInt1, int varInt2, int varInt3, String varString1,
			String varString2, String varString3) {
		// replace the variables
		message = replaceBool(message, "varBool1", varBool1);
		message = replaceBool(message, "varBool2", varBool2);
		message = replaceBool(message, "varBool3", varBool3);
		message = replaceInt(message, "varInt1", varInt1);
		message = replaceInt(message, "varInt2", varInt2);
		message = replaceInt(message, "varInt3", varInt3);
		message = replaceString(message, "varString1", varString1);
		message = replaceString(message, "varString2", varString2);
		message = replaceString(message, "varString3", varString3);
		return message;
	}
	// overloading for 1 bool messages
	public String feedVariables(String message, boolean varBool1) {
		return feedVariables(message, varBool1, false, false, 0, 0, 0, "", "", "");
	}
	// overloading for 1 String messages
	public String feedVariables(String message, String varString1) {
		return feedVariables(message, false, false, false, 0, 0, 0, varString1, "", "");
	}
	// overloading for 2 String messages
	public String feedVariables(String message, String varString1, String varString2) {
		return feedVariables(message, false, false, false, 0, 0, 0, varString1, varString2, "");
	}
	public String replaceBool(String s, String search, boolean varBool) {
		String repString = "false";
		if (varBool) {
			repString = "true";
		}
		return s.replace(search, repString);
	}
	public String replaceInt(String s, String search, int varInt) {
		String repString = "" + varInt;
		return s.replace(search, repString);
	}
	public String replaceString(String s, String search, String varString) {
		if (varString == null) {
			varString = "";
		}
		String retS = s.replace(search, varString);
		return retS;
	}
	
}
