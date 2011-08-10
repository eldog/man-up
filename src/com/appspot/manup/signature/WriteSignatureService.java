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
import android.util.Log;

public class WriteSignatureService extends IntentService
{
    private static final String TAG = WriteSignatureService.class.getSimpleName();

    private static final String ACTION_WRITE = WriteSignatureService.class.getName() + ".WRITE";
    public static final String EXTRA_ID = WriteSignatureService.class.getName() + ".ID";
    public static final String EXTRA_BITMAP = WriteSignatureService.class.getName() + ".BITMAP";
    public static final String EXTRA_SUCCESSFUL = WriteSignatureService.class.getName()
            + ".RESULT";
    public static final String EXTRA_DB_CAP_SUCCESSFUL = WriteSignatureService.class.getName()
            + ".DBRESULT";

    private static final int BITMAP_FILE_QUALITY = 100;

    private static final Map<Long, Set<WriteCompleteListener>> sListeners =
            new HashMap<Long, Set<WriteCompleteListener>>();

    public static final Map<Long, Bitmap> sBitmaps = new HashMap<Long, Bitmap>();

    public interface WriteCompleteListener
    {
        void onWriteComplete(final Intent intent);
    }

    public static void writeSignature(final Context context,
            final WriteCompleteListener listener, final long id)
    {
        synchronized (sListeners)
        {
            if (sListeners.containsKey(id))
            {
                sListeners.get(id).add(listener);
                return;
            } // if

            final Set<WriteCompleteListener> listeners = new HashSet<WriteCompleteListener>();
            listeners.add(listener);
            sListeners.put(id, listeners);

            final Intent intent = new Intent(context, WriteSignatureService.class);
            intent.setAction(ACTION_WRITE);
            intent.putExtra(EXTRA_ID, id);
            context.startService(intent);
        } // synchronized
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

    public WriteSignatureService()
    {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        synchronized (sBitmaps)
        {
            boolean writeSuccessful = false;
            boolean databaseCapturedSuccessful = false;
            final long id = intent.getLongExtra(EXTRA_ID, -1);
            if (sBitmaps.containsKey(id))
            {
                Bitmap b = sBitmaps.get(id);
                writeSuccessful = write(id, b);
                if (writeSuccessful)
                {
                    SignatureDatabase dataHelper =
                            SignatureDatabase.getInstance(WriteSignatureService.this);
                    if (dataHelper.signatureCaptured(id))
                    {
                        databaseCapturedSuccessful = true;
                        sBitmaps.remove(id);
                    }
                    else
                    {
                        Log.w(TAG, id + ": Could not update database");
                    }
                }
            }
            else
            {
                Log.w(TAG, "Bitmap could not be retrieved");
            }
            intent.putExtra(EXTRA_SUCCESSFUL, writeSuccessful);
            intent.putExtra(EXTRA_DB_CAP_SUCCESSFUL, databaseCapturedSuccessful);
            notifyListeners(intent);
        }
    }

    private boolean write(final long id, final Bitmap b)
    {
        final SignatureDatabase dataHelper = SignatureDatabase
                .getInstance(WriteSignatureService.this);
        final File imageFile = dataHelper.getImageFile(id);
        if (imageFile == null)
        {
            Log.w(TAG, "Image file cannot be retrieved from database");
            return false;
        } // if
        try
        {
            imageFile.createNewFile();
        } // try
        catch (final IOException e)
        {
            Log.w(TAG, "Error creating file", e);
            return false;
        } // catch
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(imageFile);
            if (b.compress(Bitmap.CompressFormat.PNG, BITMAP_FILE_QUALITY, fos))
            {
                return true;
            } // if
            return false;
        } // try
        catch (final IOException e)
        {
            Log.w(TAG, "Error writing file", e);
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
                    Log.w(TAG, "Error closing output stream", e);
                } // catch
            } // if
        } // finally
    }
}
