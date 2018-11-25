package com.zlm.hp.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: tab适配器
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/7/16 20:33
 * @Throws: https://blog.csdn.net/shanshan_1117/article/details/79756399
 */
public class TabFragmentAdapter extends FragmentPagerAdapter {
    //存储所有的fragment
    private List<Class> mDatas;

    public TabFragmentAdapter(FragmentManager fm, ArrayList<Class> datas) {
        super(fm);
        this.mDatas = datas;

    }

    @Override
    public Fragment getItem(int index) {
        try {
            Class clazz = mDatas.get(index);
            Fragment fragment = (Fragment) clazz.newInstance();
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

}
