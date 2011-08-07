package com.appspot.manup.signature;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

/**
 * 
 * Code from com.appspot.manup.FingerPaint
 *
 */
public class Signature extends Activity {
    private static final String TAG = "MANUP Signature";
    private MyView myView;
	private Paint mPaint;
	private MaskFilter mEmboss;
	private MaskFilter mBlur;
	private DataHelper dh;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(myView = new MyView(this));

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(Color.BLACK);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(10);

		mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 }, 0.4f, 6, 3.5f);
		mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
		
		dh = new DataHelper(this);
	}
	
	public class MyView extends View {
		private Bitmap mBitmap;
		private Canvas mCanvas;
		private Path mPath;
		private Paint mBitmapPaint;

		public MyView(Context c) {
			super(c);

			mBitmap = Bitmap.createBitmap(320, 480, Bitmap.Config.ARGB_8888);
			mCanvas = new Canvas(mBitmap);
			mPath = new Path();
			mBitmapPaint = new Paint(Paint.DITHER_FLAG);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
			mCanvas.setBitmap(mBitmap = Bitmap.createBitmap(w, h,
					Bitmap.Config.ARGB_8888));
		}

		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawColor(Color.WHITE);
			canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
			canvas.drawPath(mPath, mPaint);
		}

		private float mX, mY;
		private static final float TOUCH_TOLERANCE = 4;

		private void touch_start(float x, float y) {
			mPath.reset();
			mPath.moveTo(x, y);
			mX = x;
			mY = y;
		}

		private void touch_move(float x, float y) {
			float dx = Math.abs(x - mX);
			float dy = Math.abs(y - mY);
			if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
				mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
				mX = x;
				mY = y;
			}
		}

		private void touch_up() {
			mPath.lineTo(mX, mY);
			// commit the path to our offscreen
			mCanvas.drawPath(mPath, mPaint);
			// kill this so we don't double draw
			mPath.reset();
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX();
			float y = event.getY();

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				touch_start(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				touch_move(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_UP:
				touch_up();
				invalidate();
				break;
			}
			return true;
		}

		public Bitmap getBitMap() {
			return mBitmap;
		}
	}

	private static final int SUBMIT = Menu.FIRST;
	private static final int CLEAR = 2;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, SUBMIT, 0, "Submit").setShortcut('7', 's');
		menu.add(0, CLEAR, 0, "Clear").setShortcut('3', 'c');
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mPaint.setXfermode(null);
		mPaint.setAlpha(0xFF);

		switch (item.getItemId()) {
		case SUBMIT:
			onSubmit();
			Toast.makeText(this,"Submit function needs to be implemented",
					Toast.LENGTH_LONG).show();
			return true;
		case CLEAR:
			setContentView(myView = new MyView(this));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void onSubmit(){
		Bitmap sigBitmap = myView.getBitMap();
	}
	
}