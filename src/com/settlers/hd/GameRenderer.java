package com.settlers.hd;

import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.settlers.hd.UIButton.Type;

import android.graphics.Bitmap;
import android.opengl.GLSurfaceView.Renderer;
import android.util.DisplayMetrics;
import android.util.Log;

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

	private static final float[] backgroundColors = { 0, 0.227f, 0.521f, 1,
			0.262f, 0.698f, 0.878f, 1, 0, 0.384f, 0.600f, 1, 0.471f, 0.875f,
			1f, 1 };

	private Square background;
	private GameView view;

	public GameRenderer(GameView gameView) {
		view = gameView;
		
		if (geometry == null)
			geometry = new Geometry();

		action = Action.NONE;
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

	public void zoom(int x, int y) {
		geometry.zoomTo(translateScreenX(x), translateScreenY(y));
	}

	public void zoomBy(float factor) {
		geometry.setZoom(geometry.getZoom() * factor);
	}

	public boolean isZoomed() {
		return geometry.isZoomed();
	}

	public void unZoom() {
		geometry.reset();
	}


	public boolean cancel() {
		// TODO: cancel intermediate interactions

		if (geometry.isZoomed())
			return true;

		return ((board.isProduction() || board.isBuild()) && action != Action.NONE);
	}


	public void translate(float dx, float dy) {
		int min = geometry.getMinimalSize();
		geometry.translate(2 * dx / min, -2 * dy / min);
	}

	public boolean click(int x, int y) {
		// ignore presses during non-human turns
		if (player == null)
			return false;

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
		float min = (width < height ? width : height) / 2f;
		return (float) ((x - width / 2) / min + geometry.getTranslateX())
				/ geometry.getZoom();
	}

	private float translateScreenY(int y) {
		float min = (width < height ? width : height) / 2f;
		return (float) ((height / 2 - y) / min + geometry.getTranslateY())
				/ geometry.getZoom();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		this.width = width;
		this.height = height;

		float aspect = (float) width / (float) height;
		if (width > height)
			background = new Square(backgroundColors, 0, 0, 0, 2 * aspect, 2);
		else
			background = new Square(backgroundColors, 0, 0, 0, 2, 2 / aspect);

		gl.glViewport(0, 0, width, height);
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
		gl.glBlendFunc(GL10.GL_DST_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		texture.initGL(gl);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		gl.glColor4f(1, 1, 1, 1);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		float aspect = (float) width / (float) height;
		if (width > height)
			gl.glOrthof(-aspect, aspect, -1, 1, 0.1f, 40f);
		else
			gl.glOrthof(-1, 1, -1 / aspect, 1 / aspect, 0.1f, 40f);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();

		gl.glTranslatef(0, 0, -30);

		// draw background without transformation
		background.render(gl);

		gl.glTranslatef(-geometry.getTranslateX(), -geometry.getTranslateY(), 0);
		gl.glScalef(geometry.getZoom(), geometry.getZoom(), 1);

		// draw the hexangons with backdrop
		for (int i = 0; i < Hexagon.NUM_HEXAGONS; i++)
			texture.draw(board.getHexagon(i), gl, geometry);

		// draw the roll numbers, robber, and highlighting
		for (int i = 0; i < Hexagon.NUM_HEXAGONS; i++)
			texture.draw(board.getHexagon(i), gl, geometry, lastRoll);

		// draw traders
		for (int i = 0; i < Trader.NUM_TRADER; i++)
			texture.draw(board.getTrader(i), gl, geometry);

		boolean canBuild = false;

		// draw edges
		for (int i = 0; i < Edge.NUM_EDGES; i++) {
			Edge edge = board.getEdge(i);
			boolean build = action == Action.ROAD && player != null
					&& player.canBuild(edge);
			canBuild |= build;

			if (build || edge.getOwner() != null)
				texture.draw(edge, build, gl, geometry);
		}

		// draw vertices
		for (int i = 0; i < Vertex.NUM_VERTEX; i++) {
			Vertex vertex = board.getVertex(i);
			boolean town = player != null && action == Action.TOWN
					&& player.canBuild(vertex, Vertex.TOWN);
			boolean city = player != null && action == Action.CITY
					&& player.canBuild(vertex, Vertex.CITY);
			canBuild |= town | city;

			texture.draw(vertex, town, city, gl, geometry);
		}

		// check if player is trying to build but can't
		if (player != null
				&& !canBuild
				&& (action == Action.ROAD || action == Action.TOWN || action == Action.CITY)) {
			game.cantBuild(action);
		}

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrthof(0, width, 0, height, 0.1f, 40f);
		gl.glTranslatef(0, 0, -30);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();

		// draw the buttons
		view.placeButtons(width, height);

		synchronized (view.buttons) {
			for (UIButton button : view.buttons) {
				texture.draw(button, gl);
			}
		}

		// don't draw resource bar for non-human players
		if (player == null)
			return;

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
