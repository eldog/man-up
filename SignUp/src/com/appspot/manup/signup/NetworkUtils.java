package com.appspot.manup.signup;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public final class NetworkUtils
{
    public static InetAddress getLocalIpAddress() throws SocketException
    {
        for (final Enumeration<NetworkInterface> interfaces = NetworkInterface
                .getNetworkInterfaces(); interfaces.hasMoreElements();)
        {
            final NetworkInterface intface = interfaces.nextElement();
            for (final Enumeration<InetAddress> addresses = intface.getInetAddresses(); addresses
                    .hasMoreElements();)
            {
                final InetAddress address = addresses.nextElement();
                if (!address.isLoopbackAddress())
                {
                    return address;
                } // if
            } // for
        }// for
        return null;
    } // getLocalIpAddress()

    public static boolean isOnline(final Context context)
    {
        final ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    } // isOnline(Context)

    private NetworkUtils()
    {
        super();
        throw new AssertionError();
    } // constructor()

} // class NetworkUtils
