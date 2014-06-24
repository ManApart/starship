package org.iceburg.home.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.iceburg.home.items.Item;
import org.iceburg.home.main.Constants;
import org.iceburg.home.main.Home;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.ship.systems.Sensors;

public class TilePane extends JPanel implements ChangeListener {
	public TilePane() {
	}

	public Tile tile;
	public JSlider jsPower;

	// TODO add system / popup buttons to current tile
	// repeat for crew
	public void populateTilePane(Tile tile, boolean multi) {
		this.tile = tile;
		// get our sensor level
		int sensorLevel = tile.getParentShip().getSensors().getMaxSensorLevel();
		// and see what level of detail we'll be displaying
		boolean displayName = (tile.isBuildTile() && sensorLevel >= Sensors.buildName)
				|| (tile.isSystemTile() && sensorLevel >= Sensors.systemName);
		boolean displayHealth = (tile.isBuildTile() && sensorLevel >= Sensors.buildHealth)
				|| (tile.isSystemTile() && sensorLevel >= Sensors.systemHealth);
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBackground(Constants.colorBackground);
		setOpaque(true);
		setBounds(0, 0, GameScreen.borderSide - 30, 500);
		Item item = tile.getItem();
		this.removeAll();
		if (item != null) {
			if (displayName) {
				UIMisc.addJLabel(item.getName(), this);
				if (displayHealth) {
					UIMisc.addTileHealthBar(tile, null, this);
					if (tile.hasParentSystem()
							&& (tile.getParentShip() == Home.getShip() || displayHealth)) {
						if (multi == false) {
							tile.getParentSystem().paintTileInfo(this, item);
						} else {
							tile.getParentSystem().paintTileMultiInfo(this, item);
						}
					}
				}
			} else {
				UIMisc.addJLabel("Unknown part", this);
			}
			// only add power level if this item uses power
			if (displayName && item.getMaxPower() > 0) {
				// if system is manned, give detailed info, otherwise give
				// simple power amount
				if (tile.getParentSystem().isManned()) {
					jsPower = UIMisc.addSlider("Power Level", this, item.getMaxPower(), item.getPowerCurrent());
				} else {
					String s = "Not Powered: ";
					if (tile.isPowered()) {
						s = "Powered: ";
					}
					UIMisc.addJLabel(s + item.getPowerCurrent() + "/"
							+ item.getMaxPower(), this);
				}
			}
			if (displayHealth && multi == false && tile.isFriendly()) {
				// add buttons
				ArrayList<JButton> buttons = Home.getCurrentScreen().createGeneralOptions(tile);
				if (tile.hasParentSystem()
						&& (Home.creativeMode || tile.getParentShip() == Home.getShip())) {
					tile.getParentSystem().createSystemOptions(Home.getCurrentScreen(), tile, buttons);
				}
				// add the buttons (don't add cancel, the last button)
				for (int i = 0; i < buttons.size() - 1; i++) {
					add(buttons.get(i));
				}
				UIMisc.addJLabel("", this);
				UIMisc.addJLabel(item.getDescription(), this);
				UIMisc.addJLabel("", this);
			}
			UIMisc.addJLabel("", this);
		}
	}
	// Let's be straight, I have no idea about these layout managers
	// I need to take a swing class, and I don't mean dancing...
	public static void createPanel() {
		GameScreen parent = Home.getCurrentScreen();
		parent.rightPanel.setLayout(new BoxLayout(parent.rightPanel, BoxLayout.PAGE_AXIS));
		boolean setup = false;
		if (parent.rightScroll != null) {
			parent.remove(parent.rightScroll);
			parent.rightScroll = null;
		}
		parent.rightPanel.removeAll();
		parent.currentTilePane.removeAll();
		if (parent.currentTile != null) {
			// current tile pane
			parent.currentTilePane.populateTilePane(parent.getCurrentTile(), false);
			parent.currentTilePane.addMouseListener(parent);
			// parent.currentTilePane.setOpaque(true);
			// parent.currentTilePane.setBackground(Color.red);
			parent.getActiveTiles().clear();
			parent.rightPanel.add(parent.currentTilePane);
			parent.getActiveTiles().add(parent.currentTilePane);
			setup = true;
			// player ship view
//			if (parent.getCurrentShip() == Home.getShip()
//					&& parent.currentTile.getParentShip() == parent.getCurrentShip()) {
			if (parent.getCurrentTile().getParentShip() == Home.getShip()){
				if (parent.getCurrentTile() != null
						&& parent.getCurrentTile().hasParentSystem()) {
					ArrayList<Tile> list = parent.getCurrentTile().getParentSystem().getStations();
					for (int i = 0; i < list.size(); i++) {
						if (list.get(i) != parent.currentTile) {
							TilePane loopPanel = new TilePane();
							loopPanel.populateTilePane(list.get(i), true);
							loopPanel.addMouseListener(parent);
							parent.rightPanel.add(loopPanel);
							parent.getActiveTiles().add(loopPanel);
							// System.out.println("w="+ loopPanel.getWidth() +
							// " h=" + loopPanel.getHeight());
						}
					}
				}
			}
			// parent.rightPanel.setBounds(parent.borderSide +
			// Constants.viewScreenSize + 10, 50, parent.borderSide-15,
			// Constants.viewScreenSize);
			parent.rightPanel.setOpaque(true);
			parent.rightPanel.setBackground(Constants.colorBackground);
			// parent.rightPanel.add(Box.createVerticalGlue());
			// set up panel
			if (setup) {
				// rightPanel.setBackground(Color.red);
				parent.rightPanel.setOpaque(true);
				// rightPanel.updateUI();
				FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
				fl.setHgap(0);
				fl.setVgap(0);
				JPanel tempPane = new JPanel(fl);
				// tempPane.setBackground(Color.blue);
				tempPane.setBackground(Constants.colorBackground);
				tempPane.setOpaque(true);
				tempPane.add(parent.rightPanel);
				parent.rightScroll = new JScrollPane(tempPane);
				// parent.rightScroll = new JScrollPane(parent.rightPanel);
				parent.rightScroll.setBounds(parent.borderSide + Constants.viewScreenSize
						+ 10, 50, parent.getWidth()
						- (parent.borderSide + Constants.viewScreenSize + 15), Constants.viewScreenSize);
				// parent.rightScroll.setBounds(parent.borderSide +
				// Constants.viewScreenSize + 10, 50, parent.borderSide-15,
				// Constants.viewScreenSize);
				parent.rightScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
				parent.rightScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				parent.rightScroll.getVerticalScrollBar().setUnitIncrement(15);
				parent.rightScroll.getVerticalScrollBar().setOpaque(false);
				parent.rightScroll.setBackground(Color.red);
				parent.rightScroll.setOpaque(false);
				parent.rightScroll.setBorder(null);
				parent.rightScroll.getVerticalScrollBar().setValue(HEIGHT);
				// parent.rightScroll.add(Box.createVerticalGlue());
				// parent.rightScroll.add(Box.createHorizontalGlue());
				parent.add(parent.rightScroll);
				parent.currentTilePane.updateUI();
			}
		}
	}
	public synchronized void updatePane() {
		if (getTile() != null) {
			populateTilePane(getTile(), !(Home.getCurrentScreen().currentTilePane == this));
			updateUI();
		}
	}
	/**
	 * Searches the active tiles list. If the list contains the input tile
	 * (either as a tile, or a weapon tile's targettile) it returns the pane
	 * that contains the tile
	 */
	public static TilePane containsTile(Tile tile) {
		GameScreen parent = Home.getCurrentScreen();
		if (parent != null && parent.getActiveTiles() != null) {
			ArrayList<TilePane> activeTiles = parent.getActiveTiles();
			for (int i = 0; i < activeTiles.size(); i++) {
				TilePane pane = activeTiles.get(i);
				if (pane != null
						&& (pane.getTile() == tile || (pane.getTile().isWeapon() && pane.getTile().getWeapon().getTargetTile() == tile))) {
					return pane;
				}
			}
			return null;
		}
		return null;
	}
	/**
	 * Update's a panel by finding the label text and replacing it with the
	 * newtext
	 */
	public void updatePanel(String labelText, String newText) {
		JLabel l = findLabel(labelText, this);
		if (l != null) {
			l.setText(newText);
		}
	}
	public void updateProgressBar(String text, Tile tile) {
		JLabel l = findLabel(text, this);
		if (l != null) {
			UIMisc.updateProgressBar(text, null, tile, l);
		}
	}
	/**
	 * @return's the label in this panel that matches labeltext, or null;
	 */
	public static JLabel findLabel(String labelText, TilePane panel) {
		for (int i = 0; i < panel.getComponentCount(); i++) {
			Component comp = panel.getComponent(i);
			if (comp instanceof JLabel) {
				String s = ((JLabel) comp).getText();
				if (s.contains(labelText)) {
					// if (s.contains(labelText)){
					return ((JLabel) comp);
				}
			}
		}
		return null;
	}
	public Tile getTile() {
		return tile;
	}
	public void setTile(Tile tile) {
		this.tile = tile;
	}
	/**
	 * Return
	 * 
	 * @return
	 */
	public static TilePane getFirstActiveTilePane() {
		if (Home.getCurrentScreen().getActiveTiles().size() > 0) {
			return Home.getCurrentScreen().getActiveTiles().get(0);
		}
		return null;
	}
	public static Tile getFirstActiveTile() {
		if (getFirstActiveTilePane() != null) {
			return getFirstActiveTilePane().getTile();
		}
		return null;
	}
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() instanceof JSlider) {
			// System.out.println("Tilepane recieved state slider change");
			JSlider o = (JSlider) e.getSource();
			// only change when the player is done setting the value
			if (!o.getValueIsAdjusting()) {
				if (jsPower != null && o == jsPower) {
					// System.out.println("power");
					tile.getParentShip().getEngineering().attemptPowerSystem(tile.getItem(), o.getValue());
					// o.setValue(1);
					// o.setValue(tile.getItem().getCurrentPower());
				} else {
					tile.getParentSystem().stateChanged(o, tile);
				}
				updatePane();
			}
		} else if (e.getSource() instanceof JSpinner) {
			JSpinner o = (JSpinner) e.getSource();
			tile.getParentSystem().stateChanged(o, tile);
		}
	}
	// TODO - java button presses to use this listener as well?
	// @Override
	// public void actionPerformed(ActionEvent e) {
	// if (e.getSource() instanceof JTextField){
	// JTextField o =(JTextField) e.getSource();
	// tile.getParentSystem().textChanged(o, tile);
	// }
	//
	// }
}
