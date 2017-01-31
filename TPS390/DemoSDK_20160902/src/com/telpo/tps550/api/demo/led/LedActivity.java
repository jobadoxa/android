package com.telpo.tps550.api.demo.led;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.telpo.tps550.api.demo.R;
import com.telpo.tps550.api.led.Led;

public class LedActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_led);
	}

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.opengreen:
			Led.ledControl(Led.POS_610_GREEN_LED, Led.POS_LED_OPEN);
			break;
		case R.id.closegreen:
			Led.ledControl(Led.POS_610_GREEN_LED, Led.POS_LED_CLOSE);
			break;
		case R.id.openred:
			Led.ledControl(Led.POS_610_RED_LED, Led.POS_LED_OPEN);
			break;
		case R.id.closered:
			Led.ledControl(Led.POS_610_RED_LED, Led.POS_LED_CLOSE);
			break;
		default:
			break;
		}

	}

}
