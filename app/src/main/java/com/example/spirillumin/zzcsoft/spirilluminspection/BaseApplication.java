package com.example.spirillumin.zzcsoft.spirilluminspection;

import java.io.File;
import java.math.RoundingMode;
import java.sql.Date;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.spirillumin.zzcsoft.crash.CrashHandler;
import com.example.spirillumin.zzcsoft.log.LogInstance;
import com.example.spirillumin.zzcsoft.utils.FileUtil;
import com.example.spirillumin.zzcsoft.utils.Util;
import com.example.spirillumin.zzcsoft.utils.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android_serialport_api.ComBean;
import android_serialport_api.SerialHelper;

public class BaseApplication extends Application {
	protected Logger mLogger = LoggerFactory.getLogger(this.getClass());

	private NumberFormat wd;

	private static BaseApplication instance;

	private SharedPreferences sharedPreferences;

	private Handler mHandler;

	private boolean isStart = false;

	private String uuid;
	private String deviceId;
	private String applicationTitle;
	private String selfAdaption;
	private String sysDateTime;
	private String StandbyTime;
	private String inspectionTime;
	private String passagewayModel;
	private String defaultPrinter;
	private String bendiceliangzhi;
	private String yxsx;
	private String bendiA;
	private String bendiB;
	private String yangxing1;
	private String yangxing2;
	private String yangxing3;
	private String changshuHL;
	private String buqueding;
	private String frist;
	private String keShi;
	private String jianYanYuan;
	private int forTheFirst;
	private int rechargeNum;
	// 和PC交互
	private SerialControl0 com0;
	// 和单片机交互
	private SerialControl com1;
	// 打印机
	private SerialControl com2;

	private String paramsStr;
	public static BaseApplication getInstance() {
		return instance;
	}

	public void onCreate() {
		super.onCreate();
		try {

			instance = this;
//			ExceptionHandler.getInstance().init(getApplicationContext());

			com0 = new SerialControl0();
			com1 = new SerialControl();
			com2 = new SerialControl();

			mHandler = new printHandler();
			FileUtil.initPath();
			LogInstance.getInstance().init();

			addCrashCallback();

			loadSystemInit();

//			resetime();

			registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
				@Override
				public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
					mLogger.debug("-----onActivityCreated{}", activity);
				}

				@Override
				public void onActivityStarted(Activity activity) {
					mLogger.debug("-----onActivityStarted{}", activity);
				}

				@Override
				public void onActivityResumed(Activity activity) {
					mLogger.debug("-----onActivityResumed{}", activity);

				}

				@Override
				public void onActivityPaused(Activity activity) {
					mLogger.debug("-----onActivityPaused{}", activity);

				}

				@Override
				public void onActivityStopped(Activity activity) {
					mLogger.debug("-----onActivityStopped{}", activity);

				}

				@Override
				public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
					mLogger.debug("-----onActivitySaveInstanceState{}", activity);

				}

				@Override
				public void onActivityDestroyed(Activity activity) {
					mLogger.debug("-----onActivityDestroyed{}", activity);

				}
			});
		} catch (Exception localException) {
		}


	}

	private void resetime(){
		saveSystemInfo("0:04:10");
	}

	private void addCrashCallback() {
		CrashHandler.getInstance().init(this, new CrashHandler.CrashCallback() {
			@Override
			public void callback(Throwable ex) {
				ex.printStackTrace();
				Log.e(Application.class.getSimpleName(), "crash occur", ex);
				mLogger.error("addAppCrashCallback {}", ex);

				new Thread() {
					@Override
					public void run() {
						Looper.prepare();
						try {
							mLogger.info("当前可用内存：{}", Util.getAvailMemory(BaseApplication.this));
							Toast.makeText(BaseApplication.this, "很抱歉,出现不可预知的异常.", Toast.LENGTH_LONG).show();
						} catch (Exception e) {
							e.printStackTrace();
							mLogger.error("addAppCrashCallback toast error {}", e);
						}

						Looper.loop();
					}
				}.start();
				SystemClock.sleep(2000);

				appQuit();
			}
		});
	}

	private void appQuit() {
		Process.killProcess(Process.myPid());
	}


	public void onLowMemory() {
		super.onLowMemory();
		super.onTerminate();
	}

	public void onTerminate() {
		super.onTerminate();
	}

	public void onTrimMemory(int paramInt) {
		super.onTrimMemory(paramInt);
	}

	public void Print(HashMap<String, Object> hashMap) {
		if (com2.isOpen()) {
			com2.sendTxt("         幽门螺旋杆菌");
			com2.sendHex("0A");
			com2.sendTxt("通道" + hashMap.get("PASSAGEWAY") + "      检测报告");
			com2.sendHex("0A");
			com2.sendTxt("-----------------------------");
			com2.sendHex("0A");
			com2.sendTxt("编号: " + hashMap.get("INDEX_NO"));
			com2.sendHex("0A");
			com2.sendTxt("姓名: " + hashMap.get("USERNAME"));
			com2.sendHex("0A");
			com2.sendTxt("DPM: " + hashMap.get("PDM"));
			com2.sendHex("0A");
			com2.sendTxt("检测时间: " + hashMap.get("DTIME"));
			com2.sendHex("0A");
			com2.sendTxt("检验结果: " + hashMap.get("RESULT"));
			com2.sendHex("0A");
			com2.sendHex("0A");
			com2.sendTxt("检验人 : " + hashMap.get("OPERATOR")); //新增检验人从数据库中读取
			//com2.sendTxt("检验人 : _____________________");
			com2.sendHex("0A");
			com2.sendTxt("-----------------------------");
			com2.sendHex("0A");
			com2.sendHex("0A");
			com2.sendHex("0A");
			com2.sendHex("0A");
			com2.sendHex("0A");
			com2.sendHex("0A");
		}
	}

	public void PrintInspection(int state) {
		try {
			if (com2.isOpen()) {
				String[] changShu = changshuHL.split(",");
				NumberFormat nf = NumberFormat.getNumberInstance();
				// 保留两位小数
				nf.setMaximumFractionDigits(0);
				// 如果不需要四舍五入，可以使用RoundingMode.DOWN
				nf.setRoundingMode(RoundingMode.UP);
				switch (state) {
					case 0:
						double dbaInt = Double.parseDouble(bendiA) / 250 * 60;
						if(dbaInt == 15728.4){
							dbaInt = 0;

						}

						com2.sendTxt("-----------------------------");
						com2.sendHex("0A");
						com2.sendTxt("打印正常 ");
						com2.sendHex("0A");
						com2.sendTxt("-----------------------------");
						com2.sendHex("0A");
						com2.sendTxt("系统时间: ");
						com2.sendHex("0A");
						com2.sendTxt(getDateNow());
						com2.sendHex("0A");
						com2.sendTxt("-----------------------------");
						com2.sendHex("0A");
						com2.sendTxt("本底: " + nf.format(dbaInt) + "min-1");
						com2.sendHex("0A");
						com2.sendHex("0A");
						com2.sendHex("0A");
						com2.sendHex("0A");
						break;
					case 1:
						double dbaInt3 = Double.parseDouble(bendiA) / 250 * 60;
						if(dbaInt3 == 15728.4){
							dbaInt3 = 0;

						}

						com2.sendTxt("-----------------------------");
						com2.sendHex("0A");
						com2.sendTxt("系统参数: ");
						com2.sendHex("0A");
						com2.sendTxt("-----------------------------");
						com2.sendHex("0A");
						com2.sendTxt("检测时间: " + inspectionTime);
						com2.sendHex("0A");
						com2.sendTxt("常数: H=" + changShu[0] + ", L=" + changShu[1]);
						com2.sendHex("0A");
						com2.sendTxt("本底: " + nf.format(dbaInt3) + "min-1");
						com2.sendHex("0A");
						com2.sendTxt("-----------------------------");
						com2.sendHex("0A");
						com2.sendHex("0A");
						com2.sendHex("0A");
						com2.sendHex("0A");
						com2.sendHex("0A");
						com2.sendHex("0A");
						break;
					case 2:
						double dbaInt1 = Double.parseDouble(bendiA) / 250 * 60;
						double dbbInt1 = Double.parseDouble(bendiB) / 250 * 60;

						if(dbaInt1 == 15728.4){
							dbaInt1 = 0;

						}
						if(dbbInt1 == 15728.4){
							dbbInt1 = 0;

						}

						com2.sendTxt("-----------------------------");
						com2.sendHex("0A");
						com2.sendTxt("系统参数: ");
						com2.sendHex("0A");
						com2.sendTxt("-----------------------------");
						com2.sendHex("0A");
						com2.sendTxt("检测时间: " + inspectionTime);
						com2.sendHex("0A");
						com2.sendTxt("常数: H=" + changShu[0] + ", L=" + changShu[1]);
						com2.sendHex("0A");
						com2.sendTxt("本底A: " + nf.format(dbaInt1) + "min-1");
						com2.sendHex("0A");
						com2.sendTxt("本底B: " + nf.format(dbbInt1) + "min-1");
						com2.sendHex("0A");
						com2.sendTxt("-----------------------------");
						com2.sendHex("0A");
						com2.sendHex("0A");
						com2.sendHex("0A");
						com2.sendHex("0A");
						com2.sendHex("0A");
						com2.sendHex("0A");
						break;
					case 3:
						double dbaInt2 = Double.parseDouble(bendiA) / 250 * 60;
						double dbbInt2 = Double.parseDouble(bendiB) / 250 * 60;

						if(dbaInt2 == 15728.4){
							dbaInt2 = 0;

						}
						if(dbbInt2 == 15728.4){
							dbbInt2 = 0;

						}

						com2.sendTxt("-----------------------------");
						com2.sendHex("0A");
						com2.sendTxt("打印正常 ");
						com2.sendHex("0A");
						com2.sendTxt("-----------------------------");
						com2.sendHex("0A");
						com2.sendTxt("系统时间: ");
						com2.sendHex("0A");
						com2.sendTxt(getDateNow());
						com2.sendHex("0A");
						com2.sendTxt("-----------------------------");
						com2.sendHex("0A");
						com2.sendTxt("本底A: " + nf.format(dbaInt2) + "min-1");
						com2.sendHex("0A");
						com2.sendTxt("本底B: " + nf.format(dbbInt2) + "min-1");
						com2.sendHex("0A");
						com2.sendHex("0A");
						com2.sendHex("0A");
						com2.sendHex("0A");
						break;
				}
			}
		} catch (Exception e) {
			com2.sendTxt("打印异常: " + e.getMessage());
		}
	}

	private String getDateNow() {
		String result = "";
		if (TextUtils.isEmpty(sysDateTime)) {
			Calendar calendar = Calendar.getInstance();
			// 获取系统的日期
			// 年
			int year = calendar.get(Calendar.YEAR);
			// 月
			int month = calendar.get(Calendar.MONTH) + 1;
			// 日
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			// 小时
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			// 分钟
			int minute = calendar.get(Calendar.MINUTE);
			// 秒
			int second = calendar.get(Calendar.SECOND);
			result = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
		} else {
			result = sysDateTime;
		}

		return result;
	}

	public void doMopriaPrint(String filePath) {
		Intent intent = new Intent(Intent.ACTION_VIEW);

		String packageName = "org.mopria.printplugin";
		String className = "org.mopria.printplugin.DocumentRenderingActivity";
		intent.setClassName(packageName, className);
		//
		Uri data = null;
		//
		if (Build.VERSION.SDK_INT >= 23) {
			// 判断android版本大于等于7.0
			// 由于 从Android 7.0开始，不再允许在app中把file://
			// Uri暴露给其他app，否则应用会抛出FileUriExposedException
			// 使用FileProvider生成"content://" Uri来替代"file://" Uri，解决该异常
			data = FileProvider.getUriForFile(this, "com.test.printer.fileprovider", new File(filePath));
			// 给目标应用一个临时授权
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		} else {
			data = Uri.fromFile(new File(filePath));
		}
		intent.setData(data);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}


	public boolean isStart() {
		return isStart;
	}

	public void setStart(boolean isStart) {
		this.isStart = isStart;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
		sharedPreferences = getSharedPreferences("SystemInfo", 0);
		SharedPreferences.Editor localEditor = sharedPreferences.edit();
		localEditor.putString("deviceId", deviceId);
		localEditor.commit();
	}

	public String getSelfAdaption() {
		return selfAdaption;
	}

	public void setSelfAdaption(String selfAdaption) {
		this.selfAdaption = selfAdaption;
	}

	public void setFrist(String frist) {
		this.frist = frist;
		sharedPreferences = getSharedPreferences("SystemInfo", 0);
		SharedPreferences.Editor localEditor = sharedPreferences.edit();
		localEditor.putString("frist", frist);
		localEditor.commit();
	}

	public boolean isFrist() {
		return frist.equals("0");
	}

	public int getForTheFirst() {
		return forTheFirst;
	}

	public void setForTheFirst(int forTheFirst) {
		this.forTheFirst = forTheFirst;
	}

	public int getRechargeNum() {
		return rechargeNum;
	}

	public void setRechargeNum(int rechargeNum) {
		this.rechargeNum = rechargeNum;
		sharedPreferences = getSharedPreferences("SystemInfo", 0);
		SharedPreferences.Editor localEditor = sharedPreferences.edit();
		localEditor.putInt("rechargeNum", rechargeNum);
		localEditor.commit();
	}

	public SharedPreferences getSharedPreferences() {
		return sharedPreferences;
	}

	public void setSharedPreferences(SharedPreferences sharedPreferences) {
		this.sharedPreferences = sharedPreferences;
	}

	public String getSysDateTime() {
		return sysDateTime;
	}

	public void setSysDateTime(String sysDateTime) {
		this.sysDateTime = sysDateTime;
		sharedPreferences = getSharedPreferences("SystemInfo", 0);
		SharedPreferences.Editor localEditor = sharedPreferences.edit();
		localEditor.putString("sysDateTime", sysDateTime);
		localEditor.commit();
	}

	public String getStandbyTime() {
		return StandbyTime;
	}

	public void setStandbyTime(String standbyTime) {
		StandbyTime = standbyTime;
	}

	public String getInspectionTime() {
		return inspectionTime;
	}

	public void setInspectionTime(String inspectionTime) {
		this.inspectionTime = inspectionTime;
	}

	public String getPassagewayModel() {
		return passagewayModel;
	}

	public void setPassagewayModel(String passagewayModel) {
		this.passagewayModel = passagewayModel;
	}

	public String getDefaultPrinter() {
		return defaultPrinter;
	}

	public void setDefaultPrinter(String defaultPrinter) {
		this.defaultPrinter = defaultPrinter;
	}

	public String getBendiceliangzhi() {
		return bendiceliangzhi;
	}

	public void setBendiceliangzhi(String bendiceliangzhi) {
		this.bendiceliangzhi = bendiceliangzhi;
	}

	public String getYxsx() {
		return yxsx;
	}

	public void setYxsx(String yxsx) {
		this.yxsx = yxsx;
	}

	public String getBendiA() {
		return bendiA;
	}

	public void setBendiA(String bendiA) {
		this.bendiA = bendiA;
		sharedPreferences = getSharedPreferences("SystemInfo", 0);
		SharedPreferences.Editor localEditor = sharedPreferences.edit();
		localEditor.putString("bendiA", bendiA);
		localEditor.commit();
	}

	public String getBendiB() {
		return bendiB;
	}

	public void setBendiB(String bendiB) {
		this.bendiB = bendiB;
		sharedPreferences = getSharedPreferences("SystemInfo", 0);
		SharedPreferences.Editor localEditor = sharedPreferences.edit();
		localEditor.putString("bendiB", bendiB);
		localEditor.commit();
	}

	public String getApplicationTitle() {
		return applicationTitle;
	}

	public void setApplicationTitle(String titleContent) {
		this.applicationTitle = titleContent;
		sharedPreferences = getSharedPreferences("SystemInfo", 0);
		SharedPreferences.Editor localEditor = sharedPreferences.edit();
		localEditor.putString("applicationTitle", applicationTitle);
		localEditor.commit();
	}

	public String getKeShi() {
		return keShi;
	}

	public void setKeShi(String keShi) {
		this.keShi = keShi;
		sharedPreferences = getSharedPreferences("SystemInfo", 0);
		SharedPreferences.Editor localEditor = sharedPreferences.edit();
		localEditor.putString("keShi", keShi);
		localEditor.commit();
	}

	public String getJianYanYuan() {
		return jianYanYuan;
	}

	public void setJianYanYuan(String jianYanYuan) {
		this.jianYanYuan = jianYanYuan;
		sharedPreferences = getSharedPreferences("SystemInfo", 0);
		SharedPreferences.Editor localEditor = sharedPreferences.edit();
		localEditor.putString("jianYanYuan", jianYanYuan);
		localEditor.commit();
	}

	public String getYangxing1() {
		return yangxing1;
	}

	public void setYangxing1(String yangxing1) {
		this.yangxing1 = yangxing1;
	}

	public String getYangxing2() {
		return yangxing2;
	}

	public void setYangxing2(String yangxing2) {
		this.yangxing2 = yangxing2;
	}

	public String getYangxing3() {
		return yangxing3;
	}

	public void setYangxing3(String yangxing3) {
		this.yangxing3 = yangxing3;
	}

	public String getChangshuHL() {
		return changshuHL;
	}

	public void setChangshuHL(String changshuHL) {
		this.changshuHL = changshuHL;
	}

	public String getBuqueding() {
		return buqueding;
	}

	public void setBuqueding(String buqueding) {
		this.buqueding = buqueding;
	}

	public SerialportCom getSerialport() {
		return serialport;
	}

	private void loadSystemInit() {
		try {
			sharedPreferences = getSharedPreferences("SystemInfo", Context.MODE_PRIVATE);
			uuid = sharedPreferences.getString("uuid", "");
			deviceId = sharedPreferences.getString("deviceId", "");
			// TODO: 2020/5/16 0016
			selfAdaption = sharedPreferences.getString("selfAdaption", "0:41:40");
//			selfAdaption = sharedPreferences.getString("selfAdaption", "0:00:30");
			frist = sharedPreferences.getString("frist", "0");
			sysDateTime = sharedPreferences.getString("sysDateTime", "");
			StandbyTime = sharedPreferences.getString("StandbyTime", "0:10:00"); //进入待机的时间
			// TODO: 2020/5/16 0016
			inspectionTime = sharedPreferences.getString("inspectionTime", "0:04:10");
//			inspectionTime = sharedPreferences.getString("inspectionTime", "0:00:30");
			passagewayModel = sharedPreferences.getString("passagewayModel", "1");
			defaultPrinter = sharedPreferences.getString("defaultPrinter", "小票打印");
			//bendiceliangzhi = sharedPreferences.getString("bendiceliangzhi", "80");
			bendiceliangzhi = sharedPreferences.getString("bendiceliangzhi", "60"); //2019/10/08 本底量限更改为35 2020/07/08 40改为60
			applicationTitle = sharedPreferences.getString("applicationTitle", "");
			keShi = sharedPreferences.getString("keShi", "");
			jianYanYuan = sharedPreferences.getString("jianYanYuan", "");
			yxsx = sharedPreferences.getString("xysx", "99");
			bendiA = sharedPreferences.getString("bendiA", "0");
			bendiB = sharedPreferences.getString("bendiB", "0");
			yangxing1 = sharedPreferences.getString("yangxing1", "499");
			yangxing2 = sharedPreferences.getString("yangxing2", "1499");
			yangxing3 = sharedPreferences.getString("yangxing3", "2499");
			changshuHL = sharedPreferences.getString("changshuHL", "6,15"); //默认系数更改为H=6
			buqueding = sharedPreferences.getString("buqueding", "149");
			rechargeNum = sharedPreferences.getInt("rechargeNum", 80);
			forTheFirst = sharedPreferences.getInt("forTheFirst", 0);

			if (TextUtils.isEmpty(sysDateTime)) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date curDate = new Date(System.currentTimeMillis());
				sysDateTime = formatter.format(curDate);
			}

			if (TextUtils.isEmpty(uuid)) {
				Utils util = new Utils();
				UUID uuid = util.DeviceUuidFactory(this);
				this.uuid = uuid.toString();
				Editor editor = sharedPreferences.edit();
				editor.putString("uuid", this.uuid);
				editor.commit();
			}
		} catch (Exception e) {

		}
	}

	public void saveSystemInfo(String timeFormat) {
		sharedPreferences = getSharedPreferences("SystemInfo", 0);
		SharedPreferences.Editor localEditor = sharedPreferences.edit();
		localEditor.putString("uuid", uuid);
		localEditor.putString("selfAdaption", timeFormat);
		localEditor.putString("sysDateTime", sysDateTime);
		localEditor.putString("inspectionTime", timeFormat);
		localEditor.putString("passagewayModel", passagewayModel);
		localEditor.putString("defaultPrinter", defaultPrinter);
		localEditor.putString("bendiceliangzhi", bendiceliangzhi);
		localEditor.putString("yxsx", yxsx);
		localEditor.putString("bendiA", bendiA);
		localEditor.putString("bendiB", bendiB);
		localEditor.putString("yangxing1", yangxing1);
		localEditor.putString("yangxing2", yangxing2);
		localEditor.putString("yangxing3", yangxing3);
		localEditor.putString("changshuHL", changshuHL);
		localEditor.putString("buqueding", buqueding);
		localEditor.putInt("rechargeNum", rechargeNum);
		localEditor.putInt("forTheFirst", forTheFirst);
		try {
			SimpleDateFormat standbyDateFormat = new SimpleDateFormat("HH:mm:ss");

			// 检测时间

			java.util.Date inspectionDate = standbyDateFormat.parse(inspectionTime);
			int inspectionDateNum = ((inspectionDate.getHours() * 60) + inspectionDate.getMinutes()) * 60
					+ inspectionDate.getSeconds();

			String inspectionDateHex = Integer.toHexString(inspectionDateNum).toUpperCase();
			int[] inspectionDateNums = new int[2];

			switch (inspectionDateHex.length()) {
				case 1:
					inspectionDateHex = "000" + inspectionDateHex;
					inspectionDateNums[0] = 0;
					inspectionDateNums[1] = Integer.parseInt(inspectionDateHex, 16);
					break;
				case 2:
					inspectionDateHex = "00" + inspectionDateHex;
					inspectionDateNums[0] = 0;
					inspectionDateNums[1] = Integer.parseInt(inspectionDateHex, 16);
					break;
				case 3:
					inspectionDateHex = "0" + inspectionDateHex;
					inspectionDateNums[0] = Integer.parseInt(inspectionDateHex.substring(0, 2), 16);
					inspectionDateNums[1] = Integer.parseInt(inspectionDateHex.substring(2, 4), 16);
					break;
				case 4:
					inspectionDateNums[0] = Integer.parseInt(inspectionDateHex.substring(0, 2), 16);
					inspectionDateNums[1] = Integer.parseInt(inspectionDateHex.substring(2, 4), 16);
					break;
			}

			// 环境自适应
			java.util.Date selfAdaptionDate = standbyDateFormat.parse(selfAdaption);
			int selfAdaptionNum = ((selfAdaptionDate.getHours() * 60) + selfAdaptionDate.getMinutes()) * 60
					+ selfAdaptionDate.getSeconds();

			String selfAdaptionHex = Integer.toHexString(selfAdaptionNum).toUpperCase();
			int[] selfAdaptionNums = new int[2];

			switch (selfAdaptionHex.length()) {
				case 1:
					selfAdaptionHex = "000" + selfAdaptionHex;
					selfAdaptionNums[0] = 0;
					selfAdaptionNums[1] = Integer.parseInt(selfAdaptionHex, 16);
					break;
				case 2:
					selfAdaptionHex = "00" + selfAdaptionHex;
					selfAdaptionNums[0] = 0;
					selfAdaptionNums[1] = Integer.parseInt(selfAdaptionHex, 16);
					break;
				case 3:
					selfAdaptionHex = "0" + selfAdaptionHex;
					selfAdaptionNums[0] = Integer.parseInt(selfAdaptionHex.substring(0, 2), 16);
					selfAdaptionNums[1] = Integer.parseInt(selfAdaptionHex.substring(2, 4), 16);
					break;
				case 4:
					selfAdaptionNums[0] = Integer.parseInt(selfAdaptionHex.substring(0, 2), 16);
					selfAdaptionNums[1] = Integer.parseInt(selfAdaptionHex.substring(2, 4), 16);
					break;
			}

			// 本底A
			String bendiAHex = Integer.toHexString(Integer.parseInt(bendiA)).toUpperCase();
			int[] bendiANums = new int[2];

			switch (bendiAHex.length()) {
				case 1:
					bendiAHex = "000" + bendiAHex;
					bendiANums[0] = 0;
					bendiANums[1] = Integer.parseInt(bendiAHex, 16);
					break;
				case 2:
					bendiAHex = "00" + bendiAHex;
					bendiANums[0] = 0;
					bendiANums[1] = Integer.parseInt(bendiAHex, 16);
					break;
				case 3:
					bendiAHex = "0" + bendiAHex;
					bendiANums[0] = Integer.parseInt(bendiAHex.substring(0, 2), 16);
					bendiANums[1] = Integer.parseInt(bendiAHex.substring(2, 4), 16);
					break;
				case 4:
					bendiANums[0] = Integer.parseInt(bendiAHex.substring(0, 2), 16);
					bendiANums[1] = Integer.parseInt(bendiAHex.substring(2, 4), 16);
					break;
			}

			// 本底B
			String bendiBHex = Integer.toHexString(Integer.parseInt(bendiB)).toUpperCase();
			int[] bendiBNums = new int[2];

			switch (bendiBHex.length()) {
				case 1:
					bendiBHex = "000" + bendiBHex;
					bendiBNums[0] = 0;
					bendiBNums[1] = Integer.parseInt(bendiBHex, 16);
					break;
				case 2:
					bendiBHex = "00" + bendiBHex;
					bendiBNums[0] = 0;
					bendiBNums[1] = Integer.parseInt(bendiBHex, 16);
					break;
				case 3:
					bendiBHex = "0" + bendiBHex;
					bendiBNums[0] = Integer.parseInt(bendiBHex.substring(0, 2), 16);
					bendiBNums[1] = Integer.parseInt(bendiBHex.substring(2, 4), 16);
					break;
				case 4:
					bendiBNums[0] = Integer.parseInt(bendiBHex.substring(0, 2), 16);
					bendiBNums[1] = Integer.parseInt(bendiBHex.substring(2, 4), 16);
					break;
			}
			// 通道模式
			int model = passagewayModel.equals("1") ? 65 : 83;
			String modelHex = Integer.toHexString(model).toUpperCase();
			// 常数HL
			String[] strHL = changshuHL.split(",");
			String changshuHHex = Integer.toHexString(Integer.parseInt(strHL[0])).toUpperCase();

			if (changshuHHex.length() == 1) {
				changshuHHex = "0" + changshuHHex;
			}

			String changshuLHex = Integer.toHexString(Integer.parseInt(strHL[1])).toUpperCase();

			if (changshuLHex.length() == 1) {
				changshuLHex = "0" + changshuLHex;
			}

			int a = 11;
			int b = 20;
			int c = selfAdaptionNums[0];
			int d = selfAdaptionNums[1];
			int e = inspectionDateNums[0];
			int f = inspectionDateNums[1];
			int g = Integer.parseInt(strHL[0]);
			int h = Integer.parseInt(strHL[1]);
			int i = bendiANums[0];
			int j = bendiANums[1];
			int k = bendiBNums[0];
			int l = bendiBNums[1];
			int m = model; //可用模式，修改阴性和阳性的分割点位置
			int n = a ^ b ^ c ^ d ^ e ^ f ^ g ^ h ^ i ^ j ^ k ^ l ^ m;

			String yiHuoJiaoYan = Integer.toHexString(n).toUpperCase();

			if (!com1.isOpen()) {
				com1.setPort("/dev/ttyS3");
				com1.setBaudRate(9600);
				com1.open();
			}

			//参数设置同步两次，防止第一次同步失败
			com1.sendHex("AA0B14" + selfAdaptionHex + inspectionDateHex + changshuHHex + changshuLHex + bendiAHex
					+ bendiBHex + modelHex + yiHuoJiaoYan + "ED"); //设置系统参数
			Thread.sleep(1000);

			com1.sendHex("AA0B14" + selfAdaptionHex + inspectionDateHex + changshuHHex + changshuLHex + bendiAHex
					+ bendiBHex + modelHex + yiHuoJiaoYan + "ED"); //设置系统参数
			Thread.sleep(1000);


			String systemDateStr = sysDateTime.replace("-", "").replace(" ", "").replace(":", "");
			String dataStr = "";
			int dateHex = 7 ^ 144;
			for (int p = 0; p < systemDateStr.length(); p++) {
				int manager = Integer.parseInt(systemDateStr.substring(p, p + 2));
				dateHex = dateHex ^ manager;
				String managerStr = Integer.toHexString(manager).toUpperCase();
				dataStr += managerStr.length() == 1 ? "0" + managerStr : managerStr;
				p++;
			}

			//时间同步两次
			com1.sendHex("AA0790" + dataStr + Integer.toHexString(dateHex).toUpperCase() + "ED"); //设置采集板时间
			Thread.sleep(1000);

			com1.sendHex("AA0790" + dataStr + Integer.toHexString(dateHex).toUpperCase() + "ED"); //设置采集板时间
			Thread.sleep(1000);

		} catch (Exception e) {

		}
		localEditor.commit();
	}


	public void saveSystemInfo() {
		sharedPreferences = getSharedPreferences("SystemInfo", 0);
		SharedPreferences.Editor localEditor = sharedPreferences.edit();
		localEditor.putString("uuid", uuid);
		localEditor.putString("selfAdaption", selfAdaption);
		localEditor.putString("sysDateTime", sysDateTime);
		localEditor.putString("inspectionTime", inspectionTime);
		localEditor.putString("passagewayModel", passagewayModel);
		localEditor.putString("defaultPrinter", defaultPrinter);
		localEditor.putString("bendiceliangzhi", bendiceliangzhi);
		localEditor.putString("yxsx", yxsx);
		localEditor.putString("bendiA", bendiA);
		localEditor.putString("bendiB", bendiB);
		localEditor.putString("yangxing1", yangxing1);
		localEditor.putString("yangxing2", yangxing2);
		localEditor.putString("yangxing3", yangxing3);
		localEditor.putString("changshuHL", changshuHL);
		localEditor.putString("buqueding", buqueding);
		localEditor.putInt("rechargeNum", rechargeNum);
		localEditor.putInt("forTheFirst", forTheFirst);
		try {
			SimpleDateFormat standbyDateFormat = new SimpleDateFormat("HH:mm:ss");

			// 检测时间

			java.util.Date inspectionDate = standbyDateFormat.parse(inspectionTime);
			int inspectionDateNum = ((inspectionDate.getHours() * 60) + inspectionDate.getMinutes()) * 60
					+ inspectionDate.getSeconds();

			String inspectionDateHex = Integer.toHexString(inspectionDateNum).toUpperCase();
			int[] inspectionDateNums = new int[2];

			switch (inspectionDateHex.length()) {
				case 1:
					inspectionDateHex = "000" + inspectionDateHex;
					inspectionDateNums[0] = 0;
					inspectionDateNums[1] = Integer.parseInt(inspectionDateHex, 16);
					break;
				case 2:
					inspectionDateHex = "00" + inspectionDateHex;
					inspectionDateNums[0] = 0;
					inspectionDateNums[1] = Integer.parseInt(inspectionDateHex, 16);
					break;
				case 3:
					inspectionDateHex = "0" + inspectionDateHex;
					inspectionDateNums[0] = Integer.parseInt(inspectionDateHex.substring(0, 2), 16);
					inspectionDateNums[1] = Integer.parseInt(inspectionDateHex.substring(2, 4), 16);
					break;
				case 4:
					inspectionDateNums[0] = Integer.parseInt(inspectionDateHex.substring(0, 2), 16);
					inspectionDateNums[1] = Integer.parseInt(inspectionDateHex.substring(2, 4), 16);
					break;
			}

			// 环境自适应
			java.util.Date selfAdaptionDate = standbyDateFormat.parse(selfAdaption);
			int selfAdaptionNum = ((selfAdaptionDate.getHours() * 60) + selfAdaptionDate.getMinutes()) * 60
					+ selfAdaptionDate.getSeconds();

			String selfAdaptionHex = Integer.toHexString(selfAdaptionNum).toUpperCase();
			int[] selfAdaptionNums = new int[2];

			switch (selfAdaptionHex.length()) {
				case 1:
					selfAdaptionHex = "000" + selfAdaptionHex;
					selfAdaptionNums[0] = 0;
					selfAdaptionNums[1] = Integer.parseInt(selfAdaptionHex, 16);
					break;
				case 2:
					selfAdaptionHex = "00" + selfAdaptionHex;
					selfAdaptionNums[0] = 0;
					selfAdaptionNums[1] = Integer.parseInt(selfAdaptionHex, 16);
					break;
				case 3:
					selfAdaptionHex = "0" + selfAdaptionHex;
					selfAdaptionNums[0] = Integer.parseInt(selfAdaptionHex.substring(0, 2), 16);
					selfAdaptionNums[1] = Integer.parseInt(selfAdaptionHex.substring(2, 4), 16);
					break;
				case 4:
					selfAdaptionNums[0] = Integer.parseInt(selfAdaptionHex.substring(0, 2), 16);
					selfAdaptionNums[1] = Integer.parseInt(selfAdaptionHex.substring(2, 4), 16);
					break;
			}

			// 本底A
			String bendiAHex = Integer.toHexString(Integer.parseInt(bendiA)).toUpperCase();
			int[] bendiANums = new int[2];

			switch (bendiAHex.length()) {
				case 1:
					bendiAHex = "000" + bendiAHex;
					bendiANums[0] = 0;
					bendiANums[1] = Integer.parseInt(bendiAHex, 16);
					break;
				case 2:
					bendiAHex = "00" + bendiAHex;
					bendiANums[0] = 0;
					bendiANums[1] = Integer.parseInt(bendiAHex, 16);
					break;
				case 3:
					bendiAHex = "0" + bendiAHex;
					bendiANums[0] = Integer.parseInt(bendiAHex.substring(0, 2), 16);
					bendiANums[1] = Integer.parseInt(bendiAHex.substring(2, 4), 16);
					break;
				case 4:
					bendiANums[0] = Integer.parseInt(bendiAHex.substring(0, 2), 16);
					bendiANums[1] = Integer.parseInt(bendiAHex.substring(2, 4), 16);
					break;
			}

			// 本底B
			String bendiBHex = Integer.toHexString(Integer.parseInt(bendiB)).toUpperCase();
			int[] bendiBNums = new int[2];

			switch (bendiBHex.length()) {
				case 1:
					bendiBHex = "000" + bendiBHex;
					bendiBNums[0] = 0;
					bendiBNums[1] = Integer.parseInt(bendiBHex, 16);
					break;
				case 2:
					bendiBHex = "00" + bendiBHex;
					bendiBNums[0] = 0;
					bendiBNums[1] = Integer.parseInt(bendiBHex, 16);
					break;
				case 3:
					bendiBHex = "0" + bendiBHex;
					bendiBNums[0] = Integer.parseInt(bendiBHex.substring(0, 2), 16);
					bendiBNums[1] = Integer.parseInt(bendiBHex.substring(2, 4), 16);
					break;
				case 4:
					bendiBNums[0] = Integer.parseInt(bendiBHex.substring(0, 2), 16);
					bendiBNums[1] = Integer.parseInt(bendiBHex.substring(2, 4), 16);
					break;
			}
			// 通道模式
			int model = passagewayModel.equals("1") ? 65 : 83;
			String modelHex = Integer.toHexString(model).toUpperCase();
			// 常数HL
			String[] strHL = changshuHL.split(",");
			String changshuHHex = Integer.toHexString(Integer.parseInt(strHL[0])).toUpperCase();

			if (changshuHHex.length() == 1) {
				changshuHHex = "0" + changshuHHex;
			}

			String changshuLHex = Integer.toHexString(Integer.parseInt(strHL[1])).toUpperCase();

			if (changshuLHex.length() == 1) {
				changshuLHex = "0" + changshuLHex;
			}

			int a = 11;
			int b = 20;
			int c = selfAdaptionNums[0];
			int d = selfAdaptionNums[1];
			int e = inspectionDateNums[0];
			int f = inspectionDateNums[1];
			int g = Integer.parseInt(strHL[0]);
			int h = Integer.parseInt(strHL[1]);
			int i = bendiANums[0];
			int j = bendiANums[1];
			int k = bendiBNums[0];
			int l = bendiBNums[1];
			int m = model; //可用模式，修改阴性和阳性的分割点位置
			int n = a ^ b ^ c ^ d ^ e ^ f ^ g ^ h ^ i ^ j ^ k ^ l ^ m;

			String yiHuoJiaoYan = Integer.toHexString(n).toUpperCase();

			if (!com1.isOpen()) {
				com1.setPort("/dev/ttyS3");
				com1.setBaudRate(9600);
				com1.open();
			}

			//参数设置同步两次，防止第一次同步失败
			com1.sendHex("AA0B14" + selfAdaptionHex + inspectionDateHex + changshuHHex + changshuLHex + bendiAHex
					+ bendiBHex + modelHex + yiHuoJiaoYan + "ED"); //设置系统参数
			Thread.sleep(1000);

			com1.sendHex("AA0B14" + selfAdaptionHex + inspectionDateHex + changshuHHex + changshuLHex + bendiAHex
					+ bendiBHex + modelHex + yiHuoJiaoYan + "ED"); //设置系统参数
			Thread.sleep(1000);


			String systemDateStr = sysDateTime.replace("-", "").replace(" ", "").replace(":", "");
			String dataStr = "";
			int dateHex = 7 ^ 144;
			for (int p = 0; p < systemDateStr.length(); p++) {
				int manager = Integer.parseInt(systemDateStr.substring(p, p + 2));
				dateHex = dateHex ^ manager;
				String managerStr = Integer.toHexString(manager).toUpperCase();
				dataStr += managerStr.length() == 1 ? "0" + managerStr : managerStr;
				p++;
			}

			//时间同步两次
			com1.sendHex("AA0790" + dataStr + Integer.toHexString(dateHex).toUpperCase() + "ED"); //设置采集板时间
			Thread.sleep(1000);

			com1.sendHex("AA0790" + dataStr + Integer.toHexString(dateHex).toUpperCase() + "ED"); //设置采集板时间
			Thread.sleep(1000);

		} catch (Exception e) {

		}
		localEditor.commit();
	}


	public SerialControl0 getCom0() {
		return com0;
	}

	public SerialControl getCom1() {
		return com1;
	}

	public SerialControl getCom2() {
		return com2;
	}

	// 串口
	public class SerialControl extends SerialHelper {

		public SerialControl() {
		}

		@Override
		protected void onDataReceived(final ComBean ComRecData) {
			byte[] dataByte = ComRecData.bRec;
			byte[] checkedByed = Util.paraseReceiveData(dataByte);
			if (Util.isEmpty(checkedByed) || checkedByed.length < 5) {
				mLogger.debug("---处理接收的异常消息：{}", Util.byte2hex(dataByte));
				return;
			}
			String dataStr = byteToString(checkedByed) + " ";
			// DD 01 15 00 14 ED DD 01 26 00 27 ED
			if (!TextUtils.isEmpty(dataStr)) {
//				String[] datas = dataStr.split("ED");
//				for (int i = 0; i < datas.length; i++) {
					if (serialport != null) {
						serialport.SerialportListener(dataStr);
					}
//				}
			}
		}
	}

	public class SerialControl0 extends SerialHelper {

		public SerialControl0() {
		}

		@Override
		protected void onDataReceived(final ComBean ComRecData) {
			try {
				String dataStr = new String(ComRecData.bRec);
				if (dataStr.equals("port-s_00_port-e")) {
					com0.sendTxt("port-s_11_port-e");
					SendParmasToPc();

				}
				if (serialport0 != null) {
					serialport0.SerialportListener(dataStr);
				}
			} catch (Exception e) {
				String errorStr = e.getMessage();
			}
		}
	}

	public void SendParmasToPc() {
		try {
			SimpleDateFormat standbyDateFormat = new SimpleDateFormat("HH:mm:ss");
			java.util.Date inspectionDate = standbyDateFormat.parse(getInspectionTime());
			String[] hl = getChangshuHL().split(",");


			//发送给PC的本底值单位为每分钟
			String Ba = getBendiA();
			String Bb = getBendiB();
			String old_bdlx = getBendiceliangzhi();

			String new_bdlx = "";
			String newA = "";
			String newB = "";

			double bdlx = Double.parseDouble(old_bdlx) / 250 * 60;
			double fdba = Double.parseDouble(Ba) / 250 * 60;
			double fdbb = Double.parseDouble(Bb) / 250 * 60;

			if(fdba == 15728.4){
				fdba = 0;

			}

			if(fdbb == 15728.4){
				fdbb = 0;

			}

			wd = NumberFormat.getNumberInstance();
			// 保留两位小数
			wd.setMaximumFractionDigits(0);
			// 如果不需要四舍五入，可以使用RoundingMode.DOWN
			wd.setRoundingMode(RoundingMode.UP);

			newA = wd.format(fdba)+ "";
			newB = wd.format(fdbb)+ "";
			new_bdlx = wd.format(bdlx);

			// TODO: 2020/5/21 0021 两个版本，一个是deviceid,另一个是FR9202
			com0.sendTxt("param-s_" + ((inspectionDate.getHours() * 60) + inspectionDate.getMinutes()) + "_"
					+ inspectionDate.getSeconds() + "_" + hl[0] + "_" + hl[1] + "_" + newA + "_" + newB
					+ "_" + getBuqueding() + "_" + getYxsx() + "_" + getYangxing1() + "_" + getYangxing2() + "_"
					+ getYangxing3() + "_" + new_bdlx + "_" + "FR9202" + "_param-e"); //增加双通道机器型号FR9202

//			com0.sendTxt("param-s_" + ((inspectionDate.getHours() * 60) + inspectionDate.getMinutes()) + "_"
//					+ inspectionDate.getSeconds() + "_" + hl[0] + "_" + hl[1] + "_" + newA + "_" + newB
//					+ "_" + getBuqueding() + "_" + getYxsx() + "_" + getYangxing1() + "_" + getYangxing2() + "_"
//					+ getYangxing3() + "_" + new_bdlx + "_" + getDeviceId() + "_param-e"); //增加双通道机器型号FR9202
		} catch (Exception e) {

		}
	}

	private String byteToString(byte[] dataByte) {
		String stmp = "";
		StringBuilder sb = new StringBuilder("");
		for (int n = 0; n < dataByte.length; n++) {
			stmp = Integer.toHexString(dataByte[n] & 0xFF);
			sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
			sb.append(" ");
		}

		return sb.toString().toUpperCase().trim();
	}

	public void setParams(String key, String value) {
		sharedPreferences = getSharedPreferences("SystemInfo", 0);
		SharedPreferences.Editor localEditor = sharedPreferences.edit();
		localEditor.putString(key, value);
		localEditor.commit();
	}

	public String getParams(String key) {
		return sharedPreferences.getString(key, "");
	}

	private SerialportCom0 serialport0;
	private SerialportCom serialport;

	public interface SerialportCom0 {
		public void SerialportListener(String comStr);
	}

	public interface SerialportCom {
		public void SerialportListener(String comStr);
	}

	public void setOnSerialport0ComListener(SerialportCom0 serialport0) {
		this.serialport0 = serialport0;
	}

	public void setOnSerialportComListener(SerialportCom serialport) {
		this.serialport = serialport;
	}

	private PrintListener printListener;

	public interface PrintListener {
		public void PrintListenerHandler(Message msg);
	}

	public void setOnPrintListener(PrintListener printListener) {
		this.printListener = printListener;
	}

	class printHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if (printListener != null) {
				printListener.PrintListenerHandler(msg);
			}
		}
	}
}