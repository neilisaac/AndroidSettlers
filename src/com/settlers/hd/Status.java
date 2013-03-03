package com.settlers.hd;

import com.settlers.hd.Board.Cards;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Status extends Activity {
	
	private View[] views;

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		
		setContentView(R.layout.status);
		setTitle(getString(R.string.status));

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		views = new View[4];
		
		Board board = ((Settlers) getApplicationContext()).getBoardInstance();
		if (board == null) {
			finish();
			return;
		}
		
		for (int i = 0; i < 4; i++) {
			views[i] = inflater.inflate(R.layout.status_player, null);
			
			Player player = board.getPlayer(i);

			boolean showAll = player == board.getCurrentPlayer()
					&& player.isHuman()
					|| board.getWinner(((Settlers) getApplicationContext())
							.getSettingsInstance()) != null;

			int points;
			if (showAll)
				points = player.getVictoryPoints();
			else
				points = player.getPublicVictoryPoints();

			String message = "\n";

			message += getString(R.string.status_resources) + ": "
					+ player.getNumResources() + "\n";
			message += getString(R.string.status_dev_cards) + ": "
					+ player.getNumDevCards() + "\n";

			message += "\n";

			message += getString(R.string.status_towns) + ": "
					+ player.getNumTowns() + " / " + Player.MAX_TOWNS + "\n";
			message += getString(R.string.status_cities) + ": "
					+ player.getNumCities() + " / " + Player.MAX_CITIES + "\n";
			message += getString(R.string.status_roads) + ": "
					+ player.getNumRoads() + " / " + Player.MAX_ROADS + "\n";

			message += "\n";

			message += getString(R.string.status_road_length) + ": "
					+ player.getRoadLength() + "\n";
			if (player == board.getLongestRoadOwner())
				message += getString(R.string.status_longest_road) + ": "
						+ "2 " + getString(R.string.status_points) + "\n";

			message += getString(R.string.status_army_size) + ": "
					+ player.getArmySize() + "\n";
			if (player == board.getLargestArmyOwner())
				message += getString(R.string.status_largest_army) + ": "
						+ "2 " + getString(R.string.status_points) + "\n";

			message += "\n";

			if (showAll) {
				message += getString(R.string.status_soldier_cards) + ": "
						+ player.getNumDevCardType(Cards.SOLDIER) + "\n"
						+ getString(R.string.status_progress_cards) + ": "
						+ player.getNumDevCardType(Cards.PROGRESS) + "\n"
						+ getString(R.string.status_victory_cards) + ": "
						+ player.getVictoryCards() + "\n\n";
			}

			boolean hasTrader = false;
			if (player.hasTrader(null)) {
				message += "3:1 " + getString(R.string.status_trader) + "\n";
				hasTrader = true;
			}

			for (int j = 0; j < Hexagon.TYPES.length; j++) {
				if (player.hasTrader(Hexagon.TYPES[j])) {
					message += getString(Hexagon
							.getTypeStringResource(Hexagon.TYPES[j]))
							+ " " + getString(R.string.status_trader) + "\n";
					hasTrader = true;
				}
			}

			if (hasTrader)
				message += "\n";

			String turn = player.getActionLog();
			if (player == board.getCurrentPlayer() && turn != "")
				message += getString(R.string.status_this_turn) + ":\n" + turn;
			else if (turn != "")
				message += getString(R.string.status_last_turn) + ":\n" + turn;

			TextView text = (TextView) views[i].findViewById(R.id.status_text);
			text.setText(message);

			TextView point = (TextView) views[i].findViewById(R.id.status_points);
			point.setText(getString(R.string.status_victory_points) + ": "
					+ points + " / " + board.getMaxPoints());

			ProgressBar progress = (ProgressBar) views[i].findViewById(R.id.status_progress);
			progress.setMax(board.getMaxPoints());
			progress.setProgress(points);
		}
		
		ViewPager viewPager = (ViewPager) findViewById(R.id.status);
		viewPager.setAdapter(new StatusTabAdapter());
		viewPager.setCurrentItem(board.getCurrentPlayer().getIndex());
		
		PagerTitleStrip titleStrip = (PagerTitleStrip) findViewById(R.id.status_title_strip);
		titleStrip.setBackgroundColor(TextureManager.darken(TextureManager.getColor(
				Settlers.getInstance().getBoardInstance().getPlayer(board.getCurrentPlayer().getIndex()).getColor()), 0.35));
		
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int state) {
			}

			@Override
			public void onPageScrolled(int position, float offset, int offsetPixels) {
			}

			@Override
			public void onPageSelected(int position) {
				int color = TextureManager.getColor(Settlers.getInstance().getBoardInstance().getPlayer(position).getColor());
				color = TextureManager.darken(color, 0.35);
				
				PagerTitleStrip titleStrip = (PagerTitleStrip) findViewById(R.id.status_title_strip);
				titleStrip.setBackgroundColor(color);
			}
		});
	}

	public class StatusTabAdapter extends PagerAdapter {

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
			return Settlers.getInstance().getBoardInstance().getPlayer(position).getName();
		}
	}
}
