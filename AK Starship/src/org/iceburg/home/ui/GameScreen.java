package org.iceburg.home.ui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.iceburg.home.actors.CrewMan;
import org.iceburg.home.items.CargoBay;
import org.iceburg.home.items.Item;
import org.iceburg.home.items.Weapon;
import org.iceburg.home.main.Constants;
import org.iceburg.home.main.Controls;
import org.iceburg.home.main.Home;
import org.iceburg.home.ship.Ship;
import org.iceburg.home.ship.ShipFloorPlan;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.ship.systems.CargoHold;
import org.iceburg.home.ship.systems.Sensors;
import org.iceburg.home.ship.systems.Warp;
import org.iceburg.home.story.BattleLocation;

public class GameScreen extends JPanel implements MouseListener, MouseWheelListener,
		ActionListener, MouseMotionListener {
	ImageIcon img, gmimg;
	BufferedImage bg, gmbg;
	private String imageName = "textures/interface/View Screen Frame Overlay.png";
	private String imageNameMenu = "textures/interface/Game Menu.png";
	public static int borderSide = 150, borderTop = 50;
	Point tempClick;
	int currentFloor, currentView;
	public static int viewInternal = 1, viewExternal = 2, viewComputer = 0;
	int[] startLoc, endLoc;
	Ship currentShip;
	CrewMan currentCrewMan;
	Tile currentTile, clickTile;
	TilePane currentTilePane;
	CrewPane currentCrewPane;
	// Jpanels
	JPanel leftPanel, rightPanel;
	JScrollPane rightScroll, leftScroll;
	PopUpBox popUpBox;
	ArrayList<TilePane> activeTiles;
	ArrayList<CrewPane> activeCrew;
	// Context Buttons for the popupbux
	ArrayList<JButton> selectBtns;
	//view Buttons
	JButton btnTargetShip, btnSensors, btnComputer;
	//Computer buttons
	JButton btnSave, btnLoad, btnQuit;
	//view internal/external buttons
	JButton btnTravelMan, btnDoorOpen, btnCancel, btnRepair, btnBreak, btnTake, btnLoot,
			btnPlace, btnPower, btnChooseTarget, btnDismiss;

	public GameScreen() {
		setLayout(null);
		addMouseListener(this);
		addMouseWheelListener(this);
		addMouseMotionListener(this);
		setDoubleBuffered(true);
		setSize(Constants.windowSize);
		setPreferredSize(Constants.windowSize);
		// super(parent);
		addKeyListener(new TAdapter());
		// background
		img = new ImageIcon(Home.resources.getClass().getResource(imageName));
		bg = new BufferedImage(img.getIconWidth(), img.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics gg = bg.createGraphics();
		img.paintIcon(null, gg, 0, 0);
		gg.dispose();
		//game background
		gmimg = new ImageIcon(Home.resources.getClass().getResource(imageNameMenu));
		gmbg = new BufferedImage(gmimg.getIconWidth(), gmimg.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		gg = gmbg.createGraphics();
		gmimg.paintIcon(null, gg, 0, 0);
		gg.dispose();
		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		// initialize AI vars
		startLoc = new int[3];
		endLoc = new int[3];
		activeTiles = new ArrayList<TilePane>();
		activeCrew = new ArrayList<CrewPane>();
		currentFloor = 0;
		// Add buttons
		// Bottom
		String text = "Targeted Ship";
		if (currentShip == Home.getShip()) {
			text = "Internal Sensors";
		}
		btnTargetShip = UIMisc.createJButton(text, this);
		btnTargetShip.setBounds(140, 560, 120, 23);
		add(btnTargetShip);
		btnSensors = UIMisc.createJButton("External Sensors", this);
		btnSensors.setBounds(330, 560, 130, 23);
		add(btnSensors);
		btnComputer = UIMisc.createJButton("Main Computer", this);
		btnComputer.setBounds(520, 560, 130, 23);
		btnComputer.setText("Internal Sensors");
		add(btnComputer);
		setCurrentView(viewInternal);
		initPanels();
	}

	class TAdapter extends KeyAdapter {
		public void keyReleased(KeyEvent e) {
			Controls.keyReleased(e);
		}
		public void keyPressed(KeyEvent e) {
			// System.out.println("Key Pressed");
			Controls.keyPressed(e);
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		if (getCurrentView() == viewInternal) {
			paintViewInternal(g);
		} else if (getCurrentView() == viewExternal) {
			paintViewExternal(g);
		} else if (getCurrentView() == viewComputer) {
			paintViewComputer(g);
		}
	}
	/**
	 * Paint internal view
	 */
	public void paintViewInternal(Graphics g) {
		// background
		Graphics2D g2d = (Graphics2D) g;
		g.setColor(Color.black);
		g2d.drawImage(bg, 0, 0, img.getIconWidth(), img.getIconHeight(), this);
		// g.fillRect(borderSide, borderTop, 500, 500);
		// text setup
		g.setColor(Color.white);
		g.setFont(Home.gameFont);
		// Paint based on views
		if (currentShip == null) {
			currentShip = Home.getShip();
		}
		g.translate(borderSide, borderTop);
		currentShip.paintShip(g2d, currentFloor);
		g.translate(-borderSide, -borderTop);
	}
	public void paintViewExternal(Graphics g) {
		// background
		Graphics2D g2d = (Graphics2D) g;
		g2d.translate(borderSide, borderTop);
		getBackgroundLoc().paintWOScroll(g2d);
		g.translate(-borderSide, -borderTop);
		g2d.drawImage(bg, 0, 0, img.getIconWidth(), img.getIconHeight(), this);

	}
	public void paintViewComputer(Graphics g) {
		// background
		Graphics2D g2d = (Graphics2D) g;
		g.setColor(Color.black);
		g2d.translate(borderSide, borderTop);
//		g2d.fillRect(0, 0, Constants.viewScreenSize, Constants.viewScreenSize);
		g2d.drawImage(gmbg, 0, 0, gmimg.getIconWidth(), gmimg.getIconHeight(), this);
		g.translate(-borderSide, -borderTop);
		g2d.drawImage(bg, 0, 0, img.getIconWidth(), img.getIconHeight(), this);
	}
	
	/**
	 * Adds buttons specific to computer view
	 */
	public void buildComputerView(){
		int x = Constants.viewScreenSize/2 + borderSide - 70;
		int y = Constants.viewScreenSize* 2/3;
		if (btnSave == null){
			btnSave = new JButton("Save - Disabled");
//			btnSave.addActionListener(this);
			
		}
		if (btnLoad == null){
			btnLoad = new JButton("Load - Disabled");
//			btnLoad.addActionListener(this);
			
		}
		if (btnQuit == null){
			btnQuit = new JButton("Quit");
			btnQuit.addActionListener(this);
			
		}
		btnSave.setBounds(x, y, 130, 23);
		add(btnSave);
		btnLoad.setBounds(x, y + 40, 130, 23);
		add(btnLoad);
		btnQuit.setBounds(x, y + 80, 130, 23);
		add(btnQuit);
	}
	/**
	 * Removes buttons specific to computer view
	 */
	public void removeComputerView(){
		if (btnSave != null){
			remove(btnSave);
		}
		if (btnLoad != null){
			remove(btnLoad);
		}
		if (btnQuit != null){
			remove(btnQuit);
		}
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		// This allows the player to drag the ship around
		Point click = e.getPoint();
		// find the column/ row values for the clicks
		int x = tempClick.x / Constants.shipSquare;
		int xn = click.x / Constants.shipSquare;
		int y = tempClick.y / Constants.shipSquare;
		int yn = click.y / Constants.shipSquare;
		// distance
		int xd = xn - x;
		int yd = yn - y;
		// asign a value of -1,0, or 1
		if (xd > 0) {
			xd = 1;
		} else if (xd < 0) {
			xd = -1;
		} else {
			xd = 0;
		}
		if (yd > 0) {
			yd = 1;
		} else if (yd < 0) {
			yd = -1;
		} else {
			yd = 0;
		}
		// update
		getCurrentFloorPlan().incShiftX(xd);
		getCurrentFloorPlan().incShiftY(yd);
		if (xd != 0 || yd != 0) {
			getCurrentFloorPlan().updateImage();
		}
		tempClick = click;
	}
	public void mouseClick(MouseEvent e) {
		tempClick = e.getPoint();
		Rectangle screen = new Rectangle(borderSide, borderTop, (int) Constants.viewScreenSize, (int) Constants.viewScreenSize);
		// If we clicked inside the view pane
		// give the click to internal -ship, internal - target, external, or
		// main computer
		if (screen.contains(tempClick)) {
			if (getCurrentView() == viewInternal) {
				clickInternal(e);
			} else if (getCurrentView() == viewExternal) {
				clickExternal(e);
			}
		} else if (e.getSource() instanceof TilePane) {
			TilePane clickPane = (TilePane) e.getSource();
			setCurrentTile(clickPane.getTile());
			TilePane.createPanel();
		} else if (e.getSource() instanceof CrewPane) {
			CrewPane clickPane = (CrewPane) e.getSource();
			setCurrentCrewMan(clickPane.getMan());
			CrewPane.createPanel();
		}
	}
	public void clickInternal(MouseEvent e) {
		// System.out.println("Clicked pane");
		ShipFloorPlan floorplan = currentShip.getFloorplans().get(currentFloor);
		int gridSize = (int) (Constants.shipSquare);
		int column = (int) ((tempClick.getX() - borderSide - (floorplan.getShiftX() * Constants.shipSquare)) / gridSize);
		int row = (int) ((tempClick.getY() - borderTop - (floorplan.getShiftY() * Constants.shipSquare)) / gridSize);
		// left click selects
		if (SwingUtilities.isLeftMouseButton(e)) {
			// System.out.println("Left click floor:"+
			// currentFloor+" Tile at: "+ column + ", "+ row);
			// Commands if this is the player ship
			// if (currentShip == Home.playerShip) {
			Tile tile = floorplan.getTileAt(column, row);
			if (tile != null) {
				CrewMan man = (CrewMan) tile.getActor();
				if (man != null && man != getCurrentCrewMan()) {
					startLoc = new int[] { currentFloor, column, row };
					setCurrentCrewMan(man);
					// Home.crewPath = getCurrentCrewMan().getCrewPath();
					// System.out.println("Crewman");
				} else {
					setCurrentTile(tile);
				}
			}
			// }
		}
		// right click commands
		else if (SwingUtilities.isRightMouseButton(e)) {
			// System.out.println("Right click floor:"+
			// currentFloor+" Tile at: "+ column + ", "+ row);
			clickTile = floorplan.getTileAt(column, row);
			if (clickTile != null) {
				ArrayList<JButton> buttons = createGeneralOptions(clickTile);
				if (clickTile.hasParentSystem()
						&& (currentShip == Home.getShip() || Home.creativeMode)) {
					popUpBox = clickTile.getParentSystem().createSystemOptionsPopUp(this, tempClick, buttons, clickTile);
				} else {
					popUpBox = PopUpBox.createPopUpBox(this, tempClick, buttons);
				}
			}
		}
	}
	public void clickExternal(MouseEvent e) {
		// System.out.println("Clicked external");
		// TODO - can click ships to select them in helm
	}
	/**
	 * Create's general options regarding this tile and adds them to a list
	 */
	public ArrayList<JButton> createGeneralOptions(Tile tile) {
		if (tile != null) {
			ArrayList<JButton> buttons = new ArrayList<JButton>();
			endLoc = tile.getLocation();
			if (btnChooseTarget == null) {
				btnChooseTarget = UIMisc.createJButton("Set Target", this);
			}
			if (getCurrentTile() != null
					&& getCurrentTile().isWeapon()
					&& (clickTile != null && getCurrentTile().getParentShip() != clickTile.getParentShip())) {
				buttons.add(btnChooseTarget);
			}
			if (tile.isFriendly() || Home.creativeMode) {
				// power system
				if (tile.getHealth() > 0 && tile.hasParentSystem()
						&& tile.getParentSystem().isManned()
						&& tile.getItem().getMaxPower() > 0) {
					String s = " on";
					if (tile.isPowered()) {
						s = " off";
					}
					btnPower = UIMisc.createJButton("Power All" + s, this);
					// + tile.getName() + s, this);
					buttons.add(btnPower);
				}
				if (currentCrewMan != null && currentCrewMan.isPlayerControlled()) {
					if (tile.hasParentSystem()) {
						btnTravelMan = UIMisc.createJButton("Man " + tile.getName(), this);
					} else {
						btnTravelMan = UIMisc.createJButton("Travel here", this);
					}
					if (currentCrewMan.getCurrentTile() != tile && tile != null
							&& !tile.isSpaceTile()) {
						buttons.add(btnTravelMan);
					}
					// repair
					if (tile.getHealth() < tile.getHealthTotal() && !tile.isPowered()) {
						btnRepair = UIMisc.createJButton("Repair", this);
						buttons.add(btnRepair);
					}
					// place
					if (tile.getHealth() >= tile.getHealthTotal() || tile.isSpaceTile()) {
						//see if either the crewman has an item, or the active cargobay has an item
						Item testItem = getCurrentCrewMan().getItem();
						if (testItem == null && tile.getParentShip().getCargo().isManned()){
							if (getCurrentTile() != null && getCurrentTile().getTileColor().equals(CargoHold.systemMain)){
								testItem = ((CargoBay) getCurrentTile().getItem()).getItem();
							}
							if (testItem == null){
								testItem = tile.getParentShip().getCargo().getFirstCargoItem();
							}
						}
						if (tile != null && testItem != null
						// tile is space and crewman has floor or a floor with
						// full health
								&& ((testItem.isFloor() && tile.isSpaceAdjacentToShipTile()) || (tile.getItem() != null
										&& tile.getItem().isFloor() && !testItem.isFloor()))) {
							btnPlace = UIMisc.createJButton("Place", this);
							buttons.add(btnPlace);
						}
					}
					// uninstall
					if (!tile.isPowered() && tile.getHealth() > 0) {
						btnBreak = UIMisc.createJButton("Uninstall", this);
						buttons.add(btnBreak);
					}
					// take
					if (tile != null && !tile.isSpaceTile()) {
						if (getCurrentCrewMan().hasItem()) {
							// don't swap unless both items are floors, or both
							// arn't floors
							if (tile.getHealth() < 1
									&& getCurrentCrewMan().getItem().isFloor() == tile.getItem().isFloor()) {
								btnTake = UIMisc.createJButton("Swap", this);
								buttons.add(btnTake);
							}
						} else if (tile.getHealth() <= 0) {
							btnTake = UIMisc.createJButton("Take", this);
							btnLoot = UIMisc.createJButton("Loot item", this, "Take this item and store it in the cargo hold");
							if (tile.hasItem() && (Home.getBattleLoc().isInBattle() && !getCurrentCrewMan().getParentShip().getCargo().isManned())) {
								btnLoot.setText("Loot " + tile.getItem());
								buttons.add(btnLoot);
							}
							buttons.add(btnTake);
							//else if room in cargo bay
						} 
						if (!Home.getBattleLoc().isInBattle() && getCurrentCrewMan().getParentShip().getCargo().isManned()){
							btnLoot = UIMisc.createJButton("Store item", this, "Immediately store in the cargo hold");
							buttons.add(btnLoot);
						}
					}
					btnDismiss = UIMisc.createJButton("Dismiss man", this);
					buttons.add(btnDismiss);
				}
			}
			// add cancel and then add popupBox
			if (btnCancel == null) {
				btnCancel = UIMisc.createJButton("Cancel", this);
			}
			buttons.add(btnCancel);
			return buttons;
		}
		return null;
	}
	// Button Presses
	@Override
	public void actionPerformed(ActionEvent ae) {
		JButton o = (JButton) ae.getSource();
		// View Buttons
		if (o == btnTargetShip) {
			setCurrentView(viewInternal);
			if (getCurrentShip() == Home.getShip()) {
				Warp target = Home.getShip().getHelm().getTargetShip();
				if (target != null) {
					setCurrentShip(target.getParentShip());
				} else {
					Home.messagePanel.addMessage("No target ship!");
				}
			} else {
				setCurrentShip(Home.getShip());
			}
			// update other button if need be
			if (!btnSensors.getText().equals("External Sensors")) {
				btnSensors.setText("External Sensors");
			}
			btnComputer.setText("Main Computer");
		} else if (o == btnSensors) {
			String s = "Targeted Ship";
			if (o.getText().equals(("External Sensors"))){
				setCurrentView(viewExternal);
				s = "Internal Sensors";
			} else if (o.getText().equals(("Internal Sensors"))){
				setCurrentShip(Home.getShip());
				setCurrentView(viewInternal);
				s = "External Sensors";
			}
			btnSensors.setText(s);
			btnComputer.setText("Main Computer");
		} else if (o == btnComputer) {
			//change view
			if (o.getText().equals(("Main Computer"))){
				setCurrentView(viewComputer);
				Home.setPaused(true);
				//update texts
				if (btnSensors.getText().equals(("External Sensors")) && btnTargetShip.getText().equals(("Targeted Ship"))){
					btnComputer.setText("Internal Sensors");
				} else if (btnSensors.getText().equals(("Internal Sensors")) && btnTargetShip.getText().equals(("Targeted Ship"))){
					btnComputer.setText("External Sensors");
				}
			}else if (o.getText().equals(("External Sensors"))){
				setCurrentView(viewExternal);
				btnComputer.setText("Main Computer");
			} else if (o.getText().equals(("Internal Sensors"))){
				setCurrentView(viewInternal);
				btnComputer.setText("Main Computer");
			}
		}
		//Main Computer Specific Buttons
		if (getCurrentView()== viewComputer){
			if (o == btnSave){
				Home.save();
			} else if (o == btnLoad){
				Home.load();
			} else if (o == btnQuit){
				Home.quit();
			}
		} else if (getCurrentView()== viewInternal || getCurrentView()== viewExternal){
			// if click tile is null, it's cause we used the rightside menu buttons
			if (clickTile == null) {
				clickTile = getCurrentTile();
			}

			if (btnTravelMan != null && o.getText().equals(btnTravelMan.getText())) {
				// if we clicked the tilepane, update the endloc
				if (clickTile == getCurrentTile()) {
					endLoc = getCurrentTile().getLocation();
				}
				currentCrewMan.getAi().addTravelAction(clickTile);
			} else if (o == btnPower) {
				getCurrentShip().getEngineering().attemptPowerSubSystem(clickTile.getItem());
				TilePane.createPanel();
			} else if (o == btnChooseTarget) {
				((Weapon) getCurrentTile().getItem()).setTargetTile(clickTile);
				TilePane.createPanel();
			} else if (btnRepair != null && o.getText().equals(btnRepair.getText())) {
				currentCrewMan.getAi().addRepairAction(clickTile);
			} else if (btnBreak != null && o.getText().equals(btnBreak.getText())) {
				currentCrewMan.getAi().addBreakAction(clickTile);
			} else if (btnTake != null && o.getText().equals(btnTake.getText())) {
				currentCrewMan.getAi().addTakeAction(clickTile);
			} else if (btnLoot != null && o.getText().equals(btnLoot.getText())) {
				currentCrewMan.getAi().addLootAction(clickTile);
			} else if (btnPlace != null && o.getText().equals(btnPlace.getText())) {
				currentCrewMan.getAi().addPlaceAction(clickTile);
			} else if (o == btnDismiss) {
				int response = JOptionPane.showConfirmDialog(this, "Dismiss this crewman forever?", "Confirm", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION);
				if (response == 0) {
					currentCrewMan.die();
				}
			} else if (o == btnCancel) {
				// removePopUpBox();
			} else if (selectBtns != null && selectBtns.contains(o)) {
				System.out.println("pressed select button");
				setCurrentTile(currentTile.getParentSystem().getStations().get(selectBtns.indexOf(o)));
			} else {
				// System.out.println("System specific button pressed, passing to system");
				if (popUpBox != null) {
					if (popUpBox.getParentSystem() != null) {
						popUpBox.getParentSystem().getButtonPress(o, popUpBox.getTile());
					}
				} else if (currentTilePane != null) {
					Tile tile = currentTilePane.getTile();
					if (tile != null) {
						tile.getParentSystem().getButtonPress(o, tile);
					}
				} else {
					System.out.println("A button was pressed that doesn't exist!");
				}
			}
			removePopUpBox();
		}
		this.requestFocus();
	}
	/**
	 * Zooms in and out by the set amount
	 */
	public void incZoom(int amount) {
		// System.out.println("Amount scrolled = "+ amount);
		int size = Constants.shipSquare - amount;
		if (size > 1 && size < 100) {
			Constants.shipSquare = size;
		}
		ShipFloorPlan fp = getCurrentFloorPlan();
		if (fp != null){
			fp.updateImage();
		}
	}
	/**
	 * bump the current floor up or down, adjusts based on current view
	 */
	public void bumpCurrentFloor(boolean positive) {
		// int changeFloor = 0;
		int floorMax = 0;
		floorMax = getCurrentShip().getFloorplans().size() - 1;
		if (positive) {
			if (currentFloor < floorMax) {
				setCurrentFloor(currentFloor + 1);
			} else {
				setCurrentFloor(0);
			}
		} else {
			if (currentFloor > 0) {
				setCurrentFloor(currentFloor - 1);
			} else {
				setCurrentFloor(floorMax);
			}
		}
	}
	/**
	 * Increases (positive == true) or decreases the current view horizontally
	 * (column == true) or vertically
	 * 
	 * @param horizontal
	 *            - bump x if true, y if false
	 * @param positive
	 *            - increase if true, decrease if false
	 */
	public void bumpView(boolean horizontal, boolean positive) {
		// System.out.println("Bump view");
		ShipFloorPlan fp = getCurrentShip().getFloorPlanAt(getCurrentFloor());
		if (horizontal == true) {
			if (positive == false) {
				fp.setShiftX(fp.getShiftX() - 1);
			} else {
				fp.setShiftX(fp.getShiftX() + 1);
			}
		} else {
			if (positive == false) {
				fp.setShiftY(fp.getShiftY() + 1);
			} else {
				fp.setShiftY(fp.getShiftY() - 1);
			}
		}
		fp.updateImage();
	}
	public void resetView() {
		ShipFloorPlan fp = getCurrentShip().getFloorPlanAt(getCurrentFloor());
		fp.setShiftX(0);
		fp.setShiftY(0);
		Constants.shipSquare = Constants.shipSquareOrig;
		fp.updateImage();
	}
	public int getCurrentFloor() {
		return currentFloor;
	}
	public void setCurrentFloor(int currentFloor) {
		if (currentFloor <= getCurrentShip().getFloorplans().size() - 1) {
			this.currentFloor = currentFloor;
		}
		// go ahead and update the floorplan's image
		getCurrentShip().getFloorPlanAt(getCurrentFloor()).updateImage();
	}
	public void initPanels() {
		// left
		leftPanel = new JPanel();
		leftPanel.setBounds(10, 50, borderSide - 20, Constants.viewScreenSize);
		leftPanel.setBackground(Color.black);
		leftPanel.setOpaque(false);
		leftPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		// right
		rightPanel = new JPanel();
		rightPanel.setBackground(Color.black);
		rightPanel.setOpaque(false);
		currentTilePane = new TilePane();
		currentTilePane.setOpaque(false);
		currentCrewPane = new CrewPane();
		currentCrewPane.setOpaque(false);
		this.add(leftPanel);
	}
	public CrewMan getCurrentCrewMan() {
		return currentCrewMan;
	}
	public void setCurrentCrewMan(CrewMan currentCrewMan) {
		this.currentCrewMan = currentCrewMan;
		// update crewman panel
		CrewPane.createPanel();
	}
	public Ship getCurrentShip() {
		return currentShip;
	}
	public void setCurrentShip(Ship currentShip) {
		this.currentShip = currentShip;
		if (getCurrentShip() != Home.getShip()) {
			btnTargetShip.setText("Internal Sensors");
		} else {
			btnTargetShip.setText("Targeted Ship");
		}
		// Make sure we dont' view a non-existant floor
		if (getCurrentFloor() >= getCurrentShip().getFloorplans().size()) {
			setCurrentFloor(getCurrentShip().getFloorplans().size() - 1);
		}
		// update the floorplan's image when we change ships
		if (currentShip.getSensors().getMaxSensorLevel() < Sensors.systemHealth) {
			Home.displayAir = false;
		}
		currentShip.getFloorPlanAt(getCurrentFloor()).updateImage();
	}
	public PopUpBox getPopUpBox() {
		return popUpBox;
	}
	public void setPopUpBox(PopUpBox popUpBox) {
		this.popUpBox = popUpBox;
	}
	/**
	 * Hides the popupBox and resets it to null
	 */
	// TODO - having an odd error sometimes when setting visibility to false =/
	public synchronized void removePopUpBox() {
		if (popUpBox != null && popUpBox.isVisible()) {
			popUpBox.setVisible(false);
			popUpBox = null;
		}
		clickTile = null;
		// currentTilePane.updatePane();
	}
	public TilePane getCurrentTilePane() {
		return currentTilePane;
	}
	public void setCurrentTilePane(TilePane currentTilePane) {
		this.currentTilePane = currentTilePane;
	}
	public Tile getCurrentTile() {
		return currentTile;
	}
	public void setCurrentTile(Tile currentTile) {
		this.currentTile = currentTile;
		TilePane.createPanel();
	}
	public Tile getClickTile() {
		return clickTile;
	}
	public void setClickTile(Tile clickTile) {
		this.clickTile = clickTile;
	}
	public ArrayList<TilePane> getActiveTiles() {
		return activeTiles;
	}
	public void setActiveTiles(ArrayList<TilePane> activeTiles) {
		this.activeTiles = activeTiles;
	}
	public CrewPane getCurrentCrewPane() {
		return currentCrewPane;
	}
	public void setCurrentCrewPane(CrewPane currentCrewPane) {
		this.currentCrewPane = currentCrewPane;
	}
	public ArrayList<CrewPane> getActiveCrew() {
		return activeCrew;
	}
	public void setActiveCrew(ArrayList<CrewPane> activeCrew) {
		this.activeCrew = activeCrew;
	}
	public ShipFloorPlan getCurrentFloorPlan() {
		if (getCurrentShip() != null) {
			return getCurrentShip().getFloorPlanAt(getCurrentFloor());
		} else {
			return null;
		}
	}
	/**
	 * Update's the image of the current floorplan image
	 */
	public void updateCurrentFloorPlanImage() {
		if (getCurrentFloorPlan() != null) {
			getCurrentFloorPlan().updateImage();
		}
	}
	public int getCurrentView() {
		return currentView;
	}
	public void setCurrentView(int currentScreen) {
		//update computer buttons if needed
		if (getCurrentView()  == viewComputer){
			removeComputerView();
		}
		if (currentScreen == viewComputer){
			buildComputerView();
		}
		this.currentView = currentScreen;
		
	}
	public BattleLocation getBackgroundLoc() {
		return Home.getPlayer().getCurrentLocation();
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		// System.out.println("Clicked");
		// mouseClick(e);
	}
	@Override
	public void mousePressed(MouseEvent e) {
		// System.out.println("pressed");
		mouseClick(e);
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		// System.out.println("released");
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		// System.out.println("entered");
	}
	@Override
	public void mouseExited(MouseEvent e) {
	}
	@Override
	public void mouseMoved(MouseEvent e) {
		// System.out.println("mouse moved");
	}
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		// System.out.println("Wheel station");
		incZoom(e.getWheelRotation());
	}
}
