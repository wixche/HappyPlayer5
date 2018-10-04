package com.zlm.hp.fragment;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.jdsjlzx.interfaces.OnLoadMoreListener;
import com.github.jdsjlzx.interfaces.OnRefreshListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.zlm.hp.adapter.AudioAdapter;
import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.entity.RankInfo;
import com.zlm.hp.entity.SpecialInfo;
import com.zlm.hp.http.APIHttpClient;
import com.zlm.hp.http.HttpReturnResult;
import com.zlm.hp.receiver.FragmentReceiver;
import com.zlm.hp.ui.R;
import com.zlm.hp.util.HttpUtil;
import com.zlm.hp.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 网络歌曲
 * Created by zhangliangming on 2018-08-11.
 */

public class NetSongFragment extends BaseFragment {

    /**
     *
     */
    private LRecyclerView mRecyclerView;

    private LRecyclerViewAdapter mAdapter;

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
    public static final int NET_SONG_TYPE_SPECIAL = 0;

    /**
     * 排行
     */
    public static final int NET_SONG_TYPE_RECOMMEND = 1;
    /**
     * 网络歌曲类型
     */
    private int mNetSongType = NET_SONG_TYPE_RECOMMEND;

    /**
     * 歌曲类型key
     */
    public static final String NETSONGTYPE_KEY = "NETSONGTYPE_KEY";

    /**
     * data key
     */
    public static final String DATA_KEY = "DATA_KEY";

    /**
     * arguments key
     */
    public static final String ARGUMENTS_KEY = "ARGUMENTS_KEY";

    public NetSongFragment() {

    }

    /**
     * @return
     */
    public static NetSongFragment newInstance() {
        NetSongFragment fragment = new NetSongFragment();
        return fragment;

    }

    @Override
    protected int setContentLayoutResID() {
        return R.layout.fragment_net_song;
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
        mAdapter = new LRecyclerViewAdapter(new AudioAdapter(mContext, mDatas));
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

        String title = getString(R.string.app_name);
        Object data = null;
        Bundle bundle = getArguments();
        if (bundle != null) {
            mNetSongType = bundle.getInt(NETSONGTYPE_KEY, NET_SONG_TYPE_RECOMMEND);
            data = bundle.get(DATA_KEY);
        }
        if (data == null) {
            data = RankInfo.newDefRankInfo();
        }
        switch (mNetSongType) {
            case NET_SONG_TYPE_RECOMMEND:

                RankInfo rankInfo = (RankInfo) data;
                title = rankInfo.getRankName();

                break;
            case NET_SONG_TYPE_SPECIAL:

                SpecialInfo specialInfo = (SpecialInfo) data;
                title = specialInfo.getSpecialName();

                break;
        }
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
        int pageSize = 0;
        if (!httpReturnResult.isSuccessful()) {
            ToastUtil.showTextToast(mContext, httpReturnResult.getErrorMsg());
        } else {
            mPage++;

            Map<String, Object> returnResult = (Map<String, Object>) httpReturnResult.getResult();
            List<AudioInfo> lists = (List<AudioInfo>) returnResult.get("rows");
            pageSize = lists.size();
            if (lists == null || pageSize == 0) {
                mRecyclerView.setNoMore(true);
            } else {
                for (int i = 0; i < pageSize; i++) {
                    mDatas.add(lists.get(i));
                }
                mAdapter.notifyDataSetChanged();
            }
        }
        mRecyclerView.refreshComplete(pageSize);
    }

    /**
     * 处理加载数据
     *
     * @param httpReturnResult
     */
    private void handleLoadData(HttpReturnResult httpReturnResult) {
        int pageSize = 0;
        if (!httpReturnResult.isSuccessful()) {
            ToastUtil.showTextToast(mContext, httpReturnResult.getErrorMsg());
        } else {
            mDatas.clear();
            Map<String, Object> returnResult = (Map<String, Object>) httpReturnResult.getResult();
            List<AudioInfo> lists = (List<AudioInfo>) returnResult.get("rows");
            pageSize = lists.size();
            for (int i = 0; i < pageSize; i++) {
                mDatas.add(lists.get(i));
            }
            mAdapter.notifyDataSetChanged();
        }
        mRecyclerView.refreshComplete(pageSize);

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
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        ConfigInfo configInfo = ConfigInfo.obtain();
        int page = mPage + 1;
        HttpReturnResult httpReturnResult = null;

        Bundle bundle = getArguments();
        Object data = null;
        if (bundle != null) {
            mNetSongType = bundle.getInt(NETSONGTYPE_KEY, NET_SONG_TYPE_RECOMMEND);
            data = bundle.get(DATA_KEY);
        }
        if (data == null) {
            data = RankInfo.newDefRankInfo();
        }

        switch (mNetSongType) {
            case NET_SONG_TYPE_RECOMMEND:

                RankInfo rankInfo = (RankInfo) data;
                httpReturnResult = apiHttpClient.rankSongList(mContext, rankInfo.getRankId(), page, mPageSize, configInfo.isWifi());

                break;
            case NET_SONG_TYPE_SPECIAL:
                SpecialInfo specialInfo = (SpecialInfo) data;
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
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        resetPage();

        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        ConfigInfo configInfo = ConfigInfo.obtain();

        HttpReturnResult httpReturnResult = null;

        Bundle bundle = getArguments();
        Object data = null;
        if (bundle != null) {
            mNetSongType = bundle.getInt(NETSONGTYPE_KEY, NET_SONG_TYPE_RECOMMEND);
            data = bundle.get(DATA_KEY);
        }
        if (data == null) {
            data = RankInfo.newDefRankInfo();
        }

        switch (mNetSongType) {
            case NET_SONG_TYPE_RECOMMEND:

                RankInfo rankInfo = (RankInfo) data;
                httpReturnResult = apiHttpClient.rankSongList(mContext, rankInfo.getRankId(), mPage, mPageSize, configInfo.isWifi());

                break;
            case NET_SONG_TYPE_SPECIAL:
                SpecialInfo specialInfo = (SpecialInfo) data;
                httpReturnResult = apiHttpClient.specialSongList(mContext, specialInfo.getSpecialId(), mPage, mPageSize, configInfo.isWifi());

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

}