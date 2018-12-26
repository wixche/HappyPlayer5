package com.zlm.hp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;

import com.zlm.hp.constants.Constants;
import com.zlm.hp.entity.TimerInfo;

/**
 * @Description: app系统广播
 * @param:
 * @return:
 * @throws
 * @author: zhangliangming
 * @date: 2018-10-16 12:30
 */
public class AppSystemReceiver {
    /**
     * receiver的action
     */
    private static final String RECEIVER_ACTION = "com.zlm.hp.receiver.system.action";

    /**
     * code key
     */
    private static final String ACTION_CODE_KEY = "com.zlm.hp.receiver.system.action.code.key";

    /**
     * bundle key
     */
    public static final String ACTION_BUNDLEKEY = "com.zlm.hp.receiver.system.action.bundle.key";

    /**
     * data key
     */
    public static final String ACTION_DATA_KEY = "com.zlm.hp.receiver.system.action.data.key";

    /**
     * toast error msg
     */
    public static final int ACTION_CODE_TOAST_ERRORMSG = 0;

    /**
     * 设置定时器
     */
    public static final int ACTION_CODE_TIMER_SETTING = 1;

    /**
     * 定时器更新
     */
    public static final int ACTION_CODE_TIMER_UPDATE = 2;

    /**
     * 关闭屏幕
     */
    public static final int ACTION_CODE_SCREEN_OFF = 3;

    private final String ACTION_SCREEN_OFF = Intent.ACTION_SCREEN_OFF;


    private BroadcastReceiver mBroadcastReceiver;
    private IntentFilter mIntentFilter;
    private AppSystemReceiverListener mReceiverListener;

    public AppSystemReceiver() {
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(RECEIVER_ACTION);
        mIntentFilter.addAction(ACTION_SCREEN_OFF);
    }

    /**
     * 注册广播
     *
     * @param context
     */
    public void registerReceiver(Context context) {

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (mReceiverListener != null) {
                    int code = intent.getIntExtra(ACTION_CODE_KEY, -1);
                    if (intent.getAction().equals(ACTION_SCREEN_OFF)) {
                        code = ACTION_CODE_SCREEN_OFF;
                    }
                    if (code != -1) {
                        mReceiverListener.onReceive(context, intent, code);
                    }
                }
            }
        };
        context.registerReceiver(mBroadcastReceiver, mIntentFilter, Constants.RECEIVER_PERMISSION, null);
    }

    /**
     * 发广播
     *
     * @param context
     * @param code
     * @param bundleKey
     * @param bundleValue
     */
    public static void sendReceiver(Context context, int code, String bundleKey, Bundle bundleValue) {
        Intent intent = new Intent(RECEIVER_ACTION);
        intent.putExtra(ACTION_CODE_KEY, code);
        if (!TextUtils.isEmpty(bundleKey) && bundleValue != null) {
            intent.putExtra(bundleKey, bundleValue);
        }
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.sendBroadcast(intent, Constants.RECEIVER_PERMISSION);
    }

    /**
     * 发广播
     *
     * @param context
     * @param code
     */
    private static void sendReceiver(Context context, int code) {
        sendReceiver(context, code, null, null);
    }

    /**
     * 发error msg
     *
     * @param context
     */
    public static void sendToastErrorMsgReceiver(Context context, String msg) {
        Bundle bundle = new Bundle();
        bundle.putString(ACTION_DATA_KEY, msg);
        sendReceiver(context, ACTION_CODE_TOAST_ERRORMSG, ACTION_BUNDLEKEY, bundle);
    }

    /**
     * timer setting
     *
     * @param context
     */
    public static void sendTimerSettingMsgReceiver(Context context, TimerInfo timerInfo) {
        Bundle bundle = new Bundle();
        if (timerInfo != null)
            bundle.putParcelable(ACTION_DATA_KEY, timerInfo);
        sendReceiver(context, ACTION_CODE_TIMER_SETTING, ACTION_BUNDLEKEY, bundle);
    }

    /**
     * timer update
     *
     * @param context
     */
    public static void sendTimerUpdateMsgReceiver(Context context, TimerInfo timerInfo) {
        Bundle bundle = new Bundle();
        if (timerInfo != null)
            bundle.putParcelable(ACTION_DATA_KEY, timerInfo);
        sendReceiver(context, ACTION_CODE_TIMER_UPDATE, ACTION_BUNDLEKEY, bundle);
    }


    /**
     * 取消注册广播
     */
    public void unregisterReceiver(Context context) {
        if (mBroadcastReceiver != null) {
            context.unregisterReceiver(mBroadcastReceiver);
        }
    }

    public interface AppSystemReceiverListener {
        void onReceive(Context context, Intent intent, int code);
    }

    public void setReceiverListener(AppSystemReceiverListener receiverListener) {
        this.mReceiverListener = receiverListener;
    }
}
