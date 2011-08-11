package com.appspot.manup.autograph;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

public class CheckPreferencesActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        new CheckPreferencesSet().execute();
    }

    private final class CheckPreferencesSet extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(Void... params)
        {
            Preferences prefs = new Preferences(CheckPreferencesActivity.this);
            if (!prefs.hasHost() || !prefs.hasPort())
            {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            if (!result)
            {
                startActivity(new Intent(CheckPreferencesActivity.this,
                        SignaturePreferenceActivity.class));
            }
        }

    }
}
