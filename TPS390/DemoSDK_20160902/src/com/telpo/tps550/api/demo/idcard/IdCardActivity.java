package com.telpo.tps550.api.demo.idcard;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.other.BeepManager;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.demo.R;
import com.telpo.tps550.api.idcard.IdCard;
import com.telpo.tps550.api.idcard.IdentityInfo;

/**
 * For Chinese 2nd generation ID Card test.
 * 
 * @author linhx
 * @date 2015-02-27
 */
public class IdCardActivity extends Activity {
	Button getData;
	TextView idcardInfo;
	ImageView imageView;
	IdentityInfo info;
	BeepManager beepManager;
	Bitmap bitmap;
	byte[] image;
	byte[] fringerprint;
	String fringerprintData;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.idcard_main);
		getData = (Button) findViewById(R.id.requestDataBtn);
		idcardInfo = (TextView) findViewById(R.id.showData);
		imageView = (ImageView) findViewById(R.id.imageView1);
		getData.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				new GetIDInfoTask().execute();
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		beepManager = new BeepManager(this, R.raw.beep);
	}	

	@Override
	protected void onPause() {
		super.onPause();
		beepManager.close();
		beepManager = null;
	}



	private class GetIDInfoTask extends
			AsyncTask<Void, Integer, TelpoException> {
		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			getData.setEnabled(false);
			dialog = new ProgressDialog(IdCardActivity.this);
			dialog.setTitle(getString(R.string.idcard_czz));
			dialog.setMessage(getString(R.string.idcard_ljdkq));
			dialog.setCancelable(false);
			dialog.show();
			info = null;
			bitmap = null;
		}

		@Override
		protected TelpoException doInBackground(Void... arg0) {
			TelpoException result = null;
			try {
				IdCard.open();
				publishProgress(1);
				info = IdCard.checkIdCard(4000);
				image = IdCard.getIdCardImage();
				bitmap = IdCard.decodeIdCardImage(image);
				// luyq add 增加指纹信息
				fringerprint = IdCard.getFringerPrint();
				fringerprintData = getFingerInfo(fringerprint);
			} catch (TelpoException e) {
				e.printStackTrace();
				result = e;
			} finally {
				IdCard.close();
			}
			return result;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if (values[0] == 1) {
				dialog.setMessage(getString(R.string.idcard_hqsfzxx));
			}
		}

		@Override
		protected void onPostExecute(TelpoException result) {
			super.onPostExecute(result);
			dialog.dismiss();
			getData.setEnabled(true);
			if (result == null) {
				beepManager.playBeepSoundAndVibrate();
				idcardInfo.setText(getString(R.string.idcard_xm)
						+ info.getName() + "\n\n"
						+ getString(R.string.idcard_xb) + info.getSex()
						+ "\n\n" + getString(R.string.idcard_mz)
						+ info.getNation() + "\n\n"
						+ getString(R.string.idcard_csrq) + info.getBorn()
						+ "\n\n" + getString(R.string.idcard_dz)
						+ info.getAddress() + "\n\n"
						+ getString(R.string.idcard_sfhm) + info.getNo()
						+ "\n\n" + getString(R.string.idcard_qzjg)
						+ info.getApartment() + "\n\n"
						+ getString(R.string.idcard_yxqx) + info.getPeriod()
						+ "\n\n" + getString(R.string.idcard_zwxx)
						+ fringerprintData);
				imageView.setImageBitmap(bitmap);
			} else {
				idcardInfo.setText(getString(R.string.idcard_dqsbhcs));
				imageView.setImageBitmap(BitmapFactory.decodeResource(
						getResources(), R.drawable.logo));
			}
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		IdCard.close();
	}

	private String GetFingerName(int fingerPos) {
		String fingerName = "";
		switch (fingerPos) {
		case 11:
//			右手拇指
			fingerName = getString(R.string.idcard_ysmz);
			break;
		case 12:
//			右手食指
			fingerName = getString(R.string.idcard_yssz);
			break;
		case 13:
//			右手中指
			fingerName = getString(R.string.idcard_yszz);
			break;
		case 14:
//			右手环指
			fingerName = getString(R.string.idcard_yshz);
			break;
		case 15:
//			右手小指
			fingerName = getString(R.string.idcard_ysxz);
			break;
		case 16:
//			左手拇指
			fingerName = getString(R.string.idcard_zsmz);
			break;
		case 17:
//			左手食指
			fingerName = getString(R.string.idcard_zssz);
			break;
		case 18:
//			左手中指
			fingerName = getString(R.string.idcard_zszz);
			break;
		case 19:
//			左手环指
			fingerName = getString(R.string.idcard_zshz);
			break;
		case 20:
//			左手小指
			fingerName = getString(R.string.idcard_zsxz);
			break;
		case 97:
//			右手不确定指位
			fingerName = getString(R.string.idcard_ysbqdzw);
			break;
		case 98:
//			左手不确定指位
			fingerName = getString(R.string.idcard_zsbqdzw);
			break;
		case 99:
//			其他不确定指位
			fingerName = getString(R.string.idcard_qtbqdzw);
			break;
		default:
//			指位未知
			fingerName = getString(R.string.idcard_zwwz);
			break;
		}
		return fingerName;
	}

	// 第5字节为注册结果代码，0x01-注册成功，0x02--注册失败, 0x03--未注册, 0x09--未知
	private String GetFingerStatus(int fingerStatus) {
		String fingerStatusName = "";
		switch (fingerStatus) {
		case 0x01:
//			注册成功
			fingerStatusName = getString(R.string.idcard_zccg);
			break;
		case 0x02:
//			注册失败
			fingerStatusName = getString(R.string.idcard_zcsb);
			break;
		case 0x03:
//			未注册
			fingerStatusName = getString(R.string.idcard_wzc);
			break;
		case 0x09:
//			注册状态未知
			fingerStatusName = getString(R.string.idcard_zcztwz);
			break;
		default:
//			注册状态未知
			fingerStatusName = getString(R.string.idcard_zcztwz);
			break;
		}
		return fingerStatusName;
	}

	private String getFingerInfo(byte[] fpData) {
		// 解释第1枚指纹，总长度512字节，部分数据格式：
		// 第1字节为特征标识'C'
		// 第5字节为注册结果代码，0x01-注册成功，0x02--注册失败, 0x03--未注册, 0x09--未知
		// 第6字节为指位代码
		// 第7字节为指纹质量值，0x00表示未知，1~100表示质量值
		// 第512字节 crc8值
		String fingerInfo = "";
		if (fpData != null && fpData.length == 1024 && fpData[0] == 'C') {
			fingerInfo = fingerInfo + GetFingerName(fpData[5]);

			if (fpData[4] == 0x01) {
				fingerInfo = fingerInfo + " "+getString(R.string.idcard_zwzl) + String.valueOf(fpData[6]);
			} else {
				fingerInfo = fingerInfo + GetFingerStatus(fpData[4]);
			}

			fingerInfo = fingerInfo + "  ";
			if (fpData[512] == 'C') {
				fingerInfo = fingerInfo + GetFingerName(fpData[512 + 5]);

				if (fpData[512 + 4] == 0x01) {
					fingerInfo = fingerInfo + " "+ getString(R.string.idcard_zwzl)
							+ String.valueOf(fpData[512 + 6]);
				} else {
					fingerInfo = fingerInfo + GetFingerStatus(fpData[512 + 4]);
				}
			}
		} else {
			fingerInfo = getString(R.string.idcard_wdqhbhzw);
		}

		return fingerInfo;
	}

}