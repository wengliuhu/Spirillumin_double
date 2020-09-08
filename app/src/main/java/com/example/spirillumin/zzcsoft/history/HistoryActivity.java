package com.example.spirillumin.zzcsoft.history;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.example.spirillumin.R;
import com.example.spirillumin.zzcsoft.adapter.RecordAdapter;
import com.example.spirillumin.zzcsoft.db.SqliteHelper;
import com.example.spirillumin.zzcsoft.spirilluminspection.BaseApplication;
import com.example.spirillumin.zzcsoft.spirilluminspection.MainActivity;
import com.example.spirillumin.zzcsoft.utils.LoadingDialog;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class HistoryActivity extends AppCompatActivity {

	private final int PDF_SAVE_START = 1;
	private final int PDF_SAVE_RESULT = 2;

	private ListView dataList;
	private RecordAdapter adapter;
	private BaseApplication baseApp;
	private SqliteHelper db;
	private LoadingDialog loading;

	private Date systemDateTime;
	private SimpleDateFormat formatter;

	private BaseFont bfChinese;
	private Font FontChinese24;
	private Font FontChinese18;
	private Font FontChinese16;
	private Font FontChinese12;
	private Font FontChinese11;

	private String pdfPath;
	private String rootPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_history);

		db = new SqliteHelper(this);

		init();
	}

	private void init() {
		try {
			rootPath = Environment.getExternalStorageDirectory().getAbsolutePath()
					+ getResources().getString(R.string.cache_path);
			baseApp = BaseApplication.getInstance();

			dataList = (ListView) findViewById(R.id.historyList);

			TextView oran = (TextView) findViewById(R.id.textViewAppOrgan);
			oran.setText(baseApp.getApplicationTitle().equals("单击此处编辑") ? "" : baseApp.getApplicationTitle());

			loading = new LoadingDialog(this);
			adapter = new RecordAdapter(this);

			dataList.setAdapter(adapter);

			String yaHeiFontName = getResources().getString(R.raw.simsun);
			yaHeiFontName += ",1";

			bfChinese = BaseFont.createFont(yaHeiFontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
			FontChinese24 = new Font(bfChinese, 24, Font.BOLD);
			FontChinese18 = new Font(bfChinese, 18, Font.NORMAL);
			FontChinese16 = new Font(bfChinese, 16, Font.NORMAL);
			FontChinese12 = new Font(bfChinese, 12, Font.NORMAL);
			FontChinese11 = new Font(bfChinese, 11, Font.NORMAL);

			formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			systemDateTime = formatter.parse(baseApp.getSysDateTime());
			handler.postDelayed(runnable, 1000);

			File file = new File(rootPath + "pdf/");
			if (!file.exists())
				file.mkdirs();
		} catch (Exception ex) {
			Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG);
		}
	}

	public void Main_OnClick(View v) {
		ArrayList<HashMap<String, Object>> dataList = adapter.getDatas();
		switch (v.getId()) {
			case R.id.imgButtonDelete: //删除所选记录
				boolean[] delChecks = adapter.getChecks();
				HashMap<String, Object> deleteMap = null;
				for (int i = 0; i < delChecks.length; i++) {
					if (delChecks[i]) {
						deleteMap = dataList.get(i);
						db.DeleteDataByCond("INSPECTION_INFO", "UUID", deleteMap.get("UUID") + "");
					}
				}
				adapter.dataReset();
				break;

			case R.id.imgButtonPrint: //打印所选记录小票
				boolean[] printChecks = adapter.getChecks();
				for (int i = 0; i < printChecks.length; i++) {
					if (printChecks[i])
						baseApp.Print(dataList.get(i));
				}
				break;

			case R.id.imgButtonBack: //返回到主界面
				baseApp.setSysDateTime(formatter.format(systemDateTime));
				startActivity(new Intent(this, MainActivity.class));
				finish();
				break;

			case R.id.imgButtonDeleteAll: //全部清空
				db.DeleteDataByCond("INSPECTION_INFO", "", "");
				adapter.dataReset();
				break;
		/*
		case R.id.imgButtonPrintA4:
			boolean[] printA4Checks = adapter.getChecks();
			for (int i = 0; i < printA4Checks.length; i++) {
				if (printA4Checks[i]) {
					turnToPdf(dataList.get(i));
					break;
				}
			}
			break;*/
		}
	}

	private Handler pdfPrintHandler = new Handler(new Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
				case PDF_SAVE_START:
					if (!loading.isShowing())
						loading.show("请求打印中，请稍候...");
					break;

				case PDF_SAVE_RESULT:
					if (loading.isShowing()) {
						loading.dismiss();
						doMopriaPrint(pdfPath);
					}
					break;
			}
			return false;
		}
	});

	public void doMopriaPrint(String filePath) {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW);

			String packageName = "org.mopria.printplugin";
			String className = "org.mopria.printplugin.DocumentRenderingActivity";
			intent.setClassName(packageName, className);

			Uri data = Uri.fromFile(new File(filePath));
			intent.setData(data);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		} catch (Exception e) {
			String errorString = e.getMessage();
		}
	}

	Handler handler = new Handler();
	Runnable runnable = new Runnable() {

		@Override
		public void run() {
			// 计时器
			try {
				addSecond();
			} catch (Exception e) {

			} finally {
				handler.postDelayed(this, 1000);
			}
		}

		public void addSecond() {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(systemDateTime);
			calendar.add(Calendar.SECOND, 1);
			systemDateTime = calendar.getTime();
		}
	};

	private void turnToPdf(final HashMap<String, Object> hashMap) {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		pdfPath = rootPath + "pdf/PDF_" + sdf.format(date) + ".pdf";
		pdfPrintHandler.sendEmptyMessage(PDF_SAVE_START);
		new Thread(new Runnable() {
			@Override
			public void run() {
				Document document = new Document(PageSize.A4);
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(pdfPath);
					PdfWriter.getInstance(document, fos);
					document.open();

					Image titlePng = Image.getInstance(rootPath + "picture/icon.png");
					// upgif.scalePercent(7.5f);
					titlePng.setAlignment(Element.ALIGN_CENTER);
					PdfPCell cell0 = new PdfPCell(titlePng);
					cell0.setFixedHeight(50);
					cell0.setBorder(0);

					// row0
					PdfPTable table0 = new PdfPTable(2);
					int width0[] = { 10, 90 };
					table0.setWidths(width0);

					table0.addCell(cell0);

					PdfPCell cell01 = new PdfPCell(new Paragraph("中国医科大学附属第一医院", FontChinese24));
					cell01.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell01.setBorder(0);
					table0.addCell(cell01);

					document.add(table0);

					PdfPTable table01 = new PdfPTable(1);
					int width01[] = { 100 };
					table01.setWidths(width01);

					PdfPCell cell012 = new PdfPCell(new Paragraph("呼气试验检验报告", FontChinese16));
					cell012.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell012.setBorder(0);

					table01.addCell(cell012);

					document.add(table01);

					Paragraph blankRow0 = new Paragraph(18f, " ", FontChinese18);
					document.add(blankRow0);

					// row1
					PdfPTable table1 = new PdfPTable(1);
					int width1[] = { 100 };
					table1.setWidths(width1);
					PdfPCell cell11 = new PdfPCell(new Paragraph("", FontChinese11));
					cell11.setBorder(1);
					table1.addCell(cell11);
					document.add(table1);

					Paragraph blankRow1 = new Paragraph(18f, " ", FontChinese18);
					document.add(blankRow1);

					// row2
					PdfPTable table2 = new PdfPTable(6);
					int width2[] = { 16, 17, 17, 17, 16, 17 };
					table2.setWidths(width2);
					PdfPCell cell21 = new PdfPCell(new Paragraph("患者姓名", FontChinese11));
					PdfPCell cell22 = new PdfPCell(new Paragraph(hashMap.get("USERNAME") + "", FontChinese11));
					PdfPCell cell23 = new PdfPCell(new Paragraph("年龄", FontChinese11));
					PdfPCell cell24 = new PdfPCell(new Paragraph("", FontChinese11));
					PdfPCell cell25 = new PdfPCell(new Paragraph("性别", FontChinese11));
					PdfPCell cell26 = new PdfPCell(new Paragraph("", FontChinese11));

					cell21.setFixedHeight(25);
					cell22.setFixedHeight(25);
					cell23.setFixedHeight(25);
					cell24.setFixedHeight(25);
					cell25.setFixedHeight(25);
					cell26.setFixedHeight(25);

					cell21.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell22.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell23.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell24.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell25.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell26.setHorizontalAlignment(Element.ALIGN_CENTER);

					cell21.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell22.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell23.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell24.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell25.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell26.setVerticalAlignment(Element.ALIGN_MIDDLE);

					table2.addCell(cell21);
					table2.addCell(cell22);
					table2.addCell(cell23);
					table2.addCell(cell24);
					table2.addCell(cell25);
					table2.addCell(cell26);

					document.add(table2);

					// row3
					PdfPTable table3 = new PdfPTable(6);
					int width3[] = { 16, 17, 17, 17, 16, 17 };
					table3.setWidths(width3);
					PdfPCell cell31 = new PdfPCell(new Paragraph("住院号", FontChinese11));
					PdfPCell cell32 = new PdfPCell(new Paragraph("", FontChinese11));
					PdfPCell cell33 = new PdfPCell(new Paragraph("日期", FontChinese11));
					PdfPCell cell34 = new PdfPCell(new Paragraph(hashMap.get("DTIME") + "", FontChinese11));
					PdfPCell cell35 = new PdfPCell(new Paragraph("电话", FontChinese11));
					PdfPCell cell36 = new PdfPCell(new Paragraph("", FontChinese11));

					cell31.setFixedHeight(25);
					cell32.setFixedHeight(25);
					cell33.setFixedHeight(25);
					cell34.setFixedHeight(25);
					cell35.setFixedHeight(25);
					cell36.setFixedHeight(25);

					cell31.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell32.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell33.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell34.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell35.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell36.setHorizontalAlignment(Element.ALIGN_CENTER);

					cell31.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell32.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell33.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell34.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell35.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell36.setVerticalAlignment(Element.ALIGN_MIDDLE);

					table3.addCell(cell31);
					table3.addCell(cell32);
					table3.addCell(cell33);
					table3.addCell(cell34);
					table3.addCell(cell35);
					table3.addCell(cell36);
					document.add(table3);

					// row4
					PdfPTable table4 = new PdfPTable(6);
					int width4[] = { 16, 17, 17, 17, 16, 17 };
					table4.setWidths(width4);
					PdfPCell cell41 = new PdfPCell(new Paragraph("送检医生", FontChinese11));
					PdfPCell cell42 = new PdfPCell(new Paragraph("", FontChinese11));
					PdfPCell cell43 = new PdfPCell(new Paragraph("样品编号", FontChinese11));
					PdfPCell cell44 = new PdfPCell(new Paragraph("", FontChinese11));
					PdfPCell cell45 = new PdfPCell(new Paragraph("编号", FontChinese11));
					PdfPCell cell46 = new PdfPCell(new Paragraph(hashMap.get("INDEX_NO") + "", FontChinese11));

					cell41.setFixedHeight(25);
					cell42.setFixedHeight(25);
					cell43.setFixedHeight(25);
					cell44.setFixedHeight(25);
					cell45.setFixedHeight(25);
					cell46.setFixedHeight(25);

					cell41.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell42.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell43.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell44.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell45.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell46.setHorizontalAlignment(Element.ALIGN_CENTER);

					cell41.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell42.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell43.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell44.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell45.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell46.setVerticalAlignment(Element.ALIGN_MIDDLE);

					table4.addCell(cell41);
					table4.addCell(cell42);
					table4.addCell(cell43);
					table4.addCell(cell44);
					table4.addCell(cell45);
					table4.addCell(cell46);
					document.add(table4);

					Paragraph blankRow2 = new Paragraph(18f, " ", FontChinese18);
					document.add(blankRow2);

					Image printPng = Image.getInstance(rootPath + "picture/inspection.png");
					// upgif.scalePercent(7.5f);
					printPng.setAlignment(Element.ALIGN_CENTER);

					document.add(printPng);

					Paragraph blankRow4 = new Paragraph(18f, " ", FontChinese18);
					document.add(blankRow4);

					// row6
					PdfPTable table6 = new PdfPTable(1);
					PdfPCell cell61 = new PdfPCell(
							new Paragraph("阳性参考值(Disintegrations Per Minute(DPM))", FontChinese11));
					cell61.setBorder(0);
					table6.addCell(cell61);
					document.add(table6);

					// row7
					PdfPTable table7 = new PdfPTable(3);
					int width7[] = { 33, 34, 33 };
					table7.setWidths(width7);

					PdfPCell cell71 = new PdfPCell(new Paragraph("DPM值", FontChinese12));
					PdfPCell cell72 = new PdfPCell(new Paragraph("dpm > 149", FontChinese12));
					PdfPCell cell73 = new PdfPCell(new Paragraph("阳性+", FontChinese12));

					cell71.setFixedHeight(35);
					cell72.setFixedHeight(35);
					cell73.setFixedHeight(35);

					cell71.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell72.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell73.setHorizontalAlignment(Element.ALIGN_CENTER);

					cell71.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell72.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell73.setVerticalAlignment(Element.ALIGN_MIDDLE);

					cell71.setBorder(1);
					cell72.setBorder(1);
					cell73.setBorder(1);

					table7.addCell(cell71);
					table7.addCell(cell72);
					table7.addCell(cell73);

					document.add(table7);

					Paragraph blankRow5 = new Paragraph(18f, " ", FontChinese18);
					document.add(blankRow5);

					// row8
					PdfPTable table8 = new PdfPTable(3);
					int width8[] = { 33, 34, 33 };
					table8.setWidths(width8);

					PdfPCell cell81 = new PdfPCell(new Paragraph("", FontChinese12));
					PdfPCell cell82 = new PdfPCell(new Paragraph("dpm ≤ 99", FontChinese12));
					PdfPCell cell83 = new PdfPCell(new Paragraph("阴性-", FontChinese12));

					cell81.setFixedHeight(35);
					cell82.setFixedHeight(35);
					cell83.setFixedHeight(35);

					cell81.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell82.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell83.setHorizontalAlignment(Element.ALIGN_CENTER);

					cell81.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell82.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell83.setVerticalAlignment(Element.ALIGN_MIDDLE);

					cell81.setBorder(0);
					cell82.setBorder(0);
					cell83.setBorder(0);

					table8.addCell(cell81);
					table8.addCell(cell82);
					table8.addCell(cell83);

					document.add(table8);

					Paragraph blankRow6 = new Paragraph(18f, " ", FontChinese18);
					document.add(blankRow6);

					// table9
					PdfPTable table9 = new PdfPTable(3);
					int width9[] = { 33, 34, 33 };
					table9.setWidths(width9);
					PdfPCell cell91 = new PdfPCell(new Paragraph("检测结果", FontChinese16));
					PdfPCell cell92 = new PdfPCell(new Paragraph("dpm = " + hashMap.get("PDM"), FontChinese16));
					PdfPCell cell93 = new PdfPCell(new Paragraph(hashMap.get("RESULT") + "", FontChinese16));

					cell91.setFixedHeight(45);
					cell92.setFixedHeight(45);
					cell93.setFixedHeight(45);

					cell91.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell92.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell93.setHorizontalAlignment(Element.ALIGN_CENTER);

					cell91.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell92.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell93.setVerticalAlignment(Element.ALIGN_MIDDLE);

					cell91.setBorder(1);
					cell92.setBorder(1);
					cell93.setBorder(1);

					table9.addCell(cell91);
					table9.addCell(cell92);
					table9.addCell(cell93);

					document.add(table9);

					Paragraph blankRow7 = new Paragraph(18f, " ", FontChinese18);
					document.add(blankRow7);

					// row10
					PdfPTable table10 = new PdfPTable(2);
					int width10[] = { 33, 67 };
					table10.setWidths(width10);
					PdfPCell cell101 = new PdfPCell(new Paragraph("医生建议", FontChinese11));
					PdfPCell cell102 = new PdfPCell(
							new Paragraph("请接受正规的根除幽门螺旋杆菌(HP)的治疗，并在治疗结束后一个月内复查。注意保持健康及规律饮食！", FontChinese11));

					cell101.setFixedHeight(45);
					cell102.setFixedHeight(45);

					cell101.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell102.setHorizontalAlignment(Element.ALIGN_CENTER);

					cell101.setVerticalAlignment(Element.ALIGN_TOP);
					cell102.setVerticalAlignment(Element.ALIGN_TOP);

					cell101.setBorder(1);
					cell102.setBorder(1);

					table10.addCell(cell101);
					table10.addCell(cell102);
					document.add(table10);

					// row11
					PdfPTable table11 = new PdfPTable(2);
					int width11[] = { 33, 67 };
					table11.setWidths(width11);
					PdfPCell cell111 = new PdfPCell(new Paragraph("温馨提示", FontChinese11));
					PdfPCell cell112 = new PdfPCell(new Paragraph("", FontChinese11));

					cell111.setFixedHeight(45);
					cell112.setFixedHeight(45);

					cell111.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell112.setHorizontalAlignment(Element.ALIGN_CENTER);

					cell111.setVerticalAlignment(Element.ALIGN_TOP);
					cell112.setVerticalAlignment(Element.ALIGN_TOP);

					cell111.setBorder(0);
					cell112.setBorder(0);

					table11.addCell(cell111);
					table11.addCell(cell112);
					document.add(table11);

					Paragraph blankRow8 = new Paragraph(18f, " ", FontChinese18);
					document.add(blankRow8);

					// row12
					PdfPTable table12 = new PdfPTable(2);
					int width12[] = { 60, 40 };
					table12.setWidths(width12);
					PdfPCell cell121 = new PdfPCell(new Paragraph("", FontChinese11));
					PdfPCell cell122 = new PdfPCell(new Paragraph("检测医生_____________", FontChinese11));

					cell121.setFixedHeight(45);
					cell122.setFixedHeight(45);

					cell121.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell122.setHorizontalAlignment(Element.ALIGN_CENTER);

					cell121.setVerticalAlignment(Element.ALIGN_TOP);
					cell122.setVerticalAlignment(Element.ALIGN_TOP);

					cell121.setBorder(0);
					cell122.setBorder(0);

					table12.addCell(cell121);
					table12.addCell(cell122);
					document.add(table12);

					document.close();
					fos.flush();
					fos.close();
					pdfPrintHandler.sendEmptyMessage(PDF_SAVE_RESULT);
				} catch (Exception e) {

				}
			}
		}).start();
	}
}