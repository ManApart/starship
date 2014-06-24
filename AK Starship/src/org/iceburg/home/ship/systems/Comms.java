package org.iceburg.home.ship.systems;

import java.awt.Color;

import org.iceburg.home.items.Item;
import org.iceburg.home.main.Home;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.story.QuestEvent;
import org.iceburg.home.ui.MessageCenter;
import org.iceburg.home.ui.TilePane;
import org.iceburg.home.ui.UIMisc;

public class Comms extends ShipSystem {
	public static Color systemMain = Color.decode("#d55c76");

	public Comms(Ship ship) {
		super(ship);
		this.tileTypes.add(systemMain);
		this.name = "Comms";
	}
	@Override
	public void paintTileInfo(TilePane p, Item item) {
		if (Home.getBattleLoc() != null){
			UIMisc.addJLabel("Current: " + Home.getBattleLoc(), p);
		}
		if (Home.getPlayer().getQuest() != null){
		QuestEvent qe = Home.getPlayer().getQuest().getCurrentEvent();
			if (qe.hasDistance()) {
				if (qe.getLoc() != null){
					UIMisc.addJLabel("Next Up: " + qe.getLoc(), p);
				}
				UIMisc.addJLabel("Distance: " + qe.getDistanceDisplay(), p);
				if (qe.hasTime()) {
					UIMisc.addJLabel("Time left: " + timeToString(qe.getTime()), p);
				}
			}
			if (Home.getPlayer().getQuest().getCurrentEvent().getDistance() < 0
					&& Home.getPlayer().getQuest().getCurrentEvent().isEventSuccess()) {
				UIMisc.addJLabel(MessageCenter.colorString("Arrived in Time", Color.GREEN), p);
			}
		}
	}
	/**
	 * Translate's a time int to a string
	 */
	public static String timeToString(long time) {
		time = time / 10;
		int min = (int) (Math.abs(time / 60));
		int sec = (int) (Math.abs(time % 60));
		String sign = "";
		if (time < 0) {
			sign = "-";
		}
		String s = "" + sec;
		if (sec < 10) {
			s = "0" + sec;
		}
		return sign + min + ":" + s;
	}
	/**
	 * Translates a string in the form of minutes:seconds into a long var (total
	 * # of seconds) example: "1:33" would give a long of value of 93
	 * 
	 */
	public static long parseTime(String s) {
		int i = s.indexOf(":");
		int min = 0;
		int sec = 0;
		// if we have a number before the colon
		if (i != -1) {
			String mins = s.substring(0, i);
			min = Integer.parseInt(mins);
		}
		String secs = s.substring(i + 1, s.length());
		// if we have data after the colon
		if (secs.length() > 0) {
			sec = Integer.parseInt(secs);
		}
		return (min * 60 + sec) * 10;
	}
}
