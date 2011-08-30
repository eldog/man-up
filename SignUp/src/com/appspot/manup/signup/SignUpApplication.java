package com.appspot.manup.signup;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.util.Log;

import com.appspot.manup.signup.data.DataManager;
import com.appspot.manup.signup.data.DataManager.MemberAddedListener;
import com.appspot.manup.signup.data.DataManager.SignatureCapturedListener;
import com.appspot.manup.signup.ldap.LdapService;
import com.appspot.manup.signup.swipeupclient.SwipeUpClientService;

public final class SignUpApplication extends Application
{
    private static final String TAG = SignUpApplication.class.getSimpleName();

    private final class SignUpInitialiser extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(final Void... noParams)
        {
            final Preferences prefs = new Preferences(SignUpApplication.this);
            mDataManager = DataManager.getInstance(SignUpApplication.this);
            mDataManager.registerSignatureCapturedListener(mSignatureCapturedListener);
            prefs.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
            configureLdapLook(prefs);
            configureListenForSwipeUp(prefs);
            return null;
        } // doInBackground

    } // CheckServicePrefs

    private final OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener =
            new OnSharedPreferenceChangeListener()
    {
        @Override
        public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences,
                final String key)
        {
            final Preferences prefs = new Preferences(sharedPreferences);
            if (prefs.isLdapLookEnabledKey(key))
            {
                configureLdapLook(prefs);
            } // if
            else if (prefs.isListenForSwipeUpKey(key))
            {
                configureListenForSwipeUp(prefs);
            } // else if
        } // onSharedPreferenceChanged
    };

    private final MemberAddedListener mMemberAddedListener = new MemberAddedListener()
    {
        @Override
        public void onMemberAdded(final long id)
        {
            final Intent intent = new Intent(SignUpApplication.this, LdapService.class);
            intent.putExtra(LdapService.EXTRA_ID, id);
            startService(intent);
        } // onMemberAdded
    };

    private final SignatureCapturedListener mSignatureCapturedListener = new SignatureCapturedListener()
    {
        @Override
        public void onSignatureCaptured(final long id)
        {
            startService(new Intent(SignUpApplication.this, UploadService.class));
        } // onSignatureCaptured
    };

    private volatile DataManager mDataManager = null;

    public SignUpApplication()
    {
        super();
    } // constructor

    @Override
    public void onCreate()
    {
        super.onCreate();
        new SignUpInitialiser().execute();
    } // onCreate

    private void configureLdapLook(final Preferences prefs)
    {
        if (prefs.ldapLookupEnabled())
        {
            mDataManager.registerMemberAddedListener(mMemberAddedListener);
        } // if
        else
        {
            mDataManager.unregisterMemberAddedListener(mMemberAddedListener);
        } // else
    } // configureLdapLook

    private void configureListenForSwipeUp(final Preferences prefs)
    {
        final Intent intent = new Intent(this, SwipeUpClientService.class);
        if (prefs.listenForSwipeUp())
        {
            Log.d(TAG, "Turning it on.");
            startService(intent);
        } // if
        else
        {
            stopService(intent);
        } // else
    } // configureListenForSwipeUp

} // class SignUpApplication
