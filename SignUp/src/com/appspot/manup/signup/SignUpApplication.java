package com.appspot.manup.signup;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import com.appspot.manup.signup.ldap.LdapService;
import com.appspot.manup.signup.swipeupclient.SwipeUpClientService;

public final class SignUpApplication extends Application
{
    @SuppressWarnings("unused")
    private static final String TAG = SignUpApplication.class.getSimpleName();

    private final class SignUpInitialiser extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(final Void... noParams)
        {
            final Preferences prefs = new Preferences(SignUpApplication.this);
            final Context context = SignUpApplication.this;
            LdapService.controlService(context, prefs);
            SwipeUpClientService.controlService(context, prefs);
            return null;
        } // doInBackground(Void)

    } // class CheckServicePrefs

    public SignUpApplication()
    {
        super();
    } // constructor()

    @Override
    public void onCreate()
    {
        super.onCreate();
        new SignUpInitialiser().execute();
    } // onCreate()

} // class SignUpApplication
