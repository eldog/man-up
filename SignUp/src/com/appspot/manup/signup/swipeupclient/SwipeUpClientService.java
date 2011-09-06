package com.appspot.manup.signup.swipeupclient;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.appspot.manup.signup.Preferences;

public final class SwipeUpClientService extends Service
{
    @SuppressWarnings("unused")
    private static final String TAG = SwipeUpClientService.class.getSimpleName();

    public static void controlService(final Context context, final Preferences prefs)
    {
        final Intent intent = new Intent(context, SwipeUpClientService.class);
        if (prefs.listenForSwipeUp())
        {
            context.startService(intent);
        } // if
        else
        {
            context.stopService(intent);
        } // else
    } // controlService(Context, Preferences)

    private SwipeUpThread mSwipeUpThread = null;

    public SwipeUpClientService()
    {
        super();
    } // constructor()

    @Override
    public void onCreate()
    {
        super.onCreate();
        mSwipeUpThread = new SwipeUpThread(this);
        mSwipeUpThread.setDaemon(true);
        mSwipeUpThread.start();
    } // onCreate()

    @Override
    public void onDestroy()
    {
        mSwipeUpThread.interrupt();
        super.onDestroy();
    } // onDestroy()

    @Override
    public IBinder onBind(final Intent intent)
    {
        return null;
    } // onBind(Intent)

} // class SwipeUpClientService
