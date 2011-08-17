package com.appspot.manup.signup;

import com.appspot.manup.signup.ldap.LdapService;
import com.appspot.manup.signup.swipeupclient.SwipeUpService;

import android.app.Application;
import android.content.Intent;

public final class AutographApplication extends Application
{
    public AutographApplication()
    {
        super();
    } // AutographApplication

    @Override
    public void onCreate()
    {
        super.onCreate();
        startService(new Intent(this, LdapService.class));
        startService(new Intent(this, SwipeUpService.class));
    } // onCreate

} // AutographApplication
