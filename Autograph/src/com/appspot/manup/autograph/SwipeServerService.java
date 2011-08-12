package com.appspot.manup.autograph;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SwipeServerService extends Service
{
    @SuppressWarnings("unused")
    private final static String TAG = SwipeServerService.class.getSimpleName();

    private SwipeServerThread mSwipeServerThread;

    @Override
    public void onCreate()
    {
        mSwipeServerThread = new SwipeServerThread(SignatureDatabase.getInstance(this));
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
