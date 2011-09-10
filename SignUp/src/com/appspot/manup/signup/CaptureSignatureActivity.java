package com.appspot.manup.signup;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.appspot.manup.signup.data.DataManager;
import com.appspot.manup.signup.data.DataManager.OnChangeListener;
import com.appspot.manup.signup.ui.BaseActivity;
import com.appspot.manup.signup.ui.DoodleView;

public final class CaptureSignatureActivity extends BaseActivity implements
        OnChangeListener
{
    @SuppressWarnings("unused")
    private static final String TAG = CaptureSignatureActivity.class.getSimpleName();

    public static final String ACTION_CAPTURE =
            CaptureSignatureActivity.class.getName() + ".CAPTURE";
    public static final String EXTRA_ID = CaptureSignatureActivity.class.getName() + ".ID";

    private static final int MENU_SUBMIT = Menu.FIRST;
    private static final int MENU_CLEAR = Menu.FIRST + 1;
    private static final int MENU_SETTINGS = Menu.FIRST + 2;

    private long mId = Long.MIN_VALUE;
    private DoodleView mSignatureView = null;

    public CaptureSignatureActivity()
    {
        super();
    } // CaptureSignatureActivity

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.capture_signature);
        mSignatureView = (DoodleView) findViewById(R.id.signature);
        final Intent intent = getIntent();
        mId = intent.getLongExtra(EXTRA_ID, mId);
    } // onCreate

    @Override
    protected void onResume()
    {
        super.onResume();
        DataManager.registerListener(this);
    } // onResume

    @Override
    protected void onPause()
    {
        DataManager.unregisterListener(this);
        super.onPause();
    } // onPause

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
                startActivity(new Intent(this, SignUpPreferenceActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        } // switch
    } // onOptionsItemSelected

    private void onSubmit()
    {
        if (mSignatureView.isClear())
        {
            Toast.makeText(this, "Please sign.", Toast.LENGTH_SHORT).show();
            return;
        } // if
        WriteSignatureService.writeSignature(this, mId, mSignatureView.getDoodle());
    } // onSubmit

    @Override
    public void onChange(final DataManager dataManager)
    {
        if (dataManager.memberHasSignature(mId))
        {
            finish();
        } // if
    } // onChange(DataManager)

} // class CaptureSignatureActivity

