package com.example.spirillumin.zzcsoft.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Date;

import static com.example.spirillumin.zzcsoft.log.LogInstance.close;

/**
 * describe :
 * version  : 0.1
 * author   : created by wengliuhu
 * time     : 2019/8/22 16:32
 */
public class FileUtil {
    private final static String TAG = FileUtil.class.getName();
    private static Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    private final static String KEDACOM_PATH = "/Feelfull/TestDevices/";
    private static final String ROOT_PATH = Environment.getExternalStorageDirectory().toString() + KEDACOM_PATH;

    public static void initPath(){
       ensureFilePathExist(ROOT_PATH);
    }

    /**
     * 判断文件夹是否存在
     * @param directoryPath
     * @return
     */
    public static boolean isFolderExist(String directoryPath) {
        if (TextUtils.isEmpty(directoryPath)) {
            return false;
        }
        File dire = new File(directoryPath);
        return (dire.exists() && dire.isDirectory());
    }

    /**
     * 删除文件及文件夹下的文件
     *
     * @param file
     * @return
     */
    public static boolean delFile(File file) {
        if (!file.exists()) {
            return false;
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                delFile(f);
            }
        }
        return file.delete();
    }

    public static boolean ensureFilePathExist(String filePath) {
        try {
            File file = new File(filePath);
            boolean exists = file.exists();
            if (!exists) {
                return file.mkdirs();
            }
            return true;
        } catch (final Exception e) {
            LOGGER.info("创建文件夹失败！{}" + filePath, e);
            return false;
        }
    }

    public static byte[] getFileContent(String filePath) {
        File file = new File(filePath);
        byte[] fileContent = null;
        Log.d(TAG, "getFileContent file.exists() = " + file.exists());
        if (file.exists()) {
            long filelength = file.length();
            if (filelength != 0) {
                fileContent = new byte[(int) filelength];
                FileInputStream in = null;
                try {
                    in = new FileInputStream(file);
                    in.read(fileContent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    close(in);
                }
            }
        }
        return fileContent;
    }

    /**
     * 保存byte[]到文件
     * @param bytes
     * @param filePath
     * @param wtrieLength
     * @return
     */
    public static boolean writeFile(byte[] bytes, String filePath, int wtrieLength) {
        File file = new File(filePath);
        ensureFilePathExist(file.getParentFile().getAbsolutePath());
        FileOutputStream fos = null;
        try {
            if (file.exists()) {
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            fos.write(bytes, 0, wtrieLength);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        finally {
            close(fos);
        }

        return true;
    }

    public static void writeFile(byte[] bytes, String filePath) {
      writeFile(bytes, filePath, bytes.length);
    }

    public static void saveBitmapToFile(String path, Bitmap bitmap) {
        try {
            File file = new File(path);
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param path
     * @param bitmap
     * @param quality
     */
    public static void saveBitmapToFilePng(String path, Bitmap bitmap, int quality) {
        FileOutputStream out = null;
        try {
            File file = new File(path);
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            close(out);
        }
    }

    public static boolean deleteFile(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 清空文件夹内，某一时间之前创建的文件和文件夹
     * @param filePath
     * @param timeBefore
     * @return
     */
    public static boolean deleteFolderWithCreateTime(String filePath, long timeBefore){
        boolean result = true;
        try {
            File file = new File(filePath);//获取SD卡指定路径
            // 是文件的满足条件直接删除
            if (file.isFile() && file.lastModified() < timeBefore)
            {
                LOGGER.debug("判定时间：" + timeBefore + "//////" + file.getAbsolutePath() + "最后修改时间{}", file.lastModified());
                return file.delete();
            }
            //获取SD卡指定路径下的文件或者文件夹
            File[] files = file.listFiles();
            if (Util.isEmpty(files))
            {
                return true;
            }
            for (int i = 0; i < files.length; i++) {
                File itemFile = files[i];
                if (itemFile.isFile() && itemFile.lastModified() < timeBefore){//如果是文件直接删除
//                    LOGGER.debug("判定时间：" + timeBefore + "//////" + file.getAbsolutePath() + "最后修改时间{}", file.lastModified());

                    if (!itemFile.delete())
                    {
                        LOGGER.info("删除文件失败！{}", itemFile.getAbsolutePath());
                        result = false;
                    }
                }else {
                    File[] myfile = itemFile.listFiles();
                    // 空文件夹，之恶删除
                    if (Util.isEmpty(myfile))
                    {
                        continue;
                    }
                    // 非空文件夹，递归
                    deleteFolderWithCreateTime(itemFile.getAbsolutePath(), timeBefore);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return result;
    }

    /**
     * 删除文件夹
     * @param filePath
     * @param dedeleteThisPath
     */
    public static boolean deleteFolderFile(String filePath, boolean dedeleteThisPath){
        LOGGER.info("---清空文件---！{}", filePath);

        boolean result = true;
        try {
            File file = new File(filePath);//获取SD卡指定路径
            if (file.isFile()) return file.delete();
            File[] files = file.listFiles();//获取SD卡指定路径下的文件或者文件夹
            if (Util.isEmpty(files))
            {
                if (dedeleteThisPath) file.delete();
                return true;
            }
            for (int i = 0; i < files.length; i++) {
                File itemFile = files[i];
                if (itemFile.isFile()){//如果是文件直接删除
                    if (!itemFile.delete())
                    {
                        LOGGER.info("删除文件失败！{}", itemFile.getAbsolutePath());
                        result = false;
                    }
                }else {
                    File[] myfile = files[i].listFiles();
                    if (Util.isEmpty(myfile))
                    {
                        if (!itemFile.delete())
                        {
                            LOGGER.info("删除文件失败！{}", itemFile.getAbsolutePath());
                            result = false;
                        }
                        continue;
                    }

                    deleteFolderFile(files[i].getAbsolutePath(), dedeleteThisPath);
                }
            }

            if (dedeleteThisPath && !file.delete())
            {
                LOGGER.info("删除文件失败！{}", file.getAbsolutePath());
                result = false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return result;
    }

    public static boolean moveFile(String oldPath, String newPath) {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            File oldFile = new File(oldPath);
            if (!oldFile.exists() || !oldFile.isFile() || !oldFile.canRead()) {
                return false;
            }
            fileInputStream = new FileInputStream(oldPath);
            fileOutputStream = new FileOutputStream(newPath);
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
//            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.getFD().sync();
//            fileOutputStream.close();
            return oldFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        finally {
            close(fileInputStream);
            close(fileOutputStream);
        }
    }

    public static boolean copyFile(String oldPath, String newPath) {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            File oldFile = new File(oldPath);
            if (!oldFile.exists() || !oldFile.isFile() || !oldFile.canRead()) {
                return false;
            }
            fileInputStream = new FileInputStream(oldPath);
            fileOutputStream = new FileOutputStream(newPath);
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileOutputStream.flush();
            fileOutputStream.getFD().sync();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        finally {
            close(fileInputStream);
            close(fileOutputStream);
        }
    }


    public static int writeFile(String content, String path) {
        FileOutputStream fop = null;
        File file;
        try {
            file = new File(path);
            Log.d(TAG, "writeFile path = " + path);
            //if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            Log.d(TAG, "writeFile file = " + file);
            fop = new FileOutputStream(file);
            Log.d(TAG, "writeFile fop = " + fop);
            byte[] contentInBytes = content.getBytes();
            //true = append file
            fop.write(contentInBytes);
            fop.flush();
            fop.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int writeFileByLine(String line, String path, boolean append) {
        BufferedWriter bw = null;
        FileWriter fileWriter = null;
        try {
            File file = new File(path);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            fileWriter = new FileWriter(file.getAbsoluteFile(), append);
            bw = new BufferedWriter(fileWriter);
            bw.write(line);
            if (append == true)
                bw.newLine();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bw.flush();
                bw.close();
                fileWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * 讲assert目录的文件拷贝到指定文件路径下
     *
     * @param context
     * @param assetFileName Asset资源文件名
     * @param outFileName   输出文件名
     * @return
     */
    public static boolean copyAssetFile(Context context, final String assetFileName, String outFileName) {
        final int BUFFER_SIZE = 102400;

        File localFile = new File(outFileName);
        if (!localFile.exists()) {

            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(localFile));
                BufferedInputStream bis = new BufferedInputStream(context.getAssets().open(assetFileName));
                int len = 0;
                byte[] bytes = new byte[BUFFER_SIZE];
                while ((len = bis.read(bytes)) != -1) {
                    bos.write(bytes, 0, len);
                }
                bis.close();
                bos.close();
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }

        return true;
    }

    public static int createDir(String dirPath) {
        int ret = 0;
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdir();
        }
        return ret;
    }


    /**
     * 查看文件路径文件是否存在
     * @param filePath
     * @return
     */
    public static boolean isFileExsit(String filePath){
        if (TextUtils.isEmpty(filePath)) return false;
        File file = new File(filePath);
        return file.exists();
    }
}
