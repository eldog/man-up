package uk.ac.manchester.cs.mlo.smsproxy.proxy;

public interface SmsProxyListener
{
    void onRegister(boolean isRunning);

    void onStart();

    void onMessage(String message);

    void onStop();
} // SmsProxyListener
