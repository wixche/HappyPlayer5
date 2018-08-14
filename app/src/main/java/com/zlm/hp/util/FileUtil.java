package com.zlm.hp.util;

import android.os.StatFs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DecimalFormat;

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
     * 获取文件名（不含后缀）
     *
     * @param s
     * @return
     */
    public static String removeExt(String s) {
        int index = s.lastIndexOf(".");
        if (index == -1)
            index = s.length();
        return s.substring(0, index);
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
     * 计算文件的大小，返回相关的m字符串
     *
     * @param fileS
     * @return
     */
    public static String getFileSize(long fileS) {// 转换文件大小
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
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

}
