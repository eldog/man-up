package com.appspot.manup.signup;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
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
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.appspot.manup.signup.data.DataManager;
import com.appspot.manup.signup.data.DataManager.Member;
import com.appspot.manup.signup.data.DataManager.OnChangeListener;
import com.appspot.manup.signup.extrainfo.ExtraInfoService;
import com.appspot.manup.signup.swipeupclient.SwipeUpClientService;
import com.appspot.manup.signup.ui.BaseActivity;
import com.appspot.manup.signup.ui.MemberAdapter;

public final class MembersListActivity extends BaseActivity implements OnChangeListener
{
    @SuppressWarnings("unused")
    private static final String TAG = MembersListActivity.class.getSimpleName();

    private static final int DIALOGUE_DELETE_ALL_MEMBERS_CONFIRMATION = 0;

    private static final int MENU_SETTINGS = Menu.FIRST;
    private static final int MENU_UPLOAD = Menu.FIRST + 1;
    private static final int MENU_DELETE_ALL_MEMBERS = Menu.FIRST + 2;
    private static final int MENU_LOAD_TEST_DATA = Menu.FIRST + 3;

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

    private final AbstractStateListener mListener = new AbstractStateListener(ExtraInfoService.ID,
            SwipeUpClientService.ID)
    {
        @Override
        void onStateChange(final Map<Object, Integer> states)
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    updateServiceState(states);
                } // run
            });
        } // onStateChange(Map)
    };

    private MemberAdder mMemberAdder = null;
    private ListView mMembersList = null;
    private Button mAddButton = null;
    private EditText mPersonIdTextEdit = null;
    private TextView mServiceState = null;
    private TextView mIpAddress = null;
    private LinearLayout mAdminInfo = null;
    private MemberAdapter mMembersAdapter = null;
    private volatile DataManager mDataManager = null;

    private boolean mIsInAdminMode = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
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
        mPersonIdTextEdit.setOnFocusChangeListener(new OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(final View personIdTextEdit, final boolean hasFocus)
            {
                if (!hasFocus)
                {
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(mPersonIdTextEdit.getWindowToken(),
                                    0 /* flags */);
                } // if
            } // onFocusChange(View, boolean)
        });
        mAdminInfo = (LinearLayout) findViewById(R.id.admin_info);
        mServiceState = (TextView) findViewById(R.id.service_state);
        mIpAddress = (TextView) findViewById(R.id.ip_address);
    } // onCreate(Bundle)

    @Override
    protected void onResume()
    {
        super.onResume();
        DataManager.registerListener(this);
        mListener.register();
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

        mAdminInfo.setVisibility(mIsInAdminMode ? View.VISIBLE : View.GONE);

        String ip;
        try
        {
            final InetAddress ipAddress = NetworkUtils.getLocalIpAddress();
            if (ipAddress != null)
            {
                ip = "IP: " + ipAddress.getHostAddress();
            } // if
            else
            {
                ip = "IP: None";
            }
        }
        catch (SocketException e)
        {
            ip = "IP: None";
        }

        mIpAddress.setText(ip);

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

    private void updateServiceState(final Map<Object, Integer> states)
    {
        final String[] stateStrings =
                getResources().getStringArray(R.array.service_state);

        mServiceState.setText(new StringBuilder()
                .append(getString(R.string.extra_info))
                .append(": ")
                .append(stateStrings[states.get(ExtraInfoService.ID)])
                .append(" | ")
                .append(getString(R.string.swipe_up))
                .append(": ")
                .append(stateStrings[states.get(SwipeUpClientService.ID)]));
    } // updateServiceState(int)

    @Override
    protected void onPause()
    {
        mListener.unregister();
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
                        .setNegativeButton(R.string.no, null /* listener */)
                        .setPositiveButton(R.string.yes,
                                new OnClickListener()
                                {
                                    @Override
                                    public void onClick(final DialogInterface dialog,
                                            final int which)
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
        menu.add(MENU_GROUP_ADMIN, MENU_UPLOAD, Menu.NONE, R.string.menu_upload);
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
            case MENU_UPLOAD:
                startService(new Intent(this, UploadService.class));
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

    private void loadData()
    {
        mMembersAdapter.loadCursor();
    } // loadData()

    @Override
    public void onChange()
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

} // class MembersListActivity
