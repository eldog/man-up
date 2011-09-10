package com.appspot.manup.signup.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.appspot.manup.signup.Preferences;
import com.appspot.manup.signup.SignUpPreferenceActivity;

public abstract class BaseActivity extends Activity
{
    @SuppressWarnings("unused")
    private static final String TAG = BaseActivity.class.getSimpleName();

    private final class PreferencesLoader extends AsyncTask<Void, Void, Preferences>
    {
        @Override
        protected Preferences doInBackground(final Void... noParams)
        {
            return new Preferences(BaseActivity.this);
        } // doInBackground(Void)

        @Override
        protected void onCancelled()
        {
            onStop();
        } // onCancelled()

        @Override
        protected void onPostExecute(final Preferences prefs)
        {
            onStop();
            onCreateWithPreferences(prefs);
            synchronized (mLock)
            {
                mPrefs = prefs;
                mLock.notifyAll();
            } // synchronized
        } // onPostExecute(Preferences)

        private void onStop()
        {
            mLoader = null;
        } // onStop()

    } // class PreferencesLoader

    private final class OnResumeWithPreferencesCaller extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(final Void... noParams)
        {
            synchronized (mLock)
            {
                if (mPrefs == null)
                {
                    try
                    {
                        mLock.wait();
                    } // try
                    catch (final InterruptedException e)
                    {
                        // Return.
                    } // catch
                } // if
            } // synchronized
            return null;
        } // doInBackground(Void)

        @Override
        protected void onCancelled()
        {
            onStop();
        } // onCancelled()

        @Override
        protected void onPostExecute(final Void noResult)
        {
            onStop();
            onResumeWithPreferences(mPrefs);
        } // onPostExecute(Void)

        private void onStop()
        {
            mOnResumeWithPreferencesCaller = null;
        } // onStop()

    } // class OnResumeWithPreferencesCaller

    private final Object mLock = new Object();

    private PreferencesLoader mLoader = null;
    private OnResumeWithPreferencesCaller mOnResumeWithPreferencesCaller = null;
    private Preferences mPrefs = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mLoader = (PreferencesLoader) new PreferencesLoader().execute();
    } // onCreate

    protected void onCreateWithPreferences(final Preferences prefs)
    {
        // Do nothing.
    } // onCreateWithPreferences(Preferences)

    @Override
    protected void onResume()
    {
        super.onResume();
        if (mPrefs != null)
        {
            onResumeWithPreferences(mPrefs);
        } // if
        else
        {
            mOnResumeWithPreferencesCaller = (OnResumeWithPreferencesCaller)
                    new OnResumeWithPreferencesCaller().execute();
        } // else
    } // onResume()

    protected void onResumeWithPreferences(final Preferences prefs)
    {
        if (!mPrefs.areRequiredPreferencesSet())
        {
            startActivity(new Intent(this, SignUpPreferenceActivity.class));
        } // if
    } // onResumeWithPreferences(Preferences)

    @Override
    protected void onPause()
    {
        if (mLoader != null)
        {
            mLoader.cancel(true);
        } // if
        if (mOnResumeWithPreferencesCaller != null)
        {
            mOnResumeWithPreferencesCaller.cancel(true);
        } // if
        super.onPause();
    } // onPause()

} // class BaseActivity
