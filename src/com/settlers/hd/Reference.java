package com.settlers.hd;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Reference extends Activity {

	private static final int[] LAYOUTS = { R.layout.reference_build, R.layout.reference_buy,
			R.layout.reference_development };

	private static final int[] NAMES = { R.string.reference_tab1, R.string.reference_tab2, R.string.reference_tab3 };

	private View[] views;

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);

		setContentView(R.layout.reference);
		setTitle(getString(R.string.reference));

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		views = new View[LAYOUTS.length];
		
		for (int i = 0; i < LAYOUTS.length; i++)
			views[i] = inflater.inflate(LAYOUTS[i], null);
		
		ViewPager viewPager = (ViewPager) findViewById(R.id.reference);
		viewPager.setAdapter(new ReferenceTabAdapter());
		viewPager.setCurrentItem(1);
	}

	public class ReferenceTabAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return views.length;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public Object instantiateItem(ViewGroup collection, int position) {
			collection.addView(views[position]);
			return views[position];
		}

		@Override
		public void destroyItem(ViewGroup collection, int position, Object view) {
			collection.removeView((View) view);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return getString(NAMES[position]);
		}
	}
}
