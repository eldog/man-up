package uk.ac.manchester.cs.mlo.smsproxy;

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
