package com.zlm.hp.adapter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.entity.LrcInfo;
import com.zlm.hp.fragment.LrcFragment;

import java.util.List;

/**
 * @Description: tab适配器
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/7/16 20:33
 * @Throws: https://blog.csdn.net/shanshan_1117/article/details/79756399
 */
public class SearchLrcFragmentAdapter extends FragmentStatePagerAdapter {
    //存储所有的fragment
    private List<LrcInfo> mDatas;
    private AudioInfo mAudioInfo;
    private Fragment mCurrentFragment;

    public SearchLrcFragmentAdapter(FragmentManager fm, AudioInfo audioInfo, List<LrcInfo> datas) {
        super(fm);
        this.mDatas = datas;
        this.mAudioInfo = audioInfo;
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        mCurrentFragment = (Fragment) object;
        super.setPrimaryItem(container, position, object);
    }

    @Override
    public Fragment getItem(int index) {
        try {
            LrcInfo lrcInfo = mDatas.get(index);

            Class clazz = LrcFragment.class;
            LrcFragment fragment = (LrcFragment) clazz.newInstance();
            Bundle bundle = new Bundle();
            bundle.putParcelable(LrcFragment.AUDIO_DATA_KEY, mAudioInfo);
            bundle.putParcelable(LrcFragment.LRC_DATA_KEY, lrcInfo);
            fragment.setArguments(bundle);

            return fragment;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    public Fragment getCurrentFragment() {
        return mCurrentFragment;
    }
}
