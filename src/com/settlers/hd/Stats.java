package com.settlers.hd;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class Stats extends Activity {

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);

		int padding = (int) (10 * getResources().getDisplayMetrics().density);

		ListView view = new ListView(this);
		view.setPadding(padding, padding, padding, padding);

		String[] items;
		String[] games = Settlers.getInstance().getSettingsInstance().getStatList(getResources());

		if (games == null || games.length == 0) {
			items = new String[1];
			items[0] = getString(R.string.stats_none);
		} else {
			items = new String[games.length + 1];
			items[0] = getString(R.string.stats_clear);
			for (int i = 0; i < games.length; i++)
				items[i + 1] = games[i];

			view.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if (position == 0) {
						Settlers.getInstance().getSettingsInstance().resetScores();
						finish();
					}
				}
			});
		}

		view.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items));

		setContentView(view);
		setTitle(getString(R.string.stats));
	}
}
