package com.appspot.manup.smsproxy;

import java.util.LinkedList;
import java.util.List;

import com.appspot.manup.smsproxy.proxy.SmsProxyListener;
import com.appspot.manup.smsproxy.proxy.SmsProxyManager;

import com.appspot.manup.smsproxy.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public final class ControlProxyActivity extends Activity
{
    @SuppressWarnings("unused")
    private static final String TAG = ControlProxyActivity.class.getSimpleName();

    private List<String> mLog = null;
    private ArrayAdapter<String> mLogAdapter = null;

    private Button mToggleServiceButton;
    private ListView mLogView;

    private final SmsProxyListener mListener = new SmsProxyListener()
    {
        @Override
        public void onRegister(final boolean isRunning)
        {
            setRunning(isRunning);
        } // onRegister

        @Override
        public void onStart()
        {
            setRunning(true);
        } // onStart

        @Override
        public void onMessage(final String message)
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    mLog.add(message);
                    mLogAdapter.notifyDataSetChanged();
                } // run
            });
        } // onMessage

        @Override
        public void onStop()
        {
            setRunning(false);
        } // onStop
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_proxy_activity);
        mToggleServiceButton = (Button) findViewById(R.id.toggle_server);

        mLog = getLastLog();
        if (mLog == null)
        {
            mLog = new LinkedList<String>();
        } // if

        mLogView = (ListView) findViewById(R.id.logTable);
        mLogAdapter = new ArrayAdapter<String>(this, R.layout.log_list_item, mLog);
        mLogView.setAdapter(mLogAdapter);

        SmsProxyManager.registerListener(mListener);
    } // onCreate

    @SuppressWarnings("unchecked")
    private List<String> getLastLog()
    {
        return (List<String>) getLastNonConfigurationInstance();
    } // getLastLog

    private void setRunning(final boolean running)
    {
        mToggleServiceButton.setText(getString(
                running ? R.string.stop_server : R.string.start_server));
    } // setRunning

    public void onToggleServiceClick(final View button)
    {
        SmsProxyManager.toggleSmsProxy(this);
    } // onToggleServiceClick

    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    } // onCreateOptionsMenu

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.settings:
                startActivity(new Intent(this, SmsProxyPreferencesActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        } // switch
    } // onOptionsItemSelected

    @Override
    public Object onRetainNonConfigurationInstance()
    {
        return mLog;
    } // onRetainNonConfigurationInstance

    @Override
    protected void onDestroy()
    {
        SmsProxyManager.unregisterListener(mListener);
        super.onDestroy();
    } // onDestroy

} // ControlProxyActivity
