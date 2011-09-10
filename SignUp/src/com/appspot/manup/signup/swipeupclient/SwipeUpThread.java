package com.appspot.manup.signup.swipeupclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import android.content.Context;
import android.os.Process;
import android.util.Log;

import com.appspot.manup.signup.NetworkUtils;
import com.appspot.manup.signup.data.DataManager;

class SwipeUpThread extends Thread
{
    private static final String TAG = SwipeUpThread.class.getSimpleName();

    private static final int PORT = 12345;
    private static final int BACKLOG = 10;

    private static final long READ_TIMEOUT_MILLISECONDS = 10000L;
    private static final int SERVER_SOCKET_TIMEOUT_MILLISECONDS = 10000;
    private static final int SOCKET_TIMEOUT_MILLISECONDS = 1000;
    private static final long GET_SERVER_SOCKET_RETRY_DELAY_MS = 1000L;
    private static final int MAX_GET_SERVER_SOCKET_RERIES = 100;

    private final DataManager mDataManager;

    public SwipeUpThread(final Context context)
    {
        super(TAG);
        mDataManager = DataManager.getDataManager(context);
    } // SwipeServerThread

    @Override
    public void run()
    {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        try
        {
            handleRequests();
        } // try
        catch (final InterruptedException e)
        {
            Log.v(TAG, "Interrupted, now stopping.", e);
        } // catch
        catch (final IOException e)
        {
            Log.d(TAG, "An error occured while handling requests.", e);
        } // catch
    } // run

    private void handleRequests() throws IOException, InterruptedException
    {
        InetAddress hostAddress = NetworkUtils.getLocalIpAddress();
        if (hostAddress == null)
        {
            Log.e(TAG, "Could not get host inet address, aborting running server");
            return;
        } // if
        Log.d(TAG, "Host inet: " + hostAddress);

        ServerSocket serverSocket = null;
        try
        {
            serverSocket = getServerSocket(hostAddress);

            Log.d(TAG, "Server socket: " + serverSocket.getInetAddress() + ":"
                    + serverSocket.getLocalPort());

            // Terminates when handleRequest() is interrupted.
            for (;;)
            {
                try
                {
                    handleRequest(serverSocket);
                } // try
                catch (final IOException e)
                {
                    Log.d(TAG, "Failed to handle request.", e);
                } // catch
            } // for
        } // try
        finally
        {
            if (serverSocket != null)
            {
                serverSocket.close();
            } // if
        } // finally
    } // handleRequests()

    private ServerSocket getServerSocket(final InetAddress hostAddress) throws IOException,
            InterruptedException
    {
        ServerSocket serverSocket = null;
        for (int tries = 0; serverSocket == null; tries++)
        {
            try
            {
                serverSocket = new ServerSocket(PORT, BACKLOG, hostAddress);
            } // try
            catch (final BindException e)
            {
                if (tries >= MAX_GET_SERVER_SOCKET_RERIES)
                {
                    Log.d(TAG, "Failed to get server socket, too many tries.");
                    throw e;
                } // for
            } // catch
            if (interrupted())
            {
                throw new InterruptedException("Interrupted while getting server socket.");
            } // if
            Thread.sleep(GET_SERVER_SOCKET_RETRY_DELAY_MS);
        } // for
        serverSocket.setSoTimeout(SERVER_SOCKET_TIMEOUT_MILLISECONDS);
        return serverSocket;
    } // getServerSocket(InetAddress)

    private void handleRequest(final ServerSocket serverSocket) throws InterruptedException,
            IOException
    {
        Socket socket = null;
        try
        {
            socket = waitForIncomingConnection(serverSocket);
            final String personId = readPersonId(socket);

            if (personId == null)
            {
                Log.w(TAG, "Person ID could not be read.");
                return;
            } // if

            Log.d(TAG, "Recieved person ID '" + personId + "' from SwipeUp.");

            final boolean signatureRequested =
                    mDataManager.requestSignature(personId) != DataManager.OPERATION_FAILED;

            if (!signatureRequested)
            {
                Log.e(TAG, "Failed to insert person ID into database.");
            } // if

            writeResponse(socket, signatureRequested);
        } // try
        finally
        {
            if (socket != null)
            {
                socket.close();
            } // if
        } // finally
    } // handleRequest

    private Socket waitForIncomingConnection(final ServerSocket serverSocket)
            throws InterruptedException, IOException
    {
        Log.v(TAG, "Waiting for incoming connection...");

        Socket socket = null;
        do
        {
            try
            {
                socket = serverSocket.accept();
            } // try
            catch (final InterruptedIOException e)
            {
                // Timed out, loop.
                Log.v(TAG, "Timed out while waiting for incoming connection.");
            } // catch

            if (interrupted())
            {
                throw new InterruptedException(
                        "Interrupted while waiting for incoming connection.");
            } // if
        } while (socket == null);

        socket.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);
        return socket;
    } // read

    private String readPersonId(final Socket socket) throws IOException, InterruptedException
    {
        try
        {
            final BufferedReader input =
                    new BufferedReader(new InputStreamReader(socket.getInputStream()));
            final long timeBeforeRead = System.currentTimeMillis();
            while (System.currentTimeMillis() - timeBeforeRead < READ_TIMEOUT_MILLISECONDS)
            {
                try
                {
                    return input.readLine();
                } // try
                catch (final SocketTimeoutException e)
                {
                    Log.v(TAG, "Timed out before a line was read.");
                } // catch

                if (interrupted())
                {
                    throw new InterruptedException("Interrupted while reading from socket.");
                } // if
            } // while
            throw new SocketTimeoutException("Timed out before person ID was read.");
        } // try
        finally
        {
            socket.shutdownInput();
        } // finally
    } // readPersonId(Socket)

    private void writeResponse(final Socket socket, final boolean inserted) throws IOException
    {
        try
        {
            final PrintWriter output = new PrintWriter(socket.getOutputStream());
            output.print("Ciao buddy");
            output.flush();
        } // try
        finally
        {
            socket.shutdownOutput();
        } // finally
    } // writeResponse(Socket, boolean)

} // class SwipeServerThread
