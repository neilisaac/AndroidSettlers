package com.settlers.hd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class AcceptTrade extends Activity {

	public static final int REQUEST_TRADE_ACCEPTED = 0;

	private static final int[] RESOURCES = { R.id.trade_res1, R.id.trade_res2,
			R.id.trade_res3, R.id.trade_res4, R.id.trade_res5 };

	private static final int[] BUTTONS = { R.id.trade_player1,
			R.id.trade_player2, R.id.trade_player3 };

	private static final int[] OFFER_BUTTONS = { R.id.trade_player1_offer,
			R.id.trade_player2_offer, R.id.trade_player3_offer };

	private Board board;
	private Player current;
	private int[] trade;
	private Hexagon.Type type;
	private boolean[] accepted;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			finish();
			return;
		}

		type = Hexagon.TYPES[extras.getInt(PlayerTrade.TYPE_KEY)];
		trade = extras.getIntArray(PlayerTrade.OFFER_KEY);

		setContentView(R.layout.accepttrade);
		setTitle(R.string.trade);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.dimAmount = 1.0f;
		getWindow().setAttributes(lp);

		for (int i = 0; i < RESOURCES.length; i++) {
			TextView text = (TextView) findViewById(RESOURCES[i]);
			text.setText(Integer.toString(trade[i]));
		}

		board = ((Settlers) getApplicationContext()).getBoardInstance();
		if (board == null) {
			finish();
			return;
		}

		current = board.getCurrentPlayer();

		accepted = new boolean[3];
		for (int i = 0; i < 3; i++)
			accepted[i] = false;

		TextView wants = (TextView) findViewById(R.id.trade_player_wants);
		wants.setText(String.format(getString(R.string.trade_player_wants),
				getString(Hexagon.getTypeStringResource(type))));

		TextView playerOffer = (TextView) findViewById(R.id.trade_player_offer);
		playerOffer.setText(String.format(
				getString(R.string.trade_player_offer), current.getName()));

		int index = 0;
		for (int i = 0; i < 4; i++) {
			Player player = board.getPlayer(i);
			if (player == current)
				continue;

			boolean accepted = false;
			int[] counter = null;

			if (player.isBot()) {
				// offer to AI player automatically
				AutomatedPlayer bot = (AutomatedPlayer) player;
				int[] offer = bot.offerTrade(current, type, trade);
				if (offer == trade)
					accepted = true;
				else if (offer != null)
					counter = offer;
			} else if (player.isHuman()) {
				// add button on human players
				Button make = (Button) findViewById(OFFER_BUTTONS[index]);
				make.setVisibility(View.VISIBLE);

				make.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Player player = null;
						int index = 0;
						for (int i = 0; i < 4; i++) {
							player = board.getPlayer(i);
							if (player == current)
								continue;

							if (findViewById(OFFER_BUTTONS[index]) == v)
								break;

							index++;
						}

						Intent intent = new Intent(AcceptTrade.this,
								CounterOffer.class);
						intent.setClassName("com.settlers.hd",
								"com.settlers.hd.CounterOffer");
						intent.putExtra(PlayerTrade.TYPE_KEY, type.ordinal());
						intent.putExtra(PlayerTrade.OFFER_KEY, trade);
						intent.putExtra(PlayerTrade.PLAYER_KEY, player
								.getIndex());
						intent.putExtra(PlayerTrade.INDEX_KEY, index);
						AcceptTrade.this.startActivityForResult(intent,
								REQUEST_TRADE_ACCEPTED);
					}
				});
			}

			String text;
			if (accepted)
				text = getString(R.string.trade_accepted);
			else if (counter != null)
				text = getString(R.string.trade_counter);
			else
				text = getString(R.string.trade_rejected);

			Button button = (Button) findViewById(BUTTONS[index]);
			button.setText(String.format(text, player.getName()));
			button.setEnabled((accepted || counter != null)
					&& board.getCurrentPlayer().isHuman());

			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// determine the player
					Player player = null;
					int index = 0;
					for (int i = 0; i < 4; i++) {
						player = board.getPlayer(i);
						if (player == current)
							continue;

						if (findViewById(BUTTONS[index]) == v)
							break;

						index++;
					}

					// swap resources
					current.trade(player, type, trade);

					setResult(Activity.RESULT_OK);
					finish();
				}
			});

			index += 1;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (requestCode == REQUEST_TRADE_ACCEPTED
				&& resultCode == Activity.RESULT_OK && intent != null) {
			int acceptedIndex = intent.getIntExtra(PlayerTrade.INDEX_KEY, -1);

			Log.d(getClass().getName(), "offer accepted by player with index "
					+ acceptedIndex);

			if (acceptedIndex < 0 || !board.getCurrentPlayer().isHuman())
				return;

			accepted[acceptedIndex] = true;

			int index = 0;
			for (int i = 0; i < 4; i++) {
				Player player = board.getPlayer(i);
				if (player == current)
					continue;

				if (player.isHuman()) {
					String text;
					if (accepted[index])
						text = getString(R.string.trade_accepted);
					else
						text = getString(R.string.trade_rejected);

					Button button = (Button) findViewById(BUTTONS[index]);
					button.setEnabled(accepted[index]);
					button.setText(String.format(text, player.getName()));
				}

				index++;
			}
		}
	}
}
