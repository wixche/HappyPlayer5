package com.zlm.hp.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dou361.dialogui.DialogUIUtils;
import com.dou361.dialogui.listener.DialogUIListener;
import com.suke.widget.SwitchButton;
import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.db.util.DownloadThreadInfoDB;
import com.zlm.hp.manager.OnLineAudioManager;
import com.zlm.hp.util.FileUtil;
import com.zlm.hp.util.ResourceUtil;
import com.zlm.hp.widget.ListItemRelativeLayout;
import com.zlm.libs.widget.SwipeBackLayout;

import java.io.File;

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
     * 缓存大小
     */
    private long mCacheSize = 0;
    private TextView mCacheSizeTv;

    /**
     * 加载窗口
     */
    private Dialog mLoadingDialog = null;

    /**
     * 加载数据
     */
    private final int LOAD_DATA = 1;

    /**
     * 更新缓存数据
     */
    private final int UPDATE_CACHESIZE = 2;
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
                if (mLoadingDialog != null)
                    DialogUIUtils.dismiss(mLoadingDialog);
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
        ListItemRelativeLayout aboutLR = findViewById(R.id.about_lr);
        aboutLR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SettingActivity.this, AboutActivity.class);
                startActivity(intent);
                //去掉动画
                overridePendingTransition(0, 0);
            }
        });

        //问候语开关
        ListItemRelativeLayout helloLR = findViewById(R.id.hello_lr);
        helloLR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean flag = mHelloSwitchButton.isChecked();
                mHelloSwitchButton.setChecked(!flag);
            }
        });
        mHelloSwitchButton = findViewById(R.id.hello_switch);
        mHelloSwitchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (mConfigInfo.isSayHello() != isChecked)
                    mConfigInfo.setSayHello(isChecked).save();
            }
        });

        //线控按钮开关
        ListItemRelativeLayout controlLR = findViewById(R.id.control_lr);
        controlLR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean flag = mControlSwitchButton.isChecked();
                mControlSwitchButton.setChecked(!flag);
            }
        });
        mControlSwitchButton = findViewById(R.id.control_switch);
        mControlSwitchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (mConfigInfo.isWire() != isChecked)
                    mConfigInfo.setWire(isChecked).save();
            }
        });

        //帮助
        ListItemRelativeLayout helpLR = findViewById(R.id.help_lr);
        helpLR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, HelpActivity.class);
                startActivity(intent);
                //去掉动画
                overridePendingTransition(0, 0);
            }
        });

        //缓存
        mCacheSizeTv = findViewById(R.id.cache_size_text);
        ListItemRelativeLayout cacheLR = findViewById(R.id.cache_lr);
        cacheLR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tipMsg = getString(R.string.clear_cache_tip);
                DialogUIUtils.showMdAlert(SettingActivity.this, getString(R.string.tip_title), tipMsg, new DialogUIListener() {
                    @Override
                    public void onPositive() {
                        clearAllCache();
                    }

                    @Override
                    public void onNegative() {

                    }

                    @Override
                    public void onCancle() {

                    }
                }).setCancelable(true, false).show();
            }
        });

        //加载数据
        mWorkerHandler.sendEmptyMessage(LOAD_DATA);
    }

    /**
     * 清空所有缓存
     */
    private void clearAllCache() {

        //删除在线缓存
        DownloadThreadInfoDB.deleteAll(mContext, OnLineAudioManager.mThreadNum);

        mWorkerHandler.sendEmptyMessage(UPDATE_CACHESIZE);
        /**
         * 加载框
         *
         * @param context          上下文
         * @param msg              提示文本
         * @param isVertical       true为竖直方向false为水平方向
         * @param cancleable       true为可以取消false为不可取消
         * @param outsideTouchable true为可以点击空白区域false为不可点击
         * @param isWhiteBg        true为白色背景false为灰色背景
         */
        mLoadingDialog = DialogUIUtils.showLoading(SettingActivity.this, getString(R.string.loading_tip), false, false, false, true).show();

    }

    @Override
    protected void handleUIMessage(Message msg) {
        switch (msg.what) {
            case LOAD_DATA:
                mControlSwitchButton.setChecked(mConfigInfo.isWire());
                mHelloSwitchButton.setChecked(mConfigInfo.isSayHello());
                mCacheSizeTv.setText(FileUtil.getFileSize(mCacheSize));
                break;
            case UPDATE_CACHESIZE:
                if (mLoadingDialog != null)
                    DialogUIUtils.dismiss(mLoadingDialog);
                mCacheSizeTv.setText(FileUtil.getFileSize(mCacheSize));
                break;
        }
    }

    @Override
    protected void handleWorkerMessage(Message msg) {
        String cachePath = ResourceUtil.getFilePath(mContext, ResourceConstants.PATH_CACHE, "");
        switch (msg.what) {
            case LOAD_DATA:
                mConfigInfo = ConfigInfo.obtain();


                mCacheSize = FileUtil.getFolderSize(new File(cachePath));

                mUIHandler.sendEmptyMessage(LOAD_DATA);

                break;
            case UPDATE_CACHESIZE:

                FileUtil.deleteFolderFile(cachePath, false);
                mCacheSize = FileUtil.getFolderSize(new File(cachePath));
                mUIHandler.sendEmptyMessageDelayed(UPDATE_CACHESIZE, 500);

                break;
        }
    }

    @Override
    public void onBackPressed() {
        mSwipeBackLayout.closeView();
    }
}
