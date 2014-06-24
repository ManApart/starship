package org.iceburg.home.test;

import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.iceburg.home.data.Data;

public class TestMain extends JFrame {
	private String imageName = "textures/interface/Logo Icon.png";

	public TestMain() {
		// super(new JFrame());
		init();
	}
	public void init() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setTitle("Home");
		setVisible(true);
		setResizable(false);
		Image img = new ImageIcon((new Data()).getClass().getResource(imageName)).getImage();
		setIconImage(img);
		setFocusable(true);
		pack();
		setLocationRelativeTo(null);
	}
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new SeedRandomTest();
			}
		});
	}
}
