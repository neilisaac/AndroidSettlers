package com.settlers.hd;

import com.settlers.hd.Board.Cards;

import android.app.ActionBar;
import android.app.TabActivity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

public class Status extends TabActivity {

	private static final int[] TEXT = { R.id.status_text1, R.id.status_text2,
			R.id.status_text3, R.id.status_text4 };

	private static final int[] POINTS = { R.id.status_points1,
			R.id.status_points2, R.id.status_points3, R.id.status_points4 };

	private static final int[] PROGRESS = { R.id.status_progress1,
			R.id.status_progress2, R.id.status_progress3, R.id.status_progress4 };

	private static final int[] CONTENT = { R.id.status_content1,
			R.id.status_content2, R.id.status_content3, R.id.status_content4 };

	private static final Player.Color[] COLORS = { Player.Color.RED,
			Player.Color.BLUE, Player.Color.GREEN, Player.Color.ORANGE };

	private static final int[] ICONS = { R.drawable.city_red_small,
			R.drawable.city_blue_small, R.drawable.city_green_small,
			R.drawable.city_orange_small };

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.status);
		TabHost host = getTabHost();
		host.setup();

		Board board = ((Settlers) getApplicationContext()).getBoardInstance();
		if (board == null) {
			finish();
			return;
		}

		setTitle(getString(R.string.app_name) + " "
				+ getString(R.string.status_turn) + " " + board.getTurnNumber());

		for (int i = 0; i < 4; i++) {
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

			TextView text = (TextView) findViewById(TEXT[i]);
			text.setText(message);

			TextView point = (TextView) findViewById(POINTS[i]);
			point.setText(getString(R.string.status_victory_points) + ": "
					+ points + " / " + board.getMaxPoints());

			ProgressBar progress = (ProgressBar) findViewById(PROGRESS[i]);
			progress.setMax(board.getMaxPoints());
			progress.setProgress(points);

			int drawable = R.drawable.city_purple_small;
			for (int j = 0; j < COLORS.length; j++) {
				if (player.getColor() == COLORS[j]) {
					drawable = ICONS[j];
					break;
				}
			}

			TabSpec spec = host.newTabSpec(player.getName());
			spec.setIndicator(player.getName(), getResources().getDrawable(
					drawable));
			spec.setContent(CONTENT[i]);
			host.addTab(spec);

			if (player == board.getCurrentPlayer())
				host.setCurrentTab(i);
		}
	}
}
