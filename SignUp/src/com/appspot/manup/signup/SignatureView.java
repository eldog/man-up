package com.appspot.manup.signup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.appspot.manup.signup.ui.AnimatedDoodleView;
import com.appspot.manup.signup.ui.PathPoint;

public final class SignatureView extends AnimatedDoodleView
{
    private static final float MIN_DISTANCES = 10.0f;

    private PathPoint[] mPath = null;
    private int mEnd = Integer.MIN_VALUE;

    private PathPoint[] mPathStarts = null;
    private int[] mPathStartsPos = null;
    private int mPathStartsCount = Integer.MIN_VALUE;

    private volatile Bitmap mPaperHole = null;
    private volatile float mPaperHoleXOffset = Float.MIN_VALUE;
    private volatile float mPaperHoleYOffset = Float.MIN_VALUE;

    {
        final Paint pathPaint = new Paint();
        pathPaint.setAntiAlias(true);
        pathPaint.setDither(true);
        pathPaint.setColor(Color.BLACK);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);
        pathPaint.setStrokeJoin(Paint.Join.ROUND);
        pathPaint.setStrokeWidth(10.0f);
        pathPaint.setStyle(Paint.Style.STROKE);
        setPathPaint(pathPaint);
        setFramesPerSecond(25);
    }

    public SignatureView(final Context context)
    {
        super(context);
    } // constructor(Context)

    public SignatureView(final Context context, final AttributeSet attrs)
    {
        super(context, attrs);
    } // constructor(Context, AttributeSet)

    public SignatureView(final Context context, final AttributeSet attrs, final int defStyle)
    {
        super(context, attrs, defStyle);
    } // constructor(Context, AttributeSet, int)

    @Override
    protected void onPreAnimation()
    {
        final PathPoint[] path = getPathPoints();

        mPath = new PathPoint[path.length];
        mEnd = 0;

        mPathStarts = new PathPoint[path.length];
        mPathStartsPos = new int[path.length];
        mPathStartsCount = 0;

        for (int i = 0; i < mPath.length; i++)
        {
            final PathPoint point = path[i];
            if (!point.joinToPreviousPoint)
            {
                mPathStarts[mPathStartsCount] = point;
                mPathStartsPos[mPathStartsCount++] = i;
            } // if
            mPath[mEnd++] = point;
        } // for

    } // onPreAnimation()

    @Override
    protected void onPreAnimationBackground()
    {
        mPaperHole = BitmapFactory.decodeResource(getContext().getResources(), R.raw.paper_hole);
        mPaperHoleXOffset = mPaperHole.getWidth() / 2.0f;
        mPaperHoleYOffset = mPaperHole.getHeight() / 2.0f;
    } // onPreAnimationBackground()

    @Override
    protected boolean onAnimate()
    {
        mEnd--;

        if (mEnd <= 0)
        {
            return false;
        }

        PathPoint current = mPath[mEnd - 1];

        for (float dist = 0.0f; mEnd > 1 && dist < MIN_DISTANCES; mEnd--)
        {
            final PathPoint next = mPath[mEnd - 2];
            if (!next.joinToPreviousPoint)
            {
                mEnd--;
                mPathStartsCount--;
                break;
            } // if
            dist += current.distanceBetween(next);
            current = next;
        } // for

        if (mEnd <= 0)
        {
            return false;
        } // if

        while (mPathStartsCount > 1 && mPathStartsPos[mPathStartsCount - 1] >= mEnd)
        {
            mPathStartsCount--;
        } // while

        final PathPoint[] smallerPath = new PathPoint[mEnd];
        System.arraycopy(mPath, 0, smallerPath, 0, mEnd);
        setPathPoints(smallerPath);
        return true;
    } // onAnimate()

    @Override
    protected void onDraw(final Canvas canvas)
    {
        if (mPaperHole != null)
        {
            for (int i = 0; i < mPathStartsCount; i++)
            {
                final PathPoint point = mPathStarts[i];
                canvas.drawBitmap(
                        mPaperHole,
                        point.x - mPaperHoleXOffset,
                        point.y - mPaperHoleYOffset,
                        null /* paint */);
            } // for
        }
        super.onDraw(canvas);
    } // onDraw

} // class SigntureView
