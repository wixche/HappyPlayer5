package com.zlm.hp.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlm.down.entity.DownloadTask;
import com.zlm.hp.adapter.LrcPopSingerAdapter;
import com.zlm.hp.adapter.PopPlayListAdapter;
import com.zlm.hp.async.AsyncHandlerTask;
import com.zlm.hp.audio.utils.MediaUtil;
import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.db.util.AudioInfoDB;
import com.zlm.hp.db.util.DownloadThreadInfoDB;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.entity.tool.MakeInfo;
import com.zlm.hp.lyrics.LyricsReader;
import com.zlm.hp.lyrics.model.LyricsInfo;
import com.zlm.hp.lyrics.model.LyricsTag;
import com.zlm.hp.lyrics.utils.FileUtils;
import com.zlm.hp.lyrics.utils.LyricsIOUtils;
import com.zlm.hp.lyrics.utils.LyricsUtils;
import com.zlm.hp.lyrics.widget.AbstractLrcView;
import com.zlm.hp.lyrics.widget.ManyLyricsView;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.manager.DownloadAudioManager;
import com.zlm.hp.manager.LyricsManager;
import com.zlm.hp.manager.OnLineAudioManager;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.util.ColorUtil;
import com.zlm.hp.util.ImageUtil;
import com.zlm.hp.util.ResourceUtil;
import com.zlm.hp.util.ToastUtil;
import com.zlm.hp.widget.ButtonRelativeLayout;
import com.zlm.hp.widget.IconfontImageButtonTextView;
import com.zlm.hp.widget.IconfontTextView;
import com.zlm.hp.widget.ListItemRelativeLayout;
import com.zlm.hp.widget.PlayListBGRelativeLayout;
import com.zlm.hp.widget.TransitionImageView;
import com.zlm.libs.widget.CustomSeekBar;
import com.zlm.libs.widget.MusicSeekBar;
import com.zlm.libs.widget.RotateLayout;

import java.io.File;
import java.util.List;
import java.util.Map;

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
    private ImageView mModeAllImg;
    private ImageView mModeRandomImg;
    private ImageView mModeSingleImg;

    /**
     * 歌手写真图片
     */
    private TransitionImageView mSingerImageView;

    /**
     * 更多按钮
     */
    private boolean mIsMoreMenuPopShowing = false;
    private ViewStub mViewStubMoreMenu;
    private RelativeLayout mMoreMenuPopLayout;
    private PlayListBGRelativeLayout mMoreMenuPopRL;


    /**
     * 歌曲详情
     */
    private boolean mIsSongInfoPopShowing = false;
    private ViewStub mViewStubSongInfo;
    private RelativeLayout mSongInfoPopLayout;
    private PlayListBGRelativeLayout mSongInfoPopRL;


    /**
     * 歌手列表
     */
    private boolean mIsSingerListPopShowing = false;
    private ViewStub mViewStubSingerList;
    private RelativeLayout mSingerListPopLayout;
    private PlayListBGRelativeLayout mSingerListPopRL;
    private RecyclerView mSingerListRecyclerView;

    ///////////////////////////////歌曲列表弹出窗口布局/////////////////////////////////////////
    private boolean mIsShowPopPlayList = false;
    /**
     * 播放列表全屏界面
     */
    private RelativeLayout mPopPlayListRL;

    /**
     * 播放列表内容界面
     */
    private RelativeLayout mPopPlayContentRL;

    /**
     *
     */
    private RecyclerView mPlayListRListView;
    private PopPlayListAdapter mAdapter;

    /**
     * 当前播放列表歌曲总数
     */
    private TextView mPopListSizeTv;

    //播放模式
    private IconfontTextView mModeAllTv;
    private IconfontTextView mModeRandomTv;
    private IconfontTextView mModeSingleTv;

    /**
     * 喜欢
     */
    private IconfontImageButtonTextView mLikeMenuBtn;
    private IconfontImageButtonTextView mUnLikeMenuBtn;

    //mv
    private IconfontImageButtonTextView mMvMenuBtn;

    //下载
    private ImageView mDownloadImg;
    private ImageView mDownloadedImg;

    ////////////////////////////////////////////////////////////////////////////


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

    /**
     * 歌手写真重新加载
     */
    private final int MESSAGE_CODE_SINGER_RELOAD = 1;

    @Override
    protected int setContentLayoutResID() {
        return R.layout.activity_lrc;
    }

    @Override
    protected void preInitStatusBar() {
        setStatusBarViewBG(Color.TRANSPARENT);
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

        AudioInfo audioInfo = AudioPlayerManager.getInstance(mContext).getCurSong(mConfigInfo.getPlayHash());

        switch (msg.what) {
            case LOAD_DATA:

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

            case MESSAGE_CODE_SINGER_RELOAD:

                if (audioInfo != null) {
                    ImageUtil.release();

                    mSingerImageView.setTag(null);
                    //加载歌手写真图片
                    ImageUtil.loadSingerImage(mContext, mSingerImageView, audioInfo.getSingerName(), mConfigInfo.isWifi(), new AsyncHandlerTask(mUIHandler, mWorkerHandler));
                }

                break;
        }
    }

    @Override
    protected void handleWorkerMessage(Message msg) {
        switch (msg.what) {

        }
    }


    private void initView() {
        mRotateLayout = findViewById(R.id.rotateLayout);
        mRotateLayout.setDragType(RotateLayout.LEFT_TO_RIGHT);
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
        final ImageView backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRotateLayout.closeView();
            }
        });
        //
        mSongNameTextView = findViewById(R.id.songName);
        mSingerNameTextView = findViewById(R.id.singerName);

        //歌手写真
        mSingerImageView = findViewById(R.id.singerimg);
        mSingerImageView.setVisibility(View.INVISIBLE);

        //
        mManyLineLyricsView = findViewById(R.id.manyLineLyricsView);
        mManyLineLyricsView.setPaintColor(new int[]{ColorUtil.parserColor("#ffffff"), ColorUtil.parserColor("#ffffff")});
        mManyLineLyricsView.setOnLrcClickListener(new ManyLyricsView.OnLrcClickListener() {
            @Override
            public void onLrcPlayClicked(int progress) {
                if (isFinishing()) {
                    return;
                }
                //
                AudioInfo audioInfo = AudioPlayerManager.getInstance(mContext).getCurSong(mConfigInfo.getPlayHash());
                if (audioInfo != null && progress <= audioInfo.getDuration()) {
                    audioInfo.setPlayProgress(progress);
                    AudioPlayerManager.getInstance(mContext).seekto(audioInfo);
                }
            }
        });

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
            }
        });

        //
        mManyLineLyricsView.setExtraLyricsListener(new AbstractLrcView.ExtraLyricsListener() {
            @Override
            public void extraLrcCallback() {
                if (mManyLineLyricsView.getLyricsReader() == null) {
                    return;
                }
                changeLrcTypeIcon();
            }
        });


        mSongProgressTv = findViewById(R.id.songProgress);
        mSongDurationTv = findViewById(R.id.songDuration);

        //进度条
        mMusicSeekBar = findViewById(R.id.lrcseekbar);
        mMusicSeekBar.setTrackingTouchSleepTime(200);
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
                int playStatus = AudioPlayerManager.getInstance(mContext).getPlayStatus();
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
                AudioInfo audioInfo = AudioPlayerManager.getInstance(mContext).getCurSong(mConfigInfo.getPlayHash());
                if (audioInfo != null && progress <= audioInfo.getDuration()) {
                    audioInfo.setPlayProgress(progress);
                    AudioPlayerManager.getInstance(mContext).seekto(audioInfo);
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
                AudioPlayerManager.getInstance(mContext).play(mMusicSeekBar.getProgress());
            }
        });
        //暂停
        mPauseBtn = findViewById(R.id.pausebtn);
        mPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioPlayerManager.getInstance(mContext).pause();
            }
        });

        //下一首
        mNextBtn = findViewById(R.id.nextbtn);
        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioPlayerManager.getInstance(mContext).next();
            }
        });

        //上一首
        mPreBtn = findViewById(R.id.prebtn);
        mPreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioPlayerManager.getInstance(mContext).pre();
            }
        });

        /////////播放模式//////////////
        //顺序播放
        mModeAllImg = mLrcPlaybarLinearLayout.findViewById(R.id.modeAllImg);
        mModeRandomImg = mLrcPlaybarLinearLayout.findViewById(R.id.modeRandomImg);
        mModeSingleImg = mLrcPlaybarLinearLayout.findViewById(R.id.modeSingleImg);


        mModeAllImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPlayModeView(1, mModeAllImg, mModeRandomImg, mModeSingleImg, true);
            }
        });

        mModeRandomImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPlayModeView(3, mModeAllImg, mModeRandomImg, mModeSingleImg, true);
            }
        });

        mModeSingleImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPlayModeView(0, mModeAllImg, mModeRandomImg, mModeSingleImg, true);
            }
        });
        initPlayModeView(mConfigInfo.getPlayModel(), mModeAllImg, mModeRandomImg, mModeSingleImg, false);

        //更多菜单
        IconfontImageButtonTextView moreMenuIIBTV = findViewById(R.id.more_menu);
        moreMenuIIBTV.setConvert(true);
        moreMenuIIBTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mViewStubMoreMenu == null) {
                    initMoreMenuView();
                }
                /**
                 * 如果该界面还没初始化，则监听
                 */
                if (mMoreMenuPopLayout.getHeight() == 0) {
                    mMoreMenuPopLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            mMoreMenuPopLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            showMoreMenuView();
                        }
                    });

                } else {
                    showMoreMenuView();
                }
            }
        });

        //播放列表
        RelativeLayout playlistMenuRL = findViewById(R.id.playlistmenu);
        playlistMenuRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsShowPopPlayList) {
                    hidePopPlayListView();
                } else {
                    if (mPopPlayListRL == null) {
                        initPopPlayListViews();
                    }
                    showPopPlayListView();
                }
            }
        });

        //mv
        mMvMenuBtn = findViewById(R.id.mv_menu);
        mMvMenuBtn.setConvert(true);
        mMvMenuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioInfo audioInfo = AudioPlayerManager.getInstance(mContext).getCurSong(mConfigInfo.getPlayHash());
                if (audioInfo != null) {
                    //如果当前歌曲正在播放，则停止播放
                    if (AudioPlayerManager.getInstance(mContext).getPlayStatus() == AudioPlayerManager.PLAYING) {
                        AudioPlayerManager.getInstance(mContext).pause();
                    }

                    //打开mv搜索界面
                    Intent intent = new Intent(LrcActivity.this, SearchMVActivity.class);
                    intent.putExtra(SearchMVActivity.DATA_KEY, audioInfo);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            }
        });

        //喜欢
        mLikeMenuBtn = findViewById(R.id.liked_menu);
        mLikeMenuBtn.setConvert(true);
        mLikeMenuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioInfo audioInfo = AudioPlayerManager.getInstance(mContext).getCurSong(mConfigInfo.getPlayHash());
                if (audioInfo != null) {
                    if (AudioInfoDB.isLikeAudioExists(mContext, audioInfo.getHash())) {
                        boolean result = AudioInfoDB.deleteLikeAudio(mContext, audioInfo.getHash(), true);
                        if (result) {
                            mUnLikeMenuBtn.setVisibility(View.VISIBLE);
                            mLikeMenuBtn.setVisibility(View.GONE);
                            ToastUtil.showTextToast(mContext, getString(R.string.unlike_tip_text));
                        }
                    }
                }
            }
        });

        //不喜欢
        mUnLikeMenuBtn = findViewById(R.id.unlike_menu);
        mUnLikeMenuBtn.setConvert(true);
        mUnLikeMenuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioInfo audioInfo = AudioPlayerManager.getInstance(mContext).getCurSong(mConfigInfo.getPlayHash());
                if (audioInfo != null) {
                    if (!AudioInfoDB.isLikeAudioExists(mContext, audioInfo.getHash())) {
                        boolean result = AudioInfoDB.addLikeAudio(mContext, audioInfo, true);
                        if (result) {
                            mUnLikeMenuBtn.setVisibility(View.GONE);
                            mLikeMenuBtn.setVisibility(View.VISIBLE);
                            ToastUtil.showTextToast(mContext, getString(R.string.like_tip_text));
                        }
                    }
                }
            }
        });

        //未下载
        mDownloadImg = findViewById(R.id.download_img);
        mDownloadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioInfo audioInfo = AudioPlayerManager.getInstance(mContext).getCurSong(mConfigInfo.getPlayHash());
                if (audioInfo != null) {
                    boolean flag = DownloadAudioManager.getInstance(mContext).isDownloadAudioExists(audioInfo.getHash());
                    if (flag) {
                        ToastUtil.showTextToast(mContext, mContext.getResources().getString(R.string.undownload_tip_text));
                    } else {
                        ToastUtil.showTextToast(mContext, mContext.getResources().getString(R.string.download_tip_text));
                        DownloadAudioManager.getInstance(mContext).addTask(audioInfo);
                    }
                }
            }
        });

        mDownloadedImg = findViewById(R.id.downloaded_img);
    }

    /**
     * 刷新下载视图
     */
    private void reshDownloadView(AudioInfo audioInfo) {
        if (audioInfo != null && (audioInfo.getType() == AudioInfo.TYPE_LOCAL || AudioInfoDB.isDownloadedAudioExists(mContext, audioInfo.getHash()))) {
            mDownloadImg.setVisibility(View.INVISIBLE);
            mDownloadedImg.setVisibility(View.VISIBLE);
        } else {
            mDownloadImg.setVisibility(View.VISIBLE);
            mDownloadedImg.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 改变歌词类型图标
     */
    private void changeLrcTypeIcon() {
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


    /**
     * 初始化歌曲列表弹出窗口视图
     */
    private void initPopPlayListViews() {
        ViewStub stub = findViewById(R.id.viewstub_main_pop);
        stub.inflate();

        mPlayListRListView = findViewById(R.id.curplaylist_recyclerView);
        //初始化内容视图
        mPlayListRListView.setLayoutManager(new LinearLayoutManager(mContext));

        //全屏视图
        mPopPlayListRL = findViewById(R.id.list_pop);
        mPopPlayListRL.setVisibility(View.INVISIBLE);
        mPopPlayListRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hidePopPlayListView();
            }
        });

        //
        ListItemRelativeLayout cancelLL = findViewById(R.id.poplistcancel);
        cancelLL.setVisibility(View.VISIBLE);
        cancelLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hidePopPlayListView();
            }
        });

        //内容布局
        mPopPlayContentRL = findViewById(R.id.pop_content);
        mPopListSizeTv = findViewById(R.id.list_size);
        //播放模式
        mModeAllTv = mPopPlayListRL.findViewById(R.id.modeAll);
        mModeRandomTv = mPopPlayListRL.findViewById(R.id.modeRandom);
        mModeSingleTv = mPopPlayListRL.findViewById(R.id.modeSingle);

        mModeAllTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPlayModeView(1, mModeAllImg, mModeRandomImg, mModeSingleImg, false);
                initPlayModeView(1, mModeAllTv, mModeRandomTv, mModeSingleTv, true);
            }
        });

        mModeRandomTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPlayModeView(3, mModeAllImg, mModeRandomImg, mModeSingleImg, false);
                initPlayModeView(3, mModeAllTv, mModeRandomTv, mModeSingleTv, true);
            }
        });

        mModeSingleTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPlayModeView(0, mModeAllImg, mModeRandomImg, mModeSingleImg, false);
                initPlayModeView(0, mModeAllTv, mModeRandomTv, mModeSingleTv, true);
            }
        });
    }

    /**
     * 显示歌曲列表弹出窗口
     */
    private void showPopPlayListView() {
        //设置当前播放模式
        initPlayModeView(mConfigInfo.getPlayModel(), mModeAllTv, mModeRandomTv, mModeSingleTv, false);
        //设置当前歌曲数据
        List<AudioInfo> audioInfoList = mConfigInfo.getAudioInfos();
        mPopListSizeTv.setText(audioInfoList.size() + "");
        mAdapter = new PopPlayListAdapter(mContext, audioInfoList, mUIHandler, mWorkerHandler);
        mPlayListRListView.setAdapter(mAdapter);

        //定位
        int position = AudioPlayerManager.getInstance(mContext).getCurSongIndex(mConfigInfo.getAudioInfos(), mConfigInfo.getPlayHash());
        if (position != -1) {
            ((LinearLayoutManager) mPlayListRListView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
        }

        /**
         * 如果该界面还没初始化，则监听
         */
        if (mPopPlayContentRL.getHeight() == 0) {
            mPopPlayContentRL.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mPopPlayContentRL.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    showPopPlayListViewAnimation();
                }
            });

        } else {
            showPopPlayListViewAnimation();
        }
    }

    /**
     * 显示动画
     */
    private void showPopPlayListViewAnimation() {
        mPopPlayListRL.setVisibility(View.VISIBLE);
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, mPopPlayContentRL.getHeight(), 0);
        translateAnimation.setDuration(250);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mIsShowPopPlayList = true;

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mPopPlayListRL.setBackgroundColor(ColorUtil.parserColor(Color.BLACK, 120));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mPopPlayContentRL.clearAnimation();
        mPopPlayContentRL.startAnimation(translateAnimation);
    }

    /**
     * 隐藏歌曲列表弹出窗口
     */
    private void hidePopPlayListView() {
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, mPopPlayContentRL.getHeight());
        translateAnimation.setDuration(250);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIsShowPopPlayList = false;
                mPopPlayListRL.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mPopPlayContentRL.clearAnimation();
        mPopPlayContentRL.startAnimation(translateAnimation);
    }

    /**
     * 显示更多菜单按钮
     */
    private void showMoreMenuView() {
        if (mIsMoreMenuPopShowing) return;

        mMoreMenuPopLayout.setVisibility(View.VISIBLE);

        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, mMoreMenuPopRL.getHeight(), 0);
        translateAnimation.setDuration(250);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mIsMoreMenuPopShowing = true;
                mRotateLayout.setDragType(RotateLayout.NONE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mMoreMenuPopRL.clearAnimation();
        mMoreMenuPopRL.startAnimation(translateAnimation);

    }

    /**
     * 初始化更多菜单
     */
    private void initMoreMenuView() {
        mViewStubMoreMenu = findViewById(R.id.vs_more_menu);
        mViewStubMoreMenu.inflate();

        //制作歌词
        ImageView makeLrcImg = findViewById(R.id.makelrc);
        makeLrcImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioInfo audioInfo = AudioPlayerManager.getInstance(mContext).getCurSong(mConfigInfo.getPlayHash());
                if (audioInfo != null) {

                    hideMoreMenuView();

                    //如果当前歌曲正在播放，则停止播放
                    if (AudioPlayerManager.getInstance(mContext).getPlayStatus() == AudioPlayerManager.PLAYING) {
                        AudioPlayerManager.getInstance(mContext).pause();
                    }

                    //获取制作歌词所需的音频信息
                    MakeInfo makeInfo = new MakeInfo();
                    AudioInfo temp = new AudioInfo();
                    temp.setHash(audioInfo.getHash());
                    if (temp.getType() == AudioInfo.TYPE_LOCAL) {
                        temp.setFilePath(audioInfo.getFilePath());
                    } else {
                        String taskTempPath = ResourceUtil.getFilePath(mContext, ResourceConstants.PATH_CACHE_AUDIO, audioInfo.getHash() + ".temp");
                        temp.setFilePath(taskTempPath);
                    }
                    makeInfo.setAudioInfo(temp);
                    //默认歌词路径
                    String fileName = audioInfo.getTitle();
                    File lrcFile = LyricsUtils.getLrcFile(fileName, ResourceUtil.getFilePath(mContext, ResourceConstants.PATH_LYRICS, null));
                    if (lrcFile != null && lrcFile.exists()) {
                        makeInfo.setLrcFilePath(lrcFile.getPath());
                        //保存歌词路径
                        String saveLrcFilePath = lrcFile.getParent() + File.separator + FileUtils.removeExt(lrcFile.getName()) + ".hrc";
                        makeInfo.setSaveLrcFilePath(saveLrcFilePath);
                    }

                    //打开制作歌词设置页面
                    Intent intent = new Intent(LrcActivity.this, MakeLrcSettingActivity.class);
                    intent.putExtra(MakeInfo.DATA_KEY, makeInfo);
                    startActivity(intent);
                    overridePendingTransition(0, 0);

                } else {
                    ToastUtil.showTextToast(mContext, getString(R.string.select_song_text));
                }
            }
        });

        //搜索歌词
        ImageView lrcImgV = findViewById(R.id.search_lrc);
        lrcImgV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AudioInfo audioInfo = AudioPlayerManager.getInstance(mContext).getCurSong(mConfigInfo.getPlayHash());
                if (audioInfo != null) {

                    hideMoreMenuView();

                    //
                    if (AudioPlayerManager.getInstance(mContext).getPlayStatus() == AudioPlayerManager.PLAYING) {
                        Intent intent = new Intent(LrcActivity.this, SearchLrcActivity.class);
                        intent.putExtra(SearchLrcActivity.AUDIO_DATA_KEY, audioInfo);
                        startActivity(intent);
                        //
                        overridePendingTransition(R.anim.in_from_bottom, 0);
                    } else {
                        ToastUtil.showTextToast(mContext, getString(R.string.play_song_text));
                    }

                } else {
                    ToastUtil.showTextToast(mContext, getString(R.string.select_song_text));
                }
            }
        });

        //歌手
        ImageView singerImgV = findViewById(R.id.search_singer_pic);
        singerImgV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AudioInfo audioInfo = AudioPlayerManager.getInstance(mContext).getCurSong(mConfigInfo.getPlayHash());
                if (audioInfo != null) {

                    hideMoreMenuView();

                    String singerName = audioInfo.getSingerName();
                    //判断是否有多个歌手
                    if (singerName.contains("、")) {

                        String regex = "\\s*、\\s*";
                        final String[] singerNameArray = singerName.split(regex);

                        if (mViewStubSingerList == null) {
                            initSingerListView();
                        }
                        /**
                         * 如果该界面还没初始化，则监听
                         */
                        if (mSingerListPopRL.getHeight() == 0) {
                            mSingerListPopRL.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    mSingerListPopRL.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    showSingerListView(singerNameArray, audioInfo.getHash());
                                }
                            });

                        } else {
                            showSingerListView(singerNameArray, audioInfo.getHash());
                        }

                    } else {

                        showSearchSingerView(singerName, audioInfo.getHash());

                    }
                } else {
                    ToastUtil.showTextToast(mContext, getString(R.string.select_singer_text));
                }
            }
        });

        //歌曲详情
        ImageView songinfoImgV = findViewById(R.id.songinfo);
        songinfoImgV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AudioInfo audioInfo = AudioPlayerManager.getInstance(mContext).getCurSong(mConfigInfo.getPlayHash());
                if (audioInfo != null) {

                    hideMoreMenuView();

                    if (mViewStubSongInfo == null) {
                        initSongInfoView();
                    }
                    /**
                     * 如果该界面还没初始化，则监听
                     */
                    if (mSongInfoPopRL.getHeight() == 0) {
                        mSongInfoPopRL.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                mSongInfoPopRL.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                showSongInfoView(audioInfo);
                            }
                        });

                    } else {
                        showSongInfoView(audioInfo);
                    }
                } else {
                    ToastUtil.showTextToast(mContext, getString(R.string.select_song_text));
                }
            }
        });

        //更多菜单
        mMoreMenuPopLayout = findViewById(R.id.moreMenuPopLayout);
        mMoreMenuPopLayout.setVisibility(View.INVISIBLE);
        mMoreMenuPopLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideMoreMenuView();
            }
        });

        mMoreMenuPopRL = findViewById(R.id.menuLayout);

        //字体
        final CustomSeekBar fontSizeSB = findViewById(R.id.fontSizeSeekbar);
        fontSizeSB.setMax(ConfigInfo.MAX_LRC_FONT_SIZE - ConfigInfo.MIN_LRC_FONT_SIZE);
        fontSizeSB.setProgress((mConfigInfo.getLrcFontSize() - ConfigInfo.MIN_LRC_FONT_SIZE));
        fontSizeSB.setBackgroundPaintColor(ColorUtil.parserColor(Color.WHITE, 50));
        fontSizeSB.setProgressColor(Color.WHITE);
        fontSizeSB.setThumbColor(Color.WHITE);
        fontSizeSB.setOnChangeListener(new CustomSeekBar.OnChangeListener() {
            @Override
            public void onProgressChanged(CustomSeekBar customSeekBar) {

                int fontSize = fontSizeSB.getProgress() + ConfigInfo.MIN_LRC_FONT_SIZE;
                mManyLineLyricsView.setSize(fontSize, fontSize, true);
                mConfigInfo.setLrcFontSize(fontSize).save();


            }

            @Override
            public void onTrackingTouchStart(CustomSeekBar customSeekBar) {

            }

            @Override
            public void onTrackingTouchFinish(CustomSeekBar customSeekBar) {

            }
        });

        //字体减少
        IconfontImageButtonTextView lyricDecreaseIIBTV = findViewById(R.id.lyric_decrease);
        lyricDecreaseIIBTV.setConvert(true);
        lyricDecreaseIIBTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int curProgress = fontSizeSB.getProgress();
                curProgress -= 2;
                if (curProgress < 0) {
                    curProgress = 0;
                }
                fontSizeSB.setProgress(curProgress);

                int fontSize = fontSizeSB.getProgress() + ConfigInfo.MIN_LRC_FONT_SIZE;
                mManyLineLyricsView.setSize(fontSize, fontSize, true);
                mConfigInfo.setLrcFontSize(fontSize).save();
            }
        });


        //字体增加
        IconfontImageButtonTextView lyricIncreaseIIBTV = findViewById(R.id.lyric_increase);
        lyricIncreaseIIBTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int curProgress = fontSizeSB.getProgress();
                curProgress += 2;
                if (curProgress > fontSizeSB.getMax()) {
                    curProgress = fontSizeSB.getMax();
                }
                fontSizeSB.setProgress(curProgress);

                int fontSize = fontSizeSB.getProgress() + ConfigInfo.MIN_LRC_FONT_SIZE;
                mManyLineLyricsView.setSize(fontSize, fontSize, true);
                mConfigInfo.setLrcFontSize(fontSize).save();
            }
        });

        //歌词颜色面板
        ImageView[] colorPanel = new ImageView[ConfigInfo.LRC_COLORS_STRING.length];
        final ImageView[] colorStatus = new ImageView[colorPanel.length];

        int i = 0;
        //
        colorPanel[i] = findViewById(R.id.color_panel1);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = mConfigInfo.getLrcColorIndex();
                if (index != 0) {
                    mConfigInfo.setLrcColorIndex(0).save();
                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[0].setVisibility(View.VISIBLE);

                    int lrcColor = ColorUtil.parserColor(ConfigInfo.LRC_COLORS_STRING[mConfigInfo.getLrcColorIndex()]);
                    mManyLineLyricsView.setPaintHLColor(new int[]{lrcColor, lrcColor}, true);
                }
            }
        });
        colorStatus[i] = findViewById(R.id.color_status1);

        //
        i++;
        colorPanel[i] = findViewById(R.id.color_panel2);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = mConfigInfo.getLrcColorIndex();
                if (index != 1) {
                    mConfigInfo.setLrcColorIndex(1).save();
                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[1].setVisibility(View.VISIBLE);


                    int lrcColor = ColorUtil.parserColor(ConfigInfo.LRC_COLORS_STRING[mConfigInfo.getLrcColorIndex()]);
                    mManyLineLyricsView.setPaintHLColor(new int[]{lrcColor, lrcColor}, true);

                }
            }
        });
        colorStatus[i] = findViewById(R.id.color_status2);

        //
        i++;
        colorPanel[i] = findViewById(R.id.color_panel3);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = mConfigInfo.getLrcColorIndex();
                if (index != 2) {
                    mConfigInfo.setLrcColorIndex(2).save();
                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[2].setVisibility(View.VISIBLE);

                    int lrcColor = ColorUtil.parserColor(ConfigInfo.LRC_COLORS_STRING[mConfigInfo.getLrcColorIndex()]);
                    mManyLineLyricsView.setPaintHLColor(new int[]{lrcColor, lrcColor}, true);
                }
            }
        });
        colorStatus[i] = findViewById(R.id.color_status3);

        //
        i++;
        colorPanel[i] = findViewById(R.id.color_panel4);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = mConfigInfo.getLrcColorIndex();
                if (index != 3) {
                    mConfigInfo.setLrcColorIndex(3).save();
                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[3].setVisibility(View.VISIBLE);

                    int lrcColor = ColorUtil.parserColor(ConfigInfo.LRC_COLORS_STRING[mConfigInfo.getLrcColorIndex()]);
                    mManyLineLyricsView.setPaintHLColor(new int[]{lrcColor, lrcColor}, true);
                }
            }
        });
        colorStatus[i] = findViewById(R.id.color_status4);

        //
        i++;
        colorPanel[i] = findViewById(R.id.color_panel5);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = mConfigInfo.getLrcColorIndex();
                if (index != 4) {
                    mConfigInfo.setLrcColorIndex(4).save();
                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[4].setVisibility(View.VISIBLE);

                    int lrcColor = ColorUtil.parserColor(ConfigInfo.LRC_COLORS_STRING[mConfigInfo.getLrcColorIndex()]);
                    mManyLineLyricsView.setPaintHLColor(new int[]{lrcColor, lrcColor}, true);
                }
            }
        });
        colorStatus[i] = findViewById(R.id.color_status5);

        //
        i++;
        colorPanel[i] = findViewById(R.id.color_panel6);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = mConfigInfo.getLrcColorIndex();
                if (index != 5) {
                    mConfigInfo.setLrcColorIndex(5).save();
                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[5].setVisibility(View.VISIBLE);

                    int lrcColor = ColorUtil.parserColor(ConfigInfo.LRC_COLORS_STRING[mConfigInfo.getLrcColorIndex()]);
                    mManyLineLyricsView.setPaintHLColor(new int[]{lrcColor, lrcColor}, true);
                }
            }
        });
        colorStatus[i] = findViewById(R.id.color_status6);

        //
        colorStatus[mConfigInfo.getLrcColorIndex()].setVisibility(View.VISIBLE);

        //取消
        LinearLayout moreMenuCancel = findViewById(R.id.more_menu_calcel);
        moreMenuCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideMoreMenuView();
            }
        });

        //歌词进度减少按钮
        ButtonRelativeLayout lrcProgressJianBtn = findViewById(R.id.lyric_progress_jian);
        lrcProgressJianBtn.setDefFillColor(ColorUtil.parserColor(Color.WHITE, 20));
        lrcProgressJianBtn.setPressedFillColor(ColorUtil.parserColor(Color.WHITE, 50));
        lrcProgressJianBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mManyLineLyricsView.getLyricsReader() != null) {
                    if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                        if (mManyLineLyricsView.getLrcPlayerStatus() == AbstractLrcView.LRCPLAYERSTATUS_PLAY) {
                            mManyLineLyricsView.getLyricsReader().setOffset(mManyLineLyricsView.getLyricsReader().getOffset() + (-500));
                            ToastUtil.showTextToast(mContext, (float) mManyLineLyricsView.getLyricsReader().getOffset() / 1000 + getString(R.string.second));

                            //保存歌词文件
                            saveLrcFile(mManyLineLyricsView.getLyricsReader().getLrcFilePath(), mManyLineLyricsView.getLyricsReader().getLyricsInfo(), mManyLineLyricsView.getLyricsReader().getPlayOffset());

                        } else {
                            ToastUtil.showTextToast(mContext, getString(R.string.seek_lrc_warntip));
                        }
                    }
                }
            }
        });
        //歌词进度重置
        ButtonRelativeLayout resetProgressJianBtn = findViewById(R.id.lyric_progress_reset);
        resetProgressJianBtn.setDefFillColor(ColorUtil.parserColor(Color.WHITE, 20));
        resetProgressJianBtn.setPressedFillColor(ColorUtil.parserColor(Color.WHITE, 50));
        resetProgressJianBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mManyLineLyricsView.getLyricsReader() != null) {

                    if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                        if (mManyLineLyricsView.getLrcPlayerStatus() == AbstractLrcView.LRCPLAYERSTATUS_PLAY) {
                            mManyLineLyricsView.getLyricsReader().setOffset(0);
                            ToastUtil.showTextToast(mContext, getString(R.string.reset));

                            //保存歌词文件
                            saveLrcFile(mManyLineLyricsView.getLyricsReader().getLrcFilePath(), mManyLineLyricsView.getLyricsReader().getLyricsInfo(), mManyLineLyricsView.getLyricsReader().getPlayOffset());

                        } else {
                            ToastUtil.showTextToast(mContext, getString(R.string.seek_lrc_warntip));
                        }

                    }
                }
            }
        });
        //歌词进度增加
        ButtonRelativeLayout lrcProgressJiaBtn = findViewById(R.id.lyric_progress_jia);
        lrcProgressJiaBtn.setDefFillColor(ColorUtil.parserColor(Color.WHITE, 20));
        lrcProgressJiaBtn.setPressedFillColor(ColorUtil.parserColor(Color.WHITE, 50));
        lrcProgressJiaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mManyLineLyricsView.getLyricsReader() != null) {

                    if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                        if (mManyLineLyricsView.getLrcPlayerStatus() == AbstractLrcView.LRCPLAYERSTATUS_PLAY) {
                            mManyLineLyricsView.getLyricsReader().setOffset(mManyLineLyricsView.getLyricsReader().getOffset() + (500));
                            ToastUtil.showTextToast(mContext, (float) mManyLineLyricsView.getLyricsReader().getOffset() / 1000 + getString(R.string.second));
                            //保存歌词文件
                            saveLrcFile(mManyLineLyricsView.getLyricsReader().getLrcFilePath(), mManyLineLyricsView.getLyricsReader().getLyricsInfo(), mManyLineLyricsView.getLyricsReader().getPlayOffset());
                        } else {
                            ToastUtil.showTextToast(mContext, getString(R.string.seek_lrc_warntip));
                        }

                    }
                }
            }

        });
    }

    /**
     * 显示歌手列表
     *
     * @param singerNameArray
     */
    private void showSingerListView(String[] singerNameArray, final String hash) {
        if (mIsSingerListPopShowing) return;

        LrcPopSingerAdapter adapter = new LrcPopSingerAdapter(mContext, singerNameArray, mUIHandler, mWorkerHandler, new PopSingerListener() {
            @Override
            public void search(String singerName) {
                hideSingerListView();
                showSearchSingerView(singerName, hash);
            }
        });
        mSingerListRecyclerView.setAdapter(adapter);

        mSingerListPopLayout.setVisibility(View.VISIBLE);

        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, mSingerListPopRL.getHeight(), 0);
        translateAnimation.setDuration(250);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mIsSingerListPopShowing = true;
                mRotateLayout.setDragType(RotateLayout.NONE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mSingerListPopRL.clearAnimation();
        mSingerListPopRL.startAnimation(translateAnimation);
    }

    /**
     * 打开歌手搜索界面
     *
     * @param singerName
     */
    private void showSearchSingerView(String singerName, String hash) {
        Intent intent = new Intent(LrcActivity.this, SearchSingerActivity.class);
        intent.putExtra("hash", hash);
        intent.putExtra("singerName", singerName);
        startActivity(intent);
        //
        overridePendingTransition(0, 0);
    }

    /**
     * 初始化歌手列表
     */
    private void initSingerListView() {
        mViewStubSingerList = findViewById(R.id.vs_singer_list);
        mViewStubSingerList.inflate();

        //歌曲详情
        mSingerListPopLayout = findViewById(R.id.singerListPopLayout);
        mSingerListPopLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSingerListView();
            }
        });

        mSingerListRecyclerView = findViewById(R.id.singerlist_recyclerView);
        mSingerListRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mSingerListPopRL = findViewById(R.id.pop_singerlist_parent);

        //
        LinearLayout cancelLL = findViewById(R.id.splcalcel);
        cancelLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSingerListView();
            }
        });
    }

    /**
     * 隐藏歌手列表
     */
    private void hideSingerListView() {
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, mSingerListPopRL.getHeight());
        translateAnimation.setDuration(250);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIsSingerListPopShowing = false;
                mSingerListPopLayout.setVisibility(View.INVISIBLE);
                mRotateLayout.setDragType(RotateLayout.LEFT_TO_RIGHT);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mSingerListPopRL.clearAnimation();
        mSingerListPopRL.startAnimation(translateAnimation);
    }

    /**
     * 初始化歌曲详情窗口
     */
    private void initSongInfoView() {
        mViewStubSongInfo = findViewById(R.id.vs_songinfo);
        mViewStubSongInfo.inflate();

        //歌曲详情
        mSongInfoPopLayout = findViewById(R.id.songinfoPopLayout);
        mSongInfoPopLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSongInfoView();
            }
        });

        mSongInfoPopRL = findViewById(R.id.pop_songinfo_parent);

        //
        LinearLayout cancelLL = findViewById(R.id.songcalcel);
        cancelLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSongInfoView();
            }
        });
    }

    /**
     * 隐藏歌曲详情窗口
     */
    private void hideSongInfoView() {
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, mSongInfoPopRL.getHeight());
        translateAnimation.setDuration(250);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIsSongInfoPopShowing = false;
                mSongInfoPopLayout.setVisibility(View.INVISIBLE);
                mRotateLayout.setDragType(RotateLayout.LEFT_TO_RIGHT);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mSongInfoPopRL.clearAnimation();
        mSongInfoPopRL.startAnimation(translateAnimation);
    }

    /**
     * 显示歌曲详情窗口
     *
     * @param audioInfo
     */
    private void showSongInfoView(AudioInfo audioInfo) {
        if (mIsSongInfoPopShowing) return;

        TextView popSingerNameTv = findViewById(R.id.pop_singerName);
        popSingerNameTv.setText(audioInfo.getSingerName());

        TextView popFileExtTv = findViewById(R.id.pop_fileext);
        popFileExtTv.setText(audioInfo.getFileExt());

        TextView popTimeTv = findViewById(R.id.pop_time);
        popTimeTv.setText(audioInfo.getDurationText());

        TextView popFileSizeTv = findViewById(R.id.pop_filesize);
        popFileSizeTv.setText(audioInfo.getFileSizeText());

        mSongInfoPopLayout.setVisibility(View.VISIBLE);

        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, mSongInfoPopRL.getHeight(), 0);
        translateAnimation.setDuration(250);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mIsSongInfoPopShowing = true;
                mRotateLayout.setDragType(RotateLayout.NONE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mSongInfoPopRL.clearAnimation();
        mSongInfoPopRL.startAnimation(translateAnimation);

    }

    /**
     * @param lrcFilePath
     * @param lyricsInfo
     * @param playOffset
     */
    private void saveLrcFile(final String lrcFilePath, final LyricsInfo lyricsInfo, final long playOffset) {
        new Thread() {

            @Override
            public void run() {

                Map<String, Object> tags = lyricsInfo.getLyricsTags();

                tags.put(LyricsTag.TAG_OFFSET, playOffset);
                lyricsInfo.setLyricsTags(tags);


                //保存修改的歌词文件
                try {
                    LyricsIOUtils.getLyricsFileWriter(lrcFilePath).writer(lyricsInfo, lrcFilePath);
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }

        }.start();
    }

    /**
     * 隐藏更多菜单
     */
    private void hideMoreMenuView() {
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, mMoreMenuPopRL.getHeight());
        translateAnimation.setDuration(250);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIsMoreMenuPopShowing = false;
                mMoreMenuPopLayout.setVisibility(View.INVISIBLE);
                mRotateLayout.setDragType(RotateLayout.LEFT_TO_RIGHT);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mMoreMenuPopRL.clearAnimation();
        mMoreMenuPopRL.startAnimation(translateAnimation);

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

                //歌手写真
                mSingerImageView.setVisibility(View.INVISIBLE);
                mSingerImageView.resetData();

                //喜欢/不喜欢
                mUnLikeMenuBtn.setVisibility(View.VISIBLE);
                mLikeMenuBtn.setVisibility(View.GONE);

                reshDownloadView(null);

                if (mAdapter != null)
                    mAdapter.reshViewHolder(null);

                break;
            case AudioBroadcastReceiver.ACTION_CODE_INIT:
                Bundle initBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                AudioInfo initAudioInfo = initBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                if (initAudioInfo != null) {
                    mSongNameTextView.setText(initAudioInfo.getSongName());
                    mSingerNameTextView.setText(initAudioInfo.getSingerName());
                    mPauseBtn.setVisibility(View.INVISIBLE);
                    mPlayBtn.setVisibility(View.VISIBLE);

                    //下载
                    reshDownloadView(initAudioInfo);

                    //喜欢/不喜欢
                    if (AudioInfoDB.isLikeAudioExists(mContext, initAudioInfo.getHash())) {
                        mUnLikeMenuBtn.setVisibility(View.GONE);
                        mLikeMenuBtn.setVisibility(View.VISIBLE);
                    } else {
                        mUnLikeMenuBtn.setVisibility(View.VISIBLE);
                        mLikeMenuBtn.setVisibility(View.GONE);
                    }

                    //
                    mSongProgressTv.setText(MediaUtil.formatTime((int) initAudioInfo.getPlayProgress()));
                    mSongDurationTv.setText(MediaUtil.formatTime((int) initAudioInfo.getDuration()));

                    //设置进度条
                    mMusicSeekBar.setEnabled(true);
                    mMusicSeekBar.setMax((int) initAudioInfo.getDuration());
                    mMusicSeekBar.setProgress((int) initAudioInfo.getPlayProgress());
                    mMusicSeekBar.setSecondaryProgress(0);


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

                    if (mAdapter != null) {
                        mAdapter.reshViewHolder(initAudioInfo.getHash());

                        if (mIsShowPopPlayList) {
                            //定位
                            int position = AudioPlayerManager.getInstance(mContext).getCurSongIndex(mConfigInfo.getAudioInfos(), mConfigInfo.getPlayHash());
                            if (position != -1) {
                                ((LinearLayoutManager) mPlayListRListView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
                            }
                        }
                    }
                } else {
                    if (mAdapter != null)
                        mAdapter.reshViewHolder(null);
                }

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

            case AudioBroadcastReceiver.ACTION_CODE_DOWNLOAD_FINISH:
            case AudioBroadcastReceiver.ACTION_CODE_DOWNLOADONEDLINESONG:
                //网络歌曲下载完成
                Bundle downloadedBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                DownloadTask downloadedTask = downloadedBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                String downloadedHash = downloadedTask.getTaskId();
                if (downloadedTask != null && !TextUtils.isEmpty(downloadedHash)) {
                    if (mIsShowPopPlayList && mAdapter != null)
                        mAdapter.reshViewHolder(downloadedHash);
                    if (code == AudioBroadcastReceiver.ACTION_CODE_DOWNLOAD_FINISH) {
                        AudioInfo downloadedAudioInfo = AudioPlayerManager.getInstance(mContext).getCurSong(downloadedHash);
                        if (downloadedHash.equals(downloadedAudioInfo.getHash())) {
                            reshDownloadView(downloadedAudioInfo);
                        }
                    }
                }

                break;

            case AudioBroadcastReceiver.ACTION_CODE_UPDATE_PLAYLIST:
                if (!mIsShowPopPlayList || mAdapter == null) {
                    return;
                }

                //设置当前歌曲数据
                List<AudioInfo> audioInfoList = mConfigInfo.getAudioInfos();
                mPopListSizeTv.setText(audioInfoList.size() + "");

                mAdapter.notifyDataSetChanged();

                break;

            case AudioBroadcastReceiver.ACTION_CODE_DOWNLOADONLINESONG:
                //网络歌曲下载中
                Bundle downloadOnlineSongBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                DownloadTask downloadingTask = downloadOnlineSongBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                String hash = mConfigInfo.getPlayHash();
                AudioInfo audioInfo = AudioPlayerManager.getInstance(mContext).getCurSong(hash);
                if (audioInfo != null && downloadingTask != null && !TextUtils.isEmpty(hash) && hash.equals(downloadingTask.getTaskId())) {
                    int downloadedSize = DownloadThreadInfoDB.getDownloadedSize(mContext, downloadingTask.getTaskId(), OnLineAudioManager.mThreadNum);
                    double pre = downloadedSize * 1.0 / audioInfo.getFileSize();
                    int downloadProgress = (int) (mMusicSeekBar.getMax() * pre);
                    mMusicSeekBar.setSecondaryProgress(downloadProgress);
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

            case AudioBroadcastReceiver.ACTION_CODE_LRCRELOADING:
                //歌词重新加载中
                AudioInfo curAudioInfoTemp1 = AudioPlayerManager.getInstance(mContext).getCurSong(mConfigInfo.getPlayHash());
                LyricsReader oldLyricsReader = mManyLineLyricsView.getLyricsReader();
                if (oldLyricsReader == null || oldLyricsReader.getHash().equals(curAudioInfoTemp1.getHash())) {
                    //加载中
                    mManyLineLyricsView.initLrcData();
                    mManyLineLyricsView.setLrcStatus(AbstractLrcView.LRCSTATUS_LOADING);
                }

                break;
            case AudioBroadcastReceiver.ACTION_CODE_LRCRELOADED:
                //歌词重新加载
                Bundle lrcreloadedBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                String reloadlrcHash = lrcreloadedBundle.getString(AudioBroadcastReceiver.ACTION_DATA_KEY);
                AudioInfo curAudioInfoTemp = AudioPlayerManager.getInstance(mContext).getCurSong(mConfigInfo.getPlayHash());
                if (curAudioInfoTemp != null && reloadlrcHash.equals(curAudioInfoTemp.getHash())) {
                    LyricsReader newLyricsReader = LyricsManager.getInstance(mContext).getLyricsReader(reloadlrcHash);
                    if (newLyricsReader != null) {
                        mManyLineLyricsView.setLyricsReader(newLyricsReader);
                    }
                    if (newLyricsReader != null) {
                        if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                            mManyLineLyricsView.seekto((int) curAudioInfoTemp.getPlayProgress());
                        }
                    }
                }

                break;

            case AudioBroadcastReceiver.ACTION_CODE_RELOADSINGERIMG:
                Bundle singerReloadBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                String singerHash = singerReloadBundle.getString(AudioBroadcastReceiver.ACTION_DATA_KEY);
                AudioInfo curSingerAudioInfo = AudioPlayerManager.getInstance(mContext).getCurSong(mConfigInfo.getPlayHash());
                if (curSingerAudioInfo != null && curSingerAudioInfo.getHash().equals(singerHash)) {
                    mUIHandler.sendEmptyMessage(MESSAGE_CODE_SINGER_RELOAD);
                }
                break;
            case AudioBroadcastReceiver.ACTION_CODE_LOCK_LRC_CHANGE:
                //锁屏歌词发生改变
                changeLrcTypeIcon();

                break;
            case AudioBroadcastReceiver.ACTION_CODE_UPDATE_LIKE:
                //喜欢/不喜欢
                Bundle likeBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                String likeHash = likeBundle.getString(AudioBroadcastReceiver.ACTION_DATA_KEY);
                String curHash = mConfigInfo.getPlayHash();
                if (!curHash.equals(likeHash)) return;

                AudioInfo curAudioInfoTemp2 = AudioPlayerManager.getInstance(mContext).getCurSong(curHash);
                if (curAudioInfoTemp2 == null) return;
                if (AudioInfoDB.isLikeAudioExists(mContext, curAudioInfoTemp2.getHash())) {
                    mUnLikeMenuBtn.setVisibility(View.GONE);
                    mLikeMenuBtn.setVisibility(View.VISIBLE);
                } else {
                    mUnLikeMenuBtn.setVisibility(View.VISIBLE);
                    mLikeMenuBtn.setVisibility(View.GONE);
                }
                break;
            case AudioBroadcastReceiver.ACTION_CODE_MAKE_SUCCESS:
                //歌词制作成功
                Bundle makeBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                String lrchash = makeBundle.getString(AudioBroadcastReceiver.ACTION_DATA_KEY);
                LyricsManager.getInstance(mContext).remove(lrchash);
                if (mConfigInfo.getPlayHash().equals(lrchash)) {

                    AudioInfo lrcAudioInfo = AudioPlayerManager.getInstance(mContext).getCurSong(lrchash);
                    //加载歌词
                    String keyWords = lrcAudioInfo.getTitle();
                    LyricsManager.getInstance(mContext).loadLyrics(keyWords, keyWords, lrcAudioInfo.getDuration() + "", lrcAudioInfo.getHash(), mConfigInfo.isWifi(), new AsyncHandlerTask(mUIHandler, mWorkerHandler), null);
                    //加载中
                    mManyLineLyricsView.initLrcData();
                    mManyLineLyricsView.setLrcStatus(AbstractLrcView.LRCSTATUS_LOADING);
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
    private void initPlayModeView(int playMode, View modeAllImg, View modeRandomImg, View modeSingleImg, boolean isTipShow) {
        if (playMode == 0) {
            if (isTipShow)
                ToastUtil.showTextToast(mContext, getString(R.string.mode_all_text));
            modeAllImg.setVisibility(View.VISIBLE);
            modeRandomImg.setVisibility(View.INVISIBLE);
            modeSingleImg.setVisibility(View.INVISIBLE);
        } else if (playMode == 1) {
            if (isTipShow)
                ToastUtil.showTextToast(mContext, getString(R.string.mode_random_text));
            modeAllImg.setVisibility(View.INVISIBLE);
            modeRandomImg.setVisibility(View.VISIBLE);
            modeSingleImg.setVisibility(View.INVISIBLE);
        } else {
            if (isTipShow)
                ToastUtil.showTextToast(mContext, getString(R.string.mode_single_text));
            modeAllImg.setVisibility(View.INVISIBLE);
            modeRandomImg.setVisibility(View.INVISIBLE);
            modeSingleImg.setVisibility(View.VISIBLE);
        }
        //
        if (isTipShow)
            mConfigInfo.setPlayModel(playMode).save();
    }

    @Override
    public void onBackPressed() {
        if (mIsMoreMenuPopShowing) {
            hideMoreMenuView();
            return;
        }

        if (mIsSongInfoPopShowing) {
            hideSongInfoView();
            return;
        }

        if (mIsSingerListPopShowing) {
            hideSingerListView();
            return;
        }

        if (mIsShowPopPlayList) {
            hidePopPlayListView();
            return;
        }

        mRotateLayout.closeView();
    }

    @Override
    public void finish() {
        if (mExtraLrcTypeHandler != null) {
            mExtraLrcTypeHandler.removeCallbacksAndMessages(null);
        }
        LyricsManager.getInstance(mContext).release();
        mSingerImageView.release();
        mManyLineLyricsView.release();
        destroyReceiver();
        super.finish();
    }

    /**
     * 销毁广播
     */
    private void destroyReceiver() {

        if (mAudioBroadcastReceiver != null) {
            mAudioBroadcastReceiver.unregisterReceiver(mContext);
        }

    }

    public interface PopSingerListener {
        public void search(String singerName);
    }
}
