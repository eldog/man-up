package com.appspot.manup.signup;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.appspot.manup.signup.WriteSignatureService.WriteCompleteListener;
import com.appspot.manup.signup.ldap.LdapService;
import com.appspot.manup.signup.swipeupclient.SwipeUpService;
import com.appspot.manup.signup.ui.CheckPreferencesActivity;
import com.appspot.manup.signup.ui.DoodleView;

public final class CaptureSignatureActivity extends CheckPreferencesActivity
{
    @SuppressWarnings("unused")
    private static final String TAG = CaptureSignatureActivity.class.getSimpleName();

    public static final String ACTION_CAPTURE =
            CaptureSignatureActivity.class.getName() + ".CAPTURE";
    public static final String EXTRA_ID = CaptureSignatureActivity.class.getName() + ".ID";

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
                    CaptureSignatureActivity.this.finish();
                } // run
            });
        } // onWriteComplete
    };

    private long mId = -1L;
    private DoodleView mSignatureView = null;

    public CaptureSignatureActivity()
    {
        super();
    } // CaptureSignatureActivity

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(mSignatureView = new DoodleView(this));
        mId = getIntent().getLongExtra(EXTRA_ID, mId);
    } // onCreate

    @Override
    protected void onResume()
    {
        super.onResume();
        startService(new Intent(CaptureSignatureActivity.this, LdapService.class));
        startService(new Intent(CaptureSignatureActivity.this, SwipeUpService.class));
    }

    @Override
    protected void onPause()
    {
        stopService(new Intent(CaptureSignatureActivity.this, LdapService.class));
        stopService(new Intent(CaptureSignatureActivity.this, SwipeUpService.class));
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
                startActivity(new Intent(this, AutographPreferenceActivity.class));
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
        WriteSignatureService.writeSignature(this, mWriteListener, mId, mSignatureView.getDoodle());
    } // onSubmit

} // CaptureSignatureActivity

