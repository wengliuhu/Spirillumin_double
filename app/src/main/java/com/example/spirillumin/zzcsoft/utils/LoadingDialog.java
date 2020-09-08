package com.example.spirillumin.zzcsoft.utils;


import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.spirillumin.R;

public class LoadingDialog {

	Handler handler = new Handler();

	private Context mContext;
	private TextView textView;
	private Dialog dialog;

	private Animation animation;
	private ImageView imageView;

	public LoadingDialog(Context context) {
		this.mContext = context;
		dialog = new Dialog(context, R.style.dialog);
		dialog.setContentView(R.layout.dialog_loading);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);

		textView = (TextView) dialog.findViewById(R.id.loadingmsg);

		animation = AnimationUtils.loadAnimation(mContext, R.anim.loading_animation);
		imageView = (ImageView) dialog.findViewById(R.id.loadingImage);
	}

	public boolean isShowing() {
		return dialog.isShowing();
	}

	public void show(String msg) {
		try {
			textView.setText(msg);
			dialog.show();
			imageView.startAnimation(animation);
		} catch (Exception e) {

		}
	}

	public void hide() {
		dialog.hide();
	}

	public void dismiss() {
		dialog.dismiss();
	}

	public void setText(String str) {
		textView.setText(str);
	}
}