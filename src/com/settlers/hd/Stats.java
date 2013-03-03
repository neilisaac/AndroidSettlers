package com.settlers.hd;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class Stats extends ListActivity {

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);

		setTitle(getString(R.string.stats));

		Settings settings = ((Settlers) getApplicationContext())
				.getSettingsInstance();
		String[] games = settings.getStatList(getResources());

		if (games == null || games.length == 0) {
			games = new String[1];
			games[0] = getString(R.string.stats_none);
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, games);

		setListAdapter((ListAdapter) adapter);
		getListView().setTextFilterEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.stats, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.clear:
			((Settlers) getApplicationContext()).getSettingsInstance()
					.resetScores();
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
