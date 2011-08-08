package com.appspot.manup.signature;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.http.entity.mime.content.InputStreamBody;

import android.util.Log;

public class FileInputStreamBody extends InputStreamBody
{
    private static final String TAG = FileInputStreamBody.class.getSimpleName();

    private final FileInputStream mFileInputStream;

    public FileInputStreamBody(final FileInputStream in, final String mimeType,
            final String filename)
    {
        super(in, mimeType, filename);
        mFileInputStream = in;
    } // FileInputStreamBody

    @Override
    public long getContentLength()
    {
        try
        {
            return mFileInputStream.getChannel().size();
        } // try
        catch (final IOException e)
        {
            Log.e(TAG, "Failed to get file size. Calling super", e);
            return super.getContentLength();
        } // catch
    } // getContentLength

} // FileInputStreamBody
