package com.example.spirillumin.zzcsoft.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.spirillumin.R;
import com.example.spirillumin.zzcsoft.spirilluminspection.BaseApplication;
import com.example.spirillumin.zzcsoft.spirilluminspection.MainActivity;
import com.example.spirillumin.zzcsoft.utils.DateTimeDialog;
import com.example.spirillumin.zzcsoft.utils.InputDialog;
import com.example.spirillumin.zzcsoft.utils.TimeClockDialog;
import com.example.spirillumin.zzcsoft.welcome.WelcomeActivity;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SettingActivity extends AppCompatActivity {

	private BaseApplication baseApp;
	private DateTimeDialog dateDialog;
	private TimeClockDialog dateTimeDialog;
	private AlertDialog.Builder builder;
	private InputDialog inputDialog, passwordDialog;
	private RelativeLayout layout1, layout2;
	private Button selectBtn, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btn10, btn11, btn12, btn13, btn14;
	private Button checkView, spinnerAppTitle, spinnerDeviceId, spinnerKS, spinnerJYY;
	private ImageView layoutBtn1, layoutBtn2;
	private TextView textViewBottomMessage;

	private Date systemDateTime;
	private SimpleDateFormat formatter;
	private NumberFormat nf;

	private int userState;
	private boolean isUpdate;
	private String timebtn1,timebtn2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_setting);
		builder = new Builder(SettingActivity.this);
		dateDialog = new DateTimeDialog(this);
		dateDialog.setOnDateTimeCheckListener(new DateTimeDialog.DateTimeCheckEvent() {

			@Override
			public void TimeCheckListener(String y, String mm, String d, String h, String m, String s) {
				try {
					StringBuffer sb = new StringBuffer();
					sb.append(y).append("-").append(mm).append("-").append(d);

					sb.append(" ");
					sb.append(h).append(":").append(m).append(":").append(s);
					systemDateTime = formatter.parse(sb + "");
					btn8.setText(sb);
				} catch (Exception e) {
					String errorString = e.getMessage();
					errorString += "fuck the error! " + errorString;
				}
			}
		});

		inputDialog = new InputDialog(this);
		inputDialog.setOnInputEventListener(new InputDialog.InputEvent() {

			@Override
			public void InputListener(String resule, boolean mode) {
				if (mode) {
					String[] results = resule.split(",");
					btn12.setText("H=" + results[0] + ",L=" + results[1]);
				} else {
					checkView.setText(resule);
				}
			}
		});

		passwordDialog = new InputDialog(this);
		passwordDialog.setOnInputEventListener(new InputDialog.InputEvent() {

			@Override
			public void InputListener(String resule, boolean mode) {
				BaseApplication.getInstance().setParams("password", resule);
			}
		});

		dateTimeDialog = new TimeClockDialog(this);
		dateTimeDialog.setOnTimeCheckListener(new TimeClockDialog.TimeCheckEvent() {

			@Override
			public void TimeCheckListener(String hh, String mm, String s) {
				try {
					StringBuffer sb = new StringBuffer();

					sb.append(" ");
					sb.append(hh).append(":").append(mm).append(":").append(s);
					selectBtn.setText(sb);
				} catch (Exception e) {

				}
			}
		});
		baseApp = BaseApplication.getInstance();
		baseApp.setOnSerialportComListener(new BaseApplication.SerialportCom() {

			@Override
			public void SerialportListener(String comStr) {
				try {
					if (comStr.length() > 0) {
						if (comStr.subSequence(0, 1).equals(" ")) {
							comStr = comStr.substring(1, comStr.length());
						}
					}
					String[] datas = comStr.split(" ");
					int cmd = Integer.parseInt(datas[2]);

					switch (cmd) {
						case 15:
							Message msg = handler.obtainMessage();
							handler.sendMessage(msg);
							break;
					}
				} catch (Exception e) {

				}
			}
		});
		init();

		try {
			userState = getIntent().getIntExtra("userState", -1);
			Log.e("adaaasas", "userstate=" + userState);
			if (userState == 1) {
				ImageView imgButtonPWD = (ImageView) findViewById(R.id.imgButtonPWD);
				imgButtonPWD.setVisibility(View.INVISIBLE);

				//ImageView imageViewDSTD = (ImageView) findViewById(R.id.imageViewDSTD);
				//imageViewDSTD.setVisibility(View.GONE);

				ImageView imageViewJCSJ = (ImageView) findViewById(R.id.imageViewJCSJ);
				imageViewJCSJ.setVisibility(View.GONE);
				ImageView imageViewDJSJ = (ImageView) findViewById(R.id.imageViewDJSJ);
				imageViewDJSJ.setVisibility(View.GONE);

				Button spinnerDeviceId = (Button) findViewById(R.id.spinnerDeviceId);
				spinnerDeviceId.setVisibility(View.INVISIBLE);
				Button spinnerDSTD = (Button) findViewById(R.id.spinnerDSTD);
				spinnerDSTD.setVisibility(View.GONE);
				Button spinnerDJSJ = (Button) findViewById(R.id.spinnerDJSJ);
				spinnerDJSJ.setVisibility(View.INVISIBLE);
				Button spinnerJCSJ = (Button) findViewById(R.id.spinnerJCSJ);
				spinnerJCSJ.setVisibility(View.INVISIBLE);

				TextView textViewSFM1 = (TextView) findViewById(R.id.textViewSFM1);
				textViewSFM1.setVisibility(View.INVISIBLE);
				TextView textViewSFM2 = (TextView) findViewById(R.id.textViewSFM2);
				textViewSFM2.setVisibility(View.INVISIBLE);
				TextView textViewJCSJ = (TextView) findViewById(R.id.textViewJCSJ);
				textViewJCSJ.setVisibility(View.INVISIBLE);
				TextView textViewDJSJ = (TextView) findViewById(R.id.textViewDJSJ);
				textViewDJSJ.setVisibility(View.INVISIBLE);
				TextView textViewDSTD = (TextView) findViewById(R.id.textViewDSTD);
				textViewDSTD.setVisibility(View.GONE);
				TextView textViewPWD = (TextView) findViewById(R.id.textViewPWD);
				textViewPWD.setVisibility(View.INVISIBLE);
				TextView textViewLine1 = (TextView) findViewById(R.id.textViewLine1);
				textViewLine1.setVisibility(View.INVISIBLE);

				RelativeLayout adminLayout = (RelativeLayout) findViewById(R.id.adminLayout);
				adminLayout.setVisibility(View.INVISIBLE);
			}
		} catch (Exception e) {
			Log.e("adaaasas", e.getMessage());
		}
	}

	Handler timeHandler = new Handler();
	Runnable runnable = new Runnable() {

		@Override
		public void run() {
			// 计时器
			try {
				addSecond();
				btn8.setText(formatter.format(systemDateTime));
			} catch (Exception e) {

			} finally {
				timeHandler.postDelayed(this, 1000);
			}
		}

		public void addSecond() {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(systemDateTime);
			calendar.add(Calendar.SECOND, 1);
			systemDateTime = calendar.getTime();
		}
	};

	private void init() {
		try {
			String[] changshuHL = baseApp.getChangshuHL().split(",");

			layout1 = (RelativeLayout) findViewById(R.id.tabLayout1);
			layout2 = (RelativeLayout) findViewById(R.id.tabLayout2);

			layoutBtn1 = (ImageView) findViewById(R.id.imageViewTop);
			layoutBtn2 = (ImageView) findViewById(R.id.imageViewBottom);

			spinnerAppTitle = (Button) findViewById(R.id.spinnerAppTitle);
			spinnerAppTitle.setText(baseApp.getApplicationTitle());

			spinnerDeviceId = (Button) findViewById(R.id.spinnerDeviceId);
			spinnerDeviceId.setText(baseApp.getDeviceId());
			spinnerKS = (Button) findViewById(R.id.spinnerKS);
			spinnerKS.setText(baseApp.getKeShi());
			spinnerJYY = (Button) findViewById(R.id.spinnerJYY);
			spinnerJYY.setText(baseApp.getJianYanYuan());
			TextView textViewCompany = (TextView) findViewById(R.id.textViewCompany);
			textViewCompany.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					return false;
				}
			});
			TextView textViewDeviceId = (TextView) findViewById(R.id.textViewDeviceId);
			textViewDeviceId.setText("设备ID：" + baseApp.getDeviceId());

			btn1 = (Button) findViewById(R.id.spinnerDSTD);
			ImageView passwordImg = (ImageView) findViewById(R.id.imgButtonPWD);
			passwordImg.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					passwordDialog.show("操作员密码", "", 12, 1);
				}
			});

			String passagewayModel = baseApp.getPassagewayModel();

			//btn1.setText(passagewayModel.equals("1") ? "单通道" : "双通道");
			/*
			 * 版本分为单通道和双通道
			 * 单通道版本此处字符为 "单通道"
			 * 双通道版本此处文本为 "双通道"
			 * */
			btn1.setText("双通道"); // 双通道 FR9202

			btn2 = (Button) findViewById(R.id.spinnerMRDYJ);
			btn12 = (Button) findViewById(R.id.editTextCS);
			btn12.setText("H=" + changshuHL[0] + ",L=" + changshuHL[1]);

			btn3 = (Button) findViewById(R.id.editTextYXSX);
			btn3.setText(baseApp.getYxsx());

			btn4 = (Button) findViewById(R.id.editTextYX1);
			btn4.setText(baseApp.getYangxing1());

			btn5 = (Button) findViewById(R.id.editTextYX2);
			btn5.setText(baseApp.getYangxing2());

			btn6 = (Button) findViewById(R.id.editTextYX3);
			btn6.setText(baseApp.getYangxing3());

			btn7 = (Button) findViewById(R.id.editTextBQD);
			btn7.setText(baseApp.getBuqueding());

			btn8 = (Button) findViewById(R.id.spinnerXTSJ);
			btn8.setText(baseApp.getSysDateTime());

			btn9 = (Button) findViewById(R.id.spinnerDJSJ);  //时间1获取
			btn9.setText(baseApp.getSelfAdaption());

			timebtn1 = btn9.getText().toString();

			btn10 = (Button) findViewById(R.id.spinnerJCSJ); //时间2获取
			btn10.setText(baseApp.getInspectionTime());

			timebtn2 = btn10.getText().toString();


			String bd = baseApp.getBendiceliangzhi();
			String bda = baseApp.getBendiA();
			String bdb = baseApp.getBendiB();

			//double dbInt = Double.parseDouble(bd);
			double dbInt = Double.parseDouble(bd) / 250 * 60;
			double dbaInt = Double.parseDouble(bda) / 250 * 60;
			double dbbInt = Double.parseDouble(bdb) / 250 * 60;

			if(dbaInt == 15728.4){
				dbaInt = 0;

			}

			if(dbbInt == 15728.4){
				dbbInt = 0;

			}

			nf = NumberFormat.getNumberInstance();
			// 保留两位小数
			nf.setMaximumFractionDigits(0);
			// 如果不需要四舍五入，可以使用RoundingMode.DOWN
			nf.setRoundingMode(RoundingMode.UP);

			btn11 = (Button) findViewById(R.id.editTextBD);
			btn11.setText(nf.format(dbInt) + "");

			btn13 = (Button) findViewById(R.id.editTextBDA);
			btn13.setText(nf.format(dbaInt) + "");

			btn14 = (Button) findViewById(R.id.editTextBDB);
			btn14.setText(nf.format(dbbInt) + "");

			textViewBottomMessage = (TextView) findViewById(R.id.textViewBottomMessage);
			TextView textViewApplicationOrgan = (TextView) findViewById(R.id.textViewApplicationOrgan);
			textViewApplicationOrgan
					.setText(baseApp.getApplicationTitle().equals("单击此处编辑") ? "" : baseApp.getApplicationTitle());

			ImageView imgBack = (ImageView) findViewById(R.id.imgButtonBack);
			imgBack.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (isUpdate) {
						new AlertDialog.Builder(SettingActivity.this).setTitle("提示").setMessage("确定退出吗？")
								.setPositiveButton("确定", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int whichButton) {
										baseApp.setSysDateTime(formatter.format(systemDateTime));
										startActivity(new Intent(SettingActivity.this, MainActivity.class));
										finish();
									}
								}).setNegativeButton("取消", null).show();
					} else {
						baseApp.setSysDateTime(formatter.format(systemDateTime));
						startActivity(new Intent(SettingActivity.this, MainActivity.class));
						finish();
					}
				}
			});
			if (!baseApp.isStart()) {
				// imgBack.setEnabled(false);
			}

			formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			systemDateTime = formatter.parse(baseApp.getSysDateTime());
			timeHandler.postDelayed(runnable, 1000);
		} catch (Exception e) {

		}

	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			textViewBottomMessage.setText("参数更新成功");
		}
	};

	public void Setting_OnClick(View v) {
		switch (v.getId()) {
			case R.id.imgButtonSelfCheck:
				baseApp.setSysDateTime(btn8.getText() + "");
				baseApp.setPassagewayModel(btn1.getText().equals("单通道") ? "1" : "2");
				baseApp.setSelfAdaption(btn9.getText() + "");
				baseApp.setInspectionTime(btn10.getText() + "");
				baseApp.setDefaultPrinter("");

				baseApp.setYxsx(btn3.getText() + "");
				baseApp.setYangxing1(btn4.getText() + "");
				baseApp.setYangxing2(btn5.getText() + "");
				baseApp.setYangxing3(btn6.getText() + "");
				baseApp.setBuqueding(btn7.getText() + "");
				baseApp.setBendiA(btn13.getText() + "");
				baseApp.setBendiB(btn14.getText() + "");
				baseApp.setChangshuHL(btn12.getText().toString().replace("H=", "").replace("L=", "") + "");

				baseApp.saveSystemInfo();
				baseApp.SendParmasToPc();
				startInspection();
				break;
			case R.id.imgButtonReSet:
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = new Date(System.currentTimeMillis());

				baseApp.setSysDateTime(simpleDateFormat.format(date));
				baseApp.setPassagewayModel("1");
				baseApp.setSelfAdaption("00:41:40");
				baseApp.setInspectionTime("00:04:10");
				baseApp.setDefaultPrinter("");
				baseApp.setBendiceliangzhi("40");
				baseApp.setYxsx("99");
				baseApp.setYangxing1("499");
				baseApp.setYangxing2("1499");
				baseApp.setYangxing3("2499");
				baseApp.setBuqueding("149");
				baseApp.setBendiA("0");
				baseApp.setBendiB("0");
				baseApp.setChangshuHL("7,15");

				baseApp.saveSystemInfo();
				baseApp.SendParmasToPc();
				startInspection();
				break;
			case R.id.spinnerXTSJ:
				dateDialog.show();
				isUpdate = true;
				break;
			case R.id.spinnerJCSJ:
				selectBtn = (Button) v;
				dateTimeDialog.show();
				isUpdate = true;
				break;
			case R.id.spinnerDJSJ:
				selectBtn = (Button) v;
				dateTimeDialog.show();
				isUpdate = true;
				break;
			case R.id.spinnerDSTD:
				//屏蔽通道选择处理
			/*
			builder.setIcon(R.drawable.ic_launcher);
			builder.setTitle("单双通道选择");
			builder.setAdapter(
					new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[] { "单通道", "双通道" }),
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							case 0:
								btn1.setText("单通道");
								break;
							case 1:
								btn1.setText("双通道");
								break;
							}
						}
					});
			builder.show();
			*/
				isUpdate = true;
				break;
			case R.id.spinnerMRDYJ:
			/*
			builder.setIcon(R.drawable.ic_launcher);
			builder.setTitle("默认打印机选择");
			builder.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
					new String[] { "小票打印", "A4打印" }), new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							case 0:
								btn2.setText("小票打印");
								break;
							case 1:
								btn2.setText("A4打印");
								break;
							}
						}
					});
			builder.show();*/
				isUpdate = true;
				break;
			case R.id.editTextBDB:
				textViewBottomMessage.setText("不允许手动修改");
				break;
			case R.id.editTextBDA:
				textViewBottomMessage.setText("不允许手动修改");
				break;
			case R.id.editTextBQD:
				checkView = (Button) v;
				inputDialog.show("不确定", checkView.getText() + "", 6, 0);
				isUpdate = true;
				break;
			case R.id.editTextBD:
				textViewBottomMessage.setText("不允许手动修改");
				// checkView = (Button) v;
				// inputDialog.show("本底(测量值)", checkView.getText() + "", 6, 0);
				// isUpdate = true;
				break;
			case R.id.editTextYXSX:
				checkView = (Button) v;
				inputDialog.show("阴性上限", checkView.getText() + "", 6, 0);
				isUpdate = true;
				break;
			case R.id.editTextCS:
				checkView = (Button) v;
				String[] changshuHL = checkView.getText().toString().split(",");
				inputDialog.showMode("常数H", "常数L", changshuHL[0].replace("H=", ""), changshuHL[1].replace("L=", ""));
				isUpdate = true;
				break;
			case R.id.editTextYX1:
				checkView = (Button) v;
				inputDialog.show("阳性+", checkView.getText() + "", 6, 0);
				isUpdate = true;
				break;
			case R.id.editTextYX2:
				checkView = (Button) v;
				inputDialog.show("阳性++", checkView.getText() + "", 6, 0);
				isUpdate = true;
				break;
			case R.id.editTextYX3:
				checkView = (Button) v;
				inputDialog.show("阳性+++", checkView.getText() + "", 6, 0);
				isUpdate = true;
				break;
			case R.id.imageViewSave:
				try {
					baseApp.setSysDateTime(btn8.getText() + ""); //系统时间
					baseApp.setPassagewayModel(btn1.getText().toString().trim().equals("单通道") ? "1" : "2"); //通道选择
					baseApp.setSelfAdaption(btn9.getText() + "");//环境自适应时间
					baseApp.setInspectionTime(btn10.getText() + ""); //检测时间
					baseApp.setYxsx(btn3.getText() + ""); //阴性上限
					baseApp.setYangxing1(btn4.getText() + ""); //阳+上限
					baseApp.setYangxing2(btn5.getText() + ""); //阳++上限
					baseApp.setYangxing3(btn6.getText() + ""); //阳+++上限
					baseApp.setBuqueding(btn7.getText() + ""); //不确定上限
					baseApp.setChangshuHL(btn12.getText().toString().replace("H=", "").replace("L=", "") + ""); //HL数值

					baseApp.saveSystemInfo(); //写入系统   修改时间以外的参数无需再进行环境自适应

					if((btn9.getText().toString()) == timebtn1){
						userState=1;

					}else{
						userState=0;//需要开始环境自适应

					}

					if((btn10.getText().toString()) == timebtn2){
						userState=1;

					}else{
						userState=0;//需要开始环境自适应

					}

					if (userState != 1) {  //判断更改参数后是否需要进入环境自适应
						startInspection();
					} else {
						baseApp.setSysDateTime(formatter.format(systemDateTime));
						startActivity(new Intent(SettingActivity.this, MainActivity.class));
						finish();

					}
				} catch (Exception e) {
					textViewBottomMessage.setText(e.getMessage());
				}
				break;
			case R.id.imageViewTop:
				layout1.setVisibility(View.VISIBLE);
				layout2.setVisibility(View.GONE);

				layoutBtn1.setImageResource(R.drawable.setting_top_down);
				layoutBtn2.setImageResource(R.drawable.setting_bottom_up);
				break;
			case R.id.imageViewBottom:
				layout1.setVisibility(View.GONE);
				layout2.setVisibility(View.VISIBLE);

				layoutBtn1.setImageResource(R.drawable.setting_top_up);
				layoutBtn2.setImageResource(R.drawable.setting_bottom_down);
				break;
			case R.id.spinnerAppTitle:
				InputDialog input1 = new InputDialog(SettingActivity.this);
				input1.setOnInputEventListener(new InputDialog.InputEvent() {

					@Override
					public void InputListener(String resule, boolean mode) {
						baseApp.setApplicationTitle(resule);
						spinnerAppTitle.setText(resule);
					}
				});
				input1.show("单位名称", baseApp.getApplicationTitle(), 20, 1);
				break;
			case R.id.spinnerKS:
				InputDialog input2 = new InputDialog(SettingActivity.this);
				input2.setOnInputEventListener(new InputDialog.InputEvent() {

					@Override
					public void InputListener(String resule, boolean mode) {
						baseApp.setKeShi(resule);
						spinnerKS.setText(resule);
					}
				});
				input2.show("科室", baseApp.getKeShi(), 8, 1);
				break;
			case R.id.spinnerJYY:
				InputDialog input3 = new InputDialog(SettingActivity.this);
				input3.setOnInputEventListener(new InputDialog.InputEvent() {

					@Override
					public void InputListener(String resule, boolean mode) {
						baseApp.setJianYanYuan(resule);
						spinnerJYY.setText(resule);
					}
				});
				input3.show("检验员", baseApp.getJianYanYuan(), 6, 1);
				break;
			case R.id.spinnerDeviceId:
				InputDialog input4 = new InputDialog(SettingActivity.this);
				input4.setOnInputEventListener(new InputDialog.InputEvent() {

					@Override
					public void InputListener(String resule, boolean mode) {
						baseApp.setDeviceId(resule);
						spinnerDeviceId.setText(resule);
					}
				});
				input4.show("设备Id", baseApp.getDeviceId(), 17, 1);
				break;
		}
	}

	private void startInspection() {
		Intent Intent = new Intent();
		Intent.setClass(this, WelcomeActivity.class);
		Intent.putExtra("reSet", "1");
		baseApp.setFrist("0");
		baseApp.setSysDateTime(formatter.format(systemDateTime));
		startActivity(Intent);
		finish();
	}
}
