package com.appspot.manup.autograph;

import java.util.Enumeration;

import android.content.ContentValues;

import com.appspot.manup.autograph.DataManager.Member;
import com.novell.ldap.LDAPEntry;

public final class UomLdapEntry
{
    private static final String LDAP_MAG_STRIPE = "umanMagStripe";
    private static final String LDAP_STUDENT_ID = "umanPersonID";
    private static final String LDAP_GIVEN_NAME = "givenName";
    private static final String LDAP_SURNAME = "sn";
    private static final String LDAP_EMAIL = "mail";
    private static final String LDAP_DEPARTMENT = "ou";
    private static final String LDAP_STUDENT_TYPE = "employeeType";

    public static UomLdapEntry fromLdapEntry(LDAPEntry ldapEntry)
    {
        return new UomLdapEntry(
                getStringValue(ldapEntry, LDAP_MAG_STRIPE),
                getStringValue(ldapEntry, LDAP_STUDENT_ID),
                getStringValue(ldapEntry, LDAP_GIVEN_NAME),
                getStringValue(ldapEntry, LDAP_SURNAME),
                getStringValue(ldapEntry, LDAP_EMAIL),
                getStringValue(ldapEntry, LDAP_DEPARTMENT),
                getStringValue(ldapEntry, LDAP_STUDENT_TYPE));
    }

    private static String getStringValue(LDAPEntry ldapEntry, String attribute)
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (Enumeration<?> values = ldapEntry.getAttribute(attribute)
                .getStringValues(); values.hasMoreElements();)
        {
            stringBuilder.append(values.nextElement());
            stringBuilder.append(", ");
        }
        return stringBuilder.toString();
    }

    private final String mMagStripe;
    private final String mStudentId;
    private final String mGivenName;
    private final String mSurname;
    private final String mEmail;
    private final String mDepartment;
    private final String mStudentType;

    public UomLdapEntry(String magStripe, String studentId, String givenName, String surname,
            String email, String department, String studentType)
    {
        super();
        mMagStripe = magStripe;
        mStudentId = studentId;
        mGivenName = givenName;
        mSurname = surname;
        mEmail = email;
        mDepartment = department;
        mStudentType = studentType;
    }

    public ContentValues getContentValues()
    {
        ContentValues contentValues = new ContentValues(6);
        contentValues.put(Member.STUDENT_ID, mStudentId);
        contentValues.put(Member.MAG_STRIPE, mMagStripe);
        contentValues.put(Member.GIVEN_NAME, mGivenName);
        contentValues.put(Member.SURNAME, mSurname);
        contentValues.put(Member.EMAIL, mEmail);
        contentValues.put(Member.DEPARTMENT, mDepartment);
        contentValues.put(Member.STUDENT_TYPE, mStudentType);
        return contentValues;
    } // createContentValuesFromLdap

    public String getMagStripe()
    {
        return mMagStripe;
    }

    public String getStudentId()
    {
        return mStudentId;
    }

    public String getGivenName()
    {
        return mGivenName;
    }

    public String getSurname()
    {
        return mSurname;
    }

    public String getEmail()
    {
        return mEmail;
    }

    public String getDepartment()
    {
        return mDepartment;
    }

    public String getStudentType()
    {
        return mStudentType;
    }
}
