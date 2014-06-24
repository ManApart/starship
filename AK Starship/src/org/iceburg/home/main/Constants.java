package org.iceburg.home.main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;

public class Constants {
	public static Color colorBackground = Color.decode("#666665");
	// warp speed in light years per year
	public static double warp1 = 0.1,
			warp2 = 1,
			warp3 = 2,
			warp4 = 4,
			warp5 = 8,
			warp6 = 16,
			warp7 = 25,
			warp8 = 35,
			warp9 = 50,
			warp10 = 60,
			warpFactors[] = new double[] { warp1, warp2, warp3, warp4, warp5, warp6, warp7, warp8, warp9, warp10 };
	public static double impulse = 0.25;
	// year,month,day,hour,min,second,milisecond
	public static int origionalDate[] = new int[] { 3000, 0, 0, 0, 0, 0, 0 };
	// Screen data
	public static Rectangle messageCenterArea = new Rectangle(35, 615, 730, 70);
	public static Dimension windowSize = new Dimension(800, 700);
	// Scales
	public static int viewScreenSize = 500, shipSquare = 10, shipSquareOrig = 10,
			arenaSize = 2500;
	// Time Conversion
	// 365*24*60*60*100
	// TODO last number should be 100, but not working with 100
	public static float yearToMinute = 525600, minuteToGameTick = 6000;
	// Misc
	public static int drainRate = 10;
}
