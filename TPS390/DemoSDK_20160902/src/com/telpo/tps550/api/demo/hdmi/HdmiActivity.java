package com.telpo.tps550.api.demo.hdmi;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.telpo.tps550.api.demo.R;
import com.telpo.tps550.api.hdmi.HdmiCtrl;

public class HdmiActivity extends Activity {
	LinearLayout hdmilayout;
	Button hdmibtn,moneybox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.money_box);
		hdmilayout = (LinearLayout) findViewById(R.id.hdmi_layout);
		hdmibtn = (Button) findViewById(R.id.hdmi);
		moneybox = (Button) findViewById(R.id.open_moneybox);
		hdmilayout.setVisibility(View.VISIBLE);
		moneybox.setVisibility(View.GONE);
		hdmibtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				HdmiCtrl.switchDisplay(HdmiActivity.this);
			}
		});
	}
}
