package com.example.spirillumin.zzcsoft.utils;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.util.DisplayMetrics;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * author: created by wengliuhu
 * time: 2019/7/8 16
 */
public class Util {

    public static boolean isEmpty(List list) {
        return list == null || list.size() == 0;
    }

    public static boolean isEmpty(Map map) {
        return map == null || map.size() == 0;
    }

    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(byte[] array) {
        return array == null || array.length == 0;
    }

    /**
     *   * 获取android当前可用运行内存大小
     *   * @param context
     *   *
     */
    public static String getAvailMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        // mi.availMem; 当前系统的可用内存
        return Formatter.formatFileSize(context, mi.availMem);// 将获取的内存大小规格化
    }

    /**
     * 回收bitmap
     * @param bmp
     */
    public static void recycleBmp(Bitmap bmp){
        if (bmp != null)
        {
            bmp.recycle();
            bmp = null;
        }
    }

    /**
     * 剪裁bmp
     * @param bmp
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @return
     */
    public static Bitmap cropBmp(Bitmap bmp, float left, float top, float right, float bottom, float offset){

        System.out.println("--------left:" + left + "///top:" + top + "//right:" + right + "//bottom:" + bottom);
        int origBmpW = bmp.getWidth();
        int origBmpH = bmp.getHeight();

        int x = (int) ((left * origBmpW) * (1 - offset));
        int y = (int) ((top * origBmpH) * (1- offset));

        int width = (int) ((right - left) * origBmpW);
        int height = (int) ((bottom - top) * origBmpH);

        System.out.println("----------bmp//width:" + origBmpW + ";height:" + origBmpH + "////x:" + x + ";" + ";y" + y + "///backbmp:width:" + width + ";hight:" + height);

        return Bitmap.createBitmap(bmp, x, y, width, height, null, false);
    }

    /**
     * 获取设备序列号
     * @return
     */
    public static String getSerialNumber(){

        String serial = null;

        try {

            Class<?> c = Class.forName("android.os.SystemProperties");

            Method get =c.getMethod("get", String.class);

            serial = (String)get.invoke(c, "ro.serialno");

        } catch (Exception e) {

            e.printStackTrace();

        }

        return serial;

    }

    public static String byte2hex(byte [] buffer){
      return byte2hex(buffer, 0, buffer.length);
    }

    /**
     *
     * @param buffer
     * @param startPosition
     * @param endPosition
     * @return
     */
    public static String byte2hex(byte [] buffer, int startPosition, int endPosition){
        StringBuilder str = new StringBuilder("");
        if (endPosition < startPosition) return str.toString();
        if (startPosition < 0) startPosition = 0;
        if (endPosition > buffer.length) endPosition = buffer.length;

        for(int i = 0; i < buffer.length; i++){
            String temp = Integer.toHexString(buffer[i] & 0xFF);
            if(temp.length() == 1){
                temp = "0" + temp;
            }
            str.append(" ");
            str.append(temp);
//            h = h + " "+ temp;
        }

        return str.toString();

    }

    /**
     * 解析单片机发过来的数据，丢掉最后一位终止位
     * @return
     */
    public static byte[] paraseReceiveData(byte[] bytes){
        if (isEmpty(bytes)) return null;
        // 最少5位
        if (bytes.length < 5) return null;
        // 头部
        byte headByte = bytes[0];
        if (headByte != (byte) 0xdd) return null;
        // 数据长度
        byte lengthByte = bytes[1];
        // 命令类型
        byte cmdByte = bytes[2];
        int length = lengthByte & 0xff;
        if (bytes.length < 5 + length) return null;
        byte[] checkBytes = new byte[length + 2];
        System.arraycopy(bytes, 1,  checkBytes, 0, checkBytes.length);
        // 校验位
        byte checkByte = bytes[3 + length];
        byte checkedByte = getXor(checkBytes);
        if (checkByte != checkedByte) return null;
        // 终止位
        byte endByte = bytes[4 + length];
        if (endByte != (byte) 0xed) return null;
        // 丢掉最后一位终止位
        byte[] paraseData = new byte[4 + length];
        System.arraycopy(bytes, 0, paraseData, 0, paraseData.length);
        return paraseData;
    }

    // byte数组长度为4, bytes[3]为高8位
    public static int bytes2Int(byte[] bytes){
        int value=0;
        value = ((bytes[3] & 0xff)<<24)|
                ((bytes[2] & 0xff)<<16)|
                ((bytes[1] & 0xff)<<8)|
                (bytes[0] & 0xff);
        return value;
    }

    /**
     * 异或检验
     * @param datas
     * @return
     */
    public static byte getXor(byte[] datas){

        byte temp=datas[0];

        for (int i = 1; i <datas.length; i++) {
            temp ^=datas[i];
        }

        return temp;
    }

    /**
     * @功能: BCD码转为10进制串(阿拉伯数据)
     * @参数: BCD码
     * @param digit 进制
     * @param asc 字符串
     * @结果: 10进制串
     */
    public static byte[] str2Bcd(String asc, int digit)
    {
        int len = asc.length();
        if (len < digit * 2)
        {
            for (int i = len; i < 2 * digit; i ++)
            {
                asc = "0" + asc;
            }
        }
        len = asc.length();
        int mod = len % 2;

        if (mod != 0)
        {
            asc = "0" + asc;
            len = asc.length();
        }

        if (len >= 2)
        {
            len = len / 2;
        }

        byte bbt[] = new byte[len];
        byte abt[] = asc.getBytes();
        int j, k;

        for (int p = 0; p < asc.length() / 2; p++)
        {
            if ( (abt[2 * p] >= '0') && (abt[2 * p] <= '9'))
            {
                j = abt[2 * p] - '0';
            }
            else if ( (abt[2 * p] >= 'a') && (abt[2 * p] <= 'z'))
            {
                j = abt[2 * p] - 'a' + 0x0a;
            }
            else
            {
                j = abt[2 * p] - 'A' + 0x0a;
            }

            if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9'))
            {
                k = abt[2 * p + 1] - '0';
            }
            else if ( (abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z'))
            {
                k = abt[2 * p + 1] - 'a' + 0x0a;
            }
            else
            {
                k = abt[2 * p + 1] - 'A' + 0x0a;
            }

            int a = (j << 4) + k;
            byte b = (byte) a;
            bbt[p] = b;
        }
        return bbt;
    }

    /**
     * 根据Uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    public static String getRealPathFromUri(Context context, Uri uri) {
        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion >= 19) { // api >= 19
            return getRealPathFromUriAboveApi19(context, uri);
        } else { // api < 19
            return getRealPathFromUriBelowAPI19(context, uri);
        }
    }

    /**
     * 适配api19以下(不包括api19),根据uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    private static String getRealPathFromUriBelowAPI19(Context context, Uri uri) {
        return getDataColumn(context, uri, null, null);
    }

    /**
     * 适配api19及以上,根据uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String getRealPathFromUriAboveApi19(Context context, Uri uri) {
        String filePath = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // 如果是document类型的 uri, 则通过document id来进行处理
            String documentId = DocumentsContract.getDocumentId(uri);
            if (isMediaDocument(uri)) { // MediaProvider
                // 使用':'分割
                String id = documentId.split(":")[1];

                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = {id};
                filePath = getDataColumn(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
            } else if (isDownloadsDocument(uri)) { // DownloadsProvider
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
                filePath = getDataColumn(context, contentUri, null, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())){
            // 如果是 content 类型的 Uri
            filePath = getDataColumn(context, uri, null, null);
        } else if ("file".equals(uri.getScheme())) {
            // 如果是 file 类型的 Uri,直接获取图片对应的路径
            filePath = uri.getPath();
        }
        return filePath;
    }

    /**
     * 获取数据库表中的 _data 列，即返回Uri对应的文件路径
     * @return
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String path = null;

        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is MediaProvider
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is DownloadsProvider
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static Bitmap getBitmapFromFile(Resources res, String fileName)
    {
        FileInputStream fis = null;
        Bitmap bmp = null;
        try {
            fis = new FileInputStream(fileName);

            DisplayMetrics metrics = res.getDisplayMetrics();
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inDensity = (int)(metrics.densityDpi / metrics.scaledDensity);
            bmp = BitmapFactory.decodeStream(fis, null, opts);
            fis.close();
            fis = null;
        }
        catch(Exception e) {}
        finally
        {
            try
            {
                if (fis != null) fis.close();
            }
            catch (final IOException e) {}
        }
        return bmp;
    }

    /**
     * 压缩图片
     * @param image
     * @return
     */
    public static Bitmap compressImage(Bitmap image, int qualitr) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, qualitr, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            //第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差  ，第三个参数：保存压缩后的数据的流
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

}
