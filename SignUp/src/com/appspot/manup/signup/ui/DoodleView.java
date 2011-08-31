package com.appspot.manup.signup.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.appspot.manup.signup.R;

public final class DoodleView extends View
{
    @SuppressWarnings("unused")
    private static final String TAG = DoodleView.class.getSimpleName();

    private static final float TOUCH_TOLERANCE = 4;

    private static final int TEXT_SIZE_DIP = 16;
    private static final int TEXT_X_COORDINATE = 250;
    private static final int TEXT_Y_COORDINATE = 380;

    private final Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    private final Canvas mCanvas = new Canvas();
    private final Path mPath = new Path();
    private final Paint mPaint;

    private String mName = "";
    private boolean mClearCanvas = true;
    private float mX = -1.0f;
    private float mY = -1.0f;
    private Bitmap mBitmap = null;
    private Bitmap mBackgroundBitmap = null;

    {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(10);
    }

    public DoodleView(final Context context)
    {
        super(context);
    } // constructor

    public void setName(final String name)
    {
        mName = name;
    } // setName

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        mBackgroundBitmap = Bitmap.createBitmap(BitmapFactory.decodeResource(
                getResources(), R.drawable.signature));
        mCanvas.setBitmap(mBitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888));
        drawText();
    } // onSizeChanged

    private void drawText()
    {
        mCanvas.setBitmap(mBackgroundBitmap);
        final Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setAntiAlias(true);
        final float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP,
                getResources().getDisplayMetrics());
        textPaint.setTextSize(px);
        mCanvas.drawText(mName, TEXT_X_COORDINATE, TEXT_Y_COORDINATE, textPaint);
        mCanvas.setBitmap(mBitmap);
    }

    @Override
    protected void onDraw(final Canvas canvas)
    {
        canvas.drawBitmap(mBackgroundBitmap, 0, 0, mBitmapPaint);
        canvas.drawBitmap(mBitmap, 0 /* x */, 0 /* y */, mBitmapPaint);
        canvas.drawPath(mPath, mPaint);
    } // onDraw

    private void touchStart(final float x, final float y)
    {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    } // touchStart

    private void touchMove(final float x, final float y)
    {
        final float dx = Math.abs(x - mX);
        final float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE)
        {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        } // if
    } // touchMove

    private void touchUp()
    {
        mPath.lineTo(mX, mY);
        // Commit the path to our offscreen.
        mCanvas.drawPath(mPath, mPaint);
        // Kill this so we don't double draw.
        mPath.reset();
        mClearCanvas = false;
    } // touchUp

    @Override
    public boolean onTouchEvent(final MotionEvent event)
    {
        final float x = event.getX();
        final float y = event.getY();

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                break;
        } // switch
        invalidate();
        return true;
    } // onTouchEvent

    public boolean isClear()
    {
        return mClearCanvas;
    } // isClear

    public void clear()
    {
        mBitmap.eraseColor(Color.TRANSPARENT);
        drawText();
        invalidate();
    } // clear

    public Bitmap getDoodle()
    {
        return mBitmap;
    } // getBitMap

} // class DoodleView
