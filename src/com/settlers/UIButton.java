package com.settlers;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class UIButton {

	public enum Type {
		INFO, ROLL, ROAD, TOWN, CITY, DEVCARD, TRADE, ENDTURN, CANCEL
	}
	
	public enum Background {
		BACKDROP, PRESSED, ACTIVATED
	}

	private Type type;
	private Bitmap bitmap;

	private int x;
	private int y;
	private int width;
	private int height;
	private boolean pressed;
	private boolean enabled;

	/**
	 * Initialize button
	 * 
	 * @param type
	 *            the type
	 * @param bitmap
	 *            the bitmap
	 */
	public UIButton(Type type, Bitmap bitmap) {
		this.type = type;
		this.bitmap = bitmap;
		this.x = 0;
		this.y = 0;

		height = bitmap.getHeight();
		width = bitmap.getWidth();
		pressed = false;
		enabled = true;
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

	public Bitmap getBitmap() {
		return bitmap;
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

	/**
	 * Draw the button
	 * 
	 * @param canvas
	 *            the canvas to draw on
	 * @param background
	 *            button background (or null)
	 * @param highlight
	 *            button selection highlight overlay (or null)
	 * @param disable
	 *            button disable overlay (or null)
	 */
	public void draw(Canvas canvas, Bitmap background, Bitmap highlight,
			Bitmap disable) {
		if (background != null)
			canvas.drawBitmap(background, x, y, null);

		canvas.drawBitmap(bitmap, x, y, null);

		if (pressed && highlight != null)
			canvas.drawBitmap(highlight, x, y, null);

		if (!enabled && disable != null)
			canvas.drawBitmap(disable, x, y, null);
	}

	/**
	 * Draw the button
	 * 
	 * @param canvas
	 *            the canvas to draw on
	 * @param background
	 *            background (or null)
	 * @param highlight
	 *            selection highlight overlay (or null)
	 */
	public void draw(Canvas canvas, Bitmap background, Bitmap highlight) {
		draw(canvas, background, highlight, null);
	}

	/**
	 * Draw the button
	 * 
	 * @param canvas
	 *            the canvas to draw on
	 */
	public void draw(Canvas canvas) {
		draw(canvas, null, null, null);
	}

	/**
	 * Determine if a point is within the button
	 * 
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @return true if the given coordinate is within the button
	 */
	public boolean isWithin(int x, int y) {
		return (x > this.x && x < this.x + width && y > this.y && y < this.y
				+ height);
	}

	/**
	 * Try pressing the button
	 * 
	 * @param x
	 *            x position of click
	 * @param y
	 *            y position of click
	 * @return true if the button was pressed
	 */
	public boolean press(int x, int y) {
		if (!enabled)
			return false;

		pressed = isWithin(x, y);
		return pressed;
	}

	/**
	 * Release the button and check if it was clicked
	 * 
	 * @param x
	 *            x position of click
	 * @param y
	 *            y position of click
	 * @return true if the press was released on the button
	 */
	public boolean release(int x, int y) {
		if (!pressed || !enabled)
			return false;

		pressed = false;
		return isWithin(x, y);
	}
}
