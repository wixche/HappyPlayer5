package com.zlm.hp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.suke.widget.SwitchButton;
import com.zlm.hp.model.ConfigInfo;
import com.zlm.hp.widget.ListItemRelativeLayout;
import com.zlm.libs.widget.SwipeBackLayout;

/**
 * @Description: 设置界面
 * @author: zhangliangming
 * @date: 2018-08-19 9:21
 **/
public class SettingActivity extends BaseActivity {

    /**
     *
     */
    private SwipeBackLayout mSwipeBackLayout;

    /**
     * 问候语开关
     */
    private SwitchButton mHelloSwitchButton;
    /**
     * 线控按钮开关
     */
    private SwitchButton mControlSwitchButton;

    /**
     * 关于
     */
    private ListItemRelativeLayout mAboutLR;

    /**
     * 加载数据
     */
    private final int LOAD_DATA = 1;
    /**
     * 基本数据
     */
    private ConfigInfo mConfigInfo;

    @Override
    protected int setContentLayoutResID() {
        return R.layout.activity_setting;
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
        titleView.setText(getString(R.string.tab_setting));

        //返回
        ImageView backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwipeBackLayout.closeView();
            }
        });

        //关于
        mAboutLR = findViewById(R.id.about_lr);
        mAboutLR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SettingActivity.this, AboutActivity.class);
                startActivity(intent);
                //去掉动画
                overridePendingTransition(0, 0);
            }
        });

        //问候语开关
        mHelloSwitchButton = findViewById(R.id.hello_switch);
        mHelloSwitchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                mConfigInfo.setSayHello(isChecked).save();
            }
        });

        //线控按钮开关
        mControlSwitchButton = findViewById(R.id.control_switch);
        mControlSwitchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                mConfigInfo.setWire(isChecked).save();
            }
        });

        //加载数据
        mWorkerHandler.sendEmptyMessage(LOAD_DATA);
    }

    @Override
    protected void handleUIMessage(Message msg) {
        switch (msg.what) {
            case LOAD_DATA:
                mControlSwitchButton.setChecked(mConfigInfo.isWire());
                mHelloSwitchButton.setChecked(mConfigInfo.isSayHello());
                break;
        }
    }

    @Override
    protected void handleWorkerMessage(Message msg) {
        switch (msg.what) {
            case LOAD_DATA:
                mConfigInfo = ConfigInfo.obtain();
                mUIHandler.sendEmptyMessage(LOAD_DATA);

                break;
        }
    }

    @Override
    public void onBackPressed() {
        mSwipeBackLayout.closeView();
    }
}
