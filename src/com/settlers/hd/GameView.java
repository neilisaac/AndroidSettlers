package com.settlers.hd;

import javax.microedition.khronos.opengles.GL10;

import com.settlers.hd.UIButton.Type;

import android.content.Context;
import android.graphics.Canvas;
import android.opengl.GLSurfaceView;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.GestureDetector;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;

public class GameView extends GLSurfaceView implements OnGestureListener,
		OnDoubleTapListener, OnScaleGestureListener {

	private int width, height;
	private GameRenderer renderer;
	private GestureDetector gesture;
	private ScaleGestureDetector pinch;

	private UIButton[] buttons;
	private boolean buttonsPlaced = false;
	private GameActivity game;

	public GameView(Context context) {
		super(context);
		
		game = (GameActivity) context;
		
		gesture = new GestureDetector(context, this);
		pinch = new ScaleGestureDetector(context, this);
		
		setSystemUiVisibility(getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LOW_PROFILE);

		buttons = new UIButton[UIButton.Type.values().length];
		int size = (int) (0.5 * Geometry.BUTTON_SIZE * getResources().getDisplayMetrics().density);
		for (UIButton.Type type : UIButton.Type.values())
			buttons[type.ordinal()] = new UIButton(type, size, size);
		
		buttonsPlaced = false;
	}

	@Override
	public void setRenderer(Renderer renderer) {
		super.setRenderer(renderer);
		this.renderer = (GameRenderer) renderer;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		width = MeasureSpec.getSize(widthMeasureSpec);
		height = MeasureSpec.getSize(heightMeasureSpec);
		setMeasuredDimension(width, height);
		renderer.setSize(getContext().getResources().getDisplayMetrics(), width, height);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// handle touch events in GestureDetector
		return gesture.onTouchEvent(event) || pinch.onTouchEvent(event);
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distX, float distY) {
		// ignore scrolling started over a button
		for (UIButton button : buttons)
			if (button.isPressed())
				return false;

		// shift the board
		renderer.getGeometry().translate(distX, distY);
		
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
		// throw out button press if scrolling over a button
		release((int) e2.getX(), (int) e2.getY(), false);

		// ignore flings
		return false;
	}

	@Override
	public boolean onDown(MotionEvent event) {
		setSystemUiVisibility(getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LOW_PROFILE);
		
		// press down (consider activating buttons)
		press((int) event.getX(), (int) event.getY());

		// always return true to allow gestures to register
		return true;
	}

	@Override
	public void onShowPress(MotionEvent event) {
		// this is handled already in onDown
	}

	@Override
	public boolean onSingleTapUp(MotionEvent event) {
		// handle in onSingleTapConfirmed
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent event) {
		// button click
		return release((int) event.getX(), (int) event.getY(), true);
	}

	@Override
	public void onLongPress(MotionEvent event) {
		// TODO: long press resource to trade for it
		
		Vibrator vibrator = (Vibrator) getContext().getSystemService(
				Context.VIBRATOR_SERVICE);

		// consider buttons then a click on the board
		if (release((int) event.getX(), (int) event.getY(), true) ||
				click((int) event.getX(), (int) event.getY())) {

			vibrator.vibrate(50);
		} else {
			vibrator.vibrate(20);
		}
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent event) {
		// ignore intermediate events triggered in a double tap
		return false;
	}

	@Override
	public boolean onDoubleTap(MotionEvent event) {
		// try to ignore double taps on a button
		if (release((int) event.getX(), (int) event.getY(), false))
			return true;

		// double top zooms to point or zooms out
		Geometry geometry = renderer.getGeometry();
		geometry.toggleZoom((int) event.getX(), (int) event.getY());

		return true;
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		Geometry geometry = renderer.getGeometry();
		geometry.zoomBy(detector.getScaleFactor());
		return true;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	public void addButton(Type type) {
		buttons[type.ordinal()].setEnabled(true);
		buttonsPlaced = false;
	}

	public void removeButtons() {
		for (UIButton button : buttons)
			button.setEnabled(false);
	}

	public void placeButtons(int width, int height) {
		if (buttonsPlaced)
			return;
		
		// first button is always in the top left corner
		int x = 0;
		int y = height;

		for (UIButton button : buttons) {
			if (!button.isEnabled())
				continue;
			
			int endwidth = width - button.getWidth() / 2;
			int endheight = button.getHeight() / 2;

			// set position
			UIButton.Type type = button.getType();
			if (type == UIButton.Type.CANCEL || type == UIButton.Type.ROLL
					|| type == UIButton.Type.ENDTURN) {
				// set position to far right/bottom
				if (width < height)
					button.setPosition(endwidth,
							height - button.getHeight() / 2);
				else
					button.setPosition(button.getWidth() / 2, endheight);
			} else {
				// set to next available position
				button.setPosition(x + button.getWidth() / 2,
						y - button.getHeight() / 2);

				// get next position
				if (height >= width) {
					// portrait
					int size = button.getWidth();
					x += size;
					if (x + 1.5 * size > endwidth) {
						x = 0;
						y -= button.getHeight();
					}
				} else {
					// landscape
					int size = button.getHeight();
					y -= size;
					if (y - 1.5 * size < endheight) {
						y = height;
						x += button.getWidth();
					}
				}
			}
		}

		buttonsPlaced = true;
	}
	
	public void drawButtons(TextureManager texture, GL10 gl) {
		for (UIButton button : buttons) {
			if (button.isEnabled())
				texture.draw(button, gl);
		}
	}

	private boolean click(int x, int y) {
		Player player = Settlers.getInstance().getBoardInstance().getCurrentPlayer();
		Geometry geometry = renderer.getGeometry();
		GameRenderer.Action action = renderer.getAction();

		if (!player.isHuman())
			return false;

		int select = -1;

		switch (action) {
		case NONE:
			return false;

		case ROBBER:
			// select a hexagon
			select = geometry.getNearestHexagon(x, y);
			break;

		case TOWN:
		case CITY:
			// select a vertex
			select = geometry.getNearestVertex(x, y);
			break;

		case ROAD:
			// select an edge
			select = geometry.getNearestEdge(x, y);
			break;
		}

		if (select >= 0) {
			game.select(action, select);
			return true;
		}

		return false;
	}
	
	private boolean press(int x, int y) {
		// consider buttons
		for (UIButton button : buttons) {
			if (button != null && button.press(x, height - y))
				return true;
		}

		return false;
	}

	private boolean release(int x, int y, boolean activate) {
		boolean released = false;

		// consider buttons
		for (UIButton button : buttons) {
			if (button.release(x, height - y)) {
				released = true;
				if (activate)
					game.buttonPress(button.getType());
			}
		}

		return released;
	}
}
