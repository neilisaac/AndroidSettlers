package com.settlers.hd;

import java.util.Vector;

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

	public Vector<UIButton> buttons;
	private boolean buttonsPlaced = false;
	private GameActivity game;

	public GameView(Context context) {
		super(context);
		
		game = (GameActivity) context;
		
		gesture = new GestureDetector(context, this);
		pinch = new ScaleGestureDetector(context, this);
		
		setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

		buttons = new Vector<UIButton>();
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
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distX,
			float distY) {
		// throw out button press if scrolling over a button
		release((int) e2.getX(), (int) e2.getY(), false);

		// shift the board
		renderer.translate(distX, distY);
		
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
		setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		
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

		// consider buttons then a click on the board
		if (release((int) event.getX(), (int) event.getY(), true) ||
				renderer.click((int) event.getX(), (int) event.getY())) {
			Vibrator vibrator = (Vibrator) getContext().getSystemService(
					Context.VIBRATOR_SERVICE);

			vibrator.vibrate(50);
		}
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent event) {
		// ignore intermediate events triggered in a double tap
		return false;
	}

	@Override
	public boolean onDoubleTap(MotionEvent event) {
		// double top zooms to point or zooms out
		if (renderer.isZoomed())
			renderer.unZoom();
		else
			renderer.zoom((int) event.getX(), (int) event.getY());

		return true;
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		float factor = detector.getScaleFactor();
		renderer.zoomBy(factor);
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
		synchronized (buttons) {
			buttons.add(new UIButton(type, Geometry.BUTTON_SIZE, Geometry.BUTTON_SIZE));
			buttonsPlaced = false;
		}
	}

	public void removeButtons() {
		synchronized (buttons) {
			buttons.clear();
		}
	}

	public void placeButtons(int width, int height) {
		if (buttonsPlaced)
			return;
		
		// first button is always in the top left corner
		int x = 0;
		int y = height;

		synchronized (buttons) {
			for (UIButton button : buttons) {
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
						if (x + size > endwidth) {
							x = 0;
							y -= button.getHeight();
						}
					} else {
						// landscape
						int size = button.getHeight();
						y -= size;
						if (y - size < endheight) {
							y = height;
							x += button.getWidth();
						}
					}
				}
			}
		}

		buttonsPlaced = true;
	}
	
	public boolean press(int x, int y) {
		boolean pressed = false;

		// consider buttons
		synchronized (buttons) {
			for (UIButton button : buttons) {
				if (button != null && button.press(x, height - y))
					pressed = true;
			}
		}

		return pressed;
	}

	public boolean release(int x, int y, boolean activate) {
		boolean released = false;

		// consider buttons
		synchronized (buttons) {
			for (UIButton button : buttons) {
				if (button.release(x, height - y)) {
					released = true;
					if (activate)
						game.queueButtonPress(button.getType());
				}
			}
		}

		return released;
	}
}
