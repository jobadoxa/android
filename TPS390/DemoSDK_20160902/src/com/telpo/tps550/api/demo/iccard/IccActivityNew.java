package com.telpo.tps550.api.demo.iccard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.telpo.tps550.api.demo.R;

public class IccActivityNew extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.iccard_main_new);
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.sle4442_btn:
					Intent intent4442 = new Intent(IccActivityNew.this, SLE4442Activity.class);
					startActivity(intent4442);

					break;

				case R.id.sle4428_btn:
					Intent intent4428 = new Intent(IccActivityNew.this, SLE4428Activity.class);
					startActivity(intent4428);

					break;

				case R.id.monitor_btn:
					Intent intent = new Intent(IccActivityNew.this, MonitorActivity.class);
					startActivity(intent);

					break;
				case R.id.smartcard:
					Intent smartcardintent = new Intent(IccActivityNew.this, SmarCardNewActivity.class);
					startActivity(smartcardintent);
					break;
				}
			}
		};
		Button sle4442_btn = (Button) findViewById(R.id.sle4442_btn);
		sle4442_btn.setOnClickListener(listener);

		Button sle4428_btn = (Button) findViewById(R.id.sle4428_btn);
		sle4428_btn.setOnClickListener(listener);

		Button monitor_btn = (Button) findViewById(R.id.monitor_btn);
		monitor_btn.setOnClickListener(listener);

		Button smart_btn = (Button) findViewById(R.id.smartcard);
		smart_btn.setOnClickListener(listener);

	}

}
