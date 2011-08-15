package com.appspot.manup.autograph;

import java.util.Enumeration;

import android.content.ContentValues;

import com.appspot.manup.autograph.DataManager.Member;
import com.novell.ldap.LDAPEntry;

public final class UomLdapEntry
{
    private static final String LDAP_STUDENT_ID = "umanPersonID";
    private static final String LDAP_GIVEN_NAME = "givenName";
    private static final String LDAP_SURNAME = "sn";
    private static final String LDAP_EMAIL = "mail";
    private static final String LDAP_DEPARTMENT = "ou";
    private static final String LDAP_STUDENT_TYPE = "employeeType";

    private static final String VALUE_SEPERATER = ", ";

    public static UomLdapEntry fromLdapEntry(final LDAPEntry ldapEntry)
    {
        return new UomLdapEntry(
                getStringValue(ldapEntry, LDAP_STUDENT_ID),
                getStringValue(ldapEntry, LDAP_GIVEN_NAME),
                getStringValue(ldapEntry, LDAP_SURNAME),
                getStringValue(ldapEntry, LDAP_EMAIL),
                getStringValue(ldapEntry, LDAP_DEPARTMENT),
                getStringValue(ldapEntry, LDAP_STUDENT_TYPE));
    } // fromLdapEntry

    private static String getStringValue(final LDAPEntry ldapEntry, final String attribute)
    {
        final StringBuilder valueBuilder = new StringBuilder();
        for (Enumeration<?> values = ldapEntry.getAttribute(attribute)
                .getStringValues(); values.hasMoreElements();)
        {
            valueBuilder.append(values.nextElement());
            valueBuilder.append(VALUE_SEPERATER);
        } // for
        return valueBuilder.toString();
    } // getStringValue

    private final String mStudentId;
    private final String mGivenName;
    private final String mSurname;
    private final String mEmail;
    private final String mDepartment;
    private final String mStudentType;

    public UomLdapEntry(final String studentId, final String givenName, final String surname,
            final String email, final String department, final String studentType)
    {
        super();
        mStudentId = studentId;
        mGivenName = givenName;
        mSurname = surname;
        mEmail = email;
        mDepartment = department;
        mStudentType = studentType;
    }

    public ContentValues getContentValues()
    {
        final ContentValues contentValues = new ContentValues(6);
        contentValues.put(Member.STUDENT_ID, mStudentId);
        contentValues.put(Member.GIVEN_NAME, mGivenName);
        contentValues.put(Member.SURNAME, mSurname);
        contentValues.put(Member.EMAIL, mEmail);
        contentValues.put(Member.DEPARTMENT, mDepartment);
        contentValues.put(Member.STUDENT_TYPE, mStudentType);
        return contentValues;
    } // createContentValuesFromLdap

    public String getStudentId()
    {
        return mStudentId;
    } // getStudentId

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

    public String getStudentType()
    {
        return mStudentType;
    } // getStudentType

} // UomLdapEntry
