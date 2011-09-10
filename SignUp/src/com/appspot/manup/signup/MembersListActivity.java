package com.appspot.manup.signup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.appspot.manup.signup.data.DataManager;
import com.appspot.manup.signup.data.DataManager.Member;
import com.appspot.manup.signup.data.DataManager.OnChangeListener;
import com.appspot.manup.signup.ui.BaseActivity;
import com.appspot.manup.signup.ui.MemberAdapter;

public final class MembersListActivity extends BaseActivity implements OnChangeListener
{
    @SuppressWarnings("unused")
    private static final String TAG = MembersListActivity.class.getSimpleName();

    private static final String EXTRA_FULLSCREEN = "fullscreen";

    private static final int DIALOGUE_DELETE_ALL_MEMBERS_CONFIRMATION = 0;

    private static final int MENU_SETTINGS = Menu.FIRST;
    private static final int MENU_DELETE_ALL_MEMBERS = Menu.FIRST + 1;
    private static final int MENU_LOAD_TEST_DATA = Menu.FIRST + 2;

    private static final int MENU_GROUP_USER = 0;
    private static final int MENU_GROUP_ADMIN = 1;

    private final class MemberAdder extends AsyncTask<String, Void, Long>
    {
        @Override
        protected Long doInBackground(final String... personId)
        {
            return mDataManager.requestSignature(personId[0]);
        } // doInBackground(Void)

        @Override
        protected void onCancelled()
        {
            cleanUp();
        } // onCancelled()

        @Override
        protected void onPostExecute(final Long id)
        {
            cleanUp();
            if (id != DataManager.OPERATION_FAILED)
            {
                mPersonIdTextEdit.setText(null);
            } // if
            else
            {
                mPersonIdTextEdit.setError(getString(R.string.add_person_id_failed));
            } // else
        } // onPostExecute(Long)

        private void cleanUp()
        {
            mMemberAdder = null;
            mAddButton.setEnabled(true);
        } // cleanUp()

    } // class MemberAdder

    private MemberAdder mMemberAdder = null;
    private ListView mMembersList = null;
    private Button mAddButton = null;
    private EditText mPersonIdTextEdit = null;
    private MemberAdapter mMembersAdapter = null;
    private volatile DataManager mDataManager = null;

    private boolean mIsInAdminMode = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getIntent().getBooleanExtra(EXTRA_FULLSCREEN, true))
        {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } // if
        else
        {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        } // else

        setContentView(R.layout.members_list);
        mDataManager = DataManager.getDataManager(this);
        mMembersList = (ListView) findViewById(R.id.members_list);
        mMembersList.setEmptyView(findViewById(R.id.empy_list));
        mMembersList.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view,
                    final int position, final long id)
            {
                startCaptureSignatureActivity(position);
            } // onItemClick(AdapterView, View, int, long)
        });
        mAddButton = (Button) findViewById(R.id.add_button);
        mPersonIdTextEdit = (EditText) findViewById(R.id.person_id);
        mPersonIdTextEdit.setOnEditorActionListener(new OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(final TextView v, final int actionId,
                    final KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    addMember();
                    return true;
                } // if
                else
                {
                    return false;
                } // else
            } // onEditorAction(TextView, int, KeyEvent)
        });
    } // onCreate(Bundle)

    @Override
    protected void onResume()
    {
        super.onResume();
        DataManager.registerListener(this);
    } // onResume()

    @Override
    protected void onResumeWithPreferences(final Preferences prefs)
    {
        super.onResumeWithPreferences(prefs);
        final boolean isInAdminMode = prefs.isInAdminMode();
        if (isInAdminMode == mIsInAdminMode && mMemberAdder != null)
        {
            return;
        } // if

        // Restart activity to adjust full screen.
        if (mMembersAdapter != null)
        {
            final Intent intent = getIntent();
            intent.putExtra(EXTRA_FULLSCREEN, !isInAdminMode);
            finish();
            startActivity(intent);
            return;
        } // if

        mIsInAdminMode = isInAdminMode;
        if (isInAdminMode)
        {

            mMembersAdapter = new AdminMemberAdapter(this);
        } // if
        else
        {
            mMembersAdapter = new UserMemberAdapter(this);
        } // else
        mMembersList.setAdapter(mMembersAdapter);

        loadData();
    } // setListAdapter()

    private void startCaptureSignatureActivity(final int position)
    {
        final Cursor item = (Cursor) mMembersAdapter.getItem(position);
        final Intent intent = new Intent(MembersListActivity.this,
                CaptureSignatureActivity.class);
        intent.setAction(CaptureSignatureActivity.ACTION_CAPTURE);
        intent.putExtra(CaptureSignatureActivity.EXTRA_ID,
                item.getLong(item.getColumnIndexOrThrow(Member._ID)));
        startActivity(intent);
    } // startCaptureSignatureActivity(int)

    @Override
    protected void onPause()
    {
        DataManager.unregisterListener(this);
        if (mMembersAdapter != null)
        {
            mMembersAdapter.closeCursor();
        } // if
        super.onPause();
    } // onPause()

    private void addMember()
    {
        final String personId = mPersonIdTextEdit.getText().toString();
        if (DataManager.isValidPersonId(personId))
        {
            mAddButton.setEnabled(false);
            if (mMemberAdder != null)
            {
                mMemberAdder.cancel(true);
            } // if
            mMemberAdder = (MemberAdder) new MemberAdder().execute(personId);
        } // if
        else
        {
            mPersonIdTextEdit.setError(getString(R.string.person_id_incorrect_length));
        } // else
    } // addPerson()

    @Override
    public void onBackPressed()
    {
        if (mIsInAdminMode)
        {
            super.onBackPressed();
        } // if
    } // onBackPressed()

    @Override
    protected Dialog onCreateDialog(final int id, final Bundle args)
    {
        switch (id)
        {
            case DIALOGUE_DELETE_ALL_MEMBERS_CONFIRMATION:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_delete_all_members_title)
                        .setMessage(R.string.dialog_delete_all_members_message)
                        .setCancelable(true)
                        .setIcon(android.R.drawable.stat_sys_warning)
                        .setNegativeButton(R.string.dialog_delete_all_members_no,
                                null /* listener */)
                        .setPositiveButton(R.string.dialog_delete_all_members_yes,
                                new OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        mDataManager.deleteAllMembers();
                                        loadData();
                                    } // onClick(DialogInterface, int)
                                })
                        .create();
            default:
                throw new AssertionError();
        } // switch
    } // onCreateDialog(int, Bundle)

    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        menu.add(MENU_GROUP_USER, MENU_SETTINGS, Menu.NONE, R.string.menu_settings);
        menu.add(MENU_GROUP_ADMIN, MENU_DELETE_ALL_MEMBERS, Menu.NONE,
                R.string.menu_delete_all_members);
        menu.add(MENU_GROUP_ADMIN, MENU_LOAD_TEST_DATA, Menu.NONE, R.string.menu_load_test_data);
        return true;
    } // onCreateOptionsMenu(Menu)

    @Override
    public boolean onMenuOpened(final int featureId, final Menu menu)
    {
        menu.setGroupVisible(MENU_GROUP_ADMIN, mIsInAdminMode);
        return true;
    } // onMenuOpened(int, Menu)

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        switch (item.getItemId())
        {
            case MENU_SETTINGS:
                startActivity(new Intent(this, SignUpPreferenceActivity.class));
                return true;
            case MENU_DELETE_ALL_MEMBERS:
                showDialog(DIALOGUE_DELETE_ALL_MEMBERS_CONFIRMATION, null /* args */);
                return true;
            case MENU_LOAD_TEST_DATA:
                mDataManager.loadTestData();
                loadData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        } // switch
    } // onOptionsItemSelected(MenuItem)

    public void onAddButtonClick(final View addButton)
    {
        addMember();
    } // onAddClick(View)

    @Override
    public void onChange(final DataManager dataManager)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                loadData();
            } // run()
        });
    } // onChange(DataManager)

    private void loadData()
    {
        mMembersAdapter.loadCursor();
    } // loadData()

} // class MembersListActivity
