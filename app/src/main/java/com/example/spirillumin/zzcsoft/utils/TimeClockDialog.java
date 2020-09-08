package com.example.spirillumin.zzcsoft.utils;

import java.util.Calendar;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;

import com.example.spirillumin.R;
import com.example.spirillumin.wheel.StrericWheelAdapter;
import com.example.spirillumin.wheel.WheelView;

public class TimeClockDialog {

	private Context mContext;
	private TimeCheckEvent timeCheckEvent;
	private WheelView hourWheel, minuteWheel, secondWheel;

	public static String[] hourContent = null;
	public static String[] minuteContent = null;
	public static String[] secondContent = null;

	private Calendar calendar = Calendar.getInstance();

	private Button btn;
	private Dialog dialog;

	public TimeClockDialog(Context context) {
		this.mContext = context;
		dialog = new Dialog(context, R.style.dialog);
		dialog.setContentView(R.layout.dialog_timeclock);

		initContent();

		int curHour = calendar.get(Calendar.HOUR_OF_DAY);
		int curMinute = calendar.get(Calendar.MINUTE);
		int curSecond = calendar.get(Calendar.SECOND);

		hourWheel = (WheelView) dialog.findViewById(R.id.hourwheel);
		minuteWheel = (WheelView) dialog.findViewById(R.id.minutewheel);
		secondWheel = (WheelView) dialog.findViewById(R.id.secondwheel);

		hourWheel.setAdapter(new StrericWheelAdapter(hourContent));
		hourWheel.setCurrentItem(curHour);
		hourWheel.setCyclic(true);
		hourWheel.setInterpolator(new AnticipateOvershootInterpolator());

		minuteWheel.setAdapter(new StrericWheelAdapter(minuteContent));
		minuteWheel.setCurrentItem(curMinute);
		minuteWheel.setCyclic(true);
		minuteWheel.setInterpolator(new AnticipateOvershootInterpolator());

		secondWheel.setAdapter(new StrericWheelAdapter(secondContent));
		secondWheel.setCurrentItem(curSecond);
		secondWheel.setCyclic(true);
		secondWheel.setInterpolator(new AnticipateOvershootInterpolator());
		btn = (Button) dialog.findViewById(R.id.buttonDialogPositive);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String h = hourWheel.getCurrentItemValue();
				String m = minuteWheel.getCurrentItemValue();
				String s = secondWheel.getCurrentItemValue();

				if (timeCheckEvent != null) {
					timeCheckEvent.TimeCheckListener(h, m, s);
				}
				dismiss();
			}
		});
		Button btn2 = (Button) dialog.findViewById(R.id.buttonDialogNeutral);
		btn2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}

	public void initContent() {
		hourContent = new String[24];
		for (int i = 0; i < 24; i++) {
			hourContent[i] = String.valueOf(i);
			if (hourContent[i].length() < 2) {
				hourContent[i] = "0" + hourContent[i];
			}
		}

		minuteContent = new String[60];
		for (int i = 0; i < 60; i++) {
			minuteContent[i] = String.valueOf(i);
			if (minuteContent[i].length() < 2) {
				minuteContent[i] = "0" + minuteContent[i];
			}
		}
		secondContent = new String[60];
		for (int i = 0; i < 60; i++) {
			secondContent[i] = String.valueOf(i);
			if (secondContent[i].length() < 2) {
				secondContent[i] = "0" + secondContent[i];
			}
		}
	}

	public boolean isShowing() {
		return dialog.isShowing();
	}

	public void show() {
		dialog.show();
	}

	public void hide() {
		dialog.hide();
	}

	public void dismiss() {
		dialog.dismiss();
	}

	public interface TimeCheckEvent {
		public void TimeCheckListener(String h, String m, String s);
	}

	public void setOnTimeCheckListener(TimeCheckEvent timeCheckEvent) {
		this.timeCheckEvent = timeCheckEvent;
	}
}