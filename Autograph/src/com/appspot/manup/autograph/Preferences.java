package com.appspot.manup.autograph;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class Preferences
{
    private static final String KEY_HOST = "host";
    private static final String KEY_PORT = "port";

    private final SharedPreferences mPrefs;

    public Preferences(final Context context)
    {
        super();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    } // Preferences

    public String getHost()
    {
        return mPrefs.getString(KEY_HOST, null);
    } // getHost

    public boolean hasHost()
    {
        return (mPrefs.getString(KEY_HOST, null) != null) ? true : false;
    }

    public int getPort()
    {
        final String portString = mPrefs.getString(KEY_PORT, null);
        return portString != null ? Integer.valueOf(portString) : -1;
    } // getPort

    public boolean hasPort()
    {
        return (mPrefs.getString(KEY_PORT, null) != null) ? true : false;
    }

} // Preferences
