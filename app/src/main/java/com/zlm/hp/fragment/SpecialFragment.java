package com.zlm.hp.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.View;

import com.zlm.hp.ui.R;

/**
 * 歌单
 * Created by zhangliangming on 2018-08-11.
 */

public class SpecialFragment extends BaseFragment {

    public SpecialFragment() {

    }

    /**
     * @return
     */
    public static SpecialFragment newInstance() {
        SpecialFragment fragment = new SpecialFragment();
        return fragment;

    }

    @Override
    protected void preInitStatusBar() {
        setStatusBarViewBG(Color.TRANSPARENT);
    }

    @Override
    protected int setContentLayoutResID() {
        return R.layout.fragment_special;
    }

    @Override
    protected void initViews(View mainView, Bundle savedInstanceState) {
        showContentView();
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
