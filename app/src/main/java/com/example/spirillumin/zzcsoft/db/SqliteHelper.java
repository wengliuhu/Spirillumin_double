package com.example.spirillumin.zzcsoft.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

public class SqliteHelper extends SQLiteOpenHelper {
	// 数据库版本号
	private static final int DATABASE_VERSION = 2;
	// 数据库名
	private static final String DATABASE_NAME = "aqi.db";
	// 检测记录表
	private String tableName0 = "INSPECTION_INFO";
	// 检测记录表列名:
	private String tableName0Columns = "INDEX_NO INTEGER PRIMARY KEY, UUID TEXT, USERNAME TEXT, RESULT TEXT, PDM TEXT, DTIME TEXT, OPERATOR TEXT, DEPARTMENT TEXT, STATE TEXT, PASSAGEWAY TEXT";
	// STATE 数据枚举: 0正常1危险2报警
	private SQLiteDatabase db = this.getReadableDatabase();

	// 构造函数，调用父类SQLiteOpenHelper的构造函数
	public SqliteHelper(Context context, String name, CursorFactory factory, int version,
						DatabaseErrorHandler errorHandler) {
		super(context, name, factory, version, errorHandler);

	}

	public SqliteHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		// SQLiteOpenHelper的构造函数参数:
		// context:上下文环境
		// name:数据库名字
		// factory:游标工厂（可选）
		// version:数据库模型版本号
	}

	public SqliteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// 数据库实际被创建是在getWritableDatabase()或getReadableDatabase()方法调用时

		// CursorFactory设置为null,使用系统默认的工厂类
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// 数据库第一次创建时onCreate()方法会被调用
		// onCreate方法有一个 SQLiteDatabase对象作为参数，根据需要对这个对象填充表和初始化数据

		DataTableCreate(db);
		// 即便程序修改重新运行，只要数据库已经创建过，就不会再进入这个onCreate方法
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// 如果DATABASE_VERSION值被改为别的数,系统发现现有数据库版本不同,即会调用onUpgrade
		// onUpgrade方法的三个参数，一个 SQLiteDatabase对象，一个旧的版本号和一个新的版本号
		// 这个方法中主要完成更改数据库版本的操作
		db.execSQL("DROP TABLE IF EXISTS " + tableName0);
		onCreate(db);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		// 每次打开数据库之后首先被执行
	}

	// 创建表
	private void DataTableCreate(SQLiteDatabase db) {
		String sql = "CREATE TABLE " + tableName0 + " (" + tableName0Columns + ")";
		db.execSQL(sql);
	}

	// 添加数据函数
	public String AddDataByCond(String tableName, HashMap<String, Object> params) {
		String result = "";
		try {
			StringBuffer columns = new StringBuffer();
			StringBuffer values = new StringBuffer();

			Iterator<Entry<String, Object>> iter = params.entrySet().iterator();
			Map.Entry<String, Object> entry;
			int index = 0;
			while (iter.hasNext()) {
				index++;
				entry = iter.next();
				columns.append(entry.getKey());
				values.append("'").append(entry.getValue()).append("'");
				if (index != params.size()) {
					columns.append(",");
					values.append(",");
				}
			}
			// INSERT INTO INSPECTION_INFO (STATE,RESULT,UUID,USERNAME,OPERATOR,DEPARTMENT,DTIME,PDM)VALUES('0','阳性++','25c3a5a0-d0e7-44f2-882f-7ef24bed7727','mayu','wjq','houqinbu','2017-12-22 16:44:20','1009')
			String sql = "INSERT INTO " + tableName + " (" + columns + ")" + "VALUES(" + values + ")";
			db.execSQL(sql);
			result = "ok";
		} catch (Exception e) {
			// TODO: handle exception
			result = e.getMessage();
		}
		return result;
	}

	// 删除数据函数
	public String DeleteDataByCond(String tableName, String whereString, String valueString) {
		String result = "";
		String sql = "";
		try {
			if (!TextUtils.isEmpty(valueString)) {
				sql = "DELETE FROM " + tableName + " WHERE " + whereString + " = '" + valueString + "'";
			} else {
				sql = "DELETE FROM " + tableName;
			}

			db.execSQL(sql);
			result = "ok";
		} catch (Exception e) {
			// TODO: handle exception
			result = e.getMessage();
		}
		return result;
	}

	// 修改数据函数
	public String UpdateDataByCond(String tableName, HashMap<String, Object> params, String primaryKey,
								   String primaryValue) {
		String result = "";
		try {
			StringBuffer items = new StringBuffer();

			Iterator<Entry<String, Object>> iter = params.entrySet().iterator();
			Map.Entry<String, Object> entry;
			int index = 0;
			while (iter.hasNext()) {
				index++;
				entry = iter.next();
				items.append(entry.getKey()).append(" = ").append("'").append(entry.getValue()).append("'");
				if (index != params.size()) {
					items.append(",");
				}
			}

			String sql = "UPDATE " + tableName + " SET " + items + " WHERE " + primaryKey + " = '" + primaryValue + "'";
			db.execSQL(sql);
			result = "ok";
		} catch (Exception e) {
			// TODO: handle exception
			result = e.getMessage();
		}
		return result;
	}

	// 查询函数
	public ArrayList<HashMap<String, Object>> SelectDataByCond(String tableName, String where) {
		ArrayList<HashMap<String, Object>> resultData = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> dataItem = null;
		Cursor cursor = null;
		try {
			String sql = "SELECT " + tableName + ".* FROM " + tableName + where;
			cursor = db.rawQuery(sql, null);
			cursor.moveToFirst();

			while (!cursor.isAfterLast()) {
				dataItem = new HashMap<String, Object>();
				for (int i = 0; i < cursor.getColumnCount(); i++) {
					dataItem.put(cursor.getColumnName(i), cursor.getString(i));
				}
				resultData.add(dataItem);
				cursor.moveToNext();
			}
		} catch (Exception e) {
			// TODO: handle exception
			String errorString = e.getMessage();
		}
		return resultData;
	}

}