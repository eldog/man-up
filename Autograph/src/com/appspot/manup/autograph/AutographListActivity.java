package com.appspot.manup.autograph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.manup.autograph.SignatureDatabase.OnSignatureAddedListener;

public class AutographListActivity extends CheckPreferencesActivity
{
    @SuppressWarnings("unused")
    private static final String TAG = AutographListActivity.class.getSimpleName();

    public static final String EXTRA_ID = AutographListActivity.class.getName() + ".ID";
    private static final String ACTION_CAPTURE = AutographListActivity.class.getName() + ".CAPTURE";
    private static final String MAGSTRIPE_HINT = "Enter mag stripe number";
    private static final int MAGSTRIPE_HEADER_WEIGHT = 1;
    private static final int MAGSTRIPE_TEXT_ID = 1;
    private ListView autographList;
    private Map<String, Long> magStripes;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        final LinearLayout layout = new LinearLayout(this);
        final LayoutParams lp = new LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        layout.setLayoutParams(lp);
        setContentView(layout);

        autographList = new ListView(this);
        autographList.setLayoutParams(lp);

        final LinearLayout listHeader = new LinearLayout(this);
        listHeader.setLayoutParams(new ListView.LayoutParams(
                ListView.LayoutParams.FILL_PARENT, ListView.LayoutParams.WRAP_CONTENT));

        final EditText enterMagstripe = new EditText(this);
        enterMagstripe.setId(MAGSTRIPE_TEXT_ID);
        enterMagstripe.setHint(MAGSTRIPE_HINT);
        enterMagstripe.setInputType(InputType.TYPE_CLASS_NUMBER);
        enterMagstripe.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        MAGSTRIPE_HEADER_WEIGHT));
        listHeader.addView(enterMagstripe);

        final Button signButton = new Button(this);
        signButton.setText("Sign");

        signButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // TODO Add checks to length of string
                final String magstripe = enterMagstripe.getText().toString();
                if (!TextUtils.isEmpty(magstripe))
                {
                    new AddSignature().execute(magstripe);
                }
            }
        });
        listHeader.addView(signButton);

        autographList.addHeaderView(listHeader);

        autographList.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                // NOTE id is not the row id from the database
                // TODO Auto-generated method stub
                final String magstripe = ((TextView)v).getText().toString();
                if (magStripes.containsKey(magstripe))
                {
                    id = magStripes.get(magstripe);
                    startCaptureSignatureActivity(id);
                }
            }
        });


        layout.addView(autographList);
    } // onCreate

    private void startCaptureSignatureActivity(final long id)
    {
        final Intent intent = new Intent(
                AutographListActivity.this, CaptureSignatureActivity.class);
        intent.setAction(ACTION_CAPTURE);
        intent.putExtra(EXTRA_ID, id);
        startActivity(intent);
        Toast.makeText(this,
                "Enter new signature for id: " + id, Toast.LENGTH_SHORT)
                .show();
        ((EditText)findViewById(MAGSTRIPE_TEXT_ID)).setText("");
    }

    private long id = System.currentTimeMillis();
    private final OnSignatureAddedListener mOnSignatureAddedListener =
            new OnSignatureAddedListener()
    {
        @Override
        public void onSignatureAdded(final long newId)
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    id = newId;
                    startCaptureSignatureActivity(id);
                } // run
            });
        }
    };

    private final class AddSignature extends AsyncTask<String, Void, Long>
    {
        @Override
        protected Long doInBackground(String... params)
        {
            return SignatureDatabase.getInstance(
                    AutographListActivity.this).addSignature(params[0]);
        } // doInBackground
        @Override
        protected void onPostExecute(final Long newId)
        {
            if (newId != -1)
            {
                id = newId;
            }
            else
            {
                Toast.makeText(AutographListActivity.this, "Error adding magstripe",
                        Toast.LENGTH_SHORT).show();
            }
        } // onPostExecute
    } // AddSignature

    private static <T> List<T> mapToKeyList(Map<T, ?> map)
    {
        final Collection<T> collection = map.keySet();
        final List<T> list = new ArrayList<T>();
        list.addAll(collection);
        return list;
    }

    private final class GetMagStripesFromDb extends AsyncTask<Void, Void, Boolean>
    {
       // private List<String> magStripes;

        @Override
        protected Boolean doInBackground(Void... noParams)
        {
            magStripes = SignatureDatabase.getInstance(
                    AutographListActivity.this).getAllMagStripesNoneState();
            return magStripes != null;
        } // doInBackground

        @Override
        protected void onPostExecute(Boolean listExists)
        {
            if (listExists)
            {
                final List<String> magstripeList = mapToKeyList(magStripes);
                autographList.setAdapter(new ArrayAdapter<String>(
                        AutographListActivity.this, R.layout.autograph_list_item, magstripeList));
            }
            else
            {
                autographList.setAdapter(new ArrayAdapter<String>(
                        AutographListActivity.this, R.layout.autograph_list_item, new String[] {}));
            }

        } // onPostExecute

        @Override
        protected void onCancelled()
        {
            mGetMagStripesFromDb = null;
        } // onCancelled

    } // GetMagStripesFromDb

    private GetMagStripesFromDb mGetMagStripesFromDb = null;

    @Override
    protected void onPause()
    {
        if (mGetMagStripesFromDb != null)
        {
            mGetMagStripesFromDb.cancel(true);
            mGetMagStripesFromDb = null;
        } // if
        SignatureDatabase.getInstance(this).removeOnSignatureAddedListener(
                mOnSignatureAddedListener);
        super.onPause();
    } // onPause

    @Override
    protected void onResume()
    {
        super.onResume();
        mGetMagStripesFromDb = (GetMagStripesFromDb) new GetMagStripesFromDb().execute();
        SignatureDatabase.getInstance(this).addOnSignatureAddedListener(
                mOnSignatureAddedListener);
    } // onResume


    // Temporary options menu for testing purposes
    private static final int MENU_SETTINGS = Menu.FIRST;
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
                startActivity(new Intent(this, SignaturePreferenceActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        } // switch
    } // onOptionsItemSelected

}
