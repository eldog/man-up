package com.appspot.manup.signup;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public final class SignUpPreferenceActivity extends PreferenceActivity
{
    @SuppressWarnings("unused")
    private static final String TAG = SignUpPreferenceActivity.class.getSimpleName();

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

} // class SignaturePreferenceActivity
