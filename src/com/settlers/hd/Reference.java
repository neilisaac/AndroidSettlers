package com.settlers.hd;

import android.app.TabActivity;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class Reference extends TabActivity {

	private static final int[] CONTENT = { R.id.ref_content1,
			R.id.ref_content2, R.id.ref_content3 };

	private static final int[] NAMES = { R.string.reference_tab1,
			R.string.reference_tab2, R.string.reference_tab3 };

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.reference);
		TabHost host = getTabHost();
		host.setup();

		setTitle(getString(R.string.app_name) + " "
				+ getString(R.string.reference));

		for (int i = 0; i < NAMES.length; i++) {
			String name = getString(NAMES[i]);
			TabSpec spec = host.newTabSpec(name);
			spec.setIndicator(name);
			spec.setContent(CONTENT[i]);
			host.addTab(spec);
		}

		host.setCurrentTab(0);
	}
}
