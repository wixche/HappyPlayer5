package com.zlm.hp.ui;

import android.os.Message;
import android.support.v4.app.Fragment;
import android.os.Bundle;

import com.zlm.hp.adapter.ViewPageFragmentAdapter;
import com.zlm.hp.fragment.EditLrcFragment;
import com.zlm.hp.fragment.MakeLrcFragment;
import com.zlm.hp.fragment.PreviewLrcFragment;
import com.zlm.hp.util.AndroidBug5497WorkaroundUtils;
import com.zlm.hp.widget.CustomViewPager;

import java.util.ArrayList;

/**
 * @Description: 制作歌词界面
 * @author: zhangliangming
 * @date: 2018-12-30 23:16
 **/
public class MakeLrcActivity extends BaseActivity {

    /**
     *
     */
    private CustomViewPager mViewPager;

    /**
     * 编辑界面
     */
    private EditLrcFragment mEditLrcFragment;
    /**
     * 敲打节奏
     */
    private MakeLrcFragment mMakeLrcFragment;

    /**
     * 歌词预览
     */
    private PreviewLrcFragment mPreviewLrcFragment;

    @Override
    protected int setContentLayoutResID() {
        return R.layout.activity_make_lrc;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        mViewPager = findViewById(R.id.viewpage);
        ArrayList<Fragment> fragments = new ArrayList<Fragment>();

        //编辑歌词界面
        mEditLrcFragment = EditLrcFragment.newInstance();
        fragments.add(mEditLrcFragment);

        //敲打节奏

        mMakeLrcFragment = MakeLrcFragment.newInstance();
        fragments.add(mMakeLrcFragment);

        //预览歌词界面
        mPreviewLrcFragment = PreviewLrcFragment.newInstance();
        fragments.add(mPreviewLrcFragment);

        //
        ViewPageFragmentAdapter adapter = new ViewPageFragmentAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(fragments.size());
        mViewPager.setScanScroll(false);

        AndroidBug5497WorkaroundUtils.assistActivity(this);
    }

    @Override
    protected void handleUIMessage(Message msg) {

    }

    @Override
    protected void handleWorkerMessage(Message msg) {

    }
}
