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

    private boolean mIsClear = true;
    private float mLastX = Float.MIN_VALUE;
    private float mLastY = Float.MIN_VALUE;
    private Bitmap mDoodle = null;

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
        mCanvas.setBitmap(mDoodle = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888));
    } // onSizeChanged(int, int, int, int)

    @Override
    protected void onDraw(final Canvas canvas)
    {
        canvas.drawBitmap(mDoodle, 0.0f /* left */, 0.0f /* top */, mBitmapPaint);
        canvas.drawPath(mPath, mPathPaint);
    } // onDraw(Canvas)

    @Override
    public boolean onTouchEvent(final MotionEvent event)
    {
        final float x = event.getX();
        final float y = event.getY();

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                pathStart(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                pathMove(x, y);
                break;
            case MotionEvent.ACTION_UP:
                pathEnd();
                break;
        } // switch

        invalidate();
        return true;
    } // onTouchEvent(MotionEvent)

    private void pathStart(final float x, final float y)
    {
        mPath.moveTo(x, y);
        mLastX = x;
        mLastY = y;
    } // pathStart(float, float)

    private void pathMove(final float x, final float y)
    {
        if (isSignificantMove(x, y))
        {
            mPath.quadTo(mLastX, mLastY, (x + mLastX) / 2.0f, (y + mLastY) / 2.0f);
            mLastX = x;
            mLastY = y;
        } // if
    } // pathMove(float, float)

    private boolean isSignificantMove(final float x, final float y)
    {
        final float xMovement = Math.abs(x - mLastX);
        final float yMovement = Math.abs(y - mLastY);
        return xMovement >= TOUCH_TOLERANCE || yMovement >= TOUCH_TOLERANCE;
    } // isSignificantMove(float, float)

    private void pathEnd()
    {
        // Save the finished path.
        mCanvas.drawPath(mPath, mPathPaint);
        // Prevent the end of this path being joined to that start of the next.
        mPath.reset();
        mIsClear = false;
    } // pathEnd()

    public boolean isClear()
    {
        return mIsClear;
    } // isClear()

    public void clear()
    {
        mDoodle.eraseColor(Color.TRANSPARENT);
        mIsClear = true;
        invalidate();
    } // clear()

    public Bitmap getDoodle()
    {
        return mDoodle;
    } // getDoodle()

} // class DoodleView
