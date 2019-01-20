package com.zlm.hp.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.zlm.hp.async.AsyncHandlerTask;
import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.lyrics.LyricsReader;
import com.zlm.hp.lyrics.widget.AbstractLrcView;
import com.zlm.hp.lyrics.widget.ManyLyricsView;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.manager.LyricsManager;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.util.AniUtil;
import com.zlm.hp.util.ColorUtil;
import com.zlm.hp.util.ImageUtil;
import com.zlm.hp.widget.TransitionImageView;
import com.zlm.hp.widget.lock.LockButtonRelativeLayout;
import com.zlm.hp.widget.lock.LockPalyOrPauseButtonRelativeLayout;
import com.zlm.libs.widget.SwipeBackLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @Description: 锁屏界面
 * @author: zhangliangming
 * @date: 2018-12-08 15:00
 **/
public class LockActivity extends BaseActivity {
    //
    private ConfigInfo mConfigInfo;
    /**
     * 加载数据
     */
    private final int MESSAGE_WHAT_LOAD_DATA = 0;

    /**
     * 更新时间
     */
    private final int MESSAGE_WHAT_UPDATE_TIME = 1;

    /**
     *
     */
    private SwipeBackLayout mSwipeBackLayout;

    /**
     * 滑动提示图标
     */
    private ImageView mLockImageView;
    private AnimationDrawable mAniLoading;
    /**
     * 时间
     */
    private TextView mTimeTextView;
    /**
     * 日期
     */
    private TextView mDateTextView;
    /**
     * 星期几
     */
    private TextView mDayTextView;

    /**
     * 歌曲名称tv
     */
    private TextView mSongNameTextView;
    /**
     * 歌手tv
     */
    private TextView mSingerNameTextView;

    //暂停、播放图标
    private ImageView mPlayImageView;
    private ImageView mPauseImageView;
    /**
     * 上一首按钮
     */
    private LockButtonRelativeLayout mPrewButton;
    /**
     * 下一首按钮
     */
    private LockButtonRelativeLayout mNextButton;
    /**
     * 播放或者暂停按钮
     */
    private LockPalyOrPauseButtonRelativeLayout mPlayOrPauseButton;

    /**
     * 分钟变化广播
     */
    private BroadcastReceiver mTimeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                mUIHandler.sendEmptyMessage(MESSAGE_WHAT_UPDATE_TIME);
            }
        }
    };

    /**
     * 音频广播
     */
    private AudioBroadcastReceiver mAudioBroadcastReceiver;

    /**
     * 多行歌词视图
     */
    private ManyLyricsView mManyLineLyricsView;

    /**
     * 歌手写真图片
     */
    private TransitionImageView mSingerImageView;


    //、、、、、、、、、、、、、、、、、、、、、、、、、翻译和音译歌词、、、、、、、、、、、、、、、、、、、、、、、、、、、
    //翻译歌词
    private ImageView mHideTranslateImg;
    private ImageView mShowTranslateImg;
    //音译歌词
    private ImageView mHideTransliterationImg;
    private ImageView mShowTransliterationImg;

    //翻译歌词/音译歌词
    private ImageView mShowTTToTranslateImg;
    private ImageView mShowTTToTransliterationImg;
    private ImageView mHideTTImg;

    private final int HASTRANSLATELRC = 0;
    private final int HASTRANSLITERATIONLRC = 1;
    private final int HASTRANSLATEANDTRANSLITERATIONLRC = 2;
    private final int NOEXTRALRC = 3;

    private Handler mExtraLrcTypeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case NOEXTRALRC:

                    //翻译歌词
                    mHideTranslateImg.setVisibility(View.INVISIBLE);
                    mShowTranslateImg.setVisibility(View.INVISIBLE);
                    //音译歌词
                    mHideTransliterationImg.setVisibility(View.INVISIBLE);
                    mShowTransliterationImg.setVisibility(View.INVISIBLE);

                    //翻译歌词/音译歌词
                    mShowTTToTranslateImg.setVisibility(View.INVISIBLE);
                    mShowTTToTransliterationImg.setVisibility(View.INVISIBLE);
                    mHideTTImg.setVisibility(View.INVISIBLE);


                    break;
                case HASTRANSLATEANDTRANSLITERATIONLRC:


                    //翻译歌词
                    mHideTranslateImg.setVisibility(View.INVISIBLE);
                    mShowTranslateImg.setVisibility(View.INVISIBLE);
                    //音译歌词
                    mHideTransliterationImg.setVisibility(View.INVISIBLE);
                    mShowTransliterationImg.setVisibility(View.INVISIBLE);


                    //翻译歌词/音译歌词
                    int lrcShowType = (int) msg.obj;
                    if (lrcShowType == ConfigInfo.EXTRALRCSTATUS_SHOWTRANSLATELRC) {
                        mShowTTToTranslateImg.setVisibility(View.INVISIBLE);
                        mShowTTToTransliterationImg.setVisibility(View.VISIBLE);
                        mHideTTImg.setVisibility(View.INVISIBLE);
                    } else if (lrcShowType == ConfigInfo.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC) {
                        mShowTTToTranslateImg.setVisibility(View.INVISIBLE);
                        mShowTTToTransliterationImg.setVisibility(View.INVISIBLE);
                        mHideTTImg.setVisibility(View.VISIBLE);
                    } else {
                        mShowTTToTranslateImg.setVisibility(View.VISIBLE);
                        mShowTTToTransliterationImg.setVisibility(View.INVISIBLE);
                        mHideTTImg.setVisibility(View.INVISIBLE);

                    }


                    break;
                case HASTRANSLITERATIONLRC:

                    //翻译歌词
                    mHideTranslateImg.setVisibility(View.INVISIBLE);
                    mShowTranslateImg.setVisibility(View.INVISIBLE);


                    //音译歌词
                    if (msg.obj == null) {
                        mHideTransliterationImg.setVisibility(View.VISIBLE);
                        mShowTransliterationImg.setVisibility(View.INVISIBLE);
                    } else {
                        mShowTransliterationImg.setVisibility(View.VISIBLE);
                        mHideTransliterationImg.setVisibility(View.INVISIBLE);
                    }

                    //翻译歌词/音译歌词
                    mShowTTToTranslateImg.setVisibility(View.INVISIBLE);
                    mShowTTToTransliterationImg.setVisibility(View.INVISIBLE);
                    mHideTTImg.setVisibility(View.INVISIBLE);

                    break;
                case HASTRANSLATELRC:


                    //翻译歌词

                    if (msg.obj == null) {
                        mHideTranslateImg.setVisibility(View.VISIBLE);
                        mShowTranslateImg.setVisibility(View.INVISIBLE);
                    } else {
                        mShowTranslateImg.setVisibility(View.VISIBLE);
                        mHideTranslateImg.setVisibility(View.INVISIBLE);
                    }

                    //音译歌词
                    mHideTransliterationImg.setVisibility(View.INVISIBLE);
                    mShowTransliterationImg.setVisibility(View.INVISIBLE);

                    //翻译歌词/音译歌词
                    mShowTTToTranslateImg.setVisibility(View.INVISIBLE);
                    mShowTTToTransliterationImg.setVisibility(View.INVISIBLE);
                    mHideTTImg.setVisibility(View.INVISIBLE);


                    break;

            }

        }
    };

    //、、、、、、、、、、、、、、、、、、、、、、、、、翻译和音译歌词、、、、、、、、、、、、、、、、、、、、、、、、、、、


    @Override
    protected int setContentLayoutResID() {
        return R.layout.activity_lock;
    }

    @Override
    protected void preInitStatusBar() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        setStatusBarViewBG(Color.TRANSPARENT);
        super.preInitStatusBar();
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        initData();
        initView();
        //注册广播
        registerReceiver();
        setDate();
        mUIHandler.sendEmptyMessage(MESSAGE_WHAT_LOAD_DATA);
    }

    private void initData() {
        mConfigInfo = ConfigInfo.obtain();
    }

    private void initView() {
        mSwipeBackLayout = findViewById(R.id.swipeback_layout);
        mSwipeBackLayout.setSwipeBackLayoutListener(new SwipeBackLayout.SwipeBackLayoutListener() {

            @Override
            public void finishActivity() {
                finish();
                overridePendingTransition(0, 0);
            }
        });

        //提示右滑动图标
        mLockImageView = findViewById(R.id.tip_image);
        mAniLoading = (AnimationDrawable) mLockImageView.getBackground();
        //时间
        mTimeTextView = findViewById(R.id.time);
        mDateTextView = findViewById(R.id.date);
        mDayTextView = findViewById(R.id.day);

        //
        //歌手与歌名
        mSongNameTextView = findViewById(R.id.songName);
        mSingerNameTextView = findViewById(R.id.songer);


        mPlayImageView = findViewById(R.id.play);
        mPauseImageView = findViewById(R.id.pause);
        //播放按钮、上一首，下一首
        mPrewButton = findViewById(R.id.prev_button);
        mPrewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AudioPlayerManager.getInstance(mContext).pre();

            }
        });

        mNextButton = findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AudioPlayerManager.getInstance(mContext).next();

            }
        });

        mPlayOrPauseButton = findViewById(R.id.play_pause_button);
        mPlayOrPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AudioPlayerManager.getInstance(mContext).playOrPause();
            }
        });

        //歌手写真
        mSingerImageView = findViewById(R.id.singerimg);
        mSingerImageView.setVisibility(View.INVISIBLE);

        //
        mManyLineLyricsView = findViewById(R.id.manyLineLyricsView);
        mManyLineLyricsView.setPaintColor(new int[]{ColorUtil.parserColor("#ffffff"), ColorUtil.parserColor("#ffffff")});
        mManyLineLyricsView.setTouchAble(false);
        //设置字体大小和歌词颜色
        mManyLineLyricsView.setSize(mConfigInfo.getLrcFontSize(), mConfigInfo.getLrcFontSize(), false);
        int lrcColor = ColorUtil.parserColor(ConfigInfo.LRC_COLORS_STRING[mConfigInfo.getLrcColorIndex()]);
        mManyLineLyricsView.setPaintHLColor(new int[]{lrcColor, lrcColor}, false);
        mManyLineLyricsView.setPaintColor(new int[]{Color.WHITE, Color.WHITE}, false);


        //翻译歌词
        mHideTranslateImg = findViewById(R.id.hideTranslateImg);
        mHideTranslateImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHideTranslateImg.setVisibility(View.INVISIBLE);
                mShowTranslateImg.setVisibility(View.VISIBLE);


                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_NOSHOWEXTRALRC);
                }

                mConfigInfo.setExtraLrcStatus(ConfigInfo.EXTRALRCSTATUS_NOSHOWEXTRALRC);
                AudioBroadcastReceiver.sendReceiver(mContext,AudioBroadcastReceiver.ACTION_CODE_LOCK_LRC_CHANGE);
            }
        });
        mShowTranslateImg = findViewById(R.id.showTranslateImg);
        mShowTranslateImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHideTranslateImg.setVisibility(View.VISIBLE);
                mShowTranslateImg.setVisibility(View.INVISIBLE);

                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC);

                }

                mConfigInfo.setExtraLrcStatus(ConfigInfo.EXTRALRCSTATUS_SHOWTRANSLATELRC);
                AudioBroadcastReceiver.sendReceiver(mContext,AudioBroadcastReceiver.ACTION_CODE_LOCK_LRC_CHANGE);
            }
        });
        //音译歌词
        mHideTransliterationImg = findViewById(R.id.hideTransliterationImg);
        mHideTransliterationImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHideTransliterationImg.setVisibility(View.INVISIBLE);
                mShowTransliterationImg.setVisibility(View.VISIBLE);

                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_NOSHOWEXTRALRC);
                }

                mConfigInfo.setExtraLrcStatus(ConfigInfo.EXTRALRCSTATUS_NOSHOWEXTRALRC);
                AudioBroadcastReceiver.sendReceiver(mContext,AudioBroadcastReceiver.ACTION_CODE_LOCK_LRC_CHANGE);
            }
        });
        mShowTransliterationImg = findViewById(R.id.showTransliterationImg);
        mShowTransliterationImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHideTransliterationImg.setVisibility(View.VISIBLE);
                mShowTransliterationImg.setVisibility(View.INVISIBLE);

                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC);
                }

                mConfigInfo.setExtraLrcStatus(ConfigInfo.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC);
                AudioBroadcastReceiver.sendReceiver(mContext,AudioBroadcastReceiver.ACTION_CODE_LOCK_LRC_CHANGE);
            }
        });

        //翻译歌词/音译歌词
        mShowTTToTranslateImg = findViewById(R.id.showTTToTranslateImg);
        mShowTTToTranslateImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mShowTTToTranslateImg.setVisibility(View.INVISIBLE);
                mShowTTToTransliterationImg.setVisibility(View.VISIBLE);
                mHideTTImg.setVisibility(View.INVISIBLE);

                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC);
                }

                mConfigInfo.setExtraLrcStatus(ConfigInfo.EXTRALRCSTATUS_SHOWTRANSLATELRC);
                AudioBroadcastReceiver.sendReceiver(mContext,AudioBroadcastReceiver.ACTION_CODE_LOCK_LRC_CHANGE);
            }
        });
        mShowTTToTransliterationImg = findViewById(R.id.showTTToTransliterationImg);
        mShowTTToTransliterationImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mShowTTToTranslateImg.setVisibility(View.INVISIBLE);
                mShowTTToTransliterationImg.setVisibility(View.INVISIBLE);
                mHideTTImg.setVisibility(View.VISIBLE);

                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC);
                }

                mConfigInfo.setExtraLrcStatus(ConfigInfo.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC);
                AudioBroadcastReceiver.sendReceiver(mContext,AudioBroadcastReceiver.ACTION_CODE_LOCK_LRC_CHANGE);
            }
        });
        mHideTTImg = findViewById(R.id.hideTTImg);
        mHideTTImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mShowTTToTranslateImg.setVisibility(View.VISIBLE);
                mShowTTToTransliterationImg.setVisibility(View.INVISIBLE);
                mHideTTImg.setVisibility(View.INVISIBLE);

                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_NOSHOWEXTRALRC);
                }

                mConfigInfo.setExtraLrcStatus(ConfigInfo.EXTRALRCSTATUS_NOSHOWEXTRALRC);
                AudioBroadcastReceiver.sendReceiver(mContext,AudioBroadcastReceiver.ACTION_CODE_LOCK_LRC_CHANGE);
            }
        });

        //
        mManyLineLyricsView.setExtraLyricsListener(new AbstractLrcView.ExtraLyricsListener() {
            @Override
            public void extraLrcCallback() {
                if (mManyLineLyricsView.getLyricsReader() == null) {
                    return;
                }
                int extraLrcType = mManyLineLyricsView.getExtraLrcType();
                if (extraLrcType == AbstractLrcView.EXTRALRCTYPE_NOLRC) {
                    mExtraLrcTypeHandler.sendEmptyMessage(NOEXTRALRC);
                } else if (extraLrcType == AbstractLrcView.EXTRALRCTYPE_TRANSLATELRC) {
                    if (mConfigInfo.getExtraLrcStatus() == ConfigInfo.EXTRALRCSTATUS_SHOWTRANSLATELRC) {
                        mExtraLrcTypeHandler.sendEmptyMessage(HASTRANSLATELRC);
                        mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC);
                    } else {
                        Message msg = Message.obtain();
                        msg.what = HASTRANSLATELRC;
                        msg.obj = "";
                        mExtraLrcTypeHandler.sendMessage(msg);
                        mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_NOSHOWEXTRALRC);
                    }
                } else if (extraLrcType == AbstractLrcView.EXTRALRCTYPE_TRANSLITERATIONLRC) {

                    if (mConfigInfo.getExtraLrcStatus() == ConfigInfo.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC) {
                        mExtraLrcTypeHandler.sendEmptyMessage(HASTRANSLITERATIONLRC);
                        mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC);
                    } else {
                        Message msg = Message.obtain();
                        msg.what = HASTRANSLITERATIONLRC;
                        msg.obj = "";
                        mExtraLrcTypeHandler.sendMessage(msg);
                        mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_NOSHOWEXTRALRC);
                    }

                } else if (extraLrcType == AbstractLrcView.EXTRALRCTYPE_BOTH) {
                    if (mConfigInfo.getExtraLrcStatus() == ConfigInfo.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC) {

                        Message msg = Message.obtain();
                        msg.what = HASTRANSLATEANDTRANSLITERATIONLRC;
                        msg.obj = ConfigInfo.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC;
                        mExtraLrcTypeHandler.sendMessage(msg);
                        mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC);
                    } else if (mConfigInfo.getExtraLrcStatus() == ConfigInfo.EXTRALRCSTATUS_SHOWTRANSLATELRC) {
                        Message msg = Message.obtain();
                        msg.what = HASTRANSLATEANDTRANSLITERATIONLRC;
                        msg.obj = ConfigInfo.EXTRALRCSTATUS_SHOWTRANSLATELRC;
                        mExtraLrcTypeHandler.sendMessage(msg);
                        mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC);

                    } else {
                        Message msg = Message.obtain();
                        msg.what = HASTRANSLATEANDTRANSLITERATIONLRC;
                        msg.obj = ConfigInfo.EXTRALRCSTATUS_NOSHOWEXTRALRC;
                        mExtraLrcTypeHandler.sendMessage(msg);
                        mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_NOSHOWEXTRALRC);

                    }
                }
            }
        });

        AniUtil.startAnimation(mAniLoading);
    }

    private void registerReceiver() {
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

        //注册分钟变化广播
        IntentFilter mTimeFilter = new IntentFilter();
        mTimeFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(mTimeReceiver, mTimeFilter);
    }


    @Override
    protected void handleUIMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_WHAT_UPDATE_TIME:
                setDate();
                break;
            case MESSAGE_WHAT_LOAD_DATA:

                AudioInfo audioInfo = AudioPlayerManager.getInstance(mContext).getCurSong(mConfigInfo.getPlayHash());
                Intent intent = new Intent();
                if (audioInfo != null) {

                    Bundle bundle = new Bundle();
                    bundle.putParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY, audioInfo);
                    intent.putExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY, bundle);
                    handleAudioBroadcastReceiver(intent, AudioBroadcastReceiver.ACTION_CODE_INIT);

                    int playStatus = AudioPlayerManager.getInstance(mContext).getPlayStatus();
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
        switch (msg.what) {

        }
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


                mPlayImageView.setVisibility(View.VISIBLE);
                mPauseImageView.setVisibility(View.INVISIBLE);

                mPlayOrPauseButton.setPlayingProgress(0);
                mPlayOrPauseButton.setMaxProgress(0);
                mPlayOrPauseButton.invalidate();

                //
                mManyLineLyricsView.initLrcData();

                //歌手写真
                mSingerImageView.setVisibility(View.INVISIBLE);
                mSingerImageView.resetData();

                break;
            case AudioBroadcastReceiver.ACTION_CODE_INIT:
                Bundle initBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                AudioInfo initAudioInfo = initBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                if (initAudioInfo != null) {
                    mSongNameTextView.setText(initAudioInfo.getSongName());
                    mSingerNameTextView.setText(initAudioInfo.getSingerName());

                    mPlayImageView.setVisibility(View.VISIBLE);
                    mPauseImageView.setVisibility(View.INVISIBLE);

                    mPlayOrPauseButton.setMaxProgress((int) initAudioInfo
                            .getDuration());
                    mPlayOrPauseButton.setPlayingProgress((int) initAudioInfo.getPlayProgress());
                    mPlayOrPauseButton.invalidate();


                    LyricsReader oldLyricsReader = mManyLineLyricsView.getLyricsReader();
                    if (oldLyricsReader == null || !oldLyricsReader.getHash().equals(initAudioInfo.getHash())) {
                        //加载歌词
                        String keyWords = initAudioInfo.getTitle();
                        LyricsManager.getInstance(mContext).loadLyrics(keyWords, keyWords, initAudioInfo.getDuration() + "", initAudioInfo.getHash(), mConfigInfo.isWifi(), new AsyncHandlerTask(mUIHandler, mWorkerHandler), null);
                        //加载中
                        mManyLineLyricsView.initLrcData();
                        mManyLineLyricsView.setLrcStatus(AbstractLrcView.LRCSTATUS_LOADING);
                    }

                    //加载歌手写真图片

                    ImageUtil.loadSingerImage(mContext, mSingerImageView, initAudioInfo.getSingerName(), mConfigInfo.isWifi(), new AsyncHandlerTask(mUIHandler, mWorkerHandler));

                }

                break;
            case AudioBroadcastReceiver.ACTION_CODE_PLAY:

                if (mPauseImageView.getVisibility() != View.VISIBLE) {
                    mPauseImageView.setVisibility(View.VISIBLE);
                }
                if (mPlayImageView.getVisibility() != View.INVISIBLE) {
                    mPlayImageView.setVisibility(View.INVISIBLE);
                }

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

                    mPlayOrPauseButton.setPlayingProgress((int) playingAudioInfo.getPlayProgress());
                    mPlayOrPauseButton.invalidate();

                    if (mManyLineLyricsView.getLyricsReader() != null && mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mManyLineLyricsView.getLrcPlayerStatus() != AbstractLrcView.LRCPLAYERSTATUS_PLAY && mManyLineLyricsView.getLyricsReader().getHash().equals(playingAudioInfo.getHash())) {
                        mManyLineLyricsView.play((int) playingAudioInfo.getPlayProgress());
                    }

                }

                break;
            case AudioBroadcastReceiver.ACTION_CODE_STOP:
                //暂停完成
                mPauseImageView.setVisibility(View.INVISIBLE);
                mPlayImageView.setVisibility(View.VISIBLE);

                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.pause();
                }

                break;

            case AudioBroadcastReceiver.ACTION_CODE_LRCLOADED:
                //歌词加载完成
                Bundle lrcloadedBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                String lrcHash = lrcloadedBundle.getString(AudioBroadcastReceiver.ACTION_DATA_KEY);
                AudioInfo curAudioInfo = AudioPlayerManager.getInstance(mContext).getCurSong(mConfigInfo.getPlayHash());
                if (curAudioInfo != null && lrcHash.equals(curAudioInfo.getHash())) {
                    LyricsReader oldLyricsReader = mManyLineLyricsView.getLyricsReader();
                    LyricsReader newLyricsReader = LyricsManager.getInstance(mContext).getLyricsReader(lrcHash);
                    if (oldLyricsReader != null && newLyricsReader != null && oldLyricsReader.getHash().equals(newLyricsReader.getHash())) {

                    } else {
                        mManyLineLyricsView.setLyricsReader(newLyricsReader);
                    }

                    if (oldLyricsReader != null || newLyricsReader != null) {
                        if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                            mManyLineLyricsView.seekto((int) curAudioInfo.getPlayProgress());
                        }
                    }
                }
                break;

        }
    }

    /**
     * 设置日期
     */
    private void setDate() {

        String str = "";
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");

        Calendar lastDate = Calendar.getInstance();
        str = sdfDate.format(lastDate.getTime());
        mDateTextView.setText(str);
        str = sdfTime.format(lastDate.getTime());
        mTimeTextView.setText(str);

        String mWay = String.valueOf(lastDate.get(Calendar.DAY_OF_WEEK));
        if ("1".equals(mWay)) {
            mWay = "日";
        } else if ("2".equals(mWay)) {
            mWay = "一";
        } else if ("3".equals(mWay)) {
            mWay = "二";
        } else if ("4".equals(mWay)) {
            mWay = "三";
        } else if ("5".equals(mWay)) {
            mWay = "四";
        } else if ("6".equals(mWay)) {
            mWay = "五";
        } else if ("7".equals(mWay)) {
            mWay = "六";
        }
        mDayTextView.setText("星期" + mWay);

    }

    @Override
    public void finish() {
        AniUtil.stopAnimation(mAniLoading);
        LyricsManager.getInstance(mContext).release();
        mManyLineLyricsView.release();
        mSingerImageView.release();

        if (mTimeReceiver != null) {
            //注销分钟变化广播
            unregisterReceiver(mTimeReceiver);

        }

        if (mAudioBroadcastReceiver != null) {
            mAudioBroadcastReceiver.unregisterReceiver(mContext);
        }
        super.finish();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) { // 屏蔽按键
        if (keyCode == KeyEvent.KEYCODE_BACK
                || keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
