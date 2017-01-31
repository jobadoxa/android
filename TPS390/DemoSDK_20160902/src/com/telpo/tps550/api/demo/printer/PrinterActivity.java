package com.telpo.tps550.api.demo.printer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.demo.R;
import com.telpo.tps550.api.printer.ThermalPrinter;
import com.telpo.tps550.api.util.StringUtil;
import com.telpo.tps550.api.util.SystemUtil;

public class PrinterActivity extends Activity {

	private static String printVersion;
	private final int NOPAPER = 3;
	private final int LOWBATTERY = 4;
	private final int PRINTVERSION = 5;
	private final int PRINTBARCODE = 6;
	private final int PRINTQRCODE = 7;
	private final int PRINTPAPERWALK = 8;
	private final int PRINTCONTENT = 9;
	private final int CANCELPROMPT = 10;
	private final int PRINTERR = 11;
	private final int OVERHEAT = 12;
	private final int MAKER = 13;
	private final int PRINTPICTURE = 14;
	private final int EXECUTECOMMAND = 15;

	private LinearLayout print_text, print_pic, print_comm;
	private TextView text_index, pic_index, comm_index,textPrintVersion;
	MyHandler handler;
	private EditText editTextLeftDistance,editTextLineDistance,editTextWordFont,editTextPrintGray,
	                 editTextBarcode,editTextQrcode,editTextPaperWalk,editTextContent,
	                 edittext_maker_search_distance,edittext_maker_walk_distance,edittext_input_command;
	private Button buttonBarcodePrint,buttonPaperWalkPrint,buttonContentPrint,buttonQrcodePrint,
	               buttonGetExampleText,buttonGetZhExampleText,buttonClearText,button_maker,
	               button_papercut,button_print_picture,button_execute_command;
	private String Result;
	private Boolean nopaper = false;
	private boolean LowBattery = false;

	public static String barcodeStr;
	public static String qrcodeStr;
	public static int paperWalk;
	public static String printContent;
	private int leftDistance = 0;
	private int lineDistance;
	private int wordFont;
	private int printGray;
	private ProgressDialog progressDialog;
	private final static int MAX_LEFT_DISTANCE = 255;
	ProgressDialog dialog;
	private String picturePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/111.bmp";

	private class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NOPAPER:
				noPaperDlg();
				break;
			case LOWBATTERY:
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(PrinterActivity.this);
				alertDialog.setTitle(R.string.operation_result);
				alertDialog.setMessage(getString(R.string.LowBattery));
				alertDialog.setPositiveButton(getString(R.string.dialog_comfirm), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
					}
				});
				alertDialog.show();
				break;
			case PRINTVERSION:
				dialog.dismiss();
				if (msg.obj.equals("1")) {
					textPrintVersion.setText(printVersion);
				} else {
					Toast.makeText(PrinterActivity.this, R.string.operation_fail, Toast.LENGTH_LONG).show();
				}
				break;
			case PRINTBARCODE:
				new barcodePrintThread().start();
				break;
			case PRINTQRCODE:
				new qrcodePrintThread().start();
				break;
			case PRINTPAPERWALK:
				new paperWalkPrintThread().start();
				break;
			case PRINTCONTENT:
				new contentPrintThread().start();
				break;
			case MAKER:
				new MakerThread().start();
				break;
			case PRINTPICTURE:
				new printPicture().start();
				break;
			case CANCELPROMPT:
				if (progressDialog != null && !PrinterActivity.this.isFinishing()) {
					progressDialog.dismiss();
					progressDialog = null;
				}
				break;
			case EXECUTECOMMAND:
				new executeCommand().start();
				break;
			case OVERHEAT:
				AlertDialog.Builder overHeatDialog = new AlertDialog.Builder(PrinterActivity.this);
				overHeatDialog.setTitle(R.string.operation_result);
				overHeatDialog.setMessage(getString(R.string.overTemp));
				overHeatDialog.setPositiveButton(getString(R.string.dialog_comfirm), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
					}
				});
				overHeatDialog.show();
				break;
			default:
				Toast.makeText(PrinterActivity.this, "Print Error!", Toast.LENGTH_LONG).show();
				break;
			}
		}
	}

	private void initView() {
		print_text = (LinearLayout) findViewById(R.id.print_text);
		print_pic = (LinearLayout) findViewById(R.id.print_code_and_pic);
		print_comm = (LinearLayout) findViewById(R.id.print_comm);
		text_index = (TextView) findViewById(R.id.index_text);
		pic_index = (TextView) findViewById(R.id.index_pic);
		comm_index = (TextView) findViewById(R.id.index_comm);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.print_text);
		initView();
		savepic();
		handler = new MyHandler();
		button_papercut = (Button) findViewById(R.id.button_papercut);
		buttonBarcodePrint = (Button) findViewById(R.id.print_barcode);

		IntentFilter pIntentFilter = new IntentFilter();
		pIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		pIntentFilter.addAction("android.intent.action.BATTERY_CAPACITY_EVENT");
		registerReceiver(printReceive, pIntentFilter);

		editTextLeftDistance = (EditText) findViewById(R.id.set_leftDistance);
		editTextLineDistance = (EditText) findViewById(R.id.set_lineDistance);
		editTextWordFont = (EditText) findViewById(R.id.set_wordFont);
		editTextPrintGray = (EditText) findViewById(R.id.set_printGray);
		editTextBarcode = (EditText) findViewById(R.id.set_Barcode);
		editTextPaperWalk = (EditText) findViewById(R.id.set_paperWalk);
		editTextContent = (EditText) findViewById(R.id.set_content);
		textPrintVersion = (TextView) findViewById(R.id.print_version);
		editTextQrcode = (EditText) findViewById(R.id.set_Qrcode);
		edittext_maker_search_distance = (EditText) findViewById(R.id.edittext_maker_search_distance);
		edittext_maker_walk_distance = (EditText) findViewById(R.id.edittext_maker_walk_distance);
		edittext_input_command = (EditText) findViewById(R.id.edittext_input_command);
		buttonQrcodePrint = (Button) findViewById(R.id.print_qrcode);

		buttonQrcodePrint.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String exditText = editTextPrintGray.getText().toString();
				if (exditText == null || exditText.length() < 1) {
					Toast.makeText(PrinterActivity.this, getString(R.string.gray_level) + getString(R.string.lengthNotEnougth), Toast.LENGTH_LONG).show();
					return;
				}
				printGray = Integer.parseInt(exditText);
				if (printGray < 0 || printGray > 12) {
					Toast.makeText(PrinterActivity.this, getString(R.string.outOfGray), Toast.LENGTH_LONG).show();
					return;
				}
				qrcodeStr = editTextQrcode.getText().toString();
				if (qrcodeStr == null || qrcodeStr.length() == 0) {
					Toast.makeText(PrinterActivity.this, getString(R.string.input_print_data), Toast.LENGTH_SHORT).show();
					return;
				}
				if (LowBattery == true) {
					handler.sendMessage(handler.obtainMessage(LOWBATTERY, 1, 0, null));
				} else {
					if (!nopaper) {
						progressDialog = ProgressDialog.show(PrinterActivity.this, getString(R.string.D_barcode_loading), getString(R.string.generate_barcode_wait));
						handler.sendMessage(handler.obtainMessage(PRINTQRCODE, 1, 0, null));
					} else {
						Toast.makeText(PrinterActivity.this, getString(R.string.ptintInit), Toast.LENGTH_LONG).show();
					}
				}

			}
		});
		editTextContent.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent arg1) {
				v.getParent().requestDisallowInterceptTouchEvent(true);
				return false;
			}
		});
		buttonBarcodePrint.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String exditText = editTextPrintGray.getText().toString();
				if (exditText == null || exditText.length() < 1) {
					Toast.makeText(PrinterActivity.this, getString(R.string.gray_level) + getString(R.string.lengthNotEnougth), Toast.LENGTH_LONG).show();
					return;
				}
				printGray = Integer.parseInt(exditText);
				if (printGray < 0 || printGray > 12) {
					Toast.makeText(PrinterActivity.this, getString(R.string.outOfGray), Toast.LENGTH_LONG).show();
					return;
				}
				barcodeStr = editTextBarcode.getText().toString();
				if (barcodeStr == null || barcodeStr.length() == 0) {
					Toast.makeText(PrinterActivity.this, getString(R.string.empty), Toast.LENGTH_LONG).show();
					return;
				}
				if (LowBattery == true) {
					handler.sendMessage(handler.obtainMessage(LOWBATTERY, 1, 0, null));
				} else {
					if (!nopaper) {
						progressDialog = ProgressDialog.show(PrinterActivity.this, getString(R.string.bl_dy), getString(R.string.printing_wait));
						handler.sendMessage(handler.obtainMessage(PRINTBARCODE, 1, 0, null));
					} else {
						Toast.makeText(PrinterActivity.this, getString(R.string.ptintInit), Toast.LENGTH_LONG).show();
					}
				}
			}
		});

		buttonPaperWalkPrint = (Button) findViewById(R.id.print_paperWalk);
		buttonPaperWalkPrint.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String exditText;
				exditText = editTextPaperWalk.getText().toString();
				if (exditText == null || exditText.length() == 0) {
					Toast.makeText(PrinterActivity.this, getString(R.string.empty), Toast.LENGTH_LONG).show();
					return;
				}
				if (Integer.parseInt(exditText) < 1 || Integer.parseInt(exditText) > 255) {
					Toast.makeText(PrinterActivity.this, getString(R.string.walk_paper_intput_value), Toast.LENGTH_LONG).show();
					return;
				}
				paperWalk = Integer.parseInt(exditText);
				if (LowBattery == true) {
					handler.sendMessage(handler.obtainMessage(LOWBATTERY, 1, 0, null));
				} else {
					if (!nopaper) {
						progressDialog = ProgressDialog.show(PrinterActivity.this, getString(R.string.bl_dy), getString(R.string.printing_wait));
						handler.sendMessage(handler.obtainMessage(PRINTPAPERWALK, 1, 0, null));
					} else {
						Toast.makeText(PrinterActivity.this, getString(R.string.ptintInit), Toast.LENGTH_LONG).show();
					}
				}
			}
		});

		buttonClearText = (Button) findViewById(R.id.clearText);
		buttonClearText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				editTextContent.setText("");
			}
		});
		buttonGetExampleText = (Button) findViewById(R.id.getPrintExample);
		buttonGetExampleText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {			
				String str = "\n-----------------------------\n" +
						     "Print Test:\n" +
			                 "Device Base Information\n" + 
			                 "Printer Version:\n" + 
						     "V05.2.0.3\n" + 
			                 "Printer Gray:3\n" + 
						     "Soft Version:\n"+ 
			                 "Demo.G50.0.Build140313\n" + 
						     "Battery Level:100%\n" + 
			                 "CSQ Value:24\n" + 
						     "IMEI:86378902177527\n" +
			                 "-----------------------------\n" +
			                 "-----------------------------\n" +
						     "Print Test:\n" +
			                 "Device Base Information\n" + 
			                 "Printer Version:\n" + 
						     "V05.2.0.3\n" + 
			                 "Printer Gray:3\n" + 
						     "Soft Version:\n"+ 
			                 "Demo.G50.0.Build140313\n" + 
						     "Battery Level:100%\n" + 
			                 "CSQ Value:24\n" + 
						     "IMEI:86378902177527\n" +
			                 "-----------------------------\n" +
					         "-----------------------------\n" +
						     "Print Test:\n" +
			                 "Device Base Information\n" + 
			                 "Printer Version:\n" + 
						     "V05.2.0.3\n" + 
			                 "Printer Gray:3\n" + 
						     "Soft Version:\n"+ 
			                 "Demo.G50.0.Build140313\n" + 
						     "Battery Level:100%\n" + 
			                 "CSQ Value:24\n" + 
						     "IMEI:86378902177527\n" +
			                 "-----------------------------\n";						
				editTextContent.setText(str);
			}
		});

		buttonGetZhExampleText = (Button) findViewById(R.id.getZhPrintExample);
		buttonGetZhExampleText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String str = "\n             烧烤" + "\n----------------------------" + "\n日期：2015-01-01 16:18:20" + "\n卡号：12378945664" + "\n单号：1001000000000529142" + "\n----------------------------"
						+ "\n  项目    数量   单价  小计" + "\n秘制烤羊腿  1    56    56" + "\n黯然牛排    2    24    48" + "\n烤火鸡      2    50    100" + "\n炭烧鳗鱼    1    40    40" + "\n烤全羊      1    200   200"
						+ "\n荔枝树烧鸡  1    50    50" + "\n冰镇乳鸽    2    23    46" + "\n秘制烤羊腿  1    56    56" + "\n黯然牛排    2    24    48" + "\n烤火鸡      2    50    100" + "\n炭烧鳗鱼    1    40    40"
						+ "\n烤全羊      1    200   200" + "\n荔枝树烧鸡  1    50    50" + "\n冰镇乳鸽    2    23    46" + "\n秘制烤羊腿  1    56    56" + "\n黯然牛排    2    24    48" + "\n烤火鸡      2    50    100"
						+ "\n炭烧鳗鱼    1    40    40" + "\n烤全羊      1    200   200" + "\n荔枝树烧鸡  1    50    50" + "\n冰镇乳鸽    2    23    46" + "\n秘制烤羊腿  1    56    56" + "\n黯然牛排    2    24    48"
						+ "\n烤火鸡      2    50    100" + "\n炭烧鳗鱼    1    40    40" + "\n烤全羊      1    200   200" + "\n荔枝树烧鸡  1    50    50" + "\n冰镇乳鸽    2    23    46" + "\n冰镇乳鸽    2    23    46"
						+ "\n秘制烤羊腿  1    56    56" + "\n黯然牛排    2    24    48" + "\n烤火鸡      2    50    100" + "\n炭烧鳗鱼    1    40    40" + "\n烤全羊      1    200   200" + "\n荔枝树烧鸡  1    50    50"
						+ "\n冰镇乳鸽    2    23    46" + "\n 合计：1000：00元" + "\n----------------------------" + "\n本卡金额：10000.00" + "\n累计消费：1000.00" + "\n本卡结余：9000.00" + "\n----------------------------"
						+ "\n 地址：广东省佛山市南海区桂城街道桂澜南路45号鹏瑞利广场A317.B-18号铺" + "\n欢迎您的再次光临\n";
				editTextContent.setText(str);
			}
		});

		buttonContentPrint = (Button) findViewById(R.id.print_content);
		buttonContentPrint.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String exditText;
				exditText = editTextLeftDistance.getText().toString();
				if (exditText == null || exditText.length() < 1) {
					Toast.makeText(PrinterActivity.this, getString(R.string.left_margin) + getString(R.string.lengthNotEnougth), Toast.LENGTH_LONG).show();
					return;
				}
				leftDistance = Integer.parseInt(exditText);
				exditText = editTextLineDistance.getText().toString();
				if (exditText == null || exditText.length() < 1) {
					Toast.makeText(PrinterActivity.this, getString(R.string.row_space) + getString(R.string.lengthNotEnougth), Toast.LENGTH_LONG).show();
					return;
				}
				lineDistance = Integer.parseInt(exditText);
				printContent = editTextContent.getText().toString();
				exditText = editTextWordFont.getText().toString();
				if (exditText == null || exditText.length() < 1) {
					Toast.makeText(PrinterActivity.this, getString(R.string.font_size) + getString(R.string.lengthNotEnougth), Toast.LENGTH_LONG).show();
					return;
				}
				wordFont = Integer.parseInt(exditText);
				exditText = editTextPrintGray.getText().toString();
				if (exditText == null || exditText.length() < 1) {
					Toast.makeText(PrinterActivity.this, getString(R.string.gray_level) + getString(R.string.lengthNotEnougth), Toast.LENGTH_LONG).show();
					return;
				}
				printGray = Integer.parseInt(exditText);
				if (leftDistance > MAX_LEFT_DISTANCE) {
					Toast.makeText(PrinterActivity.this, getString(R.string.outOfLeft), Toast.LENGTH_LONG).show();
					return;
				} else if (lineDistance > 255) {
					Toast.makeText(PrinterActivity.this, getString(R.string.outOfLine), Toast.LENGTH_LONG).show();
					return;
				} else if (wordFont > 4 || wordFont < 1) {
					Toast.makeText(PrinterActivity.this, getString(R.string.outOfFont), Toast.LENGTH_LONG).show();
					return;
				} else if (printGray < 0 || printGray > 12) {
					Toast.makeText(PrinterActivity.this, getString(R.string.outOfGray), Toast.LENGTH_LONG).show();
					return;
				}
				if (printContent == null || printContent.length() == 0) {
					Toast.makeText(PrinterActivity.this, getString(R.string.empty), Toast.LENGTH_LONG).show();
					return;
				}
				if (LowBattery == true) {
					handler.sendMessage(handler.obtainMessage(LOWBATTERY, 1, 0, null));
				} else {
					if (!nopaper) {
						progressDialog = ProgressDialog.show(PrinterActivity.this, getString(R.string.bl_dy), getString(R.string.printing_wait));
						handler.sendMessage(handler.obtainMessage(PRINTCONTENT, 1, 0, null));
					} else {
						Toast.makeText(PrinterActivity.this, getString(R.string.ptintInit), Toast.LENGTH_LONG).show();
					}
				}

			}
		});

		button_maker = (Button) findViewById(R.id.button_maker);
		button_maker.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (edittext_maker_search_distance.getText().length() == 0 || edittext_maker_walk_distance.getText().length() == 0) {
					Toast.makeText(PrinterActivity.this, getString(R.string.maker_error), Toast.LENGTH_LONG).show();
					return;
				}
				if (Integer.parseInt(edittext_maker_search_distance.getText().toString()) < 0 || Integer.parseInt(edittext_maker_search_distance.getText().toString()) > 255) {
					Toast.makeText(PrinterActivity.this, getString(R.string.maker_error), Toast.LENGTH_LONG).show();
					return;
				}
				if (Integer.parseInt(edittext_maker_walk_distance.getText().toString()) < 0 || Integer.parseInt(edittext_maker_walk_distance.getText().toString()) > 255) {
					Toast.makeText(PrinterActivity.this, getString(R.string.maker_error), Toast.LENGTH_LONG).show();
					return;
				}
				if (LowBattery == true) {
					handler.sendMessage(handler.obtainMessage(LOWBATTERY, 1, 0, null));
				} else {
					if (!nopaper) {
						progressDialog = ProgressDialog.show(PrinterActivity.this, getString(R.string.maker), getString(R.string.printing_wait));
						handler.sendMessage(handler.obtainMessage(MAKER, 1, 0, null));
					} else {
						Toast.makeText(PrinterActivity.this, getString(R.string.ptintInit), Toast.LENGTH_LONG).show();
					}
				}
			}
		});

		button_papercut.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							ThermalPrinter.start(PrinterActivity.this);
							ThermalPrinter.reset();
							ThermalPrinter.paperCut();							
						} catch (Exception e) {
							e.printStackTrace();
						}finally{
							ThermalPrinter.stop(PrinterActivity.this);
						}
					}
				}).start();
			}
		});

		button_print_picture = (Button) findViewById(R.id.button_print_picture);
		button_print_picture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String exditText = editTextPrintGray.getText().toString();
				if (exditText == null || exditText.length() < 1) {
					Toast.makeText(PrinterActivity.this, getString(R.string.gray_level) + getString(R.string.lengthNotEnougth), Toast.LENGTH_LONG).show();
					return;
				}
				printGray = Integer.parseInt(exditText);
				if (printGray < 0 || printGray > 12) {
					Toast.makeText(PrinterActivity.this, getString(R.string.outOfGray), Toast.LENGTH_LONG).show();
					return;
				}
				if (LowBattery == true) {
					handler.sendMessage(handler.obtainMessage(LOWBATTERY, 1, 0, null));
				} else {
					if (!nopaper) {
						progressDialog = ProgressDialog.show(PrinterActivity.this, getString(R.string.bl_dy), getString(R.string.printing_wait));
						handler.sendMessage(handler.obtainMessage(PRINTPICTURE, 1, 0, null));
					} else {
						Toast.makeText(PrinterActivity.this, getString(R.string.ptintInit), Toast.LENGTH_LONG).show();
					}
				}
			}
		});

		button_execute_command = (Button) findViewById(R.id.button_execute_command);
		button_execute_command.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (edittext_input_command.getText().toString() == null || edittext_input_command.getText().toString().length() == 0) {
					Toast.makeText(PrinterActivity.this, getString(R.string.empty), Toast.LENGTH_LONG).show();
					return;
				}
				if (LowBattery == true) {
					handler.sendMessage(handler.obtainMessage(LOWBATTERY, 1, 0, null));
				} else {
					if (!nopaper) {
						progressDialog = ProgressDialog.show(PrinterActivity.this, getString(R.string.bl_dy), getString(R.string.printing_wait));
						handler.sendMessage(handler.obtainMessage(EXECUTECOMMAND, 1, 0, null));
					} else {
						Toast.makeText(PrinterActivity.this, getString(R.string.ptintInit), Toast.LENGTH_LONG).show();
					}
				}
			}
		});

		dialog = new ProgressDialog(PrinterActivity.this);
		dialog.setTitle(R.string.idcard_czz);
		dialog.setMessage(getText(R.string.watting));
		dialog.setCancelable(false);
		dialog.show();

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					ThermalPrinter.start(PrinterActivity.this);
					ThermalPrinter.reset();
					printVersion = ThermalPrinter.getVersion();
				} catch (TelpoException e) {
					e.printStackTrace();
				} finally {
					if (printVersion != null) {
						Message message = new Message();
						message.what = PRINTVERSION;
						message.obj = "1";
						handler.sendMessage(message);
					} else {
						Message message = new Message();
						message.what = PRINTVERSION;
						message.obj = "0";
						handler.sendMessage(message);
					}
					ThermalPrinter.stop(PrinterActivity.this);
				}
			}
		}).start();

	}

	/* Called when the application resumes */
	@Override
	protected void onResume() {
		super.onResume();
	}

	private final BroadcastReceiver printReceive = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
				int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_NOT_CHARGING);
				int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
				int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
				//TPS390 can not print,while in low battery,whether is charging or not charging
				if(SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS390.ordinal()){
					if (level * 5 <= scale) {
						LowBattery = true;
					} else {
						LowBattery = false;
					}
				}else {
					if (status != BatteryManager.BATTERY_STATUS_CHARGING) {
						if (level * 5 <= scale) {
							LowBattery = true;
						} else {
							LowBattery = false;
						}
				  } else {
					        LowBattery = false;
				        }
				  }
			}
			//Only use for TPS550MTK devices
			else if (action.equals("android.intent.action.BATTERY_CAPACITY_EVENT")) {
				int status = intent.getIntExtra("action", 0);
				int level = intent.getIntExtra("level", 0);
				if(status == 0){					
					if(level < 1){
						LowBattery = true;
					}else {
						LowBattery = false;
					}
				}else {
					LowBattery = false;
				}
			}
		}
	};

	private void noPaperDlg() {
		AlertDialog.Builder dlg = new AlertDialog.Builder(PrinterActivity.this);
		dlg.setTitle(getString(R.string.noPaper));
		dlg.setMessage(getString(R.string.noPaperNotice));
		dlg.setCancelable(false);
		dlg.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				ThermalPrinter.stop(PrinterActivity.this);
			}
		});
		dlg.show();
	}

	private class paperWalkPrintThread extends Thread {
		@Override
		public void run() {
			super.run();
			try {
				ThermalPrinter.start(PrinterActivity.this);
				ThermalPrinter.reset();
				ThermalPrinter.walkPaper(paperWalk);
			} catch (Exception e) {
				e.printStackTrace();
				Result = e.toString();
				if (Result.equals("com.telpo.tps550.api.printer.NoPaperException")) {
					nopaper = true;
				} else if (Result.equals("com.telpo.tps550.api.printer.OverHeatException")) {
					handler.sendMessage(handler.obtainMessage(OVERHEAT, 1, 0, null));
				} else {
					handler.sendMessage(handler.obtainMessage(PRINTERR, 1, 0, null));
				}
			} finally {
				handler.sendMessage(handler.obtainMessage(CANCELPROMPT, 1, 0, null));
				if (nopaper){
					handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
					nopaper = false;
					return;
				}					
				ThermalPrinter.stop(PrinterActivity.this);
			}
		}
	}

	private class barcodePrintThread extends Thread {
		@Override
		public void run() {
			super.run();
			try {
				ThermalPrinter.start(PrinterActivity.this);
				ThermalPrinter.reset();
				ThermalPrinter.setGray(printGray);
				Bitmap bitmap = CreateCode(barcodeStr, BarcodeFormat.CODE_128, 320, 176);
				if(bitmap != null){
					ThermalPrinter.printLogo(bitmap);
				}
				ThermalPrinter.addString(barcodeStr);
				ThermalPrinter.printString();
				ThermalPrinter.walkPaper(100);			
			} catch (Exception e) {
				e.printStackTrace();
				Result = e.toString();
				if (Result.equals("com.telpo.tps550.api.printer.NoPaperException")) {
					nopaper = true;
				} else if (Result.equals("com.telpo.tps550.api.printer.OverHeatException")) {
					handler.sendMessage(handler.obtainMessage(OVERHEAT, 1, 0, null));
				} else {
					handler.sendMessage(handler.obtainMessage(PRINTERR, 1, 0, null));
				}
			} finally {
				handler.sendMessage(handler.obtainMessage(CANCELPROMPT, 1, 0, null));
				if (nopaper){
					handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
					nopaper = false;
					return;
				}
				ThermalPrinter.stop(PrinterActivity.this);
			}
		}
	}

	private class qrcodePrintThread extends Thread {
		@Override
		public void run() {
			super.run();
			try {
				ThermalPrinter.start(PrinterActivity.this);
				ThermalPrinter.reset();
				ThermalPrinter.setGray(printGray);
				Bitmap bitmap = CreateCode(qrcodeStr, BarcodeFormat.QR_CODE, 256, 256);
				if(bitmap != null){
					ThermalPrinter.printLogo(bitmap);
				}			
				ThermalPrinter.addString(qrcodeStr);
				ThermalPrinter.printString();
				ThermalPrinter.walkPaper(100);
			} catch (Exception e) {
				e.printStackTrace();
				Result = e.toString();
				if (Result.equals("com.telpo.tps550.api.printer.NoPaperException")) {
					nopaper = true;
				} else if (Result.equals("com.telpo.tps550.api.printer.OverHeatException")) {
					handler.sendMessage(handler.obtainMessage(OVERHEAT, 1, 0, null));
				} else {
					handler.sendMessage(handler.obtainMessage(PRINTERR, 1, 0, null));
				}
			} finally {
				handler.sendMessage(handler.obtainMessage(CANCELPROMPT, 1, 0, null));
				if (nopaper){
					handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
					nopaper = false;
					return;
				}
				ThermalPrinter.stop(PrinterActivity.this);
			}
		}
	}

	private class contentPrintThread extends Thread {
		@Override
		public void run() {
			super.run();
			try {
				ThermalPrinter.start(PrinterActivity.this);
				ThermalPrinter.reset();
				ThermalPrinter.setAlgin(ThermalPrinter.ALGIN_LEFT);
				ThermalPrinter.setLeftIndent(leftDistance);
				ThermalPrinter.setLineSpace(lineDistance);
				if (wordFont == 4) {
					ThermalPrinter.setFontSize(2);
					ThermalPrinter.enlargeFontSize(2, 2);
				} else if (wordFont == 3) {
					ThermalPrinter.setFontSize(1);
					ThermalPrinter.enlargeFontSize(2, 2);
				} else if (wordFont == 2) {
					ThermalPrinter.setFontSize(2);
				} else if (wordFont == 1) {
					ThermalPrinter.setFontSize(1);
				}
				ThermalPrinter.setGray(printGray);
				ThermalPrinter.addString(printContent);
				ThermalPrinter.printString();
				ThermalPrinter.walkPaper(100);
			} catch (Exception e) {
				e.printStackTrace();
				Result = e.toString();
				if (Result.equals("com.telpo.tps550.api.printer.NoPaperException")) {
					nopaper = true;
				} else if (Result.equals("com.telpo.tps550.api.printer.OverHeatException")) {
					handler.sendMessage(handler.obtainMessage(OVERHEAT, 1, 0, null));
				} else {
					handler.sendMessage(handler.obtainMessage(PRINTERR, 1, 0, null));
				}
			} finally {
				handler.sendMessage(handler.obtainMessage(CANCELPROMPT, 1, 0, null));
				if (nopaper){
					handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
					nopaper = false;
					return;
				}
				ThermalPrinter.stop(PrinterActivity.this);
			}
		}
	}

	private class MakerThread extends Thread {

		@Override
		public void run() {
			super.run();
			try {
				ThermalPrinter.start(PrinterActivity.this);
				ThermalPrinter.reset();
				ThermalPrinter.searchMark(Integer.parseInt(edittext_maker_search_distance.getText().toString()),
					Integer.parseInt(edittext_maker_walk_distance.getText().toString()));
			} catch (Exception e) {
				e.printStackTrace();
				Result = e.toString();
				if (Result.equals("com.telpo.tps550.api.printer.NoPaperException")) {
					nopaper = true;
				} else if (Result.equals("com.telpo.tps550.api.printer.OverHeatException")) {
					handler.sendMessage(handler.obtainMessage(OVERHEAT, 1, 0, null));
				} else {
					handler.sendMessage(handler.obtainMessage(PRINTERR, 1, 0, null));
				}
			} finally {
				handler.sendMessage(handler.obtainMessage(CANCELPROMPT, 1, 0, null));
				if (nopaper){
					handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
					nopaper = false;
					return;
				}
				ThermalPrinter.stop(PrinterActivity.this);
			}
		}
	}

	private class printPicture extends Thread {

		@Override
		public void run() {
			super.run();
			try {
				ThermalPrinter.start(PrinterActivity.this);
				ThermalPrinter.reset();
				ThermalPrinter.setGray(printGray);
				ThermalPrinter.setAlgin(ThermalPrinter.ALGIN_MIDDLE);
				File file = new File(picturePath);
				if (file.exists()) {
					ThermalPrinter.printLogo(BitmapFactory.decodeFile(picturePath));
					ThermalPrinter.walkPaper(100);
				} else {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							Toast.makeText(PrinterActivity.this, getString(R.string.not_find_picture), Toast.LENGTH_LONG).show();
						}
					});
				}
			} catch (Exception e) {
				e.printStackTrace();
				Result = e.toString();
				if (Result.equals("com.telpo.tps550.api.printer.NoPaperException")) {
					nopaper = true;
				} else if (Result.equals("com.telpo.tps550.api.printer.OverHeatException")) {
					handler.sendMessage(handler.obtainMessage(OVERHEAT, 1, 0, null));
				} else {
					handler.sendMessage(handler.obtainMessage(PRINTERR, 1, 0, null));
				}
			} finally {
				handler.sendMessage(handler.obtainMessage(CANCELPROMPT, 1, 0, null));
				if (nopaper){
					handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
					nopaper = false;
					return;
				}
				ThermalPrinter.stop(PrinterActivity.this);
			}
		}
	}

	private class executeCommand extends Thread {

		@Override
		public void run() {
			super.run();
			try {
				ThermalPrinter.start(PrinterActivity.this);
				ThermalPrinter.reset();
				ThermalPrinter.sendCommand(edittext_input_command.getText().toString());
			} catch (Exception e) {
				e.printStackTrace();
				Result = e.toString();
				if (Result.equals("com.telpo.tps550.api.printer.NoPaperException")) {
					nopaper = true;
				} else if (Result.equals("com.telpo.tps550.api.printer.OverHeatException")) {
					handler.sendMessage(handler.obtainMessage(OVERHEAT, 1, 0, null));
				} else {
					handler.sendMessage(handler.obtainMessage(PRINTERR, 1, 0, null));
				}
			} finally {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				handler.sendMessage(handler.obtainMessage(CANCELPROMPT, 1, 0, null));
				if (nopaper){
					handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null));
					nopaper = false;
					return;
				}
				ThermalPrinter.stop(PrinterActivity.this);
			}
		}

	}

	@Override
	protected void onDestroy() {
		if (progressDialog != null && !PrinterActivity.this.isFinishing()) {
			progressDialog.dismiss();
			progressDialog = null;
		}
		unregisterReceiver(printReceive);
		ThermalPrinter.stop();
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 生成条码
	 * 
	 * @param str
	 *            条码内容
	 * @param type
	 *            条码类型： AZTEC, CODABAR, CODE_39, CODE_93, CODE_128, DATA_MATRIX,
	 *            EAN_8, EAN_13, ITF, MAXICODE, PDF_417, QR_CODE, RSS_14,
	 *            RSS_EXPANDED, UPC_A, UPC_E, UPC_EAN_EXTENSION;
	 * @param bmpWidth
	 *            生成位图宽,宽不能大于384，不然大于打印纸宽度
	 * @param bmpHeight
	 *            生成位图高，8的倍数
	 */

	public Bitmap CreateCode(String str, com.google.zxing.BarcodeFormat type, int bmpWidth, int bmpHeight) throws WriterException {
		// 生成二维矩阵,编码时要指定大小,不要生成了图片以后再进行缩放,以防模糊导致识别失败
		BitMatrix matrix = new MultiFormatWriter().encode(str, type, bmpWidth, bmpHeight);
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		// 二维矩阵转为一维像素数组（一直横着排）
		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (matrix.get(x, y)) {
					pixels[y * width + x] = 0xff000000;
				} else {
					pixels[y * width + x] = 0xffffffff;
				}
			}
		}
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		// 通过像素数组生成bitmap,具体参考api
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	public void selectIndex(View view) {
		switch (view.getId()) {
		case R.id.index_text:
			text_index.setEnabled(false);
			pic_index.setEnabled(true);
			comm_index.setEnabled(true);
			print_text.setVisibility(View.VISIBLE);
			print_pic.setVisibility(View.GONE);
			print_comm.setVisibility(View.GONE);

			break;

		case R.id.index_pic:

			text_index.setEnabled(true);
			pic_index.setEnabled(false);
			comm_index.setEnabled(true);
			print_text.setVisibility(View.GONE);
			print_pic.setVisibility(View.VISIBLE);
			print_comm.setVisibility(View.GONE);
			break;
		case R.id.index_comm:
			text_index.setEnabled(true);
			pic_index.setEnabled(true);
			comm_index.setEnabled(false);
			print_text.setVisibility(View.GONE);
			print_pic.setVisibility(View.GONE);
			print_comm.setVisibility(View.VISIBLE);

			break;
		}
	}
	
	private void savepic() {
		File file = new File(picturePath);
		if (!file.exists()) {
			InputStream inputStream = null;
			FileOutputStream fos = null;
			byte[] tmp = new byte[1024];
			try {
				inputStream = getApplicationContext().getAssets().open("syhlogo.png");
				fos = new FileOutputStream(file);
				int length = 0;
				while((length = inputStream.read(tmp)) > 0){
					fos.write(tmp, 0, length);
				}
				fos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					inputStream.close();
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
