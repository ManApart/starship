package org.iceburg.home.ui;

import java.awt.Component;
import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPopupMenu;

import org.iceburg.home.ship.Tile;
import org.iceburg.home.ship.systems.ShipSystem;

public class PopUpBox extends JPopupMenu {
	ArrayList<JButton> list;
	Tile tile;

	public PopUpBox(ArrayList<JButton> list) {
		for (int i = 0; i < list.size(); i++) {
			// list.get(i).setForeground(Color.black);
			JButton j = list.get(i);
			add(j);
		}
		// Home.getStationsPanel().getCurrentTilePane().updatePane();
		this.list = list;
		setOpaque(false);
		setBorderPainted(false);
	}
	public static PopUpBox createPopUpBox(Component comp, Point click,
			ArrayList<JButton> list) {
		PopUpBox pop = new PopUpBox(list);
		pop.show(comp, click.x, click.y);
		return pop;
	}
	public ShipSystem getParentSystem() {
		if (tile != null) {
			return tile.getParentSystem();
		}
		return null;
	}
	public Tile getTile() {
		return tile;
	}
	public void setTile(Tile tile) {
		this.tile = tile;
	}
}
