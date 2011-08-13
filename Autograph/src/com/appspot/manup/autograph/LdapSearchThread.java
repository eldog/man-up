package com.appspot.manup.autograph;

import android.util.Log;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchResults;

public class LdapSearchThread extends Thread
{
    private static final String TAG = LdapSearchThread.class.getSimpleName();

    private final long mId;
    private SignatureDatabase mSignatureDatabase;
    private final String mHost;
    private final int mPort;

    public LdapSearchThread(final long id, SignatureDatabase signatureDatabase, String host,
            int port)
    {
        mId = id;
        mSignatureDatabase = signatureDatabase;
        mHost = host;
        mPort = port;
    }

    @Override
    public void run()
    {
        final String magStripe = mSignatureDatabase.getStudentId(mId);
        LDAPConnection ldapConnection = new LDAPConnection();
        try
        {
            Log.d(TAG, "starting search");
            ldapConnection.connect(mHost, mPort);
            LDAPSearchResults results = ldapConnection.search("", LDAPConnection.SCOPE_SUB,
                    "umanMagStripe=" + magStripe, null, false);
            while (results.hasMore())
            {
                LDAPEntry entry = results.next();
                Log.d(TAG, entry.toString());
            }
            Log.d(TAG, "finished search");
        }
        catch (LDAPException e)
        {
            Log.e(TAG, "Could not search LDAP server", e);
        }

    }
}
