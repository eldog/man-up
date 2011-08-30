package com.appspot.manup.signup.ui;

import com.appspot.manup.signup.SignUpPreferenceActivity;
import com.appspot.manup.signup.Preferences;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;

public abstract class CheckPreferencesActivity extends Activity
{
    private final class CheckPreferencesSet extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(final Void... noParams)
        {
            return new Preferences(CheckPreferencesActivity.this).preferencesSet();
        } // doInBackground

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

} // CheckPreferencesActivity
