package org.iceburg.home.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.items.Shield;
import org.iceburg.home.items.Weapon;
import org.iceburg.home.main.Constants;
import org.iceburg.home.main.Home;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.ship.systems.Shields;

//Class handles many miscillanious UI functions,
//Includes JSliders, Textfields, labvels and progress bars
public class UIMisc {
	public static String health = "Health: ", breath = "Breath: ", hit = "Hit: ",
			energy = "Energy: ", loadTime = "Loading: ";

	// TODO - Sliders section
	/**
	 * Create's a JSlider and adds it to the tilepane
	 */
	public static JSlider addSlider(String text, TilePane p, int max, int start) {
		JSlider l = createSlider(text, p, max, start);
		l.setMaximumSize(new Dimension(p.getWidth(), p.getHeight()));
		// add the label
		addJLabel(text, p);
		p.add(l);
		return l;
	}
	public static JSlider createSlider(String text, ChangeListener listener, int max,
			int start) {
		if (max < start){
			max = start;
		}
		JSlider l = new JSlider(JSlider.HORIZONTAL, 0, max, start);
		l.setMajorTickSpacing(Math.max(max / 5, 1));
		l.setMinorTickSpacing(max / 10);
		l.setPaintTicks(true);
		l.setPaintLabels(true);
		l.setFont(Home.gameFontSub);
		l.setForeground(Color.white);
		l.setSnapToTicks(true);
		l.setOpaque(false);
		l.addChangeListener(listener);
		l.setAlignmentX(Component.LEFT_ALIGNMENT);
		return l;
	}
	// TODO - Spinner section
	/**
	 * Create's a JSlider and adds it to the tilepane
	 */
	public static JSpinner addSpinner(String text, int start, TilePane p) {
		JSpinner l = createSpinner(p, start);
		l.setMaximumSize(new Dimension(p.getWidth() / 2, p.getHeight()));
		// add the label
		addJLabel(text, p);
		p.add(l);
		return l;
	}
	public static JSpinner createSpinner(ChangeListener listener, int start) {
		JSpinner l = new JSpinner();
		JTextField tf = ((JSpinner.DefaultEditor) l.getEditor()).getTextField();
		tf.setDisabledTextColor(Color.red);
		tf.setBackground(Constants.colorBackground);
		tf.setForeground(Color.white);
		tf.setBorder(null);
		tf.setHorizontalAlignment(JTextField.LEFT);
		l.setBorder(null);
		l.setFont(Home.gameFontSub);
		l.setValue(start);
		l.setOpaque(true);
		l.addChangeListener(listener);
		l.setAlignmentX(Component.LEFT_ALIGNMENT);
		return l;
	}
	// TODO - TextFields section
	/**
	 * Create's a JTextField and adds it to the tilepane
	 */
	public static JTextField addTextField(String labelText, String inputText, TilePane p) {
		// TODO - not sure if right listener?
		JTextField l = createTextField(inputText, Home.getCurrentScreen());
		l.setMaximumSize(new Dimension(p.getWidth(), p.getHeight()));
		// add the label
		addJLabel(labelText, p);
		p.add(l);
		return l;
	}
	public static JTextField createTextField(String inputText, ActionListener listener) {
		JTextField l = new JTextField(inputText);
		l.setFont(Home.gameFontSub);
		l.setBackground(Constants.colorBackground);
		l.setForeground(Color.white);
		l.setBorder(null);
		l.addActionListener(listener);
		// l.getDocument().addDocumentListener(listener);
		l.setAlignmentX(Component.LEFT_ALIGNMENT);
		return l;
	}
	// TODO - Progress Bars section
	/**
	 * Updates breath, health, tilehealth, or target tile health based on the
	 * text input
	 */
	public static void updateProgressBar(String text, CrewMan man, Tile tile, JLabel old) {
		if (man != null) {
			if (text.equals(health)) {
				updateHealthBar(man, old);
			} else if (text.equals(breath)) {
				updateBreathBar(man, old);
			}
		} else {
			if (text.equals(health)) {
				updateTileHealthBar(tile, null, old);
			} else if (text.equals(hit)) {
				updateTileHealthBar(tile, hit, old);
			} else if (text.equals(energy)) {
				updateEnergyBar(tile, energy, old);
			} else if (text.equals(loadTime)) {
				updateLoadBar(tile.getWeapon(), loadTime, old);
			}
		}
	}
	public static void addHealthBar(CrewMan man, JPanel parent) {
		Color currentCol = Color.green.darker();
		Color totalCol = Color.red.darker();
		JLabel l = createProgressBar(man.getHealthCurrent(), man.getHealthTotal(), health, currentCol, totalCol);
		parent.add(l);
	}
	public static void updateHealthBar(CrewMan man, JLabel old) {
		Color currentCol = Color.green.darker();
		Color totalCol = Color.red.darker();
		JLabel l = createProgressBar(man.getHealthCurrent(), man.getHealthTotal(), health, currentCol, totalCol);
		old.setIcon(l.getIcon());
		old.setText(l.getText());
	}
	public static void addTileHealthBar(Tile tile, String text, JPanel parent) {
		if (text == null) {
			text = health;
		}
		Color currentCol = Constants.colorBackground.brighter();
		Color totalCol = Constants.colorBackground.darker();
		// could be null if this is a bar for a weapon's targettile
		JLabel l = null;
		if (tile != null) {
			l = createProgressBar(tile.getHealth(), tile.getHealthTotal(), text, currentCol, totalCol);
		} else {
			l = createProgressBar(0, 0, text, currentCol, totalCol);
		}
		parent.add(l);
	}
	public static void updateTileHealthBar(Tile tile, String text, JLabel old) {
		if (text == null) {
			text = health;
		}
		Color currentCol = Constants.colorBackground.brighter();
		Color totalCol = Constants.colorBackground.darker();
		JLabel l = null;
		if (tile != null) {
			l = createProgressBar(tile.getHealth(), tile.getHealthTotal(), text, currentCol, totalCol);
		} else {
			l = createProgressBar(0, 0, text, currentCol, totalCol);
		}
		old.setIcon(l.getIcon());
		old.setText(l.getText());
	}
	public static void addLoadBar(Weapon w, String text, JPanel parent) {
		if (text == null) {
			text = loadTime;
		}
		Color currentCol = Color.green.darker();
		Color totalCol = Constants.colorBackground.darker();
		// adjust to be based on percent
		JLabel l = createProgressBar(w.getReloadProgress() / 10, 100, text, currentCol, totalCol);
		parent.add(l);
	}
	public static void updateLoadBar(Weapon w, String text, JLabel old) {
		if (text == null) {
			text = loadTime;
		}
		Color currentCol = Color.green.darker();
		Color totalCol = Constants.colorBackground.darker();
		JLabel l = createProgressBar(w.getReloadProgress() / 10, 100, text, currentCol, totalCol);
		old.setIcon(l.getIcon());
		old.setText(l.getText());
	}
	public static void addEnergyBar(Tile tile, String text, JPanel parent) {
		if (text == null) {
			text = energy;
		}
		Color currentCol = Shields.shieldShade.brighter();
		Color totalCol = Shields.systemMain.darker();
		JLabel l = null;
		if (tile != null) {
			Shield s = (Shield) tile.getItem();
			l = createProgressBar(s.getBufferHealth(), s.getBufferMax(), text, currentCol, totalCol);
		} else {
			l = createProgressBar(0, 0, text, currentCol, totalCol);
		}
		parent.add(l);
	}
	public static void updateEnergyBar(Tile tile, String text, JLabel old) {
		if (text == null) {
			text = energy;
		}
		Color currentCol = Shields.shieldShade.brighter();
		Color totalCol = Shields.systemMain.darker();
		JLabel l = null;
		if (tile != null) {
			Shield s = (Shield) tile.getItem();
			l = createProgressBar(s.getBufferHealth(), s.getBufferMax(), text, currentCol, totalCol);
		} else {
			l = createProgressBar(0, 0, text, currentCol, totalCol);
		}
		old.setIcon(l.getIcon());
		old.setText(l.getText());
	}
	public static void addBreathBar(CrewMan man, JPanel parent) {
		Color currentCol = Color.BLUE.brighter();
		Color totalCol = Color.BLUE.darker();
		JLabel l = createProgressBar(man.getBreath(), man.getBreathTotal(), breath, currentCol, totalCol);
		parent.add(l);
	}
	public static void updateBreathBar(CrewMan man, JLabel old) {
		Color currentCol = Color.BLUE.brighter();
		Color totalCol = Color.black;
		JLabel l = createProgressBar(man.getBreath(), man.getBreathTotal(), breath, currentCol, totalCol);
		old.setIcon(l.getIcon());
		old.setText(l.getText());
	}
	/**
	 * Creates and returns a progress bar
	 * 
	 * @param w
	 *            -width of bar
	 * @param current
	 *            - current level of progress
	 * @param total
	 *            - total progress level
	 * @param currentCol
	 *            - color of progress
	 * @param totalCol
	 *            - background color
	 * @return
	 */
	public static JLabel createProgressBar(int current, int total, String text,
			Color currentCol, Color totalCol) {
		// do our math
		int h = 15;
		int w = 100;
		double progress = (double) current / total;
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics g = bi.createGraphics();
		// draw total rect
		g.setColor(totalCol);
		g.fillRect(0, 0, w, h);
		// draw progress rect
		g.setColor(currentCol);
		w = (int) (w * progress);
		g.fillRect(0, 0, w, h);
		g.dispose();
		ImageIcon i = new ImageIcon();
		i.setImage(bi);
		text = text + current + "/" + total;
		JLabel l = new JLabel(text);
		l.setFont(Home.gameFontSub);
		l.setForeground(Color.white);
		l.setIcon(i);
		l.setIconTextGap(-100);
		return l;
	}
	// TODO - JButtons section
	/**
	 * Creates a formatted JButton
	 */
	public static JButton createJButton(String text, ActionListener listener,
			String toolTip) {
		JButton but = new JButton(text);
		but.setMargin(new Insets(0, 0, 0, 0));
		but.addActionListener(listener);
		but.setToolTipText(toolTip);
		return but;
	}
	public static JButton createJButton(String text, ActionListener listener) {
		return createJButton(text, listener, "");
	}
	// TODO - JLabels section
	public static void addJLabel(String text, JPanel parent) {
		addJLabel(text, parent, Home.gameFontSub, "");
	}
	public static void addJLabel(String text, JPanel parent, String toolTip) {
		addJLabel(text, parent, Home.gameFontSub, toolTip);
	}
	public static void addJLabel(String text, JPanel parent, Font font) {
		addJLabel(text, parent, font, "");
	}
	public static void addJLabel(String text, JPanel parent, Font font, String toolTip) {
		JLabel l = createJLabel(text, font, toolTip);
		l.setMaximumSize(new Dimension(parent.getWidth(), parent.getHeight()));
		parent.add(l);
		// System.out.println("label" + l.getPreferredSize().height);
	}
	public static JLabel createJLabel(String text, Font font, String toolTip) {
		JLabel l = new JLabel("<html>" + text + "</html>");
		l.setFont(font);
		l.setForeground(Color.white);
		l.setOpaque(false);
		l.setToolTipText(toolTip);
		return l;
		// System.out.println("label" + l.getPreferredSize().height);
	}
	// TODO - Other section
	public static void addJText(String text, JPanel parent, Font font) {
		JTextArea jt = new JTextArea(4, 1);
		jt.setAlignmentY(JTextField.TOP_ALIGNMENT);
		jt.append("#" + text);
		jt.append("\n#" + "line 1");
		jt.append("\n#" + "line 2");
		jt.setFont(font);
		// jt.setBounds(parent.getBounds());
		jt.setForeground(Color.white);
		jt.setOpaque(false);
		parent.add(jt);
		// System.out.println("label" + jt.getPreferredSize().height);
	}
	public static boolean isValidString(String s) {
		return (s != null && s != "");
	}
}
