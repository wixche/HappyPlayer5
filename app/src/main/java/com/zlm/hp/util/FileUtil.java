package com.zlm.hp.util;

import android.os.StatFs;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigDecimal;

/**
 * @author zhangliangming
 */
public class FileUtil {
    /**
     * 获取文件后缀
     *
     * @param file
     * @return
     */
    public static String getFileExt(File file) {
        return getFileExt(file.getName());
    }

    /**
     * 获取不带后缀名的文件名
     */
    public static String getFileNameWithoutExt(File file) {
        String filename = file.getName();
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    /**
     * 获取文件后缀名
     *
     * @param fileName
     * @return
     */
    public static String getFileExt(String fileName) {
        int pos = fileName.lastIndexOf(".");
        if (pos == -1)
            return "";
        return fileName.substring(pos + 1).toLowerCase();
    }


    /**
     * 读内部存储文件
     *
     * @param filePath
     * @return
     * @throws Exception
     */
    public static byte[] readFile(String filePath) throws Exception {
        try {
            FileInputStream in = new FileInputStream(new File(filePath));
            byte[] data = new byte[in.available()];
            int i = in.read(data);
            if (i == data.length) {
            }
            in.close();
            return data;
        } catch (FileNotFoundException nfe) {
            return null;
        }
    }

    /**
     * 写内部存储文件
     *
     * @param data 内容
     * @throws Exception
     */
    public static void writeFile(String filePath, byte[] data) throws Exception {
        FileOutputStream out = new FileOutputStream(new File(filePath));
        out.write(data);
        out.flush();
        out.close();
    }

    /**
     * 获取存储空间
     */
    public static long getMemorySize(String path) {
        StatFs statFs = new StatFs(path);
        long blockSizeLong = statFs.getBlockSizeLong();
        long blockCountLong = statFs.getBlockCountLong();
        long size = blockCountLong * blockSizeLong;
        return size;
    }

    /**
     * 获取文件夹大小(递归)
     *
     * @param file File实例
     * @return long
     */
    public static long getFolderSize(File file) {

        long size = 0;
        try {
            java.io.File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);

                } else {
                    size = size + fileList[i].length();

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }


    /**
     * 获取当前文件夹大小，不递归子文件夹
     *
     * @param file
     * @return
     */
    public static long getCurrentFolderSize(File file) {
        long size = 0;
        try {
            java.io.File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    //跳过子文件夹

                } else {
                    size = size + fileList[i].length();

                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return size;
    }


    /**
     * 删除指定目录下文件及目录
     *
     * @param deleteThisPath
     * @param filePath
     * @return
     */
    public static boolean deleteFolderFile(String filePath, boolean deleteThisPath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                if (file.isDirectory()) {// 处理目录
                    File files[] = file.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        deleteFolderFile(files[i].getAbsolutePath(), true);
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory()) {// 如果是文件，删除
                        file.delete();
                    } else {// 目录
                        if (file.listFiles().length == 0) {// 目录下没有文件或者目录，删除
                            file.delete();
                        }
                    }
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }


    /**
     * 删除指定目录下文件
     *
     * @param filePath
     * @return
     */
    public static void deleteFile(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                java.io.File[] fileList = file.listFiles();
                for (int i = 0; i < fileList.length; i++) {
                    if (!fileList[i].isDirectory()) {
                        fileList[i].delete();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 格式化单位
     *
     * @param size
     * @return
     */
    public static String getFileSize(double size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return size + "B";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB";
        }

        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB";
    }



}
