package com.appspot.manup.signup;

import com.appspot.manup.signup.data.DataManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public final class NetworkStateBroadcastReceiver extends BroadcastReceiver
{
    @SuppressWarnings("unused")
    private static final String TAG = NetworkStateBroadcastReceiver.class.getSimpleName();

    public NetworkStateBroadcastReceiver()
    {
        super();
    } // InternetBroadcastReceiver

    @Override
    public void onReceive(final Context context, final Intent intent)
    {
        final NetworkInfo info =
                (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
        DataManager.getInstance(context).setNotifyOnSignatureCaptured(info.isConnected());
    } // onReceive

} // InternetBroadcastReceiver
