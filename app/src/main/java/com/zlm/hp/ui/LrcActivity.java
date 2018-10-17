package com.zlm.hp.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlm.down.entity.DownloadTask;
import com.zlm.hp.async.AsyncHandlerTask;
import com.zlm.hp.audio.utils.MediaUtil;
import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.db.util.DownloadThreadInfoDB;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.lyrics.LyricsReader;
import com.zlm.hp.lyrics.widget.AbstractLrcView;
import com.zlm.hp.lyrics.widget.ManyLyricsView;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.manager.LyricsManager;
import com.zlm.hp.manager.OnLineAudioManager;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.util.ColorUtil;
import com.zlm.hp.util.ToastUtil;
import com.zlm.libs.widget.MusicSeekBar;
import com.zlm.libs.widget.RotateLayout;

/**
 * @Description: 歌词界面
 * @author: zhangliangming
 * @date: 2018-10-16 19:43
 **/
public class LrcActivity extends BaseActivity {

    /**
     * 旋转布局界面
     */
    private RotateLayout mRotateLayout;
    private LinearLayout mLrcPlaybarLinearLayout;

    /**
     * 歌曲名称tv
     */
    private TextView mSongNameTextView;
    /**
     * 歌手tv
     */
    private TextView mSingerNameTextView;
    ////////////////////////////底部

    private MusicSeekBar mMusicSeekBar;
    /**
     * 播放
     */
    private RelativeLayout mPlayBtn;
    /**
     * 暂停
     */
    private RelativeLayout mPauseBtn;
    /**
     * 下一首
     */
    private RelativeLayout mNextBtn;

    /**
     * 上一首
     */
    private RelativeLayout mPreBtn;
    /**
     * 播放进度
     */
    private TextView mSongProgressTv;

    /**
     * 歌曲总长度
     */
    private TextView mSongDurationTv;

    /**
     * 多行歌词视图
     */
    private ManyLyricsView mManyLineLyricsView;

    //播放模式
    private ImageView modeAllImg;
    private ImageView modeRandomImg;
    private ImageView modeSingleImg;

    /**
     * 音频广播
     */
    private AudioBroadcastReceiver mAudioBroadcastReceiver;

    //
    private ConfigInfo mConfigInfo;

    /**
     * 加载数据
     */
    private final int LOAD_DATA = 0;

    @Override
    protected int setContentLayoutResID() {
        return R.layout.activity_lrc;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        initData();
        initView();
        initReceiver();
    }

    private void initData() {
        mConfigInfo = ConfigInfo.obtain();
        mUIHandler.sendEmptyMessage(LOAD_DATA);
    }


    @Override
    protected void handleUIMessage(Message msg) {
        switch (msg.what) {
            case LOAD_DATA:
                ConfigInfo configInfo = ConfigInfo.obtain();
                AudioInfo audioInfo = AudioPlayerManager.newInstance(mContext).getCurSong(configInfo.getPlayHash());
                Intent intent = new Intent();
                if (audioInfo != null) {

                    Bundle bundle = new Bundle();
                    bundle.putParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY, audioInfo);
                    intent.putExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY, bundle);
                    handleAudioBroadcastReceiver(intent, AudioBroadcastReceiver.ACTION_CODE_INIT);

                    int playStatus = AudioPlayerManager.newInstance(mContext).getPlayStatus();
                    if (playStatus == AudioPlayerManager.PLAYING) {
                        handleAudioBroadcastReceiver(intent, AudioBroadcastReceiver.ACTION_CODE_PLAY);
                    }

                } else {
                    handleAudioBroadcastReceiver(intent, AudioBroadcastReceiver.ACTION_CODE_NULL);
                }

                break;
        }
    }

    @Override
    protected void handleWorkerMessage(Message msg) {

    }


    private void initView() {
        mRotateLayout = findViewById(R.id.rotateLayout);
        mRotateLayout.setRotateLayoutListener(new RotateLayout.RotateLayoutListener() {
            @Override
            public void finishActivity() {
                finish();
                overridePendingTransition(0, 0);
            }
        });
        //
        mLrcPlaybarLinearLayout = findViewById(R.id.lrc_playbar);
        mRotateLayout.addIgnoreView(mLrcPlaybarLinearLayout);

        //返回按钮
        ImageView backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRotateLayout.closeView();
            }
        });
        //
        mSongNameTextView = findViewById(R.id.songName);
        mSingerNameTextView = findViewById(R.id.singerName);

        //
        mManyLineLyricsView = findViewById(R.id.manyLineLyricsView);
        mManyLineLyricsView.setPaintColor(new int[]{ColorUtil.parserColor("#ffffff"), ColorUtil.parserColor("#ffffff")});
        mManyLineLyricsView.setOnLrcClickListener(new ManyLyricsView.OnLrcClickListener() {
            @Override
            public void onLrcPlayClicked(int progress) {
                //
                AudioInfo audioInfo = AudioPlayerManager.newInstance(mContext).getCurSong(mConfigInfo.getPlayHash());
                if (audioInfo != null && progress <= audioInfo.getDuration()) {
                    audioInfo.setPlayProgress(progress);
                    AudioPlayerManager.newInstance(mContext).seekto(audioInfo);
                }
            }
        });

        //设置字体大小和歌词颜色
        mManyLineLyricsView.setSize(mConfigInfo.getLrcFontSize(), mConfigInfo.getLrcFontSize(), false);
        int lrcColor = ColorUtil.parserColor(ConfigInfo.LRC_COLORS_STRING[mConfigInfo.getLrcColorIndex()]);
        mManyLineLyricsView.setPaintHLColor(new int[]{lrcColor, lrcColor}, false);
        mManyLineLyricsView.setPaintColor(new int[]{Color.WHITE, Color.WHITE}, false);

        mSongProgressTv = findViewById(R.id.songProgress);
        mSongDurationTv = findViewById(R.id.songDuration);

        //进度条
        mMusicSeekBar = findViewById(R.id.lrcseekbar);
        mMusicSeekBar.setTrackingTouchSleepTime(1000);
        mMusicSeekBar.setOnMusicListener(new MusicSeekBar.OnMusicListener() {
            @Override
            public String getTimeText() {
                return MediaUtil.formatTime(mMusicSeekBar.getProgress());
            }

            @Override
            public String getLrcText() {
                return null;
            }

            @Override
            public void onProgressChanged(MusicSeekBar musicSeekBar) {
                int playStatus = AudioPlayerManager.newInstance(mContext).getPlayStatus();
                if (playStatus != AudioPlayerManager.PLAYING) {
                    mSongProgressTv.setText(MediaUtil.formatTime((mMusicSeekBar.getProgress())));
                }
            }

            @Override
            public void onTrackingTouchStart(MusicSeekBar musicSeekBar) {

            }

            @Override
            public void onTrackingTouchFinish(MusicSeekBar musicSeekBar) {
                int progress = mMusicSeekBar.getProgress();
                AudioInfo audioInfo = AudioPlayerManager.newInstance(mContext).getCurSong(mConfigInfo.getPlayHash());
                if (audioInfo != null && progress <= audioInfo.getDuration()) {
                    audioInfo.setPlayProgress(progress);
                    AudioPlayerManager.newInstance(mContext).seekto(audioInfo);
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
                AudioPlayerManager.newInstance(mContext).play(mMusicSeekBar.getProgress());
            }
        });
        //暂停
        mPauseBtn = findViewById(R.id.pausebtn);
        mPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioPlayerManager.newInstance(mContext).pause();
            }
        });

        //下一首
        mNextBtn = findViewById(R.id.nextbtn);
        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioPlayerManager.newInstance(mContext).next();
            }
        });

        //上一首
        mPreBtn = findViewById(R.id.prebtn);
        mPreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioPlayerManager.newInstance(mContext).pre();
            }
        });

        /////////播放模式//////////////
        //顺序播放
        modeAllImg = findViewById(R.id.modeAll);
        modeRandomImg = findViewById(R.id.modeRandom);
        modeSingleImg = findViewById(R.id.modeSingle);


        modeAllImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPlayModeView(1, modeAllImg, modeRandomImg, modeSingleImg, true);
            }
        });

        modeRandomImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPlayModeView(3, modeAllImg, modeRandomImg, modeSingleImg, true);
            }
        });

        modeSingleImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPlayModeView(0, modeAllImg, modeRandomImg, modeSingleImg, true);
            }
        });
        initPlayModeView(mConfigInfo.getPlayModel(), modeAllImg, modeRandomImg, modeSingleImg, false);

        //
        RelativeLayout playListMenu = findViewById(R.id.playlistmenu);
        playListMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }

    private void initReceiver() {
        //音频广播
        mAudioBroadcastReceiver = new AudioBroadcastReceiver();
        mAudioBroadcastReceiver.setReceiverListener(new AudioBroadcastReceiver.AudioReceiverListener() {
            @Override
            public void onReceive(Context context, final Intent intent, final int code) {
                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        handleAudioBroadcastReceiver(intent, code);
                    }
                });
            }
        });
        mAudioBroadcastReceiver.registerReceiver(mContext);
    }

    /**
     * @param intent
     * @param code
     */
    private void handleAudioBroadcastReceiver(Intent intent, int code) {
        switch (code) {
            case AudioBroadcastReceiver.ACTION_CODE_NULL:

                //空数据
                mSongNameTextView.setText(R.string.def_songName);
                mSingerNameTextView.setText(R.string.def_artist);
                mPauseBtn.setVisibility(View.INVISIBLE);
                mPlayBtn.setVisibility(View.VISIBLE);

                mSongProgressTv.setText("00:00");
                mSongDurationTv.setText("00:00");

                //
                mMusicSeekBar.setEnabled(false);
                mMusicSeekBar.setProgress(0);
                mMusicSeekBar.setSecondaryProgress(0);
                mMusicSeekBar.setMax(0);

                //
                mManyLineLyricsView.initLrcData();
                break;
            case AudioBroadcastReceiver.ACTION_CODE_INIT:
                Bundle initBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                AudioInfo initAudioInfo = initBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                if (initAudioInfo != null) {
                    mSongNameTextView.setText(initAudioInfo.getSongName());
                    mSingerNameTextView.setText(initAudioInfo.getSingerName());
                    mPauseBtn.setVisibility(View.INVISIBLE);
                    mPlayBtn.setVisibility(View.VISIBLE);

                    //
                    mSongProgressTv.setText(MediaUtil.formatTime((int) initAudioInfo.getPlayProgress()));
                    mSongDurationTv.setText(MediaUtil.formatTime((int) initAudioInfo.getDuration()));

                    //设置进度条
                    mMusicSeekBar.setEnabled(true);
                    mMusicSeekBar.setMax((int) initAudioInfo.getDuration());
                    mMusicSeekBar.setProgress((int) initAudioInfo.getPlayProgress());
                    mMusicSeekBar.setSecondaryProgress(0);

                    //加载歌词
                    String keyWords = "";
                    if (initAudioInfo.getSingerName().equals("未知")) {
                        keyWords = initAudioInfo.getSongName();
                    } else {
                        keyWords = initAudioInfo.getTitle();
                    }
                    LyricsManager.newInstance(mContext).loadLyrics(keyWords, keyWords, initAudioInfo.getDuration() + "", initAudioInfo.getHash(), mConfigInfo.isWifi(), new AsyncHandlerTask(mUIHandler, mWorkerHandler), null);

                }
                //加载中
                mManyLineLyricsView.initLrcData();
                mManyLineLyricsView.setLrcStatus(AbstractLrcView.LRCSTATUS_LOADING);
                break;
            case AudioBroadcastReceiver.ACTION_CODE_PLAY:
                if (mPauseBtn.getVisibility() != View.VISIBLE)
                    mPauseBtn.setVisibility(View.VISIBLE);

                if (mPlayBtn.getVisibility() != View.INVISIBLE)
                    mPlayBtn.setVisibility(View.INVISIBLE);

                Bundle playBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                AudioInfo playAudioInfo = playBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                if (playAudioInfo != null) {
                    //更新歌词
                    if (mManyLineLyricsView.getLyricsReader() != null && mManyLineLyricsView.getLyricsReader().getHash().equals(playAudioInfo.getHash()) && mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mManyLineLyricsView.getLrcPlayerStatus() != AbstractLrcView.LRCPLAYERSTATUS_PLAY) {
                        mManyLineLyricsView.play((int) playAudioInfo.getPlayProgress());
                    }
                }

                break;
            case AudioBroadcastReceiver.ACTION_CODE_PLAYING:

                Bundle playingBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                AudioInfo playingAudioInfo = playingBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                if (playingAudioInfo != null) {
                    mMusicSeekBar.setProgress((int) playingAudioInfo.getPlayProgress());

                    //
                    mSongProgressTv.setText(MediaUtil.formatTime((int) playingAudioInfo.getPlayProgress()));
                    if (mManyLineLyricsView.getLyricsReader() != null && mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mManyLineLyricsView.getLrcPlayerStatus() != AbstractLrcView.LRCPLAYERSTATUS_PLAY && mManyLineLyricsView.getLyricsReader().getHash().equals(playingAudioInfo.getHash())) {
                        mManyLineLyricsView.play((int) playingAudioInfo.getPlayProgress());
                    }
                }

                break;
            case AudioBroadcastReceiver.ACTION_CODE_STOP:
                //暂停完成
                if (mPauseBtn.getVisibility() != View.INVISIBLE)
                    mPauseBtn.setVisibility(View.INVISIBLE);

                if (mPlayBtn.getVisibility() != View.VISIBLE)
                    mPlayBtn.setVisibility(View.VISIBLE);

                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.pause();
                }

                break;

            case AudioBroadcastReceiver.ACTION_CODE_SEEKTO:
                Bundle seektoBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                AudioInfo seektoAudioInfo = seektoBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                if (seektoAudioInfo != null) {
                    mSongProgressTv.setText(MediaUtil.formatTime((int) seektoAudioInfo.getPlayProgress()));
                    mMusicSeekBar.setProgress(seektoAudioInfo.getPlayProgress());

                    if (mManyLineLyricsView.getLyricsReader() != null && mManyLineLyricsView.getLyricsReader().getHash().equals(seektoAudioInfo.getHash())) {
                        mManyLineLyricsView.seekto((int) seektoAudioInfo.getPlayProgress());
                    }

                }
                break;

            case AudioBroadcastReceiver.ACTION_CODE_DOWNLOADONLINESONG:
                //网络歌曲下载中
                Bundle downloadOnlineSongBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                DownloadTask downloadingTask = downloadOnlineSongBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                String hash = mConfigInfo.getPlayHash();
                AudioInfo audioInfo = AudioPlayerManager.newInstance(mContext).getCurSong(hash);
                if (audioInfo != null && downloadingTask != null && !TextUtils.isEmpty(hash) && hash.equals(downloadingTask.getTaskId())) {
                    int downloadedSize = DownloadThreadInfoDB.getDownloadedSize(mContext, downloadingTask.getTaskId(), OnLineAudioManager.threadNum);
                    double pre = downloadedSize * 1.0 / audioInfo.getFileSize();
                    int downloadProgress = (int) (mMusicSeekBar.getMax() * pre);
                    mMusicSeekBar.setSecondaryProgress(downloadProgress);
                }

                break;

            case AudioBroadcastReceiver.ACTION_CODE_LRCLOADED:
                //歌词加载完成
                Bundle lrcloadedBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                String lrcHash = lrcloadedBundle.getString(AudioBroadcastReceiver.ACTION_DATA_KEY);
                AudioInfo curAudioInfo = AudioPlayerManager.newInstance(mContext).getCurSong(mConfigInfo.getPlayHash());
                if (curAudioInfo != null && lrcHash.equals(curAudioInfo.getHash())) {
                    LyricsReader lyricsReader = LyricsManager.newInstance(mContext).getLyricsReader(lrcHash);
                    mManyLineLyricsView.setLyricsReader(lyricsReader);
                    if (lyricsReader != null) {
                        if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                            mManyLineLyricsView.seekto((int) curAudioInfo.getPlayProgress());
                        }
                    }
                }
                break;
        }
    }

    /**
     * 初始化播放列表播放模式
     *
     * @param playMode
     * @param modeAllImg
     * @param modeRandomImg
     * @param modeSingleImg
     */
    private void initPlayModeView(int playMode, ImageView modeAllImg, ImageView modeRandomImg, ImageView modeSingleImg, boolean isTipShow) {
        if (playMode == 0) {
            if (isTipShow)
                ToastUtil.showTextToast(LrcActivity.this, "顺序播放");
            modeAllImg.setVisibility(View.VISIBLE);
            modeRandomImg.setVisibility(View.INVISIBLE);
            modeSingleImg.setVisibility(View.INVISIBLE);
        } else if (playMode == 1) {
            if (isTipShow)
                ToastUtil.showTextToast(LrcActivity.this, "随机播放");
            modeAllImg.setVisibility(View.INVISIBLE);
            modeRandomImg.setVisibility(View.VISIBLE);
            modeSingleImg.setVisibility(View.INVISIBLE);
        } else {
            if (isTipShow)
                ToastUtil.showTextToast(LrcActivity.this, "单曲播放");
            modeAllImg.setVisibility(View.INVISIBLE);
            modeRandomImg.setVisibility(View.INVISIBLE);
            modeSingleImg.setVisibility(View.VISIBLE);
        }
        //
        mConfigInfo.setPlayModel(playMode).save();
    }

    @Override
    public void onBackPressed() {
        mRotateLayout.closeView();
    }

    @Override
    protected void onDestroy() {
        mManyLineLyricsView.release();
        destroyReceiver();
        super.onDestroy();
    }

    /**
     * 销毁广播
     */
    private void destroyReceiver() {

        if (mAudioBroadcastReceiver != null) {
            mAudioBroadcastReceiver.unregisterReceiver(mContext);
        }

    }
}
