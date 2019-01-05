package com.zlm.hp.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.jdsjlzx.interfaces.OnLoadMoreListener;
import com.github.jdsjlzx.interfaces.OnNetWorkErrorListener;
import com.github.jdsjlzx.interfaces.OnRefreshListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.zlm.down.entity.DownloadTask;
import com.zlm.hp.adapter.AudioAdapter;
import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.db.util.AudioInfoDB;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.entity.RankInfo;
import com.zlm.hp.entity.SpecialInfo;
import com.zlm.hp.http.APIHttpClient;
import com.zlm.hp.http.HttpClient;
import com.zlm.hp.http.HttpReturnResult;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.receiver.FragmentReceiver;
import com.zlm.hp.ui.R;
import com.zlm.hp.util.HttpUtil;
import com.zlm.hp.util.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 歌曲
 * Created by zhangliangming on 2018-08-11.
 */

public class SongFragment extends BaseFragment {

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

    /**
     * 歌单
     */
    public static final int SONG_TYPE_SPECIAL = 0;

    /**
     * 排行
     */
    public static final int SONG_TYPE_RECOMMEND = 1;

    /**
     * 本地
     */
    public static final int SONG_TYPE_LOCAL = 2;

    /**
     * 最新
     */
    public static final int SONG_TYPE_LAST = 3;

    /**
     * 喜欢
     */
    public static final int SONG_TYPE_LIKE = 4;

    /**
     * 最近
     */
    public static final int SONG_TYPE_RECENT = 5;

    /**
     * 搜索
     */
    public static final int SONG_TYPE_SEARCH = 6;

    /**
     * 网络歌曲类型
     */
    private int mSongType = SONG_TYPE_RECOMMEND;

    /**
     * 歌曲类型key
     */
    public static final String SONGTYPE_KEY = "SONGTYPE_KEY";

    /**
     * data key
     */
    public static final String DATA_KEY = "DATA_KEY";

    /**
     * arguments key
     */
    public static final String ARGUMENTS_KEY = "ARGUMENTS_KEY";

    public SongFragment() {

    }

    /**
     * @return
     */
    public static SongFragment newInstance() {
        SongFragment fragment = new SongFragment();
        return fragment;

    }

    @Override
    protected int setContentLayoutResID() {
        return R.layout.fragment_song;
    }

    @Override
    protected void initViews(View mainView, Bundle savedInstanceState) {
        initView(mainView);
        //showLoadingView();
    }

    /**
     * @param mainView
     */
    private void initView(View mainView) {

        String title = getString(R.string.app_name);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mSongType = bundle.getInt(SONGTYPE_KEY, SONG_TYPE_RECOMMEND);
        }


        mRecyclerView = mainView.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        //
        mDatas = new ArrayList<AudioInfo>();
        mAdapter = new LRecyclerViewAdapter(new AudioAdapter(mContext, mDatas, mSongType));
        mRecyclerView.setAdapter(mAdapter);


        mRecyclerView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                //refresh data here
                mWorkerHandler.sendEmptyMessage(LOADREFRESHDATA);
            }
        });
        mRecyclerView.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                // load more data here
                mWorkerHandler.sendEmptyMessage(LOADMOREDATA);
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

        switch (mSongType) {
            case SONG_TYPE_RECOMMEND:

                RankInfo rankInfo = bundle.getParcelable(DATA_KEY);
                title = rankInfo.getRankName();

                break;
            case SONG_TYPE_SPECIAL:

                SpecialInfo specialInfo = bundle.getParcelable(DATA_KEY);
                title = specialInfo.getSpecialName();

                break;
            case SONG_TYPE_LOCAL:
            case SONG_TYPE_LIKE:
            case SONG_TYPE_RECENT:
                title = bundle.getString(DATA_KEY);
                mRecyclerView.setLoadMoreEnabled(false);
                break;
        }

        //显示标题视图
        LinearLayout titleLL = mainView.findViewById(R.id.title_view_parent);
        titleLL.setVisibility(View.VISIBLE);

        TextView titleView = mainView.findViewById(R.id.title);
        titleView.setText(title);
        //返回
        ImageView backImg = mainView.findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentReceiver.sendReceiver(mContext, FragmentReceiver.ACTION_CODE_CLOSE_FRAGMENT, null, null);
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
                    case AudioBroadcastReceiver.ACTION_CODE_UPDATE_LOCAL:
                        if (mSongType == SONG_TYPE_LOCAL) {
                            //歌曲更新
                            mWorkerHandler.sendEmptyMessage(LOADREFRESHDATA);
                        }
                        break;

                    case AudioBroadcastReceiver.ACTION_CODE_UPDATE_LIKE:
                        if (mSongType == SONG_TYPE_LIKE) {
                            //歌曲更新
                            mWorkerHandler.sendEmptyMessage(LOADREFRESHDATA);
                        } else {
                            //喜欢/不喜欢
                            Bundle likeBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                            String likeHash = likeBundle.getString(AudioBroadcastReceiver.ACTION_DATA_KEY);
                            if (!TextUtils.isEmpty(likeHash)) {
                                ((AudioAdapter) (mAdapter.getInnerAdapter())).reshViewHolder(likeHash);
                            }
                        }
                        break;

                    case AudioBroadcastReceiver.ACTION_CODE_UPDATE_RECENT:
                        if (mSongType == SONG_TYPE_RECENT) {
                            //歌曲更新
                            mWorkerHandler.sendEmptyMessage(LOADREFRESHDATA);
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
            case LOADMOREDATA:
                handleLoadMoreData((HttpReturnResult) msg.obj);

                break;
        }
    }

    /**
     * 处理加载更多数据
     *
     * @param httpReturnResult
     */
    private void handleLoadMoreData(HttpReturnResult httpReturnResult) {
        if (!httpReturnResult.isSuccessful()) {
            ToastUtil.showTextToast(mContext, httpReturnResult.getErrorMsg());

            mRecyclerView.setOnNetWorkErrorListener(new OnNetWorkErrorListener() {
                @Override
                public void reload() {
                    mWorkerHandler.sendEmptyMessage(LOADMOREDATA);
                }
            });
        } else {
            mPage++;

            Map<String, Object> returnResult = (Map<String, Object>) httpReturnResult.getResult();
            List<AudioInfo> lists = (List<AudioInfo>) returnResult.get("rows");
            int total = (int) returnResult.get("total");
            int pageSize = lists.size();
            if (total <= mAdapter.getItemCount() || total == 0) {
                mRecyclerView.setNoMore(true);
            } else {
                for (int i = 0; i < pageSize; i++) {
                    mDatas.add(lists.get(i));
                }
                ((AudioAdapter) (mAdapter.getInnerAdapter())).resetMenuOpenIndex();
                mRecyclerView.refreshComplete(pageSize);
                mAdapter.notifyDataSetChanged();
            }
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

        if (mSongType == SONG_TYPE_LOCAL || mSongType == SONG_TYPE_LIKE || mSongType == SONG_TYPE_RECENT) {
            mRecyclerView.setLoadMoreEnabled(false);
        }

        showContentView();
    }

    @Override
    protected void handleWorkerMessage(Message msg) {
        switch (msg.what) {
            case LOADREFRESHDATA:

                loadRefreshData();

                break;
            case LOADMOREDATA:

                loadMoreData();

                break;
        }
    }

    /**
     * 加载更多数据
     */
    private void loadMoreData() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        ConfigInfo configInfo = ConfigInfo.obtain();
        int page = mPage + 1;
        HttpReturnResult httpReturnResult = null;

        Bundle bundle = getArguments();

        switch (mSongType) {
            case SONG_TYPE_RECOMMEND:

                RankInfo rankInfo = bundle.getParcelable(DATA_KEY);
                httpReturnResult = apiHttpClient.rankSongList(mContext, rankInfo.getRankId(), rankInfo.getRankType(), page, mPageSize, configInfo.isWifi());

                break;
            case SONG_TYPE_SPECIAL:
                SpecialInfo specialInfo = bundle.getParcelable(DATA_KEY);
                httpReturnResult = apiHttpClient.specialSongList(mContext, specialInfo.getSpecialId(), page, mPageSize, configInfo.isWifi());

                break;
        }

        //
        Message msg = Message.obtain();
        msg.what = LOADMOREDATA;
        msg.obj = httpReturnResult;
        mUIHandler.sendMessage(msg);
    }

    private void resetPage() {
        mPage = 1;
    }

    /**
     * 加载刷新数据
     */
    private void loadRefreshData() {

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        resetPage();

        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        ConfigInfo configInfo = ConfigInfo.obtain();

        HttpReturnResult httpReturnResult = null;

        Bundle bundle = getArguments();

        switch (mSongType) {
            case SONG_TYPE_RECOMMEND:

                RankInfo rankInfo = bundle.getParcelable(DATA_KEY);
                httpReturnResult = apiHttpClient.rankSongList(mContext, rankInfo.getRankId(), rankInfo.getRankType(), mPage, mPageSize, configInfo.isWifi());

                break;
            case SONG_TYPE_SPECIAL:
                SpecialInfo specialInfo = bundle.getParcelable(DATA_KEY);
                httpReturnResult = apiHttpClient.specialSongList(mContext, specialInfo.getSpecialId(), mPage, mPageSize, configInfo.isWifi());

                break;
            case SONG_TYPE_LOCAL:
            case SONG_TYPE_LIKE:
            case SONG_TYPE_RECENT:
                httpReturnResult = new HttpReturnResult();
                httpReturnResult.setStatus(HttpClient.HTTP_OK);
                List<AudioInfo> audioInfos = null;
                if (mSongType == SONG_TYPE_LOCAL) {
                    audioInfos = AudioInfoDB.getLocalAudios(mContext);
                } else if (mSongType == SONG_TYPE_LIKE) {
                    audioInfos = AudioInfoDB.getLikeAudios(mContext);
                } else if (mSongType == SONG_TYPE_RECENT) {
                    audioInfos = AudioInfoDB.getRecentAudios(mContext);
                }
                if (audioInfos == null) {
                    audioInfos = new ArrayList<AudioInfo>();
                }
                Map<String, Object> returnResult = new HashMap<String, Object>();
                returnResult.put("rows", audioInfos);
                httpReturnResult.setResult(returnResult);

                break;
        }

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