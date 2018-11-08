package com.zlm.hp.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @Description: 网络处理类
 * @author: zhangliangming
 * @date: 2018-07-29 17:21
 **/

public class NetUtil {
    /**
     * 判断网络连接是否可用
     *
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                return info.getState() == NetworkInfo.State.CONNECTED;
            }
        }
        return false;
    }

    /**
     * WIFI是否已连接
     *
     * @param context
     * @return
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager conMann = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiNetworkInfo != null && wifiNetworkInfo.isConnected() && wifiNetworkInfo.isAvailable() && wifiNetworkInfo.getState() == NetworkInfo.State.CONNECTED;
    }
}
