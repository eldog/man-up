package com.appspot.manup.signature;

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
        final Intent uploadServiceIntent = new Intent(context, UploadService.class);
        if (isProbablyOnline(context))
        {
            context.startService(uploadServiceIntent);
        } //if
    } // onReceive

    private boolean isProbablyOnline(final Context context)
    {
        final ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return info != null && info.getState() == NetworkInfo.State.CONNECTED;
    } // isProbablyOnline

} // InternetBroadcastReceiver
