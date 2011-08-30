package com.appspot.manup.signup.ldap;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.appspot.manup.signup.Preferences;
import com.appspot.manup.signup.data.DataManager;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchResults;

public final class LdapService extends IntentService
{
    private static final String TAG = LdapService.class.getSimpleName();

    public static final String EXTRA_ID = LdapService.class.getName() + ".ID";

    private static final String FORWARD_HOST = "localhost";
    private static final int FORWARD_PORT = 23456;

    private static final String LDAP_HOST = "edir.manchester.ac.uk";
    private static final int LDAP_PORT = 389;

    private final JSch mJsch = new JSch();
    private volatile DataManager mDataManager = null;
    private volatile Session mSession = null;

    public LdapService()
    {
        super(TAG);
    } // constructor

    @Override
    public void onCreate()
    {
        super.onCreate();
        mDataManager = DataManager.getInstance(this);
    } // onCreate

    @Override
    protected void onHandleIntent(final Intent intent)
    {
        try
        {
            ensurePortForwarding();
        } // try
        catch (final JSchException e)
        {
            Log.w(TAG, "Counld not ensure port forwarding.", e);
            return;
        } // catch
        final long id = intent.getLongExtra(EXTRA_ID, -1);
        if (id <= 0)
        {
            Log.e(TAG, "Illegal or no EXTRA_ID given: " + id);
            return;
        } // if
        lookup(id);
    } // onHandleIntent

    private void ensurePortForwarding() throws JSchException
    {
        if (mSession != null && mSession.isConnected())
        {
            return; // Already port forwarding.
        } // if

        final Preferences prefs = new Preferences(this);
        /*
         * Throws JschException if user name or host not set, so no need for us
         * to check.
         */
        try
        {
            mSession = mJsch.getSession(prefs.getLdapUsername(), prefs.getLdapHost());
            mSession.setConfig("StrictHostKeyChecking", "no");
            mSession.setPassword(prefs.getPassword());
            mSession.connect();
            mSession.setPortForwardingL(FORWARD_PORT, LDAP_HOST, LDAP_PORT);
        }
        catch (final JSchException e)
        {
            if (mSession != null)
            {
                mSession.disconnect();
            } // if
            throw e;
        } // catch
    } // ensurePortForwarding

    private void lookup(final long id)
    {
        final String studentId = mDataManager.getStudentId(id);
        if (studentId == null)
        {
            Log.e(TAG, "Failed to get student ID for ID " + id);
            return;
        } // if
        try
        {
            queryLdapServer(studentId);
        } // try
        catch (final LDAPException e)
        {
            Log.e(TAG, "Failed to query LDAP server", e);
        } // catch
    } // onHandleIntent

    private void queryLdapServer(final String studentId) throws LDAPException
    {
        LDAPConnection conn = null;
        try
        {
            conn = new LDAPConnection();
            conn.connect(FORWARD_HOST, FORWARD_PORT);
            final LDAPSearchResults results = conn.search(
                    "" /* base */,
                    LDAPConnection.SCOPE_SUB,
                    "umanMagStripe=" + studentId,
                    null /* attrs */,
                    false /* typesOnly */);
            while (results.hasMore())
            {
                mDataManager.addAdditionalMemberInfo(MemberLdapEntry.fromLdapEntry(results.next()));
            } // while
        } // try
        finally
        {
            if (conn != null)
            {
                conn.disconnect();
            } // if
        } // finally
    } // queryStudentId

    @Override
    public void onDestroy()
    {
        if (mSession != null)
        {
            mSession.disconnect();
        } // if
        super.onDestroy();
    } // onDestroy

} // class LdapService
