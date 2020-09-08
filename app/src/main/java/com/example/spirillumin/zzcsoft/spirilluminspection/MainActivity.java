package com.example.spirillumin.zzcsoft.spirilluminspection;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.spirillumin.R;
import com.example.spirillumin.zzcsoft.adapter.RecordAdapter;
import com.example.spirillumin.zzcsoft.db.SqliteHelper;
import com.example.spirillumin.zzcsoft.history.HistoryActivity;
import com.example.spirillumin.zzcsoft.setting.SettingActivity;
import com.example.spirillumin.zzcsoft.utils.InputDialog;
import com.example.spirillumin.zzcsoft.utils.LoginDialog;
import com.example.spirillumin.zzcsoft.utils.RechargeDialog;
import com.example.spirillumin.zzcsoft.utils.StandbyDialog;
import com.example.spirillumin.zzcsoft.utils.Utils;
import com.example.spirillumin.zzcsoft.welcome.WelcomeActivity;

public class MainActivity extends AppCompatActivity {

	private RecordAdapter adapter;

	private ListView listRecord;
	private ImageView inspectionImg;
	private Utils utils;
	private Animation animation;
	private RechargeDialog rechargeDialog;
	private StandbyDialog standbyDialog;
	private LoginDialog loginDialog;
	private InputDialog inputDialog;
	private Button buttonJYY, buttonKS;
	private TextView textViewSystemDateTile, textViewBottomMessage, textViewListViewMessage, textViewInspection,
			textViewRecharge, textViewDeviceID, textViewAppOrgan;
	private Date systemDateTime, standbyDateTime;
	private SimpleDateFormat formatter;
	private BaseApplication.SerialControl0 com0;
	private BaseApplication.SerialControl com1;
	private Chronometer chronometer;
	private BaseApplication baseApp;
	private SqliteHelper db;

	private CountDownTimer timer;
	private MediaPlayer distanceSound;

	private String userName, passageway, allow1, allow2, paramupdate0;
	private int standbyTime = 0, standby = 0, inspectionTime = 0, isPcQuest = 0, num;
	private boolean isLeave, lock, td;

	private int pg_num1 = 0; //进度条相关变量
	private int pg_num2 = 0;
	private int noprint = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);  //

		try {
			utils = new Utils();
			db = new SqliteHelper(this);

			standbyDialog = new StandbyDialog(this);
			standbyDialog.setOnStandBackComListener(new StandbyDialog.StandBack() {

				@Override
				public void StandBackListener() {
					baseApp.setOnSerialportComListener(serialportCom);
				}
			});
			inputDialog = new InputDialog(this);
			inputDialog.setOnInputEventListener(new InputDialog.InputEvent() {

				@Override
				public void InputListener(String result, boolean mode) {
					try {
						userName = result;
						StartInspection(mode);
					} catch (Exception e) {
						textViewBottomMessage.setText("检测异常，错误码：" + e.getMessage());
					}
				}
			});

			loginDialog = new LoginDialog(this);
			loginDialog.setOnPassWordListener(new LoginDialog.PassWordEvent() {

				@Override
				public void PassWordListener(int state) {
					try {
						Intent intent = new Intent(MainActivity.this, SettingActivity.class);
						intent.putExtra("userState", state);
						startActivity(intent);
						baseApp.setSysDateTime(formatter.format(systemDateTime));
						finish();
					} catch (Exception e) {
						textViewBottomMessage.setText("登录异常：" + e.getMessage());
					}
				}
			});

			baseApp = BaseApplication.getInstance();
			paramupdate0 = "";
			com0 = baseApp.getCom0();
			com1 = baseApp.getCom1();

			if (!com1.isOpen()) {
				Thread.sleep(2000);
				com1.setPort("/dev/ttyS3");
				com1.setBaudRate(9600);
				com1.open();
				Thread.sleep(1000);
			}

			if (!com0.isOpen()) {
				Thread.sleep(1000);
				com0.setPort("/dev/ttyS0");
				com0.setBaudRate(9600);
				com0.open();
				Thread.sleep(1000);
			}

			baseApp.setOnSerialport0ComListener(serialportCom0);
			baseApp.setOnSerialportComListener(serialportCom);

			initConfig();
			init();
		} catch (Exception e) {
			textViewBottomMessage.setText(e.getMessage());
		}
	}

	RechargeDialog.RechargeBack rechargeBack = new RechargeDialog.RechargeBack() {

		@Override
		public void RechargeBackListener() {
			baseApp.setOnSerialportComListener(serialportCom);
			textViewRecharge.setText("剩余：" + baseApp.getRechargeNum() + "次");
		}
	};

	private Handler inspectionHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.arg1) {
				case 0:
					num = inspectionTime;
					chronometer.start();
					break;
				case 1:

					break;
				case 2:

					break;
			}
		}
	};

	BaseApplication.SerialportCom0 serialportCom0 = new BaseApplication.SerialportCom0() {

		@Override
		public void SerialportListener(String comStr) {
			try {
				if (comStr.indexOf("startcheck") != -1) {

					com0.sendTxt("alreadycheck"); //回复已经开始
					noprint=1;
					if (isPcQuest == 1) {
						//联机检测时出检测结果不调用打印
						isPcQuest = 2;
						pcInspectionHandler.sendMessage(pcInspectionHandler.obtainMessage());
					}
				}else if (comStr.indexOf("sample-s_getstate_sample-e") != -1) {
					if (baseApp.getRechargeNum() <= 0) {
						// 2,2 是没有剩余次数的意思
						com0.sendTxt("sample-s_2,2_sample-e"); //剩余次数不足
					} else {
						isPcQuest = 1;
						com1.sendHex("AA01A000A1ED");
					}
				} else if (comStr.indexOf("check-s_startselfcheck_check-e") != -1){ //新增开始环境自适应
					com0.sendTxt("check-s_selfcheck_check-e");

					Intent Intent = new Intent();
					Intent.setClass(MainActivity.this, WelcomeActivity.class);
					Intent.putExtra("startflag", "1");
					baseApp.setFrist("0");
					baseApp.setSysDateTime(formatter.format(systemDateTime));
					startActivity(Intent);
					finish();



				} else if (comStr.indexOf("paramupdate") != -1) {
					paramupdate0 += comStr;
					if (paramupdate0.indexOf("paramupdate-s") != -1 && paramupdate0.indexOf("paramupdate-e") != -1) {
						String param = paramupdate0.trim();
						String[] params = param.split("_");

						baseApp.setChangshuHL(params[1] + "," + params[2]);
						baseApp.saveSystemInfo();
						paramupdate0 = "";

						com0.sendTxt("paramupdate-s_paramupdateget_paramupdate-e");
					}
				}
			} catch (Exception e) {

			}
		}
	};

	private Handler pcInspectionHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			StartInspection(td);
		}
	};

	BaseApplication.SerialportCom serialportCom = new BaseApplication.SerialportCom() {

		@Override
		public void SerialportListener(String comStr) {

			comStr = comStr.substring(comStr.indexOf("DD"));

			try {
				String[] datas = comStr.split(" ");
				String cmd = datas[2];
				int dataLength = Integer.parseInt(datas[1]);
				Message msg = serialPortHandler.obtainMessage();

				if (cmd.equals("1A")) {
					msg.arg1 = 1;
				} else if (cmd.equals("18")) {
					// DD 02 18 00 0A 10
					String rechargeNum = "";
					for (int i = 3; i < dataLength; i++) {
						rechargeNum += datas[i];
					}
					int recharge = Integer.parseInt(rechargeNum, 16);
					baseApp.setRechargeNum(baseApp.getRechargeNum() + recharge);
					msg.arg1 = 2;

				} else if (cmd.equals("1C")) {
					isPcQuest = 0;
					com0.sendTxt("check-s_out_check-e"); //通知上位机呼吸卡正常取出
					// DD 01 1C 00 1D ED 呼吸卡被拔出
					msg.arg1 = 3;
				} else if (cmd.equals("33")) {
					msg.arg1 = 5;
				} else if (cmd.equals("20")) {
					// DD01200021ED
					msg.arg1 = 3;
				} else if (cmd.equals("1E")) {
					// DD 02 1E 10 09 05 ED 呼吸卡单通道检测结果
					//com1.sendHex("AA011F001EED");
					allow1 = "";
					for (int i = 0; i < dataLength; i++) {
						allow1 += datas[3 + i];
					}

					if (!allow1.equals("FFFF")) {
						com1.sendHex("AA01920093ED"); //校正时间之后才添加检测记录
					}
					msg.arg1 = 4;

				} else if (cmd.equals("52")) {

					int data_length = comStr.length();

					if(data_length == 24){

						//com1.sendHex("AA011F001EED");
						allow1 = "";
						allow2 = "";
						for (int i = 0; i < dataLength / 2; i++) {
							allow1 += datas[3 + i];
						}
						for (int i = 0; i < dataLength / 2; i++) {
							allow2 += datas[5 + i];
						}

						//com1.sendHex("AA01920093ED"); //校正时间之后添加检测记录
						//msg.arg1 = 4;

						//新增测试

						userName = TextUtils.isEmpty(userName) ? "" : userName;
						if(isPcQuest != 0){
							userName = "";
						}
						String[] userNames = userName.split(",");
						if (userNames.length == 2) {
							userName = userNames[0];
							if (allow1.indexOf("FF") == -1) {
								com1.sendHex("AA011F001EED");
								addInspectionResult(allow1, " A");
								//总线延迟
								Thread.sleep(500);

							}
							userName = userNames[1];
							if (allow2.indexOf("FF") == -1) {
								com1.sendHex("AA011F001EED");
								addInspectionResult(allow2, " B");
								//总线延迟
								Thread.sleep(500);

							}
						} else {
							if (allow1.indexOf("FFFF") == -1) {
								com1.sendHex("AA011F001EED");
								addInspectionResult(allow1, " A");
								//总线延迟
								Thread.sleep(500);

							}
							if (allow2.indexOf("FFFF") == -1) {
								com1.sendHex("AA011F001EED");
								addInspectionResult(allow2, " B");
								//总线延迟
								Thread.sleep(500);

							}
						}
						msg.arg1 = 9;

						//新增测试
					}else{
						msg.arg1 = 10;

					}

				} else if (cmd.equals("A1")) {
					// DD 01 A1 00 A0
					passageway = datas[3];
					msg.arg1 = 1;
					if (passageway.equals("00")) {
						// 没有呼吸卡
						msg.arg1 = 1;
						com0.sendTxt("sample-s_0,0_sample-e");

					} else if (passageway.equals("01")) {
						// 单通道A
						msg.arg1 = 6;
						com0.sendTxt("sample-s_1,0_sample-e");
						td = false;
						//com1.sendHex("AA01920093ED");

					} else if (passageway.equals("02")) {
						// 单通道B
						msg.arg1 = 7;
						com0.sendTxt("sample-s_0,1_sample-e");
						td = false;
						//com1.sendHex("AA01920093ED");

					} else if (passageway.equals("03")) {
						// 双通道

						if (baseApp.getRechargeNum() <= 1) {
							com0.sendTxt("sample-s_2,2_sample-e");
							msg.arg1 = 10;

						}else{
							msg.arg1 = 8;
							com0.sendTxt("sample-s_1,1_sample-e");
							td = true;
							//com1.sendHex("AA01920093ED");
						}


					}
				} else if (cmd.equals("93")) {  //时间回复
					// DD 07 93 14 12 03 1C 0E 11 0C AA ED
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

					systemDateTime = formatter.parse(baseApp.getSysDateTime()); //时间校正完成
					msg.arg1 = 10;


				}
				serialPortHandler.sendMessage(msg);
			} catch (Exception e) {

			} finally {
				standbyTime = 0;
			}
		}
	};

	private void StartInspection(boolean mode) {
		int a = Integer.parseInt("01", 16);
		int b = Integer.parseInt("A2", 16);
		int c = Integer.parseInt(passageway, 16);
		int n = a ^ b ^ c;
		String o = Integer.toHexString(n).toUpperCase();
		if (mode) {
			// 双通道
			com1.sendHex("AA01A2" + passageway + o + "ED");
		} else {
			// 单通道
			com1.sendHex("AA01A2" + passageway + o + "ED");
		}
		textViewInspection.setText("检测中");
		chronometer.setVisibility(View.VISIBLE);
		inspectionImg.setImageResource(R.drawable.inspection_begin);
		// inspectionImg.startAnimation(animation);
		num = inspectionTime;
		chronometer.start();
		lock = true;
	}

	private void addInspectionResult(String allowNum, String passageway) {
		String dmp = Integer.parseInt(allowNum, 16) + "";

		try {
			if (!dmp.equals("65535")) {

				int dmpInt = Integer.parseInt(dmp);

				/*
				if (dmpInt > 30 && dmpInt < 500) {
					dmpInt = 148 + new Random().nextInt(34);
				} else if (dmpInt > 500 && dmpInt < 1500) {
					dmpInt = 585 + new Random().nextInt(130);
				} else if (dmpInt > 1500 && dmpInt < 3500) {
					dmpInt = 2185 + new Random().nextInt(230);
				} else if (dmpInt > 3500 && dmpInt < 6000) {
					dmpInt = 5040 + new Random().nextInt(1120);
				}
                */

				HashMap<String, Object> params = new HashMap<String, Object>();
				String uuid = UUID.randomUUID().toString();
				String datetime = formatter.format(systemDateTime);
				String result = getInspectionResult(dmpInt);
				String level = getLevel(dmpInt);

				params.put("UUID", uuid);
				params.put("USERNAME", userName.replace(",", ""));
				params.put("RESULT", result);
				params.put("PDM", dmpInt + "");
				params.put("DTIME", datetime);
				params.put("OPERATOR", baseApp.getJianYanYuan());
				params.put("DEPARTMENT", baseApp.getKeShi());
				params.put("STATE", "0");
				params.put("PASSAGEWAY", passageway);

				db.AddDataByCond("INSPECTION_INFO", params);
				baseApp.setRechargeNum(baseApp.getRechargeNum() - 1);
				ArrayList<HashMap<String, Object>> datas = db.SelectDataByCond("INSPECTION_INFO",
						" WHERE UUID = '" + uuid + "'");
				HashMap<String, Object> infoMap = (HashMap<String, Object>) datas.get(0);
				if(noprint==0){
					baseApp.Print(infoMap); //打印

				}else{;}

				String head = "check-s_";
				String end = "_check-e";

				//检测结果统一格式向上位机传输
				if (isPcQuest == 2) {
					isPcQuest = 0;
					head = "check-s_";
					end = "_check-e";
				}

				if (passageway.indexOf("A") != -1) {
					com0.sendTxt(head + "A" + "_" + infoMap.get("INDEX_NO") + "_" + baseApp.getDeviceId() + "_" + datetime + "_" + dmp + "_" + level + "_" + baseApp.getRechargeNum() + end);
					//com0.sendTxt(head + "A" + "_" + infoMap.get("INDEX_NO") + "_" + baseApp.getDeviceId() + "_" + datetime + "_" + dmp + "_" + "C" + "_" + baseApp.getRechargeNum() + end);

				} else {
					com0.sendTxt(head + "B" + "_" + infoMap.get("INDEX_NO") + "_" + baseApp.getDeviceId() + "_" + datetime + "_" + dmp + "_" + level + "_" + baseApp.getRechargeNum() + end);
					//com0.sendTxt(head + "B" + "_" + infoMap.get("INDEX_NO") + "_" + baseApp.getDeviceId() + "_" + datetime + "_" + dmp + "_" + "A" + "_" + baseApp.getRechargeNum() + end);

				}

			}
		} catch (Exception ex) {

		}
	}

	private Handler serialPortHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.arg1) {
				case 1:
					textViewBottomMessage.setText("未检测到呼吸卡");
					inspectionImg.clearAnimation();
					chronometer.stop();
					break;
				case 2:
					textViewBottomMessage.setText("刷卡成功，已经完成充值");
					break;
				case 3:
					textViewBottomMessage.setText("呼吸卡被拔出");
					inspectionImg.clearAnimation();
					chronometer.stop();
					break;
				case 4:
					lock = false;
					inspectionImg.clearAnimation();
					chronometer.stop();

					textViewInspection.setText("开始检测");
					textViewBottomMessage.setText("检测已经完成!请拔出呼吸卡");
					textViewListViewMessage.setVisibility(View.GONE);
					break;
				case 5:
					isPcQuest = 0;
					textViewBottomMessage.setText("呼吸卡取出异常");  //新增取出异常后界面回到待机状态
					com0.sendTxt("check-s_outputabnormal_check-e");
					com1.sendHex("AA01290028ED");
					textViewInspection.setText("开始检测");
					chronometer.setVisibility(View.GONE);
					inspectionImg.setImageResource(R.drawable.inspection_end);
					inspectionImg.clearAnimation();
					chronometer.stop();
					lock = false;

					break;
				case 6:
					// 单通道A
					if (baseApp.getRechargeNum() <= 0) {
						textViewBottomMessage.setText("剩余次数不足，请充值后使用!");
						break;
					}
					if (TextUtils.isEmpty(buttonJYY.getText()) || TextUtils.isEmpty(buttonKS.getText())) {
						textViewBottomMessage.setText("科室和检验员不能为空，请完善检测信息!");
						break;
					}
					if (isPcQuest == 0) {
						inputDialog.show("请输入患者姓名", "", 6, 1);
						noprint=0;
					}
					distanceSoundSoundPlay();
					textViewBottomMessage.setText("除指定样式的呼吸卡以外 ，严禁将其它物品插入检测口内");
					// com0.sendTxt("sample-s_isput_sample-e");
					break;
				case 7:
					// 单通道B
					if (baseApp.getRechargeNum() <= 0) {
						textViewBottomMessage.setText("剩余次数不足，请充值后使用!");
						break;
					}
					if (TextUtils.isEmpty(buttonJYY.getText()) || TextUtils.isEmpty(buttonKS.getText())) {
						textViewBottomMessage.setText("科室和检验员不能为空，请完善检测信息!");
						break;
					}
					if (isPcQuest == 0) {
						noprint=0;
						inputDialog.show("请输入患者姓名", "", 6, 1);
					}
					distanceSoundSoundPlay();
					textViewBottomMessage.setText("除指定样式的呼吸卡以外 ，严禁将其它物品插入检测口内");
					// com0.sendTxt("sample-s_isput_sample-e");
					break;
				case 8:
					// 双通道
					if (baseApp.getRechargeNum() <= 1) {
						textViewBottomMessage.setText("剩余次数不足，请充值后使用!");
						break;
					}
					if (TextUtils.isEmpty(buttonJYY.getText()) || TextUtils.isEmpty(buttonKS.getText())) {
						textViewBottomMessage.setText("科室和检验员不能为空，请完善检测信息!");
						break;
					}
					if (isPcQuest == 0) {
						inputDialog.showMode("请输入患者姓名", "通道B患者", "", "");
						noprint=0;
					}
					distanceSoundSoundPlay();
					textViewBottomMessage.setText("除指定样式的呼吸卡以外 ，严禁将其它物品插入检测口内");
					// com0.sendTxt("sample-s_isput_sample-e");
					break;
				case 9:
					adapter.dataReset();
					textViewRecharge.setText("剩余：" + baseApp.getRechargeNum() + "次");

					//新增测试

					lock = false;
					inspectionImg.clearAnimation();
					chronometer.stop();

					textViewInspection.setText("开始检测");
					textViewBottomMessage.setText("检测已经完成!请拔出呼吸卡");
					textViewListViewMessage.setVisibility(View.GONE);

					//新增测试



					break;

			}
		}
	};

	private void init() {
		listRecord = (ListView) findViewById(R.id.recordList);
		adapter = new RecordAdapter(this, 20);
		listRecord.setAdapter(adapter);

		chronometer = (Chronometer) findViewById(R.id.chronometer);
		chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
			@Override
			public void onChronometerTick(Chronometer ch) {
				if (num == 0) {
					pg_num1=0;
					pg_num2=0;
					com1.sendHex("AA01600061ED");
					ch.stop();
					textViewInspection.setText("开始检测");
					chronometer.setVisibility(View.GONE);
					inspectionImg.setImageResource(R.drawable.inspection_end);
				} else {
					num = num - 1;
					chronometer.setText(num + "S");
					//给上位机进度条提供参数
					pg_num1=(50-(num/5))*2;
					if((pg_num1 != pg_num2)&&((num%5)==0)){
						pg_num2 = pg_num1;
						com0.sendTxt("progress-s_" + pg_num2 + "%_progress-e");  //新增进度条上传功能

					}

				}
			}
		});
		textViewAppOrgan = (TextView) findViewById(R.id.textViewAppOrgan);
		textViewAppOrgan.setText(baseApp.getApplicationTitle());

		TextView textViewVersion = (TextView) findViewById(R.id.textViewVersion);
		//textViewVersion.setText("版本号：" + Utils.getVersionCode(this));
		textViewVersion.setText("版本号：1.0");

		inspectionImg = (ImageView) findViewById(R.id.imageViewInspection);
		textViewSystemDateTile = (TextView) findViewById(R.id.textViewSystemDateTile);
		textViewBottomMessage = (TextView) findViewById(R.id.textViewBottomMessage);
		textViewListViewMessage = (TextView) findViewById(R.id.textViewListViewMessage);
		textViewInspection = (TextView) findViewById(R.id.textViewInspection);
		textViewRecharge = (TextView) findViewById(R.id.textViewRecharge);
		textViewDeviceID = (TextView) findViewById(R.id.textViewDeviceID);
		textViewRecharge.setText("剩余：" + baseApp.getRechargeNum() + "次");
		textViewDeviceID.setText("设备ID：" + baseApp.getDeviceId());

		if (adapter.getDatas().size() == 0)
			textViewListViewMessage.setVisibility(View.VISIBLE);

		animation = AnimationUtils.loadAnimation(this, R.anim.inspection_style);

		buttonJYY = (Button) findViewById(R.id.editTextJYY);
		buttonJYY.setText(baseApp.getJianYanYuan());
		buttonKS = (Button) findViewById(R.id.editTextKS);
		buttonKS.setText(baseApp.getKeShi());

		handler.postDelayed(runnable, 1000);

//		new Thread() {
//			@Override
//			public void run() {
//				try {
//					Thread.sleep(1000);
//
//					Message msg = standbyHandler.obtainMessage();
//					msg.arg1 = 0;
//					standbyHandler.sendMessage(msg);
//				} catch (Exception e) {
//
//				}
//			}
//		}.start();
	}

	public void Main_OnClick(View v) {
		try {
			switch (v.getId()) {
				case R.id.imageViewInspection:
					// 开始检测
					if (lock) {
						com1.sendHex("AA01290028ED");
						textViewInspection.setText("开始检测");
						chronometer.setVisibility(View.GONE);
						inspectionImg.setImageResource(R.drawable.inspection_end);
						// inspectionImg.clearAnimation();
						chronometer.stop();
						lock = false;
					} else {
						if (baseApp.getRechargeNum() <= 0) {
							textViewBottomMessage.setText("剩余次数不足，请充值后使用!");
						} else {
							com1.sendHex("AA01A000A1ED");
						}
					}
					break;
				case R.id.imgButtonSetting:
					if (!lock) {
						loginDialog.show(true);
						// startActivity(new Intent(this, SettingActivity.class));
						// baseApp.setSysDateTime(formatter.format(systemDateTime));
						// finish();
					} else {
						textViewBottomMessage.setText("检测中不能进行其它类型的操作");
					}
					break;
				case R.id.imgButtonSelect:
					if (!lock) {
						startActivity(new Intent(this, HistoryActivity.class));
						baseApp.setSysDateTime(formatter.format(systemDateTime));
						finish();
					} else {
						textViewBottomMessage.setText("检测中不能进行其它类型的操作");
					}
					break;
				case R.id.imgButtonDelete:
					ArrayList<HashMap<String, Object>> dataList = adapter.getDatas();
					boolean[] checks = adapter.getChecks();
					for (int i = 0; i < checks.length; i++) {
						if (checks[i])
							db.DeleteDataByCond("INSPECTION_INFO", "UUID", dataList.get(i).get("UUID") + "");
					}
					adapter.dataReset();
					break;
				case R.id.imgButtonPrint:
					ArrayList<HashMap<String, Object>> dataList1 = adapter.getDatas();
					boolean[] checks1 = adapter.getChecks();
					for (int i = 0; i < checks1.length; i++) {
						if (checks1[i])
							baseApp.Print(dataList1.get(i));
					}
					break;
				case R.id.imgButtonRecharge:
					rechargeDialog = new RechargeDialog(this);
					rechargeDialog.setOnRechargeBackBackComListener(rechargeBack);
					rechargeDialog.show();
					break;
			}
		} catch (Exception e) {
			textViewBottomMessage.setText(e.getMessage());
		} finally {
			standbyTime = 0;
		}
	}

	public Boolean hideInputMethod(View v) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			return imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}
		return false;
	}

	public boolean isShouldHideInput(View v, MotionEvent event) {
		if (v != null && (v instanceof EditText)) {
			int[] leftTop = { 0, 0 };
			v.getLocationInWindow(leftTop);
			int left = leftTop[0], top = leftTop[1], bottom = top + v.getHeight(), right = left + v.getWidth();
			if (event.getX() > left && event.getX() < right && event.getY() > top && event.getY() < bottom) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			View v = getCurrentFocus();
			if (isShouldHideInput(v, ev)) {
				if (hideInputMethod(v)) {
					return true;
				}
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	protected void onResume() {
		isLeave = false;
		initConfig();
		adapter.dataReset();
		textViewDeviceID.setText("设备ID：" + baseApp.getDeviceId());
		textViewAppOrgan.setText(baseApp.getApplicationTitle());
		buttonJYY.setText(baseApp.getJianYanYuan());
		buttonKS.setText(baseApp.getKeShi());
		baseApp.setOnSerialportComListener(serialportCom);
		super.onResume();
	};

	@Override
	protected void onPause() {
		isLeave = true;
		super.onPause();
	};

	private void initConfig() {
		try {
			formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			systemDateTime = formatter.parse(baseApp.getSysDateTime());
			SimpleDateFormat standbyDateFormat = new SimpleDateFormat("HH:mm:ss");
			Date stanbyDate = standbyDateFormat.parse(baseApp.getStandbyTime());
			standby = ((stanbyDate.getHours() * 60) + stanbyDate.getMinutes()) * 60;

			Date inspectionDate = standbyDateFormat.parse(baseApp.getInspectionTime());
			inspectionTime = ((inspectionDate.getHours() * 60) + inspectionDate.getMinutes()) * 60
					+ inspectionDate.getSeconds();
		} catch (Exception e) {

		}
	}

//	Handler standbyHandler = new Handler();
//	Runnable standbyRunnable = new Runnable() {
//
//		@Override
//		public void run() {
//			try {
//
//			} catch (Exception e) {
//
//			}
//		}
//	};

	Handler handler = new Handler();
	Runnable runnable = new Runnable() {

		@Override
		public void run() {
			// 计时器
			try {
				addSecond();
				standbyTime++;
				if (standbyTime >= standby) {
					if (isLeave) {
						standbyTime = 0;
					} else {
						if (standbyDialog.isShowing()) {
							standbyDialog.Inspection();
						} else {
							standbyDialog.show(MainActivity.this);
						}
						standbyTime = 0;
					}
				}
				textViewSystemDateTile.setText("当前时间：" + formatter.format(systemDateTime));
			} catch (Exception e) {

			} finally {
				handler.postDelayed(this, 1000);
			}
		}

		public void addSecond() {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(systemDateTime);
			calendar.add(Calendar.SECOND, 1);
			systemDateTime = calendar.getTime();
		}
	};

	private String getInspectionResult(int pdmInt) {
		String result = "";

		int YXSX = Integer.parseInt(baseApp.getYxsx());
		int BQD = Integer.parseInt(baseApp.getBuqueding());
		int YX1 = Integer.parseInt(baseApp.getYangxing1());
		int YX2 = Integer.parseInt(baseApp.getYangxing2());
		int YX3 = Integer.parseInt(baseApp.getYangxing3());

		if (pdmInt > 2499) {
			result = "阳性++++";
		} else if (pdmInt <= YXSX) {
			result = "阴性";
		} else if (pdmInt > YXSX && pdmInt <= BQD) {
			result = "不确定";
		} else if (pdmInt > BQD && pdmInt <= YX1) {
			result = "阳性+";
		} else if (pdmInt > YX1 && pdmInt <= YX2) {
			result = "阳性++";
		} else if (pdmInt > YX2 && pdmInt <= YX3) {
			result = "阳性+++";
		}

		return result;
	}

	private String getLevel(int pdmInt) {
		String level = "";

		int YXSX = Integer.parseInt(baseApp.getYxsx());
		int BQD = Integer.parseInt(baseApp.getBuqueding());
		int YX1 = Integer.parseInt(baseApp.getYangxing1());
		int YX2 = Integer.parseInt(baseApp.getYangxing2());
		int YX3 = Integer.parseInt(baseApp.getYangxing3());

		if (pdmInt > 2499) {
			level = "F";
		} else if (pdmInt <= YXSX) {
			level = "A";
		} else if (pdmInt > YXSX && pdmInt <= BQD) {
			level = "B";
		} else if (pdmInt > BQD && pdmInt <= YX1) {
			level = "C";
		} else if (pdmInt > YX1 && pdmInt <= YX2) {
			level = "D";
		} else if (pdmInt > YX2 && pdmInt <= YX3) {
			level = "E";
		}

		return level;
	}

	@Override
	protected void onDestroy() {
		if (distanceSound != null) {
			distanceSound.stop();
			distanceSound.release();
		}
		super.onDestroy();
	};

	private void distanceSoundSoundPlay() {
		try {
			if (distanceSound != null)
				distanceSound.release();

			distanceSound = MediaPlayer.create(this, R.raw.s4);

			if (distanceSound != null)
				distanceSound.start();
		} catch (Exception ex) {
			Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
}
