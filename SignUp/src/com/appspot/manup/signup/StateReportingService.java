package com.appspot.manup.signup;

import android.app.Service;

public abstract class StateReportingService extends Service
{
    public static int STATE_STOPPED = StateReporter.STATE_STOPPED;
    public static int STATE_STARTED = StateReporter.STATE_STARTED;

    protected void setState(final int state)
    {
        StateReporter.updateState(getId(), state);
    } // setState(int)

    public abstract Object getId();

} // class StateReportingService
