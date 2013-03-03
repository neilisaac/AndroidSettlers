package com.settlers.hd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class PlayerTrade extends Activity {

	public static final int REQUEST_TRADE_COMPLETED = 0;

	public static final String TYPE_KEY = "com.settlers.hd.TradeType";
	public static final String OFFER_KEY = "com.settlers.hd.TradeOffer";
	public static final String PLAYER_KEY = "com.settlers.hd.TradePlayer";
	public static final String INDEX_KEY = "com.settlers.hd.TradeIndex";

	private static final int[] RESOURCES = { R.id.trade_res1, R.id.trade_res2,
			R.id.trade_res3, R.id.trade_res4, R.id.trade_res5 };

	private static final int[] OFFERS = { R.id.trade_offer1, R.id.trade_offer2,
			R.id.trade_offer3, R.id.trade_offer4, R.id.trade_offer5 };

	private static final int[] PLUS = { R.id.trade_plus1, R.id.trade_plus2,
			R.id.trade_plus3, R.id.trade_plus4, R.id.trade_plus5 };

	private static final int[] MINUS = { R.id.trade_minus1, R.id.trade_minus2,
			R.id.trade_minus3, R.id.trade_minus4, R.id.trade_minus5 };

	private static final int[] RES_VIEW = { R.id.resource1, R.id.resource2,
			R.id.resource3, R.id.resource4, R.id.resource5 };

	private static final int[] RES_STRING = { R.string.lumber, R.string.wool,
			R.string.grain, R.string.brick, R.string.ore };

	private Player player;
	private int selected;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		selected = 0;

		Bundle extras = getIntent().getExtras();
		if (extras != null)
			selected = extras.getInt(TYPE_KEY);

		setContentView(R.layout.playertrade);
		setTitle(R.string.trade);

		Board board = ((Settlers) getApplicationContext()).getBoardInstance();
		if (board == null) {
			finish();
			return;
		}

		player = board.getCurrentPlayer();

		Spinner select = (Spinner) findViewById(R.id.trade_type);
		ArrayAdapter<CharSequence> choices = new ArrayAdapter<CharSequence>(
				this, android.R.layout.simple_spinner_item);
		choices
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		for (int i = 0; i < RESOURCES.length; i++) {
			String res = String.format(getString(R.string.trade_for_resource),
					getString(Hexagon.getTypeStringResource(Hexagon.TYPES[i])));
			choices.add(res);

			int count = player.getResources(Hexagon.TYPES[i]);
			int ratio = player.getTradeValue();
			if (player.hasTrader(Hexagon.TYPES[i]))
				ratio = 2;

			TextView text = (TextView) findViewById(RESOURCES[i]);
			text.setText(Integer.toString(count));

			TextView ratioView = (TextView) findViewById(RES_VIEW[i]);
			ratioView.setText(String.format(getString(R.string.trade_ratio),
					getString(RES_STRING[i]), ratio));

			Button plus = (Button) findViewById(PLUS[i]);
			Button minus = (Button) findViewById(MINUS[i]);

			plus.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					for (int i = 0; i < PLUS.length; i++) {
						if (v == findViewById(PLUS[i])) {
							TextView offer = (TextView) findViewById(OFFERS[i]);
							String str = offer.getText().toString();
							int value = Integer.parseInt(str, 10) + 1;
							offer.setText(Integer.toString(value));

							Button minus = (Button) findViewById(MINUS[i]);
							minus.setEnabled(true);

							if (value >= player.getResources(Hexagon.Type
									.values()[i]))
								v.setEnabled(false);

							break;
						}
					}

					Button propose = (Button) findViewById(R.id.trade_propose);
					propose.setEnabled(true);

					checkAmounts();
				}
			});

			minus.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					boolean valid = false;
					for (int i = 0; i < MINUS.length; i++) {
						TextView offer = (TextView) findViewById(OFFERS[i]);
						String str = offer.getText().toString();
						int value = Integer.parseInt(str, 10);

						if (v == findViewById(MINUS[i])) {
							value -= 1;
							offer.setText(Integer.toString(value));

							Button plus = (Button) findViewById(PLUS[i]);
							plus.setEnabled(true);

							if (value == 0)
								v.setEnabled(false);
						}

						if (value > 0)
							valid = true;
					}

					Button propose = (Button) findViewById(R.id.trade_propose);
					if (!valid)
						propose.setEnabled(false);

					checkAmounts();
				}
			});

			if (count == 0 || i == selected)
				plus.setEnabled(false);
		}

		select.setAdapter(choices);
		select.setSelection(selected);

		select.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapter, View view,
					int position, long id) {
				selected = position;

				for (int i = 0; i < RESOURCES.length; i++) {
					int count = player.getResources(Hexagon.Type.values()[i]);

					TextView offer = (TextView) findViewById(OFFERS[i]);
					offer.setText("0");

					Button plus = (Button) findViewById(PLUS[i]);
					Button minus = (Button) findViewById(MINUS[i]);

					minus.setEnabled(false);
					plus.setEnabled(count > 0 && i != selected);
				}

				Button propose = (Button) findViewById(R.id.trade_propose);
				propose.setEnabled(false);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		Button propose = (Button) findViewById(R.id.trade_propose);
		propose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int[] trade = new int[Hexagon.TYPES.length];
				for (int i = 0; i < trade.length; i++) {
					TextView offer = (TextView) findViewById(OFFERS[i]);
					trade[i] = Integer.parseInt((String) offer.getText(), 10);
				}

				Intent intent = new Intent(PlayerTrade.this, AcceptTrade.class);
				intent.setClassName("com.settlers.hd", "com.settlers.hd.AcceptTrade");
				intent.putExtra(TYPE_KEY, selected);
				intent.putExtra(OFFER_KEY, trade);

				PlayerTrade.this.startActivityForResult(intent,
						REQUEST_TRADE_COMPLETED);
			}
		});

		Button tradeButton = (Button) findViewById(R.id.trade_bank);
		tradeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Hexagon.Type type = Hexagon.TYPES[selected];

				int[] offer = new int[Hexagon.TYPES.length];
				for (int i = 0; i < RESOURCES.length; i++) {
					CharSequence offerChar = ((TextView) findViewById(OFFERS[i]))
							.getText();
					int number = Integer.parseInt(offerChar.toString());
					offer[i] = number;
				}

				if (player.trade(type, offer)) {
					toast(getString(R.string.trade_for_past) + " "
							+ getString(Hexagon.getTypeStringResource(type)));
					finish();
				} else {
					toast(getString(R.string.trade_invalid));
				}
			}
		});
	}

	private void checkAmounts() {
		Hexagon.Type type = Hexagon.TYPES[selected];
		int types = 0;
		int[] offer = new int[Hexagon.TYPES.length];

		for (int i = 0; i < RESOURCES.length; i++) {
			CharSequence offerChar = ((TextView) findViewById(OFFERS[i]))
					.getText();
			int number = Integer.parseInt(offerChar.toString());
			offer[i] = number;
			if (number > 0)
				types += 1;
		}

		Button tradeButton = (Button) findViewById(R.id.trade_bank);
		if (!Player.canTradeMixed() && types != 1)
			tradeButton.setEnabled(false);
		else
			tradeButton.setEnabled(player.canTrade(type, offer));
	}

	private void toast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (requestCode == REQUEST_TRADE_COMPLETED
				&& resultCode == Activity.RESULT_OK)
			finish();
	}
}
