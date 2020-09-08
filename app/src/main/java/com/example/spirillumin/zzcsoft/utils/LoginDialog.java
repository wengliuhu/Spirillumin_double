package com.example.spirillumin.zzcsoft.utils;


import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.example.spirillumin.R;
import com.example.spirillumin.zzcsoft.spirilluminspection.BaseApplication;

public class LoginDialog {

	private Context mContext;
	private PassWordEvent passwordEvent;
	private EditText inputPassWord;
	private Button btn;
	private Dialog dialog;
	private Button btnlike;

	public LoginDialog(Context context) {
		this.mContext = context;
		dialog = new Dialog(context, R.style.dialog);
		dialog.setContentView(R.layout.dialog_login);

		inputPassWord = (EditText) dialog.findViewById(R.id.editTextDialogPassWord);

		btn = (Button) dialog.findViewById(R.id.buttonDialogPositive);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String inputStr = inputPassWord.getText() + "";
				if (TextUtils.isEmpty(inputStr)) {
					inputPassWord.setHint("密码不能为空");
				} else {
					if (inputStr.equals("fradmin")) {
						// 管理员
						if (passwordEvent != null)
							passwordEvent.PassWordListener(0);
						dismiss();
					} else if (BaseApplication.getInstance().getParams("password").equals(inputStr)) {
						// 操作员
						if (passwordEvent != null)
							passwordEvent.PassWordListener(1);
						dismiss();
					} else {
						inputPassWord.setText("");
						inputPassWord.setHint("密码输入错误");
					}
				}
			}
		});
		btnlike = (Button) dialog.findViewById(R.id.buttonDialogNeutral);
		btnlike.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				inputPassWord.setText("");
				dismiss();
			}
		});
	}

	public boolean isShowing() {
		return dialog.isShowing();
	}

	public void show(boolean isCancelable) {
		dialog.setCancelable(isCancelable);
		dialog.show();
	}

	public void hide() {
		dialog.hide();
	}

	public void dismiss() {
		dialog.dismiss();
	}

	public interface PassWordEvent {
		public void PassWordListener(int state);
	}

	public void setOnPassWordListener(PassWordEvent passwordEvent) {
		this.passwordEvent = passwordEvent;
	}
}