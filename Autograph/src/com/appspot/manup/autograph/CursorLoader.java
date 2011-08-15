package com.appspot.manup.autograph;

import android.database.Cursor;
import android.os.AsyncTask;

public abstract class CursorLoader extends AsyncTask<Void, Void, Cursor>
{
    @SuppressWarnings("unused")
    private static final String TAG = CursorLoader.class.getSimpleName();

    private volatile Cursor mCursor = null;

    public CursorLoader()
    {
        super();
    } // CursorLoader

    @Override
    protected final void onPreExecute()
    {
        // Do nothing.
    } // onPreExecute

    @Override
    protected final Cursor doInBackground(final Void... noParams)
    {
        return mCursor = loadCursor();
    } // doInBackground

    @Override
    protected final void onProgressUpdate(final Void... noValues)
    {
        throw new AssertionError();
    } // onProgressUpdate

    @Override
    protected final void onPostExecute(final Cursor cursor)
    {
        mCursor = null;
        onCursorLoaded(cursor);
    } // onPostExecute

    @Override
    protected final void onCancelled()
    {
        if (mCursor != null)
        {
            mCursor.close();
            mCursor = null;
        } // if
    } // onCancelled

    protected abstract Cursor loadCursor();

    protected abstract void onCursorLoaded(Cursor cursor);

} // CursorLoader
