package com.appspot.manup.signature;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import com.appspot.manup.signature.SignatureUploadService.UploadCompleteListener;

public class DataUploadHelperActivity extends Activity
{
    @SuppressWarnings("unused")
    private static final String TAG = DataUploadHelperActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    protected final UploadCompleteListener mListener = new UploadCompleteListener()
    {
        public void onUploadComplete(final Intent intent)
        {
            final long id = intent.getLongExtra(SignatureUploadService.EXTRA_ID, -1);
            final boolean successful =
                    intent.getBooleanExtra(SignatureUploadService.EXTRA_SUCCESSFUL, false);
            final String s = id + ": " + ((successful) ? "Successfully uploaded" : "Upload failed");
            runToastMessageOnUiThread(s);
        } // onUploadComplete
    };

    public void runToastMessageOnUiThread(final String s)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(DataUploadHelperActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private long id = -1;

    @Override
    protected void onPause()
    {
        if (id != -1)
        {
            SignatureUploadService.unregister(mListener, id);
        } // if
        super.onPause();
    }

    public void upload(final long id)
    {
        SignatureUploadService.uploadSignature(this, mListener, id);
    } // upload

}
