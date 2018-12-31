package com.zlm.hp.fragment.tool;


import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dou361.dialogui.DialogUIUtils;
import com.dou361.dialogui.listener.DialogUIListener;
import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.fragment.BaseFragment;
import com.zlm.hp.lyrics.model.LyricsInfo;
import com.zlm.hp.lyrics.utils.TimeUtils;
import com.zlm.hp.lyrics.widget.make.MakeLyricsView;
import com.zlm.hp.ui.R;
import com.zlm.hp.ui.tool.MakeLrcActivity;
import com.zlm.hp.util.ColorUtil;
import com.zlm.hp.util.ToastUtil;
import com.zlm.hp.widget.IconfontTextView;
import com.zlm.libs.widget.MusicSeekBar;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * @Description: 制作歌词
 * @author: zhangliangming
 * @date: 2018-12-30 23:45
 **/
public class MakeLrcFragment extends BaseFragment {
    /**
     * 进度条
     */
    private MusicSeekBar mMusicSeekBar;
    /**
     * 播放器
     */
    private IjkMediaPlayer mMediaPlayer;
    /**
     * 播放按钮
     */
    private ImageView mPlayImg;
    /**
     * 暂停播放按钮
     */
    private ImageView mPauseImg;

    /**
     * 制作歌词视图
     */
    private MakeLyricsView mMakeLyricsView;

    /**
     * 回滚
     */
    private Button mBackPlayBtn;
    /**
     * 敲打
     */
    private Button mPlayBtn;

    /**
     * 是否快进
     */
    private boolean isSeekTo = false;

    /**
     * 初始化歌曲数据
     */
    private final int INITAUDIODATA = 1;
    private final int AUDIO_PLAY = 2;
    private final int AUDIO_PLAYING = 3;
    private final int AUDIO_PAUSE = 4;
    private final int AUDIO_FINISH = 5;

    /**
     * 歌曲信息
     */
    private AudioInfo mAudioInfo;

    /**
     * 事件回调
     */
    private MakeLrcActivity.MakeLrcListener mMakeLrcListener;

    public MakeLrcFragment() {

    }

    /**
     * @return
     */
    public static MakeLrcFragment newInstance() {
        MakeLrcFragment fragment = new MakeLrcFragment();
        return fragment;
    }

    @Override
    protected void isFristVisibleToUser() {

    }

    @Override
    protected int setContentLayoutResID() {
        return R.layout.fragment_make_lrc;
    }

    @Override
    protected void preInitStatusBar() {
        setTitleViewId(R.layout.layout_close_title);
        super.preInitStatusBar();
    }

    @Override
    protected void initViews(View mainView, Bundle savedInstanceState) {
        //显示标题视图
        LinearLayout titleLL = mainView.findViewById(R.id.title_view_parent);
        titleLL.setVisibility(View.VISIBLE);

        TextView titleView = mainView.findViewById(R.id.title);
        titleView.setText(getString(R.string.play_lrc_text));

        //关闭
        IconfontTextView backTextView = mainView.findViewById(R.id.closebtn);
        backTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeView();
            }
        });

        //进度条
        mMusicSeekBar = mainView.findViewById(R.id.seekBar);
        mMusicSeekBar.setOnMusicListener(new MusicSeekBar.OnMusicListener() {
            @Override
            public String getTimeText() {
                return TimeUtils.parseMMSSString(mMusicSeekBar.getProgress());
            }

            @Override
            public String getLrcText() {
                return null;
            }

            @Override
            public void onProgressChanged(MusicSeekBar musicSeekBar) {

            }

            @Override
            public void onTrackingTouchStart(MusicSeekBar musicSeekBar) {

            }

            @Override
            public void onTrackingTouchFinish(MusicSeekBar musicSeekBar) {
                if (mMediaPlayer != null) {
                    isSeekTo = true;
                    mMakeLyricsView.seekTo(mMusicSeekBar.getProgress());
                    mMediaPlayer.seekTo(mMusicSeekBar.getProgress());
                }
            }
        });
        mMusicSeekBar.setTimePopupWindowViewColor(Color.argb(200, 255, 64, 129));

        //返回编辑按钮
        Button mBackEditBtn = mainView.findViewById(R.id.backEdit);
        mBackEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tipMsg = getString(R.string.close_lrc_text);
                DialogUIUtils.showMdAlert(getActivity(), getString(R.string.tip_title), tipMsg, new DialogUIListener() {
                    @Override
                    public void onPositive() {
                        releasePlayer();

                        mMakeLyricsView.initLrcData(true);
                        if (mMakeLrcListener != null) {
                            mMakeLrcListener.openView(MakeLrcActivity.INDEX_EDITLRC);
                        }
                    }

                    @Override
                    public void onNegative() {

                    }
                }).setCancelable(true, false).show();

            }
        });


        //播放
        mPlayImg = mainView.findViewById(R.id.bar_play);
        mPlayImg.setVisibility(View.VISIBLE);
        mPlayImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayer == null) {
                    initPlayerData();
                } else if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                    mMediaPlayer.start();
                    mUIHandler.sendEmptyMessage(AUDIO_PLAY);

                }
            }
        });

        //暂停
        mPauseImg = mainView.findViewById(R.id.bar_pause);
        mPauseImg.setVisibility(View.INVISIBLE);
        mPauseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseMediaPlayer();
            }
        });

        ConfigInfo configInfo = ConfigInfo.obtain();
        mMakeLyricsView = mainView.findViewById(R.id.makelyricsview);
        mMakeLyricsView.setPaintLineColor(ColorUtil.parserColor("#888888"));
        mMakeLyricsView.setFontSize(configInfo.getLrcFontSize());
        mMakeLyricsView.setPaintColors(new int[]{ColorUtil.parserColor("#888888"), ColorUtil.parserColor("#888888")});
        mMakeLyricsView.setPaintHLColors(new int[]{ColorUtil.parserColor("#0288d1"), ColorUtil.parserColor("#0288d1")});

        //设置制作歌词回调事件
        mMakeLyricsView.setOnScrollListener(new MakeLyricsView.OnScrollListener() {
            @Override
            public int getScrollStopPlayTime() {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    return (int) mMediaPlayer.getCurrentPosition();
                }
                return 0;
            }

            @Override
            public int getCurPlayTime() {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    return (int) mMediaPlayer.getCurrentPosition();
                }
                return 0;
            }

            @Override
            public void seekTo(int seektoProgress) {
                isSeekTo = true;
                if (mMediaPlayer != null) {
                    mMediaPlayer.seekTo(seektoProgress);
                }
            }
        });
        //回滚按钮
        mBackPlayBtn = mainView.findViewById(R.id.backplayBtn);
        mBackPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mMakeLyricsView.lineLrcBack();
                }
            }
        });

        //敲打按钮
        mPlayBtn = mainView.findViewById(R.id.playBtn);
        mPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mMakeLyricsView.lineLrcPlay((int) mMediaPlayer.getCurrentPosition());
                } else {
                    ToastUtil.showTextToast(mContext, getString(R.string.play_song_text));
                }
            }
        });

        //预览按钮
        Button preBtn = mainView.findViewById(R.id.preBtn);
        preBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LyricsInfo lyricsInfo = mMakeLyricsView.getFinishLyricsInfo();
                if (lyricsInfo == null) {
                    ToastUtil.showTextToast(mContext, getString(R.string.unfinish_lrc_text));
                } else {
                    pauseMediaPlayer();
                    if (mMakeLrcListener != null) {
                        mMakeLrcListener.openView(MakeLrcActivity.INDEX_PRELRC);
                    }
                }
            }
        });
        showContentView();
    }

    /**
     * 暂停
     */
    private void pauseMediaPlayer() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mUIHandler.sendEmptyMessage(AUDIO_PAUSE);
        }
    }

    /**
     * 关闭界面
     */
    private void closeView() {
        String tipMsg = getString(R.string.close_lrc_text);
        DialogUIUtils.showMdAlert(getActivity(), getString(R.string.tip_title), tipMsg, new DialogUIListener() {
            @Override
            public void onPositive() {
                releasePlayer();
                if (mMakeLrcListener != null) {
                    mMakeLrcListener.closeView();
                }
            }

            @Override
            public void onNegative() {

            }
        }).setCancelable(true, false).show();
    }

    @Override
    protected void handleUIMessage(Message msg) {
        switch (msg.what) {

            case INITAUDIODATA:

                mMusicSeekBar.setEnabled(false);
                mMusicSeekBar.setProgress(0);
                mMusicSeekBar.setSecondaryProgress(0);
                mMusicSeekBar.setMax(0);

                mPlayImg.setVisibility(View.VISIBLE);
                mPauseImg.setVisibility(View.INVISIBLE);

                break;

            case AUDIO_PLAY:

                if (mMediaPlayer != null) {

                    mMusicSeekBar.setEnabled(true);
                    mMusicSeekBar.setMax((int) mMediaPlayer.getDuration());
                    mMusicSeekBar.setProgress((int) mMediaPlayer.getCurrentPosition());

                }

                mPlayImg.setVisibility(View.INVISIBLE);
                mPauseImg.setVisibility(View.VISIBLE);

                mWorkerHandler.postDelayed(mPlayRunnable, 0);

                break;

            case AUDIO_PLAYING:
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mMusicSeekBar.setProgress((int) mMediaPlayer.getCurrentPosition());
                }

                break;

            case AUDIO_PAUSE:

                mWorkerHandler.removeCallbacks(mPlayRunnable);

                if (mMediaPlayer != null) {

                    mMediaPlayer.pause();

                    mMusicSeekBar.setProgress((int) mMediaPlayer.getCurrentPosition());
                }
                mPlayImg.setVisibility(View.VISIBLE);
                mPauseImg.setVisibility(View.INVISIBLE);

                break;

            case AUDIO_FINISH:

                mWorkerHandler.removeCallbacks(mPlayRunnable);

                mPlayImg.setVisibility(View.VISIBLE);
                mPauseImg.setVisibility(View.INVISIBLE);

                mMusicSeekBar.setEnabled(false);
                mMusicSeekBar.setProgress(0);
                mMusicSeekBar.setSecondaryProgress(0);
                mMusicSeekBar.setMax(0);

                break;
        }
    }

    /**
     * 播放线程
     */
    private Runnable mPlayRunnable = new Runnable() {
        @Override
        public void run() {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying() && !isSeekTo) {

                mUIHandler.sendEmptyMessage(AUDIO_PLAYING);
                mWorkerHandler.postDelayed(mPlayRunnable, 1000);
            }
        }
    };

    @Override
    protected void handleWorkerMessage(Message msg) {

    }

    /**
     * 设置音频数据
     *
     * @param audioInfo
     */
    public void setAudioInfo(AudioInfo audioInfo) {
        audioInfo.setPlayProgress(0);
        this.mAudioInfo = audioInfo;
        if (!TextUtils.isEmpty(mAudioInfo.getFilePath())) {
            mUIHandler.sendEmptyMessage(INITAUDIODATA);
        }
    }

    /**
     * 设置歌词文件
     *
     * @param lrcText
     */
    public void setLrcText(String lrcText) {
        mMakeLyricsView.setMakeLrcComText(lrcText, true);
    }

    /**
     * 获取录制完成的歌曲数据
     *
     * @return
     */
    public LyricsInfo getLyricsInfo() {
        return mMakeLyricsView.getFinishLyricsInfo();
    }

    /**
     * 初始化音频数据
     */
    public void initPlayerData() {

        try {
            mMediaPlayer = new IjkMediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(mAudioInfo.getFilePath());
            mMediaPlayer.prepareAsync();
            //播放器完成回调
            mMediaPlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(IMediaPlayer mp) {

                    mMediaPlayer.release();
                    mMediaPlayer = null;

                    mUIHandler.sendEmptyMessage(AUDIO_FINISH);
                }
            });

            mMediaPlayer.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(IMediaPlayer mp) {
                    isSeekTo = false;
                }
            });

            mMediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(IMediaPlayer mp) {
                    mUIHandler.sendEmptyMessage(AUDIO_PLAY);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        releasePlayer();
        super.onDestroyView();
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
    }

    public void setMakeLrcListener(MakeLrcActivity.MakeLrcListener makeLrcListener) {
        this.mMakeLrcListener = makeLrcListener;
    }

}
