package com.zlm.hp.service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.view.View;
import android.widget.RemoteViews;

import com.zlm.hp.async.AsyncHandlerTask;
import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.db.util.AudioInfoDB;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.handler.WeakRefHandler;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.ui.MainActivity;
import com.zlm.hp.ui.R;
import com.zlm.hp.util.AppOpsUtils;
import com.zlm.hp.util.DateUtil;
import com.zlm.hp.util.ImageUtil;
import com.zlm.hp.util.ResourceUtil;
import com.zlm.hp.util.ToastUtil;

import java.io.File;
import java.util.Date;

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
    /**
     * 处理ui任务
     */
    public WeakRefHandler mUIHandler;
    /**
     * 子线程用于执行耗时任务
     */
    public WeakRefHandler mWorkerHandler;
    //创建异步HandlerThread
    private HandlerThread mHandlerThread;

    private AudioBroadcastReceiver mAudioBroadcastReceiver;
    private Context mContext;

    /**
     * 播放器
     */
    private IjkMediaPlayer mMediaPlayer;

    /**
     * 加载当前播放进度
     */
    private final int MESSAGE_WHAT_LOADPLAYPROGRESSDATA = 0;

    ///////////////////////////////通知栏//////////////////////////////
    private int mNotificationPlayBarId = 19900420;
    /**
     * 状态栏播放器视图
     */
    private RemoteViews mNotifyPlayBarRemoteViews;
    /**
     *
     */
    private Notification mPlayBarNotification;

    /**
     * 焦点
     */
    private AudioManager mAudioManager;

    //注册OnAudioFocusChangeListener监听
    AudioManager.OnAudioFocusChangeListener mOfChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            } else {
                //已经失去音频焦点
                if (AudioPlayerManager.getInstance(mContext).getPlayStatus() == AudioPlayerManager.PLAYING) {
                    AudioPlayerManager.getInstance(mContext).pause();
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = getApplicationContext();
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mAudioBroadcastReceiver = new AudioBroadcastReceiver();
        mAudioBroadcastReceiver.setReceiverListener(new AudioBroadcastReceiver.AudioReceiverListener() {
            @Override
            public void onReceive(Context context, final Intent intent, final int code) {
                switch (code) {
                    case AudioBroadcastReceiver.ACTION_CODE_NOTIFY_PLAY:
                        if (getCurAudioInfo() != null) {
                            AudioPlayerManager.getInstance(mContext).play(getCurAudioInfo().getPlayProgress());
                        }

                        break;
                    case AudioBroadcastReceiver.ACTION_CODE_NOTIFY_PAUSE:
                        if (getCurAudioInfo() != null) {
                            AudioPlayerManager.getInstance(mContext).pause();
                        }
                        break;
                    case AudioBroadcastReceiver.ACTION_CODE_NOTIFY_NEXT:
                        AudioPlayerManager.getInstance(mContext).next();
                        break;
                    case AudioBroadcastReceiver.ACTION_CODE_NOTIFY_PRE:
                        AudioPlayerManager.getInstance(mContext).pre();
                        break;
                    case AudioBroadcastReceiver.ACTION_CODE_NOTIFY_UNLOCK:
                        ConfigInfo configInfo = ConfigInfo.obtain();
                        configInfo.setDesktopLrcCanMove(true).save();
                    case AudioBroadcastReceiver.ACTION_CODE_NOTIFY_LOCK:
                    case AudioBroadcastReceiver.ACTION_CODE_NOTIFY_DESLRC:

                        mUIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                doNotification(null, code, false);
                            }
                        });

                        break;
                    case AudioBroadcastReceiver.ACTION_CODE_NOTIFY_DESLRC_SHOW_ACTION:
                        if (!AppOpsUtils.allowFloatWindow(getApplication())) {
                            //没有权限
                            mUIHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtil.showTextToast(getApplicationContext(), getString(R.string.desktoplrc_tip));
                                }
                            });
                        }

                        break;

                    case AudioBroadcastReceiver.ACTION_CODE_NULL:
                    case AudioBroadcastReceiver.ACTION_CODE_PLAY:
                    case AudioBroadcastReceiver.ACTION_CODE_STOP:

                        if (code == AudioBroadcastReceiver.ACTION_CODE_STOP) {
                            //更新暂停时的进度
                            if (mMediaPlayer != null) {

                                AudioInfo curAudioInfo = getCurAudioInfo();
                                if (curAudioInfo != null)
                                    curAudioInfo.setPlayProgress((int) mMediaPlayer.getCurrentPosition());
                            }
                            releasePlayer();
                        }

                        mUIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                doNotification(null, code, false);
                            }
                        });

                        break;
                    case AudioBroadcastReceiver.ACTION_CODE_NOTIFY_SINGERICONLOADED:
                        //
                        Bundle notifySingerLoadedBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                        final AudioInfo curAudioInfo = notifySingerLoadedBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                        if (curAudioInfo != null && getCurAudioInfo() != null && curAudioInfo.getHash().equals(getCurAudioInfo().getHash())) {
                            mUIHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    doNotification(curAudioInfo, code, false);
                                }
                            });
                        }
                        break;

                    case AudioBroadcastReceiver.ACTION_CODE_INIT:

                        Bundle initBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                        final AudioInfo initAudioInfo = initBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                        mUIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                doNotification(initAudioInfo, code, false);
                            }
                        });
                        break;

                    //播放网络歌曲
                    case AudioBroadcastReceiver.ACTION_CODE_SERVICE_PLAYNETSONG:
                        //播放本地歌曲
                    case AudioBroadcastReceiver.ACTION_CODE_SERVICE_PLAYLOCALSONG:

                        mWorkerHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                Bundle bundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                                AudioInfo audioInfo = bundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                                handleSong(audioInfo);

                            }
                        });
                        break;
                }
            }
        });
        mAudioBroadcastReceiver.registerReceiver(mContext);

        //创建ui handler
        mUIHandler = new WeakRefHandler(Looper.getMainLooper(), this, new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                handleUIMessage(msg);
                return true;
            }
        });

        //创建异步HandlerThread
        mHandlerThread = new HandlerThread("AudioPlayerServiceThread", Process.THREAD_PRIORITY_BACKGROUND);
        //必须先开启线程
        mHandlerThread.start();
        //子线程Handler
        mWorkerHandler = new WeakRefHandler(mHandlerThread.getLooper(), this, new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                handleWorkerMessage(msg);
                return true;
            }
        });

        //初始化通知栏
        initNotificationView();
        bindNotificationEvent();
        loadNotificationData();
    }

    /**
     *
     */
    private void loadNotificationData() {
        //加载数据
        ConfigInfo configInfo = ConfigInfo.obtain();
        AudioInfo audioInfo = AudioPlayerManager.getInstance(mContext).getCurSong(configInfo.getPlayHash());
        if (audioInfo != null) {
            doNotification(audioInfo, AudioBroadcastReceiver.ACTION_CODE_INIT, true);
        } else {
            doNotification(audioInfo, AudioBroadcastReceiver.ACTION_CODE_NULL, false);
        }
    }


    /**
     * @param msg
     */
    private void handleWorkerMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_WHAT_LOADPLAYPROGRESSDATA:

                if (getCurAudioInfo() != null && mMediaPlayer != null && mMediaPlayer.isPlaying()) {

                    getCurAudioInfo().setPlayProgress((int) mMediaPlayer.getCurrentPosition());
                    AudioBroadcastReceiver.sendPlayingReceiver(mContext, getCurAudioInfo());
                }
                //
                mWorkerHandler.sendEmptyMessageDelayed(MESSAGE_WHAT_LOADPLAYPROGRESSDATA, 1000);

                break;
        }
    }

    /**
     * @param msg
     */
    private void handleUIMessage(Message msg) {
    }

    /**
     *
     */
    private void bindNotificationEvent() {
        Intent buttonplayIntent = AudioBroadcastReceiver.getNotifiyIntent(AudioBroadcastReceiver.ACTION_CODE_NOTIFY_PLAY);
        PendingIntent pendplayButtonIntent = PendingIntent.getBroadcast(
                AudioPlayerService.this, AudioBroadcastReceiver.ACTION_CODE_NOTIFY_PLAY, buttonplayIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        mNotifyPlayBarRemoteViews.setOnClickPendingIntent(R.id.play,
                pendplayButtonIntent);

        Intent buttonpauseIntent = AudioBroadcastReceiver.getNotifiyIntent(AudioBroadcastReceiver.ACTION_CODE_NOTIFY_PAUSE);
        PendingIntent pendpauseButtonIntent = PendingIntent.getBroadcast(
                AudioPlayerService.this, AudioBroadcastReceiver.ACTION_CODE_NOTIFY_PAUSE, buttonpauseIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        mNotifyPlayBarRemoteViews.setOnClickPendingIntent(R.id.pause,
                pendpauseButtonIntent);

        Intent buttonnextIntent = AudioBroadcastReceiver.getNotifiyIntent(AudioBroadcastReceiver.ACTION_CODE_NOTIFY_NEXT);
        PendingIntent pendnextButtonIntent = PendingIntent.getBroadcast(
                AudioPlayerService.this, AudioBroadcastReceiver.ACTION_CODE_NOTIFY_NEXT, buttonnextIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        mNotifyPlayBarRemoteViews.setOnClickPendingIntent(R.id.next,
                pendnextButtonIntent);

        Intent buttonprewtIntent = AudioBroadcastReceiver.getNotifiyIntent(AudioBroadcastReceiver.ACTION_CODE_NOTIFY_PRE);
        PendingIntent pendprewButtonIntent = PendingIntent.getBroadcast(
                AudioPlayerService.this, AudioBroadcastReceiver.ACTION_CODE_NOTIFY_PRE, buttonprewtIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        mNotifyPlayBarRemoteViews.setOnClickPendingIntent(R.id.prew,
                pendprewButtonIntent);

        // 设置歌词显示状态和解锁歌词

        Intent buttonDesLrcUnlockIntent = AudioBroadcastReceiver.getNotifiyIntent(AudioBroadcastReceiver.ACTION_CODE_NOTIFY_UNLOCK);
        PendingIntent pendDesLrcUnlockIntent = PendingIntent.getBroadcast(
                AudioPlayerService.this, AudioBroadcastReceiver.ACTION_CODE_NOTIFY_UNLOCK, buttonDesLrcUnlockIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        mNotifyPlayBarRemoteViews.setOnClickPendingIntent(R.id.deslrcUnlock,
                pendDesLrcUnlockIntent);

        Intent buttonDesLrcHideIntent = AudioBroadcastReceiver.getNotifiyIntent(AudioBroadcastReceiver.ACTION_CODE_NOTIFY_DESLRC_HIDE_ACTION);
        PendingIntent pendDesLrcHideIntent = PendingIntent.getBroadcast(
                AudioPlayerService.this, AudioBroadcastReceiver.ACTION_CODE_NOTIFY_DESLRC_HIDE_ACTION, buttonDesLrcHideIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        mNotifyPlayBarRemoteViews.setOnClickPendingIntent(R.id.showdeslrc,
                pendDesLrcHideIntent);

        Intent buttonDesLrcShowIntent = AudioBroadcastReceiver.getNotifiyIntent(AudioBroadcastReceiver.ACTION_CODE_NOTIFY_DESLRC_SHOW_ACTION);
        PendingIntent pendDesLrcShowIntent = PendingIntent.getBroadcast(
                AudioPlayerService.this, AudioBroadcastReceiver.ACTION_CODE_NOTIFY_DESLRC_SHOW_ACTION, buttonDesLrcShowIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        mNotifyPlayBarRemoteViews.setOnClickPendingIntent(R.id.hidedeslrc,
                pendDesLrcShowIntent);
    }

    /**
     * 处理通知栏视图
     */
    private void doNotification(AudioInfo audioInfo, int code, boolean isFristToLoadIcon) {

        ConfigInfo configInfo = ConfigInfo.obtain();
        if (configInfo.isShowDesktopLrc()) {
            if (configInfo.isDesktopLrcCanMove()) {
                mNotifyPlayBarRemoteViews.setViewVisibility(R.id.showdeslrc,
                        View.VISIBLE);
                mNotifyPlayBarRemoteViews.setViewVisibility(R.id.hidedeslrc,
                        View.INVISIBLE);
                mNotifyPlayBarRemoteViews.setViewVisibility(
                        R.id.deslrcUnlock, View.INVISIBLE);
            } else {
                mNotifyPlayBarRemoteViews.setViewVisibility(
                        R.id.deslrcUnlock, View.VISIBLE);
                mNotifyPlayBarRemoteViews.setViewVisibility(R.id.hidedeslrc,
                        View.INVISIBLE);
                mNotifyPlayBarRemoteViews.setViewVisibility(R.id.showdeslrc,
                        View.INVISIBLE);
            }
        } else {
            mNotifyPlayBarRemoteViews.setViewVisibility(R.id.hidedeslrc,
                    View.VISIBLE);
            mNotifyPlayBarRemoteViews.setViewVisibility(R.id.showdeslrc,
                    View.INVISIBLE);
            mNotifyPlayBarRemoteViews.setViewVisibility(R.id.deslrcUnlock,
                    View.INVISIBLE);
        }
        switch (code) {
            case AudioBroadcastReceiver.ACTION_CODE_NULL:
                //无歌曲

                mNotifyPlayBarRemoteViews.setImageViewResource(R.id.singPic,
                        R.mipmap.bpz);// 显示专辑封面图片

                mNotifyPlayBarRemoteViews.setTextViewText(R.id.titleName,
                        getString(R.string.def_text));
                mNotifyPlayBarRemoteViews.setViewVisibility(R.id.play,
                        View.VISIBLE);
                mNotifyPlayBarRemoteViews.setViewVisibility(R.id.pause,
                        View.INVISIBLE);

                break;

            case AudioBroadcastReceiver.ACTION_CODE_INIT:
                //初始化
                String titleName = audioInfo.getTitle();
                mNotifyPlayBarRemoteViews.setTextViewText(R.id.titleName,
                        titleName);

                //刚启动通知栏需要加载歌手图标
                if (isFristToLoadIcon) {
                    //加载歌手头像
                    Bitmap curbm = ImageUtil.getNotifiIcon(mContext, audioInfo.getSingerName(), 400, 400, new AsyncHandlerTask(mUIHandler, mWorkerHandler));
                    if (curbm != null) {
                        mNotifyPlayBarRemoteViews.setImageViewBitmap(
                                R.id.singPic, curbm);// 显示专辑封面图片
                    }
                } else {
                    mNotifyPlayBarRemoteViews.setImageViewResource(R.id.singPic,
                            R.mipmap.bpz);// 显示专辑封面图片
                }

                mNotifyPlayBarRemoteViews.setViewVisibility(R.id.play,
                        View.VISIBLE);
                mNotifyPlayBarRemoteViews.setViewVisibility(R.id.pause,
                        View.INVISIBLE);


                break;
            case AudioBroadcastReceiver.ACTION_CODE_PLAY:
                //播放
                mNotifyPlayBarRemoteViews.setViewVisibility(R.id.play,
                        View.INVISIBLE);
                mNotifyPlayBarRemoteViews.setViewVisibility(R.id.pause,
                        View.VISIBLE);

                break;

            case AudioBroadcastReceiver.ACTION_CODE_STOP:
                //暂停
                mNotifyPlayBarRemoteViews.setViewVisibility(R.id.play,
                        View.VISIBLE);
                mNotifyPlayBarRemoteViews.setViewVisibility(R.id.pause,
                        View.INVISIBLE);


                break;

            case AudioBroadcastReceiver.ACTION_CODE_NOTIFY_SINGERICONLOADED:
                //加载歌手头像
                Bitmap cbbm = ImageUtil.getNotifiIcon(mContext, audioInfo.getSingerName(), 400, 400, new AsyncHandlerTask(mUIHandler, mWorkerHandler));
                if (cbbm != null) {
                    mNotifyPlayBarRemoteViews.setImageViewBitmap(
                            R.id.singPic, cbbm);// 显示专辑封面图片
                }
                break;

        }

        mPlayBarNotification.contentView = mNotifyPlayBarRemoteViews;

        startForeground(mNotificationPlayBarId, mPlayBarNotification);

    }

    /**
     * 初始化通知栏
     */
    private void initNotificationView() {

        String tickerText = getString(R.string.def_artist);

        //判断系统版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // 通知渠道的id
            String CHANNEL_ID = "hp_channel";
            String CHANNEL_NAME = "hp";

            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {

                int importance = NotificationManager.IMPORTANCE_LOW;
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
                mChannel.enableLights(true);
                notificationManager.createNotificationChannel(mChannel);
            }

            // Create a notification and set the notification channel.
            mPlayBarNotification = new Notification.Builder(getApplicationContext())
                    .setContentTitle(tickerText)
                    .setContentText(getString(R.string.def_songName))
                    .setSmallIcon(R.mipmap.ic_launcher_foreground)
                    .setChannelId(CHANNEL_ID)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .build();
        } else {

            //android5.0修改通知栏图标
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                mPlayBarNotification = new Notification.Builder(getApplicationContext())
                        .setContentTitle(tickerText)
                        .setContentText(getString(R.string.def_songName))
                        .setSmallIcon(R.mipmap.ic_launcher_foreground)
                        .setVisibility(Notification.VISIBILITY_PUBLIC)
                        .build();

            } else {
                // Create a notification and set the notification channel.
                mPlayBarNotification = new Notification.Builder(getApplicationContext())
                        .setContentTitle(tickerText)
                        .setContentText(getString(R.string.def_songName))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .build();
            }
        }


        // FLAG_AUTO_CANCEL 该通知能被状态栏的清除按钮给清除掉
        // FLAG_NO_CLEAR 该通知不能被状态栏的清除按钮给清除掉
        // FLAG_ONGOING_EVENT 通知放置在正在运行
        // FLAG_INSISTENT 是否一直进行，比如音乐一直播放，知道用户响应
        mPlayBarNotification.flags |= Notification.FLAG_ONGOING_EVENT;
        // mNotification.flags |= Notification.FLAG_NO_CLEAR;

        // DEFAULT_ALL 使用所有默认值，比如声音，震动，闪屏等等
        // DEFAULT_LIGHTS 使用默认闪光提示
        // DEFAULT_SOUND 使用默认提示声音
        // DEFAULT_VIBRATE 使用默认手机震动，需加上<uses-permission
        // android:name="android.permission.VIBRATE" />权限
        // mNotification.defaults = Notification.DEFAULT_SOUND;

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setClass(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        PendingIntent pendingIntent = PendingIntent
                .getActivity(this, 0, intent,
                        PendingIntent.FLAG_CANCEL_CURRENT);
        mPlayBarNotification.contentIntent = pendingIntent;
        mNotifyPlayBarRemoteViews = new RemoteViews(getPackageName(),
                R.layout.layout_notify);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mAudioManager != null)
            mAudioManager.abandonAudioFocus(mOfChangeListener);

        releaseHandle();
        releasePlayer();
        //关闭通知栏
        stopForeground(true);
        mAudioBroadcastReceiver.unregisterReceiver(mContext);
    }

    private void releaseHandle() {
        //移除队列任务
        if (mUIHandler != null) {
            mUIHandler.removeCallbacksAndMessages(null);
        }

        //移除队列任务
        if (mWorkerHandler != null) {
            mWorkerHandler.removeCallbacksAndMessages(null);
        }

        //关闭线程
        if (mHandlerThread != null)
            mHandlerThread.quit();
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

        //在播放的时候为AudioManager添加获取焦点的监听
        mAudioManager.requestAudioFocus(mOfChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        //添加到最近播放列表中去
        if (AudioInfoDB.isRecentAudioExists(mContext, audioInfo.getHash())) {
            AudioInfoDB.updateRecentAudio(mContext, audioInfo.getHash(), DateUtil.parseDateToString(new Date()));
        } else {
            AudioInfoDB.addRecentAudio(mContext, audioInfo, true);
        }

        try {
            String fileName = audioInfo.getTitle();
            String filePath = audioInfo.getFilePath();
            if (audioInfo.getType() != AudioInfo.TYPE_LOCAL) {
                filePath = ResourceUtil.getFilePath(mContext, ResourceConstants.PATH_AUDIO, fileName + "." + audioInfo.getFileExt());
                File audioFile = new File(filePath);
                if (!audioFile.exists()) {
                    filePath = ResourceUtil.getFilePath(mContext, ResourceConstants.PATH_CACHE_AUDIO, audioInfo.getHash() + ".temp");
                }
            }
            if (mMediaPlayer != null) {
                releasePlayer();
            }

            mMediaPlayer = new IjkMediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.prepareAsync();

            mMediaPlayer.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(IMediaPlayer mp) {

                    //发送播放中广播
                    AudioBroadcastReceiver.sendPlayReceiver(mContext, audioInfo);
                    mWorkerHandler.removeMessages(MESSAGE_WHAT_LOADPLAYPROGRESSDATA);
                    mWorkerHandler.sendEmptyMessage(MESSAGE_WHAT_LOADPLAYPROGRESSDATA);

                    mMediaPlayer.start();
                }
            });


            mMediaPlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(IMediaPlayer mp) {

                    if (audioInfo.getType() == AudioInfo.TYPE_NET && mMediaPlayer.getCurrentPosition() < (audioInfo.getDuration() - 2 * 1000)) {
                        releasePlayer();
                        //网络歌曲未播放全部，需要重新调用播放歌曲
                        handleSong(audioInfo);
                    } else {
                        releasePlayer();
                        //播放完成，执行下一首操作
                        AudioPlayerManager.getInstance(mContext).next();
                    }


                }
            });

            mMediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(IMediaPlayer mp) {
                    if (audioInfo.getPlayProgress() != 0) {
                        mMediaPlayer.seekTo(audioInfo.getPlayProgress());
                    } else {

                        //发送播放中广播
                        AudioBroadcastReceiver.sendPlayReceiver(mContext, audioInfo);
                        mWorkerHandler.removeMessages(MESSAGE_WHAT_LOADPLAYPROGRESSDATA);
                        mWorkerHandler.sendEmptyMessage(MESSAGE_WHAT_LOADPLAYPROGRESSDATA);

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
        releasePlayer();
        ToastUtil.showTextToast(getApplicationContext(), "播放歌曲出错");
//        ToastUtil.showTextToast(getApplicationContext(), "播放歌曲出错，1秒后播放下一首");
//
//
//        new Thread() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(1000);
//
//                    //播放完成，执行下一首操作
//                    AudioPlayerManager.getInstance(mContext).next();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
    }

    /**
     * 释放播放器
     */
    private void releasePlayer() {

        //移除
        mWorkerHandler.removeMessages(MESSAGE_WHAT_LOADPLAYPROGRESSDATA);

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
     * 获取当前的播放歌曲
     *
     * @return
     */
    private AudioInfo getCurAudioInfo() {
        ConfigInfo configInfo = ConfigInfo.obtain();
        return AudioPlayerManager.getInstance(mContext).getCurSong(configInfo.getPlayHash());
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
