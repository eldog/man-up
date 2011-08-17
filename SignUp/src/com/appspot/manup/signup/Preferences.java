package com.appspot.manup.signup;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public final class Preferences
{
    private static final String KEY_HOST = "host";
    private static final String KEY_PORT = "port";
    private static final String KEY_LDAP_HOST = "cs_host";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

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
        return hasPreference(KEY_HOST);
    } // hasHost

    public int getPort()
    {
        final String portString = mPrefs.getString(KEY_PORT, null);
        return portString != null ? Integer.valueOf(portString) : -1;
    } // getPort

    public boolean hasPort()
    {
        return hasPreference(KEY_PORT);
    } // hasPort

    public String getCsHost()
    {
        return mPrefs.getString(KEY_LDAP_HOST, null);
    } // getCsHost

    public boolean hasCsHost()
    {
        return hasPreference(KEY_LDAP_HOST);
    } // hasCsHost

    public String getUsername()
    {
        return mPrefs.getString(KEY_USERNAME, null);
    } // getUsername

    public boolean hasUsername()
    {
        return hasPreference(KEY_USERNAME);
    } // hasUsername

    public String getPassword()
    {
        return mPrefs.getString(KEY_PASSWORD, null);
    } // getPassword

    public boolean hasPassword()
    {
        return hasPreference(KEY_PASSWORD);
    } // hasPassword

    private boolean hasPreference(String preference)
    {
        return !TextUtils.isEmpty(mPrefs.getString(preference, null));
    } // hasPreference

    public boolean preferencesSet()
    {
        return hasHost() && hasPort() && hasCsHost() && hasUsername() && hasPassword();
    } // preferencesSet

} // Preferences
