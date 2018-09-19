package com.zlm.hp.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

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
        WifiInfo wifiInfo = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
        ConnectivityManager conMann = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        //避免ssid出现假连现象，这里对ssid进行了一个unknown ssid
        return wifiNetworkInfo != null && wifiNetworkInfo.isConnected() && wifiNetworkInfo.isAvailable() && wifiNetworkInfo.getState() == NetworkInfo.State.CONNECTED && wifiInfo != null && !wifiInfo.getSSID().equals("<unknown ssid>");
    }
}
