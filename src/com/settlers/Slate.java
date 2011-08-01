package com.settlers;

import com.settlers.TextureManager.Background;
import com.settlers.UIButton.Type;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;

public class Slate extends View implements OnGestureListener,
		OnDoubleTapListener {

	public enum Action {
		NONE, TOWN, CITY, ROAD, ROBBER
	}

	private boolean initialized = false;
	private boolean buttonsPlaced = false;
	private int width, height;

	private Board board;
	private Player player;
	private TextureManager texture;
	private Game game;
	private int lastRoll;

	private GestureDetector gesture;

	private Geometry geometry;
	private Action action;

	private UIButton[] button;

	public Slate(Context context, AttributeSet attrs) {
		super(context, attrs);
		requestFocus();

		gesture = new GestureDetector(this);

		if (!initialized) {
			initialized = true;
			action = Action.NONE;

			geometry = new Geometry();

			// allocate and clear buttons
			// TODO: find better way
			button = new UIButton[UIButton.Type.values().length];
			for (int i = 0; i < button.length; i++)
				button[i] = null;
		}
	}

	public void setAction(Action action) {
		this.action = action;
		this.invalidate();
	}

	public Action getAction() {
		return action;
	}

	public void setState(Game game, Board board, Player player,
			TextureManager texture, int lastRoll) {
		this.texture = texture;
		this.board = board;
		this.player = player;
		this.game = game;
		this.lastRoll = lastRoll;
	}

	public void addButton(Type type) {
		for (int i = 0; i < button.length; i++) {
			if (button[i] != null)
				continue;

			button[i] = new UIButton(type, texture.get(type));
			buttonsPlaced = false;
			return;
		}
	}

	public void removeButtons() {
		for (int i = 0; i < button.length; i++)
			button[i] = null;
	}

	public void zoom(int x, int y) {
		geometry.setZoom(x, y, 3.0);
		invalidate();
	}

	public void zoom(Hexagon hexagon) {
		geometry.setZoom(hexagon, 3.0);
		invalidate();
	}

	public boolean unZoom() {
		if (geometry.isZoomed()) {
			geometry.setZoom(0.0, 0.0, 1.0);
			invalidate();
			return true;
		} else {
			return false;
		}
	}

	private void placeButtons() {
		// first button is always in the top left corner
		int x = 0, y = 0;

		for (int i = 0; i < button.length; i++) {
			if (button[i] == null)
				break;

			int endwidth = width - button[i].getWidth();
			int endheight = height - button[i].getHeight();

			// set position
			UIButton.Type type = button[i].getType();
			if (type == UIButton.Type.CANCEL || type == UIButton.Type.ROLL
					|| type == UIButton.Type.ENDTURN) {
				// set position to far right/bottom
				if (width < height)
					button[i].setPosition(endwidth, 0);
				else
					button[i].setPosition(0, endheight);
			} else {
				// set to next available position
				button[i].setPosition(x, y);

				// get next position
				if (height >= width) {
					// portrait
					int size = button[i].getWidth();
					x += size;
					if (x + size > endwidth) {
						x = 0;
						y += button[i].getHeight();
					}
				} else {
					// landscape
					int size = button[i].getHeight();
					y += size;
					if (y + size > endheight) {
						y = 0;
						x += button[i].getWidth();
					}
				}
			}
		}
	}

	public boolean cancel() {
		// TODO: cancel intermediate interactions

		if (geometry.isZoomed())
			return true;

		return ((board.isProduction() || board.isBuild()) && action != Action.NONE);
	}

	public boolean press(int x, int y) {
		boolean pressed = false;

		// consider buttons
		for (int i = 0; i < button.length; i++) {
			if (button[i] != null && button[i].press(x, y))
				pressed = true;
		}

		invalidate();
		return pressed;
	}

	public boolean release(int x, int y, boolean activate) {
		boolean released = false;

		// consider buttons
		for (int i = 0; i < button.length; i++) {
			if (button[i] == null)
				break;

			if (button[i].release(x, y) && activate) {
				Game game = (Game) this.getContext();
				game.buttonPress(button[i].getType());
				released = true;
			}
		}

		if (released)
			invalidate();

		return released;
	}

	public boolean translate(double dx, double dy) {
		if (!geometry.isZoomed())
			return false;

		geometry.translate(dx, dy);
		invalidate();
		return true;
	}

	public boolean click(int x, int y) {
		Game game = (Game) this.getContext();

		// consider buttons first
		if (release(x, y, true))
			return true;

		// ignore presses during non-human turns
		if (player == null)
			return false;

		// find starting corner of resource bar
		int startx = 0, starty = 0;
		if (height >= width)
			starty = height - geometry.getUnitSize();
		else
			startx = width - geometry.getUnitSize();

		// consider clicking resource bar
		if (x >= startx && y > starty) {
			if (action != Action.NONE)
				return false;

			int width = geometry.getWidth();
			int height = geometry.getHeight();
			if (height >= width)
				return game.clickResource(Hexagon.TYPES.length * x / width);
			else
				return game.clickResource(Hexagon.TYPES.length * y / height);
		}

		switch (action) {
		case NONE:
			return false;

		case ROBBER:
			// select a hexagon
			int select = geometry.getNearestHexagon(x, y);
			if (select >= 0) {
				game.select(action, board.getHexagon(select));
				return true;
			}

			break;

		case TOWN:
		case CITY:
			// select a vertex
			Vertex vertex = board.getVertex(geometry.getNearestVertex(x, y));
			if (vertex != null) {
				game.select(action, vertex);
				this.invalidate();
				return true;
			}
			break;

		case ROAD:
			// select an edge
			Edge edge = board.getEdge(geometry.getNearestEdge(x, y));
			if (edge != null) {
				game.select(action, edge);
				this.invalidate();
				return true;
			}
		}

		return false;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (!buttonsPlaced)
			placeButtons();

		texture.draw(Background.WAVES, canvas, geometry);

		// draw hexagon shore backdrops
		for (int i = 0; i < Hexagon.NUM_HEXAGONS; i++) {
			Hexagon hexagon = board.getHexagon(i);
			texture.draw(hexagon, canvas, geometry);
		}

		// draw hexagons
		for (int i = 0; i < Hexagon.NUM_HEXAGONS; i++) {
			Hexagon hexagon = board.getHexagon(i);
			texture.draw(hexagon, false, canvas, geometry, lastRoll);
		}

		boolean canBuild = false;

		// draw traders
		for (int i = 0; i < Trader.NUM_TRADER; i++)
			texture.draw(board.getTrader(i), canvas, geometry);

		// draw edges
		for (int i = 0; i < Edge.NUM_EDGES; i++) {
			Edge edge = board.getEdge(i);
			boolean build = action == Action.ROAD && player != null
					&& player.canBuild(edge);
			canBuild |= build;

			texture.draw(edge, build, canvas, geometry);
		}

		// draw vertices
		for (int i = 0; i < Vertex.NUM_VERTEX; i++) {
			Vertex vertex = board.getVertex(i);
			boolean town = player != null && action == Action.TOWN
					&& player.canBuild(vertex, Vertex.TOWN);
			boolean city = player != null && action == Action.CITY
					&& player.canBuild(vertex, Vertex.CITY);
			canBuild |= town | city;

			texture.draw(vertex, town, city, canvas, geometry);
		}

		// check if player is trying to build but can't
		if (player != null
				&& !canBuild
				&& (action == Action.ROAD || action == Action.TOWN || action == Action.CITY))
			game.cantBuild(action);

		// draw the buttons
		for (int i = 0; i < button.length; i++) {
			if (button[i] == null)
				break;

			texture
					.draw(button[i], board.getCurrentPlayer().getColor(),
							canvas);
		}

		// don't draw resource bar for non-human players
		if (player == null)
			return;

		int playerColor = TextureManager.getColor(player.getColor());
		int darkPlayerColor = TextureManager.darken(playerColor, 0.5);
		int greyTransparent = Color.argb(0xC0, 0x60, 0x60, 0x60);
		int goldColor = Color.argb(0xFF, 0xD1, 0xB0, 0x60);

		// draw info box backdrop with border
		int startx, starty;
		int size = texture.getIconSize();
		int space = geometry.getMinimalSize();
		int increment = space / Hexagon.TYPES.length;

		Paint paint = new Paint();
		paint.setColor(greyTransparent);
		paint.setStrokeWidth(1);

		if (height >= width) {
			// draw at bottom for landscape mode
			startx = increment / 12;
			starty = height - size;

			canvas.drawRect(0, starty, width, height, paint);

			paint.setColor(goldColor);
			canvas.drawLine(0, starty + 1, width, starty + 1, paint);
			canvas.drawLine(1, starty + 1, 1, height, paint);
			canvas.drawLine(width - 2, starty + 1, width - 2, height, paint);

			texture.draw(TextureManager.Location.TOP_LEFT, 0, starty, canvas);
			texture.draw(TextureManager.Location.TOP_RIGHT, width, starty,
					canvas);
		} else {
			// draw at the right in portrait mode
			startx = width - size;
			starty = increment / 12;

			canvas.drawRect(startx, 0, width, height, paint);

			paint.setColor(goldColor);
			canvas.drawLine(startx + 1, 0, startx + 1, height, paint);
			canvas.drawLine(startx + 1, 1, width, 1, paint);
			canvas.drawLine(startx + 1, height - 2, width, height - 2, paint);

			texture.draw(TextureManager.Location.TOP_LEFT, startx, 0, canvas);
			texture.draw(TextureManager.Location.BOTTOM_LEFT, startx, height,
					canvas);
		}

		// configure drawing settings
		paint.setTextSize(size / 2);
		paint.setSubpixelText(true);
		paint.setAntiAlias(true);
		paint.setStrokeCap(Paint.Cap.ROUND);

		// print amount of each resource
		for (int i = 0; i < Hexagon.TYPES.length; i++) {
			int num = player.getResources(Hexagon.TYPES[i]);

			// draw resource
			Bitmap bitmap = texture.get(Hexagon.TYPES[i]);
			canvas.drawBitmap(bitmap, startx, starty, null);

			// determine location for text
			int qx = startx + size * 2 / 3;
			int qy = starty + size;
			int length = 1;

			if (num >= 10) {
				qx -= size / 4;
				length = size / 3;
			}

			// backdrop
			int offset = size / 8;

			paint.setColor(Color.BLACK);
			paint.setStrokeWidth(size / 2 + 4);
			canvas.drawLine(qx + offset, starty + size * 3 / 4, qx + offset
					+ length, starty + size * 3 / 4, paint);

			paint.setColor(darkPlayerColor);
			paint.setStrokeWidth(size / 2);
			canvas.drawLine(qx + offset, starty + size * 3 / 4, qx + offset
					+ length, starty + size * 3 / 4, paint);

			// print quantity
			paint.setColor(Color.WHITE);
			canvas.drawText(Integer.toString(num), qx, qy - size / 12, paint);

			// increment position
			if (height >= width)
				startx += increment;
			else
				starty += increment;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		width = MeasureSpec.getSize(widthMeasureSpec);
		height = MeasureSpec.getSize(heightMeasureSpec);
		setMeasuredDimension(width, height);

		geometry.setSize(width, height);
		geometry.setRealSize(getContext().getResources().getDisplayMetrics(),
				texture.getSmallTileSize());
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// handle touch events in GestureDetector
		return gesture.onTouchEvent(event);
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distX,
			float distY) {
		// throw out button press if scrolling over a button
		release((int) e2.getX(), (int) e2.getY(), false);

		// shift the board
		return translate(distX, distY);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
		// throw out button press if scrolling over a button
		release((int) e2.getX(), (int) e2.getY(), false);

		// ignore flings
		return false;
	}

	@Override
	public boolean onDown(MotionEvent event) {
		// press down (consider activating buttons)
		press((int) event.getX(), (int) event.getY());

		// always return true to allow gestures to register
		return true;
	}

	@Override
	public void onShowPress(MotionEvent event) {
		// this is handled already in onDown
	}

	@Override
	public boolean onSingleTapUp(MotionEvent event) {
		// handle in onSingleTapConfirmed
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent event) {
		// button click
		return release((int) event.getX(), (int) event.getY(), true);
	}

	@Override
	public void onLongPress(MotionEvent event) {
		// TODO: long press resource to trade for it

		// consider a click on the board
		if (click((int) event.getX(), (int) event.getY())) {
			Vibrator vibrator = (Vibrator) getContext().getSystemService(
					Context.VIBRATOR_SERVICE);

			if (Options.vibrateLong())
				vibrator.vibrate(50);
		}
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent event) {
		// ignore intermediate events triggered in a double tap
		return false;
	}

	@Override
	public boolean onDoubleTap(MotionEvent event) {
		// double top zooms to point or zooms out
		if (geometry.isZoomed())
			unZoom();
		else
			zoom((int) event.getX(), (int) event.getY());

		return true;
	}
}
