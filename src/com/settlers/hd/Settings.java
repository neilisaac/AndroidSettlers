package com.settlers.hd;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class Settings extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 2;
	
	private static final int DEFAULT_TURN_DELAY = 750;

	private static final String DATABASE_NAME = "island_settings.db";
	private static final String SETTINGS_TABLE = "settings";
	private static final String SCORES_TABLE = "scores";

	private static final String SETTINGS_INSERT = "INSERT INTO "
			+ SETTINGS_TABLE + " (name,value) VALUES (?,?)";
	private static final String SCORE_INSERT = "INSERT INTO " + SCORES_TABLE
			+ " (date,humans,winner,points,turns) VALUES (?,?,?,?,?)";

	private static final String[] QUERY_VALUE = { "value" };
	private static final String[] QUERY_STATS = { "date", "humans", "winner",
			"points", "turns" };

	private SQLiteDatabase db;
	private SQLiteStatement settingInsert, scoreInsert;

	public Settings(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

		settingInsert = null;
		scoreInsert = null;

		getWritableDatabase();
	}

	private void set(SQLiteDatabase db, String attribute, String value) {
		if (db.isReadOnly())
			return;

		if (settingInsert == null)
			settingInsert = db.compileStatement(SETTINGS_INSERT);

		settingInsert.bindString(1, attribute);
		settingInsert.bindString(2, value);
		settingInsert.executeInsert();
	}
	
	public void set(String attribute, String value) {
		set(db, attribute, value);
	}

	public void set(String attribute, int value) {
		set(attribute, Integer.toString(value));
	}

	public void set(String attribute, boolean value) {
		set(attribute, value ? "true" : "false");
	}

	public String get(String attribute) {
		String value = null;

		Cursor cursor = db.query(SETTINGS_TABLE, QUERY_VALUE, "name = \""
				+ attribute + "\"", null, null, null, "id desc");

		if (cursor != null && cursor.moveToFirst())
			value = cursor.getString(0);

		if (cursor != null && !cursor.isClosed())
			cursor.close();

		return value;
	}

	public int getInt(String attribute) {
		try {
			return Integer.parseInt(get(attribute));
		} catch (NumberFormatException ex) {
			return 0;
		}
	}

	public boolean getBool(String attribute) {
		return Boolean.parseBoolean(get(attribute));
	}

	public void resetSettings() {
		db.delete(SETTINGS_TABLE, null, null);
	}

	@SuppressLint("SimpleDateFormat")
	public void addScore(int humans, int score, String winner, int turns) {
		if (db.isReadOnly())
			return;

		if (scoreInsert == null)
			scoreInsert = db.compileStatement(SCORE_INSERT);

		Format formatter = new SimpleDateFormat("MMM d yyyy");
		String date = formatter.format(new Date());

		scoreInsert.bindString(1, date);
		scoreInsert.bindString(2, Integer.toString(humans));
		scoreInsert.bindString(3, winner);
		scoreInsert.bindString(4, Integer.toString(score));
		scoreInsert.bindString(5, Integer.toString(turns));
		scoreInsert.executeInsert();
	}

	public String[] getStatList(Resources res) {
		Cursor cursor = db.query(SCORES_TABLE, QUERY_STATS, null, null, null,
				null, "id desc");

		String[] list = new String[cursor.getCount()];

		int index = 0;
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			String date = cursor.getString(0);
			int humans = cursor.getInt(1);
			String winner = cursor.getString(2);
			int points = cursor.getInt(3);
			int turns = cursor.getInt(4);

			if (humans > 1) {
				list[index] = date + " (" + humans + " "
						+ res.getString(R.string.option_humans) + ")\n"
						+ winner + " " + res.getString(R.string.option_won)
						+ "\n" + turns + " "
						+ res.getString(R.string.option_turns) + ", " + points
						+ " " + res.getString(R.string.option_points);
			} else {
				list[index] = date + "\n" + winner + " "
						+ res.getString(R.string.option_won) + "\n" + turns
						+ " " + res.getString(R.string.option_turns) + ", "
						+ points + " " + res.getString(R.string.option_points);
			}

			cursor.moveToNext();
			index++;
		}

		return list;
	}

	public void resetScores() {
		db.delete(SCORES_TABLE, null, null);
	}
	
	public static int getTurnDelay() {
		return DEFAULT_TURN_DELAY;
	}
	
	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		this.db = db;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		this.db = db;
		Log.d(this.getClass().getName(), "need to initialize database");

		// create key/value pair table
		db.execSQL("CREATE TABLE " + SETTINGS_TABLE
				+ " (id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "name TEXT, value TEXT)");

		// create game log table
		db.execSQL("CREATE TABLE " + SCORES_TABLE
				+ " (id INTEGER PRIMARY KEY AUTOINCREMENT, date TEXT, "
				+ "humans INTEGER, winner TEXT, points INTEGER, "
				+ "TURNS integer)");

		// add default options and players
		LocalGame.setup(this);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		this.db = db;
		Log.d(this.getClass().getName(), "database upgrade from version "
				+ oldVersion + " to " + newVersion);

		switch (oldVersion) {
		case 1:
			// add default value for turn delay
			set("option_turn_delay", DEFAULT_TURN_DELAY);
		}
	}
}
