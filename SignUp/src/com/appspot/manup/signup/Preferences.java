package com.appspot.manup.signup;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public final class Preferences
{
    /*
     * If you change the keys, check if res/xml/preferences.xml also needs
     * changing.
     */
    private static final String KEY_LDAP_LOOKUP_ENABLED = "ldap_lookup_enabled";
    private static final String KEY_LISTEN_FOR_SWIPEUP = "listen_for_swipeup";
    private static final String KEY_HOST = "host";
    private static final String KEY_PORT = "port";
    private static final String KEY_LDAP_HOST = "cs_host";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    private final SharedPreferences mPrefs;

    public Preferences(final Context context)
    {
        this(PreferenceManager.getDefaultSharedPreferences(context));
    } // constructor

    public Preferences(final SharedPreferences prefs)
    {
        super();
        mPrefs = prefs;
    } // constructor

    public void registerOnSharedPreferenceChangeListener(
            final OnSharedPreferenceChangeListener listener)
    {
        mPrefs.registerOnSharedPreferenceChangeListener(listener);
    } // registerOnSharedPreferenceChangeListener

    public void unregisterOnSharedPreferenceChangeListener(
            final OnSharedPreferenceChangeListener listener)
    {
        mPrefs.unregisterOnSharedPreferenceChangeListener(listener);
    } // unregisterOnSharedPreferenceChangeListener

    public boolean ldapLookupEnabled()
    {
        return mPrefs.getBoolean(KEY_LDAP_LOOKUP_ENABLED, false);
    } // ldapLookupEnabled

    public boolean listenForSwipeUp()
    {
        return mPrefs.getBoolean(KEY_LISTEN_FOR_SWIPEUP, false);
    } // listenForSwipeUp

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

    public String getLdapHost()
    {
        return mPrefs.getString(KEY_LDAP_HOST, null);
    } // getCsHost

    public boolean hasLdapHost()
    {
        return hasPreference(KEY_LDAP_HOST);
    } // hasCsHost

    public String getLdapUsername()
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
        return hasHost() && hasPort() && hasLdapHost() && hasUsername() && hasPassword();
    } // preferencesSet

    public boolean isLdapLookEnabledKey(final String key)
    {
        return KEY_LDAP_LOOKUP_ENABLED.equals(key);
    } // isLdapLookEnabledKey

    public boolean isListenForSwipeUpKey(final String key)
    {
        return KEY_LISTEN_FOR_SWIPEUP.equals(key);
    } // isListenForSwipeUpKey

} // class Preferences
