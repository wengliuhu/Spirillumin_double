package com.example.spirillumin.zzcsoft.utils;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.example.spirillumin.R;
import com.example.spirillumin.zzcsoft.spirilluminspection.BaseApplication;

public class RechargeDialog {

	private TextView textViewRecharge;

	private TextView textViewNum;

	private Context mContext;
	private Dialog dialog;

	private BaseApplication baseApp;

	private BaseApplication.SerialControl com1;

	public RechargeDialog(Context context) {
		this.mContext = context;
		this.baseApp = BaseApplication.getInstance();
		dialog = new Dialog(context, R.style.dialog);
		dialog.setContentView(R.layout.dialog_recharge);
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if (rechargeBack != null)
					rechargeBack.RechargeBackListener();
				com1.sendHex("AA01550054ED");
			}
		});

		baseApp = BaseApplication.getInstance();

		try {
			com1 = baseApp.getCom1();

			if (!com1.isOpen()) {
				com1.setPort("/dev/ttyS3");
				com1.setBaudRate(9600);
				com1.open();
			}
		} catch (Exception e) {

		}

		textViewRecharge = (TextView) dialog.findViewById(R.id.textViewRecharge);
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.arg1) {
				case 0:
					break;
				case 16:
					//textViewRecharge.setText("充值卡无效");
					textViewRecharge.setText("无效卡");
					break;
				case 18:
					String content = "成功充值" + msg.arg2 + "次";
					textViewRecharge.setText(content);
					break;
			}
		}
	};

	BaseApplication.SerialportCom serialportCom = new BaseApplication.SerialportCom() {

		@Override
		public void SerialportListener(String comStr) {
			Message msg = handler.obtainMessage();
			msg.arg1 = 0;
			try {
				if (comStr.length() > 0) {
					if (comStr.subSequence(0, 1).equals(" ")) {
						comStr = comStr.substring(1, comStr.length());
					}
				}
				String[] datas = comStr.split(" ");
				int cmd = Integer.parseInt(datas[2]);
				int dataLength = Integer.parseInt(datas[1]);

				switch (cmd) {
					case 16:
						// DD 01 16 00 17
						// 充值卡无效
						msg.arg1 = 16;
						break;
					case 18:
						// DD 02 18 02 10 08
						// 剩余次数
						String manager = "";
						for (int i = 0; i < dataLength; i++) {
							manager += datas[3 + i];
						}

						int managerCiShu = Integer.parseInt(manager, 16);

						baseApp.setRechargeNum(baseApp.getRechargeNum() + managerCiShu);

						msg.arg1 = 18;
						msg.arg2 = managerCiShu;
						break;
				}
			} catch (Exception e) {
				msg.obj = e.getMessage();
			} finally {
				handler.sendMessage(msg);
			}
		}
	};

	public void show() {
		dialog.show();
		baseApp.setOnSerialportComListener(serialportCom);
		com1.sendHex("AA01530052ED");
	}

	public boolean isShowing() {
		return dialog.isShowing();
	}

	public void hide() {
		dialog.hide();
	}

	private RechargeBack rechargeBack;

	public interface RechargeBack {
		public void RechargeBackListener();
	}

	public void setOnRechargeBackBackComListener(RechargeBack rechargeBack) {
		this.rechargeBack = rechargeBack;
	}
}
