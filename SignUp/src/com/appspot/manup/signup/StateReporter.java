package com.appspot.manup.signup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class StateReporter
{
    public interface StateListener
    {
        void onStateChange(Object id, int newState);
    } // interface StateListener

    public static final int STATE_UNKNOWN = 0;
    public static final int STATE_STOPPED = 1;
    public static final int STATE_STARTED = 2;

    private static final Map<Object, Integer> sStates = new HashMap<Object, Integer>();
    private static final Map<Object, Set<StateListener>> sListeners =
            new HashMap<Object, Set<StateListener>>();

    public static synchronized int register(final StateListener listener, final Object id)
    {
        Set<StateListener> listeners = sListeners.get(id);
        if (listeners == null)
        {
            sListeners.put(id, listeners = new HashSet<StateListener>());
        } // if
        listeners.add(listener);
        return getState(id);
    } // register(StateListener, id)

    public static synchronized void unregister(final StateListener listener, final Object id)
    {
        final Set<StateListener> listeners = sListeners.get(id);
        listeners.remove(listener);
        if (listeners.size() == 0)
        {
            sListeners.remove(listeners);
        } // if
    } // unregister(StateListener, int)

    public static synchronized void updateState(final Object id, final int newState)
    {
        sStates.put(id, newState);
        final Set<StateListener> listeners = sListeners.get(id);
        if (listeners != null)
        {
            for (final StateListener listener : listeners)
            {
                listener.onStateChange(id, newState);
            } // for
        } // if
    } // updateState(int, int)

    private static int getState(final Object id)
    {
        final Integer state = sStates.get(id);
        return (state != null) ? state : STATE_UNKNOWN;
    } // getState(id)

    private StateReporter()
    {
        super();
        throw new AssertionError();
    } // constructor()

} // class StateReportingService
