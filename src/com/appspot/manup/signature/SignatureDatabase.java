package com.appspot.manup.signature;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public final class SignatureDatabase
{
    private static final String TAG = SignatureDatabase.class.getSimpleName();

    public interface SignatureCapturedListener
    {
        void onSignatureCaptured(long id);
    } // SignatureCapturedListener

    public static final class Signature implements BaseColumns
    {
        private static final String TABLE_NAME = "signature";

        public static final String STUDENT_ID = "student_id";
        public static final String SIGNATURE_STATE = "signature_state";

        public static final String SIGNATURE_NONE = "n";
        public static final String SIGNATURE_CAPTURED = "c";
        public static final String SIGNATURE_UPLOADED = "u";

        private Signature()
        {
            super();
            throw new AssertionError();
        } // Signature

    } // Signature

    private static final class OpenHelper extends SQLiteOpenHelper
    {
        private static final String DATABASE_NAME = "manup_signatures.db";
        private static final int DATABASE_VERSION = 5;

        public OpenHelper(final Context context)
        {
            super(context, DATABASE_NAME, null /* factory */, DATABASE_VERSION);
        } // OpenHelper

        @Override
        public void onCreate(final SQLiteDatabase db)
        {
            //@formatter:off

            final String createTableSql =

            "CREATE TABLE " + Signature.TABLE_NAME + "("                                +
                Signature._ID             + " INTEGER PRIMARY KEY AUTOINCREMENT,"       +
                Signature.STUDENT_ID      + " TEXT UNIQUE ON CONFLICT REPLACE,"          +
                Signature.SIGNATURE_STATE + " TEXT DEFAULT " + Signature.SIGNATURE_NONE +
            ")";

            //@formatter:on

            Log.v(TAG, createTableSql);

            db.execSQL(createTableSql);
        } // onCreate

        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion)
        {
            Log.w("DB Upgrade", "Upgrading database, this will drop tables and recreate.");
            db.execSQL("DROP TABLE IF EXISTS " + Signature.TABLE_NAME);
            onCreate(db);
        } // onUpgrade

    } // OpenHelper

    private static SignatureDatabase sDataHelper = null;

    public static synchronized SignatureDatabase getInstance(final Context context)
    {
        if (sDataHelper == null)
        {
            sDataHelper = new SignatureDatabase(context);
        } // if
        return sDataHelper;
    } // getInstance

    private final Context mContext;
    private SQLiteDatabase mDb = null;
    private volatile OpenHelper mOpenHelper = null;

    private SignatureDatabase(final Context context)
    {
        super();
        mContext = context.getApplicationContext();
        mOpenHelper = new OpenHelper(mContext);
    } // DataHelper

    private synchronized SQLiteDatabase getDatabase()
    {
        if (mDb == null)
        {
            mDb = mOpenHelper.getWritableDatabase();
        } // if
        return mDb;
    } // getDatabase

    public long addSignature(final String studentId)
    {
        final SQLiteDatabase db = getDatabase();
        final ContentValues cv = new ContentValues(1);
        cv.put(Signature.STUDENT_ID, studentId);
        long id = db.insert(Signature.TABLE_NAME, Signature.STUDENT_ID, cv);
        notifyOnSignatureAddedListeners(id);
        return id;
    } // insert

    private void notifyOnSignatureAddedListeners(long id)
    {
        if (id != -1)
        {
            for (OnSignatureAddedListener onSignatureAddedListener : mOnSignatureAddedListeners)
            {
                onSignatureAddedListener.onSignatureAdded(id);
            }
        }
    }

    public interface OnSignatureAddedListener
    {
        void onSignatureAdded(long id);
    }

    private Set<OnSignatureAddedListener> mOnSignatureAddedListeners = new HashSet<OnSignatureAddedListener>();

    public boolean addOnSignatureAddedListener(OnSignatureAddedListener onSignatureAddedListener)
    {
        return mOnSignatureAddedListeners.add(onSignatureAddedListener);
    }

    public boolean removeOnSignatureAddedListener(OnSignatureAddedListener onSignatureAddedListener)
    {
        return mOnSignatureAddedListeners.remove(onSignatureAddedListener);
    }

    public File getImageFile(final long id)
    {
        final File externalDir = mContext.getExternalFilesDir(null /* type */);
        if (externalDir == null)
        {
            return null;
        } // if
        return new File(externalDir, Long.toString(id) + ".png");
    } // getImageFile

    public String getStudentId(final long id)
    {
        Cursor c = null;
        try
        {
            c = getDatabase().query(
                    Signature.TABLE_NAME,
                    new String[] { Signature.STUDENT_ID },
                    Signature._ID + "=?",
                    new String[] { Long.toString(id) },
                    null /* groupBy */,
                    null /* having */,
                    null /* orderBy */);
            if (c != null && c.moveToFirst())
            {
                return c.getString(0);
            } // if
            Log.e(TAG, "Failed to get student ID for " + id);
            return null;
        } // try
        finally
        {
            if (c != null)
            {
                c.close();
            } // if
        } // finally
    } // getStudentId

    private boolean signatureNone(final long id)
    {
        return updateSignatureState(id, Signature.SIGNATURE_NONE);
    } // signatureNone

    public boolean signatureCaptured(final long id)
    {
        final boolean stateChanged = updateSignatureState(id, Signature.SIGNATURE_CAPTURED);
        if (stateChanged)
        {
            mContext.startService(new Intent(mContext, UploadService.class));
        } // if
        return stateChanged;
    } // signatureCaptured

    public boolean signatureUploaded(final long id)
    {
        final boolean stateChanged = updateSignatureState(id, Signature.SIGNATURE_UPLOADED);
        if (stateChanged)
        {
            deleteSignature(id);
        } // if
        return stateChanged;
    } // signatureUploaded

    private boolean updateSignatureState(final long id, final String newState)
    {
        final ContentValues newSignatureState = new ContentValues(1);
        newSignatureState.put(Signature.SIGNATURE_STATE, newState);
        final int rowsUpdated = getDatabase().update(
                Signature.TABLE_NAME,
                newSignatureState,
                Signature._ID + "=?",
                new String[] { Long.toString(id) });
        return rowsUpdated == 1;
    } // update

    private boolean deleteSignature(final long id)
    {
        final File image = getImageFile(id);
        if (image == null)
        {
            return false;
        } // if

        if (image.delete())
        {
            final int signaturesDeleted = getDatabase().delete(
                    Signature.TABLE_NAME,
                    Signature._ID + "=?",
                    new String[] { Long.toString(id) });
            return signaturesDeleted == 1;
        } // if

        Log.w(TAG, "Failed to delete image file for " + id);

        if (!signatureNone(id))
        {
            Log.e(TAG, "Failed to set signature state to NONE for " + id);
        } // if

        return false;
    } // deleteSignature

    public Cursor getCapturedSignatures()
    {
        return getDatabase().query(
                Signature.TABLE_NAME,
                null /* columns */,
                Signature.SIGNATURE_STATE + "=?",
                new String[] { Signature.SIGNATURE_CAPTURED },
                null /* groupBy */,
                null /* having */,
                null /* orderBy */);
    } // getCapturedSignatures

} // SignatureDatabase
