package com.appspot.manup.signup;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public final class AutographPreferenceActivity extends PreferenceActivity
{
    public AutographPreferenceActivity()
    {
        super();
    } // SignaturePreferenceActivity

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    } // onCreate

} // SignaturePreferenceActivity
