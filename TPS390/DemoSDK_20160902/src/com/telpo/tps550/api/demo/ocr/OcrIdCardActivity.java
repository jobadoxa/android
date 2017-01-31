package com.telpo.tps550.api.demo.ocr;

import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.telpo.tps550.api.demo.R;
import com.telpo.tps550.api.idcard.IdentityInfo;

/***
 * For OCR of Chinese 2nd ID card test.
 * @author linhx
 * @date 2015-02-27
 */
public class OcrIdCardActivity extends Activity {

	private final int ID_REQ1 = 2;
	private final int ID_REQ2 = 3;

	TextView idInfo1, idInfo2;
	Button recog2, recog3;
	RadioGroup radioGroup;
	ImageView imageView;

    private int Oriental = -1;
    Boolean show_head_photo=false;
	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.ocr_main);

        if(-1 == Oriental){

            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                Oriental = 0;
            }
            else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                Oriental = 1;
            }
        }

		idInfo1 = (TextView) findViewById(R.id.idInfo1);
		idInfo2 = (TextView) findViewById(R.id.idInfo2);

		recog2 = (Button) findViewById(R.id.recog2);
		recog3 = (Button) findViewById(R.id.recog3);
		
		radioGroup=(RadioGroup) findViewById(R.id.select_show_head_photo);
		imageView=(ImageView) findViewById(R.id.image_head);

		recog2.setEnabled(false);
		recog3.setEnabled(false);

		recog2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				recog2.setEnabled(false);
				Intent intent = new Intent();
				intent.setClassName("com.telpo.tps550.api",
						"com.telpo.tps550.api.ocr.IdCardOcr");
				intent.putExtra("type", true);
				intent.putExtra("isKeepPicture", true);// 是否保存图片
														// true是，false:否，不传入时，默认为否
				intent.putExtra("PictPath", "/sdcard/DCIM/Camera/003.png");// 图片路径，不传入时保存到默认路径/sdcard/OCRPict
				intent.putExtra("PictFormat", "PNG");// 图片格式：JPEG，PNG，WEBP，不传入时默认为PNG格式
                intent.putExtra("show_head_photo", show_head_photo);
				try {
					startActivityForResult(intent, ID_REQ1);
				} catch (ActivityNotFoundException exception) {
					Toast.makeText(OcrIdCardActivity.this,
							getResources().getString(R.string.identify_fail),
 Toast.LENGTH_LONG).show();// "未安装API模块，无法进行二维码/身份证识别"
				}
			}
		});

		recog3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				recog3.setEnabled(false);
				Intent intent = new Intent();
				intent.setClassName("com.telpo.tps550.api",
						"com.telpo.tps550.api.ocr.IdCardOcr");
				intent.putExtra("type", false);
				intent.putExtra("isKeepPicture", true);// 是否保存图片
														// true是，false:否，不传入时，默认为否
				intent.putExtra("PictPath", "/sdcard/DCIM/Camera/002.png");// 图片路径，不传入时保存到默认路径/sdcard/OCRPict
				intent.putExtra("PictFormat", "PNG");// 图片格式：JPEG，PNG，WEBP，不传入时默认为PNG格式

				try {
					startActivityForResult(intent, ID_REQ2);
				} catch (ActivityNotFoundException exception) {
					Toast.makeText(OcrIdCardActivity.this,
							getResources().getString(R.string.identify_fail),
 Toast.LENGTH_LONG).show();// "未安装API模块，无法进行二维码/身份证识别"
				}
			}
		});
		
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				show_head_photo=checkedId==R.id.select_show_head_photo_true?true:false;
			}
		});
	}

	@Override
	protected void onResume() {

        setRequestedOrientation(Oriental);
		super.onResume();
		if (!checkPackage("com.telpo.tps550.api")) {
			Toast.makeText(this,
					getResources().getString(R.string.identify_fail),
 Toast.LENGTH_LONG).show();// "未安装API模块，无法进行二维码/身份证识别"
			recog2.setEnabled(false);
			recog3.setEnabled(false);
		} else {
			recog2.setEnabled(true);
			recog3.setEnabled(true);
		}
		if(!show_head_photo){
			imageView.setImageBitmap(null);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ID_REQ1:
			if (resultCode == 0) {
				IdentityInfo info = null;
				try {
					info = (IdentityInfo) data.getSerializableExtra("idInfo");
				} catch (NullPointerException e) {
					Toast.makeText(
							this,
							getResources()
									.getString(R.string.get_id_front_fail),
 Toast.LENGTH_LONG).show();// "获取身份证正面信息失败"
					idInfo1.setText(getResources().getString(R.string.none));// "无"
					recog2.setEnabled(true);
					break;
				}
				if (info != null && info.getName() != null
						&& info.getNo() != null) {
					Toast.makeText(
							this,
							getResources().getString(
									R.string.get_id_front_success),
 Toast.LENGTH_SHORT).show();// "获取身份证正面信息成功"
					idInfo1.setText(getResources()
							.getString(R.string.idcard_xm)
							+ info.getName()
 + // "姓名:"
							"   "
							+ getResources().getString(R.string.idcard_xb)
							+ info.getSex()
							+ //
							"   "
							+ getResources().getString(R.string.idcard_mz)
							+ info.getNation()
 + // "民族:"
							"\n"
							+ getResources().getString(R.string.idcard_csrq)
							+ info.getBorn()
 + // "出生日期: "
							"\n"
							+ getResources().getString(R.string.idcard_dz)
 + "　　"
							+ info.getAddress()
 + // "地址:     "
							"\n"
							+ getResources().getString(R.string.idcard_sfhm)
 + info.getNo()); // "身份证号码:"
					if(show_head_photo & (info.getHead_photo()!=null)){
					imageView.setImageBitmap(BitmapFactory.decodeByteArray(info.getHead_photo(), 0, info.getHead_photo().length));
					}
					recog2.setEnabled(true);
				} else {
					Toast.makeText(
							this,
							getResources()
									.getString(R.string.get_id_front_fail),
 Toast.LENGTH_LONG).show();// "获取身份证正面信息失败"
					idInfo1.setText(getResources().getString(R.string.none));// "无"
					recog2.setEnabled(true);
				}
			} else {
				Toast.makeText(this,
						getResources().getString(R.string.get_id_front_fail),
 Toast.LENGTH_LONG).show();// "获取身份证正面信息失败"
				idInfo1.setText(getResources().getString(R.string.none));// "无"
				recog2.setEnabled(true);
			}
			break;
		case ID_REQ2:
			if (resultCode == 0) {
				IdentityInfo info = null;
				try {
					info = (IdentityInfo) data.getSerializableExtra("idInfo");
				} catch (NullPointerException e) {
					Toast.makeText(
							this,
							getResources().getString(R.string.get_id_back_fail),
 Toast.LENGTH_LONG).show();// "获取身份证反面信息失败"
					idInfo2.setText(getResources().getString(R.string.none));// "无"
					recog3.setEnabled(true);
					break;
				}

				if (info != null && info.getPeriod() != null
						&& info.getApartment() != null) {
					Toast.makeText(
							this,
							getResources().getString(
									R.string.get_id_back_success),
 Toast.LENGTH_SHORT).show();// "获取身份证反面信息成功"
					idInfo2.setText(getResources().getString(
							R.string.idcard_qzjg)
							+ info.getApartment()
							+ "\n"
							+ getResources().getString(R.string.idcard_yxqx)
 + info.getPeriod());// "签证机关: ""有效期限: "
					recog3.setEnabled(true);
				} else {
					Toast.makeText(
							this,
							getResources().getString(R.string.get_id_back_fail),
 Toast.LENGTH_LONG).show();// "获取身份证反面信息失败"
					idInfo2.setText(getResources().getString(R.string.none));// "无"
					recog3.setEnabled(true);
				}
			} else {
				Toast.makeText(this,
						getResources().getString(R.string.get_id_back_fail),
 Toast.LENGTH_LONG).show();// "获取身份证反面信息失败"
				idInfo2.setText(getResources().getString(R.string.none));// "无"
				recog3.setEnabled(true);
			}
			break;
		default:
			break;
		}
	}

	private boolean checkPackage(String packageName) {
		PackageManager manager = this.getPackageManager();
		Intent intent = new Intent().setPackage(packageName);
		List<ResolveInfo> infos = manager.queryIntentActivities(intent,
				PackageManager.GET_INTENT_FILTERS);
		if (infos == null || infos.size() < 1) {
			return false;
		}
		return true;
	}

}
