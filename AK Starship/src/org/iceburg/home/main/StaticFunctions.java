package org.iceburg.home.main;

import java.awt.Color;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class StaticFunctions {
	public static Image rotateImage(BufferedImage image, double angle) {
		double rotationRequired = Math.toRadians(angle);
		double locationX = image.getWidth() / 2;
		double locationY = image.getHeight() / 2;
		AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX, locationY);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
		// Drawing the rotated image at the required drawing locations
		return op.filter(image, null);
	}
	// Seed algorithm
	public static int algorithmRand(int seed) {
		Home.generator.setSeed(ConfigVariables.algorithmSeed + seed * 1000);
		return (int) (Home.generator.nextDouble() * (100));
	}
	public static int algorithmRandRange(int seed, int min, int max) {
		return min + (int) (algorithmRand(seed) / 100 * ((max - min) + 1));
	}
	public static int randRange(int min, int max) {
		return min + (int) (Math.random() * ((max - min) + 1));
	}
	/**
	 * My version of a simple rand, so that I know where then non-algorithm
	 * rands are
	 * 
	 * @return
	 */
	public static double gameRand() {
		return Math.random();
	}
	public static String starDateToString(int[] date) {
		String stardate = date[0] + "." + date[1] + "." + date[2] + "." + date[3] + "."
				+ date[4] +
				// "."+date[5]+"."+date[6] +
				"";
		return stardate;
	}
	/**
	 * Blends two colors, optionally taking a percent alpha for the first color
	 * 
	 * @param opacity
	 *            - (0-1) color1's percent opacity
	 */
	public static Color combineColors(Color color1, Color color2, double opacity) {
		if (opacity > 1) {
			opacity = 1;
		}
		double opacity2 = 1 - opacity;
		int r = (int) (color1.getRed() * opacity + color2.getRed() * opacity2);
		int g = (int) (color1.getGreen() * opacity + color2.getGreen() * opacity2);
		int b = (int) (color1.getBlue() * opacity + color2.getBlue() * opacity2);
		return new Color(r, g, b);
	}
	/**
	 * Returns the average of two colors
	 */
	public static Color combineColors(Color color1, Color color2) {
		return combineColors(color1, color2, 0.5);
	}
	/**
	 * Takes any int angle (pos/neg) and returns an angle within 360* (for
	 * example, an input of 365 would give 5*. an input of -10* would give an
	 * output of 350*)
	 */
	public static int within360(int i) {
		if (i > 360) {
			while (i > 360) {
				i -= 360;
			}
		} else if (i < 0) {
			while (i < 0) {
				i += 360;
			}
		}
		return i;
	}
	// /**
	// * Creates a formatted JButton
	// */
	// public static JButton createJButton(String text, ActionListener listener,
	// String toolTip) {
	// JButton but = new JButton(text);
	// but.setMargin(new Insets(0, 0, 0, 0));
	// but.addActionListener(listener);
	// but.setToolTipText(toolTip);
	// return but;
	// }
	// public static JButton createJButton(String text, ActionListener listener)
	// {
	// return createJButton(text, listener, "");
	// }
	// public static void addJLabel(String text, JPanel parent) {
	// addJLabel(text, parent, Home.gameFontSub, "");
	// }
	// public static void addJLabel(String text, JPanel parent, String toolTip)
	// {
	// addJLabel(text, parent, Home.gameFontSub, toolTip);
	// }
	// public static void addJLabel(String text, JPanel parent, Font font) {
	// addJLabel(text, parent, font, "");
	// }
	// public static void addJLabel(String text, JPanel parent, Font font,
	// String toolTip) {
	// JLabel l = new JLabel("<html>" + text + "</html>");
	// l.setFont(font);
	// l.setMaximumSize(new Dimension(parent.getWidth(), parent.getHeight()));
	// l.setForeground(Color.white);
	// l.setOpaque(false);
	// l.setToolTipText(toolTip);
	// parent.add(l);
	// // System.out.println("label" + l.getPreferredSize().height);
	// }
	// public static void addJText(String text, JPanel parent, Font font) {
	// JTextArea jt = new JTextArea(4, 1);
	// jt.setAlignmentY(JTextField.TOP_ALIGNMENT);
	// jt.append("#" + text);
	// jt.append("\n#" + "line 1");
	// jt.append("\n#" + "line 2");
	// jt.setFont(font);
	// // jt.setBounds(parent.getBounds());
	// jt.setForeground(Color.white);
	// jt.setOpaque(false);
	// parent.add(jt);
	// // System.out.println("label" + jt.getPreferredSize().height);
	// }
	// public static boolean isValidString(String s) {
	// return (s != null && s != "");
	// }
}
