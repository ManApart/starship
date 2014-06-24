package org.iceburg.home.items;

import java.awt.Image;

public class Door extends Item {
	// public boolean open;
	// image to be used when the door is open
	Image openImage;
	String openImagePath;

	public Image getOpenImage() {
		return openImage;
	}
	public void setOpenImage(Image openImage) {
		this.openImage = openImage;
	}
	public String getOpenImagePath() {
		return openImagePath;
	}
	public void setOpenImagePath(String openImagePath) {
		this.openImagePath = openImagePath;
	}
}
