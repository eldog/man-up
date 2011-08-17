package com.appspot.manup.signup;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.appspot.manup.signup.data.DataManager;
import com.appspot.manup.signup.swipeupclient.SwipeUpThread;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class LdapSwipeService extends IntentService
{
    private static final String TAG = LdapSwipeService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1;

    public LdapSwipeService()
    {
        super(TAG);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d(TAG, "Service started");
        final Notification notification = new Notification(
                R.drawable.icon,
                null /* tickerText */,
                0 /* when */);
        notification.setLatestEventInfo(this,
                getString(R.string.app_name),
                getString(R.string.notification_message),
                PendingIntent.getActivity(
                        this,
                        0 /* requestCode */,
                        new Intent(this, AutographPreferenceActivity.class),
                        0 /* flags */));
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, notification);
    }

    public static void startServiceAction(final Context context, final Intent intent)
    {
        final Intent i = new Intent(context, LdapSwipeService.class);
        i.setAction(intent.getAction());
        context.startService(i);
    }

    private SwipeUpThread mSwipeServerThread;

    private static final int SSH_PORT = 22;
    private static final int LOCAL_PORT = 23456;
    private static final String REMOTE_ADDRESS = "edir.manchester.ac.uk";
    private static final int REMOTE_PORT = 389;

    private Session mSession = null;
    private Thread mSshThread;


    @Override
    protected void onHandleIntent(final Intent intent)
    {
        if (intent.getAction().equals(AutographApplication.ACTION_LDAP))
        {
            mSshThread = new SshForward();
            mSshThread.run();
        }
        else if (intent.getAction().equals(AutographApplication.ACTION_SWIPE))
        {
            mSwipeServerThread = new SwipeUpThread(DataManager.getInstance(this));
            mSwipeServerThread.start();
        }
    } // onHandleIntent

    private class SshForward extends Thread
    {
        public SshForward()
        {
           super(TAG);
        } // SshForward

        @Override
        public void run()
        {
            super.run();
            mSession = setUpSsh();
        }

        private Session setUpSsh()
        {
            Preferences preference = new Preferences(LdapSwipeService.this);
            JSch jsch = new JSch();
            try
            {

                Session session = jsch.getSession(preference.getUsername(),
                        preference.getCsHost(),
                        SSH_PORT);
                session.setPassword(preference.getPassword());
                session.setUserInfo(new UserInfo()
                {

                    @Override
                    public void showMessage(String message)
                    {
                        Log.d(TAG, message);

                    }

                    @Override
                    public boolean promptYesNo(String arg0)
                    {
                        return false;
                    }

                    @Override
                    public boolean promptPassword(String arg0)
                    {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public boolean promptPassphrase(String arg0)
                    {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public String getPassword()
                    {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public String getPassphrase()
                    {
                        // TODO Auto-generated method stub
                        return null;
                    }
                });
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                session.connect();
                session.setPortForwardingL(LOCAL_PORT, REMOTE_ADDRESS, REMOTE_PORT);
                Log.d(TAG, "port fowarding");
                return session;
            }
            catch (JSchException e)
            {
                Log.e(TAG, "Could not create pipe", e);
            }
            return null;
        } // setUpSsh
    } // class SshForward

    @Override
    public IBinder onBind(Intent arg0)
    {
        // TODO Auto-generated method stub
        return null;
    } // onBind

    @Override
    public void onDestroy()
    {
        if (mSwipeServerThread.isAlive())
        {
            mSwipeServerThread.interrupt();
        }
        if (mSshThread.isAlive())
        {
            mSshThread.interrupt();
        }
        if (mSession != null)
        {
            try
            {
                mSession.delPortForwardingL(LOCAL_PORT);
            }
            catch (JSchException e)
            {
                Log.e(TAG, "Could not remove port forwarding", e);
            }
        }
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
        super.onDestroy();
    } // onDestroy
} // class LdapSwipeService
