package com.appspot.manup.signature;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public final class SignaturePreferenceActivity extends PreferenceActivity
{
    public SignaturePreferenceActivity()
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
