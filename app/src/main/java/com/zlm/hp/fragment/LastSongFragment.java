package com.zlm.hp.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.View;

import com.zlm.hp.ui.R;

/**
 * 新歌
 * Created by zhangliangming on 2018-08-11.
 */

public class LastSongFragment extends BaseFragment {


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
        setStatusBarViewBG(Color.TRANSPARENT);
    }

    @Override
    protected int setContentLayoutResID() {
        return R.layout.fragment_last_song;
    }

    @Override
    protected void initViews(View mainView, Bundle savedInstanceState) {
        showLoadingView();
    }

    @Override
    protected void handleUIMessage(Message msg) {

    }

    @Override
    protected void handleWorkerMessage(Message msg) {

    }

    @Override
    protected void isFristVisibleToUser() {
        showContentView();
    }
}
