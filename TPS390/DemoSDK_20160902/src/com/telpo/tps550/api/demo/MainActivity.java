package com.telpo.tps550.api.demo;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.other.BeepManager;
import com.telpo.tps550.api.demo.decode.DecodeActivity;
import com.telpo.tps550.api.demo.hdmi.HdmiActivity;
import com.telpo.tps550.api.demo.iccard.IccActivityNew;
import com.telpo.tps550.api.demo.iccard.PsamActivity;
import com.telpo.tps550.api.demo.idcard.IdCardActivity;
import com.telpo.tps550.api.demo.ir.IrActivity;
import com.telpo.tps550.api.demo.led.LedActivity;
import com.telpo.tps550.api.demo.megnetic.MegneticActivity;
import com.telpo.tps550.api.demo.moneybox.MoneyBoxActivity;
import com.telpo.tps550.api.demo.nfc.NfcActivity;
import com.telpo.tps550.api.demo.ocr.OcrIdCardActivity;
import com.telpo.tps550.api.demo.printer.PrinterActivity;
import com.telpo.tps550.api.demo.printer.UsbPrinterActivity;
import com.telpo.tps550.api.demo.rfid.RfidActivity;
import com.telpo.tps550.api.util.StringUtil;
import com.telpo.tps550.api.util.SystemUtil;

public class MainActivity extends Activity {

	private int Oriental = -1;
	private Button BnPrint, BnQRCode, psambtn, magneticCardBtn, rfidBtn, pcscBtn, identifyBtn, 
	               hdmibtn, moneybox, irbtn, ledbtn, decodebtn, nfcbtn;
	private BeepManager mBeepManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (-1 == Oriental) {
			if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				Oriental = 0;
			} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				Oriental = 1;
			}
		}

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main);

		BnPrint = (Button) findViewById(R.id.print_test);
		BnQRCode = (Button) findViewById(R.id.qrcode_verify);
		magneticCardBtn = (Button) findViewById(R.id.magnetic_card_btn);
		rfidBtn = (Button) findViewById(R.id.rfid_btn);
		pcscBtn = (Button) findViewById(R.id.pcsc_btn);
		identifyBtn = (Button) findViewById(R.id.identity_btn);
		hdmibtn = (Button) findViewById(R.id.hdmi_btn);
		moneybox = (Button) findViewById(R.id.moneybox_btn);
		irbtn = (Button) findViewById(R.id.ir_btn);
		ledbtn = (Button) findViewById(R.id.led_btn);
		psambtn = (Button) findViewById(R.id.psam);
		decodebtn = (Button) findViewById(R.id.decode_btn);
		nfcbtn = (Button) findViewById(R.id.nfc_btn);
		mBeepManager = new BeepManager(this, R.raw.beep);
		setfuncview();

		//MoneyBox
		moneybox.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(MainActivity.this, MoneyBoxActivity.class));
			}
		});

		// HDMI
		hdmibtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(MainActivity.this, HdmiActivity.class));
			}
		});
		
		//Barcode And Qrcode
		BnQRCode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (checkPackage("com.telpo.tps550.api")) {
					Intent intent = new Intent();
					intent.setClassName("com.telpo.tps550.api", "com.telpo.tps550.api.barcode.Capture");
					try {
						startActivityForResult(intent, 0x124);
					} catch (ActivityNotFoundException e) {
						Toast.makeText(MainActivity.this, getResources().getString(R.string.identify_fail), Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(MainActivity.this, getResources().getString(R.string.identify_fail), Toast.LENGTH_LONG).show();
				}
			}
		});
		
		//Print
		BnPrint.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
				dialog.setTitle(getString(R.string.printer_type_select));
				dialog.setNegativeButton(getString(R.string.printer_type_common), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						startActivity(new Intent(MainActivity.this, PrinterActivity.class));
					}
				});
				dialog.setPositiveButton(getString(R.string.printer_type_usb), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						startActivity(new Intent(MainActivity.this, UsbPrinterActivity.class));
					}
				});
				dialog.show();
			}
		});
		
		//Magnetic Card
		magneticCardBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivity(new Intent(MainActivity.this, MegneticActivity.class));
			}
		});
		
		//RFID
		rfidBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivity(new Intent(MainActivity.this, RfidActivity.class));
			}
		});

		//IC Card
		pcscBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, IccActivityNew.class));
			}
		});

		//IR
		irbtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(MainActivity.this, IrActivity.class));
			}
		});
		
		//Led
		ledbtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(MainActivity.this, LedActivity.class));
			}
		});

		//ID Card
		identifyBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
				dialog.setTitle(getString(R.string.idcard_xzgn));
				dialog.setMessage(getString(R.string.idcard_xzsfsbfs));

				dialog.setNegativeButton(getString(R.string.idcard_sxtsb), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						//use camera
						startActivity(new Intent(MainActivity.this, OcrIdCardActivity.class));
					}
				});
				dialog.setPositiveButton(getString(R.string.idcard_dkqsb), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						//use ID Card reader
						startActivity(new Intent(MainActivity.this, IdCardActivity.class));
					}
				});
				dialog.show();

			}

		});
		
		//PSAM Card
		psambtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(MainActivity.this, PsamActivity.class));
			}
		});		

		//laser qrcode
		decodebtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, DecodeActivity.class));
			}
		});
		
		//NFC
		nfcbtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, NfcActivity.class));
			}
		});
	}

	private boolean checkPackage(String packageName) {
		PackageManager manager = this.getPackageManager();
		Intent intent = new Intent().setPackage(packageName);
		List<ResolveInfo> infos = manager.queryIntentActivities(intent, PackageManager.GET_INTENT_FILTERS);
		if (infos == null || infos.size() < 1) {
			return false;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0x124) {
			if (resultCode == 0) {
				if (data != null) {
					mBeepManager.playBeepSoundAndVibrate();
					String qrcode = data.getStringExtra("qrCode");
					Toast.makeText(MainActivity.this, "Scan result:" + qrcode, Toast.LENGTH_LONG).show();
					return;
				}
			} else {
				Toast.makeText(MainActivity.this, "Scan Failed", Toast.LENGTH_LONG).show();
			}
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		setRequestedOrientation(Oriental);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mBeepManager.close();
		mBeepManager = null;
	}

	private void setfuncview() {
		if(SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS350.ordinal()){
			BnQRCode.setEnabled(true);
			pcscBtn.setEnabled(true);
		}else if(SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS390.ordinal()){
			BnPrint.setEnabled(true);
			BnQRCode.setEnabled(true);
			magneticCardBtn.setEnabled(true);
			pcscBtn.setEnabled(true);
			nfcbtn.setEnabled(true);
			decodebtn.setEnabled(true);
		}else if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS510.ordinal()) {
			BnPrint.setEnabled(true);
			BnQRCode.setEnabled(true);
			magneticCardBtn.setEnabled(true);
			rfidBtn.setEnabled(true);
			pcscBtn.setEnabled(true);
			identifyBtn.setEnabled(true);
			hdmibtn.setEnabled(true);
			moneybox.setEnabled(true);
			psambtn.setEnabled(true);
		}else if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS510A.ordinal()) {
			BnPrint.setEnabled(true);
			BnQRCode.setEnabled(true);
			magneticCardBtn.setEnabled(true);
			rfidBtn.setEnabled(true);
			pcscBtn.setEnabled(true);
			identifyBtn.setEnabled(true);
			moneybox.setEnabled(true);
			psambtn.setEnabled(true);
		}else if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS510D.ordinal()) {
			BnPrint.setEnabled(true);
			BnQRCode.setEnabled(true);
			pcscBtn.setEnabled(true);
			identifyBtn.setEnabled(true);
			moneybox.setEnabled(true);
			psambtn.setEnabled(true);
		}else if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS515.ordinal()) {
			moneybox.setEnabled(true);
		}else if(SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS550.ordinal()){
			BnPrint.setEnabled(true);
			BnQRCode.setEnabled(true);
			magneticCardBtn.setEnabled(true);
			rfidBtn.setEnabled(true);
			pcscBtn.setEnabled(true);
			identifyBtn.setEnabled(true);
			psambtn.setEnabled(true);
		}else if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS550A.ordinal()) {
			BnPrint.setEnabled(true);
			BnQRCode.setEnabled(true);
			magneticCardBtn.setEnabled(true);
			pcscBtn.setEnabled(true);
			nfcbtn.setEnabled(true);
			identifyBtn.setEnabled(true);
			psambtn.setEnabled(true);
		}else if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS550MTK.ordinal()) {
			BnPrint.setEnabled(true);
			BnQRCode.setEnabled(true);
			pcscBtn.setEnabled(true);
			identifyBtn.setEnabled(true);
			nfcbtn.setEnabled(true);
			psambtn.setEnabled(true);
		}else if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS580.ordinal()) {
			BnPrint.setEnabled(true);
			BnQRCode.setEnabled(true);
			magneticCardBtn.setEnabled(true);
			rfidBtn.setEnabled(true);
			pcscBtn.setEnabled(true);
			identifyBtn.setEnabled(true);
			psambtn.setEnabled(true);
		}else if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS580A.ordinal()) {
			BnPrint.setEnabled(true);
			BnQRCode.setEnabled(true);
			magneticCardBtn.setEnabled(true);
			pcscBtn.setEnabled(true);
			nfcbtn.setEnabled(true);
			identifyBtn.setEnabled(true);
			psambtn.setEnabled(true);
		}else if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS586.ordinal()) {
			BnPrint.setEnabled(true);
			BnQRCode.setEnabled(true);
			rfidBtn.setEnabled(true);
			psambtn.setEnabled(true);
		}else if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS586A.ordinal()) {
			BnPrint.setEnabled(true);
			BnQRCode.setEnabled(true);
			rfidBtn.setEnabled(true);
		}else if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS610.ordinal()) {
			BnPrint.setEnabled(true);
			BnQRCode.setEnabled(true);
			identifyBtn.setEnabled(true);
			irbtn.setEnabled(true);
			ledbtn.setEnabled(true);
		}else if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS612.ordinal()) {
			moneybox.setEnabled(true);
		}else if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS613.ordinal()) {
			BnPrint.setEnabled(true);
			BnQRCode.setEnabled(true);
			magneticCardBtn.setEnabled(true);
			pcscBtn.setEnabled(true);
			identifyBtn.setEnabled(true);
			moneybox.setEnabled(true);
			psambtn.setEnabled(true);
		}else if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS615.ordinal()) {
			moneybox.setEnabled(true);
		}else {
			BnPrint.setEnabled(true);
			BnQRCode.setEnabled(true);
			magneticCardBtn.setEnabled(true);
			rfidBtn.setEnabled(true);
			pcscBtn.setEnabled(true);
			nfcbtn.setEnabled(true);
			identifyBtn.setEnabled(true);
			hdmibtn.setEnabled(true);
			moneybox.setEnabled(true);
			irbtn.setEnabled(true);
			ledbtn.setEnabled(true);
			psambtn.setEnabled(true);
			decodebtn.setEnabled(true);
		}

	}

}
