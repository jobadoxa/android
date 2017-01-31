package com.telpo.tps550.api.demo.moneybox;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.telpo.tps550.api.ErrorCode;
import com.telpo.tps550.api.demo.R;
import com.telpo.tps550.api.moneybox.MoneyBox;

public class MoneyBoxActivity extends Activity {
	private LinearLayout hdmilayout;
	private Button moneybox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.money_box);
		hdmilayout = (LinearLayout) findViewById(R.id.hdmi_layout);
		moneybox = (Button) findViewById(R.id.open_moneybox);
		hdmilayout.setVisibility(View.GONE);
		moneybox.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (MoneyBox.open() == ErrorCode.ERR_LOW_POWER)
				{
					Toast.makeText(MoneyBoxActivity.this, getString(R.string.moneybox_toast_text_low_battery), Toast.LENGTH_SHORT).show();
					return;
				}
				try {
					Thread.sleep(500);
					MoneyBox.close();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		});
	}

}
