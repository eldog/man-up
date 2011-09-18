package com.appspot.manup.signup.swipeupclient;

import java.io.IOException;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.appspot.manup.signup.R;
import com.appspot.manup.signup.SignUpPreferenceActivity;
import com.appspot.manup.signup.StateReportingService;

public final class SwipeUpClientService extends StateReportingService
{
    private static final String TAG = SwipeUpClientService.class.getSimpleName();

    private static final int NOTIFICATION_ID = 1;

    public static final Object ID = new Object();

    private SwipeUpClient mSwipeUpThread = null;

    public SwipeUpClientService()
    {
        super();
    } // constructor()

    @Override
    public void onCreate()
    {
        super.onCreate();
        startForeground();
        try
        {
            mSwipeUpThread = new SwipeUpClient(this);
        } // try
        catch (IOException e)
        {
            Log.d(TAG, "Bluetooth not supported.", e);
            return;
        } // catch
        mSwipeUpThread.setDaemon(true);
        mSwipeUpThread.start();
        setState(STATE_STARTED);
    } // onCreate()

    private void startForeground()
    {
        final Notification notification = new Notification(
                R.drawable.icon,
                null /* tickerText */,
                0L /* when */);
        notification.setLatestEventInfo(this,
                getString(R.string.notification_title),
                getString(R.string.notification_message),
                PendingIntent.getActivity(
                        this,
                        0 /* requestCode */,
                        new Intent(this, SignUpPreferenceActivity.class),
                        0 /* flags */));
        startForeground(NOTIFICATION_ID, notification);
    } // startForeground()

    @Override
    public void onDestroy()
    {
        mSwipeUpThread.cancel();
        stopForeground(true);
        setState(STATE_STOPPED);
    } // onDestroy()

    @Override
    public Object getId()
    {
        return ID;
    } // getId()

    @Override
    public IBinder onBind(final Intent intent)
    {
        return null;
    } // onBind(Intent)

} // class SwipeUpClientService
