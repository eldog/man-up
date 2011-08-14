package com.appspot.manup.autograph;

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
        startService(new Intent(this, SwipeServerService.class));
    } // onCreate

} // AutographApplication
