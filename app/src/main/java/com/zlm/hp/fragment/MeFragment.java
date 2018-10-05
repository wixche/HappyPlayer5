package com.zlm.hp.fragment;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zlm.hp.db.util.AudioInfoDB;
import com.zlm.hp.ui.R;

/**
 * Created by zhangliangming on 2018-08-11.
 */

public class MeFragment extends BaseFragment {

    /**
     * 本地音乐
     */
    private LinearLayout mLocalMusic;

    /**
     * 本地音乐个数
     */
    private TextView mLocalCountTv;


    /**
     * 加载本地歌曲的个数
     */
    private final int LOAD_LOCAL_AUDIO_COUNT = 0;

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
        mLocalMusic = mainView.findViewById(R.id.tab_local_music);
        mLocalMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        mLocalCountTv = mainView.findViewById(R.id.local_music_count);
    }

    @Override
    protected void handleUIMessage(Message msg) {
        switch (msg.what) {
            case LOAD_LOCAL_AUDIO_COUNT:

                int count = (int) msg.obj;
                mLocalCountTv.setText(count + "");

                break;
        }
    }

    @Override
    protected void handleWorkerMessage(Message msg) {
        switch (msg.what) {
            case LOAD_LOCAL_AUDIO_COUNT:

                int count = AudioInfoDB.getLocalAudioCount(mContext);
                Message newMsg = Message.obtain();
                newMsg.what = LOAD_LOCAL_AUDIO_COUNT;
                newMsg.obj = count;

                mUIHandler.sendMessage(newMsg);

                break;
        }
    }
}
