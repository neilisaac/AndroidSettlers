package com.settlers;

import java.util.Hashtable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.BitmapFactory.Options;

public class TextureManager {

	private enum Type {
		NONE, BACKGROUND, BUTTON, BUTTONBG, TILE, ROLL, ROBBER, RESOURCE, TRADER, ROAD, TOWN, CITY, ORNAMENT
	}

	public enum Location {
		BOTTOM_LEFT, TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT
	}

	public enum Background {
		NONE, WAVES, WAVES_HORIZONTAL
	}

	private Hashtable<Integer, Bitmap> bitmap;

	private int iconHeight, smallTileSize;

	private static final int REFERENCE_ICON_HEIGHT = 75;

	private static int hash(Type type, int variant) {
		return variant << 6 | type.ordinal();
	}

	private static int hash(Location location) {
		return location.ordinal();
	}

	private static int hash(Trader.Position trader) {
		return trader.ordinal();
	}

	private static int hash(Background background) {
		return background.ordinal();
	}

	private static int hash(Hexagon.Type resource) {
		return resource.ordinal();
	}

	private static int hash(UIButton.Background background) {
		return background.ordinal();
	}

	private static int hash(UIButton.Type button) {
		return button.ordinal();
	}

	private static int hash(Player.Color player) {
		return player.ordinal();
	}

	private Bitmap get(Type type, int variant) {
		return bitmap.get(hash(type, variant));
	}

	private void add(Type type, int variant, Bitmap bitmap) {
		this.bitmap.put(hash(type, variant), bitmap);
	}

	private void add(Type type, int variant, int id, Resources res) {
		add(type, variant, load(id, res));
	}

	private static Bitmap load(int id, Resources res) {
		return BitmapFactory.decodeResource(res, id, new Options());
	}

	private static void draw(Canvas canvas, Bitmap bitmap, int x, int y) {
		if (bitmap == null)
			return;

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		canvas.drawBitmap(bitmap, x - w / 2, y - h / 2, null);
	}

	public TextureManager(Resources res) {
		// initialize hash table
		bitmap = new Hashtable<Integer, Bitmap>();

		// load background
		add(Type.BACKGROUND, hash(Background.WAVES),
				R.drawable.background_waves, res);

		// load large tile textures
		add(Type.TILE, hash(Hexagon.Type.SHORE), R.drawable.tile_shore, res);
		add(Type.TILE, hash(Hexagon.Type.DESERT), R.drawable.tile_desert, res);
		add(Type.TILE, hash(Hexagon.Type.WOOL), R.drawable.tile_wool, res);
		add(Type.TILE, hash(Hexagon.Type.GRAIN), R.drawable.tile_grain, res);
		add(Type.TILE, hash(Hexagon.Type.LUMBER), R.drawable.tile_lumber, res);
		add(Type.TILE, hash(Hexagon.Type.BRICK), R.drawable.tile_brick, res);
		add(Type.TILE, hash(Hexagon.Type.ORE), R.drawable.tile_ore, res);
		add(Type.TILE, hash(Hexagon.Type.DIM), R.drawable.tile_dim, res);
		add(Type.TILE, hash(Hexagon.Type.LIGHT), R.drawable.tile_light, res);

		// load roll number textures
		add(Type.ROLL, 2, R.drawable.roll_2, res);
		add(Type.ROLL, 3, R.drawable.roll_3, res);
		add(Type.ROLL, 4, R.drawable.roll_4, res);
		add(Type.ROLL, 5, R.drawable.roll_5, res);
		add(Type.ROLL, 6, R.drawable.roll_6, res);
		add(Type.ROLL, 8, R.drawable.roll_8, res);
		add(Type.ROLL, 9, R.drawable.roll_9, res);
		add(Type.ROLL, 10, R.drawable.roll_10, res);
		add(Type.ROLL, 11, R.drawable.roll_11, res);
		add(Type.ROLL, 12, R.drawable.roll_12, res);

		// load robber textures
		add(Type.ROBBER, 0, R.drawable.tile_robber, res);

		// load button textures
		add(Type.BUTTONBG, hash(UIButton.Background.BACKDROP),
				R.drawable.button_backdrop, res);
		add(Type.BUTTONBG, hash(UIButton.Background.PRESSED),
				R.drawable.button_press, res);
		add(Type.BUTTON, hash(UIButton.Type.INFO), R.drawable.button_status,
				res);
		add(Type.BUTTON, hash(UIButton.Type.ROLL), R.drawable.button_roll, res);
		add(Type.BUTTON, hash(UIButton.Type.ROAD), R.drawable.button_road, res);
		add(Type.BUTTON, hash(UIButton.Type.TOWN),
				R.drawable.button_settlement, res);
		add(Type.BUTTON, hash(UIButton.Type.CITY), R.drawable.button_city, res);
		add(Type.BUTTON, hash(UIButton.Type.DEVCARD),
				R.drawable.button_development, res);
		add(Type.BUTTON, hash(UIButton.Type.TRADE), R.drawable.button_trade,
				res);
		add(Type.BUTTON, hash(UIButton.Type.ENDTURN),
				R.drawable.button_endturn, res);
		add(Type.BUTTON, hash(UIButton.Type.CANCEL), R.drawable.button_cancel,
				res);

		// load large town textures
		add(Type.TOWN, hash(Player.Color.SELECT), R.drawable.settlement_purple,
				res);
		add(Type.TOWN, hash(Player.Color.RED), R.drawable.settlement_red, res);
		add(Type.TOWN, hash(Player.Color.BLUE), R.drawable.settlement_blue, res);
		add(Type.TOWN, hash(Player.Color.GREEN), R.drawable.settlement_green,
				res);
		add(Type.TOWN, hash(Player.Color.ORANGE), R.drawable.settlement_orange,
				res);

		// load large city textures
		add(Type.CITY, hash(Player.Color.SELECT), R.drawable.city_purple, res);
		add(Type.CITY, hash(Player.Color.RED), R.drawable.city_red, res);
		add(Type.CITY, hash(Player.Color.BLUE), R.drawable.city_blue, res);
		add(Type.CITY, hash(Player.Color.GREEN), R.drawable.city_green, res);
		add(Type.CITY, hash(Player.Color.ORANGE), R.drawable.city_orange, res);

		// load large resource icons
		add(Type.RESOURCE, hash(Hexagon.Type.LUMBER), R.drawable.res_lumber,
				res);
		add(Type.RESOURCE, hash(Hexagon.Type.WOOL), R.drawable.res_wool, res);
		add(Type.RESOURCE, hash(Hexagon.Type.GRAIN), R.drawable.res_grain, res);
		add(Type.RESOURCE, hash(Hexagon.Type.BRICK), R.drawable.res_brick, res);
		add(Type.RESOURCE, hash(Hexagon.Type.ORE), R.drawable.res_ore, res);
		add(Type.RESOURCE, hash(Hexagon.Type.ANY), R.drawable.trader_any, res);

		// load large trader textures
		add(Type.TRADER, hash(Trader.Position.NORTH), R.drawable.trader_north,
				res);
		add(Type.TRADER, hash(Trader.Position.SOUTH), R.drawable.trader_south,
				res);
		add(Type.TRADER, hash(Trader.Position.NORTHEAST),
				R.drawable.trader_northeast, res);
		add(Type.TRADER, hash(Trader.Position.NORTHWEST),
				R.drawable.trader_northwest, res);
		add(Type.TRADER, hash(Trader.Position.SOUTHEAST),
				R.drawable.trader_southeast, res);
		add(Type.TRADER, hash(Trader.Position.SOUTHWEST),
				R.drawable.trader_southwest, res);

		// load corner ornaments
		add(Type.ORNAMENT, hash(Location.BOTTOM_LEFT), R.drawable.bl_corner,
				res);
		add(Type.ORNAMENT, hash(Location.TOP_LEFT), R.drawable.tl_corner, res);
		add(Type.ORNAMENT, hash(Location.TOP_RIGHT), R.drawable.tr_corner, res);

		// get some size measurements
		iconHeight = get(Type.RESOURCE, hash(Hexagon.Type.LUMBER)).getHeight();
		smallTileSize = get(Type.TILE, hash(Hexagon.Type.LUMBER)).getHeight();
	}

	private static void shorten(int[] points, double factor) {
		int center = (points[0] + points[1]) / 2;
		points[0] = (int) (center - factor * (center - points[0]));
		points[1] = (int) (center - factor * (center - points[1]));
	}

	public static int getColor(Player.Color color) {
		switch (color) {
		case RED:
			return Color.rgb(0xBE, 0x28, 0x20);
		case BLUE:
			return Color.rgb(0x37, 0x57, 0xB3);
		case GREEN:
			return Color.rgb(0x13, 0xA6, 0x19);
		case ORANGE:
			return Color.rgb(0xE9, 0xD3, 0x03);
		default:
			return Color.rgb(0x87, 0x87, 0x87);
		}
	}

	public static int darken(int color, double factor) {
		return Color.argb(Color.alpha(color),
				(int) (Color.red(color) * factor),
				(int) (Color.green(color) * factor),
				(int) (Color.blue(color) * factor));
	}

	public static void setPaintColor(Paint paint, Player.Color color) {
		paint.setColor(getColor(color));
	}

	public void draw(Background background, Canvas canvas, Geometry geometry) {
		if (background == Background.NONE) {
			canvas.drawColor(Color.BLACK);
			return;
		}

		Bitmap bitmap = get(Type.BACKGROUND, hash(background));
		int xsize = bitmap.getWidth();
		int ysize = bitmap.getHeight();

		int width = geometry.getWidth();
		int height = geometry.getHeight();

		for (int y = 0; y < height; y += ysize) {
			for (int x = 0; x < width; x += xsize)
				canvas.drawBitmap(bitmap, x, y, null);
		}
	}

	public void draw(UIButton button, Player.Color player, Canvas canvas) {
		Bitmap background = get(Type.BUTTONBG,
				hash(UIButton.Background.BACKDROP));
		Bitmap pressed = get(Type.BUTTONBG, hash(UIButton.Background.PRESSED));

		button.draw(canvas, background, pressed);
	}

	public void draw(Location location, int x, int y, Canvas canvas) {
		Bitmap image = get(Type.ORNAMENT, hash(location));

		int dx = x;
		int dy = y;

		if (location == Location.BOTTOM_RIGHT || location == Location.TOP_RIGHT)
			dx -= image.getWidth();

		if (location == Location.BOTTOM_LEFT
				|| location == Location.BOTTOM_RIGHT)
			dy -= image.getHeight();

		canvas.drawBitmap(image, dx, dy, null);
	}

	public void draw(Hexagon hexagon, Canvas canvas, Geometry geometry) {
		int id = hexagon.getId();
		int x = geometry.getHexagonX(id);
		int y = geometry.getHexagonY(id);

		Bitmap bitmap = get(Type.TILE, hash(Hexagon.Type.SHORE));
		draw(canvas, bitmap, x, y);
	}

	public void draw(Hexagon hexagon, boolean robber, Canvas canvas,
			Geometry geometry, int lastRoll) {
		int id = hexagon.getId();
		int x = geometry.getHexagonX(id);
		int y = geometry.getHexagonY(id);

		Bitmap bitmap = get(Type.TILE, hash(hexagon.getType()));
		draw(canvas, bitmap, x, y);

		int roll = hexagon.getRoll();

		if (hexagon.hasRobber())
			draw(canvas, get(Type.ROBBER, 0), x, y);
		else if (lastRoll != 0 && roll == lastRoll)
			draw(canvas, get(Type.TILE, hash(Hexagon.Type.LIGHT)), x, y);

		if (roll != 0)
			draw(canvas, get(Type.ROLL, roll), x, y);

		// // debug label
		// Paint paint = new Paint();
		// paint.setColor(Color.WHITE);
		// paint.setTextSize(20);
		// canvas.drawText("H" + hexagon.getId(), x, y, paint);
	}

	public void draw(Trader trader, Canvas canvas, Geometry geometry) {
		int id = trader.getIndex();
		int x = geometry.getTraderX(id);
		int y = geometry.getTraderY(id);

		// draw shore access notches
		Bitmap notches = get(Type.TRADER, hash(trader.getPosition()));
		draw(canvas, notches, x, y);

		// get offset from shore
		x = (int) (geometry.getTraderIconOffsetX(id));
		y = (int) (geometry.getTraderIconOffsetY(id));

		// draw type icon
		Bitmap icon = get(Type.RESOURCE, hash(trader.getType()));
		draw(canvas, icon, x, y);
	}

	public void draw(Edge edge, boolean build, Canvas canvas, Geometry geometry) {
		int[] x = new int[2];
		int[] y = new int[2];
		x[0] = geometry.getVertexX(edge.getVertex1().getIndex());
		x[1] = geometry.getVertexX(edge.getVertex2().getIndex());
		y[0] = geometry.getVertexY(edge.getVertex1().getIndex());
		y[1] = geometry.getVertexY(edge.getVertex2().getIndex());

		shorten(x, 0.55);
		shorten(y, 0.55);

		Paint paint = new Paint();
		Player owner = edge.getOwner();

		paint.setAntiAlias(true);
		paint.setStrokeCap(Paint.Cap.SQUARE);

		// draw black backdrop
		if (owner != null || build) {
			paint.setARGB(255, 0, 0, 0);
			paint.setStrokeWidth((int) (geometry.getUnitSize()
					* geometry.getZoom() / 7));
			canvas.drawLine(x[0], y[0], x[1], y[1], paint);
		}

		// set size
		paint.setStrokeWidth((int) (geometry.getUnitSize() * geometry.getZoom() / 12));
		shorten(x, 0.95);
		shorten(y, 0.95);

		// set the color
		if (owner != null)
			setPaintColor(paint, owner.getColor());
		else
			setPaintColor(paint, Player.Color.SELECT);

		// draw road
		if (owner != null || build)
			canvas.drawLine(x[0], y[0], x[1], y[1], paint);

		// // debug label
		// paint.setColor(Color.WHITE);
		// paint.setTextSize(20);
		// canvas.drawText("E" + edge.getIndex(), (x[0] + x[1]) / 2, (y[0] +
		// y[1]) / 2, paint);
	}

	public void draw(Vertex vertex, boolean buildTown, boolean buildCity,
			Canvas canvas, Geometry geometry) {

		Type type = Type.NONE;
		if (vertex.getBuilding() == Vertex.CITY || buildCity)
			type = Type.CITY;
		else if (vertex.getBuilding() == Vertex.TOWN || buildTown)
			type = Type.TOWN;

		Player.Color color;
		Player owner = vertex.getOwner();
		if (buildTown || buildCity)
			color = Player.Color.SELECT;
		else if (owner != null)
			color = owner.getColor();
		else
			color = Player.Color.NONE;

		int id = vertex.getIndex();
		Bitmap bitmap = get(type, hash(color));
		draw(canvas, bitmap, geometry.getVertexX(id), geometry.getVertexY(id));
	}

	public Bitmap get(UIButton.Type type) {
		return get(Type.BUTTON, hash(type));
	}

	public Bitmap get(Hexagon.Type type) {
		return get(Type.RESOURCE, hash(type));
	}

	public int getIconSize() {
		return iconHeight;
	}

	public int getSmallTileSize() {
		return smallTileSize;
	}

	public double getRelativeScaling() {
		return (double) iconHeight / (double) REFERENCE_ICON_HEIGHT;
	}
}
