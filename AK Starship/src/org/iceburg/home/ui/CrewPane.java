package org.iceburg.home.ui;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.main.Constants;
import org.iceburg.home.main.Home;
import org.iceburg.home.ship.systems.Sensors;

public class CrewPane extends JPanel {
	public CrewMan man;
	private boolean multi;

	/**
	 * Create's a panel with the current crewman's info
	 */
	public void populateCrewPane(CrewMan man, boolean multi) {
		this.man = man;
		this.multi = multi;
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBackground(Constants.colorBackground);
		setOpaque(true);
		setBounds(0, 0, GameScreen.borderSide - 30, 50);
		if (man != null && man.getHealthCurrent() > 0) {
			UIMisc.addJLabel(man.getName(), this, Home.gameFont);
			if (man.isPlayerControlled()) {
				UIMisc.addJLabel(man.getDivisionTitle(), this);
			} else {
				UIMisc.addJLabel("Enemy", this);
			}
			if (man.isPlayerControlled()
					|| man.getParentShip().getSensors().getMaxSensorLevel() >= Sensors.crewHealth) {
				UIMisc.addHealthBar(man, this);
				UIMisc.addBreathBar(man, this);
			}
			if (man.isPlayerControlled() && man.getItem() != null) {
				UIMisc.addJLabel("Holding: " + man.getItem().getName(), this);
			}
			if (man.isPlayerControlled() && multi == false) {
				UIMisc.addJLabel("Manning: " + man.getMannedStationName(), this);
				UIMisc.addJLabel("", this);
				// Skills
				UIMisc.addJLabel(man.getName() + "'s Skills: ", this, Home.gameFont);
				UIMisc.addJLabel("Analysis: " + man.getSkillLVL(man.getAnalysis()), this);
				UIMisc.addJLabel("Energy Fields: "
						+ man.getSkillLVL(man.getEnergyFields()), this);
				UIMisc.addJLabel("Navigation: " + man.getSkillLVL(man.getNavigation()), this);
				UIMisc.addJLabel("Power Distribution: "
						+ man.getSkillLVL(man.getPowerDistribution()), this);
			}
			// add some space for tile panel underneath - there should be a
			// better way to do this
			UIMisc.addJLabel("", this, Home.gameFont);
			UIMisc.addJLabel("", this, Home.gameFont);
		}
		updateUI();
	}
	public static void createPanel() {
		GameScreen parent = Home.getCurrentScreen();
		parent.leftPanel.setLayout(new BoxLayout(parent.leftPanel, BoxLayout.PAGE_AXIS));
		boolean setup = false;
		if (parent.leftScroll != null) {
			parent.remove(parent.leftScroll);
			parent.leftScroll = null;
		}
		parent.leftPanel.removeAll();
		parent.currentCrewPane.removeAll();
		if (parent.getCurrentCrewMan() != null) {
			// current crew pane
			parent.currentCrewPane.populateCrewPane(parent.getCurrentCrewMan(), false);
			parent.currentCrewPane.addMouseListener(parent);
			parent.getActiveCrew().clear();
			parent.leftPanel.add(parent.currentCrewPane);
			parent.getActiveCrew().add(parent.getCurrentCrewPane());
			setup = true;
			// player ship view
			if (parent.getCurrentShip() == Home.getShip()) {
				if (parent.getCurrentCrewMan() != null) {
					ArrayList<CrewMan> list = parent.getCurrentCrewMan().getParentShip().getCrew();
					for (int i = 0; i < list.size(); i++) {
						if (list.get(i) != parent.getCurrentCrewMan()) {
							CrewPane loopPanel = new CrewPane();
							loopPanel.populateCrewPane(list.get(i), true);
							loopPanel.addMouseListener(parent);
							parent.leftPanel.add(loopPanel);
							parent.getActiveCrew().add(loopPanel);
							// System.out.println("w="+ loopPanel.getWidth() +
							// " h=" + loopPanel.getHeight());
						}
					}
				}
			}
			parent.leftPanel.setOpaque(true);
			parent.leftPanel.setBackground(Constants.colorBackground);
			parent.leftPanel.add(Box.createVerticalGlue());
			// set up panel
			if (setup) {
				parent.leftPanel.setOpaque(true);
				parent.leftScroll = new JScrollPane(parent.leftPanel);
				parent.leftScroll.setBounds(10, 50, parent.borderSide - 15, Constants.viewScreenSize);
				parent.leftScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
				parent.leftScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				parent.leftScroll.getVerticalScrollBar().setUnitIncrement(15);
				parent.leftScroll.getVerticalScrollBar().setOpaque(false);
				parent.leftScroll.setBackground(Color.red);
				parent.leftScroll.setOpaque(false);
				parent.leftScroll.setBorder(null);
				parent.leftScroll.getVerticalScrollBar().setValue(HEIGHT);
				parent.leftScroll.add(Box.createVerticalGlue());
				parent.add(parent.leftScroll);
				parent.currentCrewPane.updateUI();
			}
		}
	}
	// TODO
	public void updatePane() {
		removeAll();
		if (getMan() != null) {
			populateCrewPane(getMan(), !(Home.getCurrentScreen().currentCrewPane == this));
		}
	}
	/**
	 * Searches the active crew list. If the list contains the input man it
	 * returns the pane that contains the man
	 */
	public static CrewPane containsMan(CrewMan man) {
		GameScreen parent = Home.getCurrentScreen();
		ArrayList<CrewPane> activeCrew = parent.getActiveCrew();
		for (int i = 0; i < activeCrew.size(); i++) {
			CrewPane pane = activeCrew.get(i);
			if (pane.getMan() == man) {
				return pane;
			}
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
	public void updateProgressBar(String text, CrewMan man) {
		JLabel l = findLabel(text, this);
		if (l != null) {
			UIMisc.updateProgressBar(text, man, null, l);
		}
	}
	/**
	 * @return's the label in this panel that matches labeltext, or null;
	 */
	public static JLabel findLabel(String labelText, CrewPane panel) {
		for (int i = 0; i < panel.getComponentCount(); i++) {
			Component comp = panel.getComponent(i);
			if (comp instanceof JLabel) {
				String s = ((JLabel) comp).getText();
				if (s.contains(labelText)) {
					return ((JLabel) comp);
				}
			}
		}
		return null;
	}
	public CrewMan getMan() {
		return man;
	}
	public void setMan(CrewMan man) {
		this.man = man;
	}
	public boolean isMulti() {
		return multi;
	}
	public void setMulti(boolean multi) {
		this.multi = multi;
	}
}
