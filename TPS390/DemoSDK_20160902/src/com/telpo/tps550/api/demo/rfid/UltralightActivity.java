package com.telpo.tps550.api.demo.rfid;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.demo.R;
import com.telpo.tps550.api.iccard.Picc;

public class UltralightActivity extends Activity {

	private Button Btn_read, Btn_write;
	private EditText ed_input;
	private final int TEST_BLOCK = 4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.ultralight);

		Btn_read = (Button) findViewById(R.id.read);
		Btn_write = (Button) findViewById(R.id.write);
		ed_input = (EditText) findViewById(R.id.input_number);

		Btn_read.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new Ul_ReadTask().execute();
			}
		});

		Btn_write.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String str_number = ed_input.getText().toString();
				if ((null == str_number) || (str_number.length() < 1)) {
					Toast.makeText(UltralightActivity.this, getString(R.string.empty), Toast.LENGTH_SHORT).show();
					return ;
				}
				new Ul_WriteTask().execute();
			}
		});
	}

	private class Ul_ReadTask extends AsyncTask<Void, Void, TelpoException> {

        byte[] sn = new byte[64];
        byte[] sak = new byte[1];
        byte[] tag = new byte[2];			
		byte[] buffer = new byte[4];
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Btn_read.setEnabled(false);
			Btn_write.setEnabled(false);
			ed_input.setEnabled(false);
		}

		@Override
		protected TelpoException doInBackground(Void... params) {
			TelpoException result = null;
			try {
				Picc.openReader(UltralightActivity.this);
				Picc.selectCard(sn,sak,tag);
				Picc.ultralightRead(TEST_BLOCK, buffer);
			} catch (TelpoException e) {
				e.printStackTrace();
				result = e;
			} finally {
				Picc.closeReader(UltralightActivity.this);
			}
			return result;
		}

		@Override
		protected void onPostExecute(TelpoException result) {
			super.onPostExecute(result);
			if (result == null) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(UltralightActivity.this);
                dialogBuilder.setMessage(new String(buffer).trim());
                dialogBuilder.setPositiveButton(R.string.confirm,null);
                dialogBuilder.create();
                dialogBuilder.show();
			} else {
				Toast.makeText(UltralightActivity.this, getString(R.string.operation_fail), Toast.LENGTH_LONG).show();
			}

			Btn_read.setEnabled(true);
			Btn_write.setEnabled(true);
			ed_input.setEnabled(true);

		}
	}

	private class Ul_WriteTask extends AsyncTask<Void, Void, TelpoException> {
		
        byte[] sn = new byte[64];
        byte[] sak = new byte[1];
        byte[] tag = new byte[2];
		byte[] Data = new byte[4];
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Btn_read.setEnabled(false);
			Btn_write.setEnabled(false);
			ed_input.setEnabled(false);
		}

		@Override
		protected TelpoException doInBackground(Void... params) {
			TelpoException result = null;
			byte[] Temp = ed_input.getText().toString().getBytes();
			for (int i = 0; i < Temp.length && i < 4; ++i) {
				Data[i] = Temp[i];
			}
			try {
				Picc.openReader(UltralightActivity.this);
				Picc.selectCard(sn,sak,tag);
				Picc.ultralightWrite(TEST_BLOCK, Data);
			} catch (TelpoException e) {
				e.printStackTrace();
				result = e;
			} finally {
				Picc.closeReader(UltralightActivity.this);
			}
			return result;
		}

		@Override
		protected void onPostExecute(TelpoException result) {
			super.onPostExecute(result);
			if (result == null) {
				Toast.makeText(UltralightActivity.this, getString(R.string.write_succss), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(UltralightActivity.this, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();
			}

			Btn_read.setEnabled(true);
			Btn_write.setEnabled(true);
			ed_input.setEnabled(true);
		}
	}

}
