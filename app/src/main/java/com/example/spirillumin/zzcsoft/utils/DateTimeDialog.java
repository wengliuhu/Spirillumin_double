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

public class DateTimeDialog {

	private Context mContext;
	private DateTimeCheckEvent timeCheckEvent;
	private WheelView yearWheel, monthWheel, dayWheel, hourWheel, minuteWheel, secondWheel;
	private Button btn;
	private Dialog dialog;

	public static String[] yearContent = null;
	public static String[] monthContent = null;
	public static String[] dayContent = null;
	public static String[] hourContent = null;
	public static String[] minuteContent = null;
	public static String[] secondContent = null;

	private Calendar calendar = Calendar.getInstance();

	public DateTimeDialog(Context context) {
		this.mContext = context;
		dialog = new Dialog(context, R.style.dialog);
		dialog.setContentView(R.layout.dialog_datetime);

		initContent();

		int curYear = calendar.get(Calendar.YEAR);
		int curMonth = calendar.get(Calendar.MONTH) + 1;
		int curDay = calendar.get(Calendar.DAY_OF_MONTH);
		int curHour = calendar.get(Calendar.HOUR_OF_DAY);
		int curMinute = calendar.get(Calendar.MINUTE);
		int curSecond = calendar.get(Calendar.SECOND);

		yearWheel = (WheelView) dialog.findViewById(R.id.yearwheel);
		monthWheel = (WheelView) dialog.findViewById(R.id.monthwheel);
		dayWheel = (WheelView) dialog.findViewById(R.id.daywheel);
		hourWheel = (WheelView) dialog.findViewById(R.id.hourwheel);
		minuteWheel = (WheelView) dialog.findViewById(R.id.minutewheel);
		secondWheel = (WheelView) dialog.findViewById(R.id.secondwheel);

		yearWheel.setAdapter(new StrericWheelAdapter(yearContent));
		yearWheel.setCurrentItem(curYear - 2013);
		yearWheel.setCyclic(true);
		yearWheel.setInterpolator(new AnticipateOvershootInterpolator());

		monthWheel.setAdapter(new StrericWheelAdapter(monthContent));

		monthWheel.setCurrentItem(curMonth - 1);

		monthWheel.setCyclic(true);
		monthWheel.setInterpolator(new AnticipateOvershootInterpolator());

		dayWheel.setAdapter(new StrericWheelAdapter(dayContent));
		dayWheel.setCurrentItem(curDay - 1);
		dayWheel.setCyclic(true);
		dayWheel.setInterpolator(new AnticipateOvershootInterpolator());

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

		dialog.setTitle("ѡȡʱݤ");

		btn = (Button) dialog.findViewById(R.id.buttonDialogPositive);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String y = yearWheel.getCurrentItemValue();
				String mm = monthWheel.getCurrentItemValue();
				String d = dayWheel.getCurrentItemValue();
				String h = hourWheel.getCurrentItemValue();
				String m = minuteWheel.getCurrentItemValue();
				String s = secondWheel.getCurrentItemValue();

				if (timeCheckEvent != null) {
					timeCheckEvent.TimeCheckListener(y, mm, d, h, m, s);
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
		yearContent = new String[18];
		for (int i = 0; i < 18; i++)
			yearContent[i] = String.valueOf(i + 2013);

		monthContent = new String[12];
		for (int i = 0; i < 12; i++) {
			monthContent[i] = String.valueOf(i + 1);
			if (monthContent[i].length() < 2) {
				monthContent[i] = "0" + monthContent[i];
			}
		}

		dayContent = new String[31];
		for (int i = 0; i < 31; i++) {
			dayContent[i] = String.valueOf(i + 1);
			if (dayContent[i].length() < 2) {
				dayContent[i] = "0" + dayContent[i];
			}
		}
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

	public interface DateTimeCheckEvent {
		public void TimeCheckListener(String y, String mm, String d, String h, String m, String s);
	}

	public void setOnDateTimeCheckListener(DateTimeCheckEvent timeCheckEvent) {
		this.timeCheckEvent = timeCheckEvent;
	}
}