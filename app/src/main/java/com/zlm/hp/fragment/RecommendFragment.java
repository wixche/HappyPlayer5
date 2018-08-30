package com.zlm.hp.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.View;

import com.zlm.hp.ui.R;

/**
 * Created by zhangliangming on 2018-08-11.
 */

@SuppressLint("ValidFragment")
public class RecommendFragment extends BaseFragment {

    private boolean isFristVisibleToUser = false;


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
    protected void preInitStatusBar() {
        setStatusBarViewBG(Color.TRANSPARENT);
    }

    @Override
    protected int setContentLayoutResID() {
        return R.layout.fragment_recommend;
    }

    @Override
    protected void initViews(View mainView, Bundle savedInstanceState) {

    }

    @Override
    protected void handleUIMessage(Message msg) {

    }

    @Override
    protected void handleWorkerMessage(Message msg) {

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (!isFristVisibleToUser && isVisibleToUser) {
            isFristVisibleToUser = true;
            showContentView();
        }
    }
}
