package com.settlers.hd;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebView;

public class Rules extends Activity {
	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);

		setContentView(R.layout.rules);
		setTitle(R.string.rules_button);

		ActionBar actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		String data = null;

		try {
			InputStream is = getResources().openRawResource(R.raw.rules);

			byte[] buffer = new byte[4096];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			while (true) {
				int read = is.read(buffer);
				if (read == -1)
					break;

				baos.write(buffer, 0, read);
			}

			baos.close();
			is.close();

			data = baos.toString();
		} catch (Exception e) {
			Log.e(this.getClass().getName(), "failed to load resource to string", e);
		}

		final WebView rules = (WebView) findViewById(R.id.rules);
		rules.loadData(data != null ? data : getString(R.string.rules_failed), "text/html", "utf-8");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
