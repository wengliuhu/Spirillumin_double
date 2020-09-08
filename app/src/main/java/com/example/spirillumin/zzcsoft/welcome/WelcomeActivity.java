package com.example.spirillumin.zzcsoft.welcome;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.spirillumin.R;
import com.example.spirillumin.zzcsoft.spirilluminspection.BaseApplication;
import com.example.spirillumin.zzcsoft.spirilluminspection.MainActivity;
import com.example.spirillumin.zzcsoft.utils.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WelcomeActivity extends AppCompatActivity {
	protected Logger mLogger = LoggerFactory.getLogger(this.getClass());

	private Animation animation;
	private ImageView imageView;
	private TextView textViewWelcomeMsg, textViewWelcomeTitle;
	private BaseApplication baseApp;
	private CountDownTimer timer;

	private Date inspectionDate, selfAdaptionDate;

	private EditText dataView;

	private BaseApplication.SerialControl0 com0;
	// 打印机
	private BaseApplication.SerialControl com1;
	private BaseApplication.SerialControl com2;
	private String reset;
	private String manager;
	private String managerContext;
	private String chaoChu;
	private String startflag;
	private double chaZhi;
	private boolean isCountDown;
	private int state = 0;
	private int fristState = 0;
	private int inspectionDateNum, selfAdaptionNum;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_welcome);

		try {

			animation = AnimationUtils.loadAnimation(this, R.anim.load_anim);
			textViewWelcomeMsg = (TextView) findViewById(R.id.textViewWelcomeMsg);
			textViewWelcomeTitle = (TextView) findViewById(R.id.textViewWelcomeTitle);

			dataView = (EditText) findViewById(R.id.dataView);
			imageView = (ImageView) findViewById(R.id.imageViewLoading);
			imageView.startAnimation(animation);

			baseApp = BaseApplication.getInstance();
			baseApp.setOnSerialportComListener(serialportCom);

			textViewWelcomeTitle
					.setText(baseApp.getApplicationTitle().equals("单击此处编辑") ? "" : baseApp.getApplicationTitle());

			SimpleDateFormat standbyDateFormat = new SimpleDateFormat("HH:mm:ss");
			inspectionDate = standbyDateFormat.parse(baseApp.getInspectionTime());
			inspectionDateNum = ((inspectionDate.getHours() * 60) + inspectionDate.getMinutes()) * 60
					+ inspectionDate.getSeconds();

			selfAdaptionDate = standbyDateFormat.parse(baseApp.getSelfAdaption());
			selfAdaptionNum = ((selfAdaptionDate.getHours() * 60) + selfAdaptionDate.getMinutes()) * 60
					+ selfAdaptionDate.getSeconds();

			Intent dataIntent = getIntent();
			if (dataIntent != null) {
				reset = dataIntent.getStringExtra("reSet");
			}

			Intent stflag = getIntent();
			if (stflag != null) {
				startflag = stflag.getStringExtra("startflag");
				if(startflag == "1"){
					manager = "AA01220023ED";
					SendSerialPort("AA01220023ED"); //开始环境自适应
					state = 1;

				}

			}


			com0 = baseApp.getCom0();
			com1 = baseApp.getCom1();
			com2 = baseApp.getCom2();

			baseApp.setOnSerialport0ComListener(new BaseApplication.SerialportCom0() {

				@Override
				public void SerialportListener(String comStr) {
					if(comStr.indexOf("sample-s_getstate_sample-e") != -1){
						com0.sendTxt("sample-s_0,0_sample-e");
					}
				}
			});

			new Thread() {
				@Override
				public void run() {
					try {
						if (!com1.isOpen()) {
							Thread.sleep(4000);
							com1.setPort("/dev/ttyS3");
							com1.setBaudRate(9600);
							com1.open();
						}

						if (!com0.isOpen()) {
							Thread.sleep(1000);
							com0.setPort("/dev/ttyS0");
							com0.setBaudRate(9600);
							com0.open();
							Thread.sleep(1000);
						}

						if (baseApp.isFrist()) {
							// 环境自适应

							// saveTestSetting();

							Thread.sleep(1000);
							manager = "AA01220023ED";
							SendSerialPort("AA01220023ED");
							state = 1;
						} else {
							// 开机自检
							inspection();
						}
					} catch (Exception e) {
						String errorString = e.getMessage();
					} finally {
						saveImgToLocl("inspection.png");
						saveImgToLocl("icon.png");
					}
				}
			}.start();
		} catch (Exception ex) {

		}
	}

	private void saveImgToLocl(String picture) {
		try {
			String path = Environment.getExternalStorageDirectory().getAbsolutePath()
					+ getResources().getString(R.string.cache_path) + "picture/" + picture;

			File file = new File(path);

			if (file == null || !file.exists()) {
				Utils util = new Utils();
				util.fileCreate(path);

				InputStream is = getAssets().open("img/" + picture);

				Bitmap image = BitmapFactory.decodeStream(is);
				FileOutputStream out = new FileOutputStream(path);

				image.compress(Bitmap.CompressFormat.PNG, 100, out);
				out.flush();
				out.close();
				is.close();
			}
		} catch (Exception ex) {

		}
	}

	BaseApplication.SerialportCom serialportCom = new BaseApplication.SerialportCom() {

		@Override
		public void SerialportListener(String comStr) {
			Message msg = handler.obtainMessage();

			//字符串截取操作
			if (TextUtils.isEmpty(comStr)) return;
			if (!comStr.contains("DD")){
				mLogger.debug("---获取的数据异常--{}", comStr);
				return;
			}
			comStr = comStr.substring(comStr.indexOf("DD"));

			msg.obj = comStr;
			msg.arg1 = 99;

			try {
				if (comStr.length() > 0) {
					if (comStr.subSequence(0, 1).equals(" ")) {
						comStr = comStr.substring(1, comStr.length());
					}
				}

				if(comStr.length() >= 5){ //新增数据长度限制
					String[] datas = comStr.split(" ");
					String comManager = datas[2];
					if (comManager.equals("1C")) {
						SendSerialPort(manager);
					} else {
						int cmd = Integer.parseInt(comManager.trim());
						int dataLength = Integer.parseInt(datas[1].trim());

						switch (cmd) {
							case 10:
								// 单通道 DD 02 10 00 A0 B2
								String manager = "";
								for (int i = 0; i < dataLength; i++) {
									manager += datas[3 + i];
								}

								SendSerialPort("AA01110010ED");
								baseApp.PrintInspection(1);

								double managerBenDi = Integer.parseInt(manager, 16);
								double bendiA = Integer.parseInt(baseApp.getBendiA());

								chaZhi = difference(managerBenDi, bendiA);
								double bendiLiangXian = Integer.parseInt(baseApp.getBendiceliangzhi());

								if (bendiLiangXian < chaZhi && state == 0) {
									state = 1;
									fristState = 1;
									baseApp.setFrist("0");
									SendSerialPort("AA01220023ED");
									msg.arg1 = 4;
								} else {
									sendStart();
								}

								break;
							case 12:
								if (baseApp.isFrist()) {
									baseApp.setFrist("1");
									state = 0;
									String passageway = "";
									for (int i = 0; i < dataLength; i++) {
										passageway += datas[3 + i];
									}
									baseApp.setBendiA(Integer.parseInt(passageway, 16) + "");

									SendSerialPort("AA01130012ED");
									baseApp.PrintInspection(0);

									inspection();
									msg.arg1 = 0;
								}
								break;
							case 23:
								msg.arg1 = 3;
								break;
							case 26:
								msg.arg1 = 0;
								break;
							case 31:
								msg.arg1 = 2;
								break;
							case 50:  //判断数据长度
								int new1_len = comStr.length();

								if(new1_len == 24){ //新增长度判断

									// 双通道 DD 04 50 00 A0 00 00 F4 ED
									String manager1 = "";
									String manager2 = "";

									for (int i = 0; i < dataLength / 2; i++) {
										manager1 += datas[3 + i];
									}

									for (int i = 0; i < dataLength / 2; i++) {
										manager2 += datas[5 + i];
									}

									SendSerialPort("AA01110010ED");



									baseApp.PrintInspection(2);  //长度固定24

									double managerBenDi1 = Integer.parseInt(manager1, 16);
									double bendiA1 = Integer.parseInt(baseApp.getBendiA());

									chaZhi = difference(managerBenDi1, bendiA1);
									double bendiLiangXian1 = Integer.parseInt(baseApp.getBendiceliangzhi());

									if (bendiLiangXian1 < chaZhi && state == 0) {
										state = 1;
										fristState = 1;
										baseApp.setFrist("0");
										SendSerialPort("AA01220023ED");
										msg.arg1 = 6;
										chaoChu = "本底A";
									} else {
										double managerBenDi2 = Integer.parseInt(manager2, 16);
										double bendiA2 = Integer.parseInt(baseApp.getBendiB());

										chaZhi = difference(managerBenDi2, bendiA2);
										double bendiLiangXian2 = Integer.parseInt(baseApp.getBendiceliangzhi());

										if (bendiLiangXian2 < chaZhi && state == 0) {
											state = 1;
											fristState = 1;
											baseApp.setFrist("0");
											SendSerialPort("AA01220023ED");
											msg.arg1 = 5;
											chaoChu = "本底B";
										} else {
											//同步参数到上位机
											baseApp.SendParmasToPc();
											sendStart();
										}
									}

								}

								break;
							case 51: //判断数据长度
								//DD 04 50 00 A0 00 00 F4

								int new2_len = comStr.length(); //长度固定 24

								if(new2_len == 24){

									if (baseApp.isFrist()) {
										baseApp.setFrist("1");
										state = 0;
										String passageway1 = "";
										String passageway2 = "";

										for (int i = 0; i < dataLength / 2; i++) {
											passageway1 += datas[3 + i];
										}

										for (int i = 0; i < dataLength / 2; i++) {
											passageway2 += datas[5 + i];
										}

										baseApp.setBendiA(Integer.parseInt(passageway1, 16) + "");
										baseApp.setBendiB(Integer.parseInt(passageway2, 16) + "");

										SendSerialPort("AA01130012ED");

										baseApp.PrintInspection(3);
										Thread.sleep(1000);
										inspection();
										msg.arg1 = 0;
									}

								}
								break;
							case 93:  //判断数据长度
								// DD 07 93 14 12 03 1C 0E 11 0C AA ED

								int new3_len = comStr.length();  //长度固定 33

								if(new3_len == 33){

									String yearA = Integer.parseInt(datas[3], 16) + "";
									String yearB = Integer.parseInt(datas[4], 16) + "";
									String month = Integer.parseInt(datas[5], 16) + "";
									String day = Integer.parseInt(datas[6], 16) + "";

									String hour = Integer.parseInt(datas[7], 16) + "";
									String minute = Integer.parseInt(datas[8], 16) + "";
									String ss = Integer.parseInt(datas[9], 16) + "";

									baseApp.setSysDateTime(yearA + yearB + "-" + (month.length() == 1 ? "0" + month : month) + "-"
											+ (day.length() == 1 ? "0" + day : day) + " " + (hour.length() == 1 ? "0" + hour : hour)
											+ ":" + (minute.length() == 1 ? "0" + minute : minute) + ":"
											+ (ss.length() == 1 ? "0" + ss : ss));



									msg.arg1 = 1;
									break;
								}

						}
					}
				}//新增数据长度限制
			} catch (Exception e) {
				msg.arg1 = 9;
				msg.obj = e.getMessage() + "\r\n" + comStr;
			} finally {
				handler.sendMessage(msg);
			}
		}
	};

	private void SendSerialPort(String portData) {
		Message msg = handler.obtainMessage();
		msg.arg1 = 99;
		com1.sendHex(portData);
		msg.obj = portData;
		handler.sendMessage(msg);
	}

	private void CountDown(int type) {
		if (!isCountDown) {
			isCountDown = true;
			int timeInt = 0;
			if (type == 0) {
				timeInt = selfAdaptionNum;
			} else {
				timeInt = inspectionDateNum;
			}

			timer = new CountDownTimer(timeInt * 1000, 1000) {
				@Override
				public void onTick(long millisUntilFinished) {
					textViewWelcomeMsg.setText(managerContext + " , 还剩 " + millisUntilFinished / 1000 + " 秒");
				}

				@Override
				public void onFinish() {
					isCountDown = false;
				}
			}.start();
		}
	}

	private void sendStart() {
		try {
			if (com1.isOpen()) {
				//SendSerialPort("AA01110010ED");
				Thread.sleep(1000);
				SendSerialPort("AA01920093ED");
			}
		} catch (Exception e) {

		}
	}

	private void saveTestSetting() {
		try {
			SimpleDateFormat standbyDateFormat = new SimpleDateFormat("HH:mm");
			Date inspectionDate = standbyDateFormat.parse(baseApp.getInspectionTime());
			int inspectionDateNum = ((inspectionDate.getHours() * 60) + inspectionDate.getMinutes()) * 60
					+ inspectionDate.getSeconds();
			inspectionDateNum = 100;
			int a = 11;
			int b = 20;
			int c = 3;
			int d = 232;
			int e = 0;
			int f = inspectionDateNum;
			int g = 5;
			int h = 15;
			int i = 0;
			int j = 0;
			int k = 0;
			int l = 0;
			int m = 65;
			int n = a ^ b ^ c ^ d ^ e ^ f ^ g ^ h ^ i ^ j ^ k ^ l ^ m;

			String inspectionDateStr = Integer.toHexString(inspectionDateNum).toUpperCase();

			switch (inspectionDateStr.length()) {
				case 1:
					inspectionDateStr = "000" + inspectionDateStr;
					break;
				case 2:
					inspectionDateStr = "00" + inspectionDateStr;
					break;
				case 3:
					inspectionDateStr = "0" + inspectionDateStr;
					break;
				case 4:
					break;
			}

			SendSerialPort(
					"AA0B1403E8" + inspectionDateStr + "050F0000000041" + Integer.toHexString(n).toUpperCase() + "ED");
		} catch (Exception e) {

		}
	}

	private void inspection() {
		try {
			if (!com2.isOpen()) {
				com2.setPort("/dev/ttyS2");
				com2.setBaudRate(9600);
				com2.open();

				com2.sendHex("1D2101");
				com2.sendHex("1B3901");
			}

			manager = "AA01280029ED";
			SendSerialPort("AA01280029ED");
		} catch (Exception e) {

		}
	}

	private double difference(double one, double two) {
		if (one > two)
			return one - two;
		else
			return two - one;
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			try {
				switch (msg.arg1) {
					case 0:
						CountDown(1);
						managerContext = "系统自检中...";
						textViewWelcomeMsg.setText(managerContext);
						textViewWelcomeMsg.setTextColor(Color.BLUE);
						break;
					case 1:

						StartMainActivity();
						break;
					case 2:
						if (timer != null) {
							timer.cancel();
							timer = null;
						}
						isCountDown = false;
						if (baseApp.isFrist()) {
							managerContext = "环境自适应...";
						} else {
							managerContext = "系统自检中...";
						}
						textViewWelcomeMsg.setText("检测异常，请拔出呼吸卡");
						textViewWelcomeMsg.setTextColor(Color.RED);
						com0.sendTxt("check-s_inputabnormal_check-e"); // 通知上位机呼吸卡取出异常
						break;
					case 3:
						if (baseApp.isFrist()) {
							CountDown(0);
						}
						if (fristState == 1) {
							managerContext = "本底值(" + baseApp.getBendiA() + ")，超出量限，重新自适应...";
							textViewWelcomeMsg.setText(managerContext);
							textViewWelcomeMsg.setTextColor(Color.RED);
						} else {
							managerContext = "环境自适应...";
							textViewWelcomeMsg.setText(managerContext);
							textViewWelcomeMsg.setTextColor(Color.BLUE);
						}
						break;
					case 4:
						managerContext = "环境自适应...";
						textViewWelcomeMsg.setText(managerContext);
						textViewWelcomeMsg.setTextColor(Color.RED);
						break;
					case 5:
						managerContext = chaoChu + "(" + baseApp.getBendiB() + ")，超出量限，重新自适应...";
						textViewWelcomeMsg.setText(managerContext);
						textViewWelcomeMsg.setTextColor(Color.RED);
						break;
					case 6:
						managerContext = "本底值(" + baseApp.getBendiA() + ")，超出量限，重新自适应...";
						textViewWelcomeMsg.setText(managerContext);
						textViewWelcomeMsg.setTextColor(Color.RED);
						break;
					case 9:
						if (timer != null) {
							timer.cancel();
							timer = null;
						}

						textViewWelcomeMsg.setText("检测异常: " + msg.obj);
						textViewWelcomeMsg.setTextColor(Color.RED);


						//如果不进行异常

						break;
				}
			} catch (Exception ex) {

			} finally {
				dataView.setText(dataView.getText() + "\r\n" + msg.obj);
			}
		}
	};

	private void StartMainActivity() {
		animation.cancel();
		if (!TextUtils.isEmpty(reset)) {
			if (reset.equals("1")) {
				//startActivity(new Intent(WelcomeActivity.this, SettingActivity.class));  //回到主界面
				startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
			}
		} else {
			startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
		}
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				SendSerialPort("AA01290028ED");  //停止检测
				if (timer != null)
					timer.cancel();
				break;
			default:
				break;
		}
		return super.onKeyDown(keyCode, event);
	}
}
