package com.appspot.manup.signup.ui;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;

public abstract class AnimatedDoodleView extends DoodleView
{
    private final class Animation extends AsyncTask<Void, Void, Void>
    {
        public Animation()
        {
            super();
        } // constructor()

        @Override
        protected void onPreExecute()
        {
            onPreAnimation();
        } // onPreExecute

        @Override
        protected Void doInBackground(final Void... noParams)
        {
            onPreAnimationBackground();

            do
            {
                try
                {
                    Thread.sleep(mFrameGapMs);
                } // try
                catch (final InterruptedException e)
                {
                    break;
                } // catch
                publishProgress();
            } while(mKeepAnimating && !Thread.interrupted());

                return null;
        } // doInBackground(Void)

        @Override
        protected void onProgressUpdate(final Void... noParams)
        {
            mKeepAnimating = onAnimate();
            invalidate();
        } // onProgressUpdate(Void)

        @Override
        protected void onCancelled()
        {
            cleanUp();
            animationCleanUp();
        } // onCancelled()

        @Override
        protected void onPostExecute(final Void noResult)
        {

            onPostAnimation();
            mOnAnimationFinishCallback.run();
            animationCleanUp();
        } // onPostExecute(Void)

        private void animationCleanUp()
        {
            mAnimation = null;
            mOnAnimationFinishCallback = null;
            mIsAnimating = false;
        } // animationCleanUp()

    } // class Animation

    private Animation mAnimation = null;
    private Runnable mOnAnimationFinishCallback = null;
    private volatile long mFrameGapMs = Long.MIN_VALUE;

    private volatile boolean mIsAnimating = false;
    private volatile boolean mKeepAnimating = false;

    {
        setFramesPerSecond(25);
    }

    public AnimatedDoodleView(final Context context)
    {
        super(context);
    } // constructor(Context)

    public AnimatedDoodleView(final Context context, final AttributeSet attrs)
    {
        super(context, attrs);
    } // constructor(Context, AttributeSet)

    public AnimatedDoodleView(final Context context, final AttributeSet attrs, final int defStyle)
    {
        super(context, attrs, defStyle);
    } // constructor(Context, AttributeSet, int)

    public void animate(final Runnable onAnimationFinishCallback)
    {
       cancelAnimation();
       mIsAnimating = true;
       mOnAnimationFinishCallback = onAnimationFinishCallback;
       mAnimation = (Animation) new Animation().execute();
    } // animate(Runnable)

    public boolean isAnimating()
    {
        return mIsAnimating;
    } // isAnimating()

    public void cancelAnimation()
    {
        if (mAnimation != null)
        {
            mAnimation.cancel(true);
        } // if
        mIsAnimating = false;
    } // cancelAnimation()

    public void setFramesPerSecond(final int fps)
    {
        mFrameGapMs = 1000L / fps;
    } // setFramesPerSecond(int)

    protected void onPreAnimation()
    {
        // Do nothing.
    } // onPreAnimation()

    protected void onPreAnimationBackground()
    {
        // Do nothing.
    } // onPreAnimationBackground()

    protected abstract boolean onAnimate();

    protected void onPostAnimation()
    {
        // Do nothing.
    } // onPostAnimation()

    protected void cleanUp()
    {
        // Do nothing.
    } // cleanUp()

} // class AnimatedDoodleView
