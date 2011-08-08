package com.appspot.manup.signature;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MultipartEntity;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public final class SignatureUploadService extends IntentService
{
    private static final String TAG = SignatureUploadService.class.getSimpleName();

    private static final String MIME_PNG = "image/png";

    private static final String ACTION_UPLOAD = SignatureUploadService.class.getName() + ".UPLOAD";
    public static final String EXTRA_PATH = SignatureUploadService.class.getName() + ".PATH";
    public static final String EXTRA_STUDENT_ID =
            SignatureUploadService.class.getName() + ".STUDENT_ID";
    public static final String EXTRA_SUCCESSFUL = SignatureUploadService.class.getName()
            + ".RESULT";

    private static final Map<String, Set<UploadCompleteListener>> sListeners =
            new HashMap<String, Set<UploadCompleteListener>>();

    public interface UploadCompleteListener
    {
        void onUploadComplete(final Intent intent);
    }

    public static void uploadSignature(final Context context,
            final UploadCompleteListener listener, final String path, final String studentId)
    {
        synchronized (sListeners)
        {
            if (sListeners.containsKey(path))
            {
                sListeners.get(path).add(listener);
                return;
            } // if
            final Set<UploadCompleteListener> listeners = new HashSet<UploadCompleteListener>();
            listeners.add(listener);
            sListeners.put(path, listeners);
            final Intent intent = new Intent(context, SignatureUploadService.class);
            intent.setAction(ACTION_UPLOAD);
            intent.putExtra(EXTRA_PATH, path);
            intent.putExtra(EXTRA_STUDENT_ID, studentId);
            Log.d(TAG, path + " " + studentId);
            context.startService(intent);
        } // synchronized
    } // uploadSignature

    public static void unregister(final UploadCompleteListener listener, final String path)
    {
        synchronized (sListeners)
        {
            final Set<UploadCompleteListener> listeners = sListeners.get(path);
            if (listeners != null)
            {
                listeners.remove(listener);
            } // if
        } // synchronized
    } // unregister

    private static void notifyListeners(final Intent intent)
    {
        final String path = intent.getStringExtra(EXTRA_PATH);

        synchronized (sListeners)
        {
            for (final UploadCompleteListener listener : sListeners.get(path))
            {
                listener.onUploadComplete(intent);
            } // for
            sListeners.remove(path);
        } // synchronized
    } // notifyListeners

    private SignatureHttpClient mClient = null;

    public SignatureUploadService()
    {
        super(TAG);
    } // SignatureUploadService

    @Override
    public void onCreate()
    {
        super.onCreate();
        mClient = new SignatureHttpClient();
    } // onCreate

    @Override
    protected void onHandleIntent(final Intent intent)
    {
        boolean successful = true;
        try
        {
            successful = upload(intent.getStringExtra(EXTRA_PATH),
                    intent.getStringExtra(EXTRA_STUDENT_ID));
        } // try
        catch (final IOException e)
        {
            Log.w(TAG, "Upload failed.", e);
            successful = false;
        } // catch
        intent.putExtra(EXTRA_SUCCESSFUL, successful);
        notifyListeners(intent); 
    } // onHandleIntent

    private boolean upload(final String path, final String studentId) throws IOException
    {

        final URI uri;
        try
        {
            uri = new URI("http", null /* userInfo */, "192.168.1.3", 8080,
                    "/members/" + studentId, null /* query */, null /* fragment */);
        } // try
        catch (final URISyntaxException e)
        {
            throw new AssertionError(e);
        } // catch

        final File pngFile = new File(path);

        FileInputStream pngStream = null;
        HttpResponse response = null;
        try
        {
            pngStream = new FileInputStream(pngFile);
            final MultipartEntity entity = new MultipartEntity();
            entity.addPart(new FormBodyPart("signature",
                    new FileInputStreamBody(pngStream, MIME_PNG, pngFile.getName())));
            final HttpPost post = new HttpPost(uri);
            post.setEntity(entity);
            response = mClient.execute(post);
            return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
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
    } // upload

    @Override
    public void onDestroy()
    {
        mClient.shutdown();
        super.onDestroy();
    } // onDestroy

    @Override
    public IBinder onBind(final Intent intent)
    {
        return null;
    } // onBind

} // SignatureUploadService
