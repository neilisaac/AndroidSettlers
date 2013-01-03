package com.settlers.hd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class LocalGame extends Activity {

	private String[] names;
	private boolean[] types;
	private boolean mixed = false, auto_discard = false;

	private static final String MIXED_KEY = "mixed_trade";
	private static final String AUTO_KEY = "auto_discard";

	private static final String[] NAME_KEYS = { "player_name1", "player_name2",
			"player_name3", "player_name4" };
	private static final String[] HUMAN_KEYS = { "player_human1",
			"player_human2", "player_human3", "player_human4" };

	private static final int[] TEXT = { R.id.name1, R.id.name2, R.id.name3,
			R.id.name4 };
	private static final int[] CHECK = { R.id.human1, R.id.human2, R.id.human3,
			R.id.human4 };

	public static final int[] DEFAULT_NAMES = { R.string.player_name1,
			R.string.player_name2, R.string.player_name3, R.string.player_name4 };

	public static final boolean[] DEFAULT_HUMANS = { true, false, false, false };

	public static void setup(Settings settings) {
		for (int i = 0; i < 4; i++)
			settings.set(HUMAN_KEYS[i], DEFAULT_HUMANS[i]);
	}

	private void reset() {
		for (int i = 0; i < 4; i++) {
			names[i] = getString(DEFAULT_NAMES[i]);
			types[i] = DEFAULT_HUMANS[i];
		}
	}

	private void populate() {
		for (int i = 0; i < 4; i++) {
			EditText name = (EditText) findViewById(TEXT[i]);
			name.setText(names[i]);

			CheckBox human = (CheckBox) findViewById(CHECK[i]);
			human.setChecked(types[i]);
		}

		CheckBox mixedTrade = (CheckBox) findViewById(R.id.mixed_trade);
		mixedTrade.setChecked(mixed);

		CheckBox discardCheck = (CheckBox) findViewById(R.id.auto_discard);
		discardCheck.setChecked(auto_discard);
	}

	private void load(Settings settings) {
		for (int i = 0; i < 4; i++) {
			names[i] = settings.get(NAME_KEYS[i]);
			if (names[i] == null || names[i] == "")
				names[i] = getString(DEFAULT_NAMES[i]);

			types[i] = settings.getBool(HUMAN_KEYS[i]);
		}

		mixed = settings.getBool(MIXED_KEY);
		auto_discard = settings.getBool(AUTO_KEY);
	}

	private void save(Settings settings) {
		for (int i = 0; i < 4; i++) {
			settings.set(NAME_KEYS[i], names[i]);
			settings.set(HUMAN_KEYS[i], types[i]);
		}

		settings.set(MIXED_KEY, mixed);
		settings.set(AUTO_KEY, auto_discard);
	}

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.localgame);

		names = new String[4];
		types = new boolean[4];
		load(((Settlers) getApplicationContext()).getSettingsInstance());
		populate();

		Spinner pointSpinner = (Spinner) findViewById(R.id.option_max_points);
		ArrayAdapter<CharSequence> pointChoices = new ArrayAdapter<CharSequence>(
				this, android.R.layout.simple_spinner_item);
		pointChoices
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		for (int i = 5; i <= 15; i++) {
			String choice = i + " " + getString(R.string.points_to_win);

			if (i == 5)
				choice += " " + getString(R.string.length_quick);
			else if (i == 10)
				choice += " " + getString(R.string.length_standard);
			else if (i == 15)
				choice += " " + getString(R.string.length_long);

			pointChoices.add(choice);
		}

		pointSpinner.setAdapter(pointChoices);
		pointSpinner.setSelection(5);

		final Button start = (Button) findViewById(R.id.start);
		start.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				boolean blankName = false;

				for (int i = 0; i < 4; i++) {
					EditText name = (EditText) findViewById(TEXT[i]);
					names[i] = name.getText().toString().trim();

					if (names[i].length() == 0)
						blankName = true;

					CheckBox human = (CheckBox) findViewById(CHECK[i]);
					types[i] = human.isChecked();
				}

				CheckBox mixedTrade = (CheckBox) findViewById(R.id.mixed_trade);
				mixed = mixedTrade.isChecked();
				Player.enableMixedTrades(mixed);

				if (blankName) {
					Toast.makeText(getApplicationContext(),
							getString(R.string.player_set_names),
							Toast.LENGTH_SHORT).show();
					return;
				}

				save(((Settlers) getApplicationContext()).getSettingsInstance());

				Spinner pointSpinner = (Spinner) findViewById(R.id.option_max_points);
				int maxPoints = pointSpinner.getSelectedItemPosition() + 5;

				CheckBox discardCheck = (CheckBox) findViewById(R.id.auto_discard);
				boolean autoDiscard = discardCheck.isChecked();
				
				finish();

				Settlers app = (Settlers) getApplicationContext();
				app.setBoardInstance(new Board(names, types, maxPoints,
						autoDiscard));
				LocalGame.this.startActivity(new Intent(LocalGame.this,
						GameActivity.class));
			}
		});

		final Button reset = (Button) findViewById(R.id.reset);
		reset.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				reset();
				populate();
			}
		});

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}
}
