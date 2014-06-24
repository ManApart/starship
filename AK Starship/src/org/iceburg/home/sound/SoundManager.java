package org.iceburg.home.sound;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import org.iceburg.home.main.Home;

//Credit to JavaZoom's JLayer Player! http://www.javazoom.net/javalayer/javalayer.html
public class SoundManager {
	public Sound themeSong, themeSong2, currentSong;
	private AdvancedPlayer currentPlayer;
	int position;
	boolean paused, loop;

	public SoundManager() {

	}
	/**
	 * Play's the background track - this sound is looped until another song is
	 * played
	 */
	public void playSong(Sound sound) {
		stop(currentSong);
		this.currentSong = sound;
		loop = true;
		play(sound);
		Home.messagePanel.addMessage("Playing: " + sound.getName());
	}
	/**
	 * Play a wav or mp3 sound once
	 */
	public void play(Sound sound) {
		play(sound, 1);
	}
	/**
	 * Play a wav or mp3 sound, times amount of times
	 * 
	 */
	public void play(Sound sound, int times) {
		if (sound != null) {
			if (sound.isWav()) {
				playWav(sound, times);
			} else if (sound.isMp3()) {
				playMp3(sound);
			} else {
				System.out.println("Unkown song type: " + sound.getPath());
			}
		} else {
			System.out.println("Song does not exist");
		}
	}
	private void playWav(Sound sound, int times) {
		times -= 1;
		// Get a sound clip resource.
		try {
			Clip clip = AudioSystem.getClip();
			// Open audio clip and load samples from the audio input stream.
			clip.open(sound.getAudio());
			clip.loop(times);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
	// TODO pause and play lines //play new music at each battle location
	private void playMp3(Sound sound) {
		try {
			InputStream fis = Home.resources.getClass().getClassLoader().getResourceAsStream(sound.getPath());
			BufferedInputStream bis = new BufferedInputStream(fis);
			currentPlayer = new AdvancedPlayer(bis);
		} catch (Exception e) {
			System.out.println("Problem playing file " + sound.getPath());
			System.out.println(e);
		}
		// run in new thread to play in background
		new Thread() {
			public void run() {
				try {
					currentPlayer.setPlayBackListener(new PlaybackListener() {
						@Override
						public void playbackFinished(PlaybackEvent event) {
							if (event.getId() == event.STOPPED) {
								// System.out.println("stop");
								position = event.getFrame();
							} else {
								// System.out.println("go");
							}
							// if we have a background track, play it again
							if (loop) {
								play(currentSong);
							}
						}
					});
					currentPlayer.play(position, Integer.MAX_VALUE);
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		}.start();
	}
	/**
	 * Pauses/ resumes the background music
	 */
	// TODO - currently mutes but does not pause, position seems to not be
	// setting correctly
	public void pause() {
		if (currentSong != null && currentSong.isMp3()) {
			if (paused) {
				loop = true;
				playMp3(currentSong);
			} else {
				loop = false;
				currentPlayer.stop();
			}
			paused = !paused;
		}
	}
	public void stop(Sound sound) {
		if (sound != null) {
			if (sound.isWav()) {
				try {
					AudioSystem.getClip().stop();
				} catch (LineUnavailableException e) {
					e.printStackTrace();
				}
			} else if (sound.isMp3()) {
				loop = false;
				currentPlayer.stop();
				position = 0;
			} else {
				System.out.println("Unkown song type: " + sound.getPath());
			}
		} else {
			// System.out.println("Song does not exist");
		}
	}
}
