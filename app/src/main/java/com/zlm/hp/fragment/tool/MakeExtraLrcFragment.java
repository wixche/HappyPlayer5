package com.zlm.hp.fragment.tool;

import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dou361.dialogui.DialogUIUtils;
import com.dou361.dialogui.listener.DialogUIListener;
import com.zlm.hp.adapter.tool.MakeExtraLrcAdapter;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.fragment.BaseFragment;
import com.zlm.hp.lyrics.LyricsReader;
import com.zlm.hp.lyrics.model.LyricsInfo;
import com.zlm.hp.lyrics.model.LyricsLineInfo;
import com.zlm.hp.lyrics.model.TranslateLrcLineInfo;
import com.zlm.hp.lyrics.model.make.MakeExtraLrcLineInfo;
import com.zlm.hp.lyrics.utils.StringUtils;
import com.zlm.hp.lyrics.utils.TimeUtils;
import com.zlm.hp.ui.R;
import com.zlm.hp.ui.tool.MakeLrcActivity;
import com.zlm.hp.widget.IconfontTextView;
import com.zlm.libs.widget.MusicSeekBar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * @Description: 制作额外歌词
 * @author: zhangliangming
 * @date: 2019-01-01 0:37
 **/
public class MakeExtraLrcFragment extends BaseFragment {

    private TextView mTitleTextView;
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
     * 加载歌词
     */
    private final int MESSAGE_WHAT_LOADLRC = 0;
    /**
     * 歌曲信息
     */
    private AudioInfo mAudioInfo;

    /**
     * 事件回调
     */
    private MakeLrcActivity.MakeLrcListener mMakeLrcListener;
    /**
     * 翻译歌词
     */
    public static final int EXTRA_LRC_TYPE_TRANSLATE = 0;

    /**
     * 音译歌词
     */
    public static final int EXTRA_LRC_TYPE_TRANSLITERATION = 1;

    /**
     * 歌词类型
     */
    private int mExtraLrcType = EXTRA_LRC_TYPE_TRANSLATE;


    private ExtraItemEvent mExtraItemEvent;

    /**
     * 歌词列表
     */
    private ArrayList<MakeExtraLrcLineInfo> mMakeLrcs = new ArrayList<MakeExtraLrcLineInfo>();

    /**
     * 制作歌词list视图
     */
    private RecyclerView mLinearLayoutRecyclerView;
    /**
     *
     */
    private MakeExtraLrcAdapter mMakeExtraLrcAdapter;

    /**
     *
     */
    private MakeExtraLrcAdapter.ItemEvent mItemEvent;

    /**
     *
     */
    private LyricsInfo mLyricsInfo;


    /**
     * 歌词路径
     */
    private String mLrcFilePath;

    public MakeExtraLrcFragment() {

    }


    /**
     * @return
     */
    public static MakeExtraLrcFragment newInstance() {
        MakeExtraLrcFragment fragment = new MakeExtraLrcFragment();
        return fragment;
    }


    @Override
    protected void isFristVisibleToUser() {

    }

    @Override
    protected int setContentLayoutResID() {
        return R.layout.fragment_make_extra_lrc;
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

        mTitleTextView = mainView.findViewById(R.id.title);

        if (mExtraLrcType == EXTRA_LRC_TYPE_TRANSLATE) {
            mTitleTextView.setText(getString(R.string.extra_lrc_1));
        } else {
            mTitleTextView.setText(getString(R.string.extra_lrc_2));
        }

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
                    mMediaPlayer.seekTo(mMusicSeekBar.getProgress());
                }
            }
        });
        mMusicSeekBar.setTimePopupWindowViewColor(Color.argb(200, 255, 64, 129));

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

        //
        mLinearLayoutRecyclerView = mainView.findViewById(R.id.listview);
        //初始化内容视图
        mLinearLayoutRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        DividerItemDecoration divider = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        mLinearLayoutRecyclerView.addItemDecoration(divider);


        mMakeExtraLrcAdapter = new MakeExtraLrcAdapter(getActivity().getApplicationContext(), mMakeLrcs);
        mItemEvent = new MakeExtraLrcAdapter.ItemEvent() {
            @Override
            public void itemClick(int index) {
                if (mExtraItemEvent != null) {
                    mExtraItemEvent.itemClick(index);
                }
            }
        };
        mMakeExtraLrcAdapter.setItemEvent(mItemEvent);
        mLinearLayoutRecyclerView.setAdapter(mMakeExtraLrcAdapter);

        //预览按钮
        Button preBtn = mainView.findViewById(R.id.preBtn);
        preBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseMediaPlayer();
                if (mMakeLrcListener != null) {
                    mMakeLrcListener.openView(MakeLrcActivity.INDEX_PRELRC);
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

            case MESSAGE_WHAT_LOADLRC:
                mMakeExtraLrcAdapter.notifyDataSetChanged();

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
        switch (msg.what) {
            case MESSAGE_WHAT_LOADLRC:

                loadLrcData();

                break;
        }
    }

    /**
     * 加载歌词数据
     */
    private void loadLrcData() {
        File lrcFile = new File(mLrcFilePath);
        LyricsReader lyricsReader = new LyricsReader();
        lyricsReader.loadLrc(lrcFile);
        if (lyricsReader.getLrcLineInfos() != null && lyricsReader.getLrcLineInfos().size() > 0) {
            mLyricsInfo = lyricsReader.getLyricsInfo();
            TreeMap<Integer, LyricsLineInfo> lrcLineInfos = lyricsReader.getLrcLineInfos();

            List<LyricsLineInfo> extraLrcLineInfos = null;
            if (mExtraLrcType == EXTRA_LRC_TYPE_TRANSLATE) {
                //翻译
                extraLrcLineInfos = lyricsReader.getTranslateLrcLineInfos();
            } else {
                //音译
                extraLrcLineInfos = lyricsReader.getTransliterationLrcLineInfos();
            }

            //初始化数据
            for (int i = 0; i < lrcLineInfos.size(); i++) {
                LyricsLineInfo lyricsLineInfo = lrcLineInfos.get(i);
                MakeExtraLrcLineInfo makeExtraLrcLineInfo = new MakeExtraLrcLineInfo();
                makeExtraLrcLineInfo.setLyricsLineInfo(lyricsLineInfo);
                if (extraLrcLineInfos != null && i < extraLrcLineInfos.size()) {
                    if (mExtraLrcType != EXTRA_LRC_TYPE_TRANSLATE) {
                        String extraLyrics = extraLrcLineInfos.get(i).getLineLyrics();
                        String extraLineLyrics = "";
                        if (!StringUtils.isBlank(extraLyrics)) {
                            //音译
                            String[] extraLyricsWords = extraLrcLineInfos.get(i).getLyricsWords();
                            for (int j = 0; j < extraLyricsWords.length; j++) {
                                if (j == 0) {
                                    extraLineLyrics += extraLyricsWords[j].trim();
                                } else {
                                    extraLineLyrics += "∮" + extraLyricsWords[j].trim();
                                }
                            }
                        }
                        makeExtraLrcLineInfo.setExtraLineLyrics(extraLineLyrics);
                    } else {
                        //翻译
                        String extraLineLyrics = extraLrcLineInfos.get(i).getLineLyrics();
                        if (StringUtils.isNotBlank(extraLineLyrics)) {
                            makeExtraLrcLineInfo.setExtraLineLyrics(extraLineLyrics);
                        } else {
                            makeExtraLrcLineInfo.setExtraLineLyrics("");
                        }
                    }
                }

                mMakeLrcs.add(makeExtraLrcLineInfo);
            }
        }
        mUIHandler.sendEmptyMessage(MESSAGE_WHAT_LOADLRC);
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
     * 设置歌词路径
     *
     * @param mLrcFilePath
     */
    public void setLrcFilePath(String mLrcFilePath) {
        this.mLrcFilePath = mLrcFilePath;
        if (mLrcFilePath != null && !mLrcFilePath.equals("")) {
            mWorkerHandler.sendEmptyMessage(MESSAGE_WHAT_LOADLRC);
        }
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

    public void setExtraLrcType(int extraLrcType) {
        this.mExtraLrcType = extraLrcType;
    }

    public void setMakeLrcListener(MakeLrcActivity.MakeLrcListener makeLrcListener) {
        this.mMakeLrcListener = makeLrcListener;
    }

    public void setExtraItemEvent(ExtraItemEvent mExtraItemEvent) {
        this.mExtraItemEvent = mExtraItemEvent;
    }

    /**
     * @param index
     * @return
     */
    public MakeExtraLrcLineInfo getMakeExtraLrcLineInfo(int index) {
        if (index >= 0 && index < mMakeLrcs.size()) {
            return mMakeLrcs.get(index);
        }
        return null;
    }

    /**
     * 保存和更新
     */
    public void saveAndUpdate() {
        mMakeExtraLrcAdapter.saveAndUpdate();
    }

    /**
     * 获取歌词信息
     *
     * @return
     */
    public LyricsInfo getLyricsInfo() {
        List<TranslateLrcLineInfo> mTranslateLrcLineInfos = new ArrayList<TranslateLrcLineInfo>();
        List<LyricsLineInfo> transliterationLrcLineInfos = new ArrayList<LyricsLineInfo>();
        for (int i = 0; i < mMakeLrcs.size(); i++) {
            MakeExtraLrcLineInfo makeExtraLrcLineInfo = mMakeLrcs.get(i);
            //原始歌词
            LyricsLineInfo lyricsLineInfo = makeExtraLrcLineInfo.getLyricsLineInfo();

            if (mExtraLrcType != EXTRA_LRC_TYPE_TRANSLATE) {
                String[] lyricsWords = lyricsLineInfo.getLyricsWords();
                //音译
                LyricsLineInfo transliterationLrcLineInfo = new LyricsLineInfo();
                String extraLineLyrics = makeExtraLrcLineInfo.getExtraLineLyrics();
                if (extraLineLyrics == null) {
                    extraLineLyrics = "";
                } else {
                    extraLineLyrics = extraLineLyrics.trim();
                }
                if (StringUtils.isBlank(extraLineLyrics)) {
                    String[] extraLyricsWords = new String[lyricsWords.length];
                    for (int j = 0; j < extraLyricsWords.length; j++) {
                        extraLyricsWords[j] = "";
                    }
                    transliterationLrcLineInfo.setLyricsWords(extraLyricsWords);
                } else {
                    String[] extraLyricsWords = extraLineLyrics.split("∮");
                    //歌词字验证
                    if (lyricsWords.length != extraLyricsWords.length) {

                        final int finalI = i;

                        ((LinearLayoutManager) mLinearLayoutRecyclerView.getLayoutManager()).scrollToPositionWithOffset(i, 0);
                        mUIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "第" + String.format("%0" + (mMakeLrcs.size() + "").length() + "d", (finalI + 1)) + "行歌词未完成，请先完成后再预览!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return null;
                    }
                    for (int j = 0; j < extraLyricsWords.length; j++) {
                        extraLyricsWords[j] = extraLyricsWords[j].trim();
                    }
                    transliterationLrcLineInfo.setLyricsWords(extraLyricsWords);
                }
                transliterationLrcLineInfo.setLineLyrics(extraLineLyrics);
                transliterationLrcLineInfos.add(transliterationLrcLineInfo);
            } else {
                //翻译歌词
                TranslateLrcLineInfo translateLrcLineInfo = new TranslateLrcLineInfo();
                String extraLineLyrics = makeExtraLrcLineInfo.getExtraLineLyrics();
                if (extraLineLyrics == null) {
                    extraLineLyrics = "";
                } else {
                    extraLineLyrics = extraLineLyrics.trim();
                }
                if (StringUtils.isBlank(extraLineLyrics)) {
                    extraLineLyrics = "";
                }
                translateLrcLineInfo.setLineLyrics(extraLineLyrics);
                mTranslateLrcLineInfos.add(translateLrcLineInfo);
            }
        }
        if (mTranslateLrcLineInfos != null && mTranslateLrcLineInfos.size() > 0) {
            mLyricsInfo.setTranslateLrcLineInfos(mTranslateLrcLineInfos);
        }
        if (transliterationLrcLineInfos != null && transliterationLrcLineInfos.size() > 0) {
            mLyricsInfo.setTransliterationLrcLineInfos(transliterationLrcLineInfos);
        }
        return mLyricsInfo;
    }

    /**
     * 获取上一行歌词
     *
     * @return
     */
    public int getPreIndex() {
        int index = mMakeExtraLrcAdapter.getPreIndex();
        ((LinearLayoutManager) mLinearLayoutRecyclerView.getLayoutManager()).scrollToPositionWithOffset(index, 0);
        return index;
    }

    /**
     * 获取下一行歌词
     *
     * @return
     */
    public int getNextIndex() {
        int index = mMakeExtraLrcAdapter.getNextIndex();
        ((LinearLayoutManager) mLinearLayoutRecyclerView.getLayoutManager()).scrollToPositionWithOffset(index, 0);
        return index;
    }


    /**
     * 获取歌词数据大小
     *
     * @return
     */
    public int getLrcDataSize() {
        return mMakeLrcs.size();
    }


    public interface ExtraItemEvent {
        void itemClick(int index);
    }
}
