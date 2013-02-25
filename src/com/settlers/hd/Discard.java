package com.settlers.hd;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Discard extends Activity {

	public static final String QUANTITY_KEY = "com.settlers.hd.DiscardQuantity";
	public static final String PLAYER_KEY = "com.settlers.hd.DiscardPlayer";

	private static final int[] RESOURCES = { R.id.trade_res1, R.id.trade_res2,
			R.id.trade_res3, R.id.trade_res4, R.id.trade_res5 };

	private static final int[] SELECTIONS = { R.id.trade_offer1,
			R.id.trade_offer2, R.id.trade_offer3, R.id.trade_offer4,
			R.id.trade_offer5 };

	private static final int[] PLUS = { R.id.trade_plus1, R.id.trade_plus2,
			R.id.trade_plus3, R.id.trade_plus4, R.id.trade_plus5 };

	private static final int[] MINUS = { R.id.trade_minus1, R.id.trade_minus2,
			R.id.trade_minus3, R.id.trade_minus4, R.id.trade_minus5 };

	private Player player;
	private int quantity;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		setFinishOnTouchOutside(false);

		setContentView(R.layout.discard);

		quantity = 0;
		player = null;

		Board board = ((Settlers) getApplicationContext()).getBoardInstance();
		if (board == null) {
			finish();
			return;
		}

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			quantity = extras.getInt(QUANTITY_KEY);
			player = board.getPlayer(extras.getInt(PLAYER_KEY));
		}
		
		if (extras == null || quantity == 0) {
			finish();
			return;
		}

		setTitle(String.format(getString(R.string.discard_reason), board.getCurrentPlayer().getName()));

		String instructionText = getString(R.string.discard_instruction);
		TextView instruction = (TextView) findViewById(R.id.discard_instruction);
		instruction.setText(player.getName() + ": "
				+ String.format(instructionText, quantity));

		for (int i = 0; i < RESOURCES.length; i++) {
			int count = player.getResources(Hexagon.TYPES[i]);

			TextView text = (TextView) findViewById(RESOURCES[i]);
			text.setText(Integer.toString(count));

			Button plus = (Button) findViewById(PLUS[i]);
			Button minus = (Button) findViewById(MINUS[i]);

			plus.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int total = 0;

					for (int i = 0; i < PLUS.length; i++) {
						TextView offer = (TextView) findViewById(SELECTIONS[i]);
						String str = offer.getText().toString();
						int value = Integer.parseInt(str, 10);

						if (v == findViewById(PLUS[i])) {
							value += 1;
							offer.setText(Integer.toString(value));

							findViewById(MINUS[i]).setEnabled(true);

							if (value >= player.getResources(Hexagon.Type
									.values()[i]))
								v.setEnabled(false);
						}

						total += value;
					}

					if (total == quantity) {
						for (int i = 0; i < PLUS.length; i++)
							findViewById(PLUS[i]).setEnabled(false);

						findViewById(R.id.discard_button).setEnabled(true);
					}
				}
			});

			minus.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					for (int i = 0; i < MINUS.length; i++) {
						TextView offer = (TextView) findViewById(SELECTIONS[i]);
						String str = offer.getText().toString();
						int value = Integer.parseInt(str, 10);

						if (v == findViewById(MINUS[i])) {
							value -= 1;
							offer.setText(Integer.toString(value));

							if (value == 0)
								v.setEnabled(false);
						}

						int count = player.getResources(Hexagon.TYPES[i]);
						if (value < count)
							findViewById(PLUS[i]).setEnabled(true);
					}

					findViewById(R.id.discard_button).setEnabled(false);
				}
			});

			plus.setEnabled(count > 0);
		}

		Button discard = (Button) findViewById(R.id.discard_button);
		discard.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				for (int i = 0; i < RESOURCES.length; i++) {
					TextView number = (TextView) findViewById(SELECTIONS[i]);
					int count = Integer.parseInt((String) number.getText(), 10);

					for (int j = 0; j < count; j++)
						player.discard(Hexagon.TYPES[i]);
				}

				finish();
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		// don't allow bypassing discard stage by pressing back
	}
}
