package com.appspot.manup.signature;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

// Upload images that failed on first attempt
public class SigsNotUploaded extends DataUploadHelperActivity {
	private static final String TAG = SigsNotUploaded.class.getSimpleName();
	private TableLayout tl;
	private Button uploadAllButton; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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

	@Override
	public void runToastMessageOnUiThread(final String s){ 
    	runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "upload message thread started");
				Toast.makeText(SigsNotUploaded.this, s, Toast.LENGTH_SHORT).show();
				reloadActivity();
			}
		});
	}
}