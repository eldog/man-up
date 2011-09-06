package com.appspot.manup.signup.data;

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
    } // constructor()

    @Override
    protected final void onPreExecute()
    {
        // Do nothing.
    } // onPreExecute()

    @Override
    protected final Cursor doInBackground(final Void... noParams)
    {
        mCursor = loadCursor();
        return mCursor;
    } // doInBackground(Void...)

    @Override
    protected final void onProgressUpdate(final Void... noValues)
    {
        throw new AssertionError();
    } // onProgressUpdate(Void...)

    @Override
    protected final void onPostExecute(final Cursor cursor)
    {
        mCursor = null;
        onCursorLoaded(cursor);
    } // onPostExecute(Cursor)

    @Override
    protected final void onCancelled()
    {
        if (mCursor != null)
        {
            mCursor.close();
            mCursor = null;
        } // if
    } // onCancelled()

    protected abstract Cursor loadCursor();

    protected abstract void onCursorLoaded(Cursor cursor);

} // class CursorLoader
