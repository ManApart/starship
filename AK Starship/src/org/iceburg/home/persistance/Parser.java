package org.iceburg.home.persistance;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.iceburg.home.main.Home;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Parser {
	public Parser() {
	}
	static boolean elementExists(Element eElement, String tagName) {
		return eElement.getElementsByTagName(tagName).getLength() > 0;
	}
	/**
	 * Does this element have an attribute?
	 */
	static boolean hasAttribute(Element eElement) {
		return eElement.getAttributes().getLength() > 0;
	}
	/**
	 * Does this element have an attribute with this tagname?
	 */
	static boolean attributeExists(Element eElement, String tagName) {
		String s = eElement.getAttribute(tagName);
		return s != null && s != "";
		// for (int i=0; i< eElement.getAttributes().getLength(); i++){
		//
		// }
		// return false;
	}
	static String getTextValue(Element eElement, String tagName) {
		return eElement.getElementsByTagName(tagName).item(0).getTextContent();
	}
	static String getTextValue(int i, Element eElement, String tagName) {
		return eElement.getElementsByTagName(tagName).item(i).getTextContent();
	}
	static String getTextAttributeValue(int i, Element eElement, String tagName) {
		return ((Element) eElement.getElementsByTagName(tagName).item(i)).getAttribute("id");
	}
	static String getTextAttributeValue(Element ele, String tagName) {
		return ele.getAttribute(tagName);
	}
	static int getIntValue(Element ele, String tagName) {
		return Integer.parseInt(getTextValue(ele, tagName));
	}
	static int getIntAttributeValue(Element ele, String tagName) {
		return Integer.parseInt(ele.getAttribute(tagName));
	}
	static boolean getBoolValue(Element ele, String tagName) {
		return Boolean.parseBoolean(getTextValue(ele, tagName));
	}
	static boolean getBoolAttributeValue(Element ele, String tagName) {
		return Boolean.parseBoolean(getTextAttributeValue(ele, tagName));
	}
	static Double getDoubleValue(Element ele, String tagName) {
		// in production application you would catch the exception
		return Double.parseDouble(getTextValue(ele, tagName));
	}
	public void parseFile(String file) {
		InputStream is = Home.resources.getClass().getResourceAsStream(file);
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			parse(doc);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error parsing " + file);
		}
	}
	/**
	 * This guy get's overridden by the other parsers
	 */
	public void parse(Document doc) {
	}
}
