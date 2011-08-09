package com.appspot.manup.signature;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.appspot.manup.signature.SignatureUploadService.UploadCompleteListener;

public class DataUploadHelperActivity extends Activity {
	protected DataHelper dh;
	private static final String TAG = DataUploadHelperActivity.class.getSimpleName();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dh = new DataHelper(this);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		return true;
	}
	
	String filePath = null;
	
	protected final UploadCompleteListener mListener = new UploadCompleteListener() {
        public void onUploadComplete(final Intent intent) {
            Log.d(TAG, "Got callback.");
            final String studentId = intent.getStringExtra(SignatureUploadService.EXTRA_STUDENT_ID);
            final boolean successful =
                    intent.getBooleanExtra(SignatureUploadService.EXTRA_SUCCESSFUL, false);
            Log.d(TAG, studentId + " upload complete. Successful? " + successful);
            final String s = studentId + ": " + ((successful) ? "Successfully uploaded" : "Upload failed");
            if (successful){
            	try {
            		filePath = dh.selectImagePath(studentId);
            		if (filePath != null){
            			File f = new File(filePath);
            			if (!f.delete())
            				throw new IOException("Delete failed");
            			Log.d(TAG, studentId + " " + filePath + ": Deleted file from external storage");
            		}
            	} catch (IOException e) { e.printStackTrace();}
            	dh.delete(studentId);
            }
            runToastMessageOnUiThread(s);
        } // onUploadComplete
    };
    
    public void runToastMessageOnUiThread(final String s){ 
    	runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(DataUploadHelperActivity.this, s, Toast.LENGTH_SHORT).show();
			}
		});
	}

    @Override
    protected void onPause() {
    	if (filePath != null)
    		SignatureUploadService.unregister(mListener, filePath);
        super.onPause();
    }
    
    public void upload(final String path, String id) throws IOException {
        SignatureUploadService.uploadSignature(this, mListener, path, id);
    } // upload
	
}
