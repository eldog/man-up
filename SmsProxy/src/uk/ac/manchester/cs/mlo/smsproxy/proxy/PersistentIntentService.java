package uk.ac.manchester.cs.mlo.smsproxy.proxy;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

abstract class PersistentIntentService extends Service
{
    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;
    private String mName;

    private final class ServiceHandler extends Handler
    {
        public ServiceHandler(final Looper looper)
        {
            super(looper);
        } // ServiceHandler

        @Override
        public void handleMessage(final Message msg)
        {
            onHandleIntent((Intent) msg.obj);
        } // handleMessage
    } // ServiceHandler

    public PersistentIntentService(final String name)
    {
        super();
        mName = name;
    } // PersistentIntentService

    @Override
    public void onCreate()
    {
        super.onCreate();
        HandlerThread thread = new HandlerThread("PersistentIntentService[" + mName + "]");
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    } // onCreate

    @Override
    public void onStart(final Intent intent, final int startId)
    {
        super.onStart(intent, startId);
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
    } // onStart

    @Override
    public IBinder onBind(final Intent intent)
    {
        return null;
    } // onBind

    @Override
    public void onDestroy()
    {
        mServiceLooper.quit();
        super.onDestroy();
    } // onDestroy

    protected abstract void onHandleIntent(Intent intent);

} // PersistentIntentService
