package com.settlers;

import android.content.Context;
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

	public GameView(Context context) {
		super(context);
		
		gesture = new GestureDetector(context, this);
		pinch = new ScaleGestureDetector(context, this);
		
		setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
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
		renderer.release((int) e2.getX(), (int) e2.getY(), false);

		// shift the board
		renderer.translate(distX, distY);
		
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
		// throw out button press if scrolling over a button
		renderer.release((int) e2.getX(), (int) e2.getY(), false);

		// ignore flings
		return false;
	}

	@Override
	public boolean onDown(MotionEvent event) {
		setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		
		// press down (consider activating buttons)
		renderer.press((int) event.getX(), (int) event.getY());

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
		return renderer.release((int) event.getX(), (int) event.getY(), true);
	}

	@Override
	public void onLongPress(MotionEvent event) {
		// TODO: long press resource to trade for it

		// consider a click on the board
		if (renderer.click((int) event.getX(), (int) event.getY())) {
			Vibrator vibrator = (Vibrator) getContext().getSystemService(
					Context.VIBRATOR_SERVICE);

			if (Options.vibrateLong())
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
}
