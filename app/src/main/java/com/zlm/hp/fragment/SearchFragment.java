package com.zlm.hp.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.http.APIHttpClient;
import com.zlm.hp.http.HttpClient;
import com.zlm.hp.http.HttpReturnResult;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.receiver.FragmentReceiver;
import com.zlm.hp.ui.R;
import com.zlm.hp.util.HttpUtil;
import com.zlm.hp.util.ToastUtil;
import com.zlm.hp.widget.IconfontTextView;
import com.zlm.hp.widget.SearchEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 搜索
 * @author: zhangliangming
 * @date: 2018-12-22 22:09
 **/
public class SearchFragment extends BaseFragment {

    /**
     *
     */
    private LRecyclerView mRecyclerView;

    private LRecyclerViewAdapter mAdapter;

    /**
     * 搜索框
     */
    private SearchEditText mSearchEditText;

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
     * 音频广播
     */
    private AudioBroadcastReceiver mAudioBroadcastReceiver;

    public SearchFragment() {
    }

    /**
     * @return
     */
    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        return fragment;

    }

    @Override
    protected void isFristVisibleToUser() {
        showContentView();
    }

    @Override
    protected int setContentLayoutResID() {
        return R.layout.fragment_search;
    }

    @Override
    protected void initViews(View mainView, Bundle savedInstanceState) {
        //显示标题视图
        LinearLayout titleLL = mainView.findViewById(R.id.title_view_parent);
        titleLL.setVisibility(View.VISIBLE);

        //返回
        ImageView backImg = mainView.findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //关闭输入法
                hideInput(mContext, mSearchEditText);

                FragmentReceiver.sendReceiver(mContext, FragmentReceiver.ACTION_CODE_CLOSE_FRAGMENT, null, null);
            }
        });

        //清除输入内容
        final IconfontTextView clearBtn = mainView.findViewById(R.id.clean_img);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchEditText.setText("");
            }
        });

        //搜索按钮
        TextView searchBtn = mainView.findViewById(R.id.right_flag);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchKey = mSearchEditText.getText().toString();
                if (searchKey == null || searchKey.equals("")) {
                    ToastUtil.showTextToast(mContext, getString(R.string.input_key));
                    return;
                }
                doSearch();
            }
        });

        //搜索框
        mSearchEditText = mainView.findViewById(R.id.search);
        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String searchKey = mSearchEditText.getText().toString();
                    if (searchKey == null || searchKey.equals("")) {
                        ToastUtil.showTextToast(mContext, getString(R.string.input_key));
                        return true;
                    }
                    doSearch();
                    return false;
                }
                return false;
            }
        });
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String searchKey = mSearchEditText.getText().toString();
                if (searchKey == null || searchKey.equals("")) {
                    if (clearBtn.getVisibility() != View.INVISIBLE) {
                        clearBtn.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (clearBtn.getVisibility() != View.VISIBLE) {
                        clearBtn.setVisibility(View.VISIBLE);
                    }
                }
            }
        });


        mRecyclerView = mainView.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        //
        mDatas = new ArrayList<AudioInfo>();
        mAdapter = new LRecyclerViewAdapter(new AudioAdapter(mContext, mDatas, SongFragment.SONG_TYPE_SEARCH));
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

    /**
     * 搜索
     */
    private void doSearch() {
        //关闭输入法
        hideInput(mContext, mSearchEditText);
        showLoadingView();
        mWorkerHandler.sendEmptyMessage(LOADREFRESHDATA);
    }

    /**
     * 强制隐藏输入法键盘
     *
     * @param context Context
     * @param view    EditText
     */
    private void hideInput(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    @Override
    protected void preInitStatusBar() {
        setTitleViewId(R.layout.layout_search_title);
        super.preInitStatusBar();
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
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //判断是否有关键字
        String searchKey = mSearchEditText.getText().toString();
        HttpReturnResult httpReturnResult = null;
        if (searchKey == null || searchKey.equals("")) {

            ToastUtil.showTextToast(mContext, getString(R.string.input_key));
            httpReturnResult = new HttpReturnResult();
            httpReturnResult.setStatus(HttpClient.HTTP_OK);
            List<AudioInfo> audioInfos = new ArrayList<AudioInfo>();
            Map<String, Object> returnResult = new HashMap<String, Object>();
            returnResult.put("rows", audioInfos);
            httpReturnResult.setResult(returnResult);

        } else {

            APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
            ConfigInfo configInfo = ConfigInfo.obtain();
            int page = mPage + 1;
            httpReturnResult = apiHttpClient.searchSongList(mContext, searchKey, page, mPageSize, configInfo.isWifi());
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
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        resetPage();

        //判断是否有关键字
        String searchKey = mSearchEditText.getText().toString();
        HttpReturnResult httpReturnResult = null;
        if (searchKey == null || searchKey.equals("")) {

            ToastUtil.showTextToast(mContext, getString(R.string.input_key));
            httpReturnResult = new HttpReturnResult();
            httpReturnResult.setStatus(HttpClient.HTTP_OK);
            List<AudioInfo> audioInfos = new ArrayList<AudioInfo>();
            Map<String, Object> returnResult = new HashMap<String, Object>();
            returnResult.put("rows", audioInfos);
            httpReturnResult.setResult(returnResult);

        } else {
            APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
            ConfigInfo configInfo = ConfigInfo.obtain();
            httpReturnResult = apiHttpClient.searchSongList(mContext, searchKey, mPage, mPageSize, configInfo.isWifi());
        }
        //
        Message msg = Message.obtain();
        msg.what = LOADREFRESHDATA;
        msg.obj = httpReturnResult;
        mUIHandler.sendMessage(msg);
    }

    @Override
    public void onDestroy() {
        if (mAudioBroadcastReceiver != null)
            mAudioBroadcastReceiver.unregisterReceiver(mContext);
        super.onDestroy();
    }

}
