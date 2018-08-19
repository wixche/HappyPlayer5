package com.zlm.hp.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.dou361.dialogui.DialogUIUtils;
import com.dou361.dialogui.listener.DialogUIListener;
import com.suke.widget.SwitchButton;
import com.zlm.hp.adapter.TabFragmentAdapter;
import com.zlm.hp.async.AsyncHandlerTask;
import com.zlm.hp.fragment.LastSongFragment;
import com.zlm.hp.fragment.MeFragment;
import com.zlm.hp.fragment.RecommendFragment;
import com.zlm.hp.fragment.SpecialFragment;
import com.zlm.hp.manager.ActivityManager;
import com.zlm.hp.model.ConfigInfo;
import com.zlm.hp.util.AppOpsUtils;
import com.zlm.hp.util.CodeLineUtil;
import com.zlm.hp.util.ColorUtil;
import com.zlm.hp.util.IntentUtils;
import com.zlm.hp.util.StatusBarUtil;
import com.zlm.hp.util.ToastUtil;
import com.zlm.hp.util.ZLog;
import com.zlm.hp.widget.IconfontImageButtonTextView;
import com.zlm.hp.widget.IconfontIndicatorTextView;
import com.zlm.libs.widget.SlidingMenuLayout;

import java.util.ArrayList;

/**
 * @Description: 主界面
 * @author: zhangliangming
 * @date: 2018-07-29 10:21
 **/
public class MainActivity extends BaseActivity {

    /**
     * slidingmenu
     */
    private SlidingMenuLayout mSlidingMenuLayout;
    private SlidingMenuLayout.SlidingMenuOnListener mSlidingMenuOnListener;
    /**
     * 中间视图
     */
    private ViewPager mViewPager;

    /**
     * 图标按钮
     */
    private IconfontImageButtonTextView mIconButton;


    private IconfontImageButtonTextView mSearchButton;

    /**
     * tab菜单图标按钮
     */
    private IconfontIndicatorTextView[] mTabImageButton;

    /**
     * 选中索引
     */
    private int mSelectedIndex = 0;

    /**
     * 保存退出时间
     */
    private long mExitTime;

    /**
     * 设置
     */
    private LinearLayout mSettingLL;

    /**
     * 退出
     */
    private LinearLayout mExitLL;

    /**
     * wifi开关
     */
    private SwitchButton mWifiSwitchButton;

    /**
     * 桌面歌词开关
     */
    private SwitchButton mDesktoplrcSwitchButton;
    /**
     * 锁屏歌词开关
     */
    private SwitchButton mLocklrcSwitchButton;

    /**
     * 基本数据
     */
    private ConfigInfo mConfigInfo;
    /**
     * 加载基本数据
     */
    private final int LOAD_CONFIG_DATA = 1;

    @Override
    protected int setContentLayoutResID() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        initSlidingMenu();
        initTitleViews();
        initViewPage();
        initMenu();
        loadData();
    }

    /**
     * 加载数据
     */
    private void loadData() {
        //加载数据
        mWorkerHandler.sendEmptyMessage(LOAD_CONFIG_DATA);
    }

    @Override
    protected void handleUIMessage(Message msg) {
        switch (msg.what) {
            case LOAD_CONFIG_DATA:
                resetMenuPageData();
                break;
        }
    }

    /**
     * 重新设置menu页面的数据
     */
    private void resetMenuPageData() {
        mWifiSwitchButton.setChecked(mConfigInfo.isWifi());
        mDesktoplrcSwitchButton.setChecked(mConfigInfo.isShowDesktopLrc());
        mLocklrcSwitchButton.setChecked(mConfigInfo.isShowLockScreenLrc());
    }

    @Override
    protected void handleWorkerMessage(Message msg) {
        switch (msg.what) {
            case LOAD_CONFIG_DATA:
                mConfigInfo = ConfigInfo.obtain();
                mUIHandler.sendEmptyMessage(LOAD_CONFIG_DATA);
                break;
        }
    }

    /**
     * 初始化slidingmenu
     */
    private void initSlidingMenu() {
        mSlidingMenuLayout = findViewById(R.id.slidingMenuLayout);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        int screensWidth = displayMetrics.widthPixels;
        int menuViewWidth = screensWidth / 5 * 4;

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, StatusBarUtil.getStatusBarHeight(getApplicationContext()));
        //菜单界面
        LinearLayout menuView = (LinearLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_menu, null);
        FrameLayout.LayoutParams menuLayoutParams = new FrameLayout.LayoutParams(menuViewWidth, FrameLayout.LayoutParams.MATCH_PARENT);
        boolean isAddStatusBar = StatusBarUtil.isAddStatusBar();
        if (isAddStatusBar) {
            View menuStatusBarView = menuView.findViewById(R.id.status_bar_view);
            menuStatusBarView.setVisibility(View.VISIBLE);
            menuStatusBarView.setLayoutParams(lp);
        }

        //主界面
        LinearLayout mainView = (LinearLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_main, null);
        FrameLayout.LayoutParams mainLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        if (isAddStatusBar) {
            View mainStatusBarView = mainView.findViewById(R.id.status_bar_view);
            mainStatusBarView.setBackgroundColor(ColorUtil.parserColor(ContextCompat.getColor(getApplicationContext(), R.color.defColor)));
            mainStatusBarView.setVisibility(View.VISIBLE);
            mainStatusBarView.setLayoutParams(lp);
        }
        //
        mSlidingMenuLayout.setFragmentPaintFade(true);
        mSlidingMenuLayout.onAttachView(menuLayoutParams, menuView, mainLayoutParams, mainView);
        mSlidingMenuOnListener = new SlidingMenuLayout.SlidingMenuOnListener() {
            @Override
            public void addAndShowFragment(Fragment fragment) {
                mSlidingMenuLayout.addAndShowFragment(getSupportFragmentManager(), fragment);
            }

            @Override
            public void hideFragment() {
                mSlidingMenuLayout.hideFragment();
            }
        };
    }

    /**
     * 初始化viewpage
     */
    private void initViewPage() {
        mViewPager = findViewById(R.id.viewpage);

        //
        ArrayList<Fragment> fragments = new ArrayList<Fragment>();
        MeFragment meFragment = new MeFragment(this);
        LastSongFragment lastSongFragment = new LastSongFragment(this);
        RecommendFragment recommendFragment = new RecommendFragment(this);
        SpecialFragment specialFragment = new SpecialFragment(this);

        //
        fragments.add(meFragment);
        fragments.add(lastSongFragment);
        fragments.add(recommendFragment);
        fragments.add(specialFragment);

        TabFragmentAdapter adapter = new TabFragmentAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(fragments.size());
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if (position == 0) {
                    mSlidingMenuLayout.setDragType(SlidingMenuLayout.LEFT_TO_RIGHT);
                } else {
                    mSlidingMenuLayout.setDragType(SlidingMenuLayout.NONE);
                }

                if (position != mSelectedIndex) {
                    mTabImageButton[mSelectedIndex].setSelected(false);
                    mTabImageButton[position].setSelected(true);
                    mSelectedIndex = position;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * 初始化标题栏视图
     */
    private void initTitleViews() {
        //图标
        mIconButton = findViewById(R.id.iconImageButton);
        mIconButton.setConvert(true);
        mIconButton.setPressed(false);
        mIconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSlidingMenuLayout.showMenu();
            }
        });

        //初始化tab菜单

        mTabImageButton = new IconfontIndicatorTextView[4];
        int index = 0;
        //我的tab
        mTabImageButton[index] = findViewById(R.id.myImageButton);
        mTabImageButton[index].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean selected = mTabImageButton[0].isSelected();
                if (!selected) {
                    mViewPager.setCurrentItem(0, true);
                }
            }
        });
        mTabImageButton[index++].setSelected(true);

        //新歌
        mTabImageButton[index] = findViewById(R.id.lastSongImageButton);
        mTabImageButton[index].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean selected = mTabImageButton[1].isSelected();
                if (!selected) {
                    mViewPager.setCurrentItem(1, true);
                }
            }
        });
        mTabImageButton[index++].setSelected(false);

        //排行
        mTabImageButton[index] = findViewById(R.id.recommendImageButton);
        mTabImageButton[index].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean selected = mTabImageButton[2].isSelected();
                if (!selected) {
                    mViewPager.setCurrentItem(2, true);
                }
            }
        });
        mTabImageButton[index++].setSelected(false);


        //歌单
        mTabImageButton[index] = findViewById(R.id.specialImageButton);
        mTabImageButton[index].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean selected = mTabImageButton[3].isSelected();
                if (!selected) {
                    mViewPager.setCurrentItem(3, true);
                }
            }
        });
        mTabImageButton[index++].setSelected(false);


        //搜索
        mSearchButton = findViewById(R.id.searchImageButton);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });
        mSearchButton.setConvert(true);
        mSearchButton.setPressed(false);

        new AsyncHandlerTask(getApplicationContext()).execute(mUIHandler, mWorkerHandler, new AsyncHandlerTask.WorkerRunnable() {
            @Override
            public void run() {
                String ee = "";
                ZLog.e(new CodeLineUtil().getCodeLineInfo(), ee);
                //通知执行回调任务
                runCallBackTask();
            }
        }, new AsyncHandlerTask.Callback() {
            @Override
            public void runOnUiThread() {
                String ee = "";
                ZLog.e(new CodeLineUtil().getCodeLineInfo(), ee);
            }
        });
    }

    /**
     * 初始化菜单栏
     */
    private void initMenu() {
        //设置
        mSettingLL = findViewById(R.id.setting_ll);
        mSettingLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                //去掉动画
                overridePendingTransition(0, 0);
            }
        });

        //退出
        mExitLL = findViewById(R.id.exit_ll);
        mExitLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tipMsg = getString(R.string.exit_app_tip);
                DialogUIUtils.showMdAlert(MainActivity.this, null, tipMsg, new DialogUIListener() {
                    @Override
                    public void onPositive() {
                        ActivityManager.getInstance().exit();
                    }

                    @Override
                    public void onNegative() {

                    }
                }).setCancelable(true, false).show();
            }
        });

        //wifi开关
        mWifiSwitchButton = findViewById(R.id.wifi_switch);
        mWifiSwitchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                mConfigInfo.setWifi(isChecked).save();
            }
        });

        //桌面歌词开关
        mDesktoplrcSwitchButton = findViewById(R.id.desktoplrc_switch);
        mDesktoplrcSwitchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked) {
                    if (!AppOpsUtils.allowFloatWindow(getApplication())) {

                        String tipMsg = getString(R.string.desktoplrc_tip);
                        DialogUIUtils.showMdAlert(MainActivity.this, null, tipMsg, new DialogUIListener() {
                            @Override
                            public void onPositive() {
                                //跳转权限设置页面
                                IntentUtils.gotoPermissionSetting(MainActivity.this);
                                mDesktoplrcSwitchButton.setChecked(false);
                            }

                            @Override
                            public void onNegative() {
                                mDesktoplrcSwitchButton.setChecked(false);
                            }

                            @Override
                            public void onCancle() {
                                mDesktoplrcSwitchButton.setChecked(false);
                            }
                        }).setCancelable(true, false).show();
                        return;
                    }
                }
                mConfigInfo.setShowDesktopLrc(isChecked).save();
            }
        });

        //锁屏歌词开关
        mLocklrcSwitchButton = findViewById(R.id.locklrc_switch);
        mLocklrcSwitchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                mConfigInfo.setShowLockScreenLrc(isChecked).save();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mSlidingMenuLayout.isShowingFragment()) {
            mSlidingMenuLayout.hideFragment();
        } else if (mSlidingMenuLayout.isShowingMenu()) {
            mSlidingMenuLayout.hideMenu();
        } else {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                ToastUtil.showTextToast(getApplicationContext(), getString(R.string.back_tip));
                mExitTime = System.currentTimeMillis();
            } else {
                // 跳转到桌面
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            }
        }
    }

}
