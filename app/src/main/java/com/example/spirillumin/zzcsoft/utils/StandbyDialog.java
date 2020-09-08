package com.example.spirillumin.zzcsoft.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnHoverListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.spirillumin.R;
import com.example.spirillumin.zzcsoft.spirilluminspection.BaseApplication;

public class StandbyDialog {

	private Context mContext;
	private Dialog dialog;
	private View viewDialog;
	private BaseApplication baseApp;

	private BaseApplication.SerialControl com1;

	private double chaZhi;
	private boolean Inspection;

	public StandbyDialog(Context context) {
		this.mContext = context;

		LayoutInflater inflater = LayoutInflater.from(mContext);
		viewDialog = inflater.inflate(R.layout.dialog_standby, null);

		RelativeLayout rootLayout = (RelativeLayout) viewDialog.findViewById(R.id.standbyLayout);
		rootLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		rootLayout.setOnHoverListener(new OnHoverListener() {
			@Override
			public boolean onHover(View v, MotionEvent event) {
				int what = event.getAction();
				switch (what) {
					case MotionEvent.ACTION_HOVER_ENTER: // 鼠标进入view
						dismiss();
						break;
					case MotionEvent.ACTION_HOVER_MOVE: // 鼠标在view上
						dismiss();
						break;
					case MotionEvent.ACTION_HOVER_EXIT: // 鼠标离开view
						dismiss();
						break;
				}
				return false;
			}
		});

		dialog = new Dialog(context, R.style.dialog);

		baseApp = BaseApplication.getInstance();

		com1 = baseApp.getCom1();
	}

	BaseApplication.SerialportCom serialportCom = new BaseApplication.SerialportCom() {

		@Override
		public void SerialportListener(String comStr) {
			try {// DD 01 31 00 30
				if (comStr.length() > 0) {
					if (comStr.subSequence(0, 1).equals(" ")) {
						comStr = comStr.substring(1, comStr.length());
					}
				}
				String[] datas = comStr.split(" ");
				String comManager = datas[2];

				int cmd = Integer.parseInt(comManager);
				int dataLength = Integer.parseInt(datas[1]);

				switch (cmd) {
					case 10:
						// 单通道 DD 02 10 00 A0 B2
						String manager = "";
						for (int i = 0; i < dataLength; i++) {
							manager += datas[3 + i];
						}

						com1.sendHex("AA01110010ED");
						baseApp.PrintInspection(1);

						double managerBenDi = Integer.parseInt(manager, 16);
						double bendiA = Integer.parseInt(baseApp.getBendiA());

						chaZhi = difference(managerBenDi, bendiA);
						double bendiLiangXian = Integer.parseInt(baseApp.getBendiceliangzhi());

						if (bendiLiangXian < chaZhi) {
							com1.sendHex("AA01220023ED");
						}

						break;
					case 12:
						if (baseApp.isFrist()) {
							baseApp.setFrist("1");
							String passageway = "";
							for (int i = 0; i < dataLength; i++) {
								passageway += datas[3 + i];
							}
							baseApp.setBendiA(Integer.parseInt(passageway, 16) + "");

							com1.sendHex("AA01130012ED");
							baseApp.PrintInspection(0);
							Inspection = false;
						}
						break;
					case 50:
						// 双通道 DD 04 50 00 A0 00 00 F4 ED
						String manager1 = "";
						String manager2 = "";

						for (int i = 0; i < dataLength / 2; i++) {
							manager1 += datas[3 + i];
						}

						for (int i = 0; i < dataLength / 2; i++) {
							manager2 += datas[5 + i];
						}

						com1.sendHex("AA01110010ED");
						baseApp.PrintInspection(2);

						double managerBenDi1 = Integer.parseInt(manager1, 16);
						double bendiA1 = Integer.parseInt(baseApp.getBendiA());

						chaZhi = difference(managerBenDi1, bendiA1);
						double bendiLiangXian1 = Integer.parseInt(baseApp.getBendiceliangzhi());

						if (bendiLiangXian1 < chaZhi) {
							baseApp.setFrist("0");
							com1.sendHex("AA01220023ED");
						} else {
							double managerBenDi2 = Integer.parseInt(manager2, 16);
							double bendiA2 = Integer.parseInt(baseApp.getBendiB());

							chaZhi = difference(managerBenDi2, bendiA2);
							double bendiLiangXian2 = Integer.parseInt(baseApp.getBendiceliangzhi());

							if (bendiLiangXian2 < chaZhi) {
								com1.sendHex("AA01220023ED");
							}
						}
						break;
					case 51:
						if (baseApp.isFrist()) {
							baseApp.setFrist("1");
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

							com1.sendHex("AA01130012ED");
							Inspection = false;
						}
						break;
				}
			} catch (Exception e) {

			}
		}
	};

	private double difference(double one, double two) {
		double bad;
		if (one > two)
			return bad = one - two;
		else
			return bad = two - one;
	}

	public boolean isShowing() {
		return dialog.isShowing();
	}

	public void show(Activity activity) {
		Display display = activity.getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		// 设置dialog的宽高为屏幕的宽高
		ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width, height);
		dialog.setContentView(viewDialog, layoutParams);
		dialog.show();

		if (!Inspection) {
			Inspection = true;
			baseApp.setOnSerialportComListener(serialportCom);
			com1.sendHex("AA01220023ED");
		}
	}

	public void hide() {
		dialog.hide();
	}

	public void dismiss() {
		if (standBack != null)
			standBack.StandBackListener();
		com1.sendHex("AA01290028ED");
		Inspection = false;
		dialog.dismiss();
	}

	public void Inspection() {
		if (!Inspection) {
			Inspection = true;
			com1.sendHex("AA01220023ED");
		}
	}

	private StandBack standBack;

	public interface StandBack {
		public void StandBackListener();
	}

	public void setOnStandBackComListener(StandBack standBack) {
		this.standBack = standBack;
	}
}