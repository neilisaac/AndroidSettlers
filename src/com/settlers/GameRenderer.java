package com.settlers;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.settlers.UIButton.Type;

import android.opengl.GLSurfaceView.Renderer;
import android.util.DisplayMetrics;

public class GameRenderer implements Renderer {

	public enum Action {
		NONE, TOWN, CITY, ROAD, ROBBER
	}

	private TextureManager texture;
	private Board board;
	private Player player;
	private GameActivity game;
	private int lastRoll;
	private Action action;

	private int width, height;

	private static Geometry geometry = null;
	private boolean buttonsPlaced = false;

	private UIButton[] button;

	public GameRenderer() {
		if (geometry == null)
			geometry = new Geometry();

		action = Action.NONE;
		buttonsPlaced = false;

		// allocate and clear buttons
		// TODO: find better way
		buttonsPlaced = false;
		button = new UIButton[UIButton.Type.values().length];
		for (int i = 0; i < button.length; i++)
			button[i] = null;
	}

	public void setState(GameActivity game, Board board, Player player,
			TextureManager texture, int lastRoll) {
		this.texture = texture;
		this.board = board;
		this.player = player;
		this.game = game;
		this.lastRoll = lastRoll;
	}

	public void setSize(DisplayMetrics screen, int width, int height) {
		geometry.setSize(screen.widthPixels, screen.heightPixels);
		this.width = width;
		this.height = height;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public Action getAction() {
		return action;
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
		geometry.zoomTo(translateScreenX(x), translateScreenY(y));
	}
	
	public boolean isZoomed() {
		return geometry.isZoomed();
	}

	public void unZoom() {
		geometry.reset();
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

		return pressed;
	}

	public boolean release(int x, int y, boolean activate) {
		boolean released = false;

		// consider buttons
		for (int i = 0; i < button.length; i++) {
			if (button[i] == null)
				break;

			if (button[i].release(x, y) && activate) {
				game.buttonPress(button[i].getType());
				released = true;
			}
		}

		return released;
	}

	public void translate(float dx, float dy) {
		int min = geometry.getMinimalSize();
		geometry.translate(2 * dx / min, -2 * dy / min);
	}

	public boolean click(int x, int y) {
		// consider buttons first
		if (release(x, y, true))
			return true;

		// ignore presses during non-human turns
		if (player == null)
			return false;

		// find starting corner of resource bar
		int startx = 0, starty = 0;
		int barsize = geometry.getMinimalSize() / 5;
		if (height >= width)
			starty = height - barsize;
		else
			startx = width - barsize;

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
		
		// translate to centered coordinates
		float px = translateScreenX(x);
		float py = translateScreenY(y);

		switch (action) {
		case NONE:
			return false;

		case ROBBER:
			// select a hexagon
			int select = geometry.getNearestHexagon(px, py);
			if (select >= 0) {
				game.select(action, board.getHexagon(select));
				return true;
			}

			break;

		case TOWN:
		case CITY:
			// select a vertex
			Vertex vertex = board.getVertex(geometry.getNearestVertex(px, py));
			if (vertex != null) {
				game.select(action, vertex);
				return true;
			}
			break;

		case ROAD:
			// select an edge
			Edge edge = board.getEdge(geometry.getNearestEdge(px, py));
			if (edge != null) {
				game.select(action, edge);
				return true;
			}
		}

		return false;
	}
	
	private float translateScreenX(int x) {
		int min = width < height ? width : height; 
		float factor = geometry.getZoom();
		return 2 * factor * (x - width / 2) / min;
	}

	private float translateScreenY(int y) {
		int min = width < height ? width : height;
		float factor = geometry.getZoom();
		return 2 * factor * (height / 2 - y) / min;
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		this.width = width;
		this.height = height;

		gl.glViewport(0, 0, width, height);

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		float aspect = (float) width / (float) height;
		if (width > height)
			gl.glOrthof(-aspect, aspect, -1, 1, 0.1f, 100f);
		else
			gl.glOrthof(-1, 1, -1 / aspect, 1 / aspect, 0.1f, 100f);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		gl.glShadeModel(GL10.GL_SMOOTH);

		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);

		gl.glEnable(GL10.GL_TEXTURE_2D);

		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		texture.initGL(gl);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		gl.glLoadIdentity();
		gl.glTranslatef(0, 0, -100);
		
		gl.glTranslatef(-geometry.getTranslateX(), -geometry.getTranslateY(), 0);
		gl.glScalef(geometry.getZoom(), geometry.getZoom(), 1f);

		// texture.draw(Background.WAVES, canvas, geometry);

		// draw hexagon shore backdrops
		for (int i = 0; i < Hexagon.NUM_HEXAGONS; i++) {
			Hexagon hexagon = board.getHexagon(i);
			texture.draw(hexagon, gl, geometry);
		}

		// draw hexagons
		for (int i = 0; i < Hexagon.NUM_HEXAGONS; i++) {
			Hexagon hexagon = board.getHexagon(i);
			texture.draw(hexagon, false, gl, geometry, lastRoll);
		}

		boolean canBuild = false;

		// draw traders
		for (int i = 0; i < Trader.NUM_TRADER; i++)
			texture.draw(board.getTrader(i), gl, geometry);

		// // draw edges
		// for (int i = 0; i < Edge.NUM_EDGES; i++) {
		// Edge edge = board.getEdge(i);
		// boolean build = action == Action.ROAD && player != null
		// && player.canBuild(edge);
		// canBuild |= build;
		//
		// texture.draw(edge, build, canvas, geometry);
		// }

		// draw vertices
		for (int i = 0; i < Vertex.NUM_VERTEX; i++) {
			Vertex vertex = board.getVertex(i);
			boolean town = player != null && action == Action.TOWN
					&& player.canBuild(vertex, Vertex.TOWN);
			boolean city = player != null && action == Action.CITY
					&& player.canBuild(vertex, Vertex.CITY);
			canBuild |= town | city;

//			texture.draw(vertex, town, city, gl, geometry);
		}

		// check if player is trying to build but can't
		if (player != null
				&& !canBuild
				&& (action == Action.ROAD || action == Action.TOWN || action == Action.CITY))
			game.cantBuild(action);

		// // draw the buttons
		// for (int i = 0; i < button.length; i++) {
		// if (button[i] == null)
		// break;
		//
		// texture
		// .draw(button[i], board.getCurrentPlayer().getColor(),
		// canvas);
		// }
		//
		// // don't draw resource bar for non-human players
		// if (player == null)
		// return;

		// int playerColor = TextureManager.getColor(player.getColor());
		// int darkPlayerColor = TextureManager.darken(playerColor, 0.5);
		// int greyTransparent = Color.argb(0xC0, 0x60, 0x60, 0x60);
		// int goldColor = Color.argb(0xFF, 0xD1, 0xB0, 0x60);
		//
		// // draw info box backdrop with border
		// int startx, starty;
		// int size = texture.getIconSize();
		// int space = geometry.getMinimalSize();
		// int increment = space / Hexagon.TYPES.length;

		// Paint paint = new Paint();
		// paint.setColor(greyTransparent);
		// paint.setStrokeWidth(1);
		//
		// if (height >= width) {
		// // draw at bottom for landscape mode
		// startx = increment / 12;
		// starty = height - size;
		//
		// canvas.drawRect(0, starty, width, height, paint);
		//
		// paint.setColor(goldColor);
		// canvas.drawLine(0, starty + 1, width, starty + 1, paint);
		// canvas.drawLine(1, starty + 1, 1, height, paint);
		// canvas.drawLine(width - 2, starty + 1, width - 2, height, paint);
		//
		// // texture.draw(TextureManager.Location.TOP_LEFT, 0, starty,
		// // canvas);
		// // texture.draw(TextureManager.Location.TOP_RIGHT, width, starty,
		// // canvas);
		// } else {
		// // draw at the right in portrait mode
		// startx = width - size;
		// starty = increment / 12;
		//
		// canvas.drawRect(startx, 0, width, height, paint);
		//
		// paint.setColor(goldColor);
		// canvas.drawLine(startx + 1, 0, startx + 1, height, paint);
		// canvas.drawLine(startx + 1, 1, width, 1, paint);
		// canvas.drawLine(startx + 1, height - 2, width, height - 2, paint);
		//
		// // texture.draw(TextureManager.Location.TOP_LEFT, startx, 0,
		// // canvas);
		// // texture.draw(TextureManager.Location.BOTTOM_LEFT, startx, height,
		// // canvas);
		// }
		//
		// // configure drawing settings
		// paint.setTextSize(size / 2);
		// paint.setSubpixelText(true);
		// paint.setAntiAlias(true);
		// paint.setStrokeCap(Paint.Cap.ROUND);
		//
		// // print amount of each resource
		// for (int i = 0; i < Hexagon.TYPES.length; i++) {
		// int num = player.getResources(Hexagon.TYPES[i]);
		//
		// // draw resource
		// Bitmap bitmap = texture.get(Hexagon.TYPES[i]);
		// canvas.drawBitmap(bitmap, startx, starty, null);
		//
		// // determine location for text
		// int qx = startx + size * 2 / 3;
		// int qy = starty + size;
		// int length = 1;
		//
		// if (num >= 10) {
		// qx -= size / 4;
		// length = size / 3;
		// }
		//
		// // backdrop
		// int offset = size / 8;
		//
		// paint.setColor(Color.BLACK);
		// paint.setStrokeWidth(size / 2 + 4);
		// canvas.drawLine(qx + offset, starty + size * 3 / 4, qx + offset
		// + length, starty + size * 3 / 4, paint);
		//
		// paint.setColor(darkPlayerColor);
		// paint.setStrokeWidth(size / 2);
		// canvas.drawLine(qx + offset, starty + size * 3 / 4, qx + offset
		// + length, starty + size * 3 / 4, paint);
		//
		// // print quantity
		// paint.setColor(Color.WHITE);
		// canvas.drawText(Integer.toString(num), qx, qy - size / 12, paint);
		//
		// // increment position
		// if (height >= width)
		// startx += increment;
		// else
		// starty += increment;
		// }

	}
}
