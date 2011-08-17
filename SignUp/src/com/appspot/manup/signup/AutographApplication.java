package com.appspot.manup.signup;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;

public final class AutographApplication extends Application
{
    Preferences mPrefs;
    public static final String ACTION_LDAP = AutographApplication.class.getName() + ".LDAP";
    public static final String ACTION_SWIPE = AutographApplication.class.getName() + ".SWIPE";
    public static final String ACTION_STOP_LDAP =
            AutographApplication.class.getName() + ".STOPLDAP";
    public static final String ACTION_STOP_SWIPE =
            AutographApplication.class.getName() + ".STOPSWIPE";

    public AutographApplication()
    {
        super();
    } // AutographApplication

    @Override
    public void onCreate()
    {
        super.onCreate();
        new CheckServicePrefs().execute();

    } // onCreate

    private final class CheckServicePrefs extends AsyncTask<Void, Void, Boolean[]>
    {
        private static final int LDAP = 0, SWIPE = 1;

        @Override
        protected Boolean[] doInBackground(Void... noParams)
        {
            mPrefs = new Preferences(AutographApplication.this);
            final boolean ldap = mPrefs.getLdapPref();
            final boolean swipe = mPrefs.getSwipePref();

            // This needs fixing
            OnSharedPreferenceChangeListener listener =
                    new OnSharedPreferenceChangeListener()
                    {
                        @Override
                        public void onSharedPreferenceChanged(
                                SharedPreferences sharedPreferences, String key)
                        {
                            if (key.equals(Preferences.KEY_LDAP) || key.equals(
                                    Preferences.KEY_SWIPE))
                            {
                                new CheckServicePrefs().execute();
                            }
                        }
                    };
            mPrefs.registerOnPreferenceChangeListener(listener);

            return new Boolean[]{ldap, swipe};
        } // doInBackground

        @Override
        protected void onPostExecute(Boolean[] result)
        {
            if (result[LDAP])
            {
                final Intent i  = new Intent(ACTION_LDAP);
                LdapSwipeService.serviceAction(AutographApplication.this, i);
            }
            else
            {
                final Intent i  = new Intent(ACTION_STOP_LDAP);
                LdapSwipeService.serviceAction(AutographApplication.this, i);
            }
            if (result[SWIPE])
            {
                final Intent i = new Intent(ACTION_SWIPE);
                LdapSwipeService.serviceAction(AutographApplication.this, i);
            }
            else
            {
                final Intent i = new Intent(ACTION_STOP_SWIPE);
                LdapSwipeService.serviceAction(AutographApplication.this, i);
            }

        } // onPostExecute
    } // CheckServicePrefs

} // AutographApplication
