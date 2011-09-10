package com.appspot.manup.signup.swipeupclient;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public final class SwipeUpClientService extends Service
{
    @SuppressWarnings("unused")
    private static final String TAG = SwipeUpClientService.class.getSimpleName();

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
