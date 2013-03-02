package com.settlers.hd;

public class UIButton {

	public enum Type {
		INFO, TRADE, ROLL, ROAD, TOWN, CITY, DEVCARD, CANCEL, ENDTURN
	}
	
	public enum Background {
		BACKDROP, PRESSED, ACTIVATED
	}

	private Type type;
	private int bitmap;

	private int x;
	private int y;
	private int width;
	private int height;
	private boolean pressed;
	private boolean enabled;

	public UIButton(Type type, int width, int height) {
		this.type = type;
		this.x = 0;
		this.y = 0;
		this.width = width;
		this.height = height;

		pressed = false;
		enabled = false;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
	public int getResource() {
		return bitmap;
	}
	
	public boolean isPressed() {
		return pressed;
	}

	public Type getType() {
		return type;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isWithin(int x, int y) {
		x += width / 2;
		y += height / 2;
		return (x > this.x && x < this.x + width && y > this.y && y < this.y
				+ height);
	}

	public boolean press(int x, int y) {
		if (!enabled)
			return false;

		pressed = isWithin(x, y);
		return pressed;
	}

	public boolean release(int x, int y) {
		if (!pressed || !enabled)
			return false;

		pressed = false;
		return isWithin(x, y);
	}
}
