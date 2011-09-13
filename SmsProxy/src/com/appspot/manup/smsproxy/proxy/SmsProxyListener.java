package com.appspot.manup.smsproxy.proxy;

public interface SmsProxyListener
{
    void onRegister(boolean isRunning);

    void onStart();

    void onMessage(String message);

    void onStop();
} // SmsProxyListener
