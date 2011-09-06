package com.appspot.manup.signup;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.appspot.manup.signup.ldap.LdapService;
import com.appspot.manup.signup.swipeupclient.SwipeUpClientService;

public final class SignUpPreferenceActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener
{
    @SuppressWarnings("unused")
    private static final String TAG = SignUpPreferenceActivity.class.getSimpleName();

    private final class RegisterListeners extends AsyncTask<Void, Void, Preferences>
    {
        @Override
        protected Preferences doInBackground(final Void... noParams)
        {
            return new Preferences(SignUpPreferenceActivity.this);
        } // doInBackground(Void)

        @Override
        protected void onPostExecute(final Preferences prefs)
        {
            cleanUp();
            mPrefs = prefs;
            mPrefs.registerOnSharedPreferenceChangeListener(SignUpPreferenceActivity.this);
        } // onPostExecute(SharedPreferences)

        @Override
        protected void onCancelled()
        {
            cleanUp();
        } // onCancelled()

        private void cleanUp()
        {
            mRegisterListeners = null;
        } // cleanUp()

    } // class RegisterLiseners

    private RegisterListeners mRegisterListeners = null;
    private Preferences mPrefs = null;

    public SignUpPreferenceActivity()
    {
        super();
    } // constructor()

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    } // onCreate()

    @Override
    protected void onResume()
    {
        super.onResume();
        if (mPrefs != null)
        {
            mPrefs.registerOnSharedPreferenceChangeListener(this);
        } // if
        else
        {
            mRegisterListeners = (RegisterListeners) new RegisterListeners().execute();
        } // else
    } // onResume()

    @Override
    protected void onPause()
    {
        if (mRegisterListeners != null)
        {
            mRegisterListeners.cancel(true);
            mRegisterListeners = null;
        } // if
        if (mPrefs != null)
        {
            mPrefs.unregisterOnSharedPreferenceChangeListener(this);
        } // if
        super.onPause();
    } // onPause()

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences,
            final String key)
    {
        final Preferences prefs = new Preferences(sharedPreferences);
        if (prefs.isLdapLookEnabledKey(key))
        {
            LdapService.controlService(this, prefs);
        } // if
        else if (prefs.isListenForSwipeUpKey(key))
        {
            SwipeUpClientService.controlService(this, prefs);
        } // else if
    } // onSharedPreferenceChanged(SharedPreferences, String)

} // class SignaturePreferenceActivity
