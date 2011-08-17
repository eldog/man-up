package com.appspot.manup.signup.swipeupclient;

import com.appspot.manup.signup.data.DataManager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SwipeUpService extends Service
{
    @SuppressWarnings("unused")
    private final static String TAG = SwipeUpService.class.getSimpleName();

    private SwipeUpThread mSwipeServerThread;

    @Override
    public void onCreate()
    {
        mSwipeServerThread = new SwipeUpThread(DataManager.getInstance(this));
        mSwipeServerThread.start();
    }

    @Override
    public void onDestroy()
    {
        mSwipeServerThread.interrupt();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

}
