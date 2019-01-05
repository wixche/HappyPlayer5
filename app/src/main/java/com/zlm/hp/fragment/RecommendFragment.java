package com.zlm.hp.fragment;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.github.jdsjlzx.interfaces.OnRefreshListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.zlm.hp.adapter.RecommendAdapter;
import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.entity.RankInfo;
import com.zlm.hp.http.APIHttpClient;
import com.zlm.hp.http.HttpReturnResult;
import com.zlm.hp.ui.R;
import com.zlm.hp.util.HttpUtil;
import com.zlm.hp.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 排行
 * Created by zhangliangming on 2018-08-11.
 */

public class RecommendFragment extends BaseFragment {

    /**
     *
     */
    private LRecyclerView mRecyclerView;
    /**
     *
     */
    private LRecyclerViewAdapter mAdapter;

    /**
     *
     */
    private ArrayList<RankInfo> mDatas;
    /**
     * 加载刷新数据
     */
    private final int LOADREFRESHDATA = 0;

    public RecommendFragment() {

    }

    /**
     * @return
     */
    public static RecommendFragment newInstance() {
        RecommendFragment fragment = new RecommendFragment();
        return fragment;

    }

    @Override
    protected int setContentLayoutResID() {
        return R.layout.fragment_recommend;
    }

    @Override
    protected void preInitStatusBar() {
        setAddStatusBarView(false);
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
        mRecyclerView = mainView.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        //
        mDatas = new ArrayList<RankInfo>();
        mAdapter = new LRecyclerViewAdapter(new RecommendAdapter(mUIHandler, mWorkerHandler, mContext, mDatas));
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
            List<RankInfo> lists = (List<RankInfo>) returnResult.get("rows");
            int pageSize = lists.size();
            for (int i = 0; i < pageSize; i++) {
                mDatas.add(lists.get(i));
            }
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
        HttpReturnResult httpReturnResult = apiHttpClient.rankList(mContext, configInfo.isWifi());

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
