package org.iceburg.home.sound;

import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.iceburg.home.main.Home;

public class Sound {
	String path;

	public Sound(String path) {
		path = "org/iceburg/home/data/sound/" + path;
		this.path = path;
	}
	private AudioInputStream createStream() {
		URL url = Home.resources.getClass().getClassLoader().getResource(path);
		AudioInputStream audio = null;
		if (url != null) {
			try {
				audio = AudioSystem.getAudioInputStream(url);
			} catch (UnsupportedAudioFileException e) {
				e.printStackTrace();
				System.out.println("Sound file at '" + path + "' is null!");
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Sound file at '" + path + "' is null!");
			}
		} else {
			System.out.println("Sound file at '" + path + "' is null!");
		}
		return audio;
	}
	/**
	 * Play this sound
	 */
	public void play() {
		play(1);
	}
	/**
	 * Play this sound, times amount of times
	 */
	public void play(int times) {
		Home.getSoundManager().play(this, times);
	}
	public String getPath() {
		return path;
	}
	/**
	 * Return's the song's file name
	 * 
	 * @return
	 */
	public String getName() {
		int i = 1 + path.lastIndexOf("/");
		String s = path.substring(i);
		return s;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public AudioInputStream getAudio() {
		return createStream();
	}
	public boolean isWav() {
		if (this != null) {
			return getPath().endsWith(".wav");
		}
		return false;
	}
	public boolean isMp3() {
		if (this != null) {
			return getPath().endsWith(".mp3");
		}
		return false;
	}
}
