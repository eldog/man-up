package com.appspot.manup.smsproxy.proxy;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.Intent;


public final class SmsProxyManager
{
    private static final Object sListenerLock = new Object();
    private static final Set<SmsProxyListener> sListeners = new HashSet<SmsProxyListener>();
    private static boolean sRunning = false;

    public static void toggleSmsProxy(final Context context)
    {
        synchronized (sListenerLock)
        {
            if (sRunning)
            {
                stopSmsProxy(context);
            } // if
            else
            {
                startSmsProxy(context);
            } // else
        } // synchronized
    } // toggleSmsProxy

    public static void startSmsProxy(final Context context)
    {
        context.startService(getIntent(context));
    } // startSmsProxy

    public static void stopSmsProxy(final Context context)
    {
        context.stopService(getIntent(context));
    } // stopSmsProxy

    private static Intent getIntent(final Context context)
    {
        return new Intent(context, SmsProxyService.class);
    } // getIntent

    public static void registerListener(final SmsProxyListener listener)
    {
        synchronized (sListenerLock)
        {
            sListeners.add(listener);
            listener.onRegister(sRunning);
        } // synchronized
    } // registerListener

    public static void unregisterListener(final SmsProxyListener listener)
    {
        synchronized (sListenerLock)
        {
            sListeners.remove(listener);
        } // synchronized
    } // unregisterListener

    static void sendMessage(final String message)
    {
        synchronized (sListenerLock)
        {
            for (final SmsProxyListener listener : sListeners)
            {
                listener.onMessage(message);
            } // for
        } // synchronized
    } // sendMessage

    static void setRunning(final boolean running)
    {
        synchronized (sListenerLock)
        {
            sRunning = running;
            for (final SmsProxyListener listener : sListeners)
            {
                if (running)
                {
                    listener.onStart();
                } // if
                else
                {
                    listener.onStop();
                } // else
            } // for
        } // synchronized
    } // setIsRunning

    private SmsProxyManager()
    {
        throw new AssertionError("SmsProxyManager cannot be instantiated.");
    } // SmsProxyManager

} // SmsProxyManager
