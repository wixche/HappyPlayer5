package com.zlm.hp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;

/**
 * @Description: 音频监听
 * @author: zhangliangming
 * @date: 2018-10-09 21:36
 **/
public class AudioBroadcastReceiver {
    /**
     * audio的receiver的action
     */
    private static final String AUDIO_RECEIVER_ACTION = "com.zlm.hp.receiver.audio.action";

    /**
     * code key
     */
    private static final String ACTION_CODE_KEY = "com.zlm.hp.receiver.audio.action.code.key";

    /**
     * bundle key
     */
    public static final String ACTION_BUNDLEKEY = "com.zlm.hp.receiver.audio.action.bundle.key";

    /**
     * data key
     */
    public static final String ACTION_DATA_KEY = "com.zlm.hp.receiver.audio.action.data.key";

    /**
     * 播放本地歌曲
     */
    public static final int ACTION_CODE_PLAYLOCALSONG = 1;

    /**
     * 播放网络歌曲
     */
    public static final int ACTION_CODE_PLAYNETSONG = 2;

    /**
     * 停止播放歌曲
     */
    public static final int ACTION_CODE_STOP = 3;


    private BroadcastReceiver mAudioBroadcastReceiver;
    private IntentFilter mAudioIntentFilter;
    private AudioReceiverListener mAudioReceiverListener;

    public AudioBroadcastReceiver(Context context) {
        mAudioIntentFilter = new IntentFilter();
        mAudioIntentFilter.addAction(AUDIO_RECEIVER_ACTION);
    }

    /**
     * 注册广播
     *
     * @param context
     */
    public void registerReceiver(Context context) {

        mAudioBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (mAudioReceiverListener != null) {
                    int code = intent.getIntExtra(ACTION_CODE_KEY, -1);
                    if (code != -1) {
                        mAudioReceiverListener.onReceive(context, intent, code);
                    }
                }
            }
        };
        context.registerReceiver(mAudioBroadcastReceiver, mAudioIntentFilter);
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
        Intent intent = new Intent(AUDIO_RECEIVER_ACTION);
        intent.putExtra(ACTION_CODE_KEY, code);
        if (!TextUtils.isEmpty(bundleKey) && bundleValue != null) {
            intent.putExtra(bundleKey, bundleValue);
        }
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.sendBroadcast(intent);
    }

    /**
     * 发广播
     *
     * @param context
     * @param code
     */
    public static void sendReceiver(Context context, int code) {
        sendReceiver(context, code, null, null);
    }


    /**
     * 取消注册广播
     */
    public void unregisterReceiver(Context context) {
        if (mAudioBroadcastReceiver != null) {
            context.unregisterReceiver(mAudioBroadcastReceiver);
        }
    }

    public interface AudioReceiverListener {
        void onReceive(Context context, Intent intent, int code);
    }

    public void setAudioReceiverListener(AudioReceiverListener audioReceiverListener) {
        this.mAudioReceiverListener = audioReceiverListener;
    }
}
