package com.appspot.manup.signup;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.appspot.manup.signup.data.DataManager;
import com.appspot.manup.signup.data.DataManager.Member;
import com.appspot.manup.signup.data.DataManager.OnChangeListener;
import com.appspot.manup.signup.ui.CheckPreferencesActivity;

public final class MembersListActivity extends CheckPreferencesActivity implements OnChangeListener
{
    private static final String TAG = MembersListActivity.class.getSimpleName();

    private static final int MENU_SETTINGS = Menu.FIRST;
    private static final int MENU_UPLOAD_SIGNATURES = Menu.FIRST + 1;
    private static final int MENU_FLUSH_MEMBERS = Menu.FIRST + 2;
    private static final int MENU_LOAD_TEST_DATA = Menu.FIRST + 3;

    private final class DataLoader extends AsyncTask<Void, Void, Void>
    {
        private volatile Cursor c1 = null;// , c2 = null;

        @Override
        protected Void doInBackground(Void... params)
        {
            c1 = mDataManager.getMembersWithoutSignatures();
            // c2 = mDataManager.getSignatureRequests(mResumedAtUnixTime);
            Log.d(TAG, "They've been loaded.");
            if (isCancelled())
            {
                c1.close();
                // c2.close();
            } // if
            return null;
        }

        @Override
        protected void onCancelled()
        {
            if (c1 != null)
                c1.close();
            /*
             * if (c2 != null) c2.close();
             */Log.d(TAG, "Closed them.");
        }

        @Override
        protected void onPostExecute(Void result)
        {
            mUncapturedSignatureAdapter.changeCursor(c1);
            /*
             * if (c2 == null) { return; } // if
             *
             * Log.d(TAG, "Closing in onCursorLoaded " + c2); c2.close();
             */
        }
    }

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
        } // onPostExecute(Long)

        private void cleanUp()
        {
            mAdd.setEnabled(true);
        } // cleanUp()

    } // class MemberAdder

    private Button mAdd = null;
    private EditText mPersonId = null;
    private DataLoader mLoader = null;
    private SimpleCursorAdapter mUncapturedSignatureAdapter = null;

    private volatile DataManager mDataManager = null;
    //private volatile long mResumedAtUnixTime = Long.MIN_VALUE;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.members_list);
        mDataManager = DataManager.getDataManager(this);
        mAdd = (Button) findViewById(R.id.add_button);
        mPersonId = (EditText) findViewById(R.id.person_id);
        mUncapturedSignatureAdapter = new SimpleCursorAdapter(
                this,
                R.layout.member_list_item,
                null /* cursor */,
                new String[] { Member.PERSON_ID, Member.GIVEN_NAME, Member.SURNAME,
                        Member.EXTRA_INFO_STATE },
                new int[] { R.id.person_id, R.id.given_name, R.id.surname, R.id.extra_info_state });
        final ListView uncapturedSignaturesList =
                (ListView) findViewById(R.id.members_with_uncaptured_signatures_list);
        uncapturedSignaturesList.setAdapter(mUncapturedSignatureAdapter);
        uncapturedSignaturesList.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view,
                    final int position, final long id)
            {
                final Cursor c = (Cursor) mUncapturedSignatureAdapter.getItem(position);
                final Intent intent = new Intent(MembersListActivity.this,
                        CaptureSignatureActivity.class);
                intent.setAction(CaptureSignatureActivity.ACTION_CAPTURE);
                intent.putExtra(CaptureSignatureActivity.EXTRA_ID,
                        c.getLong(c.getColumnIndexOrThrow(Member._ID)));
                startActivity(intent);
            } // onItemClick(AdapterView, View, int, long)
        });
    } // onCreate(Bundle)

    @Override
    protected void onResume()
    {
        super.onResume();
     //   mResumedAtUnixTime = DataManager.getUnixTime();
        mDataManager.registerListener(this);
        loadData();
    } // onResume()

    private void loadData()
    {
        Log.d(TAG, "Loading data. " + Thread.currentThread().getName());
        if (mLoader != null)
        {
            mLoader.cancel(true);
        } // if
        mLoader = (DataLoader) new DataLoader().execute();
    } // loadData()

    @Override
    protected void onPause()
    {
        mDataManager.unregisterListener(this);
        if (mLoader != null)
        {
            mLoader.cancel(true);
        } // if
        final Cursor cursor = mUncapturedSignatureAdapter.getCursor();
        if (cursor != null)
        {
            Log.d(TAG, "Closing cursor " + cursor);
            cursor.close();
        } // if
        super.onPause();
    } // onPause()

    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_SETTINGS, 0, "Settings");
        menu.add(0, MENU_UPLOAD_SIGNATURES, 0, "Upload signatures");
        menu.add(0, MENU_FLUSH_MEMBERS, 0, "Delete members");
        menu.add(0, MENU_LOAD_TEST_DATA, 0, "Load test data");
        return true;
    } // onCreateOptionsMenu(Menu)

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
            new MemberAdder().execute(personId);
        } // if
    } // onAddClick(View)

    @Override
    public void onChange(final DataManager dataManager)
    {
        Log.d(TAG, "onChange called.");
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
