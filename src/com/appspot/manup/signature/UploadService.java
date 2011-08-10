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
import org.apache.http.entity.mime.content.FileBody;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public final class UploadService extends IntentService
{
    private static final String TAG = UploadService.class.getSimpleName();

    private static final String MIME_PNG = "image/png";

    private static final String ACTION_UPLOAD = UploadService.class.getName() + ".UPLOAD";
    public static final String EXTRA_ID = UploadService.class.getName() + ".ID";
    public static final String EXTRA_SUCCESSFUL = UploadService.class.getName()
            + ".RESULT";

    private static final Map<Long, Set<UploadCompleteListener>> sListeners =
            new HashMap<Long, Set<UploadCompleteListener>>();

    public interface UploadCompleteListener
    {
        void onUploadComplete(final Intent intent);
    }

    public static void uploadSignature(final Context context,
            final UploadCompleteListener listener, final long id)
    {
        synchronized (sListeners)
        {
            if (sListeners.containsKey(id))
            {
                sListeners.get(id).add(listener);
                return;
            } // if
            final Set<UploadCompleteListener> listeners = new HashSet<UploadCompleteListener>();
            listeners.add(listener);
            sListeners.put(id, listeners);
            final Intent intent = new Intent(context, UploadService.class);
            intent.setAction(ACTION_UPLOAD);
            intent.putExtra(EXTRA_ID, id);
            context.startService(intent);
        } // synchronized
    } // uploadSignature

    public static void unregister(final UploadCompleteListener listener, final long id)
    {
        synchronized (sListeners)
        {
            final Set<UploadCompleteListener> listeners = sListeners.get(id);
            if (listeners != null)
            {
                listeners.remove(listener);
            } // if
        } // synchronized
    } // unregister

    private static void notifyListeners(final Intent intent)
    {
        final long id = intent.getLongExtra(EXTRA_ID, -1);

        synchronized (sListeners)
        {
            for (final UploadCompleteListener listener : sListeners.get(id))
            {
                listener.onUploadComplete(intent);
            } // for
            sListeners.remove(id);
        } // synchronized
    } // notifyListeners

    private SignatureHttpClient mClient = null;

    public UploadService()
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
            successful = upload(intent.getLongExtra(EXTRA_ID, -1));
        } // try
        catch (final IOException e)
        {
            Log.d(TAG, "Upload failed.", e);
            successful = false;
        } // catch
        intent.putExtra(EXTRA_SUCCESSFUL, successful);
        notifyListeners(intent);
    } // onHandleIntent

    private boolean upload(final long id) throws IOException
    {
        final SignatureDatabase db = SignatureDatabase.getInstance(this);
        final URI uri;
        try
        {
            final Preferences prefs = new Preferences(this);
            uri = new URI("http", null /* userInfo */, prefs.getHost(), prefs.getPort(),
                    "/members/" + db.getStudentId(id), null /* query */, null /* fragment */);
        } // try
        catch (final URISyntaxException e)
        {
            throw new AssertionError(e);
        } // catch

        final File pngFile = db.getImageFile(id);
        if (pngFile == null)
        {
            Log.d(TAG, "Failed to get image file for " + id);
            return false;
        } // if

        FileInputStream pngStream = null;
        HttpResponse response = null;
        try
        {
            pngStream = new FileInputStream(pngFile);
            final MultipartEntity entity = new MultipartEntity();
            entity.addPart(new FormBodyPart("signature", new FileBody(pngFile, MIME_PNG)));
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
