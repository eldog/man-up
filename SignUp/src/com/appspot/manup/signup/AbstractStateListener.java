package com.appspot.manup.signup;

import java.util.HashMap;
import java.util.Map;

import com.appspot.manup.signup.StateReporter.StateListener;

public abstract class AbstractStateListener implements StateListener
{
    private final Object[] mIds;
    private final Map<Object, Integer> mStates = new HashMap<Object, Integer>();

    public AbstractStateListener(Object... ids)
    {
        mIds = ids;
    }

    public void register()
    {
        for (final Object  id: mIds)
        {
            mStates.put(id, StateReporter.register(this, id));
        }
        onStateChange();
    }

    public void unregister()
    {
        for (final Object  id: mIds)
        {
            StateReporter.unregister(this, id);
            mStates.put(id, StateReporter.STATE_UNKNOWN);
        }
    }

    @Override
    public final void onStateChange(final Object id, final int newState)
    {
        mStates.put(id, newState);
        onStateChange();
    }

    public Map<Object, Integer> getStates()
    {
        return mStates;
    }

    private void onStateChange()
    {
        onStateChange(mStates);
    }

    abstract void onStateChange(final Map<Object, Integer> states);
}
