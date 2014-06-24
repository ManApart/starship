package org.iceburg.home.main;

import java.awt.Font;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.iceburg.home.data.Data;
import org.iceburg.home.persistance.PlayerParser;
import org.iceburg.home.persistance.PlayerSave;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.sound.SoundManager;
import org.iceburg.home.story.BattleLocation;
import org.iceburg.home.story.Player;
import org.iceburg.home.ui.GameScreen;
import org.iceburg.home.ui.MessageCenter;

public class Home extends JFrame {
	// ship is the ship the player is actually controlling,
	// public static Ship playerShip;
	private static Player player;
	private static PlayerProfile profile;
	private String imageName = "textures/world/ship/floorplans/ISV_Sol_P.png";
	private Timer timer;
	private boolean ingame = true;
	private static GameScreen currentScreen;
	public static Font gameFont, gameFontSub;
	public static Data resources;
	private static SoundManager soundManager;
	public static Random generator = new Random(ConfigVariables.algorithmSeed * 5333);
	private static JScrollPane messageScroll;
	public static MessageCenter messagePanel;
	// Debug vars
	public static HashMap<Tile, Integer> crewPath;
	// creative mode: unlimited engine power, all crew controlled by player,
	// sensors at max
	public static boolean displayAir, displayCrew, displayAlt, displayGrid, creativeMode, processAI,
			paused;
	public static JFrame frame;

	public Home() {
		init();
	}
	public void init() {
		// Parse Data
		resources = new Data();
		resources.parseData();
		// set up frame
		// TODO - update so we have full screen
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setTitle("Starship");
		frame.setVisible(true);
		frame.setResizable(false);
		gameFont = new Font("Helvetica", Font.BOLD, 14);
		gameFontSub = new Font("Helvetica", Font.TRUETYPE_FONT, 14);
		Image img = new ImageIcon((resources).getClass().getResource(imageName)).getImage();
		frame.setIconImage(img);
		frame.addKeyListener(new TAdapter());
		frame.setFocusable(true);
		soundManager = new SoundManager();
		// init message Center
		messagePanel = new MessageCenter(messageScroll);
		setProfile(new PlayerProfile());
		//HSV_Sol, jumper
		setPlayer(new Player(Ship.shipComplete("jumper", 10, 10)));
		processAI = true;
		// battleLocCurrent = new BattleLocation();
		// battleLocCurrent.genImages();
		// battleLocCurrent.addShip(getShip());
		// TODO - initiate main game views
		setCurrentScreen(new GameScreen());
		getCurrentScreen().add(messageScroll);
		frame.add(currentScreen);
		timer = new Timer();
		timer.scheduleAtFixedRate(new gameTick(), 100, 10);
		frame.pack();
		frame.setLocationRelativeTo(null);
		testFunctions();
	}
	// public class FullScreen extends Window {
	//
	// public FullScreen() {
	// super(new JFrame());
	//
	//
	// Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	// setBounds(0,0,screenSize.width, screenSize.height);
	// }
	// }
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Home home = new Home();
				// home.setVisible(true);
				// (home).frame.setVisible(true);
				// try {
				// home.setFullScreenWindow(frame);
				// } finally {
				// home.setFullScreenWindow(null);
				// }
			}
		});
	}

	// runs ever 10 milliseconds, or 1/100 a second
	class gameTick extends TimerTask {
		public void run() {
			// System.out.println("Game Tick");
			if (!isPaused()) {
				// battleLocCurrent.updateShips();
				if (getPlayer().getCurrentLocation() != null) {
					getPlayer().getCurrentLocation().updateShips();
				}
				if (getPlayer().getQuest() != null){
					getPlayer().getQuest().update();
				}
				Controls.update();
				// getStationsPanel().updateScreen();
			}
			frame.repaint();
		}
	}

	// TODO - use this to get away from JFrame refresh/ have more control over
	// repainting?
	// public void paintScreen() {
	// Graphics g = getGraphics();
	// // getCurrentScreen().paintScreen(g);
	// Toolkit.getDefaultToolkit().sync();
	// g.dispose();
	// }
	private class TAdapter extends KeyAdapter {
		public void keyReleased(KeyEvent e) {
			if (ingame) {
				Controls.keyReleased(e);
			} else {
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER) {
					init();
				} else if (key == KeyEvent.VK_ESCAPE) {
					System.exit(0);
				}
			}
		}
		public void keyPressed(KeyEvent e) {
			// System.out.println("Key Pressed");
			Controls.keyPressed(e);
		}
	}
	//TODO - seperate saving and loading of profile.
	/**
	 * Create a save file
	 * Currently also saves profile
	 */
	public static void save(){
		System.out.println("Home: Save button");
		PlayerParser.writePlayerProfile(getProfile());
		PlayerParser.writePlayerSave(getPlayer());
		System.out.println("Home: Saved!");
		Home.messagePanel.addMessage("Saved!");
	}
	/**
	 * Load a save file
	 * Currently also loads profile
	 */
	public static void load(){
		System.out.println("Home: Load button");
		int response = JOptionPane.showConfirmDialog(getCurrentScreen(), "If you load a file, any unsaved progress will be lost in a nebula.", "Load File?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.DEFAULT_OPTION);
		if (response == 0) {
			PlayerParser.readPlayerSave();
		
			//TODO - add select file?
			//read profile in same directory, if it exists
			//then read save
	//		Home.messagePanel.addMessage("Loaded!");
		}
	}
	/**
	 * Quit the game
	 */
	public static void quit(){
//		System.out.println("Home: Quit button");
		int response = JOptionPane.showConfirmDialog(getCurrentScreen(), "Are you sure you want to quit? Unsaved progress will seep into a black hole.", "Quit?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.DEFAULT_OPTION);
		if (response == 0) {
			System.exit(0);
		}
	}
	
	private static File promptForFtlPath() {
		File ftlPath = null;

		String message = "FTL Homeworld uses images and data from FTL,\n";
		message += "but the path to FTL's resources could not be guessed.\n\n";
		message += "You will now be prompted to locate FTL manually.\n";
		message += "Select '(FTL dir)/resources/data.dat'.\n";
		message += "Or 'FTL.app', if you're on OSX.";
		JOptionPane.showMessageDialog(null,  message, "FTL Not Found", JOptionPane.INFORMATION_MESSAGE);

		final JFileChooser fc = new JFileChooser();
		fc.setDialogTitle( "Find data.dat or FTL.app" );
		fc.addChoosableFileFilter( new FileFilter() {
			@Override
			public String getDescription() {
				return "FTL Data File - (FTL dir)/resources/data.dat";
			}
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().equals("data.dat") || f.getName().equals("FTL.app");
			}
		});
		fc.setMultiSelectionEnabled(false);

		if ( fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION ) {
			File f = fc.getSelectedFile();
			if ( f.getName().equals("data.dat") )
				ftlPath = f.getParentFile();
			else if ( f.getName().endsWith(".app") && f.isDirectory() ) {
				File contentsPath = new File(f, "Contents");
				if( contentsPath.exists() && contentsPath.isDirectory() && new File(contentsPath, "Resources").exists() )
					ftlPath = new File(contentsPath, "Resources");
			}
			// log.trace( "User selected: " + ftlPath.getAbsolutePath() );
		} else {
			// log.trace( "User cancelled FTL dats path selection." );
		}

		if ( ftlPath != null && isPathValid(ftlPath) ) {
			return ftlPath;
		}

		return null;
	}
	private static boolean isPathValid(File path) {
		return (path.exists() && path.isDirectory() && new File(path,"Save.sav").exists());
	}

	public static GameScreen getCurrentScreen() {
		return currentScreen;
	}
	public static void setCurrentScreen(GameScreen currentScreen) {
		Home.currentScreen = currentScreen;
	}
	/**
	 * Run various test functions
	 */
	public void testFunctions() {
//		getPlayer().setQuest(resources.getQuests().getQuestByID("startQuest"));
		PlayerParser.writePlayerProfile(getProfile());
		// PlayerParser.writePlayerSave(getPlayer());
		// add enemy ship
		// Ship testship = Ship.shipComplete("test", ((int)
		// getShip().getWarp().getXpos() + 10), ((int)
		// getShip().getWarp().getYpos() + 0));
		// Ship testship = Ship.shipComplete("ISV_Shard", ((int)
		// getShip().getWarp().getXpos() + 10), ((int)
		// getShip().getWarp().getYpos() + 0));
		// battleLocCurrent.addShip(testship);
		// getShip().getHelm().setTargetShip(testship.getWarp());
		displayAlt = true;
	}
	/**
	 * Returns the player's ship
	 */
	public static Ship getShip() {
		if (getPlayer() == null) {
			return null;
		}
		return getPlayer().getPlayerShip();
	}
	public static void setShip(Ship inputShip) {
		getPlayer().setPlayerShip(inputShip);
	}
	public static Player getPlayer() {
		return player;
	}
	public static void setPlayer(Player player) {
		Home.player = player;
	}
	public static BattleLocation getBattleLoc() {
		if (getPlayer() != null) {
			return getPlayer().getCurrentLocation();
		} else {
			return null;
		}
	}
	public static SoundManager getSoundManager() {
		return soundManager;
	}
	public static boolean isPaused() {
		return paused;
	}
	public static void setPaused(boolean paused) {
		Home.paused = paused;
		String s = "Unpaused";
		if (Home.isPaused()) {
			s = "Paused";
		}
		Home.messagePanel.addMessage(s);
	}
	public static PlayerProfile getProfile() {
		return profile;
	}
	public static void setProfile(PlayerProfile profile) {
		Home.profile = profile;
	}
	public static void setMessageScroll(JScrollPane messageScroll) {
		Home.messageScroll = messageScroll;
	}
	public static JScrollPane getMessageScroll() {
		return messageScroll;
	}
	public static void updateQuestAction(){
		if (getPlayer().getQuest() != null && getPlayer().getQuest().getCurrentEvent() != null && getPlayer().getQuest().getCurrentEvent().getAction() != null){
			getPlayer().getQuest().getCurrentEvent().getAction().updateAction();
		}
	}
}
