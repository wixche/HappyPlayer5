package com.zlm.hp.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.entity.LrcInfo;
import com.zlm.hp.http.APIHttpClient;
import com.zlm.hp.http.HttpReturnResult;
import com.zlm.hp.lyrics.LyricsReader;
import com.zlm.hp.lyrics.widget.AbstractLrcView;
import com.zlm.hp.lyrics.widget.ManyLyricsView;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.manager.LyricsManager;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.ui.R;
import com.zlm.hp.util.ColorUtil;
import com.zlm.hp.util.HttpUtil;
import com.zlm.hp.util.ImageUtil;
import com.zlm.hp.util.ResourceUtil;
import com.zlm.hp.util.ToastUtil;
import com.zlm.hp.widget.ButtonRelativeLayout;

/**
 * @Description: 歌词
 * @author: zhangliangming
 * @date: 2018-11-24 22:45
 **/
public class LrcFragment extends BaseFragment {

    /**
     *
     */
    public static final String LRC_DATA_KEY = "lrcDataKey";

    /**
     *
     */
    public static final String AUDIO_DATA_KEY = "audioDataKey";
    /**
     *
     */
    private boolean isFristINVisibleToUser = false;

    /**
     *
     */
    private AudioInfo mAudioInfo;

    /**
     * 歌词数据
     */
    private LrcInfo mLrcInfo;

    private ConfigInfo mConfigInfo;

    //多行歌词
    private ManyLyricsView mManyLineLyricsView;

    /**
     * 使用歌词按钮
     */
    private ButtonRelativeLayout mUseBtn;

    private final int MESSAGE_WHAT_LOADDATA = 0;

    //、、、、、、、、、、、、、、、、、、、、、、、、、翻译和音译歌词、、、、、、、、、、、、、、、、、、、、、、、、、、、
    //翻译歌词
    private ImageView hideTranslateImg;
    private ImageView showTranslateImg;
    //音译歌词
    private ImageView hideTransliterationImg;
    private ImageView showTransliterationImg;

    //翻译歌词/音译歌词
    private ImageView showTTToTranslateImg;
    private ImageView showTTToTransliterationImg;
    private ImageView hideTTImg;

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
                    hideTranslateImg.setVisibility(View.INVISIBLE);
                    showTranslateImg.setVisibility(View.INVISIBLE);
                    //音译歌词
                    hideTransliterationImg.setVisibility(View.INVISIBLE);
                    showTransliterationImg.setVisibility(View.INVISIBLE);

                    //翻译歌词/音译歌词
                    showTTToTranslateImg.setVisibility(View.INVISIBLE);
                    showTTToTransliterationImg.setVisibility(View.INVISIBLE);
                    hideTTImg.setVisibility(View.INVISIBLE);


                    break;
                case HASTRANSLATEANDTRANSLITERATIONLRC:


                    //翻译歌词
                    hideTranslateImg.setVisibility(View.INVISIBLE);
                    showTranslateImg.setVisibility(View.INVISIBLE);
                    //音译歌词
                    hideTransliterationImg.setVisibility(View.INVISIBLE);
                    showTransliterationImg.setVisibility(View.INVISIBLE);

                    //翻译歌词/音译歌词
                    showTTToTranslateImg.setVisibility(View.INVISIBLE);
                    showTTToTransliterationImg.setVisibility(View.VISIBLE);
                    hideTTImg.setVisibility(View.INVISIBLE);

                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC);

                    break;
                case HASTRANSLITERATIONLRC:

                    //翻译歌词
                    hideTranslateImg.setVisibility(View.INVISIBLE);
                    showTranslateImg.setVisibility(View.INVISIBLE);
                    //音译歌词
                    hideTransliterationImg.setVisibility(View.VISIBLE);
                    showTransliterationImg.setVisibility(View.INVISIBLE);

                    //翻译歌词/音译歌词
                    showTTToTranslateImg.setVisibility(View.INVISIBLE);
                    showTTToTransliterationImg.setVisibility(View.INVISIBLE);
                    hideTTImg.setVisibility(View.INVISIBLE);

                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC);

                    break;
                case HASTRANSLATELRC:

                    //翻译歌词
                    hideTranslateImg.setVisibility(View.VISIBLE);
                    showTranslateImg.setVisibility(View.INVISIBLE);
                    //音译歌词
                    hideTransliterationImg.setVisibility(View.INVISIBLE);
                    showTransliterationImg.setVisibility(View.INVISIBLE);

                    //翻译歌词/音译歌词
                    showTTToTranslateImg.setVisibility(View.INVISIBLE);
                    showTTToTransliterationImg.setVisibility(View.INVISIBLE);
                    hideTTImg.setVisibility(View.INVISIBLE);

                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC);


                    break;

            }

        }
    };

    public LrcFragment() {

    }

    public static LrcFragment newInstance(AudioInfo audioInfo, LrcInfo lrcInfo) {
        LrcFragment fragment = new LrcFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(AUDIO_DATA_KEY, audioInfo);
        bundle.putParcelable(LRC_DATA_KEY, lrcInfo);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    protected void preInitStatusBar() {
        //添加状态栏
        setAddStatusBarView(false);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (mUIHandler != null && !getUserVisibleHint() && !isFristINVisibleToUser) {
            isFristINVisibleToUser = true;
            mUIHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mAudioInfo != null) {
                        AudioInfo audioInfo = AudioPlayerManager.getInstance(mContext).getCurSong(mConfigInfo.getPlayHash());
                        if (audioInfo == null) {
                            refreshView((int) mAudioInfo.getDuration());
                        } else if (mAudioInfo.getHash().equals(audioInfo.getHash())) {
                            refreshView((int) audioInfo.getPlayProgress());
                        }
                    }
                }
            }, 100);
        } else if (getUserVisibleHint()) {
            isFristINVisibleToUser = false;
        }
    }

    @Override
    protected void isFristVisibleToUser() {
        initData();
    }

    private void initData() {
        mWorkerHandler.sendEmptyMessage(MESSAGE_WHAT_LOADDATA);
    }

    @Override
    protected int setContentLayoutResID() {
        return R.layout.fragment_lrc;
    }

    @Override
    protected void initViews(View mainView, Bundle savedInstanceState) {
        if (getArguments() != null) {
            mAudioInfo = getArguments().getParcelable(AUDIO_DATA_KEY);
            mLrcInfo = getArguments().getParcelable(LRC_DATA_KEY);
        }

        isFristINVisibleToUser = false;
        mConfigInfo = ConfigInfo.obtain();

        //歌词视图
        mManyLineLyricsView = mainView.findViewById(R.id.lrcview);
        mManyLineLyricsView.setSize(mConfigInfo.getLrcFontSize(), mConfigInfo.getLrcFontSize(), false);
        mManyLineLyricsView.setPaintColor(new int[]{ColorUtil.parserColor("#888888"), ColorUtil.parserColor("#888888")}, false);
        mManyLineLyricsView.setPaintHLColor(new int[]{ColorUtil.parserColor("#0288d1"), ColorUtil.parserColor("#0288d1")}, false);
        mManyLineLyricsView.setIsDrawIndicator(false);

        //翻译歌词
        hideTranslateImg = mainView.findViewById(R.id.hideTranslateImg);
        ImageUtil.getTranslateColorImg(mContext, hideTranslateImg, R.mipmap.bql, ColorUtil.parserColor("#0288d1"));

        hideTranslateImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideTranslateImg.setVisibility(View.INVISIBLE);
                showTranslateImg.setVisibility(View.VISIBLE);

                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_NOSHOWEXTRALRC);
                }

            }
        });
        showTranslateImg = mainView.findViewById(R.id.showTranslateImg);
        ImageUtil.getTranslateColorImg(mContext, showTranslateImg, R.mipmap.bqm, ColorUtil.parserColor("#0288d1"));


        showTranslateImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideTranslateImg.setVisibility(View.VISIBLE);
                showTranslateImg.setVisibility(View.INVISIBLE);

                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC);
                }

            }
        });
        //音译歌词
        hideTransliterationImg = mainView.findViewById(R.id.hideTransliterationImg);
        ImageUtil.getTranslateColorImg(mContext, hideTransliterationImg, R.mipmap.bqn, ColorUtil.parserColor("#0288d1"));


        hideTransliterationImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideTransliterationImg.setVisibility(View.INVISIBLE);
                showTransliterationImg.setVisibility(View.VISIBLE);

                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_NOSHOWEXTRALRC);
                }


            }
        });
        showTransliterationImg = mainView.findViewById(R.id.showTransliterationImg);
        ImageUtil.getTranslateColorImg(mContext, showTransliterationImg, R.mipmap.bqo, ColorUtil.parserColor("#0288d1"));


        showTransliterationImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideTransliterationImg.setVisibility(View.VISIBLE);
                showTransliterationImg.setVisibility(View.INVISIBLE);

                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC);
                }

            }
        });

        //翻译歌词/音译歌词
        showTTToTranslateImg = mainView.findViewById(R.id.showTTToTranslateImg);
        ImageUtil.getTranslateColorImg(mContext, showTTToTranslateImg, R.mipmap.bqi, ColorUtil.parserColor("#0288d1"));


        showTTToTranslateImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTTToTranslateImg.setVisibility(View.INVISIBLE);
                showTTToTransliterationImg.setVisibility(View.VISIBLE);
                hideTTImg.setVisibility(View.INVISIBLE);

                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC);
                }

            }
        });
        showTTToTransliterationImg = mainView.findViewById(R.id.showTTToTransliterationImg);
        ImageUtil.getTranslateColorImg(mContext, showTTToTransliterationImg, R.mipmap.bqj, ColorUtil.parserColor("#0288d1"));


        showTTToTransliterationImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTTToTranslateImg.setVisibility(View.INVISIBLE);
                showTTToTransliterationImg.setVisibility(View.INVISIBLE);
                hideTTImg.setVisibility(View.VISIBLE);

                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC);
                }

            }
        });
        hideTTImg = mainView.findViewById(R.id.hideTTImg);
        ImageUtil.getTranslateColorImg(mContext, hideTTImg, R.mipmap.bqk, ColorUtil.parserColor("#0288d1"));

        hideTTImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTTToTranslateImg.setVisibility(View.VISIBLE);
                showTTToTransliterationImg.setVisibility(View.INVISIBLE);
                hideTTImg.setVisibility(View.INVISIBLE);


                if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mManyLineLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_NOSHOWEXTRALRC);
                }

            }
        });

        //
        mManyLineLyricsView.setExtraLyricsListener(new AbstractLrcView.ExtraLyricsListener() {
            @Override
            public void extraLrcCallback() {
                int extraLrcType = mManyLineLyricsView.getExtraLrcType();
                if (extraLrcType == AbstractLrcView.EXTRALRCTYPE_NOLRC) {
                    mExtraLrcTypeHandler.sendEmptyMessage(NOEXTRALRC);
                } else if (extraLrcType == AbstractLrcView.EXTRALRCTYPE_TRANSLATELRC) {
                    mExtraLrcTypeHandler.sendEmptyMessage(HASTRANSLATELRC);
                } else if (extraLrcType == AbstractLrcView.EXTRALRCTYPE_TRANSLITERATIONLRC) {
                    mExtraLrcTypeHandler.sendEmptyMessage(HASTRANSLITERATIONLRC);
                } else if (extraLrcType == AbstractLrcView.EXTRALRCTYPE_BOTH) {
                    mExtraLrcTypeHandler.sendEmptyMessage(HASTRANSLATEANDTRANSLITERATIONLRC);
                }


            }
        });

        //使用歌词按钮
        mUseBtn = mainView.findViewById(R.id.uselrcbtn);
        mUseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAudioInfo != null && mManyLineLyricsView != null && mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {

                    String hash = mAudioInfo.getHash();

                    //使用歌词加载中
                    AudioBroadcastReceiver.sendLrcReLoadingReceiver(mContext, hash);

                    //歌词文件名
                    String fileName = mAudioInfo.getTitle();
                    String lrcFilePath = ResourceUtil.getFilePath(mContext, ResourceConstants.PATH_LYRICS, fileName + ".krc");
                    mManyLineLyricsView.getLyricsReader().setLrcFilePath(lrcFilePath);
                    LyricsManager.getInstance(mContext).setLyricsReader(hash, mManyLineLyricsView.getLyricsReader());

                    //发送使用歌词广播
                    AudioBroadcastReceiver.sendLrcReLoadedReceiver(mContext, hash);

                    ToastUtil.showTextToast(mContext, getString(R.string.setting_song_text));
                }
            }
        });

        showLoadingView();
    }

    /**
     * 刷新view
     *
     * @param playProgress
     */
    public void refreshView(int playProgress) {
        boolean isPlay = getUserVisibleHint();
        if (mManyLineLyricsView != null) {
            if (mManyLineLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                if (isPlay) {
                    if (mManyLineLyricsView.getLrcPlayerStatus() != AbstractLrcView.LRCPLAYERSTATUS_PLAY) {
                        mManyLineLyricsView.play(playProgress);
                    }
                } else {
                    if (mManyLineLyricsView.getLrcPlayerStatus() == AbstractLrcView.LRCPLAYERSTATUS_PLAY) {
                        mManyLineLyricsView.pause();
                    }
                    if (mManyLineLyricsView.getLrcPlayerStatus() != AbstractLrcView.LRCPLAYERSTATUS_SEEKTO) {
                        mManyLineLyricsView.seekto(playProgress);
                    }
                }
            }
        }
    }

    @Override
    protected void handleUIMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_WHAT_LOADDATA:
                if (msg.obj != null && msg.obj instanceof LrcInfo) {
                    LrcInfo lrcInfo = (LrcInfo) msg.obj;
                    if (mAudioInfo != null && mManyLineLyricsView.getLyricsReader() == null) {
                        String hash = mAudioInfo.getHash();
                        //歌词文件名
                        String fileName = mAudioInfo.getTitle();
                        String lrcFilePath = ResourceUtil.getFilePath(mContext, ResourceConstants.PATH_LYRICS, fileName + ".krc");
                        //
                        LyricsReader lyricsReader = new LyricsReader();
                        lyricsReader.setHash(hash);
                        lyricsReader.loadLrc(lrcInfo.getContent(), null, lrcFilePath);

                        mManyLineLyricsView.setLyricsReader(lyricsReader);
                    }
                }
                showContentView();
                break;
        }
    }

    @Override
    protected void handleWorkerMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_WHAT_LOADDATA:

                Message nMsg = Message.obtain();
                nMsg.what = MESSAGE_WHAT_LOADDATA;


                if (mAudioInfo != null && mManyLineLyricsView.getLyricsReader() == null) {
                    APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
                    HttpReturnResult httpReturnResult = apiHttpClient.getLyricsInfo(mContext, mLrcInfo.getId(), mLrcInfo.getAccesskey(), mConfigInfo.isWifi());
                    if (httpReturnResult.isSuccessful()) {
                        LrcInfo lrcInfo = (LrcInfo) httpReturnResult.getResult();
                        nMsg.obj = lrcInfo;
                    }

                }

                mUIHandler.sendMessage(nMsg);

                break;
        }
    }

    @Override
    public void onDestroyView() {
        if (mManyLineLyricsView != null) {
            mManyLineLyricsView.release();
        }
        System.gc();
        super.onDestroyView();
    }

}
