package com.zlm.hp.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zlm.hp.db.util.AudioInfoDB;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.receiver.FragmentReceiver;
import com.zlm.hp.ui.R;

/**
 * Created by zhangliangming on 2018-08-11.
 */

public class MeFragment extends BaseFragment {

    /**
     * 本地音乐
     */
    private LinearLayout mLocalMusicLL;

    /**
     * 本地音乐个数
     */
    private TextView mLocalCountTv;

    /**
     * 加载本地歌曲的个数
     */
    private final int LOAD_LOCAL_AUDIO_COUNT = 0;

    /**
     * 喜欢音乐
     */
    private LinearLayout mLikeMusicLL;

    /**
     * 喜欢音乐个数
     */
    private TextView mLikeCountTv;

    /**
     * 加载喜欢歌曲的个数
     */
    private final int LOAD_LIKE_AUDIO_COUNT = 1;

    /**
     * 最近音乐
     */
    private LinearLayout mRecentMusicLL;

    /**
     * 最近音乐个数
     */
    private TextView mRecentCountTv;

    /**
     * 加载最近歌曲的个数
     */
    private final int LOAD_RECENT_AUDIO_COUNT = 2;

    /**
     * 下载
     */
    private LinearLayout mDownloadMusicLL;

    /**
     * 下载音乐个数
     */
    private TextView mDownloadCountTv;

    /**
     * 加载下载歌曲的个数
     */
    private final int LOAD_DOWNLOAD_AUDIO_COUNT = 3;

    /**
     * 音频广播
     */
    private AudioBroadcastReceiver mAudioBroadcastReceiver;

    public MeFragment() {
    }

    /**
     * @return
     */
    public static MeFragment newInstance() {
        MeFragment fragment = new MeFragment();
        return fragment;

    }

    @Override
    protected void isFristVisibleToUser() {
        loadData();
    }

    private void loadData() {
        mWorkerHandler.sendEmptyMessage(LOAD_LOCAL_AUDIO_COUNT);
        mWorkerHandler.sendEmptyMessage(LOAD_LIKE_AUDIO_COUNT);
        mWorkerHandler.sendEmptyMessage(LOAD_RECENT_AUDIO_COUNT);
        mWorkerHandler.sendEmptyMessage(LOAD_DOWNLOAD_AUDIO_COUNT);
    }

    @Override
    protected void preInitStatusBar() {
        setAddStatusBarView(false);
    }


    @Override
    protected int setContentLayoutResID() {
        return R.layout.fragment_me;
    }

    @Override
    protected void initViews(View mainView, Bundle savedInstanceState) {
        initViews(mainView);
        showContentView();
    }

    private void initViews(View mainView) {

        //本地音乐
        mLocalMusicLL = mainView.findViewById(R.id.tab_local_music);
        mLocalMusicLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentReceiver.sendLocalFragmentReceiver(mContext, getString(R.string.tab_localmusic));
            }
        });
        mLocalCountTv = mainView.findViewById(R.id.local_music_count);

        //喜欢音乐
        mLikeMusicLL = mainView.findViewById(R.id.tab_like_music);
        mLikeMusicLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentReceiver.sendLikeFragmentReceiver(mContext, getString(R.string.tab_like));
            }
        });
        mLikeCountTv = mainView.findViewById(R.id.like_music_count);

        //最近音乐
        mRecentMusicLL = mainView.findViewById(R.id.tab_recent_music);
        mRecentMusicLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentReceiver.sendRecentFragmentReceiver(mContext, getString(R.string.tab_recent));
            }
        });
        mRecentCountTv = mainView.findViewById(R.id.recent_music_count);

        //下载音乐
        mDownloadMusicLL = mainView.findViewById(R.id.tab_download_music);
        mDownloadMusicLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentReceiver.sendDownloadFragmentReceiver(mContext);
            }
        });
        mDownloadCountTv = mainView.findViewById(R.id.download_music_count);

        //广播
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

            private void handleAudioBroadcastReceiver(Intent intent, int code) {
                switch (code) {
                    case AudioBroadcastReceiver.ACTION_CODE_UPDATE_RECENT:
                        //最近歌曲更新
                        mWorkerHandler.sendEmptyMessage(LOAD_RECENT_AUDIO_COUNT);
                        break;
                    case AudioBroadcastReceiver.ACTION_CODE_UPDATE_LOCAL:
                        //本地歌曲更新
                        mWorkerHandler.sendEmptyMessage(LOAD_LOCAL_AUDIO_COUNT);
                        break;
                    case AudioBroadcastReceiver.ACTION_CODE_UPDATE_LIKE:
                        //喜欢歌曲更新
                        mWorkerHandler.sendEmptyMessage(LOAD_LIKE_AUDIO_COUNT);
                        break;
                    case AudioBroadcastReceiver.ACTION_CODE_UPDATE_DOWNLOAD:

                        //下载歌曲更新
                        mWorkerHandler.sendEmptyMessage(LOAD_DOWNLOAD_AUDIO_COUNT);
                        break;
                }
            }
        });
        mAudioBroadcastReceiver.registerReceiver(mContext);
    }

    @Override
    protected void handleUIMessage(Message msg) {
        switch (msg.what) {
            case LOAD_LOCAL_AUDIO_COUNT:

                int localCount = (int) msg.obj;
                mLocalCountTv.setText(localCount + "");

                break;
            case LOAD_LIKE_AUDIO_COUNT:

                int likeCount = (int) msg.obj;
                mLikeCountTv.setText(likeCount + "");

                break;
            case LOAD_RECENT_AUDIO_COUNT:

                int recentCount = (int) msg.obj;
                mRecentCountTv.setText(recentCount + "");

                break;
            case LOAD_DOWNLOAD_AUDIO_COUNT:

                int downloadCount = (int) msg.obj;
                mDownloadCountTv.setText(downloadCount + "");
                break;
        }
    }

    @Override
    protected void handleWorkerMessage(Message msg) {
        switch (msg.what) {
            case LOAD_LOCAL_AUDIO_COUNT:

                int localCount = AudioInfoDB.getLocalAudioCount(mContext);
                Message localMsg = Message.obtain();
                localMsg.what = LOAD_LOCAL_AUDIO_COUNT;
                localMsg.obj = localCount;

                mUIHandler.sendMessage(localMsg);

                break;

            case LOAD_LIKE_AUDIO_COUNT:
                int likeCount = AudioInfoDB.getLikeAudioCount(mContext);
                Message likeMsg = Message.obtain();
                likeMsg.what = LOAD_LIKE_AUDIO_COUNT;
                likeMsg.obj = likeCount;

                mUIHandler.sendMessage(likeMsg);

                break;

            case LOAD_RECENT_AUDIO_COUNT:
                int recentCount = AudioInfoDB.getRecentAudioCount(mContext);
                Message recentMsg = Message.obtain();
                recentMsg.what = LOAD_RECENT_AUDIO_COUNT;
                recentMsg.obj = recentCount;

                mUIHandler.sendMessage(recentMsg);
                break;

            case LOAD_DOWNLOAD_AUDIO_COUNT:
                int downloadCount = AudioInfoDB.getDownloadAudioCount(mContext);
                Message downloadMsg = Message.obtain();
                downloadMsg.what = LOAD_DOWNLOAD_AUDIO_COUNT;
                downloadMsg.obj = downloadCount;

                mUIHandler.sendMessage(downloadMsg);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        if (mAudioBroadcastReceiver != null) {
            mAudioBroadcastReceiver.unregisterReceiver(mContext);
        }
        super.onDestroyView();
    }
}
