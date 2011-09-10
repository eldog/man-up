package com.appspot.manup.signup;

import android.app.Application;
import android.app.Service;
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
            controlSwipeUpClientService();
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
            controlSwipeUpClientService();
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
        controlSwipeUpClientService();
        controlUploadService();
    } // controlAllServices()

    private void controlLdapService()
    {
        controlService(LdapService.class, mLdapLookupEnabled);
    } // controlLdapService()

    private void controlSwipeUpClientService()
    {
        controlService(SwipeUpClientService.class, mListenForSwipeUp);
    } // controlSwipeUpService()

    private void controlUploadService()
    {
        controlService(UploadService.class, mShouldUploadSignatures);
    } // controlUploadService()

    private <S extends Service> void controlService(final Class<S> service,
            final boolean startService)
    {
        final Intent intent = new Intent(this, service);

        if (startService && mIsConnected)
        {
            startService(intent);
        } // if
        else
        {
            stopService(intent);
        } // else
    } // controlService(Class, boolean)

} // class SignUpApplication
