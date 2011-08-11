package com.appspot.manup.signature;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

public class WriteSignatureService extends IntentService
{
    private static final String TAG = WriteSignatureService.class.getSimpleName();

    private static final String ACTION_WRITE = WriteSignatureService.class.getName() + ".WRITE";

    public static final String EXTRA_ID = WriteSignatureService.class.getName() + ".ID";
    public static final String EXTRA_SUCCESSFUL = WriteSignatureService.class.getName() + ".RESULT";

    private static final int BITMAP_FILE_QUALITY = 100;

    private static final Map<Long, Set<WriteCompleteListener>> sListeners =
            new HashMap<Long, Set<WriteCompleteListener>>();

    private static final Map<Long, Bitmap> sSignatures = new HashMap<Long, Bitmap>();

    public interface WriteCompleteListener
    {
        void onWriteComplete(Intent intent);
    } // WriteCompleteListener

    public static void writeSignature(final Context context, final WriteCompleteListener listener,
            final long id, final Bitmap signature)
    {
        synchronized (sListeners)
        {
            if (sListeners.containsKey(id))
            {
                sListeners.get(id).add(listener);
                return;
            } // if
            final Set<WriteCompleteListener> listeners = new HashSet<WriteCompleteListener>();
            sListeners.put(id, listeners);
            listeners.add(listener);
        } // synchronized

        sSignatures.put(id, signature);
        final Intent intent = new Intent(context, WriteSignatureService.class);
        intent.setAction(ACTION_WRITE);
        intent.putExtra(EXTRA_ID, id);
        context.startService(intent);
    } // uploadSignature

    public static void unregister(final WriteCompleteListener listener, final long id)
    {
        synchronized (sListeners)
        {
            final Set<WriteCompleteListener> listeners = sListeners.get(id);
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
            for (final WriteCompleteListener listener : sListeners.get(id))
            {
                listener.onWriteComplete(intent);
            } // for
            sListeners.remove(id);
        } // synchronized
    } // notifyListeners

    private final Matrix mClockwise90DegRotation = new Matrix();

    {
        mClockwise90DegRotation.setRotate(90.0f);
    }

    public WriteSignatureService()
    {
        super(TAG);
    } // WriteSignatureService

    @Override
    protected void onHandleIntent(final Intent intent)
    {
        final long id = intent.getLongExtra(EXTRA_ID, -1);
        intent.putExtra(EXTRA_SUCCESSFUL, writeSignature(id));
        notifyListeners(intent);
    } // onHandleIntent

    private boolean writeSignature(final long id)
    {
        final SignatureDatabase db = SignatureDatabase.getInstance(WriteSignatureService.this);

        final File imageFile = db.getImageFile(id);
        if (imageFile == null)
        {
            Log.w(TAG, "Failed to retrieve image file for " + id);
            return false;
        } // if

        try
        {
            imageFile.createNewFile();
        } // try
        catch (final IOException e)
        {
            Log.w(TAG, "Failed to create file for " + id, e);
            return false;
        } // catch

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(imageFile);
            final Bitmap signature = sSignatures.remove(id);
            if (!signature.compress(Bitmap.CompressFormat.PNG, BITMAP_FILE_QUALITY, fos))
            {
                return false;
            } // if
        } // try
        catch (final IOException e)
        {
            Log.w(TAG, "Failed to write image for " + id, e);
            return false;
        } // catch
        finally
        {
            if (fos != null)
            {
                try
                {
                    fos.close();
                } // try
                catch (final IOException e)
                {
                    Log.w(TAG, "Failed to close image file for " + id, e);
                } // catch
            } // if
        } // finally

        return db.signatureCaptured(id);
    } // write

} // WriteSignatureService
