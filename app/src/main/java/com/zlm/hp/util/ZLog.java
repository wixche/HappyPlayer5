package com.zlm.hp.util;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @Description: 日志输出
 * @author: zhangliangming
 * @date: 2018-07-29 10:42
 **/

public class ZLog {
    private static final String TYPE_DEBUG = "[DEBUG]\n";
    private static final String TYPE_INFO = "[INFO]\n";
    private static final String TYPE_ERROR = "[ERROR]\n";

    /**
     * 日志文件的最多保存天数
     */
    private static int LOG_FILE_SAVE_DAYS = 3;
    /**
     *
     */
    private static final byte[] LOCK = new byte[1];

    /**
     * 后缀名
     */
    private static final String NAME_EXT = ".log";
    /**
     * 文件名格式
     */
    private static SimpleDateFormat LOGFILEFORMAT = new SimpleDateFormat("yyyy-MM-dd");// 日志文件格式

    /**
     * 日志路径
     */
    private static String mLogFilePath;
    /**
     * 日志tag
     */
    private static String mLogTag;
    /**
     * 是否是debug模式
     */
    private static boolean isDebug = false;

    /**
     * 初始化
     *
     * @param logFilePath 日志路径
     * @param logTag      日志tag
     */
    public static void init(Context context, String logFilePath, String logTag) {
        mLogFilePath = logFilePath;
        mLogTag = logTag;
        isDebug = ApkUtil.isApkInDebug(context);

        //清空旧的日志
        cleanOldLogFile(logFilePath);

    }

    /**
     * 清空旧日志文件
     *
     * @param logFilePath
     */
    private static void cleanOldLogFile(String logFilePath) {
        File logFileParent = new File(logFilePath);
        if (logFileParent.exists()) {
            String needDelTime = LOGFILEFORMAT.format(getCleanDate());
            File[] files = logFileParent.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isFile()) {
                        String fileName = files[i].getName();
                        fileName = fileName.substring(0, fileName.lastIndexOf("."));
                        if (needDelTime.compareTo(fileName) > 0) {
                            files[i].delete();
                        }
                    }
                }
            }
        }
    }

    /**
     * 得到现在时间前的几天日期，用来得到需要删除的日志文件名
     */
    private static Date getCleanDate() {
        Date nowtime = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(nowtime);
        now.set(Calendar.DATE, now.get(Calendar.DATE)
                - LOG_FILE_SAVE_DAYS);
        return now.getTime();
    }

    /**
     * 输出配置信息
     */
    public static void logBuildInfo(Context context, String codeLineInfo) {
        ZLog.i(codeLineInfo, "\n==========Build==========",
                "\r\nBuild.ID:", Build.ID,
                "\r\nBuild.DISPLAY:", Build.DISPLAY,
                "\r\nBuild.PRODUCT:", Build.PRODUCT,
                "\r\nBuild.DEVICE:", Build.DEVICE,
                "\r\nBuild.BOARD:", Build.BOARD,
                "\r\nBuild.CPU_ABI:", Build.CPU_ABI,
                "\r\nBuild.CPU_ABI2:", Build.CPU_ABI2,
                "\r\nBuild.MANUFACTURER:", Build.MANUFACTURER,
                "\r\nBuild.BRAND:", Build.BRAND,
                "\r\nBuild.MODEL:", Build.MODEL,
                "\r\nBuild.BOOTLOADER:", Build.BOOTLOADER,
                "\r\nBuild.HARDWARE:", Build.HARDWARE,
                "\r\nBuild.SERIAL:", Build.SERIAL,
                "\r\nBuild.TYPE:", Build.TYPE,
                "\r\nBuild.TAGS:", Build.TAGS,
                "\r\nBuild.FINGERPRINT:", Build.FINGERPRINT,
                "\r\nBuild.USER:", Build.USER,
                "\r\nBuild.HOST:", Build.HOST,
                "\r\nBuild.DeviceID:", ApkUtil.getUniquePsuedoID(context),
                "\r\nAPP.VERSION:", ApkUtil.getVersionName(context)
        );
    }

    /**
     * 错误日志
     *
     * @param args
     */
    public static void e(String codeLineInfo, String... args) {
        writeLog(codeLineInfo, TYPE_ERROR, args);
    }

    /**
     * 普通日志
     *
     * @param args
     */
    public static void i(String codeLineInfo, String... args) {
        writeLog(codeLineInfo, TYPE_INFO, args);
    }

    /**
     * debug 日志
     *
     * @param args
     */
    public static void d(String codeLineInfo, String... args) {
        writeLog(codeLineInfo, TYPE_DEBUG, args);
    }

    /**
     * 写入日志
     *
     * @param type
     * @param args
     */
    private static void writeLog(String codeLineInfo, String type, String... args) {
        //收集输出日志
        StringBuilder sb = new StringBuilder();
        //添加当前所在的方法名
        sb.append("\n["
                + getNowDateToString(new Date()) + " " + codeLineInfo + "]\n");
        sb.append(type);
        //添加输出的信息
        for (String str : args) {
            sb.append(str);
        }

        if (TYPE_INFO.equals(type)) {
            Log.i(mLogTag, sb.toString());
        } else if (TYPE_ERROR.equals(type)) {
            Log.e(mLogTag, sb.toString());
        } else if (TYPE_DEBUG.equals(type)) {
            if (isDebug) {
                Log.d(mLogTag, sb.toString());
            }else{
                //不写入日志文件
                return;
            }
        } else {
            Log.v(mLogTag, sb.toString());
        }
        //保存日志文件
        saveLogFile(sb.toString());
    }

    /**
     * 日期转字符串 yyyy-MM-dd HH:mm:ss
     *
     * @param date
     * @return
     */
    private static String getNowDateToString(Date date) {
        if (date == null) {
            date = new Date();
        }
        try {
            DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return dateformat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 保存日志文件
     *
     * @param log
     */
    private static void saveLogFile(String log) {
        synchronized (LOCK) {
            try {
                String fileName = LOGFILEFORMAT.format(new Date()) + NAME_EXT;
                File logFile = new File(mLogFilePath + File.separator + fileName);
                if (!logFile.getParentFile().exists()) {
                    logFile.getParentFile().mkdirs();
                }
                boolean append = true;
                if (!logFile.exists()) {
                    append = false;
                }
                FileWriter writer = new FileWriter(logFile, append);
                writer.append(log);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
