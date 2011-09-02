package com.appspot.manup.signup.data;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.appspot.manup.signup.ldap.MemberLdapEntry;

public final class DataManager
{
    private static final String TAG = DataManager.class.getSimpleName();

    private static final String SIGNATURE_FILE_EXT = ".png";

    public static final class Member implements BaseColumns
    {
        private static final String TABLE_NAME = "member";

        public static final String STUDENT_ID = "student_id";

        public static final String SIGNATURE_STATE = "signature_state";
        public static final String SIGNATURE_STATE_UNCAPTURED = "uncaptured";
        public static final String SIGNATURE_STATE_CAPTURED = "captured";
        public static final String SIGNATURE_STATE_UPLOADED = "uploaded";

        public static final String STUDENT_ID_VALIDATED = "student_id_validated";
        public static final String STUDENT_ID_VALIDATED_NO = "no";
        public static final String STUDENT_ID_VALIDATED_VALID = "valid";
        public static final String STUDENT_ID_VALIDATED_INVALID = "invalid";

        public static final String RETRIEVED_ADDITIONAL_INFO = "retrieved_additional_info";
        public static final String RETRIEVED_ADDITIONAL_INFO_NO = "no";
        public static final String RETRIEVED_ADDITIONAL_INFO_YES = "yes";

        public static final String GIVEN_NAME = "given_name";
        public static final String SURNAME = "surname";
        public static final String EMAIL = "email";
        public static final String STUDENT_TYPE = "student_type";
        public static final String DEPARTMENT = "department";

        private Member()
        {
            super();
            throw new AssertionError();
        } // Signature

    } // Signature

    private static final class OpenHelper extends SQLiteOpenHelper
    {
        private static final String DATABASE_NAME = "members.db";
        private static final int DATABASE_VERSION = 8;

        public OpenHelper(final Context context)
        {
            super(context, DATABASE_NAME, null /* factory */, DATABASE_VERSION);
        } // OpenHelper

        @Override
        public void onCreate(final SQLiteDatabase db)
        {
            //@formatter:off

            final String createTableSql =

            "CREATE TABLE " + Member.TABLE_NAME + "("                                    +
                Member._ID                       + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Member.STUDENT_ID                + " TEXT NOT NULL UNIQUE,"              +

                Member.STUDENT_ID_VALIDATED      + " TEXT NOT NULL "
                    + "DEFAULT " + Member.STUDENT_ID_VALIDATED_NO + ","                  +

                Member.RETRIEVED_ADDITIONAL_INFO + " TEXT NOT NULL "
                    + "DEFAULT " + Member.RETRIEVED_ADDITIONAL_INFO_NO + ","             +

                Member.GIVEN_NAME                + " TEXT,"                              +
                Member.SURNAME                   + " TEXT,"                              +
                Member.EMAIL                     + " TEXT,"                              +
                Member.STUDENT_TYPE              + " TEXT,"                              +
                Member.DEPARTMENT                + " TEXT,"                              +

                Member.SIGNATURE_STATE           + " TEXT NOT NULL "
                + "DEFAULT " + Member.SIGNATURE_STATE_UNCAPTURED                         +
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
    private volatile SQLiteDatabase mDb = null;

    private DataManager(final Context context)
    {
        super();
        mContext = context.getApplicationContext();
        mDb = new OpenHelper(mContext).getWritableDatabase();
    } // DataManager

    /*
     * Member Added Notification
     */

    private EventNotifier<Long> mMemberAddedNotifier = new EventNotifier<Long>();

    public static abstract class MemberAddedListener implements EventListener<Long>
    {
        @Override
        public final void onEvent(final Long id)
        {
            onMemberAdded(id);
        } // onEvent
        public abstract void onMemberAdded(long id);
    } // class MemberAddedListener

    public void registerMemberAddedListener(final MemberAddedListener listener)
    {
        mMemberAddedNotifier.registerListener(listener);
    } // registerMemberAddedListener

    public void unregisterMemberAddedListener(final MemberAddedListener listener)
    {
        mMemberAddedNotifier.unregisterListener(listener);
    } // unregisterMemberAddedListener

    /***/

    /*
     * Signature Captured Notification
     */

    private EventNotifier<Long> mSignatureCatpuredNotifier = new EventNotifier<Long>();

    public static abstract class SignatureCapturedListener implements EventListener<Long>
    {
        @Override
        public final void onEvent(final Long id)
        {
            onSignatureCaptured(id);
        } // onEvent

        public abstract void onSignatureCaptured(final long id);
    } // SignatureCapturedListener

    public void registerSignatureCapturedListener(final SignatureCapturedListener listener)
    {
        mSignatureCatpuredNotifier.registerListener(listener);
    } // registerSignatureCapturedListener

    public void unregisterSignatureCapturedListener(final SignatureCapturedListener listener)
    {
        mSignatureCatpuredNotifier.unregisterListener(listener);
    } // unregisterSignatureCapturedListener

    /***/

    /*
     * Additional Info Added Notification
     */
    private final EventNotifier<Long> mAdditionalInfoAddedNotifier = new EventNotifier<Long>();

    public static abstract class AdditionInfoAddedListener implements EventListener<Long>
    {
        @Override
        public final void onEvent(final Long id)
        {
            onAdditionalInfoAdded(id);
        } //
        public abstract void onAdditionalInfoAdded(final long id);
    } // AdditionInfoAddedListener

    public void registerAdditionInfoAddedListener(AdditionInfoAddedListener listener)
    {
        mAdditionalInfoAddedNotifier.registerListener(listener);
    } // registerAdditionInfoAddedListener

    public void unregisterAdditionalInfoAddedListener(AdditionInfoAddedListener listener)
    {
        mAdditionalInfoAddedNotifier.unregisterListener(listener);
    } // unregisterAdditionalInfoAddedListener

    /***/

    public long addMember(final String studentId)
    {
        final ContentValues memberValues = new ContentValues(1);
        memberValues.put(Member.STUDENT_ID, studentId);
        return insertMember(memberValues);
    } // addMember

    public boolean addAdditionalMemberInfo(final MemberLdapEntry additionalInfo)
    {
        Log.d(TAG, "Student ID: " + additionalInfo.getStudentId());
        final long id = getId(additionalInfo.getStudentId());
        if (id == -1L)
        {
            Log.e(TAG, "Failed to get ID");
            return false;
        } // if

        final ContentValues additionalValues = additionalInfo.getContentValues();
        Log.v(TAG, "Adding additional info for " + additionalInfo.getStudentId() + ": "
                + additionalValues.toString());
        /*
         * As additional information was retrieved, the student ID must be
         * valid.
         */
        additionalValues.put(Member.STUDENT_ID_VALIDATED, Member.STUDENT_ID_VALIDATED_VALID);
        additionalValues.put(Member.RETRIEVED_ADDITIONAL_INFO,
                Member.RETRIEVED_ADDITIONAL_INFO_YES);
        final boolean addedAdditionalInfo = updateMember(id, additionalValues);
        if (addedAdditionalInfo)
        {
            mAdditionalInfoAddedNotifier.notifyListeners(id);
        } // if
        return addedAdditionalInfo;
    } // addAdditionalMemberInfo

    public String getStudentId(final long id)
    {
        return getMemberField(Member._ID, Long.toString(id), Member.STUDENT_ID);
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

    public Cursor getMembersWithoutAdditionalInfo()
    {
        return mDb.query(
                Member.TABLE_NAME,
                null /* columns */,
                Member.RETRIEVED_ADDITIONAL_INFO + "=?",
                new String[] { Member.RETRIEVED_ADDITIONAL_INFO_NO },
                null /* groupBy */,
                null /* having */,
                null /* orderBy */);
    } // getMembersWithoutAdditionalInfo

    public boolean setSignatureCaptured(final long id)
    {
        final boolean updated = updateSignatureState(id, Member.SIGNATURE_STATE_CAPTURED);
        if (updated)
        {
            mSignatureCatpuredNotifier.notifyListeners(id);
        } // if
        return updated;
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
        return updateMember(id, newStateValue);
    } // updateSignatureState

    public boolean setStudentIdInvalid(final long id)
    {
        final ContentValues newStudentIdValidValue = new ContentValues(1);
        newStudentIdValidValue.put(Member.STUDENT_ID_VALIDATED, Member.STUDENT_ID_VALIDATED_VALID);
        return updateMember(id, newStudentIdValidValue);
    } // setStudentIdInvalid

    public int flushMembers()
    {
        return mDb.delete(Member.TABLE_NAME, null /* whereClause */, null /* whereArgs */);
    } // flushMembers

    private long getId(final String studentId)
    {
        final String id = getMemberField(Member.STUDENT_ID, studentId, Member._ID);
        return id != null ? Long.parseLong(id) : -1L;
    } // getId

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

    private boolean updateMember(final long id, final ContentValues updatedMemberValues)
    {
        return mDb.update(
                Member.TABLE_NAME,
                updatedMemberValues,
                Member._ID + "=?",
                new String[] { Long.toString(id) }) == 1;
    } // updateMember

    private long insertMember(final ContentValues memberValues)
    {
        final long id = mDb.insert(
                Member.TABLE_NAME,
                Member.STUDENT_ID /* nullColumnHack */,
                memberValues);
        if (id != -1L)
        {
            mMemberAddedNotifier.notifyListeners(id);
        } // if
        return id;
    } // insertMember

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

    public File getSignatureFile(final long id)
    {
        final File externalDir = mContext.getExternalFilesDir(null /* type */);
        if (externalDir == null)
        {
            return null;
        } // if
        return new File(externalDir, Long.toString(id) + SIGNATURE_FILE_EXT);
    } // getSignatureFile

} // class DataManager
