package com.appspot.manup.signup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import com.appspot.manup.signup.data.DataManager;

public class WriteSignatureService extends IntentService
{
    private static final String TAG = WriteSignatureService.class.getSimpleName();

    private static final String ACTION_WRITE = WriteSignatureService.class.getName() + ".WRITE";

    public static final String EXTRA_ID = WriteSignatureService.class.getName() + ".ID";

    private static final int BITMAP_FILE_QUALITY = 100;

    private static final Map<Long, Bitmap> sSignatures = new HashMap<Long, Bitmap>();

    public static void writeSignature(final Context context, final long id, final Bitmap signature)
    {
        sSignatures.put(id, signature);
        final Intent intent = new Intent(context, WriteSignatureService.class);
        intent.setAction(ACTION_WRITE);
        intent.putExtra(EXTRA_ID, id);
        context.startService(intent);
    } // uploadSignature

    private final Matrix mClockwise90DegRotation;

    {
        mClockwise90DegRotation = new Matrix();
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
        writeSignature(id);
    } // onHandleIntent

    private boolean writeSignature(final long id)
    {
        final DataManager db = DataManager.getDataManager(this);

        final File imageFile = db.getSignatureFile(id);
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

        return db.setSignatureStateCaptured(id);
    } // write

} // class WriteSignatureService
