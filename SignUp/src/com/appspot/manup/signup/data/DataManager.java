package com.appspot.manup.signup.data;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.appspot.manup.signup.extrainfo.MemberInfo;

public final class DataManager
{
    private static final String TAG = DataManager.class.getSimpleName();

    public static final long OPERATION_FAILED = -1L;

    private static final String SIGNATURE_FILE_EXT = ".png";

    public interface OnChangeListener
    {
        void onChange();
    } // interface OnChangeListener

    public static final class Member implements BaseColumns
    {
        private static final String TABLE_NAME = "member";

        public static final String PERSON_ID = "person_id";
        public static final String PERSON_ID_VALIDATED = "person_id_validated";
        public static final String LATEST_PENDING_SIGNATURE_REQUEST =
                "latest_pending_signature_request";
        public static final String EXTRA_INFO_STATE = "extra_info_state";
        public static final String GIVEN_NAME = "given_name";
        public static final String SURNAME = "surname";
        public static final String EMAIL = "email";
        public static final String MEMBER_TYPE = "member_type";
        public static final String DEPARTMENT = "department";
        public static final String SIGNATURE_STATE = "signature_state";

        public static final int PERSON_ID_LENGTH = 7;

        public static final String SIGNATURE_STATE_UNCAPTURED = "uncaptured";
        public static final String SIGNATURE_STATE_CAPTURED = "captured";
        public static final String SIGNATURE_STATE_UPLOADED = "uploaded";

        public static final String PERSON_ID_VALIDATED_UNKNWON = "unknown";
        public static final String PERSON_ID_VALIDATED_VALID = "valid";
        public static final String PERSON_ID_VALIDATED_INVALID = "invalid";

        public static final String EXTRA_INFO_STATE_NONE = "none";
        public static final String EXTRA_INFO_STATE_RETRIEVING = "retrieving";
        public static final String EXTRA_INFO_STATE_RETRIEVED = "retrieved";
        public static final String EXTRA_INFO_STATE_ERROR = "error";

        private Member()
        {
            super();
            throw new AssertionError();
        } // constructor()

    } // class Member

    private static final class MemberDbOpenHelper extends SQLiteOpenHelper
    {
        private static final String DATABASE_NAME = "members.db";
        private static final int DATABASE_VERSION = 16;

        public MemberDbOpenHelper(final Context context)
        {
            super(context, DATABASE_NAME, null /* cursor factory */, DATABASE_VERSION);
        } // constructor(Context)

        @Override
        public void onCreate(final SQLiteDatabase db)
        {
            //@formatter:off

            final String createMemberTableSql =

            "CREATE TABLE " + Member.TABLE_NAME + "(" +

                Member._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                Member.PERSON_ID + " TEXT NOT NULL UNIQUE "
                    + "CHECK (length(" + Member.PERSON_ID + ")=" + Member.PERSON_ID_LENGTH + ")," +

                Member.LATEST_PENDING_SIGNATURE_REQUEST + " INTEGER," +

                Member.PERSON_ID_VALIDATED + " TEXT NOT NULL "
                    + "DEFAULT " + Member.PERSON_ID_VALIDATED_UNKNWON
                    + " CHECK (" + Member.PERSON_ID_VALIDATED + " IN ('"
                        + Member.PERSON_ID_VALIDATED_UNKNWON + "','"
                        + Member.PERSON_ID_VALIDATED_VALID + "','"
                        + Member.PERSON_ID_VALIDATED_INVALID + "'))," +

                Member.EXTRA_INFO_STATE + " TEXT NOT NULL "
                    + "DEFAULT " + Member.EXTRA_INFO_STATE_NONE
                    + " CHECK (" + Member.EXTRA_INFO_STATE + " IN ('"
                        + Member.EXTRA_INFO_STATE_NONE + "','"
                        + Member.EXTRA_INFO_STATE_RETRIEVING + "','"
                        + Member.EXTRA_INFO_STATE_RETRIEVED + "','"
                        + Member.EXTRA_INFO_STATE_ERROR + "'))," +

                Member.GIVEN_NAME + " TEXT," +
                Member.SURNAME + " TEXT," +
                Member.EMAIL+ " TEXT," +
                Member.MEMBER_TYPE + " TEXT," +
                Member.DEPARTMENT + " TEXT," +

                Member.SIGNATURE_STATE + " TEXT NOT NULL "
                    + "DEFAULT " + Member.SIGNATURE_STATE_UNCAPTURED
                    + " CHECK (" + Member.SIGNATURE_STATE + " IN ('"
                        + Member.SIGNATURE_STATE_UNCAPTURED + "','"
                        + Member.SIGNATURE_STATE_CAPTURED + "','"
                        + Member.SIGNATURE_STATE_UPLOADED + "'))" +
            ")";

            //@formatter:on

            Log.v(TAG, createMemberTableSql);

            db.execSQL(createMemberTableSql);
        } // onCreate

        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion)
        {
            db.execSQL("DROP TABLE IF EXISTS " + Member.TABLE_NAME);
            onCreate(db);
        } // onUpgrade(SQLiteDatabase, int, int)

    } // class MemberDbOpenHelper

    private static final Set<OnChangeListener> sListeners = new HashSet<OnChangeListener>();
    private static DataManager sSingleton = null;

    public static synchronized DataManager getDataManager(final Context context)
    {
        if (sSingleton == null)
        {
            sSingleton = new DataManager(context);
        } // if
        return sSingleton;
    } // getDataManager(Context)

    public static void registerListener(final OnChangeListener listener)
    {
        synchronized (sListeners)
        {
            sListeners.add(listener);
        } // synchronized
    } // registerListener(OnChangeListener)

    public static void unregisterListener(final OnChangeListener listener)
    {
        synchronized (sListeners)
        {
            sListeners.remove(listener);
        } // synchronized
    } // unregisterListener(OnChangeListener)

    public static String getDisplayName(final Cursor cursor)
    {
        final String givenName = cursor.getString(cursor.getColumnIndexOrThrow(Member.GIVEN_NAME));
        final String surname = cursor.getString(cursor.getColumnIndexOrThrow(Member.SURNAME));
        final boolean hasGivenName = !TextUtils.isEmpty(givenName);
        final boolean hasSurname = !TextUtils.isEmpty(surname);
        if (hasGivenName && hasSurname)
        {
            return givenName + " " + surname;
        } // if
        else if (hasGivenName)
        {
            return givenName;
        } // else if
        else if (hasSurname)
        {
            return surname;
        } // else if
        else
        {
            return null;
        } // else
    } // getDisplayName(Cursor)

    public static boolean isValidPersonId(final String possiblePersonId)
    {
        return possiblePersonId.length() == Member.PERSON_ID_LENGTH
                && TextUtils.isDigitsOnly(possiblePersonId);
    } // isValidPersonId(String)

    private static long getUnixTime()
    {
        return System.currentTimeMillis() / 1000L;
    } // getUnixTime()

    private final Object mLock = new Object();
    private final Context mContext;
    private SQLiteDatabase mDb = null;

    private DataManager(final Context context)
    {
        super();
        mContext = context.getApplicationContext();
    } // DataManager(Context)

    private SQLiteDatabase getDb()
    {
        synchronized (mLock)
        {
            if (mDb == null)
            {
                mDb = new MemberDbOpenHelper(mContext).getWritableDatabase();
            } // if
        } // synchronized
        return mDb;
    } // getDb()

    private void notifyListeners()
    {
        synchronized (sListeners)
        {
            for (final OnChangeListener listener : sListeners)
            {
                listener.onChange();
            } // for
        } // synchronized
    } // notifyListeners()

    private String getMemberField(final String uniqueColumn, final String uniqueValue,
            final String returnColumn)
    {
        Cursor cursor = null;
        try
        {
            cursor = queryMembers(
                    new String[] { returnColumn },
                    uniqueColumn + "=?",
                    new String[] { uniqueValue },
                    null /* order by */);
            if (cursor != null && cursor.moveToFirst())
            {
                return cursor.getString(0);
            } // if
            return null;
        } // try
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            } // if
        } // finally
    } // getMemberField(String, String, String)

    public Cursor queryMembers(final String[] columns, final String selection,
            final String[] selectionArgs, final String orderBy)
    {
        return getDb().query(
                Member.TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null /* group by */,
                null /* having */,
                orderBy);
    } // queryMembers(String[], String, String[], String)

    public long requestSignature(final MemberInfo memberInfo)
    {
        final long id = requestSignature(memberInfo.getPersonId());
        if (id == OPERATION_FAILED)
        {
            return OPERATION_FAILED;
        } // if
        if (!addExtraInfo(memberInfo))
        {
            Log.w(TAG, "Failed to add extract info.\n" + memberInfo);
        } // if
        return id;
    } // requestSignature(MemberInfo)

    public long requestSignature(final String personId)
    {
        final SQLiteDatabase db = getDb();
        final ContentValues requestValues = new ContentValues(2);
        requestValues.put(Member.LATEST_PENDING_SIGNATURE_REQUEST, getUnixTime());
        long id = OPERATION_FAILED;
        boolean requestMade = false;
        db.beginTransaction();
        try
        {
            id = getId(personId);
            if (id != OPERATION_FAILED)
            {
                requestMade = updateMember(id, requestValues);
            } // if
            else
            {
                requestValues.put(Member.PERSON_ID, personId);
                id = insertMember(requestValues);
            } // else
            requestMade = id != OPERATION_FAILED;
            if (requestMade)
            {
                db.setTransactionSuccessful();
            } // if
        } // try
        finally
        {
            db.endTransaction();
        } // finally
        if (requestMade)
        {
            notifyListeners();
        } // if
        return id;
    } // requestSignature(String)

    public boolean addExtraInfo(final MemberInfo memberInfo)
    {
        final long id = getId(memberInfo.getPersonId());
        if (id == OPERATION_FAILED)
        {
            Log.e(TAG, "Failed to get internal ID of the member " + memberInfo.getPersonId());
            return false;
        } // if

        final ContentValues extraValues = memberInfo.getContentValues();
        Log.v(TAG, "Adding extra info for " + memberInfo.getPersonId() + ": "
                + extraValues.toString());

        // As extra information was retrieved, the person ID must be valid.
        extraValues.put(Member.PERSON_ID_VALIDATED, Member.PERSON_ID_VALIDATED_VALID);
        extraValues.put(Member.EXTRA_INFO_STATE, Member.EXTRA_INFO_STATE_RETRIEVED);
        final boolean addedextraInfo = updateMember(id, extraValues);

        if (addedextraInfo)
        {
            notifyListeners();
        } // if

        return addedextraInfo;
    } // addExtraInfo(MemberLdapEntry)

    public boolean memberHasSignature(final long id)
    {
        return Member.SIGNATURE_STATE_CAPTURED.equals(
                getMemberField(Member._ID, Long.toString(id), Member.SIGNATURE_STATE));
    } // memberHasSignature(long)

    public boolean setExtraInfoStateToRetrieving(final long id)
    {
        return setExtraInfoState(id, Member.EXTRA_INFO_STATE_RETRIEVING);
    } // setExtraInfoStateToRetrieving(long)

    public boolean setExtraInfoStateToError(final long id)
    {
        return setExtraInfoState(id, Member.EXTRA_INFO_STATE_ERROR);
    } // setExtraInfoState(long)

    private boolean setExtraInfoState(final long id, final String newState)
    {
        final ContentValues memberInfoStateValues = new ContentValues(1);
        memberInfoStateValues.put(Member.EXTRA_INFO_STATE, newState);
        return updateMember(id, memberInfoStateValues);
    } // setExtraInfoState(String)

    public boolean setSignatureStateCaptured(final long id)
    {
        return updateSignatureState(id, Member.SIGNATURE_STATE_CAPTURED);
    } // setSignatureCaptured(long)

    public boolean setSignatureStateUploaded(final long id)
    {
        return updateSignatureState(id, Member.SIGNATURE_STATE_UPLOADED);
    } // setSignatureUploaded(long)

    private boolean updateSignatureState(final long id, final String newState)
    {
        final ContentValues newStateValue = new ContentValues(2);
        newStateValue.put(Member.SIGNATURE_STATE, newState);
        if (Member.SIGNATURE_STATE_CAPTURED.equals(newState))
        {
            newStateValue.put(Member.LATEST_PENDING_SIGNATURE_REQUEST, (String) null);
        } // if
        final boolean updated = updateMember(id, newStateValue);
        if (updated)
        {
            notifyListeners();
        } // if
        return updated;
    } // updateSignatureState(long, String)

    public boolean setPersonIdInvalid(final long id)
    {
        final ContentValues newPersonIdValidValue = new ContentValues(1);
        newPersonIdValidValue.put(Member.PERSON_ID_VALIDATED, Member.PERSON_ID_VALIDATED_INVALID);
        return updateMember(id, newPersonIdValidValue);
    } // setPersonIdInvalid(long)

    public void deleteAllMembers()
    {
        getDb().delete(Member.TABLE_NAME, null /* whereClause */, null /* whereArgs */);
    } // deleteAllMembers()

    public long loadTestData()
    {
        deleteAllMembers();
        final ContentValues[] memberValues = TestData.getMembers();
        final SQLiteDatabase db = getDb();
        for (final ContentValues cv : memberValues)
        {
            if (db.insert(Member.TABLE_NAME, null /* null column hack */, cv) == OPERATION_FAILED)
            {
                Log.e(TAG, "Failed to insert test data. " + cv);
            } // if
        } // for
        return memberValues.length;
    } // loadTestData()

    public String getPersonId(final long id)
    {
        return getMemberField(Member._ID, Long.toString(id), Member.PERSON_ID);
    } // getPersonId(long)

    private long getId(final String personId)
    {
        final String id = getMemberField(Member.PERSON_ID, personId, Member._ID);
        return id != null ? Long.parseLong(id) : OPERATION_FAILED;
    } // getId(String)

    private long insertMember(final ContentValues memberValues)
    {
        try
        {
            return getDb().insert(
                    Member.TABLE_NAME,
                    Member._ID /* null column hack */,
                    memberValues);
        } // try
        catch (final SQLiteConstraintException e)
        {
            Log.d(TAG, "Failed to inser member with values: " + memberValues, e);
            return OPERATION_FAILED;
        } // catch
    } // insertMember(ContentValues)

    private boolean updateMember(final long id, final ContentValues updatedMemberValues)
    {
        final boolean updated = getDb().update(
                Member.TABLE_NAME,
                updatedMemberValues,
                Member._ID + "=?",
                new String[] { Long.toString(id) }) == 1;
        if (updated)
        {
            notifyListeners();
        } // if
        return updated;
    } // updateMember(long, ContentValues)

    public File getSignatureFile(final long id)
    {
        final String personId = getPersonId(id);
        if (personId == null)
        {
            return null;
        } // if
        final File externalDir = mContext.getExternalFilesDir(null /* type */);
        if (externalDir == null)
        {
            return null;
        } // if
        return new File(externalDir, personId + SIGNATURE_FILE_EXT);
    } // getSignatureFile(long)

} // class DataManager
