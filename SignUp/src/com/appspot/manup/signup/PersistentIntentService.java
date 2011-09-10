package com.appspot.manup.signup;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

public abstract class PersistentIntentService extends Service
{
    private static final String TAG = PersistentIntentService.class.getSimpleName();

    private final class ServiceHandler extends Handler
    {
        public ServiceHandler(final Looper looper)
        {
            super(looper);
        } // constructor(Looper)

        @Override
        public void handleMessage(final Message msg)
        {
            onHandleIntent((Intent) msg.obj);
        } // handleMessage(Message)
    } // class ServiceHandler

    private final String mName;
    private boolean mRedelivery = false;
    private volatile Looper mServiceLooper = null;
    private volatile ServiceHandler mServiceHandler = null;

    public PersistentIntentService(final String name)
    {
        super();
        mName = name;
    } // constructor(String)

    @Override
    public void onCreate()
    {
        super.onCreate();
        HandlerThread thread = new HandlerThread(TAG + "[" + mName + "]");
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

    } // onCreate()

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
        return mRedelivery ? START_REDELIVER_INTENT : START_NOT_STICKY;
    } // onStartCommand(Intent, flags, startId)

    @Override
    public void onDestroy()
    {
        mServiceLooper.quit();
    } // onDestroy()

    public void setIntentRedelivery(final boolean enabled)
    {
        mRedelivery = enabled;
    } // setIntentRedelivery(boolean)

    @Override
    public IBinder onBind(final Intent intent)
    {
        return null;
    } // onBind(Intent)

    protected abstract void onHandleIntent(Intent intent);

} // class PersistentIntentService
