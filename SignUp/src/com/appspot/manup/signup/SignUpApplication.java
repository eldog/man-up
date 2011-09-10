package com.appspot.manup.signup;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.appspot.manup.signup.data.DataManager;
import com.appspot.manup.signup.data.DataManager.OnChangeListener;
import com.appspot.manup.signup.ldap.LdapService;
import com.appspot.manup.signup.swipeupclient.SwipeUpClientService;

public final class SignUpApplication extends Application implements OnChangeListener,
        OnSharedPreferenceChangeListener
{
    @SuppressWarnings("unused")
    private static final String TAG = SignUpApplication.class.getSimpleName();

    private final BroadcastReceiver mNetworkReciever = new BroadcastReceiver()
    {
        @Override
        public void onReceive(final Context context, final Intent intent)
        {
            final NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(
                    ConnectivityManager.EXTRA_NETWORK_INFO);
            mIsConnected = info.isConnected();
            controlSwipeUpService();
        } // onReceive(Context, Intent)
    };

    private volatile boolean mIsConnected = false;
    private volatile boolean mLdapLookupEnabled = false;
    private volatile boolean mShouldUploadSignatures = false;
    private volatile boolean mListenForSwipeUp = false;

    public SignUpApplication()
    {
        super();
    } // constructor()

    @Override
    public void onCreate()
    {
        new Thread()
        {
            public void run()
            {
                init();
            } // run()
        }.start();

        registerReceiver(mNetworkReciever,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        DataManager.registerListener(this);
    } // onCreate()

    private void init()
    {
        final Preferences prefs = new Preferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        mLdapLookupEnabled = prefs.ldapLookupEnabled();
        mListenForSwipeUp = prefs.listenForSwipeUp();
        controlAllServices();
    } // loadPreferences()

    @Override
    public void onChange(final DataManager dataManager)
    {
        controlAllServices();
    } // onChange(DataManager)

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences,
            final String key)
    {
        final Preferences prefs = new Preferences(sharedPreferences);

        if (prefs.isLdapLookEnabledKey(key))
        {
            mLdapLookupEnabled = prefs.ldapLookupEnabled();
            controlLdapService();
        } // if
        else if (prefs.isListenForSwipeUpKey(key))
        {
            mListenForSwipeUp = prefs.listenForSwipeUp();
            controlSwipeUpService();
        } // else if
        else if (prefs.isShouldUploadSignaturesKey(key))
        {
            mShouldUploadSignatures = prefs.shouldUploadSignatures();
            controlUploadService();
        } // else if
    } // onSharedPreferenceChanged(SharedPreferences, String)

    private void controlAllServices()
    {
        controlLdapService();
        controlSwipeUpService();
        controlUploadService();
    } // controlAllServices()

    private void controlLdapService()
    {
        if (mLdapLookupEnabled && mIsConnected)
        {
            startService(new Intent(this, LdapService.class));
        } // if
    } // controlLdapService()

    private void controlSwipeUpService()
    {
        final Intent intent = new Intent(this, SwipeUpClientService.class);

        if (mListenForSwipeUp && mIsConnected)
        {
            startService(intent);
        } // if
        else
        {
            stopService(intent);
        } // else
    } // controlSwipeUpService()

    private void controlUploadService()
    {
        if (mShouldUploadSignatures && mIsConnected)
        {
            startService(new Intent(this, UploadService.class));
        } // if
    } // controlUploadService()

} // class SignUpApplication
