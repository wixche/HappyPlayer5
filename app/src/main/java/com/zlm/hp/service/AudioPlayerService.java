package com.zlm.hp.service;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;

import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.util.ResourceUtil;
import com.zlm.hp.util.ToastUtil;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * @Description: 音频服务器
 * @param:
 * @return:
 * @throws
 * @author: zhangliangming
 * @date: 2018-10-09 21:13
 */
public class AudioPlayerService extends Service {
    private AudioBroadcastReceiver mAudioBroadcastReceiver;
    private Context mContext;

    /**
     * 播放器
     */
    private IjkMediaPlayer mMediaPlayer;

    private AudioInfo mAudioInfo;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = getApplicationContext();
        mAudioBroadcastReceiver = new AudioBroadcastReceiver(mContext);
        mAudioBroadcastReceiver.setAudioReceiverListener(new AudioBroadcastReceiver.AudioReceiverListener() {
            @Override
            public void onReceive(Context context, Intent intent, int code) {
                switch (code) {

                    //播放网络歌曲
                    case AudioBroadcastReceiver.ACTION_CODE_PLAYNETSONG:
                        //播放本地歌曲
                    case AudioBroadcastReceiver.ACTION_CODE_PLAYLOCALSONG:
                        Bundle bundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                        AudioInfo audioInfo = bundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                        handleSong(audioInfo);

                        break;

                    case AudioBroadcastReceiver.ACTION_CODE_STOP:
                        releasePlayer();
                        break;
                }
            }
        });
        mAudioBroadcastReceiver.registerReceiver(mContext);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        releasePlayer();
        mAudioBroadcastReceiver.unregisterReceiver(mContext);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * 处理歌曲
     *
     * @param audioInfo
     */
    private void handleSong(final AudioInfo audioInfo) {
        this.mAudioInfo = audioInfo;
        try {
            String filePath = null;
            if (audioInfo.getType() == AudioInfo.TYPE_LOCAL) {
                String fileName = audioInfo.getTitle();
                filePath = ResourceUtil.getFilePath(mContext, ResourceConstants.PATH_AUDIO, fileName + "." + audioInfo.getFileExt());
            } else {
                filePath = ResourceUtil.getFilePath(mContext, ResourceConstants.PATH_CACHE_AUDIO, audioInfo.getHash() + ".temp");
            }

            mMediaPlayer = new IjkMediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.prepareAsync();

            mMediaPlayer.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(IMediaPlayer mp) {
                    mMediaPlayer.start();
                }
            });


            mMediaPlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(IMediaPlayer mp) {

                    if (audioInfo.getType() == AudioInfo.TYPE_NET && mMediaPlayer.getCurrentPosition() < (audioInfo.getDuration() - 2 * 1000)) {
                        //网络歌曲未播放全部，需要重新调用播放歌曲
                        handleSong(audioInfo);
                    } else {
                        //播放完成，执行下一首操作
                        AudioPlayerManager.newInstance(mContext).next();
                    }

                    releasePlayer();

                }
            });

            mMediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(IMediaPlayer mp) {
                    if (mAudioInfo.getPlayProgress() != 0) {
                        mMediaPlayer.seekTo(mAudioInfo.getPlayProgress());
                    } else {
                        mMediaPlayer.start();
                    }
                }
            });

            mMediaPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(IMediaPlayer mp, int what, int extra) {

                    handleError();

                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            handleError();
        }
    }

    /**
     * 处理错误
     */
    private void handleError() {
        ToastUtil.showTextToast(getApplicationContext(), "播放歌曲出错，1秒后播放下一首");


        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);

                    //播放完成，执行下一首操作
                    AudioPlayerManager.newInstance(mContext).next();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 释放播放器
     */
    private void releasePlayer() {

        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }

            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        System.gc();
    }

    /**
     * 启动服务
     *
     * @param context
     */
    public static void startService(Activity context) {
        Intent intent = new Intent(context, AudioPlayerService.class);
        context.startService(intent);
    }

    /**
     * 停止服务
     *
     * @param context
     */
    public static void stopService(Activity context) {
        Intent intent = new Intent(context, AudioPlayerService.class);
        context.stopService(intent);
    }
}
