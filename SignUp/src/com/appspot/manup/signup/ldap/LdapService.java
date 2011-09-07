package com.appspot.manup.signup.ldap;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.appspot.manup.signup.Preferences;
import com.appspot.manup.signup.data.DataManager;
import com.appspot.manup.signup.data.DataManager.Member;
import com.appspot.manup.signup.data.DataManager.OnChangeListener;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchResults;

public final class LdapService extends Service implements OnChangeListener
{
    private static final String TAG = LdapService.class.getSimpleName();

    private static final String FORWARD_HOST = "localhost";
    private static final String LDAP_HOST = "edir.manchester.ac.uk";
    private static final int LDAP_PORT = 389;
    private static final int FORWARD_PORT = 23456;

    private static final int CMD_START_PORT_FORWARDING = 1;
    private static final int CMD_RETRIEVE_EXTRA_INFO = 0;

    public static void controlService(final Context context, final Preferences prefs)
    {
        final Intent intent = new Intent(context, LdapService.class);
        if (prefs.ldapLookupEnabled())
        {
            context.startService(intent);
        } // if
        else
        {
            context.stopService(intent);
        } // else
    } // controlService(Context, Preferences)

    private final class LdapLookupHandler extends Handler
    {
        public LdapLookupHandler(final Looper looper)
        {
            super(looper);
        } // constructor(Looper)

        @Override
        public void handleMessage(final Message msg)
        {
            switch (msg.what)
            {
                case CMD_START_PORT_FORWARDING:
                    startPortForwarding();
                    break;
                case CMD_RETRIEVE_EXTRA_INFO:
                    retrieveExtraInfo();
                    break;
                default:
                    throw new AssertionError();
            } // switch
        } // handleMessage(Message)
    } // class LdapLookupHandler

    private final BroadcastReceiver mNetworkReciever = new BroadcastReceiver()
    {
        @Override
        public void onReceive(final Context context, final Intent intent)
        {
            final NetworkInfo info =
                    (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            final DataManager dataManager = DataManager.getDataManager(context);
            if (info.isConnected())
            {
                dataManager.registerListener(LdapService.this);
                mHandler.sendEmptyMessage(CMD_START_PORT_FORWARDING);
                mHandler.sendEmptyMessage(CMD_RETRIEVE_EXTRA_INFO);
            } // if
            else
            {
                dataManager.unregisterListener(LdapService.this);
            } // else
        } // onReceive(Context, Intent)
    };

    private final JSch mJsch = new JSch();
    private final IntentFilter mIntentFilter;
    private LdapLookupHandler mHandler = null;
    private volatile DataManager mDataManager = null;
    private volatile Session mSession = null;

    {
        mIntentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
    }

    public LdapService()
    {
        super();
    } // constructor()

    @Override
    public void onCreate()
    {
        super.onCreate();
        mDataManager = DataManager.getDataManager(this);
        final HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mHandler = new LdapLookupHandler(thread.getLooper());
        mHandler.sendEmptyMessage(CMD_START_PORT_FORWARDING);
        mHandler.sendEmptyMessage(CMD_RETRIEVE_EXTRA_INFO);
        // CONNECTIVITY_ACTION is sticky, let the receiver register with the data manager.
        registerReceiver(mNetworkReciever, mIntentFilter);
    } // onCreate()

    private boolean startPortForwarding()
    {
        final Preferences prefs = new Preferences(this);
        if (mSession != null && mSession.isConnected())
        {
            return true;
        } // if
        try
        {
            /*
             * Throws JschException if user name or host not set, so no need for
             * us to check.
             */
            mSession = mJsch.getSession(prefs.getLdapUsername(), prefs.getLdapHost());
            mSession.setConfig("StrictHostKeyChecking", "no");
            mSession.setPassword(prefs.getPassword());
            mSession.connect();
            mSession.setPortForwardingL(FORWARD_PORT, LDAP_HOST, LDAP_PORT);
        } // try
        catch (final JSchException e)
        {
            if (mSession != null)
            {
                mSession.disconnect();
            } // if
            Log.e(TAG, "Failed to start port forwarding.");
            return false;
        } // catch
        Log.v(TAG, "Port forwaridng started.");
        return true;
    } // startPortForwarding()

    private void retrieveExtraInfo()
    {
        Cursor cursor = null;
        try
        {
            cursor = mDataManager.getMembersWithoutExtraInfo();
            if (cursor == null)
            {
                Log.e(TAG, "Failed to get members without extra info.");
                return;
            } // if
            if (!cursor.moveToFirst())
            {
                Log.v(TAG, "No members without extra info.");
                return;
            } // if
            final int idColumn = cursor.getColumnIndexOrThrow(Member._ID);
            final int personIdColumn = cursor.getColumnIndexOrThrow(Member.PERSON_ID);
            do
            {
                try
                {
                    queryLdapServer(cursor.getLong(idColumn), cursor.getString(personIdColumn));
                } // try
                catch (final LDAPException e)
                {
                    Log.w(TAG, "Failed to query LDAP server", e);
                } // catch
            } while (cursor.moveToNext() && !Thread.interrupted());
        } // try
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            } // if
        } // finally
    } // retrieveExtraInfo()

    private void queryLdapServer(final long id, final String personId) throws LDAPException
    {
        if (!mDataManager.setExtraInfoStateToRetrieving(id))
        {
            Log.w(TAG, "Failed to set extra info state to retrieving for " + id + ". Ignoring.");
        } // if

        LDAPConnection conn = null;
        try
        {
            conn = new LDAPConnection();
            conn.connect(FORWARD_HOST, FORWARD_PORT);
            Log.v(TAG, "Looking up " + personId);
            final LDAPSearchResults results = conn.search(
                    "" /* base */,
                    LDAPConnection.SCOPE_SUB,
                    "umanPersonID=" + personId,
                    null /* attrs */,
                    false /* types only */);

            final MemberLdapEntry memberEntry = extractLdapEntry(id, personId, results);

            if (memberEntry == null || !mDataManager.addExtraInfo(memberEntry))
            {
                Log.e(TAG, "Failed to add extra infomation for person ID " + personId);
            } // if
        } // try
        catch (final LDAPException e)
        {
            Log.d(TAG, "Setting error.");
            mDataManager.setExtraInfoStateToError(id);
            throw e;
        } // catch
        finally
        {
            if (conn != null)
            {
                conn.disconnect();
            } // if
        } // finally
    } // queryLdapServer

    private MemberLdapEntry extractLdapEntry(final long id, final String personId,
            final LDAPSearchResults results)
    {
        MemberLdapEntry memberEntry = null;

        /*
         * Don't use results.getCount(). It doesn't appear to work and always
         * returns 0.
         */

        if (results.hasMore())
        {
            try
            {
                memberEntry = MemberLdapEntry.fromLdapEntry(results.next());
            } // try
            catch (final LDAPException e)
            {
                Log.w(TAG, "Failed to get LDAP search result.", e);
                return null;
            } // catch
        } // if

        if (memberEntry == null || results.hasMore())
        {
            Log.w(TAG, "No or multiple results, invalidating person ID " + personId);
            mDataManager.setPersonIdInvalid(id);
            mDataManager.setExtraInfoStateToError(id);
            return null;
        } // if

        Log.v(TAG, memberEntry.toString());
        return memberEntry;
    } // extactLdapEntry(long, String, LDAPSearchResults)

    @Override
    public void onDestroy()
    {
        mDataManager.unregisterListener(this);
        mHandler.getLooper().quit();
        if (mSession != null)
        {
            mSession.disconnect();
        } // if
        super.onDestroy();
    } // onDestroy()

    @Override
    public void onChange(final DataManager dataManager)
    {
        mHandler.removeMessages(CMD_RETRIEVE_EXTRA_INFO);
        mHandler.sendEmptyMessage(CMD_RETRIEVE_EXTRA_INFO);
    } // onChange(DataManager)

    @Override
    public IBinder onBind(final Intent intent)
    {
        return null;
    } // onBind(Intent)

} // class LdapService
