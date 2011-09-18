package com.appspot.manup.signup.swipeupclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import com.appspot.manup.signup.data.DataManager;
import com.appspot.manup.signup.extrainfo.MemberInfo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class SwipeUpClient extends Thread
{
    private static final String TAG = SwipeUpClient.class.getSimpleName();

    private static final String SERVICE_NAME = "SwipeUpClient";
    private static final UUID SERVICE_UUID =
            UUID.fromString("28adccbc-41a3-4ffd-924d-1c6a70d70b4e");

    private final BroadcastReceiver mBtReciever = new BroadcastReceiver()
    {
        @Override
        public void onReceive(final Context context, final Intent intent)
        {
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction()))
            {
                final int btState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.STATE_OFF);
                if (btState == BluetoothAdapter.STATE_ON)
                {
                    synchronized (mBtStateLock)
                    {
                        mBtStateLock.notifyAll();
                    } // synchroized
                } // if
            } // if
        } // onReceiver(Context, Intent)
    };

    private final Object mBtStateLock = new Object();
    private final Context mContext;
    private final DataManager mDataManager;
    private final BluetoothAdapter mAdapter;
    private BluetoothServerSocket mServerSocket = null;
    private BluetoothSocket mSocket = null;

    public SwipeUpClient(final Context context) throws IOException
    {
        super(TAG);
        mContext = context;
        mDataManager = DataManager.getDataManager(context);
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mAdapter == null)
        {
            throw new IOException("Bluetooth not supported.");
        } // if
    } // constructor()

    @Override
    public void run()
    {
        final IntentFilter btFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mContext.registerReceiver(mBtReciever, btFilter);

        while (!isInterrupted())
        {
            if (mAdapter.isEnabled())
            {
                try
                {
                    handleConnection();
                } // try
                catch (final IOException acceptConnectionException)
                {
                    Log.e(TAG, "Accept connection failed.", acceptConnectionException);
                } // catch
            } // if
            else
            {
                final Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                mContext.startActivity(enableBtIntent);
                synchronized (mBtStateLock)
                {
                    try
                    {
                        mBtStateLock.wait();
                    } // try
                    catch (final InterruptedException interrupted)
                    {
                        break;
                    } // catch
                } // synchronized
            } // else
        } // while

        mContext.unregisterReceiver(mBtReciever);

        cancel();
    } // run()

    private void handleConnection() throws IOException
    {
        final String message = readMessageFromClient();
        try
        {
            parseClientMessage(message);
        } // try
        catch (final JSONException invalidMessage)
        {
            throw new IOException("Invalid message from client: " + message);
        } // catch
    } // handleConnection(()

    private String readMessageFromClient() throws IOException
    {
        mAdapter.cancelDiscovery();

        BluetoothSocket socket = null;
        try
        {
            synchronized (this)
            {
                if (isInterrupted())
                {
                    throw new IOException("Operation cancelled.");
                } // if
                mServerSocket = mAdapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME,
                        SERVICE_UUID);
            } // synchronized

            socket = mServerSocket.accept();
            mServerSocket.close();

            synchronized (this)
            {
                if (isInterrupted())
                {
                    throw new IOException("Operation cancelled.");
                } // if
                mSocket = socket;
                mServerSocket = null;
            } // synchronized

            final BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            return in.readLine();
        } // try
        catch (final IOException socketException)
        {
            if (socket != null)
            {
                try
                {
                    socket.close();
                } // try
                catch (final IOException closeSocketException)
                {
                    Log.d(TAG, "Failed to close temporary socket.", closeSocketException);
                } // catch
            } // if
            throw socketException;
        } // catch
        finally
        {
            cancel();
        } // finally

    } // readFromClient()

    private void parseClientMessage(final String message) throws JSONException
    {
        final MemberInfo memberInfo = MemberInfo.fromJson(new JSONObject(message));
        Log.d(TAG, memberInfo.toString());
        if (mDataManager.requestSignature(memberInfo) == DataManager.OPERATION_FAILED)
        {
            Log.d(TAG, "Failed to request signature.");
        } // if
    } // parseClientMessage(String)

    public synchronized void cancel()
    {
        interrupt();
        if (mServerSocket != null)
        {
            try
            {
                mServerSocket.close();
            } // try
            catch (final IOException closeServerSocketException)
            {
                Log.e(TAG, "Failed to close server socket.", closeServerSocketException);
            } // catch
            mServerSocket = null;
        } // if
        if (mSocket != null)
        {
            try
            {
                mSocket.close();
            } // try
            catch (IOException closeSocketException)
            {
                Log.e(TAG, "Failed to close socket.", closeSocketException);
            } // catch
            mSocket = null;
        } // if
    } // cancel()

} // class SwipeUpClient
