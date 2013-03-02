package com.settlers.hd;

import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Main extends Activity {

	private static final String DONATE_URL = "https://www.paypal.com/cgi-bin/"
			+ "webscr?cmd=_donations&business=isaac.neil@gmail.com&"
			+ "item_name=Island+Settlers+donation&no_shipping=1";

	private Vector<Runnable> actions;

	@Override
	public void onResume() {
		super.onResume();

		Board board = ((Settlers) getApplicationContext()).getBoardInstance();
		Settings settings = ((Settlers) getApplicationContext()).getSettingsInstance();

		Vector<String> labels = new Vector<String>();
		actions = new Vector<Runnable>();

		if (board != null && board.getWinner(settings) == null) {
			labels.add(getString(R.string.resume_button));
			actions.add(new Runnable() {
				@Override
				public void run() {
					Main.this.startActivity(new Intent(Main.this, GameActivity.class));
				}
			});
		}

		labels.add(getString(R.string.new_button));
		actions.add(new Runnable() {
			@Override
			public void run() {
				Main.this.startActivity(new Intent(Main.this, LocalGame.class));
			}
		});

		labels.add(getString(R.string.stats));
		actions.add(new Runnable() {
			@Override
			public void run() {
				Main.this.startActivity(new Intent(Main.this, Stats.class));
			}
		});

		labels.add(getString(R.string.rules_button));
		actions.add(new Runnable() {
			@Override
			public void run() {
				Main.this.startActivity(new Intent(Main.this, Rules.class));
			}
		});

		labels.add(getString(R.string.about_button));
		actions.add(new Runnable() {
			@Override
			public void run() {
				final Builder aboutDialog = new AlertDialog.Builder(Main.this);
				aboutDialog.setTitle(R.string.app_name);
				aboutDialog.setIcon(R.drawable.icon);
				aboutDialog.setMessage(getString(R.string.about_text) + "\n\n" + getString(R.string.acknowledgements)
						+ "\n\n" + getString(R.string.translators));
				aboutDialog.show();
			}
		});

		labels.add(getString(R.string.site_button));
		actions.add(new Runnable() {
			@Override
			public void run() {
				Main.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website_url))));
			}
		});

		labels.add(getString(R.string.donate_button));
		actions.add(new Runnable() {
			@Override
			public void run() {
				Main.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(DONATE_URL)));
			}
		});

		String[] values = new String[labels.size()];
		for (int i = 0; i < values.length; i++)
			values[i] = labels.get(i);

		int padding = (int) (10 * getResources().getDisplayMetrics().density);

		ListView view = new ListView(this);
		view.setPadding(padding, padding, padding, padding);

		view.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values));

		view.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				actions.get(position).run();
			}
		});

		setContentView(view);
	}
}
