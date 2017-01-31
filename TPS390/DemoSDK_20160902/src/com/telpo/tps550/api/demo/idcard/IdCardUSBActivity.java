package com.telpo.tps550.api.demo.idcard;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.other.BeepManager;
import com.sam.sdticreader.WltDec;
import com.sdt.Common;
import com.sdt.Sdtapi;
import com.telpo.tps550.api.demo.R;
import com.telpo.tps550.api.idcard.IdCard;
import com.telpo.tps550.api.idcard.IdentityInfo;

/**
 * For Chinese 2nd generation ID Card test.
 * 
 * @author linhx
 * @date 2015-02-27
 */
public class IdCardUSBActivity extends Activity {
	Common common; // common对象，存储一些需要的参数
	/*民族列表*/
	String[] nation = { "汉", "蒙古", "回", "藏", "维吾尔", "苗", "彝", "壮", "布依", "朝鲜", "满", "侗", "瑶", "白", "土家", "哈尼", "哈萨克", "傣", "黎", "傈僳", "佤", "畲", "高山", "拉祜", "水", "东乡", "纳西", "景颇", "克尔克孜", "土", "达斡尔",
			"仫佬", "羌", "布朗", "撒拉", "毛南", "仡佬", "锡伯", "阿昌", "普米", "塔吉克", "怒", "乌兹别克", "俄罗斯", "鄂温克", "德昂", "保安", "裕固", "京", "塔塔尔", "独龙", "鄂伦春", "赫哲", "门巴", "珞巴", "基诺" };

	Button getData;
	TextView idcardInfo;
	ImageView imageView;
	IdentityInfo info;
	BeepManager beepManager;
	Bitmap bitmap;
	byte[] image;
	Sdtapi sdta;
	UsbManager usbManager;
	private PendingIntent mPermissionIntent;
	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.idcard_main);

		getData = (Button) findViewById(R.id.requestDataBtn);
		idcardInfo = (TextView) findViewById(R.id.showData);
		imageView = (ImageView) findViewById(R.id.imageView1);

		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		registerReceiver(mUsbReceiver, filter);
		usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		for (UsbDevice device : usbManager.getDeviceList().values()) {
			if (device.getVendorId() == 1024 && device.getProductId() == 50010) {
				usbManager.requestPermission(device, mPermissionIntent);
			}
		}

		beepManager = new BeepManager(this, R.raw.beep);

		getData.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				new GetIDInfoTask().execute();
			}
		});

	}

	class GetIDInfoTask extends AsyncTask<Void, Integer, Integer> {
		ProgressDialog dialog;
		String show;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			getData.setEnabled(false);
			dialog = new ProgressDialog(IdCardUSBActivity.this);
			dialog.setTitle(getString(R.string.idcard_czz));
			dialog.setMessage(getString(R.string.idcard_ljdkq));
			dialog.setCancelable(false);
			dialog.show();
			info = null;
			bitmap = null;
		}

		@Override
		protected Integer doInBackground(Void... arg0) {
			int result = 0x80;
			int num = 0;
			boolean end = false;
			try {
				while (result == 0x80 && num < 20 && !end) {
					Thread.sleep(10);
					Log.i("Status", String.format("0x%02x", sdta.SDT_GetSAMStatus()));
					result = sdta.SDT_StartFindIDCard();// 寻找身份证
					num++;
					Log.i("num", "num in:" + num);

				}
			} catch (InterruptedException e) {
				end = true;
				e.printStackTrace();
			}

			Log.i("num", "num out:" + num);
			return result;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			dialog.dismiss();
			getData.setEnabled(true);
			Log.i("result", String.format("0x%02x", result));
			if (result == 0x9f) {
				sdta.SDT_SelectIDCard();// 选取身份证

				IdCardMsg msg = new IdCardMsg();// 身份证信息对象，存储身份证上的文字信息

				int ret = ReadBaseMsgToStr(msg);
				Log.i("ret", String.format("0x%02x", ret));
				if (ret == 0x90) {
					show = "姓名:" + msg.name + '\n' + "性别:" + msg.sex + '\n' + "民族:" + msg.nation_str + "族" + '\n' + "出生日期:" + msg.birth_year + "-" + msg.birth_month + "-" + msg.birth_day + '\n'
							+ "住址:" + msg.address + '\n' + "身份证号码:" + msg.id_num + '\n' + "签发机关:" + msg.sign_office + '\n' + "有效期起始日期:" + msg.useful_s_date_year + "-" + msg.useful_s_date_month + "-"
							+ msg.useful_s_date_day + '\n' + "有效期截止日期:" + msg.useful_e_date_year + "-" + msg.useful_e_date_month + "-" + msg.useful_e_date_day + '\n';

					byte[] bm = WltDec.decodeToBitmap(msg.photo);
					bitmap = BitmapFactory.decodeByteArray(bm, 0, bm.length);

				} else
					show = "读基本信息失败:" + String.format("0x%02x", ret);
				idcardInfo.setText(show);
				imageView.setImageBitmap(bitmap);

			} else {
				show = "读基本信息失败:" + String.format("0x%02x", result);
				idcardInfo.setText(show);
				imageView.setImageBitmap(bitmap);
			}
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		beepManager.updatePrefs();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mUsbReceiver);
		IdCard.close();
	}

	class IdCardMsg {
		public String name;
		public String sex;
		public String nation_str;

		public String birth_year;
		public String birth_month;
		public String birth_day;
		public String address;
		public String id_num;
		public String sign_office;

		public String useful_s_date_year;
		public String useful_s_date_month;
		public String useful_s_date_day;

		public String useful_e_date_year;
		public String useful_e_date_month;
		public String useful_e_date_day;
		public byte[] photo;

	}

	// 读取身份证中的文字信息（可阅读格式的）
	public int ReadBaseMsgToStr(IdCardMsg msg) {
		int ret;
		int[] puiCHMsgLen = new int[1];
		int[] puiPHMsgLen = new int[1];

		byte[] pucCHMsg = new byte[256];
		byte[] pucPHMsg = new byte[1024];

		// sdtapi中标准接口，输出字节格式的信息。
		ret = sdta.SDT_ReadBaseMsg(pucCHMsg, puiCHMsgLen, pucPHMsg, puiPHMsgLen);
		if (ret == 0x90) {
			try {
				char[] pucCHMsgStr = new char[128];
				DecodeByte(pucCHMsg, pucCHMsgStr);// 将读取的身份证中的信息字节，解码成可阅读的文字
				PareseItem(pucCHMsgStr, pucPHMsg, msg); // 将信息解析到msg中

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return ret;

	}

	// 字节解码函数
	void DecodeByte(byte[] msg, char[] msg_str) throws Exception {
		byte[] newmsg = new byte[msg.length + 2];

		newmsg[0] = (byte) 0xff;
		newmsg[1] = (byte) 0xfe;

		for (int i = 0; i < msg.length; i++)
			newmsg[i + 2] = msg[i];

		String s = new String(newmsg, "UTF-16");
		for (int i = 0; i < s.toCharArray().length; i++)
			msg_str[i] = s.toCharArray()[i];

	}

	// 分段信息提取
	void PareseItem(char[] pucCHMsgStr, byte[] photo, IdCardMsg msg) {
		msg.name = String.copyValueOf(pucCHMsgStr, 0, 15);
		String sex_code = String.copyValueOf(pucCHMsgStr, 15, 1);

		if (sex_code.equals("1"))
			msg.sex = "男";
		else if (sex_code.equals("2"))
			msg.sex = "女";
		else if (sex_code.equals("0"))
			msg.sex = "未知";
		else if (sex_code.equals("9"))
			msg.sex = "未说明";

		String nation_code = String.copyValueOf(pucCHMsgStr, 16, 2);
		msg.nation_str = nation[Integer.valueOf(nation_code) - 1];

		msg.birth_year = String.copyValueOf(pucCHMsgStr, 18, 4);
		msg.birth_month = String.copyValueOf(pucCHMsgStr, 22, 2);
		msg.birth_day = String.copyValueOf(pucCHMsgStr, 24, 2);
		msg.address = String.copyValueOf(pucCHMsgStr, 26, 35);
		msg.id_num = String.copyValueOf(pucCHMsgStr, 61, 18);
		msg.sign_office = String.copyValueOf(pucCHMsgStr, 79, 15);

		msg.useful_s_date_year = String.copyValueOf(pucCHMsgStr, 94, 4);
		msg.useful_s_date_month = String.copyValueOf(pucCHMsgStr, 98, 2);
		msg.useful_s_date_day = String.copyValueOf(pucCHMsgStr, 100, 2);

		msg.useful_e_date_year = String.copyValueOf(pucCHMsgStr, 102, 4);
		msg.useful_e_date_month = String.copyValueOf(pucCHMsgStr, 106, 2);
		msg.useful_e_date_day = String.copyValueOf(pucCHMsgStr, 108, 2);
		msg.photo = photo;
	}

	// 广播接收器
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						if (device != null) {
							try {
								sdta = new Sdtapi(IdCardUSBActivity.this);

							} catch (Exception e1) {// 捕获异常，
								Log.i("sdta", "不能初始化Sdtapi");

								if (e1.getCause() == null) // USB设备异常或无连接，应用程序即将关闭。
								{

									new Thread() {
										@Override
										public void run() {
											Log.i("sdta", "USB设备异常或无连接，应用程序即将关闭");
										}

									}.start();
								} else // USB设备未授权，需要确认授权
								{
									idcardInfo.setGravity(0);
									idcardInfo.setTextSize(30);
									Log.i("sdta", "USB设备未授权，需要确认授权");
								}

							}
						}
					} else {
						Log.d("IdCardUSBActivity", "Permission denied for device " + device.getDeviceName());
					}
				}

			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {

				Log.d("IdCardUSBActivity", "Device Detached");

			} else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
				for (UsbDevice device : usbManager.getDeviceList().values()) {
					if (device.getVendorId() == 1024 && device.getProductId() == 50010) {
						usbManager.requestPermission(device, mPermissionIntent);
					}
				}
			}

		}
	};
}