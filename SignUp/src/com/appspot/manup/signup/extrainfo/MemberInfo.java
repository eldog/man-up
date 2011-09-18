package com.appspot.manup.signup.extrainfo;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.text.TextUtils;

import com.appspot.manup.signup.data.DataManager.Member;
import com.novell.ldap.LDAPEntry;

public final class MemberInfo
{
    private static final String LDAP_PERSON_ID = "umanPersonID";
    private static final String LDAP_GIVEN_NAME = "givenName";
    private static final String LDAP_SURNAME = "sn";
    private static final String LDAP_EMAIL = "mail";
    private static final String LDAP_DEPARTMENT = "ou";
    private static final String LDAP_EMPLOYEE_TYPE = "employeeType";

    private static final String VALUE_DELIMITER = ", ";

    public static MemberInfo fromLdapEntry(final LDAPEntry memberEntry)
    {
        return new MemberInfo(
                getStringValue(memberEntry, LDAP_PERSON_ID),
                getStringValue(memberEntry, LDAP_GIVEN_NAME),
                getStringValue(memberEntry, LDAP_SURNAME),
                getStringValue(memberEntry, LDAP_EMAIL),
                getStringValue(memberEntry, LDAP_DEPARTMENT),
                getStringValue(memberEntry, LDAP_EMPLOYEE_TYPE));
    } // fromLdapEntry(LDAPEntrty)

    private static String getStringValue(final LDAPEntry memberEntry, final String attribute)
    {
        return TextUtils.join(VALUE_DELIMITER, memberEntry.getAttribute(attribute)
                .getStringValueArray());
    } // getStringValue(LDAPEntry, String)

    public static MemberInfo fromJson(final JSONObject jsonMemberInfo)
    {
        return new MemberInfo(
                getJsonValue(jsonMemberInfo, LDAP_PERSON_ID),
                getJsonValue(jsonMemberInfo, LDAP_GIVEN_NAME),
                getJsonValue(jsonMemberInfo, LDAP_SURNAME),
                getJsonValue(jsonMemberInfo, LDAP_EMAIL),
                getJsonValue(jsonMemberInfo, LDAP_DEPARTMENT),
                getJsonValue(jsonMemberInfo, LDAP_EMPLOYEE_TYPE));
    } // fromJson(JSONObject)

    private static String getJsonValue(final JSONObject jsonObject, final String key)
    {
        if (jsonObject.has(key))
        {
            try
            {
                return jsonObject.getString(key);
            } // try
            catch (final JSONException valueNotStringException)
            {
                return null;
            } // catch
        } // if
        return null;
    } // getJsonValue(JSONObject, String)

    private final String mPersonId;
    private final String mGivenName;
    private final String mSurname;
    private final String mEmail;
    private final String mDepartment;
    private final String mMemberType;

    private MemberInfo(final String personId, final String givenName, final String surname,
            final String email, final String department, final String memberType)
    {
        super();
        if (personId == null)
        {
            throw new IllegalArgumentException("personId cannot be null.");
        } // if
        mPersonId = personId;
        mGivenName = givenName;
        mSurname = surname;
        mEmail = email;
        mDepartment = department;
        mMemberType = memberType;
    } // constructor(String, String, String, String, String, String)

    public ContentValues getContentValues()
    {
        final ContentValues contentValues = new ContentValues();
        contentValues.put(Member.PERSON_ID, mPersonId);
        if (mGivenName != null)
        {
            contentValues.put(Member.GIVEN_NAME, mGivenName);
        } // if
        if (mSurname != null)
        {
            contentValues.put(Member.SURNAME, mSurname);
        } // if
        if (mEmail != null)
        {
            contentValues.put(Member.EMAIL, mEmail);
        } // if
        if (mDepartment != null)
        {
            contentValues.put(Member.DEPARTMENT, mDepartment);
        } // if
        if (mMemberType != null)
        {
            contentValues.put(Member.MEMBER_TYPE, mMemberType);
        } // if
        return contentValues;
    } // getContentValues()

    @Override
    public String toString()
    {
        return new StringBuilder(MemberInfo.class.getSimpleName())
                .append("\n{\n\tPerson ID: ")
                .append(mPersonId)
                .append("\n\tGiven Name: ")
                .append(mGivenName)
                .append("\n\tSurname: ")
                .append(mSurname)
                .append("\n\tEmail: ")
                .append(mEmail)
                .append("\n\tDepartment: ")
                .append(mDepartment)
                .append("\n\tMember Type: ")
                .append(mMemberType)
                .append("\n}")
                .toString();
    } // toString()

    public boolean hasExtraInfo()
    {
        return mGivenName != null || mSurname != null || mEmail != null || mDepartment != null
                || mMemberType != null;
    } // hasExtraInfo()

    public String getPersonId()
    {
        return mPersonId;
    } // getPersonId()

    public String getGivenName()
    {
        return mGivenName;
    } // getGivenName()

    public String getSurname()
    {
        return mSurname;
    } // getSurname

    public String getEmail()
    {
        return mEmail;
    } // getEmail()

    public String getDepartment()
    {
        return mDepartment;
    } // getDepartment()

    public String getMemberType()
    {
        return mMemberType;
    } // getMemberType()

} // class MemberInfo
