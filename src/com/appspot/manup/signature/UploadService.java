package com.appspot.manup.signature;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;

import com.appspot.manup.signature.SignatureDatabase.Signature;
import com.appspot.manup.signature.SignatureDatabase.SignatureCapturedListener;

public final class UploadService extends Service
{
    private static final String TAG = UploadService.class.getSimpleName();

    private static final String IMAGE_MIME = "image/png";

    private final SignatureCapturedListener mListener = new SignatureCapturedListener()
    {
        @Override
        public void onSignatureCaptured(final long id)
        {
            synchronized (mUploadThread)
            {
                mUploadThread.notify();
            } // mUploadThread
        } // onSignatureCaptured
    };

    private final Thread mUploadThread = new Thread(TAG)
    {
        @Override
        public void run()
        {
            Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            while (true)
            {
                uploadSignaturesLoop();
                synchronized (this)
                {
                    if (interrupted())
                    {
                        break;
                    } // if
                    Log.d(TAG, "Waiting...");
                    try
                    {
                        wait();
                    } // try
                    catch (final InterruptedException e)
                    {
                        break;
                    } // catch
                } // synchronized
                Log.d(TAG, "On the move.");
            } // while
            Log.d(TAG, "Interrupted.");
        } // run
    };

    private final SignatureHttpClient mClient = new SignatureHttpClient();

    public UploadService()
    {
        super();
    } // UploadService

    @Override
    public void onCreate()
    {
        super.onCreate();
        mUploadThread.start();
        SignatureDatabase.getInstance(this).registerSignatureCapturedListener(mListener);
    } // onCreate

    private void uploadSignaturesLoop()
    {
        while (uploadSignatures());
    } // uploadSignaturesLoop

    private boolean uploadSignatures()
    {
        Log.d(TAG, "GONA HAVE an upload.");
        Cursor c = null;
        try
        {
            c = SignatureDatabase.getInstance(this).getCapturedSignatures();
            if (c == null || !c.moveToFirst())
            {
                return false;
            } // if

            final int idColumn = c.getColumnIndex(Signature._ID);
            final int studentIdColumn = c.getColumnIndex(Signature.STUDENT_ID);

            do
            {
                try
                {
                    uploadSignature(c.getLong(idColumn), c.getString(studentIdColumn));
                } // try
                catch (final IOException e)
                {
                    Log.w(TAG, "Failed to upload signature.", e);
                } // catch
                Log.d(TAG, "Uploaded somethings.");
            } while (c.moveToNext());

        } // try
        finally
        {
            if (c != null)
            {
                c.close();
            } // if
        } // finally

        return true;
    } // uploadSignatures

    private void uploadSignature(final long id, final String studentId) throws IOException
    {
        final SignatureDatabase db = SignatureDatabase.getInstance(this);

        final URI uri;
        try
        {
            final Preferences prefs = new Preferences(this);
            uri = new URI(
                    "http",
                    null /* userInfo */,
                    prefs.getHost(),
                    prefs.getPort(),
                    "/members/" + studentId,
                    null /* query */,
                    null /* fragment */);
        } // try
        catch (final URISyntaxException e)
        {
            throw new AssertionError(e);
        } // catch

        final File imageFile = db.getImageFile(id);
        if (imageFile == null)
        {
            throw new IOException("Failed to get image file for " + id);
        } // if

        FileInputStream pngStream = null;
        HttpResponse response = null;
        try
        {
            pngStream = new FileInputStream(imageFile);
            final MultipartEntity entity = new MultipartEntity();
            entity.addPart(new FormBodyPart("signature", new FileBody(imageFile, IMAGE_MIME)));
            final HttpPost post = new HttpPost(uri);
            post.setEntity(entity);
            response = mClient.execute(post);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK)
            {
                throw new IOException("Server returned " + statusCode);
            } // if
        } // try
        finally
        {
            if (pngStream != null)
            {
                try
                {
                    pngStream.close();
                } // try
                finally
                {
                    if (response != null)
                    {
                        final HttpEntity entity = response.getEntity();
                        if (entity != null)
                        {
                            entity.getContent().close();
                        } // if
                    } // if
                } // finally
            } // if
        } // finally
        if (!db.signatureUploaded(id))
        {
            throw new IOException("Failed to update signature state for " + id);
        } // if
    } // uploadSignature

    @Override
    public void onDestroy()
    {
        SignatureDatabase.getInstance(this).unregisterSignatureCapturedListener(mListener);
        synchronized (mUploadThread)
        {
            mUploadThread.interrupt();
        } // synchronized
        super.onDestroy();
    } // onDestroy

    @Override
    public IBinder onBind(final Intent intent)
    {
        return null;
    } // onBind

} // SignatureUploadService
