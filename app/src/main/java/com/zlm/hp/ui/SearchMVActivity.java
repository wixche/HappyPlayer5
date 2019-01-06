package com.zlm.hp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.jdsjlzx.interfaces.OnLoadMoreListener;
import com.github.jdsjlzx.interfaces.OnNetWorkErrorListener;
import com.github.jdsjlzx.interfaces.OnRefreshListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.zlm.hp.adapter.MVAdapter;
import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.entity.VideoInfo;
import com.zlm.hp.http.APIHttpClient;
import com.zlm.hp.http.HttpReturnResult;
import com.zlm.hp.util.HttpUtil;
import com.zlm.hp.util.ToastUtil;
import com.zlm.libs.widget.SwipeBackLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: mv搜索界面
 * @author: zhangliangming
 * @date: 2019-01-05 18:38
 **/
public class SearchMVActivity extends BaseActivity {

    /**
     *
     */

    public static final String DATA_KEY = "Data_Key";


    /**
     *
     */
    private LRecyclerView mRecyclerView;

    private LRecyclerViewAdapter mAdapter;

    /**
     *
     */
    private ArrayList<VideoInfo> mDatas;

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
     *
     */
    private SwipeBackLayout mSwipeBackLayout;

    /**
     * 音频歌曲
     */
    private AudioInfo mAudioInfo;

    /**
     * 关键字
     */
    private String mKeyword;

    @Override
    protected int setContentLayoutResID() {
        return R.layout.activity_search_mv;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        mSwipeBackLayout = findViewById(R.id.swipeback_layout);
        mSwipeBackLayout.setSwipeBackLayoutListener(new SwipeBackLayout.SwipeBackLayoutListener() {

            @Override
            public void finishActivity() {
                finish();
                overridePendingTransition(0, 0);
            }
        });

        TextView titleView = findViewById(R.id.title);

        //搜索信息
        mAudioInfo = getIntent().getParcelableExtra(DATA_KEY);
        mKeyword = mAudioInfo.getSongName();
        if (mKeyword.contains("【")) {
            int index = mKeyword.indexOf("【");
            mKeyword = mKeyword.substring(0, index);
        }
        if (mKeyword.contains("(")) {
            int index = mKeyword.indexOf("(");
            mKeyword = mKeyword.substring(0, index);
        }
        titleView.setText(mKeyword.trim());

        //返回
        ImageView backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwipeBackLayout.closeView();
            }
        });

        mRecyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        //
        mDatas = new ArrayList<VideoInfo>();
        mAdapter = new LRecyclerViewAdapter(new MVAdapter(mUIHandler, mWorkerHandler, mContext, mDatas));
        ((MVAdapter) (mAdapter.getInnerAdapter())).setOnClickListener(new MVAdapter.OnClickListener() {
            @Override
            public void openVideoView(VideoInfo videoInfo) {
                //打开视频界面
                Intent intent = new Intent(SearchMVActivity.this, VideoActivity.class);
                intent.putExtra(VideoInfo.DATA_KEY, videoInfo);
                startActivity(intent);
            }
        });
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

        //加载数据
        mRecyclerView.forceToRefresh();
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
            List<VideoInfo> lists = (List<VideoInfo>) returnResult.get("rows");
            int total = (int) returnResult.get("total");
            int pageSize = lists.size();
            if (total <= mAdapter.getItemCount() || total == 0) {
                mRecyclerView.setNoMore(true);
            } else {
                for (int i = 0; i < pageSize; i++) {
                    mDatas.add(lists.get(i));
                }
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
            List<VideoInfo> lists = (List<VideoInfo>) returnResult.get("rows");
            int pageSize = lists.size();
            for (int i = 0; i < pageSize; i++) {
                mDatas.add(lists.get(i));
            }
            mRecyclerView.refreshComplete(pageSize);
            mAdapter.notifyDataSetChanged();
        }

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
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        ConfigInfo configInfo = ConfigInfo.obtain();
        int page = mPage + 1;
        HttpReturnResult httpReturnResult = apiHttpClient.searchMVList(mContext, mKeyword, page, mPageSize, configInfo.isWifi());

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
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        resetPage();

        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        ConfigInfo configInfo = ConfigInfo.obtain();
        HttpReturnResult httpReturnResult = apiHttpClient.searchMVList(mContext, mKeyword, mPage, mPageSize, configInfo.isWifi());

        //
        Message msg = Message.obtain();
        msg.what = LOADREFRESHDATA;
        msg.obj = httpReturnResult;
        mUIHandler.sendMessage(msg);
    }


    @Override
    public void onBackPressed() {
        mSwipeBackLayout.closeView();
    }
}
