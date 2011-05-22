package com.settlers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Main extends Activity {

	private static final String DONATE_URL = "https://www.paypal.com/cgi-bin/"
			+ "webscr?cmd=_donations&business=isaac.neil@gmail.com&"
			+ "item_name=Island+Settlers+donation&no_shipping=1";

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);

		if (state == null) {
			// we were just launched
		} else {
			// we are being restored
		}

		setContentView(R.layout.main);

		final Button newgame = (Button) findViewById(R.id.newgame);
		newgame.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Main.this.startActivity(new Intent(Main.this, LocalGame.class));
			}
		});

		final Button resume = (Button) findViewById(R.id.resume);
		resume.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Main.this.startActivity(new Intent(Main.this, Game.class));
			}
		});

		final Button options = (Button) findViewById(R.id.options);
		options.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Main.this.startActivity(new Intent(Main.this, Options.class));
			}
		});

		final Button stats = (Button) findViewById(R.id.stats);
		stats.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Main.this.startActivity(new Intent(Main.this, Stats.class));
			}
		});

		final Button rules = (Button) findViewById(R.id.rules_button);
		rules.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Main.this.startActivity(new Intent(Main.this, Rules.class));
			}
		});

		final Button about = (Button) findViewById(R.id.about);
		about.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final Builder aboutDialog = new AlertDialog.Builder(Main.this);
				aboutDialog.setTitle(R.string.app_name);
				aboutDialog.setIcon(R.drawable.icon);
				aboutDialog.setMessage(getString(R.string.about_text) + "\n\n"
						+ getString(R.string.acknowledgements) + "\n\n"
						+ getString(R.string.translators));

				// aboutDialog.setNegativeButton(R.string.dismiss_button, null);
				aboutDialog.setPositiveButton(R.string.donate_button,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Main.this.startActivity(new Intent(
										Intent.ACTION_VIEW, Uri
												.parse(DONATE_URL)));
							}
						});
				aboutDialog.setNeutralButton(R.string.site_button,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Main.this.startActivity(new Intent(
										Intent.ACTION_VIEW,
										Uri.parse(getString(R.string.website_url))));
							}
						});
				aboutDialog.show();
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

		final Button resume = (Button) findViewById(R.id.resume);

		Board board = ((Settlers) getApplicationContext()).getBoardInstance();
		Settings settings = ((Settlers) getApplicationContext())
				.getSettingsInstance();

		String events = settings.get("game_events");

		if (board != null && board.getWinner(settings) == null) {
			resume.setEnabled(true);
		} else if (board == null && events != null && events != "") {
			resume.setEnabled(true);
			resume.setText(getString(R.string.load_button));
		}
	}

}
