package com.example.spirillumin.zzcsoft.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;


import com.example.spirillumin.zzcsoft.spirilluminspection.BaseApplication;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

public class Utils {

	public static int getVersionCode(Context context) {
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return pi.versionCode;
		} catch (NameNotFoundException e) {

			return 0;
		}
	}

	public UUID DeviceUuidFactory(Context context) {
		UUID uuid = null;
		String PREFS_FILE = "device_id.xml";
		String PREFS_DEVICE_ID = "device_id";
		SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, 0);
		String id = prefs.getString(PREFS_DEVICE_ID, null);
		if (id != null) {
			uuid = UUID.fromString(id);
		} else {
			final String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

			try {
				if (!"9774d56d682e549c".equals(androidId)) {
					uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
				} else {
					final String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
							.getDeviceId();
					uuid = deviceId != null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.randomUUID();
				}
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
			prefs.edit().putString(PREFS_DEVICE_ID, uuid.toString()).commit();
		}
		return uuid;
	}

	public Font setChineseFont() {
		BaseFont bf = null;
		Font fontChinese = null;
		try {
			// STSong-Light : Adobe的字体
			// UniGB-UCS2-H : pdf 字体
			bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H",
					BaseFont.NOT_EMBEDDED);
			fontChinese = new Font(bf, 12, Font.NORMAL);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fontChinese;
	}

	public Bitmap GetSmallBitmap(String filePath, int width, int height) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		BitmapFactory.decodeFile(filePath, options);

		int inSampleSize = 1;

		int newHeight = options.outHeight;
		int newWidth = options.outWidth;
		if (newHeight > height || newWidth > width) {
			int heightRatio = Math.round((float) newHeight / (float) height);
			int widthRatio = Math.round((float) newWidth / (float) width);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		options.inSampleSize = inSampleSize;
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeFile(filePath, options);
	}

	public int getFileStreamLength(InputStream inputStream) {
		byte[] buf = new byte[1024];
		int len = -1;
		int size = 0;
		try {
			while ((len = inputStream.read(buf)) != -1) {
				size += len;
			}
		} catch (Exception e) {
			return 0;
		}
		return size;
	}

	// 判断奇数或偶数，位运算，最后一位是1则为奇数，为0是偶数
	public int isOdd(int num) {
		return num & 0x1;
	}

	public int HexToInt(String inHex)// Hex字符串转int
	{
		return Integer.parseInt(inHex, 16);
	}

	public byte HexToByte(String inHex)// Hex字符串转byte
	{
		return (byte) Integer.parseInt(inHex, 16);
	}

	public String Byte2Hex(Byte inByte)// 1字节转2个Hex字符
	{
		return String.format("%02x", inByte).toUpperCase();
	}

	public String ByteArrToHex(byte[] inBytArr)// 字节数组转转hex字符串
	{
		StringBuilder strBuilder = new StringBuilder();
		int j = inBytArr.length;
		for (int i = 0; i < j; i++) {
			strBuilder.append(Byte2Hex(inBytArr[i]));
			strBuilder.append(" ");
		}
		return strBuilder.toString();
	}

	public String ByteArrToHex(byte[] inBytArr, int offset, int byteCount)// 字节数组转转hex字符串，可选长度
	{
		StringBuilder strBuilder = new StringBuilder();
		int j = byteCount;
		for (int i = offset; i < j; i++) {
			strBuilder.append(Byte2Hex(inBytArr[i]));
		}
		return strBuilder.toString();
	}

	// 转hex字符串转字节数组
	public byte[] HexToByteArr(String inHex)// hex字符串转字节数组
	{
		int hexlen = inHex.length();
		byte[] result;
		if (isOdd(hexlen) == 1) {// 奇数
			hexlen++;
			result = new byte[(hexlen / 2)];
			inHex = "0" + inHex;
		} else {// 偶数
			result = new byte[(hexlen / 2)];
		}
		int j = 0;
		for (int i = 0; i < hexlen; i += 2) {
			result[j] = HexToByte(inHex.substring(i, i + 2));
			j++;
		}
		return result;
	}

	public String getFilePath() {
		String file_dir = "";
		// SD卡是否存在
		boolean isSDCardExist = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
		// Environment.getExternalStorageDirectory()相当于File file=new
		// File("/sdcard")
		boolean isRootDirExist = Environment.getExternalStorageDirectory().exists();
		if (isSDCardExist && isRootDirExist) {
			file_dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/BouncingDevice/excel/";
		} else {
			file_dir = BaseApplication.getInstance().getFilesDir().getAbsolutePath() + "/BouncingDevice/excel/";
		}
		File dir = new File(file_dir);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return file_dir;
	}

	public void fileCreate(String filePath) {
		File file = new File(filePath);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
	}

	public double getAQI(String aqi05) {
		// 计算公式
		double result = 0.0;
		try {
			result = 0.118 * (Double.parseDouble(aqi05) / 2830);
		} catch (Exception e) {

		}
		return result;
	}
}