package com.appspot.manup.signup.extrainfo;

import android.content.ContentValues;
import android.text.TextUtils;

import com.appspot.manup.signup.data.DataManager.Member;
import com.novell.ldap.LDAPEntry;

public final class MemberExtraInfo
{
    private static final String LDAP_PERSON_ID = "umanPersonID";
    private static final String LDAP_GIVEN_NAME = "givenName";
    private static final String LDAP_SURNAME = "sn";
    private static final String LDAP_EMAIL = "mail";
    private static final String LDAP_DEPARTMENT = "ou";
    private static final String LDAP_EMPLOYEE_TYPE = "employeeType";

    private static final String VALUE_DELIMITER = ", ";

    static MemberExtraInfo fromLdapEntry(final LDAPEntry memberEntry)
    {
        return new MemberExtraInfo(
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

    private final String mPersonId;
    private final String mGivenName;
    private final String mSurname;
    private final String mEmail;
    private final String mDepartment;
    private final String mMemberType;

    private MemberExtraInfo(final String personId, final String givenName, final String surname,
            final String email, final String department, final String memberType)
    {
        super();
        mPersonId = personId;
        mGivenName = givenName;
        mSurname = surname;
        mEmail = email;
        mDepartment = department;
        mMemberType = memberType;
    } // constructor(String, String, String, String, String, String)

    public ContentValues getContentValues()
    {
        final ContentValues contentValues = new ContentValues(6);
        contentValues.put(Member.PERSON_ID, mPersonId);
        contentValues.put(Member.GIVEN_NAME, mGivenName);
        contentValues.put(Member.SURNAME, mSurname);
        contentValues.put(Member.EMAIL, mEmail);
        contentValues.put(Member.DEPARTMENT, mDepartment);
        contentValues.put(Member.MEMBER_TYPE, mMemberType);
        return contentValues;
    } // createContentValuesFromLdap

    @Override
    public String toString()
    {
        return new StringBuilder(MemberExtraInfo.class.getSimpleName())
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

    public String getPersonId()
    {
        return mPersonId;
    } // getPersonId()

    public String getGivenName()
    {
        return mGivenName;
    } // getGivenName

    public String getSurname()
    {
        return mSurname;
    } // getSurname

    public String getEmail()
    {
        return mEmail;
    } // getEmail

    public String getDepartment()
    {
        return mDepartment;
    } // getDepartment

    public String getMemberType()
    {
        return mMemberType;
    } // getMemberType()

} // class MemberExtraInfo
