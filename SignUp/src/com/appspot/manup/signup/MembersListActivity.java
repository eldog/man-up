package com.appspot.manup.signup;

import com.appspot.manup.signup.data.CursorLoader;
import com.appspot.manup.signup.data.DataManager;
import com.appspot.manup.signup.data.DataManager.Member;
import com.appspot.manup.signup.ui.CheckPreferencesActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public final class MembersListActivity extends CheckPreferencesActivity
{
    @SuppressWarnings("unused")
    private static final String TAG = MembersListActivity.class.getSimpleName();

    private static final int MENU_SETTINGS = Menu.FIRST;

    private final class MemeberAdder extends AsyncTask<String, Void, Long>
    {
        @Override
        protected Long doInBackground(final String... studentId)
        {
            return DataManager.getInstance(
                    MembersListActivity.this).addMember(studentId[0]);
        } // doInBackground

        @Override
        protected void onCancelled()
        {
            onStop();
        } // onCancelled

        @Override
        protected void onPostExecute(final Long id)
        {
            onStop();
            if (id != -1)
            {
                startUncapturedSignatureLoader();
            } // if
        } // onPostExecute

        private void onStop()
        {
           mMemberAdder = null;
           mAdd.setEnabled(true);
        } // onStop

    } // MemeberAdder

    private final class UncapturedSignaturesLoader extends CursorLoader
    {
        @Override
        protected Cursor loadCursor()
        {
            return DataManager.getInstance(MembersListActivity.this)
                    .getMembersWithUncapturedSignatures();
        } // loadCursor

        @Override
        protected void onCursorLoaded(final Cursor uncapturedSignatures)
        {
            mUncapturedSignatureAdapter.changeCursor(uncapturedSignatures);
        } // onCursorLoaded

    } // UncapturedSignaturesLoader

    private Button mAdd = null;
    private EditText mStudentId = null;

    private SimpleCursorAdapter mUncapturedSignatureAdapter = null;
    private MemeberAdder mMemberAdder = null;
    private UncapturedSignaturesLoader mUncapturedSignaturesLoader = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.autograph_list);
        mAdd = (Button) findViewById(R.id.add_button);
        mStudentId = (EditText) findViewById(R.id.student_id);
        mUncapturedSignatureAdapter = new SimpleCursorAdapter(
                this,
                R.layout.autograph_list_item,
                null,
                new String[] { Member.STUDENT_ID },
                new int[] { R.id.student_id });
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
            } // onItemClick
        });
    } // onCreate

    @Override
    protected void onResume()
    {
        super.onResume();
        startUncapturedSignatureLoader();
    } // onResume

    private void startUncapturedSignatureLoader()
    {
        if (mUncapturedSignaturesLoader != null)
        {
            mUncapturedSignaturesLoader.cancel(true);
        } // if
        mUncapturedSignaturesLoader = (UncapturedSignaturesLoader)
                new UncapturedSignaturesLoader().execute();
    } // startUncapturedSignatureLoader

    @Override
    protected void onPause()
    {
        if (mUncapturedSignaturesLoader != null)
        {
            mUncapturedSignaturesLoader.cancel(true);
            mUncapturedSignaturesLoader = null;
        } // if
        if (mMemberAdder != null)
        {
            mMemberAdder.cancel(true);
            mMemberAdder = null;
        } // if
        if (mUncapturedSignatureAdapter != null)
        {
            final Cursor c = mUncapturedSignatureAdapter.getCursor();
            if (c != null)
            {
                c.close();
            } // if
        } // if
        super.onPause();
    } // onPause

    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_SETTINGS, 0, "Settings");
        return true;
    } // onCreateOptionsMenu

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        switch (item.getItemId())
        {
            case MENU_SETTINGS:
                startActivity(new Intent(this, AutographPreferenceActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        } // switch
    } // onOptionsItemSelected

    public void onAddClick(final View v)
    {
        final String studentId = mStudentId.getText().toString();
        if (!TextUtils.isEmpty(studentId))
        {
            new MemeberAdder().execute(studentId);
            mAdd.setEnabled(false);
        } // if
    } // onAddClick

} // AutographListActivity
