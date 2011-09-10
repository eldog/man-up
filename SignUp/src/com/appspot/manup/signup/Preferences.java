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
    private static final String KEY_MAN_UP_HOST = "man_up_host";
    private static final String KEY_MAN_UP_PORT = "man_up_port";
    private static final String KEY_SHOULD_UPLOAD_SIGNATURES = "should_upload_signatures";
    private static final String KEY_SWIPEUP_HOST = "swipeup_host";

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

    private boolean getBoolean(final String key)
    {
        return mPrefs.getBoolean(key, false);
    } // getBoolean(String)

    private String getString(final String key)
    {
        return getString(key, null);
    } // getString(String)

    private String getString(final String key, final String defValue)
    {
        return mPrefs.getString(key, defValue);
    } // getString(String, String)

    private int getIntFromString(final String key)
    {
        final String value = getString(key);
        return (value != null) ? Integer.valueOf(value) : -1;
    } // getIntFromString(String)

    public boolean isInAdminMode()
    {
        return getBoolean(KEY_ADMIN_MODE);
    } // isInAdminMode()

    public boolean ldapLookupEnabled()
    {
        return getBoolean(KEY_LDAP_LOOKUP_ENABLED);
    } // ldapLookupEnabled()

    public boolean listenForSwipeUp()
    {
        return getBoolean(KEY_LISTEN_FOR_SWIPEUP);
    } // listenForSwipeUp()

    public boolean shouldUploadSignatures()
    {
        return getBoolean(KEY_SHOULD_UPLOAD_SIGNATURES);
    } // shouldUploadSignatures()

    public String getSwipeUpHost()
    {
        return getString(KEY_SWIPEUP_HOST);
    } // getSwipeUpHost()

    public boolean hasSwipeUpHost()
    {
        return isPreferenceSet(KEY_SWIPEUP_HOST);
    } // hasSwipeUpHost()

    public String getCsHost()
    {
        return getString(KEY_CS_HOST);
    } // getCsHost()

    public boolean hasCsHost()
    {
        return isPreferenceSet(KEY_CS_HOST);
    } // hasCsHost()

    public String getCsUsername()
    {
        return getString(KEY_CS_USERNAME);
    } // getCsUsername()

    public boolean hasCsUsername()
    {
        return isPreferenceSet(KEY_CS_USERNAME);
    } // hasCsUsername()

    public String getCsPassword()
    {
        return getString(KEY_CS_PASSWORD);
    } // getCsPassword()

    public boolean hasCsPassword()
    {
        return isPreferenceSet(KEY_CS_PASSWORD);
    } // hasCsPassword()

    public String getManUpHost()
    {
        return getString(KEY_MAN_UP_HOST);
    } // getManUpHost()

    public boolean hasManUpHost()
    {
        return isPreferenceSet(KEY_MAN_UP_HOST);
    } // hasManUpHost()

    public int getManUpPort()
    {
        return getIntFromString(KEY_MAN_UP_PORT);
    } // getManUpPort()

    public boolean hasManUpPort()
    {
        return isPreferenceSet(KEY_MAN_UP_PORT);
    } // hasManUpPort()

    private boolean isPreferenceSet(final String preference)
    {
        return !TextUtils.isEmpty(mPrefs.getString(preference, null));
    } // hasPreference(String)

    public boolean areRequiredPreferencesSet()
    {
        return hasSwipeUpHost() && hasCsHost() && hasCsUsername() && hasCsPassword()
                && hasManUpHost() && hasManUpPort();
    } // areRequiredPreferencesSet()

    public boolean isLdapLookEnabledKey(final String key)
    {
        return KEY_LDAP_LOOKUP_ENABLED.equals(key);
    } // isLdapLookEnabledKey(String)

    public boolean isListenForSwipeUpKey(final String key)
    {
        return KEY_LISTEN_FOR_SWIPEUP.equals(key);
    } // isListenForSwipeUpKey(String)

    public boolean isShouldUploadSignaturesKey(final String key)
    {
        return KEY_SHOULD_UPLOAD_SIGNATURES.equals(key);
    } // isShouldUploadSignaturesKey(String)

} // class Preferences
