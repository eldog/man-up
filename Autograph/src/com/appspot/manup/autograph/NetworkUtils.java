package com.appspot.manup.autograph;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class NetworkUtils
{
    static InetAddress getLocalIpAddress() throws SocketException
    {
        // Using Enumeration instead of Iterator as NetworkInterface is some
        // ancient relic and getNetworkInterfaces() returns an Enumeration
        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
                .hasMoreElements();)
        {
            NetworkInterface intf = en.nextElement();
            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                    .hasMoreElements();)
            {
                InetAddress inetAddress = enumIpAddr.nextElement();
                if (!inetAddress.isLoopbackAddress())
                {
                    return inetAddress;
                } // if
            } // for
        }// for
        return null;
    } // getLocalIpAddress

    private NetworkUtils()
    {
        super();
        throw new AssertionError();
    } // NetworkUtils
}
