package com.zlm.hp.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zlm.hp.entity.VideoInfo;

/**
 * @Description: 视频
 * @author: zhangliangming
 * @date: 2019-01-05 21:01
 **/
public class VideoActivity extends BaseActivity {

    /**
     * 视频信息
     */
    private VideoInfo mVideoInfo;

    @Override
    protected void preInitStatusBar() {
        setStatusBarViewBG(Color.TRANSPARENT);
        super.preInitStatusBar();
    }

    @Override
    protected int setContentLayoutResID() {
        return R.layout.activity_video;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        TextView titleView = findViewById(R.id.title);

        //视频信息
        mVideoInfo = getIntent().getParcelableExtra(VideoInfo.DATA_KEY);
        titleView.setText(mVideoInfo.getMvName());

        //返回
        ImageView backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
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
