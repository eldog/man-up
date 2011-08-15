package com.appspot.manup.autograph;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public final class DataManager
{
    private static final String TAG = DataManager.class.getSimpleName();

    private static final String SIGNATURE_FILE_EXT = ".png";

    public interface SignatureCapturedListener
    {
        void onSignatureCaptured(long id);
    } // SignatureCapturedListener

    public static final class Member implements BaseColumns
    {
        private static final String TABLE_NAME = "member";

        public static final String MAG_STRIPE = "mag_stripe";
        public static final String STUDENT_ID = "student_id";
        public static final String GIVEN_NAME = "gavin_name";
        public static final String SURNAME = "surname";
        public static final String EMAIL = "email";
        public static final String STUDENT_TYPE = "student_type";
        public static final String DEPARTMENT = "department";
        public static final String SIGNATURE_STATE = "signature_state";

        public static final String SIGNATURE_STATE_UNCAPTURED = "uncaptured";
        public static final String SIGNATURE_STATE_CAPTURED = "captured";
        public static final String SIGNATURE_STATE_UPLOADED = "uploaded";

        private Member()
        {
            super();
            throw new AssertionError();
        } // Signature

    } // Signature

    private static final class OpenHelper extends SQLiteOpenHelper
    {
        private static final String DATABASE_NAME = "members.db";
        private static final int DATABASE_VERSION = 1;

        public OpenHelper(final Context context)
        {
            super(context, DATABASE_NAME, null /* factory */, DATABASE_VERSION);
        } // OpenHelper

        @Override
        public void onCreate(final SQLiteDatabase db)
        {
            //@formatter:off

            final String createTableSql =

            "CREATE TABLE " + Member.TABLE_NAME + "("                          +
                Member._ID             + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Member.MAG_STRIPE      + " TEXT NOT NULL UNIQUE,"              +
                Member.STUDENT_ID      + " TEXT UNIQUE,"                       +
                Member.GIVEN_NAME      + " TEXT,"                              +
                Member.SURNAME         + " TEXT,"                              +
                Member.EMAIL           + " TEXT,"                              +
                Member.STUDENT_TYPE    + " TEXT,"                              +
                Member.DEPARTMENT      + " TEXT,"                              +
                Member.SIGNATURE_STATE + " TEXT NOT NULL DEFAULT "
                    + Member.SIGNATURE_STATE_UNCAPTURED                        +
            ")";

            //@formatter:on

            Log.v(TAG, createTableSql);

            db.execSQL(createTableSql);
        } // onCreate

        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion)
        {
            Log.w("DB Upgrade", "Upgrading database, this will drop tables and recreate.");
            db.execSQL("DROP TABLE IF EXISTS " + Member.TABLE_NAME);
            onCreate(db);
        } // onUpgrade

    } // OpenHelper

    private static DataManager sDataHelper = null;

    public static synchronized DataManager getInstance(final Context context)
    {
        if (sDataHelper == null)
        {
            sDataHelper = new DataManager(context);
        } // if
        return sDataHelper;
    } // getInstance

    private final Context mContext;
    private boolean mNotifyOnSignatureCaptured = false;
    private volatile SQLiteDatabase mDb = null;

    private DataManager(final Context context)
    {
        super();
        mContext = context.getApplicationContext();
        mNotifyOnSignatureCaptured = NetworkUtils.isOnline(mContext);
        mDb = new OpenHelper(mContext).getWritableDatabase();
    } // DataHelper

    public long addMember(final String magStripe)
    {
        final ContentValues memberValues = new ContentValues(1);
        memberValues.put(Member.MAG_STRIPE, magStripe);
        return upsertMember(magStripe, memberValues);
    } // addMember

    public long addOrUpdateMember(final UomLdapEntry uomLdapEntry)
    {
        return upsertMember(uomLdapEntry.getMagStripe(), uomLdapEntry.getContentValues());
    } // addOrUpdateMember

    public String getMagStripe(final long id)
    {
        return getMemberField(Member._ID, Long.toString(id), Member.MAG_STRIPE);
    } // getMagStripe

    public Cursor getMembersWithUncapturedSignatures()
    {
        return getMembersWithSignaturesInState(Member.SIGNATURE_STATE_UNCAPTURED);
    } // getMembersWithUncapturedSignatures

    public Cursor getMembersWithCapturedSignatures()
    {
        return getMembersWithSignaturesInState(Member.SIGNATURE_STATE_CAPTURED);
    } // getMembersWithCapturedSignatures

    private Cursor getMembersWithSignaturesInState(final String signatureState)
    {
        return mDb.query(
                Member.TABLE_NAME,
                null /* columns */,
                Member.SIGNATURE_STATE + "=?",
                new String[] { signatureState },
                null /* groupBy */,
                null /* having */,
                null /* orderBy */);
    } // getMembersWithSignaturesInState

    public boolean setSignatureCaptured(final long id)
    {
        final boolean stateChanged = updateSignatureState(id, Member.SIGNATURE_STATE_CAPTURED);
        if (stateChanged && mNotifyOnSignatureCaptured)
        {
            notifySignaturesCaptured();
        } // if
        return stateChanged;
    } // setSignatureCaptured

    public boolean setSignatureUploaded(final long id)
    {
        final boolean stateChanged = updateSignatureState(id, Member.SIGNATURE_STATE_UPLOADED);
        if (stateChanged)
        {
            deleteMember(id);
        } // if
        return stateChanged;
    } // setSignatureUploaded

    private boolean updateSignatureState(final long id, final String newState)
    {
        final ContentValues newStateValue = new ContentValues(1);
        newStateValue.put(Member.SIGNATURE_STATE, newState);
        return updateMember(id, newStateValue) == 1;
    } // updateSignatureState

    private void notifySignaturesCaptured()
    {
        mContext.startService(new Intent(mContext, UploadService.class));
    } // notifySignatureCaptured

    private boolean deleteMember(final long id)
    {
        final File signature = getSignatureFile(id);
        if (signature == null)
        {
            return false;
        } // if

        if (!signature.exists() || signature.delete())
        {
            final int membersDeleted = mDb.delete(
                    Member.TABLE_NAME,
                    Member._ID + "=?",
                    new String[] { Long.toString(id) });
            return membersDeleted == 1;
        } // if

        Log.w(TAG, "Failed to delete image file for " + id);

        return false;
    } // deleteMember

    public void setNotifyOnSignatureCaptured(final boolean notifyOnSignatureCapture)
    {
        mNotifyOnSignatureCaptured = notifyOnSignatureCapture;
        if (mNotifyOnSignatureCaptured)
        {
            notifySignaturesCaptured();
        } // if
    } // setNotifyOnSignatureCaptured

    private String getMemberField(final String uniqueColumn, final String uniqueValue,
            final String returnColumn)
    {
        Cursor c = null;
        try
        {
            c = mDb.query(
                    Member.TABLE_NAME,
                    new String[] { returnColumn },
                    uniqueColumn + "=?",
                    new String[] { uniqueValue },
                    null /* groupBy */,
                    null /* having */,
                    null /* orderBy */);
            if (c != null && c.moveToFirst())
            {
                return c.getString(0);
            } // if
            return null;
        } // try
        finally
        {
            if (c != null)
            {
                c.close();
            } // if
        } // finally
    } // getMemberField

    private long upsertMember(final String magStripe, final ContentValues memberValues)
    {
        mDb.beginTransaction();
        try
        {
            long id = getId(magStripe);
            if (id != -1)
            {
                if (updateMember(id, memberValues) != 1)
                {
                    return -1;
                } // if
            } // if
            else
            {
                id = insertMember(memberValues);
                if (id <= 0)
                {
                    return -1;
                } // if
            } // else
            mDb.setTransactionSuccessful();
            return id;
        } // try
        finally
        {
            mDb.endTransaction();
        } // finally
    } // upsertMember

    private long getId(final String magStripe)
    {
        final String id = getMemberField(Member.MAG_STRIPE, magStripe, Member._ID);
        return id != null ? Long.parseLong(id) : -1L;
    } // getId

    private int updateMember(final long id, final ContentValues updateValues)
    {
        return mDb.update(
                Member.TABLE_NAME,
                updateValues,
                Member._ID + "=?",
                new String[] { Long.toString(id) });
    } // updateMember

    private long insertMember(final ContentValues memberValues)
    {
        return mDb.insert(
                Member.TABLE_NAME,
                Member.MAG_STRIPE /* null column hack */,
                memberValues);
    } // insertMember

    public File getSignatureFile(final long id)
    {
        final File externalDir = mContext.getExternalFilesDir(null /* type */);
        if (externalDir == null)
        {
            return null;
        } // if
        return new File(externalDir, Long.toString(id) + SIGNATURE_FILE_EXT);
    } // getSignatureFile

} // SignatureDatabase
