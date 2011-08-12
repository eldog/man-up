package com.appspot.manup.signature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import android.os.Process;
import android.util.Log;

/**
 *
 * Opens socket server.
 *
 * Can be killed by an interrupt
 *
 */
public class SwipeServerThread extends Thread
{
    private static final String TAG = SwipeServerThread.class.getSimpleName();

    private static final int PORT = 12345;
    private static final int BACKLOG = 10;
    private static final int SOCKET_TIME_OUT = 5000;

    private SignatureDatabase mSignatureDatabase;

    public SwipeServerThread(SignatureDatabase signatureDatabase)
    {
        super(TAG);
        mSignatureDatabase = signatureDatabase;
    }

    @Override
    public void run()
    {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        InetAddress hostInetAddress = getLocalIpAddress();
        if (hostInetAddress == null)
        {
            Log.e(TAG,
                    "Could not get host inet address, aborting running server");
            return;
        } // if
        Log.d(TAG, "host inet: " + hostInetAddress);
        ServerSocket serverSocket;
        try
        {
            serverSocket = new ServerSocket(PORT, BACKLOG, hostInetAddress);
            try
            {
                try
                {
                    serverSocket.setSoTimeout(5000);
                } // try
                catch (SocketException e)
                {
                    Log.e(TAG, "Could not set server timeout", e);
                    return;
                } // catch

                Log.d(TAG, "socket created on address " + serverSocket.getInetAddress().toString()
                        + " on port " + serverSocket.getLocalPort());
                while (true)
                {
                    Socket socket = null;
                    Log.d(TAG, "Reading socket...");
                    boolean hasRead = false;
                    while (!hasRead)
                    {
                        try
                        {
                            socket = serverSocket.accept();
                        } // try
                        catch (InterruptedIOException e)
                        {
                            // Assuming the exception was because of the timeout
                            // we check to see if we've been interrupted and
                            // should
                            // stop
                            if (Thread.currentThread().isInterrupted())
                            {
                                Log.d(TAG, "Interrupted, now stopping");
                                return;
                            } // if
                            continue;
                        } // catch
                        catch (IOException e)
                        {
                            Log.e(TAG, "Could not accept new connection", e);
                            return;
                        } // catch
                          // We must have accepted a connection then
                        hasRead = true;
                    } // while
                    Log.d(TAG, "socket read");
                    try
                    {
                        try
                        {
                            socket.setSoLinger(true, SOCKET_TIME_OUT);
                        } // try
                        catch (SocketException e)
                        {
                            Log.e(TAG, "Unable to set linger time", e);
                            return;
                        } // catch

                        BufferedReader input = null;
                        try
                        {
                            try
                            {
                                input = new BufferedReader(
                                        new InputStreamReader(
                                                socket.getInputStream()));
                            } // try
                            catch (IOException e)
                            {
                                Log.e(TAG, "Could not read sockets input", e);
                                return;
                            } // catch

                            String studentId = null;
                            try
                            {
                                studentId = input.readLine();
                            } // try
                            catch (IOException e)
                            {
                                Log.e(TAG, "Could not read input", e);
                                return;
                            } // catch
                            if (studentId != null)
                            {
                                Log.d(TAG, "Client: " + studentId);

                                // Send the request to out handler
                                long id = mSignatureDatabase.addSignature(studentId);
                                if (id == -1)
                                {
                                    Log.e(TAG, "Could not insert the signature");
                                }
                                else
                                {
                                    Log.d(TAG, "Inserted signature with id: "
                                            + id);
                                }
                            }
                            else
                            {
                                Log.e(TAG, "Client request was null");
                            }
                            PrintWriter output = null;
                            try
                            {
                                try
                                {
                                    output = new PrintWriter(socket.getOutputStream());
                                } // try
                                catch (IOException e)
                                {
                                    Log.e(TAG, "Could not get the output stream", e);
                                    return;
                                } // catch
                                output.print("Ciao buddy");
                            } // try
                            finally
                            {
                                if (output != null)
                                {
                                    output.close();
                                } // if
                            } // finally
                        } // try
                        finally
                        {
                            if (input != null)
                            {
                                try
                                {
                                    input.close();
                                } // try
                                catch (IOException e)
                                {
                                    Log.e(TAG, "Could not close input stream", e);
                                } // catch
                            } // if
                        } // finally
                    } // try
                    finally
                    {
                        try
                        {
                            socket.close();
                        } // try
                        catch (IOException e)
                        {
                            Log.e(TAG, "Could not close socket", e);
                        } // catch
                    } // finally
                } // while
            } // try
            finally
            {
                try
                {
                    serverSocket.close();
                } // try
                catch (IOException e1)
                {
                    Log.e(TAG, "Could not close server socket");
                } // catch
            } // finally
        } // try
        catch (IOException e)
        {
            Log.e(TAG, "Socket creation failed", e);
            return;
        } // catch
    } // run

    private static InetAddress getLocalIpAddress()
    {
        try
        {
            // Using Enumeration instead of Iterator as NetworkInterface is some
            // ancient relic and getNetworkInterfaces() returns an Enumeration
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                    en.hasMoreElements();)
            {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                        enumIpAddr.hasMoreElements();)
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress())
                    {
                        return inetAddress;
                    } // if
                } // for
            }// for
        } // try
        catch (SocketException ex)
        {
            Log.e(TAG, ex.toString());
        } // catch
        return null;
    } // getLocalIpAddress

} // SwipeServerThread
