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
import android.os.IBinder;
import android.util.Log;

public class ImageWriteToStorageService extends IntentService
{
    private static final String TAG = ImageWriteToStorageService.class.getSimpleName();

    private static final String ACTION_WRITE = ImageWriteToStorageService.class.getName()
            + ".WRITE";
    public static final String EXTRA_ID = ImageWriteToStorageService.class.getName() + ".ID";
    public static final String EXTRA_BITMAP = ImageWriteToStorageService.class.getName()
            + ".BITMAP";
    public static final String EXTRA_SUCCESSFUL = ImageWriteToStorageService.class.getName()
            + ".RESULT";

    private static final int BITMAP_FILE_QUALITY = 100;

    private static final Map<Long, Set<WriteCompleteListener>> sListeners =
            new HashMap<Long, Set<WriteCompleteListener>>();

    public interface WriteCompleteListener
    {
        void onWriteComplete(final Intent intent);
    }

    public static void writeSignature(final Context context,
            final WriteCompleteListener listener, final long id, final Bitmap b)
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
            final Intent intent = new Intent(context, ImageWriteToStorageService.class);
            intent.setAction(ACTION_WRITE);
            intent.putExtra(EXTRA_ID, id);
            intent.putExtra(EXTRA_BITMAP, b);
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

    public ImageWriteToStorageService()
    {
        super(TAG);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        // Yet to determine code to go here

    } // onCreate

    @Override
    protected void onHandleIntent(final Intent intent)
    {
        boolean successful = false;
        try
        {
            Bitmap signature = intent.getParcelableExtra(EXTRA_BITMAP);
            if (signature != null)
            {
                successful = writeToStorage(intent.getLongExtra(EXTRA_ID, -1), signature);
            }
            else
            {
                Log.w(TAG, "Bitmap could not be retrieved");
            }
        } // try
        catch (final IOException e)
        {
            Log.d(TAG, "Write failed.", e);
        } // catch
        intent.putExtra(EXTRA_SUCCESSFUL, successful);
        notifyListeners(intent);
    }

    private boolean writeToStorage(final long id, final Bitmap signature) throws IOException
    {
        final SignatureDatabase dataHelper =
                SignatureDatabase.getInstance(ImageWriteToStorageService.this);
        final File imageFile = dataHelper.getImageFile(id);
        if (imageFile == null)
        {
            Log.w(TAG, "Image file cannot be retrieved from database");
            return false;
        }
        try
        {
            imageFile.createNewFile();
        }
        catch (final IOException e)
        {
            Log.w(TAG, "Error creating file", e);
            return false;
        }
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(imageFile);
            if (signature.compress(Bitmap.CompressFormat.PNG, BITMAP_FILE_QUALITY, fos))
            {
                return true;
            }
        }
        catch (final IOException e)
        {
            Log.w(TAG, "Error writing file", e);
            return false;
        }
        finally
        {
            if (fos != null)
            {
                try
                {
                    fos.close();
                }
                catch (final IOException e)
                {
                    Log.w(TAG, "Error closing output stream", e);
                }
            }
        }
        return false;
    }

    @Override
    public IBinder onBind(final Intent intent)
    {
        return null;
    } // onBind

}
