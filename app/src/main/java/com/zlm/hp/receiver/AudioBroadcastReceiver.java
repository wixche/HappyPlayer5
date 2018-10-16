package com.zlm.hp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;

import com.zlm.down.entity.DownloadTask;
import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.entity.AudioInfo;


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
     * null
     */
    public static final int ACTION_CODE_NULL = 0;

    /**
     * 播放初始化
     */
    public static final int ACTION_CODE_INIT = 1;

    /**
     * 播放
     */
    public static final int ACTION_CODE_PLAY = 2;
    /**
     * 播放
     */
    public static final int ACTION_CODE_PLAYING = 3;

    /**
     * 播放本地歌曲
     */
    public static final int ACTION_CODE_SERVICE_PLAYLOCALSONG = 4;

    /**
     * 播放网络歌曲
     */
    public static final int ACTION_CODE_SERVICE_PLAYNETSONG = 5;

    /**
     * 停止播放歌曲
     */
    public static final int ACTION_CODE_STOP = 6;

    /**
     * seekto歌曲
     */
    public static final int ACTION_CODE_SEEKTO = 7;


    /**
     * 网络歌曲下载中
     */
    public static final int ACTION_CODE_DOWNLOADONLINESONG = 8;

    /**
     * 歌词加载完成
     */
    public static final int ACTION_CODE_LRCLOADED = 9;

    private BroadcastReceiver mAudioBroadcastReceiver;
    private IntentFilter mAudioIntentFilter;
    private AudioReceiverListener mAudioReceiverListener;

    public AudioBroadcastReceiver() {
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
    private static void sendReceiver(Context context, int code) {
        sendReceiver(context, code, null, null);
    }

    /**
     * 发null广播
     *
     * @param context
     */
    public static void sendNullReceiver(Context context) {

        //清空当前的播放的索引
        ConfigInfo configInfo = ConfigInfo.obtain();
        configInfo.setPlayHash("");
        //configInfo.setAudioInfos(new ArrayList<AudioInfo>());
        configInfo.save();

        sendReceiver(context, ACTION_CODE_NULL, null, null);
    }

    /**
     * 发null广播
     *
     * @param context
     */
    public static void sendStopReceiver(Context context) {
        sendReceiver(context, ACTION_CODE_STOP, null, null);
    }

    /**
     * 发播放中广播
     */
    public static void sendPlayingReceiver(Context context, AudioInfo audioInfo) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ACTION_DATA_KEY, audioInfo);
        sendReceiver(context, ACTION_CODE_PLAYING, ACTION_BUNDLEKEY, bundle);
    }

    /**
     * 发播放广播
     */
    public static void sendPlayReceiver(Context context, AudioInfo audioInfo) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ACTION_DATA_KEY, audioInfo);
        sendReceiver(context, ACTION_CODE_PLAY, ACTION_BUNDLEKEY, bundle);
    }

    /**
     * 播放网络歌曲
     *
     * @param audioInfo
     */
    public static void sendPlayNetSongReceiver(Context context, AudioInfo audioInfo) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ACTION_DATA_KEY, audioInfo);
        sendReceiver(context, ACTION_CODE_SERVICE_PLAYNETSONG, ACTION_BUNDLEKEY, bundle);
    }

    /**
     * init歌曲
     *
     * @param audioInfo
     */
    public static void sendPlayInitReceiver(Context context, AudioInfo audioInfo) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ACTION_DATA_KEY, audioInfo);
        sendReceiver(context, ACTION_CODE_INIT, ACTION_BUNDLEKEY, bundle);
    }


    /**
     * 播放本地歌曲
     *
     * @param audioInfo
     */
    public static void sendPlayLocalSongReceiver(Context context, AudioInfo audioInfo) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ACTION_DATA_KEY, audioInfo);
        sendReceiver(context, ACTION_CODE_SERVICE_PLAYLOCALSONG, ACTION_BUNDLEKEY, bundle);
    }

    /**
     * 网络歌曲下载中
     *
     * @param context
     * @param task
     */
    public static void sendDownloadingOnlineSongReceiver(Context context, DownloadTask task) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ACTION_DATA_KEY, task);
        sendReceiver(context, ACTION_CODE_DOWNLOADONLINESONG, ACTION_BUNDLEKEY, bundle);

    }

    /**
     * seekto歌曲
     *
     * @param audioInfo
     */
    public static void sendSeektoSongReceiver(Context context, AudioInfo audioInfo) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ACTION_DATA_KEY, audioInfo);
        sendReceiver(context, ACTION_CODE_SEEKTO, ACTION_BUNDLEKEY, bundle);
    }

    /**
     * 发lrc loaded
     *
     * @param context
     */
    public static void sendLrcLoadedReceiver(Context context, String hash) {
        Bundle bundle = new Bundle();
        bundle.putString(ACTION_DATA_KEY, hash);
        sendReceiver(context, ACTION_CODE_LRCLOADED, ACTION_BUNDLEKEY, bundle);
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

    public void setReceiverListener(AudioReceiverListener audioReceiverListener) {
        this.mAudioReceiverListener = audioReceiverListener;
    }
}
