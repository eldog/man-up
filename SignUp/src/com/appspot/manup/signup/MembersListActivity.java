package com.appspot.manup.signup;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

    private static final int MENU_SETTINGS = Menu.FIRST;
    private static final int MENU_UPLOAD_SIGNATURES = Menu.FIRST + 1;
    private static final int MENU_FLUSH_MEMBERS = Menu.FIRST + 2;
    private static final int MENU_LOAD_TEST_DATA = Menu.FIRST + 3;

    private static final int MENU_GROUP_USER = 0;
    private static final int MENU_GROUP_ADMIN = 1;

    private final class MemberAdder extends AsyncTask<String, Void, Long>
    {
        @Override
        protected Long doInBackground(final String... personId)
        {
            return mDataManager.addMember(personId[0]);
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
                mPersonId.setText(null);
            } // if
            else
            {
                /*
                 * Assume that if the operations failed it was because the
                 * person ID already exists.
                 */
                mPersonId.setError(getString(R.string.person_id_already_exists));
            } // else
        } // onPostExecute(Long)

        private void cleanUp()
        {
            mMemberAdder = null;
            mAdd.setEnabled(true);
        } // cleanUp()

    } // class MemberAdder

    private MemberAdder mMemberAdder = null;
    private Button mAdd = null;
    private EditText mPersonId = null;
    private MemberAdapter mMemberAdapter = null;
    private volatile DataManager mDataManager = null;

    private boolean mUsingAdminMemberAdapter = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.members_list);
        mDataManager = DataManager.getDataManager(this);
        mAdd = (Button) findViewById(R.id.add_button);
        mPersonId = (EditText) findViewById(R.id.person_id);
        mPersonId.setOnEditorActionListener(new OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(final TextView v, final int actionId,
                    final KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    onAddClick(null);
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
    protected void onResumeWithPreferences(final Preferences prefs)
    {
        super.onResumeWithPreferences(prefs);

        if (mMemberAdapter != null)
        {
            mMemberAdapter.closeCursor();
        } // if

        if (mUsingAdminMemberAdapter = prefs.isInAdminMode())
        {
            mMemberAdapter = new AdminMemberAdapter(this);
        } // if
        else
        {
            mMemberAdapter = new UserMemberAdapter(this);
        } // else

        final ListView membersList =
                (ListView) findViewById(R.id.members_with_uncaptured_signatures_list);
        membersList.setAdapter(mMemberAdapter);
        membersList.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view,
                    final int position, final long id)
            {
                final Object item = mMemberAdapter.getItem(position);
                if (item instanceof Cursor)
                {
                    final Cursor c = (Cursor) item;
                    final Intent intent = new Intent(MembersListActivity.this,
                            CaptureSignatureActivity.class);
                    intent.setAction(CaptureSignatureActivity.ACTION_CAPTURE);
                    intent.putExtra(CaptureSignatureActivity.EXTRA_ID,
                            c.getLong(c.getColumnIndexOrThrow(Member._ID)));
                    startActivity(intent);
                } // if
            } // onItemClick(AdapterView, View, int, long)
        });
        loadData();
    } // setListAdapter()

    @Override
    protected void onResume()
    {
        super.onResume();
        DataManager.registerListener(this);
    } // onResume()

    @Override
    protected void onPause()
    {
        DataManager.unregisterListener(this);
        mMemberAdapter.closeCursor();
        super.onPause();
    } // onPause()

    @Override
    public void onBackPressed()
    {
        // Prevent users, not admins, of accidently exiting SignUp.
        if (mUsingAdminMemberAdapter)
        {
            super.onBackPressed();
        } // if
    } // onBackPressed()

    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        menu.add(MENU_GROUP_USER, MENU_SETTINGS, 0, "Settings");
        menu.add(MENU_GROUP_ADMIN, MENU_UPLOAD_SIGNATURES, 0, "Upload signatures");
        menu.add(MENU_GROUP_ADMIN, MENU_FLUSH_MEMBERS, 0, "Delete members");
        menu.add(MENU_GROUP_ADMIN, MENU_LOAD_TEST_DATA, 0, "Load test data");
        return true;
    } // onCreateOptionsMenu(Menu)

    @Override
    public boolean onMenuOpened(final int featureId, final Menu menu)
    {
        menu.setGroupVisible(MENU_GROUP_ADMIN, mUsingAdminMemberAdapter);
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
            case MENU_UPLOAD_SIGNATURES:
                startService(new Intent(this, UploadService.class));
                return true;
            case MENU_FLUSH_MEMBERS:
                mDataManager.flushMembers();
                loadData();
                return true;
            case MENU_LOAD_TEST_DATA:
                mDataManager.loadTestData();
                loadData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        } // switch
    } // onOptionsItemSelected(MenuItem)

    public void onAddClick(final View v)
    {
        final String personId = mPersonId.getText().toString();
        if (DataManager.isValidPersonId(personId))
        {
            mAdd.setEnabled(false);
            if (mMemberAdder != null)
            {
                mMemberAdder.cancel(true);
            } // if
            mMemberAdder = (MemberAdder) new MemberAdder().execute(personId);
        } // if
        else
        {
            mPersonId.setError(getString(R.string.person_id_incorrect_length));
        } // else
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
        mMemberAdapter.loadCursor();
    } // loadData()

} // class MembersListActivity
