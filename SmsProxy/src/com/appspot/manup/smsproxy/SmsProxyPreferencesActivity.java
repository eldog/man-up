package com.appspot.manup.smsproxy;

import com.appspot.manup.smsproxy.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public final class SmsProxyPreferencesActivity extends PreferenceActivity
{
    public SmsProxyPreferencesActivity()
    {
        super();
    } // SmsProxyPreferencesActivity

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    } // onCreate

} // SmsProxyPreferencesActivity
