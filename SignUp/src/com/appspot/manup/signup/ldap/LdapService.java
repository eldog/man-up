package com.appspot.manup.signup.ldap;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.appspot.manup.signup.Preferences;
import com.appspot.manup.signup.data.DataManager;
import com.appspot.manup.signup.data.DataManager.Member;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchResults;

public final class LdapService extends IntentService
{
    private static final String TAG = LdapService.class.getSimpleName();

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

        Cursor c = null;
        try
        {
            c = mDataManager.getMembersWithoutAdditionalInfo();
            if (c == null)
            {
                Log.e(TAG, "Failed to get members without additional info");
                return;
            } // if
            final int idColumn = c.getColumnIndex(Member._ID);
            final int studentIdColumn = c.getColumnIndex(Member.STUDENT_ID);
            while (c.moveToNext())
            {
                try
                {
                    queryLdapServer(c.getLong(idColumn), c.getString(studentIdColumn));
                } // try
                catch (final LDAPException e)
                {
                    Log.e(TAG, "Failed to query LDAP server", e);
                } // catch
            } // while
        } // try
        finally
        {
            if (c != null)
            {
                c.close();
            } // if
        } // finally
    } // onHandleIntent

    private void ensurePortForwarding() throws JSchException
    {
        if (mSession != null && mSession.isConnected())
        {
            Log.v(TAG, "Port forwarding already established.");
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
        } // try
        catch (final JSchException e)
        {
            if (mSession != null)
            {
                mSession.disconnect();
            } // if
            throw e;
        } // catch
        Log.v(TAG, "Port forwaridng established.");
    } // ensurePortForwarding

    private void queryLdapServer(final long id, final String studentId) throws LDAPException
    {
        LDAPConnection conn = null;
        try
        {
            conn = new LDAPConnection();
            conn.connect(FORWARD_HOST, FORWARD_PORT);
            Log.v(TAG, "Looking up " + studentId);
            final LDAPSearchResults results = conn.search(
                    "" /* base */,
                    LDAPConnection.SCOPE_SUB,
                    "umanPersonID=" + studentId,
                    null /* attrs */,
                    false /* typesOnly */);
            /*
             * Don't use results.getCount(). It doesn't appear to work and
             * always returns 0.
             */
            if (!results.hasMore())
            {
                Log.w(TAG, "Invalid student ID (" + studentId + "). Got no results.");
                if (!mDataManager.setStudentIdInvalid(id))
                {
                    Log.e(TAG, "Failed to mark student ID " + studentId + " as invalid.");
                } // if
                return;
            } // if
            final LDAPEntry memberEntry = results.next();
            Log.v(TAG, memberEntry.toString());
            if (results.hasMore())
            {
                Log.w(TAG, "Invalid student ID (" + studentId + "). Got more than one results.");
                if (!mDataManager.setStudentIdInvalid(id))
                {
                    Log.e(TAG, "Failed to mark student ID " + studentId + " as invalid.");
                } // if
                return;
            }
            if (!mDataManager.addAdditionalMemberInfo(MemberLdapEntry.fromLdapEntry(memberEntry)))
            {
                Log.e(TAG, "Failed to add additional infomation for student ID " + studentId);
            } // if
        } // try
        finally
        {
            if (conn != null)
            {
                conn.disconnect();
            } // if
        } // finally
    } // queryLdapServer

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
