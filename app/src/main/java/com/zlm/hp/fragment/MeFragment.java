package com.zlm.hp.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;

import com.zlm.hp.ui.R;

/**
 * Created by zhangliangming on 2018-08-11.
 */

@SuppressLint("ValidFragment")
public class MeFragment extends BaseFragment {

    public MeFragment(Activity activity) {
        super(activity);
    }

    @Override
    protected void preInitStatusBar() {
        setStatusBarViewBG(Color.TRANSPARENT);
    }

    @Override
    protected int setContentLayoutResID() {
        return R.layout.fragment_me;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        showContentView();
    }

    @Override
    protected void handleUIMessage(Message msg) {

    }

    @Override
    protected void handleWorkerMessage(Message msg) {

    }
}
