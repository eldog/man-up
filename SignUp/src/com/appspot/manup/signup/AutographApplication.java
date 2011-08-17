package com.appspot.manup.signup;

import android.app.Application;
import android.content.Intent;

public final class AutographApplication extends Application
{
    public static final String ACTION_LDAP = AutographApplication.class.getName() + ".LDAP";
    public static final String ACTION_SWIPE = AutographApplication.class.getName() + ".SWIPE";
    public AutographApplication()
    {
        super();
    } // AutographApplication

    @Override
    public void onCreate()
    {
        super.onCreate();
        Intent i  = new Intent(ACTION_LDAP);
        LdapSwipeService.startServiceAction(this, i);
        i = new Intent(ACTION_SWIPE);
        LdapSwipeService.startServiceAction(this, i);
    } // onCreate

} // AutographApplication
