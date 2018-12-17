package com.zlm.hp.fragment;


import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlm.hp.receiver.FragmentReceiver;
import com.zlm.hp.ui.R;

/**
 * @Description: 下载音乐
 * @author: zhangliangming
 * @date: 2018-12-17 21:17
 **/
public class DownloadMusicFragment extends BaseFragment {

    public DownloadMusicFragment() {

    }


    @Override
    protected void isFristVisibleToUser() {

    }

    /**
     * @return
     */
    public static DownloadMusicFragment newInstance() {
        DownloadMusicFragment fragment = new DownloadMusicFragment();
        return fragment;

    }

    @Override
    protected int setContentLayoutResID() {
        return R.layout.fragment_download;
    }

    @Override
    protected void initViews(View mainView, Bundle savedInstanceState) {
        initView(mainView);
    }

    private void initView(View mainView) {
        //显示标题视图
        RelativeLayout titleRL = mainView.findViewById(R.id.title_view);
        titleRL.setVisibility(View.VISIBLE);

        TextView titleView = mainView.findViewById(R.id.title);
        titleView.setText(getString(R.string.tab_download));
        //返回
        ImageView backImg = mainView.findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentReceiver.sendReceiver(mContext, FragmentReceiver.ACTION_CODE_CLOSE_FRAGMENT, null, null);
            }
        });
    }

    @Override
    protected void handleUIMessage(Message msg) {

    }

    @Override
    protected void handleWorkerMessage(Message msg) {

    }
}
