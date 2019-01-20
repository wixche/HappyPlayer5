package com.zlm.hp.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlm.down.entity.DownloadTask;
import com.zlm.hp.audio.utils.MediaUtil;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.db.util.DownloadThreadInfoDB;
import com.zlm.hp.entity.VideoInfo;
import com.zlm.hp.manager.OnLineVideoManager;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.util.ColorUtil;
import com.zlm.hp.util.ResourceUtil;
import com.zlm.hp.util.ToastUtil;
import com.zlm.hp.widget.IconfontImageButtonTextView;
import com.zlm.hp.widget.IconfontTextView;
import com.zlm.libs.widget.MusicSeekBar;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * @Description: 视频
 * @author: zhangliangming
 * @date: 2019-01-05 21:01
 **/
public class VideoActivity extends BaseActivity {

    /**
     * 视频信息
     */
    private VideoInfo mVideoInfo;

    /**
     * 播放器
     */
    private IjkMediaPlayer mMediaPlayer;

    /**
     *
     */
    private SurfaceView mSurfaceView;

    /**
     * 视频在线下载管理器
     */
    private OnLineVideoManager mOnLineVideoManager;

    private AudioBroadcastReceiver mAudioBroadcastReceiver;

    /**
     * 下载视频
     */
    private final int MESSAGE_WHAT_DOWNLOAD_VIDEO = 0;
    /**
     * 视频播放
     */
    private final int MESSAGE_WHAT_PLAY = 1;
    /**
     * 视频播放中
     */
    private final int MESSAGE_WHAT_PLAYING = 2;

    /**
     * 视频暂停
     */
    private final int MESSAGE_WHAT_PAUSE = 3;

    /**
     * 视频完成
     */
    private final int MESSAGE_WHAT_FINISH = 4;

    /**
     * 操作面板
     */
    private RelativeLayout mOpRL;

    /**
     * 标题栏
     */
    private RelativeLayout mHeaderView;
    /**
     * 底部栏
     */
    private LinearLayout mFooterView;

    /**
     *
     */
    private boolean isVisibility = false;

    /**
     * 播放进度
     */
    private TextView mSongProgressTv;

    /**
     * 歌曲总长度
     */
    private TextView mSongDurationTv;
    private MusicSeekBar mMusicSeekBar;
    private ImageView mPauseBtn;
    private ImageView mPlayBtn;

    /**
     * 是否快进
     */
    private boolean isSeekTo = false;

    /**
     * 播放线程
     */
    private Runnable mPlayRunnable = new Runnable() {
        @Override
        public void run() {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying() && !isSeekTo) {

                mUIHandler.sendEmptyMessage(MESSAGE_WHAT_PLAYING);
            }
            mWorkerHandler.postDelayed(mPlayRunnable, 1000);
        }
    };

    /**
     * pop
     */
    private RelativeLayout mPopRelativeLayout;
    private LinearLayout mPopMenuLinearLayout;
    private boolean mIsShowPopMenu = false;

    @Override
    protected void preInitStatusBar() {
        setFullScreen(true);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setStatusBarViewBG(Color.TRANSPARENT);

        setFullScreenActivity();
        super.preInitStatusBar();
    }

    /**
     * 设置全屏
     */
    private void setFullScreenActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFullScreenActivity();
    }

    @Override
    protected int setContentLayoutResID() {
        return R.layout.activity_video;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        TextView titleView = findViewById(R.id.title);

        //视频信息
        mVideoInfo = getIntent().getParcelableExtra(VideoInfo.DATA_KEY);
        titleView.setText(mVideoInfo.getMvName());

        //返回
        ImageView backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //菜单
        IconfontImageButtonTextView menu = findViewById(R.id.right_flag);
        menu.setConvert(true);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsShowPopMenu) {
                    hidePopMenuView();
                } else {
                    if (mPopRelativeLayout == null) {
                        initPopMenuView();
                    }
                    showPopMenuView();
                }
            }
        });
        //视频view
        mSurfaceView = findViewById(R.id.video_surface);
        //操作面板
        mOpRL = findViewById(R.id.op_bg);
        mOpRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isVisibility) {
                    isVisibility = false;
                    mHeaderView.setVisibility(View.INVISIBLE);
                    mFooterView.setVisibility(View.INVISIBLE);
                } else {
                    isVisibility = true;
                    mHeaderView.setVisibility(View.VISIBLE);
                    mFooterView.setVisibility(View.VISIBLE);
                }
            }
        });
        mHeaderView = findViewById(R.id.video_title);
        mFooterView = findViewById(R.id.video_footer);
        //
        mAudioBroadcastReceiver = new AudioBroadcastReceiver();
        mAudioBroadcastReceiver.setReceiverListener(new AudioBroadcastReceiver.AudioReceiverListener() {
            @Override
            public void onReceive(Context context, final Intent intent, final int code) {
                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        handleVideoBroadcastReceiver(intent, code);
                    }
                });
            }
        });
        mAudioBroadcastReceiver.registerReceiver(mContext);
        mOnLineVideoManager = new OnLineVideoManager(mContext);

        mSongProgressTv = findViewById(R.id.songProgress);
        mSongDurationTv = findViewById(R.id.songDuration);

        //进度条
        mMusicSeekBar = findViewById(R.id.lrcseekbar);
        mMusicSeekBar.setTrackingTouchSleepTime(200);
        mMusicSeekBar.setOnMusicListener(new MusicSeekBar.OnMusicListener() {
            @Override
            public String getTimeText() {
                return null;
            }

            @Override
            public String getLrcText() {
                return null;
            }

            @Override
            public void onProgressChanged(MusicSeekBar musicSeekBar) {
                int playStatus = mOnLineVideoManager.getPlayStatus();
                if (playStatus != OnLineVideoManager.PLAYING) {
                    mSongProgressTv.setText(MediaUtil.formatTime((mMusicSeekBar.getProgress())));
                }
            }

            @Override
            public void onTrackingTouchStart(MusicSeekBar musicSeekBar) {

            }

            @Override
            public void onTrackingTouchFinish(MusicSeekBar musicSeekBar) {
                int progress = mMusicSeekBar.getProgress();
                if (mVideoInfo != null && progress <= mVideoInfo.getDuration()) {
                    isSeekTo = true;
                    mMediaPlayer.seekTo(mMusicSeekBar.getProgress());
                }
            }
        });
        //
        mMusicSeekBar.setBackgroundPaintColor(ColorUtil.parserColor("#eeeeee", 50));
        mMusicSeekBar.setSecondProgressColor(Color.argb(100, 255, 255, 255));
        mMusicSeekBar.setProgressColor(Color.rgb(255, 64, 129));
        mMusicSeekBar.setThumbColor(Color.rgb(255, 64, 129));
        mMusicSeekBar.setTimePopupWindowViewColor(Color.argb(200, 255, 64, 129));

        //播放
        mPlayBtn = findViewById(R.id.playbtn);
        mPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                    mOnLineVideoManager.setPlayStatus(OnLineVideoManager.PLAYING);
                    mMediaPlayer.start();
                    mUIHandler.sendEmptyMessage(MESSAGE_WHAT_PLAY);

                } else {
                    //播放视频
                    playVideo();
                }
            }
        });
        //暂停
        mPauseBtn = findViewById(R.id.pausebtn);
        mPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUIHandler.sendEmptyMessage(MESSAGE_WHAT_PAUSE);
            }
        });


        //播放视频
        playVideo();
    }

    /**
     * 初始化pop menu
     */
    private void initPopMenuView() {
        //全屏视图
        mPopRelativeLayout = findViewById(R.id.video_pop);
        mPopRelativeLayout.setVisibility(View.INVISIBLE);
        mPopRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hidePopMenuView();
            }
        });
        //窗口
        mPopMenuLinearLayout = findViewById(R.id.video_pop_menu);

        //关闭窗口
        IconfontTextView closeBtn = findViewById(R.id.closebtn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePopMenuView();
            }
        });
    }

    /**
     *
     */
    private void showPopMenuView() {
        /**
         * 如果该界面还没初始化，则监听
         */
        if (mPopMenuLinearLayout.getHeight() == 0) {
            mPopMenuLinearLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mPopMenuLinearLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    showPopMenuViewAnimation();
                }
            });

        } else {
            showPopMenuViewAnimation();
        }
    }

    /**
     * 隐藏pop menu
     */
    private void hidePopMenuView() {
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, mPopMenuLinearLayout.getHeight());
        translateAnimation.setDuration(250);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIsShowPopMenu = false;
                mPopRelativeLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mPopMenuLinearLayout.clearAnimation();
        mPopMenuLinearLayout.startAnimation(translateAnimation);
    }


    /**
     *
     */
    private void showPopMenuViewAnimation() {
        mPopRelativeLayout.setVisibility(View.VISIBLE);
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, mPopMenuLinearLayout.getHeight(), 0);
        translateAnimation.setDuration(250);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mIsShowPopMenu = true;

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mPopMenuLinearLayout.clearAnimation();
        mPopMenuLinearLayout.startAnimation(translateAnimation);
    }

    /**
     * 播放视频
     */
    private void playVideo() {
        mUIHandler.sendEmptyMessage(MESSAGE_WHAT_PLAY);
        mOnLineVideoManager.setPlayStatus(OnLineVideoManager.PLAYINGNET);
        //下载视频
        mWorkerHandler.sendEmptyMessage(MESSAGE_WHAT_DOWNLOAD_VIDEO);
    }

    /**
     * @param intent
     * @param code
     */
    private void handleVideoBroadcastReceiver(Intent intent, int code) {
        switch (code) {
            case AudioBroadcastReceiver.ACTION_CODE_VIDEO_DOWNLOADING:
            case AudioBroadcastReceiver.ACTION_CODE_VIDEO_DOWNLOADED:

                //网络歌曲下载中
                Bundle downloadOnlineSongBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                DownloadTask downloadingTask = downloadOnlineSongBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                if (mVideoInfo != null && downloadingTask != null && !TextUtils.isEmpty(mVideoInfo.getHash()) && mVideoInfo.getHash().equals(downloadingTask.getTaskId())) {
                    int downloadedSize = DownloadThreadInfoDB.getDownloadedSize(mContext, downloadingTask.getTaskId(), OnLineVideoManager.mThreadNum);
                    double pre = downloadedSize * 1.0 / mVideoInfo.getFileSize();
                    int downloadProgress = (int) (mMusicSeekBar.getMax() * pre);
                    mMusicSeekBar.setSecondaryProgress(downloadProgress);
                }


                //下载中
                break;
            case AudioBroadcastReceiver.ACTION_CODE_VIDEO_STOP:
                mUIHandler.sendEmptyMessage(MESSAGE_WHAT_PAUSE);
                //停止
                break;

            case AudioBroadcastReceiver.ACTION_CODE_PLAYNETVIDEO:
                //播放视频
                Bundle playBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                DownloadTask playTask = playBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                if (playTask != null && mVideoInfo != null && playTask.getTaskId().equals(mVideoInfo.getHash())) {
                    playNetVideo(mVideoInfo);

                }

                break;
        }
    }

    /**
     * 播放网络视频
     *
     * @param videoInfo
     */
    private void playNetVideo(final VideoInfo videoInfo) {
        mOnLineVideoManager.setPlayStatus(OnLineVideoManager.PLAYING);
        String fileName = videoInfo.getTitle();
        String filePath = ResourceUtil.getFilePath(mContext, ResourceConstants.PATH_VIDEO, fileName + "." + videoInfo.getFileExt());
        if (mMediaPlayer != null) {
            releasePlayer();
        }

        mMediaPlayer = new IjkMediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setDataSource(filePath);

        mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mMediaPlayer.setDisplay(mSurfaceView.getHolder());
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.setDisplay(holder);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mUIHandler.sendEmptyMessage(MESSAGE_WHAT_PAUSE);
                }

                if (!isVisibility) {
                    isVisibility = true;
                    mHeaderView.setVisibility(View.VISIBLE);
                    mFooterView.setVisibility(View.VISIBLE);
                }
            }
        });
        mMediaPlayer.prepareAsync();

        mMediaPlayer.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(IMediaPlayer mp) {
                isSeekTo = false;
            }
        });


        mMediaPlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer mp) {

                if (mMediaPlayer.getCurrentPosition() < (videoInfo.getDuration() - 2 * 1000)) {
                    releasePlayer();
                    //网络视频未播放全部，需要重新调用播放视频
                    playNetVideo(videoInfo);
                } else {
                    releasePlayer();
                    //播放完成
                    mUIHandler.sendEmptyMessage(MESSAGE_WHAT_FINISH);
                }


            }
        });

        mMediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                mMediaPlayer.start();
            }
        });

        mMediaPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {

                handleError();

                return false;
            }
        });
    }

    /**
     * 处理错误
     */
    private void handleError() {
        releasePlayer();
        ToastUtil.showTextToast(getApplicationContext(), "播放视频出错");
    }


    @Override
    protected void handleUIMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_WHAT_PLAY:

                //
                mSongProgressTv.setText(MediaUtil.formatTime(0));
                mSongDurationTv.setText(MediaUtil.formatTime((int) mVideoInfo.getDuration()));

                //设置进度条
                mMusicSeekBar.setEnabled(true);
                mMusicSeekBar.setMax((int) mVideoInfo.getDuration());
                mMusicSeekBar.setProgress(0);
                mMusicSeekBar.setSecondaryProgress(0);

                if (mPauseBtn.getVisibility() != View.VISIBLE)
                    mPauseBtn.setVisibility(View.VISIBLE);

                if (mPlayBtn.getVisibility() != View.INVISIBLE)
                    mPlayBtn.setVisibility(View.INVISIBLE);

                mWorkerHandler.postDelayed(mPlayRunnable, 0);

                break;
            case MESSAGE_WHAT_PLAYING:

                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    int playProgress = (int) mMediaPlayer.getCurrentPosition();
                    mMusicSeekBar.setProgress(playProgress);
                    mSongProgressTv.setText(MediaUtil.formatTime(playProgress));
                }

                break;
            case MESSAGE_WHAT_PAUSE:

                mOnLineVideoManager.setPlayStatus(OnLineVideoManager.PAUSE);

                mWorkerHandler.removeCallbacks(mPlayRunnable);

                if (mMediaPlayer != null) {

                    mMediaPlayer.pause();

                    int playProgress = (int) mMediaPlayer.getCurrentPosition();
                    mMusicSeekBar.setProgress(playProgress);
                    mSongProgressTv.setText(MediaUtil.formatTime(playProgress));
                }
                mPlayBtn.setVisibility(View.VISIBLE);
                mPauseBtn.setVisibility(View.INVISIBLE);

                break;
            case MESSAGE_WHAT_FINISH:

                mOnLineVideoManager.setPlayStatus(OnLineVideoManager.STOP);
                mWorkerHandler.removeCallbacks(mPlayRunnable);

                mPlayBtn.setVisibility(View.VISIBLE);
                mPauseBtn.setVisibility(View.INVISIBLE);

                mMusicSeekBar.setEnabled(false);
                mMusicSeekBar.setProgress(0);
                mMusicSeekBar.setSecondaryProgress(0);
                mMusicSeekBar.setMax(0);

                break;
        }
    }

    @Override
    protected void handleWorkerMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_WHAT_DOWNLOAD_VIDEO:
                mOnLineVideoManager.addDownloadTask(mVideoInfo);
                break;
        }
    }

    @Override
    public void finish() {

        releasePlayer();

        if (mOnLineVideoManager != null) {
            mOnLineVideoManager.release();
        }
        if (mAudioBroadcastReceiver != null) {
            mAudioBroadcastReceiver.unregisterReceiver(mContext);
        }

        super.finish();
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

    @Override
    public void onBackPressed() {
        if (mIsShowPopMenu) {
            hidePopMenuView();
            return;
        }
        super.onBackPressed();
    }
}
