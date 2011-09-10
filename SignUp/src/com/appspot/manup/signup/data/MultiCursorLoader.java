package com.appspot.manup.signup.data;

import android.database.Cursor;
import android.os.AsyncTask;

public abstract class MultiCursorLoader extends AsyncTask<Void, Void, Cursor[]>
{
    @SuppressWarnings("unused")
    private static final String TAG = MultiCursorLoader.class.getSimpleName();

    private volatile Cursor mCursors[] = null;

    public MultiCursorLoader()
    {
        super();
    } // constructor()

    @Override
    protected final void onPreExecute()
    {
        // Do nothing.
    } // onPreExecute()

    @Override
    protected final Cursor[] doInBackground(final Void... noParams)
    {
        mCursors = loadCursors();
        return mCursors;
    } // doInBackground(Void...)

    @Override
    protected final void onProgressUpdate(final Void... noValues)
    {
        throw new AssertionError();
    } // onProgressUpdate(Void...)

    @Override
    protected final void onPostExecute(final Cursor[] cursor)
    {
        mCursors = null;
        onCursorsLoaded(cursor);
        cleanUp();
    } // onPostExecute(Cursor)

    @Override
    protected final void onCancelled()
    {
        if (mCursors != null)
        {
            for (final Cursor cursor : mCursors)
            {
                if (cursor != null)
                {
                    cursor.close();
                } // if
            } // for
            mCursors = null;
        } // if
        cleanUp();
    } // onCancelled()

    protected void cleanUp()
    {
        // Do nothing.
    } // cleanUp()

    protected abstract Cursor[] loadCursors();

    protected abstract void onCursorsLoaded(Cursor[] cursors);

} // class MultiCursorLoader
