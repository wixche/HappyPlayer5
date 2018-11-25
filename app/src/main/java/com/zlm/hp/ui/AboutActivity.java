package com.zlm.hp.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zlm.hp.util.ApkUtil;
import com.zlm.libs.widget.SwipeBackLayout;

import java.util.Calendar;

/**
 * @Description: 关于
 * @author: zhangliangming
 * @date: 2018-08-19 12:08
 **/
public class AboutActivity extends BaseActivity {
    /**
     * 加载版本号
     */
    private final int LOAD_VERSION = 1;

    /**
     *
     */
    private SwipeBackLayout mSwipeBackLayout;
    /**
     * 版本
     */
    private TextView mVersionTV;

    /**
     * 版权
     */
    private TextView mCopyrightTV;

    @Override
    protected int setContentLayoutResID() {
        return R.layout.activity_about;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        mSwipeBackLayout = findViewById(R.id.swipeback_layout);
        mSwipeBackLayout.setSwipeBackLayoutListener(new SwipeBackLayout.SwipeBackLayoutListener() {

            @Override
            public void finishActivity() {
                finish();
                overridePendingTransition(0, 0);
            }
        });

        TextView titleView = findViewById(R.id.title);
        titleView.setText(getString(R.string.about_app));

        //返回
        ImageView backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwipeBackLayout.closeView();
            }
        });

        //版本号文本控件
        mVersionTV = findViewById(R.id.app_name_version_name);
        mCopyrightTV = findViewById(R.id.copyright);

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        String copyright = getString(R.string.copyright);
        mCopyrightTV.setText(String.format(copyright,year + ""));

        //加载数据
        mWorkerHandler.sendEmptyMessage(LOAD_VERSION);
    }

    @SuppressLint("StringFormatMatches")
    @Override
    protected void handleUIMessage(Message msg) {
        switch (msg.what) {
            case LOAD_VERSION:

                String versionInfo = (String) msg.obj;
                mVersionTV.setText(versionInfo);

                break;
        }
    }

    @Override
    protected void handleWorkerMessage(Message msg) {
        switch (msg.what) {
            case LOAD_VERSION:

                loadVersionData();

                break;
        }
    }

    /**
     * 加载版本号数据
     */
    private void loadVersionData() {
        String versionInfo = getString(R.string.app_name) + " " + ApkUtil.getVersionName(getApplicationContext());
        Message msg = Message.obtain();
        msg.what = LOAD_VERSION;
        msg.obj = versionInfo;
        mUIHandler.sendMessage(msg);
    }

    @Override
    public void onBackPressed() {
        mSwipeBackLayout.closeView();
    }
}
