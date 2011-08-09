package com.appspot.manup.signature;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.manup.signature.SignatureUploadService.UploadCompleteListener;

// Upload images that failed on first attempt
public class SigsNotUploaded extends Activity {
	private DataHelper dh;
	private TableLayout tl;
	private Button uploadAllButton; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dh = new DataHelper(this);

		LinearLayout v = new LinearLayout(this);
		final int padding_in_dp = 10;
		final float scale = getResources().getDisplayMetrics().density;
		int padding_in_px = (int) (padding_in_dp * scale + 0.5f);
		v.setBackgroundColor(Color.WHITE);
		v.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);
		v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		v.setOrientation(LinearLayout.VERTICAL);
		setContentView(v);

		uploadAllButton = new Button(this);
		uploadAllButton.setText("Upload all");
		uploadAllButton.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		v.addView(uploadAllButton);

		ScrollView sv = new ScrollView(this);
		sv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		v.addView(sv);

		tl = new TableLayout(this);
		tl.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		sv.addView(tl);

		reloadActivity();
	}

	private void reloadActivity() {
		tl.removeAllViews();
		final List<Map<String, Object>> notUploadedList = dh.selectAllNotUploaded();
		uploadAllButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				for (Map<String, Object> map : notUploadedList)
					try{
						upload(map.get(DataHelper.FILEPATH).toString(), map.get(DataHelper.ID).toString());
					} catch (IOException e) { e.printStackTrace(); }
			}
		});
		for (Map<String, Object> map : notUploadedList) {
			TableRow tr = new TableRow(this);
			TextView tv = new TextView(this);
			tv.setTextColor(Color.BLACK);
			String studentId = map.get(DataHelper.ID).toString();
			tv.setText(studentId + ": "
					+ map.get(DataHelper.FILEPATH).toString());
			tr.addView(tv);

			Button b = new Button(this);
			b.setTag(studentId);
			b.setText("Upload");
			b.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String studentId = v.getTag().toString();
					try {
						upload(dh.selectImagePath(studentId), studentId);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			tr.addView(b);
			tl.addView(tr);
		}
	}

	private static final String TAG = SigsNotUploaded.class.getSimpleName();
	private final UploadCompleteListener mListener = new UploadCompleteListener() {
		public void onUploadComplete(final Intent intent) {
			Log.d(TAG, "Got callback.");
			final String studentId = intent
					.getStringExtra(SignatureUploadService.EXTRA_STUDENT_ID);
			final boolean successful = intent.getBooleanExtra(
					SignatureUploadService.EXTRA_SUCCESSFUL, false);
			Log.d(TAG, studentId + " upload complete. Successful? "
					+ successful);
			final String s = studentId
					+ ": "
					+ ((successful) ? "successfully uploaded" : "upload failed");
			if (successful) {
				try {
					String filePath = dh.selectImagePath(studentId);
					if (filePath != null) {
						File f = new File(filePath);
						if (!f.delete())
							throw new IOException("Delete failed");
						Log.d(TAG, studentId + " " + filePath
								+ ": Deleted file from external storage");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				dh.delete(studentId);
			}
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Log.d(TAG, "upload message thread started");
					// TODO Auto-generated method stub
					Toast.makeText(SigsNotUploaded.this, s, Toast.LENGTH_SHORT)
							.show();
					reloadActivity();
				}
			});
		} // onUploadComplete
	};

	public void upload(final String path, String id) throws IOException {
		SignatureUploadService.uploadSignature(this, mListener, path, id);
	} // upload

}