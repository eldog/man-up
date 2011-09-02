package com.appspot.manup.signup.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public final class DoodleView extends View
{
    @SuppressWarnings("unused")
    private static final String TAG = DoodleView.class.getSimpleName();

    private static final float TOUCH_TOLERANCE = 4.0f;

    private final Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    private final Canvas mCanvas = new Canvas();
    private final Path mPath = new Path();
    private final Paint mPathPaint;

    private boolean mClearCanvas = true;
    private float mX = -1.0f;
    private float mY = -1.0f;
    private Bitmap mBitmap = null;

    {
        mPathPaint = new Paint();
        mPathPaint.setAntiAlias(true);
        mPathPaint.setDither(true);
        mPathPaint.setColor(Color.BLACK);
        mPathPaint.setStrokeCap(Paint.Cap.ROUND);
        mPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mPathPaint.setStrokeWidth(10.0f);
        mPathPaint.setStyle(Paint.Style.STROKE);
    }

    public DoodleView(final Context context)
    {
        super(context);
    } // constructor(Context)

    public DoodleView(final Context context, final AttributeSet attrs)
    {
        super(context, attrs);
    } // constructor(Context, AttributeSet)

    public DoodleView(final Context context, final AttributeSet attrs, final int defStyle)
    {
        super(context, attrs, defStyle);
    } // constructor(Context, AttributeSet, int)

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh)
    {
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas.setBitmap(mBitmap);
    } // onSizeChanged

    @Override
    protected void onDraw(final Canvas canvas)
    {
        canvas.drawBitmap(mBitmap, 0 /* x */, 0 /* y */, mBitmapPaint);
        canvas.drawPath(mPath, mPathPaint);
    } // onDraw

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

    private void touchStart(final float x, final float y)
    {
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
            mPath.quadTo(mX, mY, (x + mX) / 2.0f, (y + mY) / 2.0f);
            mX = x;
            mY = y;
        } // if
    } // touchMove

    private void touchUp()
    {
        mPath.lineTo(mX, mY);
        // Commit the path to our offscreen.
        mCanvas.drawPath(mPath, mPathPaint);
        // Kill this so we don't double draw.
        mPath.reset();
        mClearCanvas = false;
    } // touchUp

    public boolean isClear()
    {
        return mClearCanvas;
    } // isClear

    public void clear()
    {
        mBitmap.eraseColor(Color.TRANSPARENT);
        invalidate();
    } // clear

    public Bitmap getDoodle()
    {
        return mBitmap;
    } // getBitMap

} // class DoodleView
