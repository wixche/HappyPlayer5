package com.zlm.hp.fragment;


import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zlm.hp.ui.R;

/**
 * @Description: 歌词预览
 * @author: zhangliangming
 * @date: 2018-12-30 23:50
 **/
public class PreviewLrcFragment extends BaseFragment {


    public PreviewLrcFragment() {
    }

    /**
     * @return
     */
    public static PreviewLrcFragment newInstance() {
        PreviewLrcFragment fragment = new PreviewLrcFragment();
        return fragment;
    }

    @Override
    protected void isFristVisibleToUser() {

    }

    @Override
    protected int setContentLayoutResID() {
        return R.layout.fragment_preview_lrc;
    }

    @Override
    protected void preInitStatusBar() {
        setTitleViewId(R.layout.layout_close_title);
        super.preInitStatusBar();
    }

    @Override
    protected void initViews(View mainView, Bundle savedInstanceState) {
        //显示标题视图
        LinearLayout titleLL = mainView.findViewById(R.id.title_view_parent);
        titleLL.setVisibility(View.VISIBLE);

        TextView titleView = mainView.findViewById(R.id.title);
        titleView.setText(getString(R.string.pre_lrc_text));

        showContentView();
    }

    @Override
    protected void handleUIMessage(Message msg) {

    }

    @Override
    protected void handleWorkerMessage(Message msg) {

    }
}
