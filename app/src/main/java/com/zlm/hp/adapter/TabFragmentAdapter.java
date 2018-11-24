package com.zlm.hp.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

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
public class TabFragmentAdapter extends FragmentStatePagerAdapter {
    //存储所有的fragment
    private List<Fragment> list;

    public TabFragmentAdapter(FragmentManager fm, ArrayList<Fragment> list) {
        super(fm);
        this.list = list;

    }

    @Override
    public Fragment getItem(int index) {

        return list.get(index);
    }



    @Override
    public int getItemPosition(Object object) {
        //注意：默认是PagerAdapter.POSITION_UNCHANGED，不会重新加载
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public int getCount() {

        return list.size();
    }

}
