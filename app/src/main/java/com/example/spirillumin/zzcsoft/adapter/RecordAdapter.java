package com.example.spirillumin.zzcsoft.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.spirillumin.R;
import com.example.spirillumin.zzcsoft.db.SqliteHelper;

public class RecordAdapter extends BaseAdapter {

	private Context mContext;

	private SqliteHelper db;
	private ArrayList<HashMap<String, Object>> dataList;
	private boolean[] selectDatas;
	private boolean bgSwitch;

	private int maxCount = 0;

	public RecordAdapter(Context mContext) {
		try {
			this.mContext = mContext;
			db = new SqliteHelper(mContext);
			dataList = db.SelectDataByCond("INSPECTION_INFO", " ORDER BY DATE(DTIME) DESC, TIME(DTIME) DESC");
			selectDatas = new boolean[dataList.size()];
		} catch (Exception e) {

		}
	}

	public RecordAdapter(Context mContext, int maxCount) {
		try {
			this.mContext = mContext;
			this.maxCount = maxCount;
			db = new SqliteHelper(mContext);
			dataList = db.SelectDataByCond("INSPECTION_INFO",
					" ORDER BY DATE(DTIME) DESC, TIME(DTIME) DESC " + " Limit " + maxCount);
			selectDatas = new boolean[dataList.size()];
		} catch (Exception e) {

		}
	}

	public void dataReset() {
		if (maxCount != 0) {
			dataList = db.SelectDataByCond("INSPECTION_INFO",
					" ORDER BY DATE(DTIME) DESC, TIME(DTIME) DESC " + " Limit " + maxCount);
		} else {
			dataList = db.SelectDataByCond("INSPECTION_INFO", " ORDER BY DATE(DTIME) DESC, TIME(DTIME) DESC");
		}
		selectDatas = new boolean[dataList.size()];
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return dataList == null ? 0 : dataList.size();
	}

	@Override
	public HashMap<String, Object> getItem(int position) {
		try {
			return dataList == null ? null : dataList.get(position);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		try {
			ViewHolder viewHolder = null;
			if (null == convertView) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.item_record, parent, false);
				convertView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						CheckBox selectCheck = ((ViewHolder) v.getTag()).checkBoxCZ;
						int index = Integer.parseInt(selectCheck.getTag() + "");
						selectCheck.setChecked(!selectCheck.isChecked());
						selectDatas[index] = selectCheck.isChecked();
					}
				});
				viewHolder.textViewYPBH = (TextView) convertView.findViewById(R.id.textViewYPBH);
				viewHolder.textViewXM = (TextView) convertView.findViewById(R.id.textViewXM);
				viewHolder.textViewJCJG = (TextView) convertView.findViewById(R.id.textViewJCJG);
				viewHolder.textViewPDM = (TextView) convertView.findViewById(R.id.textViewPDM);
				viewHolder.textViewJCSJ = (TextView) convertView.findViewById(R.id.textViewJCSJ);
				viewHolder.textViewJCY = (TextView) convertView.findViewById(R.id.textViewJCY);
				viewHolder.checkBoxCZ = (CheckBox) convertView.findViewById(R.id.checkBoxCZ);

				viewHolder.checkBoxCZ.setChecked(false);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
				if (selectDatas[position]) {
					viewHolder.checkBoxCZ.setChecked(true);
				} else {
					viewHolder.checkBoxCZ.setChecked(false);
				}
			}

			viewHolder.checkBoxCZ.setTag(position + "");
			HashMap<String, Object> item = dataList.get(position);

			viewHolder.textViewYPBH.setText(item.get("INDEX_NO") + "");
			viewHolder.textViewXM.setText(item.get("USERNAME") + "");
			viewHolder.textViewJCJG.setText(item.get("RESULT") + "");
			viewHolder.textViewPDM.setText(item.get("PDM") + "");
			viewHolder.textViewJCSJ.setText(item.get("DTIME") + "");
			viewHolder.textViewJCY.setText(item.get("OPERATOR") + "");

			if (bgSwitch) {
				if(((String)item.get("RESULT")).contains("阳性")){
					viewHolder.textViewJCJG.setTextColor(android.graphics.Color.RED);
					viewHolder.textViewPDM.setTextColor(android.graphics.Color.RED);
					convertView.setBackgroundResource(R.drawable.listitem1);
				}else{

					viewHolder.textViewJCJG.setTextColor(android.graphics.Color.BLACK);
					viewHolder.textViewPDM.setTextColor(android.graphics.Color.BLACK);
					convertView.setBackgroundResource(R.drawable.listitem1);

				}

			} else {
				if(((String)item.get("RESULT")).contains("阳性")){
					viewHolder.textViewJCJG.setTextColor(android.graphics.Color.RED);
					viewHolder.textViewPDM.setTextColor(android.graphics.Color.RED);
					convertView.setBackgroundResource(R.drawable.listitem2);
				}else{
					viewHolder.textViewJCJG.setTextColor(android.graphics.Color.BLACK);
					viewHolder.textViewPDM.setTextColor(android.graphics.Color.BLACK);
					convertView.setBackgroundResource(R.drawable.listitem2);
				}

			}

			bgSwitch = !bgSwitch;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return convertView;
	}

	public boolean[] getChecks() {
		return selectDatas;
	}

	public ArrayList<HashMap<String, Object>> getDatas() {
		return dataList;
	}

	public final class ViewHolder {
		public TextView textViewYPBH;
		public TextView textViewXM;
		public TextView textViewJCJG;
		public TextView textViewPDM;
		public TextView textViewJCSJ;
		public TextView textViewJCY;
		public CheckBox checkBoxCZ;
	}

	private ItemCheckEvent itemCheck;

	public interface ItemCheckEvent {
		public void ItemCheckListener(int index);
	}

	public void setOnItemClickListener(ItemCheckEvent itemCheck) {
		this.itemCheck = itemCheck;
	}
}