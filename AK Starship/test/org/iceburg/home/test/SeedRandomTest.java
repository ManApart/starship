package org.iceburg.home.test;

import java.util.Random;

import javax.swing.JFrame;

public class SeedRandomTest {
	int hardSeed = 1000;
	Random generator = new Random(hardSeed);

	public SeedRandomTest() {
		for (int i = 0; i < 10; i++) {
			algorithmRand(i);
		}
	}
	public void algorithmRand(int seed) {
		// seed would be a argument, hardseed from config properties
		int i = 0;
		int genSeed = hardSeed + seed * 1000;
		// Random generator = new Random(genSeed);
		generator.setSeed(genSeed);
		double j = (generator.nextDouble() * (100));
		i = (int) j;
		System.out.println("num = " + i);
	}
}
