package org.iceburg.home.main;

import java.awt.event.KeyEvent;

import org.iceburg.home.ship.Ship;
import org.iceburg.home.ship.Tile;
import org.iceburg.home.ship.systems.Sensors;
import org.iceburg.home.ship.systems.Warp;
import org.iceburg.home.story.BattleLocation;
import org.iceburg.home.ui.GameScreen;

public class Controls {
	static boolean rightP, leftP;
	static int lastSpeed, currentTurn;

	// public static int navigation=1, internalSensors=2, mainComputer=3;
	public static void keyPressed(KeyEvent e) {
		GameScreen screen = Home.getCurrentScreen();
		if (screen.getCurrentView() == GameScreen.viewExternal) {
			keyPressedNav(e);
		} else if (screen.getCurrentView() == GameScreen.viewInternal) {
			keyPressedInternalSensors(e);
		} else if (screen.getCurrentView() == GameScreen.viewComputer) {
			System.out.println("Comp Button pressed, but no listner");
		} else {
			System.out.println("Button pressed on unknown screen type");
		}
	}
	public static void keyPressedNav(KeyEvent e) {
		int key = e.getKeyCode();
		Warp ship = Home.getShip().getWarp();
		if (key == KeyEvent.VK_LEFT) {
			leftP = true;
		}
		if (key == KeyEvent.VK_RIGHT) {
			rightP = true;
			// System.out.println("Bearing "+ bearing);
		}
		if (key == KeyEvent.VK_UP) {
			Home.getShip().getWarp().attemptSpeed(Home.getShip().getWarp().getSpeed() + 1);
		}
		if (key == KeyEvent.VK_DOWN) {
			Home.getShip().getWarp().attemptSpeed(Home.getShip().getWarp().getSpeed() - 1);
		}
		if (key == KeyEvent.VK_BACK_SPACE) {
			if (ship.getSpeed() > 0) {
				lastSpeed = (int) ship.getSpeed();
				ship.attemptSpeed(0);
			} else {
				ship.setSpeed(lastSpeed);
			}
			// System.out.println("All Stop");
		}
		// TODO Reset Ship Test
		if (key == KeyEvent.VK_ENTER) {
			for (int i = 0; i < Home.getBattleLoc().getShips().size(); i++) {
				Ship s = Home.getBattleLoc().getShips().get(i);
				s.getWarp().setXpos(10 * i);
				s.getWarp().setYpos(10 * i);
			}
		} else if (key == KeyEvent.VK_ESCAPE) {
			Home.setPaused(!Home.isPaused());
		} else if (key == KeyEvent.VK_NUMPAD4) {
			Home.creativeMode = !Home.creativeMode;
			String s = "off.";
			if (Home.creativeMode) {
				s = "on.";
			}
			Home.getCurrentScreen().updateCurrentFloorPlanImage();
			Home.messagePanel.addMessage("Creative mode is now " + s);
		} else if (key == KeyEvent.VK_NUMPAD5) {
			Home.getPlayer().setCurrentLocation(new BattleLocation());
		}
		if (key >= 0x30 && key < 0x40) {
			if (key == KeyEvent.VK_0) {
				Home.getShip().getWarp().attemptSpeed(10);
			} else if (key == KeyEvent.VK_1) {
				Home.getShip().getWarp().attemptSpeed(1);
			} else if (key == KeyEvent.VK_2) {
				Home.getShip().getWarp().attemptSpeed(2);
			} else if (key == KeyEvent.VK_3) {
				Home.getShip().getWarp().attemptSpeed(3);
			} else if (key == KeyEvent.VK_4) {
				Home.getShip().getWarp().attemptSpeed(4);
			} else if (key == KeyEvent.VK_5) {
				Home.getShip().getWarp().attemptSpeed(5);
			} else if (key == KeyEvent.VK_6) {
				Home.getShip().getWarp().attemptSpeed(6);
			} else if (key == KeyEvent.VK_7) {
				Home.getShip().getWarp().attemptSpeed(7);
			} else if (key == KeyEvent.VK_8) {
				Home.getShip().getWarp().attemptSpeed(8);
			} else if (key == KeyEvent.VK_9) {
				Home.getShip().getWarp().attemptSpeed(9);
			}
			// System.out.println("Speed: "+ speed);
		}
	}
	public static void keyPressedInternalSensors(KeyEvent e) {
		// System.out.println("Internal Sensors");
		int key = e.getKeyCode();
		Warp ship = Home.getShip().getWarp();
		// weapons view adjust floor level for target ship
		GameScreen currentScreen = (GameScreen) Home.getCurrentScreen();
		if (key == KeyEvent.VK_UP) {
			currentScreen.bumpView(false, true);
		}
		if (key == KeyEvent.VK_DOWN) {
			currentScreen.bumpView(false, false);
		}
		if (key == KeyEvent.VK_RIGHT) {
			currentScreen.bumpView(true, true);
		}
		if (key == KeyEvent.VK_LEFT) {
			currentScreen.bumpView(true, false);
		}
		if (key == KeyEvent.VK_ENTER) {
			currentScreen.resetView();
		}
		if (key == KeyEvent.VK_BACK_SPACE) {
			if (ship.getSpeed() > 0) {
				lastSpeed = (int) ship.getSpeed();
				ship.attemptSpeed(0);
			} else {
				ship.setSpeed(lastSpeed);
			}
			// System.out.println("All Stop");
		}
		// // Reset Ship Test
		// if (key == KeyEvent.VK_ENTER) {
		// Home.ship.travel = new Travel(Constants.spawnLoc, Home.getShip(),
		// 10, 10);
		// }
		// TODO - use these to change the floor, create a floor change method
		if (key >= 0x30 && key < 0x40) {
			if (key == KeyEvent.VK_0) {
				currentScreen.setCurrentFloor(0);
			}
			if (key == KeyEvent.VK_1) {
				currentScreen.setCurrentFloor(1);
			}
			if (key == KeyEvent.VK_2) {
				currentScreen.setCurrentFloor(2);
			}
			if (key == KeyEvent.VK_3) {
				currentScreen.setCurrentFloor(3);
			}
			if (key == KeyEvent.VK_4) {
				currentScreen.setCurrentFloor(4);
			}
			if (key == KeyEvent.VK_5) {
				currentScreen.setCurrentFloor(5);
			}
			if (key == KeyEvent.VK_6) {
				currentScreen.setCurrentFloor(6);
			}
			if (key == KeyEvent.VK_7) {
				currentScreen.setCurrentFloor(7);
			}
			if (key == KeyEvent.VK_8) {
				currentScreen.setCurrentFloor(8);
			}
			if (key == KeyEvent.VK_9) {
				currentScreen.setCurrentFloor(9);
			}
			// System.out.println("Speed: "+ speed);
		} else if (key == KeyEvent.VK_NUMPAD0) {
			if (!Home.displayAir
					&& Home.getCurrentScreen().getCurrentShip().getSensors().getMaxSensorLevel() >= Sensors.systemHealth) {
				Home.displayAir = true;
			} else {
				Home.displayAir = false;
			}
			Home.getCurrentScreen().updateCurrentFloorPlanImage();
			String s = "Not displaying air levels.";
			if (Home.displayAir) {
				s = "Displaying air levels.";
			}
			Home.messagePanel.addMessage(s);
		} else if (key == KeyEvent.VK_NUMPAD1) {
			Home.displayCrew = !Home.displayCrew;
			String s = "Not displaying costs";
			if (Home.displayCrew) {
				s = "Displaying Tile Costs";
			}
			Home.messagePanel.addMessage(s);
		} else if (key == KeyEvent.VK_NUMPAD2) {
			Home.displayAlt = !Home.displayAlt;
			Home.getCurrentScreen().updateCurrentFloorPlanImage();
		} else if (key == KeyEvent.VK_NUMPAD3) {
			if (Home.getCurrentScreen().getCurrentTile() != null) {
				if (Home.getCurrentScreen().getCurrentTile().getHealth() != 0) {
					Home.getCurrentScreen().getCurrentTile().setHealth(0);
				} else {
					Home.getCurrentScreen().getCurrentTile().setHealth(100);
				}
			} else if (Home.getCurrentScreen().getCurrentCrewMan() != null) {
				Home.getCurrentScreen().getCurrentCrewMan().incHealthCurrent(-1);
			}
			Home.messagePanel.addMessage("Changed tile (or crewman) health ");
		} else if (key == KeyEvent.VK_NUMPAD4) {
			Home.creativeMode = !Home.creativeMode;
			String s = "off.";
			if (Home.creativeMode) {
				s = "on.";
			}
			Home.getCurrentScreen().updateCurrentFloorPlanImage();
			Home.messagePanel.addMessage("Creative mode is now " + s);
		} else if (key == KeyEvent.VK_SPACE) {
			if (Home.getCurrentScreen().getCurrentShip() == Home.getShip()) {
				Ship target = Home.getShip().getHelm().getTargetShip().getParentShip();
				if (target != null) {
					Home.getCurrentScreen().setCurrentShip(target);
				} else {
					Home.messagePanel.addMessage("No target ship!");
				}
			} else {
				Home.getCurrentScreen().setCurrentShip(Home.getShip());
			}
		} else if (key == KeyEvent.VK_ESCAPE) {
			Home.setPaused(!Home.isPaused());
			
		} else if (key == KeyEvent.VK_NUMPAD5) {
			Home.displayGrid = !Home.displayGrid;
			String s = "Not displaying Grid.";
			if (Home.creativeMode) {
				s = "Displaying Grid.";
			}
			Home.getCurrentScreen().updateCurrentFloorPlanImage();
			Home.messagePanel.addMessage(s);
		} else if (key == KeyEvent.VK_NUMPAD6) {
			Home.getSoundManager().pause();
		} else if (key == KeyEvent.VK_HOME) {
			Home.getCurrentScreen().bumpCurrentFloor(true);
		} else if (key == KeyEvent.VK_END) {
			Home.getCurrentScreen().bumpCurrentFloor(false);
		} else if (key == KeyEvent.VK_NUMPAD8) {
			Tile t = Home.getCurrentScreen().getCurrentTile();
			if (t != null && t.getItem() != null) {
				t.getItem().setMaxPower(t.getItem().getMaxPower() + 1);
			}
			Home.messagePanel.addMessage("Increased current Item's max power");
		} else if (key == KeyEvent.VK_NUMPAD7) {
			Tile t = Home.getCurrentScreen().getCurrentTile();
			if (t != null && t.getItem() != null) {
				t.getItem().setMaxPower(t.getItem().getMaxPower() - 1);
			}
			Home.messagePanel.addMessage("Decreased current Item's max power");
		} else if (key == KeyEvent.VK_NUMPAD9) {
			Home.processAI = !Home.processAI;
			String s = "off.";
			if (Home.processAI) {
				s = "on.";
			}
			Home.messagePanel.addMessage("Process AI is now " + s);
		}
	}
	public static void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_LEFT) {
			leftP = false;
		}
		if (key == KeyEvent.VK_RIGHT) {
			rightP = false;
		}
		// if (key == KeyEvent.VK_UP) {
		// upP = false;
		// }
		//
		// if (key == KeyEvent.VK_DOWN) {
		// downP = false;
		// }
	}
	public static void update() {
		Warp ship = Home.getShip().getWarp();
		if (rightP || leftP) {
			currentTurn -= 1;
			if (currentTurn <= 0) {
				currentTurn = 10 - ship.getMaxTurn();
				if (rightP) {
					ship.setBearing(ship.getBearing() + 1);
					if (ship.getBearing() >= 360) {
						ship.setBearing(ship.getBearing() - 360);
					}
				} else {
					if (ship.getBearing() <= 0) {
						ship.setBearing(ship.getBearing() + 360);
					}
					ship.setBearing(ship.getBearing() - 1);
				}
			}
		}
	}
}
