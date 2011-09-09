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

    private static final String KEY_ADMIN_MODE = "admin_mode";
    private static final String KEY_CS_HOST = "cs_host";
    private static final String KEY_CS_PASSWORD = "cs_password";
    private static final String KEY_CS_USERNAME = "cs_username";
    private static final String KEY_LDAP_LOOKUP_ENABLED = "ldap_lookup_enabled";
    private static final String KEY_LISTEN_FOR_SWIPEUP = "listen_for_swipeup";
    private static final String KEY_SWIPEUP_HOST = "swipeup_host";
    private static final String KEY_SWIPEUP_PORT = "swipeup_port";

    private final SharedPreferences mPrefs;

    public Preferences(final Context context)
    {
        this(PreferenceManager.getDefaultSharedPreferences(context));
    } // constructor(Context)

    public Preferences(final SharedPreferences prefs)
    {
        super();
        mPrefs = prefs;
    } // constructor(SharedPreferences)

    public void registerOnSharedPreferenceChangeListener(
            final OnSharedPreferenceChangeListener listener)
    {
        mPrefs.registerOnSharedPreferenceChangeListener(listener);
    } // registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener)

    public void unregisterOnSharedPreferenceChangeListener(
            final OnSharedPreferenceChangeListener listener)
    {
        mPrefs.unregisterOnSharedPreferenceChangeListener(listener);
    } // unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener)

    public boolean isInAdminMode()
    {
        return mPrefs.getBoolean(KEY_ADMIN_MODE, false);
    } // isInAdminMode()

    public boolean ldapLookupEnabled()
    {
        return mPrefs.getBoolean(KEY_LDAP_LOOKUP_ENABLED, false);
    } // ldapLookupEnabled()

    public boolean listenForSwipeUp()
    {
        return mPrefs.getBoolean(KEY_LISTEN_FOR_SWIPEUP, false);
    } // listenForSwipeUp()

    public String getSwipeUpHost()
    {
        return mPrefs.getString(KEY_SWIPEUP_HOST, null);
    } // getSwipeUpHost()

    public boolean hasSwipeUpHost()
    {
        return isPreferenceSet(KEY_SWIPEUP_HOST);
    } // hasSwipeUpHost()

    public int getSwipeUpPort()
    {
        final String portString = mPrefs.getString(KEY_SWIPEUP_PORT, null);
        return portString != null ? Integer.valueOf(portString) : -1;
    } // getSwipeUpPort()

    public boolean hasSwipePort()
    {
        return isPreferenceSet(KEY_SWIPEUP_PORT);
    } // hasSwipePort()

    public String getCsHost()
    {
        return mPrefs.getString(KEY_CS_HOST, null);
    } // getCsHost()

    public boolean hasCsHost()
    {
        return isPreferenceSet(KEY_CS_HOST);
    } // hasCsHost()

    public String getCsUsername()
    {
        return mPrefs.getString(KEY_CS_USERNAME, null);
    } // getCsUsername()

    public boolean hasCsUsername()
    {
        return isPreferenceSet(KEY_CS_USERNAME);
    } // hasCsUsername()

    public String getCsPassword()
    {
        return mPrefs.getString(KEY_CS_PASSWORD, null);
    } // getCsPassword()

    public boolean hasCsPassword()
    {
        return isPreferenceSet(KEY_CS_PASSWORD);
    } // hasCsPassword()

    private boolean isPreferenceSet(final String preference)
    {
        return !TextUtils.isEmpty(mPrefs.getString(preference, null));
    } // hasPreference(String)

    public boolean areRequiredPreferencesSet()
    {
        return hasSwipeUpHost() && hasSwipePort() && hasCsHost() && hasCsUsername()
                && hasCsPassword();
    } // areRequiredPreferencesSet()

    public boolean isLdapLookEnabledKey(final String key)
    {
        return KEY_LDAP_LOOKUP_ENABLED.equals(key);
    } // isLdapLookEnabledKey(String)

    public boolean isListenForSwipeUpKey(final String key)
    {
        return KEY_LISTEN_FOR_SWIPEUP.equals(key);
    } // isListenForSwipeUpKey(String)

} // class Preferences
