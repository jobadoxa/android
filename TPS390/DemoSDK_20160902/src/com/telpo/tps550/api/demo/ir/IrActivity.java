package com.telpo.tps550.api.demo.ir;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.telpo.tps550.api.demo.R;

public class IrActivity extends Activity {

	TextView statTextView;
	BroadcastReceiver mBroadcastReceiver;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_ir);
        
		statTextView = (TextView) findViewById(R.id.stat);
        
        IntentFilter intentFilter = new IntentFilter("android.intent.action.IR");
        mBroadcastReceiver = new BroadcastReceiver()
        {

			@Override
			public void onReceive(Context context, Intent intent)
			{
				Bundle bundle = intent.getExtras();
				if (bundle != null)
				{
					int state = bundle.getInt("state");
					Log.d("ANDROID_INFO", "state = " + state);
					if (state == 0)
					{
						statTextView.setText(getString(R.string.ir_tv_close));
						statTextView.setTextColor(Color.parseColor("#0000FF"));
					}
					else if (state == 1)
					{
						statTextView.setText(getString(R.string.ir_tv_leave));
						statTextView.setTextColor(Color.parseColor("#FF0000"));
					}
				}
			}
        	
        };
        
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

	@Override
	protected void onDestroy()
	{
		unregisterReceiver(mBroadcastReceiver);
		super.onDestroy();
	}
    
    
}
