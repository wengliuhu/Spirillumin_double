package com.example.spirillumin.zzcsoft.utils;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.example.spirillumin.R;

public class InputDialog {

	private Context mContext;
	private InputEvent inputEvent;
	private EditText input, input2;
	private Button btn;
	private Dialog dialog;

	private boolean isMode;

	public InputDialog(Context context) {
		this.mContext = context;
		dialog = new Dialog(context, R.style.dialog);
		dialog.setContentView(R.layout.dialog_input);
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				isMode = false;
				input2.setVisibility(View.GONE);
			}
		});

		input = (EditText) dialog.findViewById(R.id.editTextDialog);
		input2 = (EditText) dialog.findViewById(R.id.editTextDialog2);

		btn = (Button) dialog.findViewById(R.id.buttonDialogPositive);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String result = "";
				if (isMode) {
					// if (TextUtils.isEmpty(input.getText()) ||
					// TextUtils.isEmpty(input2.getText())) {
					// return;
					// }
					result = input.getText() + "," + input2.getText();
					if (inputEvent != null) {
						inputEvent.InputListener(result, true);
					}
				} else {
					// if (TextUtils.isEmpty(input.getText())) {
					// return;
					// }
					result = input.getText() + "";
					if (inputEvent != null) {
						inputEvent.InputListener(result, false);
					}
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

	public boolean isShowing() {
		return dialog.isShowing();
	}

	public void show(String hint, String context, int length, int inputType) {
		dialog.setCancelable(true);
		input.setFilters(new InputFilter[] { new InputFilter.LengthFilter(length) });
		if (inputType == 0) {
			input.setInputType(InputType.TYPE_CLASS_NUMBER);
		} else {
			input.setInputType(InputType.TYPE_CLASS_TEXT);
		}
		input.setText(context);
		input.setHint(hint);
		dialog.show();
	}

	public void showDialog(String hint, String context, int length) {
		dialog.setCancelable(false);
		input.setFilters(new InputFilter[] { new InputFilter.LengthFilter(length) });
		input.setText(context);
		input.setHint(hint);
		dialog.show();
	}

	public void showMode(String hint1, String hint2, String value1, String value2) {
		isMode = true;
		input2.setInputType(InputType.TYPE_CLASS_TEXT);
		input2.setVisibility(View.VISIBLE);
		dialog.setCancelable(true);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		input.setText(value1);
		input.setHint(hint1);

		input2.setText(value2);
		input2.setHint(hint2);
		dialog.show();
	}

	public void hide() {
		dialog.hide();
	}

	public void dismiss() {
		dialog.dismiss();
	}

	public interface InputEvent {
		public void InputListener(String resule, boolean mode);
	}

	public void setOnInputEventListener(InputEvent inputEvent) {
		this.inputEvent = inputEvent;
	}
}