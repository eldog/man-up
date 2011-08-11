package com.appspot.manup.autograph;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.text.format.DateUtils;

public final class SignatureHttpClient extends DefaultHttpClient
{
    @SuppressWarnings("unused")
    private static final String TAG = SignatureHttpClient.class.getSimpleName();

    private static HttpParams createParams()
    {
        final int secondInMillis = (int) DateUtils.SECOND_IN_MILLIS;
        final HttpParams params = new BasicHttpParams();
        ConnManagerParams.setTimeout(params, 30 * secondInMillis);
        HttpConnectionParams.setConnectionTimeout(params, 30 * secondInMillis);
        HttpConnectionParams.setSoTimeout(params, 30 * secondInMillis);
        HttpConnectionParams.setSocketBufferSize(params, 8192);
        return params;
    } // createParams

    public SignatureHttpClient()
    {
        super(createParams());
    } // SignatureHttpClient

    @Override
    protected ClientConnectionManager createClientConnectionManager()
    {
        final SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        return new ThreadSafeClientConnManager(getParams(), schemeRegistry);
    } // createClientConnectionManager

    public void shutdown()
    {
        getConnectionManager().shutdown();
    } // shutdown

} // SignatureHttpClient