package com.settlers;

import java.util.Enumeration;
import java.util.Hashtable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.BitmapFactory.Options;

public class TextureManager {

	private enum Type {
		NONE, BACKGROUND, BUTTON, BUTTONBG, TILE, ROLL, ROBBER, RESOURCE, TRADER, ROAD, TOWN, CITY, ORNAMENT
	}

	public enum Location {
		BOTTOM_LEFT, TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT
	}

	private enum Size {
		GENERIC, SMALL, LARGE
	}

	public enum Background {
		NONE, WAVES, WAVES_HORIZONTAL
	}

	private Hashtable<Integer, Bitmap> bitmap;

	private double scaleFactor;
	private int iconHeight, smallTileSize;

	private static final int REFERENCE_ICON_HEIGHT = 75;

	private static int hash(Type type, Size size, int variant) {
		return variant << 6 | size.ordinal() << 4 | type.ordinal();
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

	private static Size getSize(int key) {
		return Size.values()[(key >> 4) & 3];
	}

	private Bitmap get(Type type, Size size, int variant) {
		return bitmap.get(hash(type, size, variant));
	}

	private void add(Type type, Size size, int variant, Bitmap bitmap) {
		this.bitmap.put(hash(type, size, variant), bitmap);
	}

	private void add(Type type, Size size, int variant, int id, Resources res) {
		add(type, size, variant, load(id, res));
	}

	private static Bitmap load(int id, Resources res) {
		return BitmapFactory.decodeResource(res, id, new Options());
	}

	private static Bitmap scale(Bitmap bitmap, double scale) {
		int width = (int) (bitmap.getWidth() * scale);
		int height = (int) (bitmap.getHeight() * scale);
		return Bitmap.createScaledBitmap(bitmap, width, height, false);
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

		scaleFactor = 1.0;

		// load background
		add(Type.BACKGROUND, Size.GENERIC, hash(Background.WAVES),
				R.drawable.background_waves, res);

		// load small tile textures
		add(Type.TILE, Size.SMALL, hash(Hexagon.Type.SHORE),
				R.drawable.tile_shore_small, res);
		add(Type.TILE, Size.SMALL, hash(Hexagon.Type.DESERT),
				R.drawable.tile_desert_small, res);
		add(Type.TILE, Size.SMALL, hash(Hexagon.Type.WOOL),
				R.drawable.tile_wool_small, res);
		add(Type.TILE, Size.SMALL, hash(Hexagon.Type.GRAIN),
				R.drawable.tile_grain_small, res);
		add(Type.TILE, Size.SMALL, hash(Hexagon.Type.LUMBER),
				R.drawable.tile_lumber_small, res);
		add(Type.TILE, Size.SMALL, hash(Hexagon.Type.BRICK),
				R.drawable.tile_brick_small, res);
		add(Type.TILE, Size.SMALL, hash(Hexagon.Type.ORE),
				R.drawable.tile_ore_small, res);
		add(Type.TILE, Size.SMALL, hash(Hexagon.Type.DIM),
				R.drawable.tile_dim_small, res);
		add(Type.TILE, Size.SMALL, hash(Hexagon.Type.LIGHT),
				R.drawable.tile_light_small, res);

		// load large tile textures
		add(Type.TILE, Size.LARGE, hash(Hexagon.Type.SHORE),
				R.drawable.tile_shore_large, res);
		add(Type.TILE, Size.LARGE, hash(Hexagon.Type.DESERT),
				R.drawable.tile_desert_large, res);
		add(Type.TILE, Size.LARGE, hash(Hexagon.Type.WOOL),
				R.drawable.tile_wool_large, res);
		add(Type.TILE, Size.LARGE, hash(Hexagon.Type.GRAIN),
				R.drawable.tile_grain_large, res);
		add(Type.TILE, Size.LARGE, hash(Hexagon.Type.LUMBER),
				R.drawable.tile_lumber_large, res);
		add(Type.TILE, Size.LARGE, hash(Hexagon.Type.BRICK),
				R.drawable.tile_brick_large, res);
		add(Type.TILE, Size.LARGE, hash(Hexagon.Type.ORE),
				R.drawable.tile_ore_large, res);
		add(Type.TILE, Size.LARGE, hash(Hexagon.Type.DIM),
				R.drawable.tile_dim_large, res);
		add(Type.TILE, Size.LARGE, hash(Hexagon.Type.LIGHT),
				R.drawable.tile_light_large, res);

		// load roll number textures
		add(Type.ROLL, Size.SMALL, 2, R.drawable.roll_2_small, res);
		add(Type.ROLL, Size.SMALL, 3, R.drawable.roll_3_small, res);
		add(Type.ROLL, Size.SMALL, 4, R.drawable.roll_4_small, res);
		add(Type.ROLL, Size.SMALL, 5, R.drawable.roll_5_small, res);
		add(Type.ROLL, Size.SMALL, 6, R.drawable.roll_6_small, res);
		add(Type.ROLL, Size.SMALL, 8, R.drawable.roll_8_small, res);
		add(Type.ROLL, Size.SMALL, 9, R.drawable.roll_9_small, res);
		add(Type.ROLL, Size.SMALL, 10, R.drawable.roll_10_small, res);
		add(Type.ROLL, Size.SMALL, 11, R.drawable.roll_11_small, res);
		add(Type.ROLL, Size.SMALL, 12, R.drawable.roll_12_small, res);
		add(Type.ROLL, Size.LARGE, 2, R.drawable.roll_2_large, res);
		add(Type.ROLL, Size.LARGE, 3, R.drawable.roll_3_large, res);
		add(Type.ROLL, Size.LARGE, 4, R.drawable.roll_4_large, res);
		add(Type.ROLL, Size.LARGE, 5, R.drawable.roll_5_large, res);
		add(Type.ROLL, Size.LARGE, 6, R.drawable.roll_6_large, res);
		add(Type.ROLL, Size.LARGE, 8, R.drawable.roll_8_large, res);
		add(Type.ROLL, Size.LARGE, 9, R.drawable.roll_9_large, res);
		add(Type.ROLL, Size.LARGE, 10, R.drawable.roll_10_large, res);
		add(Type.ROLL, Size.LARGE, 11, R.drawable.roll_11_large, res);
		add(Type.ROLL, Size.LARGE, 12, R.drawable.roll_12_large, res);

		// load robber textures
		add(Type.ROBBER, Size.SMALL, 0, R.drawable.tile_robber_small, res);
		add(Type.ROBBER, Size.LARGE, 0, R.drawable.tile_robber_large, res);

		// load button textures
		add(Type.BUTTONBG, Size.GENERIC, hash(UIButton.Background.BACKDROP),
				R.drawable.button_backdrop, res);
		add(Type.BUTTONBG, Size.GENERIC, hash(UIButton.Background.PRESSED),
				R.drawable.button_press, res);
		add(Type.BUTTON, Size.GENERIC, hash(UIButton.Type.INFO),
				R.drawable.button_status, res);
		add(Type.BUTTON, Size.GENERIC, hash(UIButton.Type.ROLL),
				R.drawable.button_roll, res);
		add(Type.BUTTON, Size.GENERIC, hash(UIButton.Type.ROAD),
				R.drawable.button_road, res);
		add(Type.BUTTON, Size.GENERIC, hash(UIButton.Type.TOWN),
				R.drawable.button_settlement, res);
		add(Type.BUTTON, Size.GENERIC, hash(UIButton.Type.CITY),
				R.drawable.button_city, res);
		add(Type.BUTTON, Size.GENERIC, hash(UIButton.Type.DEVCARD),
				R.drawable.button_development, res);
		add(Type.BUTTON, Size.GENERIC, hash(UIButton.Type.TRADE),
				R.drawable.button_trade, res);
		add(Type.BUTTON, Size.GENERIC, hash(UIButton.Type.ENDTURN),
				R.drawable.button_endturn, res);
		add(Type.BUTTON, Size.GENERIC, hash(UIButton.Type.CANCEL),
				R.drawable.button_cancel, res);

		// load small town textures
		add(Type.TOWN, Size.SMALL, hash(Player.Color.SELECT),
				R.drawable.settlement_purple_small, res);
		add(Type.TOWN, Size.SMALL, hash(Player.Color.RED),
				R.drawable.settlement_red_small, res);
		add(Type.TOWN, Size.SMALL, hash(Player.Color.BLUE),
				R.drawable.settlement_blue_small, res);
		add(Type.TOWN, Size.SMALL, hash(Player.Color.GREEN),
				R.drawable.settlement_green_small, res);
		add(Type.TOWN, Size.SMALL, hash(Player.Color.ORANGE),
				R.drawable.settlement_orange_small, res);

		// load large town textures
		add(Type.TOWN, Size.LARGE, hash(Player.Color.SELECT),
				R.drawable.settlement_purple_large, res);
		add(Type.TOWN, Size.LARGE, hash(Player.Color.RED),
				R.drawable.settlement_red_large, res);
		add(Type.TOWN, Size.LARGE, hash(Player.Color.BLUE),
				R.drawable.settlement_blue_large, res);
		add(Type.TOWN, Size.LARGE, hash(Player.Color.GREEN),
				R.drawable.settlement_green_large, res);
		add(Type.TOWN, Size.LARGE, hash(Player.Color.ORANGE),
				R.drawable.settlement_orange_large, res);

		// load small city textures
		add(Type.CITY, Size.SMALL, hash(Player.Color.SELECT),
				R.drawable.city_purple_small, res);
		add(Type.CITY, Size.SMALL, hash(Player.Color.RED),
				R.drawable.city_red_small, res);
		add(Type.CITY, Size.SMALL, hash(Player.Color.BLUE),
				R.drawable.city_blue_small, res);
		add(Type.CITY, Size.SMALL, hash(Player.Color.GREEN),
				R.drawable.city_green_small, res);
		add(Type.CITY, Size.SMALL, hash(Player.Color.ORANGE),
				R.drawable.city_orange_small, res);

		// load large city textures
		add(Type.CITY, Size.LARGE, hash(Player.Color.SELECT),
				R.drawable.city_purple_large, res);
		add(Type.CITY, Size.LARGE, hash(Player.Color.RED),
				R.drawable.city_red_large, res);
		add(Type.CITY, Size.LARGE, hash(Player.Color.BLUE),
				R.drawable.city_blue_large, res);
		add(Type.CITY, Size.LARGE, hash(Player.Color.GREEN),
				R.drawable.city_green_large, res);
		add(Type.CITY, Size.LARGE, hash(Player.Color.ORANGE),
				R.drawable.city_orange_large, res);

		// load small resource icons
		add(Type.RESOURCE, Size.SMALL, hash(Hexagon.Type.LUMBER),
				R.drawable.res_lumber_small, res);
		add(Type.RESOURCE, Size.SMALL, hash(Hexagon.Type.WOOL),
				R.drawable.res_wool_small, res);
		add(Type.RESOURCE, Size.SMALL, hash(Hexagon.Type.GRAIN),
				R.drawable.res_grain_small, res);
		add(Type.RESOURCE, Size.SMALL, hash(Hexagon.Type.BRICK),
				R.drawable.res_brick_small, res);
		add(Type.RESOURCE, Size.SMALL, hash(Hexagon.Type.ORE),
				R.drawable.res_ore_small, res);
		add(Type.RESOURCE, Size.SMALL, hash(Hexagon.Type.ANY),
				R.drawable.trader_any_small, res);

		// load large resource icons
		add(Type.RESOURCE, Size.LARGE, hash(Hexagon.Type.LUMBER),
				R.drawable.res_lumber_large, res);
		add(Type.RESOURCE, Size.LARGE, hash(Hexagon.Type.WOOL),
				R.drawable.res_wool_large, res);
		add(Type.RESOURCE, Size.LARGE, hash(Hexagon.Type.GRAIN),
				R.drawable.res_grain_large, res);
		add(Type.RESOURCE, Size.LARGE, hash(Hexagon.Type.BRICK),
				R.drawable.res_brick_large, res);
		add(Type.RESOURCE, Size.LARGE, hash(Hexagon.Type.ORE),
				R.drawable.res_ore_large, res);
		add(Type.RESOURCE, Size.LARGE, hash(Hexagon.Type.ANY),
				R.drawable.trader_any_large, res);

		// load small trader textures
		add(Type.TRADER, Size.SMALL, hash(Trader.Position.NORTH),
				R.drawable.trader_north_small, res);
		add(Type.TRADER, Size.SMALL, hash(Trader.Position.SOUTH),
				R.drawable.trader_south_small, res);
		add(Type.TRADER, Size.SMALL, hash(Trader.Position.NORTHEAST),
				R.drawable.trader_northeast_small, res);
		add(Type.TRADER, Size.SMALL, hash(Trader.Position.NORTHWEST),
				R.drawable.trader_northwest_small, res);
		add(Type.TRADER, Size.SMALL, hash(Trader.Position.SOUTHEAST),
				R.drawable.trader_southeast_small, res);
		add(Type.TRADER, Size.SMALL, hash(Trader.Position.SOUTHWEST),
				R.drawable.trader_southwest_small, res);

		// load large trader textures
		add(Type.TRADER, Size.LARGE, hash(Trader.Position.NORTH),
				R.drawable.trader_north_large, res);
		add(Type.TRADER, Size.LARGE, hash(Trader.Position.SOUTH),
				R.drawable.trader_south_large, res);
		add(Type.TRADER, Size.LARGE, hash(Trader.Position.NORTHEAST),
				R.drawable.trader_northeast_large, res);
		add(Type.TRADER, Size.LARGE, hash(Trader.Position.NORTHWEST),
				R.drawable.trader_northwest_large, res);
		add(Type.TRADER, Size.LARGE, hash(Trader.Position.SOUTHEAST),
				R.drawable.trader_southeast_large, res);
		add(Type.TRADER, Size.LARGE, hash(Trader.Position.SOUTHWEST),
				R.drawable.trader_southwest_large, res);

		// load corner ornaments
		add(Type.ORNAMENT, Size.GENERIC, hash(Location.BOTTOM_LEFT),
				R.drawable.bl_corner, res);
		add(Type.ORNAMENT, Size.GENERIC, hash(Location.TOP_LEFT),
				R.drawable.tl_corner, res);
		add(Type.ORNAMENT, Size.GENERIC, hash(Location.TOP_RIGHT),
				R.drawable.tr_corner, res);

		// get some size measurements
		iconHeight = get(Type.RESOURCE, Size.LARGE, hash(Hexagon.Type.LUMBER))
				.getHeight();
		smallTileSize = get(Type.TILE, Size.SMALL, hash(Hexagon.Type.LUMBER))
				.getHeight();
	}

	public void scaleTextures(Geometry geometry) {
		double scale = geometry.getScaleFactor();
		double relative = scale / scaleFactor;
		
		int key = hash(Type.BACKGROUND, Size.GENERIC, hash(Background.WAVES));
		Bitmap background = bitmap.get(key);
		
		int width = geometry.getRealWidth();
		int height = geometry.getRealHeight();
		if (width != background.getWidth() || height != background.getHeight()) {
			background = Bitmap.createScaledBitmap(background, width, height, false);
			bitmap.remove(key);
			bitmap.put(key, background);
		}
		
		key = hash(Type.BACKGROUND, Size.GENERIC, hash(Background.WAVES_HORIZONTAL));
		if (bitmap.get(key) == null) {
			Matrix rotate = new Matrix();
			rotate.postRotate(90);
			background = Bitmap.createBitmap(background, 0, 0, width, height, rotate, false);
			bitmap.put(key, background);
		}

		if (Math.abs(relative - 1.0) < 0.2)
			return;

		Enumeration<Integer> keys = bitmap.keys();
		while (keys.hasMoreElements()) {
			key = keys.nextElement();

			// skip generic elements (not scaled)
			if (getSize(key) == Size.GENERIC)
				continue;

			// scale bitmap relative to current size
			Bitmap texture = bitmap.get(key);
			Bitmap scaled = scale(texture, relative);
			texture.recycle();

			// replace image with scaled bitmap
			bitmap.remove(key);
			bitmap.put(key, scaled);
		}

		scaleFactor = scale;
		iconHeight *= relative;
		smallTileSize *= relative;
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

		Bitmap bitmap = get(Type.BACKGROUND, Size.GENERIC, hash(background));
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
		Bitmap background = get(Type.BUTTONBG, Size.GENERIC,
				hash(UIButton.Background.BACKDROP));
		Bitmap pressed = get(Type.BUTTONBG, Size.GENERIC,
				hash(UIButton.Background.PRESSED));

		button.draw(canvas, background, pressed);
	}

	public void draw(Location location, int x, int y, Canvas canvas) {
		Bitmap image = get(Type.ORNAMENT, Size.GENERIC, hash(location));

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
		Size size = geometry.isZoomed() ? Size.LARGE : Size.SMALL;

		Bitmap bitmap = get(Type.TILE, size, hash(Hexagon.Type.SHORE));
		draw(canvas, bitmap, x, y);
	}

	public void draw(Hexagon hexagon, boolean robber, Canvas canvas,
			Geometry geometry, int lastRoll) {
		int id = hexagon.getId();
		int x = geometry.getHexagonX(id);
		int y = geometry.getHexagonY(id);
		Size size = geometry.isZoomed() ? Size.LARGE : Size.SMALL;

		Bitmap bitmap = get(Type.TILE, size, hash(hexagon.getType()));
		draw(canvas, bitmap, x, y);

		int roll = hexagon.getRoll();

		if (hexagon.hasRobber())
			draw(canvas, get(Type.ROBBER, size, 0), x, y);
		else if (lastRoll != 0 && roll == lastRoll)
			draw(canvas, get(Type.TILE, size, hash(Hexagon.Type.LIGHT)), x, y);

		if (roll != 0)
			draw(canvas, get(Type.ROLL, size, roll), x, y);
		
//		// debug label
//		Paint paint = new Paint();
//		paint.setColor(Color.WHITE);
//		paint.setTextSize(20);
//		canvas.drawText("H" + hexagon.getId(), x, y, paint);
	}

	public void draw(Trader trader, Canvas canvas, Geometry geometry) {
		int id = trader.getIndex();
		int x = geometry.getTraderX(id);
		int y = geometry.getTraderY(id);
		Size size = geometry.isZoomed() ? Size.LARGE : Size.SMALL;

		// draw shore access notches
		Bitmap notches = get(Type.TRADER, size, hash(trader.getPosition()));
		draw(canvas, notches, x, y);

		// get offset from shore
		x = (int) (geometry.getTraderIconOffsetX(id));
		y = (int) (geometry.getTraderIconOffsetY(id));

		// draw type icon
		Bitmap icon = get(Type.RESOURCE, size, hash(trader.getType()));
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
		paint
				.setStrokeWidth((int) (geometry.getUnitSize()
						* geometry.getZoom() / 12));
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
		
//		// debug label
//		paint.setColor(Color.WHITE);
//		paint.setTextSize(20);
//		canvas.drawText("E" + edge.getIndex(), (x[0] + x[1]) / 2, (y[0] + y[1]) / 2, paint);
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
		Size size = geometry.isZoomed() ? Size.LARGE : Size.SMALL;
		Bitmap bitmap = get(type, size, hash(color));
		draw(canvas, bitmap, geometry.getVertexX(id), geometry.getVertexY(id));
	}

	public Bitmap get(UIButton.Type type) {
		return get(Type.BUTTON, Size.GENERIC, hash(type));
	}

	public Bitmap get(Hexagon.Type type) {
		return get(Type.RESOURCE, Size.LARGE, hash(type));
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
