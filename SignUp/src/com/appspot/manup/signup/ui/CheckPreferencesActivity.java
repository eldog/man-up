package com.appspot.manup.signup.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;

import com.appspot.manup.signup.Preferences;
import com.appspot.manup.signup.SignUpPreferenceActivity;

public abstract class CheckPreferencesActivity extends Activity
{
    private final class CheckPreferencesSet extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(final Void... noParams)
        {
            final Preferences prefs = new Preferences(CheckPreferencesActivity.this);
            mIsInAdminMode = prefs.isInAdminMode();
            return prefs.areRequiredPreferencesSet();
        } // doInBackground(Void)

        @Override
        protected void onCancelled()
        {
            onStop();
        } // onCancelled

        @Override
        protected void onPostExecute(final Boolean prefsSet)
        {
            onStop();
            if (!prefsSet)
            {
                startActivity(new Intent(CheckPreferencesActivity.this,
                        SignUpPreferenceActivity.class));
            } // if
        } // onPostExecute

        private void onStop()
        {
            mCheckPreferencesSet = null;
        } // onStop

    } // CheckPreferencesSet

    private CheckPreferencesSet mCheckPreferencesSet = null;

    private volatile boolean mIsInAdminMode = false;

    @Override
    protected void onResume()
    {
        super.onResume();
        mCheckPreferencesSet = (CheckPreferencesSet) new CheckPreferencesSet().execute();
    } // onResume

    @Override
    protected void onPause()
    {
        if (mCheckPreferencesSet != null)
        {
            mCheckPreferencesSet.cancel(true);
            mCheckPreferencesSet = null;
        } // if
        super.onPause();
    } // onPause

    protected boolean isInAdminMode()
    {
        return mIsInAdminMode;
    } // isInAdminMode()

} // CheckPreferencesActivity
