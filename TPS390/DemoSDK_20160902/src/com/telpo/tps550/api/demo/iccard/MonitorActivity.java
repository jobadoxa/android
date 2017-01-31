package com.telpo.tps550.api.demo.iccard;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.telpo.tps550.api.demo.R;
import com.telpo.tps550.api.reader.CardReader;
import com.telpo.tps550.api.reader.ReaderMonitor;
import com.telpo.tps550.api.reader.SmartCardReader;
import com.telpo.tps550.api.util.StringUtil;

public class MonitorActivity extends Activity {
	Button readButton;
	Button writeButton;
	Button startMonitorButton;
	Button stopMonitorButton;
	EditText addrEditText;
	EditText numEditText;
	EditText readResultEditText;
	EditText writeEditText;
	EditText writeContent, mEditTextApdu;
	TextView cardTypeTextView, textReader;
	EditText mscTrackData;
	Button verifyPscButton;
	Button modifyPscButton;
	EditText pscEditText;
	private Button atrButton, protocolbtn, buttonAPDU;
	LinearLayout smartcardlayout, slecardlayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.monitor);

		addrEditText = (EditText) findViewById(R.id.addr_edt);
		numEditText = (EditText) findViewById(R.id.num_edt);
		readResultEditText = (EditText) findViewById(R.id.read_result_edt);
		writeEditText = (EditText) findViewById(R.id.write_addr_edt);
		writeContent = (EditText) findViewById(R.id.write_content_edt);
		cardTypeTextView = (TextView) findViewById(R.id.card_type_txtv);
		mscTrackData = (EditText) findViewById(R.id.msc_data_edt);
		pscEditText = (EditText) findViewById(R.id.psc_edt);
		smartcardlayout = (LinearLayout) findViewById(R.id.smartcard);
		slecardlayout = (LinearLayout) findViewById(R.id.sle_card);
		textReader = (TextView) findViewById(R.id.textReader);
		mEditTextApdu = (EditText) findViewById(R.id.editTextAPDU);
		OnClickListener listener = new OnClickListener() {

			@SuppressWarnings("static-access")
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.start_monitor_btn:
					ReaderMonitor.setContext(MonitorActivity.this);
					ReaderMonitor.startMonitor();
					readButton.setEnabled(true);
					writeButton.setEnabled(true);
					startMonitorButton.setEnabled(false);
					stopMonitorButton.setEnabled(true);
					verifyPscButton.setEnabled(true);
					modifyPscButton.setEnabled(true);
					atrButton.setEnabled(true);
					protocolbtn.setEnabled(true);
					buttonAPDU.setEnabled(true);
					break;

				case R.id.stop_monitor_btn:
					ReaderMonitor.stopMonitor();
					readButton.setEnabled(false);
					writeButton.setEnabled(false);
					startMonitorButton.setEnabled(true);
					stopMonitorButton.setEnabled(false);
					verifyPscButton.setEnabled(false);
					modifyPscButton.setEnabled(false);
					atrButton.setEnabled(false);
					protocolbtn.setEnabled(false);
					buttonAPDU.setEnabled(false);
					break;

				case R.id.read_btn:
					String addr_s = addrEditText.getText().toString();
					if (addr_s.length() == 0) {
						Toast.makeText(MonitorActivity.this, "addr can not be null", Toast.LENGTH_SHORT).show();
						break;
					}
					String num_s = numEditText.getText().toString();
					if (num_s.length() == 0) {
						Toast.makeText(MonitorActivity.this, "num can not be null", Toast.LENGTH_SHORT).show();
						break;
					}
					byte[] r = ReaderMonitor.readMainMemory(Integer.valueOf(addr_s), Integer.valueOf(num_s));
					if (r != null) {
						readResultEditText.setText(BCD2Str(r));
					} else {
						Toast.makeText(MonitorActivity.this, "read failed", Toast.LENGTH_SHORT).show();
					}
					break;

				case R.id.write_btn:
					String addr_w = writeEditText.getText().toString();
					if (addr_w.length() == 0) {
						Toast.makeText(MonitorActivity.this, "addr can not be null", Toast.LENGTH_SHORT).show();
						break;
					}
					String content = writeContent.getText().toString();
					if (content.length() == 0) {
						Toast.makeText(MonitorActivity.this, "no content", Toast.LENGTH_SHORT).show();
					}
					String addr = writeEditText.getText().toString();
					String cont = writeContent.getText().toString().replace(" ", "").toUpperCase();
					if (ReaderMonitor.updateMainMemory(Integer.valueOf(addr), str2BCD(content))) {
						Toast.makeText(MonitorActivity.this, "update main memory success", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(MonitorActivity.this, "update main memory failed", Toast.LENGTH_SHORT).show();
					}
					break;

				case R.id.verify_psc_btn: {
					String psc = pscEditText.getText().toString();
					if (ReaderMonitor.pscVerify(str2BCD(psc))) {
						Toast.makeText(MonitorActivity.this, "psc verify successful", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(MonitorActivity.this, "psc verify failed", Toast.LENGTH_SHORT).show();
					}
				}
					break;

				case R.id.modify_psc_btn: {
					String psc = pscEditText.getText().toString();
					if (ReaderMonitor.pscModify(str2BCD(psc))) {
						Toast.makeText(MonitorActivity.this, "psc modify successful", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(MonitorActivity.this, "psc modify failed", Toast.LENGTH_SHORT).show();
					}
				}
					break;

				case R.id.atr_btn:
					if (ReaderMonitor.getATRString() != null) {
						textReader.setText(getString(R.string.get_ATR_success) + ReaderMonitor.getATRString());
					} else {
						textReader.setText(getString(R.string.get_ATR_fail));
					}
					break;
				case R.id.protocol_btn:
					int proto = ReaderMonitor.getProtocol();
					if (proto == SmartCardReader.PROTOCOL_T0) {
						textReader.setText("protocol: T0");
					} else if (proto == SmartCardReader.PROTOCOL_T1) {
						textReader.setText("protocol: T1");
					} else {
						textReader.setText("protocol: NA");
					}
					break;

				case R.id.buttonAPDU:
					byte[] pSendAPDU;
					byte[] result;
					int[] pRevAPDULen = new int[1];
					String apduStr;

					Log.d("sendAPDUkOnClick", "sendAPDUkOnClick");
					pRevAPDULen[0] = 300;
					apduStr = mEditTextApdu.getText().toString();
					pSendAPDU = toByteArray(apduStr);

					result = ReaderMonitor.transmit(pSendAPDU);

					textReader.setText(TextUtils.isEmpty(StringUtil.toHexString(result)) ? getString(R.string.send_APDU_fail) : getString(R.string.send_APDU_success) + StringUtil.toHexString(result));

					break;
				default:
					break;
				}
			}
		};

		IntentFilter filter = new IntentFilter();
		filter.addAction(ReaderMonitor.ACTION_ICC_PRESENT);
		filter.addAction(ReaderMonitor.ACTION_MSC);
		registerReceiver(mReceiver, filter);

		readButton = (Button) findViewById(R.id.read_btn);
		readButton.setOnClickListener(listener);
		readButton.setEnabled(false);
		writeButton = (Button) findViewById(R.id.write_btn);
		writeButton.setOnClickListener(listener);
		writeButton.setEnabled(false);
		startMonitorButton = (Button) findViewById(R.id.start_monitor_btn);
		startMonitorButton.setOnClickListener(listener);
		stopMonitorButton = (Button) findViewById(R.id.stop_monitor_btn);
		stopMonitorButton.setOnClickListener(listener);
		stopMonitorButton.setEnabled(false);
		verifyPscButton = (Button) findViewById(R.id.verify_psc_btn);
		verifyPscButton.setOnClickListener(listener);
		verifyPscButton.setEnabled(false);
		modifyPscButton = (Button) findViewById(R.id.modify_psc_btn);
		modifyPscButton.setOnClickListener(listener);
		modifyPscButton.setEnabled(false);
		atrButton = (Button) findViewById(R.id.atr_btn);
		atrButton.setOnClickListener(listener);
		protocolbtn = (Button) findViewById(R.id.protocol_btn);
		protocolbtn.setOnClickListener(listener);
		buttonAPDU = (Button) findViewById(R.id.buttonAPDU);
		buttonAPDU.setOnClickListener(listener);
	}

	@Override
	protected void onDestroy() {
		ReaderMonitor.stopMonitor();
		super.onDestroy();
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("123", "icc present Broadcast Receiver");
			if (intent.getAction() == ReaderMonitor.ACTION_ICC_PRESENT) {
				if (intent.getExtras().getBoolean(ReaderMonitor.EXTRA_IS_PRESENT)) {
					int cardType = intent.getExtras().getInt(ReaderMonitor.EXTRA_CARD_TYPE);
					if (cardType == CardReader.CARD_TYPE_SLE4428) {
						cardTypeTextView.setText("SLE4428");
						slecardlayout.setVisibility(View.VISIBLE);
						smartcardlayout.setVisibility(View.GONE);

					} else if (cardType == CardReader.CARD_TYPE_SLE4442) {
						cardTypeTextView.setText("SLE4442");
						slecardlayout.setVisibility(View.VISIBLE);
						smartcardlayout.setVisibility(View.GONE);
					} else if (cardType == CardReader.CARD_TYPE_ISO7816) {
						cardTypeTextView.setText("Smart Card");

						slecardlayout.setVisibility(View.GONE);
						smartcardlayout.setVisibility(View.VISIBLE);
					} else {
						cardTypeTextView.setText("Unknown");
					}
				} else {
					cardTypeTextView.setText("NO Card");
				}
			} else if (intent.getAction() == ReaderMonitor.ACTION_MSC) {
				String[] trackData = intent.getExtras().getStringArray(ReaderMonitor.EXTRA_MSC_TRACK);
				StringBuilder builder = new StringBuilder();
				builder.append("Track1:\n" + trackData[0] + "\nTrack2:\n" + trackData[1] + "\nTrack3:\n" + trackData[2]);
				mscTrackData.setText(builder.toString());
			}
		}

	};

	private String BCD2Str(byte[] data) {
		String string;
		StringBuilder stringBuilder = new StringBuilder();

		for (int i = 0; i < data.length; i++) {
			string = Integer.toHexString(data[i] & 0xFF);
			if (string.length() == 1) {
				stringBuilder.append("0");
			}

			stringBuilder.append(string.toUpperCase());
			stringBuilder.append(" ");
		}

		return stringBuilder.toString();
	}

	private byte[] str2BCD(String string) {
		int len;
		String str;
		String hexStr = "0123456789ABCDEF";

		String s = string.toUpperCase();

		len = s.length();
		if ((len % 2) == 1) {
			// 长度不为偶数，右补0
			str = s + "0";
			len = (len + 1) >> 1;
		} else {
			str = s;
			len >>= 1;
		}

		byte[] bytes = new byte[len];
		byte high;
		byte low;

		for (int i = 0, j = 0; i < len; i++, j += 2) {
			high = (byte) (hexStr.indexOf(str.charAt(j)) << 4);
			low = (byte) hexStr.indexOf(str.charAt(j + 1));
			bytes[i] = (byte) (high | low);
		}

		return bytes;
	}

	public static byte[] toByteArray(String hexString) {

		int hexStringLength = hexString.length();
		byte[] byteArray = null;
		int count = 0;
		char c;
		int i;

		// Count number of hex characters
		for (i = 0; i < hexStringLength; i++) {

			c = hexString.charAt(i);
			if (c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f') {
				count++;
			}
		}

		byteArray = new byte[(count + 1) / 2];
		boolean first = true;
		int len = 0;
		int value;
		for (i = 0; i < hexStringLength; i++) {

			c = hexString.charAt(i);
			if (c >= '0' && c <= '9') {
				value = c - '0';
			} else if (c >= 'A' && c <= 'F') {
				value = c - 'A' + 10;
			} else if (c >= 'a' && c <= 'f') {
				value = c - 'a' + 10;
			} else {
				value = -1;
			}

			if (value >= 0) {

				if (first) {

					byteArray[len] = (byte) (value << 4);

				} else {

					byteArray[len] |= value;
					len++;
				}

				first = !first;
			}
		}

		return byteArray;
	}

}
