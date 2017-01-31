package com.telpo.tps550.api.demo.decode;

import com.telpo.tps550.api.decode.Decode;
import com.telpo.tps550.api.demo.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class DecodeActivity extends Activity {
	
	private Button bt_decode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.decode);
		bt_decode = (Button) findViewById(R.id.bt_decode);
		bt_decode.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new GetDataAndTypeTask().execute();
			}
		});
	}
    
    private class GetDataAndTypeTask extends AsyncTask<Void, Void, Exception> {
        ProgressDialog dialog;
        String message;
        
		@Override
		protected Exception doInBackground(Void... params) {
		  Exception result = null;
          try{
        	Decode.open();
        	byte[] data = Decode.read(10000);
        	message = new String(data, 2, data[1], "UTF-8");
        }catch (Exception e){
            e.printStackTrace();
            result = e;
        }finally {
            Decode.close();
        }
            return result;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if(dialog == null){
				dialog = new ProgressDialog(DecodeActivity.this);
		  }          
            dialog.setMessage(getString(R.string.operating));
            dialog.setCancelable(false);
            dialog.show();
		}

		@Override
		protected void onPostExecute(Exception result) {
			super.onPostExecute(result);
            dialog.dismiss();
            if(result == null){
              AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(DecodeActivity.this);
              dialogBuilder.setMessage(message);
              dialogBuilder.setPositiveButton(R.string.confirm,null);
              dialogBuilder.setCancelable(true);
              dialogBuilder.create();
              dialogBuilder.show();
          }else {
        	  Toast.makeText(DecodeActivity.this, getString(R.string.operation_fail), Toast.LENGTH_LONG).show();
		}
	  }
    }
}
