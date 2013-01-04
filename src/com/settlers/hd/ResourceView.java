package com.settlers.hd;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ResourceView extends LinearLayout {
	
	private static final int[] RESOURCES = {
		R.drawable.res_lumber_large,
		R.drawable.res_wool_large,
		R.drawable.res_grain_large,
		R.drawable.res_brick_large,
		R.drawable.res_ore_large,
	};
	
	TextView[] views;

	public ResourceView(Context context) {
		super(context);

		int padding = (int) (10 * context.getResources().getDisplayMetrics().density);

		setVisibility(View.INVISIBLE);
		setOrientation(LinearLayout.HORIZONTAL);
		setPadding(padding, padding, padding, padding);
		
		views = new TextView[RESOURCES.length];
		
		for (int i = 0; i < RESOURCES.length; i++) {
			ImageView image = new ImageView(context);
			image.setImageResource(RESOURCES[i]);
			
			TextView text = new TextView(context);
			text.setText("");
			text.setTextColor(Color.WHITE);
			text.setTextSize(24);
			text.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
			text.setGravity(Gravity.BOTTOM);
			text.setPadding(0, 0, 2 * padding, 0);
			views[i] = text;
			
			addView(image);
			addView(text);
		}
	}

	public void setValues(Player player) {		
		if (player == null || !player.isHuman()) {
			setVisibility(View.INVISIBLE);
			return;
		}
		
		setVisibility(View.VISIBLE);

		int color = TextureManager.darken(TextureManager.getColor(player.getColor()), 0.35f);
		color = Color.argb(128, Color.red(color), Color.green(color), Color.blue(color));
		setBackgroundColor(color);
		
		int[] resources = player.getResources();
		for (int i = 0; i < Hexagon.TYPES.length; i++)
			views[i].setText(String.valueOf(resources[i]));
	}
}
