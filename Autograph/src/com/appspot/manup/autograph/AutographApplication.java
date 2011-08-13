package com.appspot.manup.autograph;

import android.app.Application;
import android.content.Intent;

public class AutographApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        startService(new Intent(AutographApplication.this, LdapService.class));
        startService(new Intent(AutographApplication.this, SwipeServerService.class));
    } // onCreate
} // AutographApplication
