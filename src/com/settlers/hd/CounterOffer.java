package com.settlers.hd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class CounterOffer extends Activity {

	private Hexagon.Type type;
	private int[] trade;
	private Player current, player;
	private int index;

	private static final int[] RESOURCES = { R.id.trade_res1, R.id.trade_res2,
			R.id.trade_res3, R.id.trade_res4, R.id.trade_res5 };

	private static final int[] OFFER = { R.id.trade_offer1, R.id.trade_offer2,
			R.id.trade_offer3, R.id.trade_offer4, R.id.trade_offer5 };

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		Board board = ((Settlers) getApplicationContext()).getBoardInstance();

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			finish();
			return;
		}

		type = Hexagon.TYPES[extras.getInt(PlayerTrade.TYPE_KEY)];
		trade = extras.getIntArray(PlayerTrade.OFFER_KEY);
		player = board.getPlayer(extras.getInt(PlayerTrade.PLAYER_KEY));
		current = board.getCurrentPlayer();
		index = extras.getInt(PlayerTrade.INDEX_KEY);

		Log.d(getClass().getName(), player.getName() + " (index " + index
				+ ") considering trade");

//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.counteroffer);

		TextView wants = (TextView) findViewById(R.id.trade_player_wants);
		wants.setText(String.format(getString(R.string.trade_player_wants),
				getString(Hexagon.getTypeStringResource(type))));

		TextView playerOffer = (TextView) findViewById(R.id.trade_player_offer);
		playerOffer.setText(String.format(
				getString(R.string.trade_player_offer), current.getName()));

		for (int i = 0; i < RESOURCES.length; i++) {
			int count = player.getResources(Hexagon.TYPES[i]);

			TextView res = (TextView) findViewById(RESOURCES[i]);
			res.setText(Integer.toString(count));

			TextView offer = (TextView) findViewById(OFFER[i]);
			offer.setText(Integer.toString(trade[i]));
		}

		Button accept = (Button) findViewById(R.id.trade_accept);
		accept.setEnabled(player.getResources(type) > 0);
		accept.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra(PlayerTrade.INDEX_KEY, index);
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		});
	}
}
