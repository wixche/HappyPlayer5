package com.zlm.hp.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import com.zlm.hp.async.AsyncHandlerTask;
import com.zlm.hp.audio.utils.MediaUtil;
import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.db.util.DownloadThreadInfoDB;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.lyrics.LyricsReader;
import com.zlm.hp.lyrics.model.LyricsInfo;
import com.zlm.hp.lyrics.model.LyricsTag;
import com.zlm.hp.lyrics.utils.LyricsIOUtils;
import com.zlm.hp.lyrics.widget.AbstractLrcView;
import com.zlm.hp.lyrics.widget.ManyLyricsView;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.manager.LyricsManager;
import com.zlm.hp.manager.OnLineAudioManager;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.util.ColorUtil;
import com.zlm.hp.util.ImageUtil;
import com.zlm.hp.util.ToastUtil;
import com.zlm.hp.widget.ButtonRelativeLayout;
import com.zlm.hp.widget.IconfontImageButtonTextView;
import com.zlm.hp.widget.PlayListBGRelativeLayout;
import com.zlm.hp.widget.TransitionImageView;
import com.zlm.libs.widget.CustomSeekBar;
import com.zlm.libs.widget.MusicSeekBar;
import com.zlm.libs.widget.RotateLayout;

import java.util.Map;

/**
 * @Description: 歌词界面
 * @author: zhangliangming
 * @date: 2018-10-16 19:43
 **/
public class LrcActivity extends BaseActivity {

    /**
     *
     */
    public static final int RESULT_SINGER_RELOAD = 1000;

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
     * 歌手写真图片
     */
    private TransitionImageView mSingerImageView;

    /**
     * 更多按钮
     */
    private boolean isMoreMenuPopShowing = false;
    private ViewStub mViewStubMoreMenu;
    private RelativeLayout mMoreMenuPopLayout;
    private PlayListBGRelativeLayout mMoreMenuPopRL;


    /**
     * 歌曲详情
     */
    private boolean isSongInfoPopShowing = false;
    private ViewStub mViewStubSongInfo;
    private RelativeLayout mSongInfoPopLayout;
    private PlayListBGRelativeLayout mSongInfoPopRL;


    /**
     * 歌手列表
     */
    private boolean isSingerListPopShowing = false;
    private ViewStub mViewStubSingerList;
    private RelativeLayout mSingerListPopLayout;
    private PlayListBGRelativeLayout mSingerListPopRL;
    private RecyclerView mSingerListRecyclerView;

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

        AudioInfo audioInfo = AudioPlayerManager.newInstance(mContext).getCurSong(mConfigInfo.getPlayHash());

        switch (msg.what) {
            case LOAD_DATA:

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

        //歌手写真
        mSingerImageView = findViewById(R.id.singerimg);
        mSingerImageView.setVisibility(View.INVISIBLE);

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

        //播放列表
        RelativeLayout playListMenu = findViewById(R.id.playlistmenu);
        playListMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

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
    }

    /**
     * 显示更多菜单按钮
     */
    private void showMoreMenuView() {
        if (isMoreMenuPopShowing) return;

        mMoreMenuPopLayout.setVisibility(View.VISIBLE);

        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, mMoreMenuPopRL.getHeight(), 0);
        translateAnimation.setDuration(250);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isMoreMenuPopShowing = true;
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

        //歌手
        ImageView singerImgV = findViewById(R.id.search_singer_pic);
        singerImgV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AudioInfo audioInfo = AudioPlayerManager.newInstance(mContext).getCurSong(mConfigInfo.getPlayHash());
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
                                    showSingerListView(singerNameArray);
                                }
                            });

                        } else {
                            showSingerListView(singerNameArray);
                        }

                    } else {

                        showSearchSingerView(singerName);

                    }
                }
            }
        });

        //歌曲详情
        ImageView songinfoImgV = findViewById(R.id.songinfo);
        songinfoImgV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AudioInfo audioInfo = AudioPlayerManager.newInstance(mContext).getCurSong(mConfigInfo.getPlayHash());
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
    private void showSingerListView(String[] singerNameArray) {
        if (isSingerListPopShowing) return;

        LrcPopSingerAdapter adapter = new LrcPopSingerAdapter(mContext, singerNameArray, mUIHandler, mWorkerHandler, new PopSingerListener() {
            @Override
            public void search(String singerName) {
                hideSingerListView();
                showSearchSingerView(singerName);
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
                isSingerListPopShowing = true;
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
    private void showSearchSingerView(String singerName) {
        Intent intent = new Intent(LrcActivity.this, SearchSingerActivity.class);
        intent.putExtra("singerName", singerName);
        startActivityForResult(intent, RESULT_SINGER_RELOAD);
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
                isSingerListPopShowing = false;
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
                isSongInfoPopShowing = false;
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
        if (isSongInfoPopShowing) return;

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
                isSongInfoPopShowing = true;
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
                isMoreMenuPopShowing = false;
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


                    LyricsReader oldLyricsReader = mManyLineLyricsView.getLyricsReader();
                    if (oldLyricsReader == null || !oldLyricsReader.getHash().equals(initAudioInfo.getHash())) {
                        //加载歌词
                        String keyWords = "";
                        if (initAudioInfo.getSingerName().equals(getString(R.string.unknow))) {
                            keyWords = initAudioInfo.getSongName();
                        } else {
                            keyWords = initAudioInfo.getTitle();
                        }
                        LyricsManager.newInstance(mContext).loadLyrics(keyWords, keyWords, initAudioInfo.getDuration() + "", initAudioInfo.getHash(), mConfigInfo.isWifi(), new AsyncHandlerTask(mUIHandler, mWorkerHandler), null);
                        //加载中
                        mManyLineLyricsView.initLrcData();
                        mManyLineLyricsView.setLrcStatus(AbstractLrcView.LRCSTATUS_LOADING);
                    }


                    //加载歌手写真图片

                    ImageUtil.loadSingerImage(mContext, mSingerImageView, initAudioInfo.getSingerName(), mConfigInfo.isWifi(), new AsyncHandlerTask(mUIHandler, mWorkerHandler));

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
                    LyricsReader oldLyricsReader = mManyLineLyricsView.getLyricsReader();
                    LyricsReader newLyricsReader = LyricsManager.newInstance(mContext).getLyricsReader(lrcHash);
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
        mConfigInfo.setPlayModel(playMode).save();
    }

    @Override
    public void onBackPressed() {
        if (isMoreMenuPopShowing) {
            hideMoreMenuView();
            return;
        }

        if (isSongInfoPopShowing) {
            hideSongInfoView();
            return;
        }

        if (isSingerListPopShowing) {
            hideSingerListView();
            return;
        }

        mRotateLayout.closeView();
    }

    @Override
    protected void onDestroy() {
        mSingerImageView.release();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_SINGER_RELOAD) {
            mUIHandler.sendEmptyMessage(MESSAGE_CODE_SINGER_RELOAD);
        }
    }

    public interface PopSingerListener {
        public void search(String singerName);
    }
}
