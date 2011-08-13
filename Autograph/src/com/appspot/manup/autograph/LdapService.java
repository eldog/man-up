package com.appspot.manup.autograph;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.appspot.manup.autograph.SignatureDatabase.OnSignatureAddedListener;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class LdapService extends Service
{
    private static final String TAG = LdapService.class.getSimpleName();

    private final static int SSH_PORT = 22;
    private final static int LOCAL_PORT = 23456;
    private static final String REMOTE_ADDRESS = "edir.manchester.ac.uk";
    private final static int REMOTE_PORT = 389;

    private OnSignatureAddedListener mOnSignatureAddedListener = new OnSignatureAddedListener()
    {
        @Override
        public void onSignatureAdded(long id)
        {

            new LdapSearchThread(id, SignatureDatabase.getInstance(LdapService.this),
                    "localhost", LOCAL_PORT).start();

        }
    };

    private Session mSession = null;

    private class SshForwardAsyncTask extends AsyncTask<Preferences, Void, Session>
    {

        @Override
        protected Session doInBackground(Preferences... preference)
        {
            JSch jsch = new JSch();
            try
            {

                Session session = jsch.getSession(preference[0].getUsername(),
                        preference[0].getCsHost(),
                        SSH_PORT);
                session.setPassword(preference[0].getPassword());
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
        }

        @Override
        protected void onPostExecute(Session session)
        {
            super.onPostExecute(session);
            SignatureDatabase.getInstance(LdapService.this).addOnSignatureAddedListener(
                    mOnSignatureAddedListener);
            mSession = session;
        }

    }

    private SshForwardAsyncTask mSshForwardAsyncTask = new SshForwardAsyncTask();

    @Override
    public void onCreate()
    {
        super.onCreate();
        mSshForwardAsyncTask.execute(new Preferences(this));
    }

    @Override
    public void onDestroy()
    {
        mSshForwardAsyncTask.cancel(true);
        SignatureDatabase.getInstance(this).removeOnSignatureAddedListener(
                mOnSignatureAddedListener);
        if (mSession != null)
        {
            try
            {
                mSession.delPortForwardingL(LOCAL_PORT);
            }
            catch (JSchException e)
            {
                Log.w("Could not remove port forwarding", e);
            }
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

}
