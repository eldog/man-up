package com.appspot.manup.signature;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SigsNotUploaded extends Activity {
	private DataHelper dh;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		LinearLayout v = new LinearLayout(this);
		v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		v.setOrientation(LinearLayout.VERTICAL);
		setContentView(v);
		dh = new DataHelper(this);
		List<Map<String, Object>> list = dh.selectAllNotUploaded();
		for (Map<String,Object> map : list){
			TextView tv = new TextView(this);
			tv.setText(map.get(DataHelper.FILEPATH).toString());
			tv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			v.addView(tv);
		}
	}
}
