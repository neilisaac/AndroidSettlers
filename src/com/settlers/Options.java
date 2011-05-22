package com.settlers;

import java.util.Locale;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

public class Options extends Activity {
	
	public static final int DEFAULT_TURN_DELAY = 750;

	private static boolean beep_turn, vibrate_turn, vibrate_long, turn_log,
			show_status;
	private static int force_locale, turn_delay;

	private static final Locale[] LOCALES = { Locale.ENGLISH, Locale.GERMAN,
			Locale.CHINESE, Locale.KOREAN };

	public static boolean beepTurn() {
		return beep_turn;
	}

	public static boolean vibrateTurn() {
		return vibrate_turn;
	}

	public static boolean vibrateLong() {
		return vibrate_long;
	}

	public static boolean turnLog() {
		return turn_log;
	}
	
	public static int turnDelay() {
		return turn_delay;
	}

	public static boolean showStatus() {
		return show_status;
	}

	public static void setup(Settings settings) {
		reset();
		save(settings);
	}

	private static void reset() {
		show_status = false;
		beep_turn = false;
		vibrate_turn = false;
		vibrate_long = true;
		turn_log = true;
		force_locale = 0;
		turn_delay = DEFAULT_TURN_DELAY;
	}

	public static void load(Settings settings) {
		show_status = settings.getBool("option_show_status");
		beep_turn = settings.getBool("option_beep_turn");
		vibrate_turn = settings.getBool("option_vibrate_turn");
		vibrate_long = settings.getBool("option_vibrate_long");
		turn_log = settings.getBool("option_turn_log");
		force_locale = settings.getInt("option_force_locale");
		turn_delay = settings.getInt("option_turn_delay");

		if (force_locale >= LOCALES.length)
			force_locale = 0;
	}

	private static void save(Settings settings) {
		settings.set("option_show_status", show_status);
		settings.set("option_beep_turn", beep_turn);
		settings.set("option_vibrate_turn", vibrate_turn);
		settings.set("option_vibrate_long", vibrate_long);
		settings.set("option_turn_log", turn_log);
		settings.set("option_force_locale", force_locale);
		settings.set("option_turn_delay", turn_delay);
	}

	private void set() {
		CheckBox showStatus = (CheckBox) findViewById(R.id.show_status);
		CheckBox beepTurn = (CheckBox) findViewById(R.id.beep_turn);
		CheckBox vibrateTurn = (CheckBox) findViewById(R.id.vibrate_turn);
		CheckBox vibrateLong = (CheckBox) findViewById(R.id.vibrate_long);
		CheckBox turnLog = (CheckBox) findViewById(R.id.turn_log);
		Spinner locale = (Spinner) findViewById(R.id.force_locale);
		CheckBox delay = (CheckBox) findViewById(R.id.turn_delay);

		showStatus.setChecked(show_status);
		beepTurn.setChecked(beep_turn);
		vibrateTurn.setChecked(vibrate_turn);
		vibrateLong.setChecked(vibrate_long);
		turnLog.setChecked(turn_log);
		locale.setSelection(force_locale);
		delay.setChecked(turn_delay > 0);
	}

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.options);

		setTitle(getString(R.string.app_name) + " "
				+ getString(R.string.options));

		Spinner locale = (Spinner) findViewById(R.id.force_locale);
		ArrayAdapter<CharSequence> choices = new ArrayAdapter<CharSequence>(
				this, android.R.layout.simple_spinner_item);
		choices
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		choices.add(getString(R.string.option_language_auto));
		for (int i = 0; i < LOCALES.length; i++)
			choices.add(LOCALES[i].getDisplayName());

		locale.setAdapter(choices);

		// load and set options
		load(((Settlers) getApplicationContext()).getSettingsInstance());
		set();

		final Button set = (Button) findViewById(R.id.set);
		set.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				CheckBox showStatus = (CheckBox) findViewById(R.id.show_status);
				CheckBox beepTurn = (CheckBox) findViewById(R.id.beep_turn);
				CheckBox vibrateTurn = (CheckBox) findViewById(R.id.vibrate_turn);
				CheckBox vibrateLong = (CheckBox) findViewById(R.id.vibrate_long);
				CheckBox turnLog = (CheckBox) findViewById(R.id.turn_log);
				Spinner locale = (Spinner) findViewById(R.id.force_locale);
				CheckBox delay = (CheckBox) findViewById(R.id.turn_delay);

				show_status = showStatus.isChecked();
				beep_turn = beepTurn.isChecked();
				vibrate_turn = vibrateTurn.isChecked();
				vibrate_long = vibrateLong.isChecked();
				turn_log = turnLog.isChecked();
				force_locale = locale.getSelectedItemPosition();
				turn_delay = delay.isChecked() ? DEFAULT_TURN_DELAY : 0;

				save(((Settlers) getApplicationContext()).getSettingsInstance());

				// set locale
				Locale newLocale = Locale.getDefault();
				if (force_locale > 0)
					newLocale = LOCALES[force_locale - 1];

				Configuration config = new Configuration();
				config.locale = newLocale;

				getBaseContext().getResources().updateConfiguration(config,
						getBaseContext().getResources().getDisplayMetrics());

				finish();
			}
		});

		final Button reset = (Button) findViewById(R.id.reset);
		reset.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				reset();
				set();
			}
		});
	}
}
