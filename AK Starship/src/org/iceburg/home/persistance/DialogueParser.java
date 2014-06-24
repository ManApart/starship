package org.iceburg.home.persistance;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.iceburg.home.main.Home;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DialogueParser extends Parser {
	public static HashMap<Integer, ArrayList<String>> parseDialogue() {
		// System.out.println("Parsing Dialogue");
		HashMap<Integer, ArrayList<String>> map = new HashMap<Integer, ArrayList<String>>();
		InputStream starFile = Home.resources.getClass().getResourceAsStream("xml/Dialogue.xml");
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(starFile);
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("type");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					int type = getIntAttributeValue(eElement, "id");
					// if the array is null for this type, add it
					if (map.get(type) == null) {
						map.put(type, new ArrayList<String>());
					}
					Element eBool = (Element) eElement.getElementsByTagName("yes").item(0);
					for (int i = 0; i < eBool.getElementsByTagName("line").getLength(); i++) {
						map.get(type).add(getTextValue(i, eBool, "line"));
					}
					// now do the no list for this element
					type += 1;
					// if the array is null for this type, add it
					if (map.get(type) == null) {
						map.put(type, new ArrayList<String>());
					}
					eBool = (Element) eElement.getElementsByTagName("no").item(0);
					for (int i = 0; i < eBool.getElementsByTagName("line").getLength(); i++) {
						map.get(type).add(getTextValue(i, eBool, "line"));
					}
				}
			}
			// System.out.println(nList.getLength() +
			// " lines of dialogue processed");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error parsing xml/Dialogue.xml");
		} finally {
			return map;
		}
	}
}
