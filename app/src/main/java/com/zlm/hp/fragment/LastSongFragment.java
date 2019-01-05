package com.zlm.hp.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;

import com.github.jdsjlzx.interfaces.OnRefreshListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.zlm.down.entity.DownloadTask;
import com.zlm.hp.adapter.AudioAdapter;
import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.http.APIHttpClient;
import com.zlm.hp.http.HttpReturnResult;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.ui.R;
import com.zlm.hp.util.HttpUtil;
import com.zlm.hp.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 新歌
 * Created by zhangliangming on 2018-08-11.
 */

public class LastSongFragment extends BaseFragment {

    /**
     *
     */
    private LRecyclerView mRecyclerView;

    private LRecyclerViewAdapter mAdapter;

    /**
     * 音频广播
     */
    private AudioBroadcastReceiver mAudioBroadcastReceiver;


    /**
     *
     */
    private ArrayList<AudioInfo> mDatas;

    /**
     * 加载刷新数据
     */
    private final int LOADREFRESHDATA = 0;

    /**
     * 加载更多数据
     */
    private final int LOADMOREDATA = 1;

    private int mPage = 1;
    /**
     *
     */
    private int mPageSize = 20;


    public LastSongFragment() {

    }

    /**
     * @return
     */
    public static LastSongFragment newInstance() {
        LastSongFragment fragment = new LastSongFragment();
        return fragment;

    }

    @Override
    protected void preInitStatusBar() {
        setAddStatusBarView(false);
    }

    @Override
    protected int setContentLayoutResID() {
        return R.layout.fragment_last_song;
    }

    @Override
    protected void initViews(View mainView, Bundle savedInstanceState) {
        initView(mainView);
        showLoadingView();
    }

    /**
     * @param mainView
     */
    private void initView(View mainView) {
        mRecyclerView = mainView.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        //
        mDatas = new ArrayList<AudioInfo>();
        mAdapter = new LRecyclerViewAdapter(new AudioAdapter(mContext, mDatas, SongFragment.SONG_TYPE_LAST));
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.setLoadMoreEnabled(false);

        mRecyclerView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                //refresh data here
                mWorkerHandler.sendEmptyMessage(LOADREFRESHDATA);
            }
        });

        //
        setRefreshListener(new RefreshListener() {
            @Override
            public void refresh() {
                showLoadingView();
                mWorkerHandler.sendEmptyMessage(LOADREFRESHDATA);
            }
        });

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

            private void handleAudioBroadcastReceiver(Intent intent, int code) {
                switch (code) {
                    case AudioBroadcastReceiver.ACTION_CODE_NULL:
                    case AudioBroadcastReceiver.ACTION_CODE_INIT:

                        if (mAdapter != null) {
                            Bundle initBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                            if (initBundle == null) {
                                ((AudioAdapter) (mAdapter.getInnerAdapter())).reshViewHolder(null);
                                return;
                            }
                            AudioInfo initAudioInfo = initBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                            ((AudioAdapter) (mAdapter.getInnerAdapter())).reshViewHolder(initAudioInfo.getHash());
                        }
                        break;

                    case AudioBroadcastReceiver.ACTION_CODE_UPDATE_LIKE:

                        //喜欢/不喜欢
                        Bundle likeBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                        String likeHash = likeBundle.getString(AudioBroadcastReceiver.ACTION_DATA_KEY);
                        if (!TextUtils.isEmpty(likeHash)) {
                            ((AudioAdapter) (mAdapter.getInnerAdapter())).reshViewHolder(likeHash);
                        }

                        break;

                    case AudioBroadcastReceiver.ACTION_CODE_DOWNLOAD_FINISH:
                    case AudioBroadcastReceiver.ACTION_CODE_DOWNLOADONEDLINESONG:
                        if (mAdapter == null) {
                            return;
                        }
                        //网络歌曲下载完成
                        Bundle downloadedBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                        DownloadTask downloadedTask = downloadedBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                        String downloadedHash = downloadedTask.getTaskId();
                        if (downloadedTask != null && !TextUtils.isEmpty(downloadedHash)) {
                            ((AudioAdapter) (mAdapter.getInnerAdapter())).reshViewHolder(downloadedHash);
                        }

                        break;

                }
            }
        });
        mAudioBroadcastReceiver.registerReceiver(mContext);
    }

    @Override
    protected void handleUIMessage(Message msg) {
        switch (msg.what) {
            case LOADREFRESHDATA:

                handleLoadData((HttpReturnResult) msg.obj);

                break;
        }
    }

    /**
     * 处理加载数据
     *
     * @param httpReturnResult
     */
    private void handleLoadData(HttpReturnResult httpReturnResult) {

        if (!httpReturnResult.isSuccessful()) {
            ToastUtil.showTextToast(mContext, httpReturnResult.getErrorMsg());

            mRecyclerView.refreshComplete(0);
            mAdapter.notifyDataSetChanged();
        } else {
            mDatas.clear();
            Map<String, Object> returnResult = (Map<String, Object>) httpReturnResult.getResult();
            List<AudioInfo> lists = (List<AudioInfo>) returnResult.get("rows");
            int pageSize = lists.size();
            for (int i = 0; i < pageSize; i++) {
                mDatas.add(lists.get(i));
            }

            ((AudioAdapter) (mAdapter.getInnerAdapter())).resetMenuOpenIndex();
            mRecyclerView.refreshComplete(pageSize);
            mAdapter.notifyDataSetChanged();
        }
        mRecyclerView.setLoadMoreEnabled(false);

        showContentView();
    }

    @Override
    protected void handleWorkerMessage(Message msg) {
        switch (msg.what) {
            case LOADREFRESHDATA:

                loadRefreshData();

                break;
        }
    }

    /**
     * 加载刷新数据
     */
    private void loadRefreshData() {

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        ConfigInfo configInfo = ConfigInfo.obtain();
        HttpReturnResult httpReturnResult = apiHttpClient.lastSongList(mContext, configInfo.isWifi());

        //
        Message msg = Message.obtain();
        msg.what = LOADREFRESHDATA;
        msg.obj = httpReturnResult;
        mUIHandler.sendMessage(msg);
    }

    @Override
    protected void isFristVisibleToUser() {
        mWorkerHandler.sendEmptyMessage(LOADREFRESHDATA);
    }

    @Override
    public void onDestroy() {
        if (mAudioBroadcastReceiver != null)
            mAudioBroadcastReceiver.unregisterReceiver(mContext);
        super.onDestroy();
    }
}
