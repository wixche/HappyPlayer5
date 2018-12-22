package com.zlm.hp.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zlm.down.entity.DownloadTask;
import com.zlm.hp.adapter.DownloadMusicAdapter;
import com.zlm.hp.db.util.AudioInfoDB;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.entity.Category;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.receiver.FragmentReceiver;
import com.zlm.hp.ui.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 下载音乐
 * @author: zhangliangming
 * @date: 2018-12-17 21:17
 **/
public class DownloadMusicFragment extends BaseFragment {

    /**
     * 加载数据
     */
    private final int MESSAGE_WHAT_LOADDATA = 0;

    /**
     * 列表视图
     */
    private RecyclerView mRecyclerView;
    private DownloadMusicAdapter mAdapter;
    /**
     * 分类数据
     */
    private ArrayList<Category> mDatas;

    /**
     * 音频广播
     */
    private AudioBroadcastReceiver mAudioBroadcastReceiver;

    public DownloadMusicFragment() {

    }


    @Override
    protected void isFristVisibleToUser() {
        mWorkerHandler.sendEmptyMessageDelayed(MESSAGE_WHAT_LOADDATA, 500);
    }

    /**
     * @return
     */
    public static DownloadMusicFragment newInstance() {
        DownloadMusicFragment fragment = new DownloadMusicFragment();
        return fragment;

    }

    @Override
    protected int setContentLayoutResID() {
        return R.layout.fragment_download;
    }

    @Override
    protected void initViews(View mainView, Bundle savedInstanceState) {
        initView(mainView);
    }

    private void initView(View mainView) {
        //显示标题视图
        LinearLayout titleLL = mainView.findViewById(R.id.title_view_parent);
        titleLL.setVisibility(View.VISIBLE);

        TextView titleView = mainView.findViewById(R.id.title);
        titleView.setText(getString(R.string.tab_download));
        //返回
        ImageView backImg = mainView.findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentReceiver.sendReceiver(mContext, FragmentReceiver.ACTION_CODE_CLOSE_FRAGMENT, null, null);
            }
        });
        //
        mRecyclerView = mainView.findViewById(R.id.recyclerView);
        //初始化内容视图
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

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
                    case AudioBroadcastReceiver.ACTION_CODE_NULL:
                    case AudioBroadcastReceiver.ACTION_CODE_INIT:

                        if (mAdapter != null) {
                            Bundle initBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                            if (initBundle == null) {
                                mAdapter.reshViewHolder(null);
                                return;
                            }
                            AudioInfo initAudioInfo = initBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                            mAdapter.reshViewHolder(initAudioInfo.getHash());
                        }
                        break;
                    case AudioBroadcastReceiver.ACTION_CODE_UPDATE_DOWNLOAD:
                    case AudioBroadcastReceiver.ACTION_CODE_DOWNLOAD_CANCEL:
                    case AudioBroadcastReceiver.ACTION_CODE_DOWNLOAD_FINISH:
                        //重新加载数据
                        mWorkerHandler.sendEmptyMessageDelayed(MESSAGE_WHAT_LOADDATA, 500);
                        break;

                    case AudioBroadcastReceiver.ACTION_CODE_DOWNLOAD_SONG:
                    case AudioBroadcastReceiver.ACTION_CODE_DOWNLOAD_WAIT:
                    case AudioBroadcastReceiver.ACTION_CODE_DOWNLOAD_ERROR:
                    case AudioBroadcastReceiver.ACTION_CODE_DOWNLOAD_PAUSE:
                        //网络歌曲下载
                        Bundle downloadBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                        DownloadTask downloadTask = downloadBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                        String downloadHash = downloadTask.getTaskId();
                        if (downloadTask != null && !TextUtils.isEmpty(downloadHash)) {
                            if (mAdapter != null)
                                mAdapter.reshViewHolder(downloadHash);
                        }

                        break;
                }
            }
        });
        mAudioBroadcastReceiver.registerReceiver(mContext);
        //
        mDatas = new ArrayList<Category>();
        mAdapter = new DownloadMusicAdapter(mContext, mDatas);
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    protected void handleUIMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_WHAT_LOADDATA:

                if (mDatas != null) {
                    mAdapter.resetMenuOpenIndex();
                    mAdapter.notifyDataSetChanged();
                }
                showContentView();

                break;
        }
    }

    @Override
    protected void handleWorkerMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_WHAT_LOADDATA:
                if (mDatas != null) {
                    mDatas.clear();
                }

                //下载中
                Category category = new Category();
                category.setCategoryName(getString(R.string.downloading_text));
                List<AudioInfo> downloadInfos = AudioInfoDB.getDownloadingAudios(mContext);
                category.setCategoryItem(downloadInfos);
                mDatas.add(category);

                //下载完成
                category = new Category();
                category.setCategoryName(getString(R.string.downloaded_text));
                downloadInfos = AudioInfoDB.getDownloadedAudios(mContext);
                category.setCategoryItem(downloadInfos);
                mDatas.add(category);
                //
                mUIHandler.sendEmptyMessage(MESSAGE_WHAT_LOADDATA);
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
