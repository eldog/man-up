package com.appspot.manup.signup.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DoodleView extends View
{
    @SuppressWarnings("unused")
    private static final String TAG = DoodleView.class.getSimpleName();

    private final Path mPath = new Path();
    private final List<PathPoint> mPathPoints = new ArrayList<PathPoint>();
    private boolean mIsClear = true;
    private float mLastX = Float.MIN_VALUE;
    private float mLastY = Float.MIN_VALUE;
    private float mTouchTolerance = 4.0f;
    private Paint mPathPaint = null;

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
    protected void onDraw(final Canvas canvas)
    {
        canvas.drawPath(mPath, mPathPaint);
    } // onDraw(Canvas)

    @Override
    public boolean onTouchEvent(final MotionEvent event)
    {
        if (!isEnabled())
        {
            return true;
        } // if

        final float x = event.getX();
        final float y = event.getY();

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                pathStart(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                pathMove(x, y);
        } // switch

        invalidate();
        return true;
    } // onTouchEvent(MotionEvent)

    private void pathStart(final float x, final float y)
    {
        mPath.moveTo(x, y);
        mPathPoints.add(new PathPoint(x, y, false));
        mIsClear = false;
        mLastX = x;
        mLastY = y;
    } // pathStart(float, float)

    private void pathMove(final float x, final float y)
    {
        if (isSignificantMove(x, y))
        {
            quadTo(x, mLastX, y, mLastY);
            mLastX = x;
            mLastY = y;
            mPathPoints.add(new PathPoint(x, y, true));
        } // if
    } // pathMove(float, float)

    private boolean isSignificantMove(final float x, final float y)
    {
        final float xMovement = Math.abs(x - mLastX);
        final float yMovement = Math.abs(y - mLastY);
        return xMovement >= mTouchTolerance || yMovement >= mTouchTolerance;
    } // isSignificantMove(float, float)

    private void quadTo(final float x, final float lastX, final float y, final float lastY)
    {
        mPath.quadTo(lastX, lastY, (x + lastX) / 2.0f, (y + lastY) / 2.0f);
    } // quadTo(flaot, float)

    public boolean isClear()
    {
        return mIsClear;
    } // isClear()

    public void clear()
    {
        mPath.rewind();
        mPathPoints.clear();
        mIsClear = true;
        invalidate();
    } // clear()

    public Bitmap getDoodle()
    {
        final Bitmap doodle = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(doodle);
        canvas.drawPath(mPath, mPathPaint);
        return doodle;
    } // getDoodle()

    public void setPathPaint(final Paint pathPaint)
    {
        mPathPaint = pathPaint;
    } // setPathPaint(Paint)

    public PathPoint[] getPathPoints()
    {
        final PathPoint[] pathPoints = new PathPoint[mPathPoints.size()];
        mPathPoints.toArray(pathPoints);
        return pathPoints;
    } // getPathPoints()

    public void setPathPoints(PathPoint[] pathPoints)
    {
        mPathPoints.clear();
        mPath.rewind();
        final int pathPointCount = pathPoints.length;
        float lastX = Float.MIN_VALUE;
        float lastY = Float.MIN_VALUE;
        for (int i = 0; i < pathPointCount; i++)
        {
            final PathPoint pathPoint = pathPoints[i];

            mPathPoints.add(pathPoint);

            final float x = pathPoint.x;
            final float y = pathPoint.y;

            if (pathPoint.joinToPreviousPoint)
            {
               quadTo(x, lastX, y, lastY);
            } // if
            else
            {
                mPath.moveTo(x, y);
            } // else

            lastX = x;
            lastY = y;
        } // for
    } // setPathPoints(PathPoint[])

    public float getTouchTolerance()
    {
        return mTouchTolerance;
    } // getTouchTolerance()

    public void setTouchTolerance(final float touchTolerance)
    {
        mTouchTolerance = touchTolerance;
    } // setTouchTolerance(float)
} // class DoodleView
