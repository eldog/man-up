package com.appspot.manup.signup.swipeupclient;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.IBinder;

import com.appspot.manup.signup.R;
import com.appspot.manup.signup.SignUpPreferenceActivity;
import com.appspot.manup.signup.StateReportingService;

public final class SwipeUpClientService extends StateReportingService
{
    @SuppressWarnings("unused")
    private static final String TAG = SwipeUpClientService.class.getSimpleName();

    private static final int NOTIFICATION_ID = 1;

    public static final Object ID = new Object();

    private SwipeUpThread mSwipeUpThread = null;

    public SwipeUpClientService()
    {
        super();
    } // constructor()

    @Override
    public void onCreate()
    {
        super.onCreate();
        startForeground();
        mSwipeUpThread = new SwipeUpThread(this);
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
        mSwipeUpThread.interrupt();
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
