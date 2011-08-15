package com.appspot.manup.autograph;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.appspot.manup.autograph.WriteSignatureService.WriteCompleteListener;

public final class CaptureSignatureActivity extends CheckPreferencesActivity
{
    @SuppressWarnings("unused")
    private static final String TAG = CaptureSignatureActivity.class.getSimpleName();

    private static final int MENU_SUBMIT = Menu.FIRST;
    private static final int MENU_CLEAR = Menu.FIRST + 1;
    private static final int MENU_SETTINGS = Menu.FIRST + 2;

    private final WriteCompleteListener mWriteListener = new WriteCompleteListener()
    {
        @Override
        public void onWriteComplete(Intent intent)
        {
            final long id = intent.getLongExtra(WriteSignatureService.EXTRA_ID, -1);
            final boolean successful =
                    intent.getBooleanExtra(WriteSignatureService.EXTRA_SUCCESSFUL, false);
            final String s = id + ": " + ((successful) ? "Successfully written" : "Write failed");
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast.makeText(CaptureSignatureActivity.this, s, Toast.LENGTH_SHORT).show();
                    mSignatureView.clear();
                } // run
            });
        } // onWriteComplete
    };

    private DoodleView mSignatureView = null;

    // TODO: Replace fake student ID generation.
    private long id = System.currentTimeMillis();

    public CaptureSignatureActivity()
    {
        super();
    } // CaptureSignatureActivity

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(mSignatureView = new DoodleView(this));
    } // onCreate

    @Override
    protected void onResume()
    {
        super.onResume();
        startService(new Intent(CaptureSignatureActivity.this, LdapService.class));
        startService(new Intent(CaptureSignatureActivity.this, SwipeServerService.class));
    }

    @Override
    protected void onPause()
    {
        stopService(new Intent(CaptureSignatureActivity.this, LdapService.class));
        stopService(new Intent(CaptureSignatureActivity.this, SwipeServerService.class));
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_SUBMIT, 0, "Submit").setShortcut('7', 's');
        menu.add(0, MENU_CLEAR, 0, "Clear").setShortcut('3', 'c');
        menu.add(0, MENU_SETTINGS, 0, "Settings");
        return true;
    } // onCreateOptionsMenu

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        switch (item.getItemId())
        {
            case MENU_SUBMIT:
                onSubmit();
                return true;
            case MENU_CLEAR:
                mSignatureView.clear();
                return true;
            case MENU_SETTINGS:
                startActivity(new Intent(this, SignaturePreferenceActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        } // switch
    } // onOptionsItemSelected

    private void onSubmit()
    {
        if (mSignatureView.isClear())
        {
            Toast.makeText(this, "A signature is required", Toast.LENGTH_SHORT).show();
            return;
        } // if
        WriteSignatureService.writeSignature(this, mWriteListener, id, mSignatureView.getDoodle());
    } // onSubmit

} // CaptureSignatureActivity

