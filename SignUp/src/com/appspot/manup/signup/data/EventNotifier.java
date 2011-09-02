package com.appspot.manup.signup.data;

import java.util.HashSet;
import java.util.Set;

public class EventNotifier<T>
{
    private final Set<EventListener<T>> mListeners = new HashSet<EventListener<T>>();

    public EventNotifier()
    {
        super();
    } // constructor

    public void notifyListeners(final T obj)
    {
        synchronized (mListeners)
        {
            for (final EventListener<T> listener : mListeners)
            {
                listener.onEvent(obj);
            } // for
        } // synchronized

    } // notifyListeners

    public void registerListener(final EventListener<T> listener)
    {
        synchronized (mListeners)
        {
            mListeners.add(listener);
        } // synchronized
    } // registerListener

    public void unregisterListener(final EventListener<T> listener)
    {
        synchronized (mListeners)
        {
            mListeners.remove(listener);
        } // synchronized
    } // unregisterListener

} // class EventNotifier
