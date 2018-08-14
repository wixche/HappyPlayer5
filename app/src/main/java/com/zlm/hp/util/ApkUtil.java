package com.zlm.hp.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.zlm.hp.constants.Constants;

import java.util.UUID;

/**
 * @Description: app信息处理类
 * @author: zhangliangming
 * @date: 2018-07-29 10:27
 **/
public class ApkUtil {
    /**
     * @throws
     * @Description: 获得独一无二的Psuedo ID
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-07-29 10:28
     */
    public static String getUniquePsuedoID(Context context) {
        String deviceId = PreferencesUtil.getString(context, Constants.DEVICE_ID, "");
        if (!TextUtils.isEmpty(deviceId)) {
            return deviceId;
        }
        String m_szDevIDShort = "35" +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                Build.USER.length() % 10; //13 位

        String serial = "";
        try {
            //API>=9 使用serial号
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();
        } catch (Exception e) {
            //serial需要一个初始化
            serial = UUID.randomUUID().toString();
        }
        //使用硬件信息拼凑出来的15位号码
        deviceId = new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        PreferencesUtil.putString(context, Constants.DEVICE_ID, deviceId);
        return deviceId;
    }

    // 版本名
    public static String getVersionName(Context context) {
        return getPackageInfo(context).versionName;
    }

    // 版本号
    public static int getVersionCode(Context context) {
        return getPackageInfo(context).versionCode;
    }

    /**
     * 获取包名
     *
     * @param context
     * @return
     */
    private static PackageInfo getPackageInfo(Context context) {
        PackageInfo pi = null;

        try {
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);

            return pi;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pi;
    }

    /**
     * 判断当前应用是否是debug状态
     *
     * @param context
     * @return
     */
    public static boolean isApkInDebug(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }
}