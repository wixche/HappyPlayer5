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
import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.fragment.LastSongFragment;
import com.zlm.hp.fragment.MeFragment;
import com.zlm.hp.fragment.NetSongFragment;
import com.zlm.hp.fragment.RecommendFragment;
import com.zlm.hp.fragment.SpecialFragment;
import com.zlm.hp.manager.ActivityManager;
import com.zlm.hp.receiver.FragmentReceiver;
import com.zlm.hp.util.AppBarUtil;
import com.zlm.hp.util.AppOpsUtils;
import com.zlm.hp.util.CodeLineUtil;
import com.zlm.hp.util.ColorUtil;
import com.zlm.hp.util.IntentUtil;
import com.zlm.hp.util.ToastUtil;
import com.zlm.hp.util.ZLog;
import com.zlm.hp.widget.IconfontImageButtonTextView;
import com.zlm.hp.widget.IconfontIndicatorTextView;
import com.zlm.hp.widget.WhiteTranRelativeLayout;
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
     *
     */
    private LinearLayout mPlayerBarLL;

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
    private int mSelectedIndex = 1;

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

    private WhiteTranRelativeLayout mWifiLR;

    /**
     * wifi开关
     */
    private SwitchButton mWifiSwitchButton;

    private WhiteTranRelativeLayout mDesktoplrcLR;
    /**
     * 桌面歌词开关
     */
    private SwitchButton mDesktoplrcSwitchButton;

    private WhiteTranRelativeLayout mLocklrcLR;
    /**
     * 锁屏歌词开关
     */
    private SwitchButton mLocklrcSwitchButton;

    /**
     *
     */
    private FragmentReceiver mFragmentReceiver;

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
        initViewPage();
        initTitleViews();
        initMenu();
        initReceiver();
        loadData();
    }


    /**
     * 初始化广播
     */
    private void initReceiver() {
        mFragmentReceiver = new FragmentReceiver(mContext);
        mFragmentReceiver.setFragmentReceiverListener(new FragmentReceiver.FragmentReceiverListener() {
            @Override
            public void onReceive(Context context, Intent intent, int code) {
                handleFragmentReceiver(intent, code);
            }

            /**
             * 处理fragment
             * @param intent
             * @param code
             */
            private void handleFragmentReceiver(Intent intent, int code) {

                switch (code) {
                    case FragmentReceiver.ACTION_CODE_OPEN_RECOMMENDFRAGMENT:
                        //排行
                        Bundle recommendBundle = intent.getBundleExtra(NetSongFragment.ARGUMENTS_KEY);
                        NetSongFragment recommendSongFragment = NetSongFragment.newInstance();
                        recommendBundle.putInt(NetSongFragment.NETSONGTYPE_KEY, NetSongFragment.NET_SONG_TYPE_RECOMMEND);

                        recommendSongFragment.setArguments(recommendBundle);
                        mSlidingMenuOnListener.addAndShowFragment(recommendSongFragment);

                        break;
                    case FragmentReceiver.ACTION_CODE_OPEN_SPECIALFRAGMENT:
                        //歌单
                        Bundle specialBundle = intent.getBundleExtra(NetSongFragment.ARGUMENTS_KEY);
                        NetSongFragment specialSongFragment = NetSongFragment.newInstance();
                        specialBundle.putInt(NetSongFragment.NETSONGTYPE_KEY, NetSongFragment.NET_SONG_TYPE_SPECIAL);
                        specialSongFragment.setArguments(specialBundle);

                        mSlidingMenuOnListener.addAndShowFragment(specialSongFragment);
                        break;
                    case FragmentReceiver.ACTION_CODE_CLOSE_FRAGMENT:
                        mSlidingMenuOnListener.hideFragment();
                        break;
                }
            }
        });
        mFragmentReceiver.registerReceiver(mContext);
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
        int menuViewWidth = screensWidth / 4 * 3;

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, AppBarUtil.getStatusBarHeight(getApplicationContext()));
        //菜单界面
        LinearLayout menuView = (LinearLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_menu, null);
        FrameLayout.LayoutParams menuLayoutParams = new FrameLayout.LayoutParams(menuViewWidth, FrameLayout.LayoutParams.MATCH_PARENT);
        boolean isAddStatusBar = AppBarUtil.isAddStatusBar();
        if (isAddStatusBar) {
            View menuStatusBarView = menuView.findViewById(R.id.status_bar_view);
            menuStatusBarView.setVisibility(View.VISIBLE);
            menuStatusBarView.setLayoutParams(lp);
        }

        //主界面
        LinearLayout mainView = (LinearLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_main, null);
        FrameLayout.LayoutParams mainLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mPlayerBarLL = findViewById(R.id.playerBar);
        //mSlidingMenuLayout.addIgnoreHorizontalView(mPlayerBarLL);
        mSlidingMenuLayout.addOnPageChangeListener(new SlidingMenuLayout.OnPageChangeListener() {
            @Override
            public void onMainPageScrolled(int leftx) {
                mPlayerBarLL.setTranslationX(leftx);
            }
        });

        mViewPager = mainView.findViewById(R.id.viewpage);

        //添加状态栏
        if (isAddStatusBar) {
            View mainStatusBarView = mainView.findViewById(R.id.status_bar_view);
            mainStatusBarView.setBackgroundColor(ColorUtil.parserColor(ContextCompat.getColor(getApplicationContext(), R.color.defColor)));
            mainStatusBarView.setVisibility(View.VISIBLE);
            mainStatusBarView.setLayoutParams(lp);
        }

        //
        mSlidingMenuLayout.setFragmentPaintFade(true);
        mSlidingMenuLayout.setAllowScale(false);
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

        ArrayList<Fragment> fragments = new ArrayList<Fragment>();
        MeFragment meFragment = MeFragment.newInstance();
        LastSongFragment lastSongFragment = LastSongFragment.newInstance();
        RecommendFragment recommendFragment = RecommendFragment.newInstance();
        SpecialFragment specialFragment = SpecialFragment.newInstance();

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
        mViewPager.setCurrentItem(mSelectedIndex);
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
        mTabImageButton[index++].setSelected(false);

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

        new AsyncHandlerTask(mUIHandler, mWorkerHandler).execute(new AsyncHandlerTask.Task() {
            @Override
            protected Object doInBackground() {
                String ee = "";
                ZLog.e(new CodeLineUtil().getCodeLineInfo(), ee);
                return "success";
            }

            @Override
            protected void onPostExecute(Object result) {
                ZLog.e(new CodeLineUtil().getCodeLineInfo(), result.toString());
            }
        });

        mTabImageButton[mSelectedIndex].setSelected(true);
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
                DialogUIUtils.showMdAlert(MainActivity.this, getString(R.string.tip_title), tipMsg, new DialogUIListener() {
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
        mWifiLR = findViewById(R.id.wifi_lr);
        mWifiLR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean flag = mWifiSwitchButton.isChecked();
                mWifiSwitchButton.setChecked(!flag);
            }
        });
        mWifiSwitchButton = findViewById(R.id.wifi_switch);
        mWifiSwitchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (mConfigInfo.isWifi() != isChecked)
                    mConfigInfo.setWifi(isChecked).save();
            }
        });

        //桌面歌词开关
        mDesktoplrcLR = findViewById(R.id.desktoplrc_lr);
        mDesktoplrcLR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean flag = mDesktoplrcSwitchButton.isChecked();
                mDesktoplrcSwitchButton.setChecked(!flag);
            }
        });
        mDesktoplrcSwitchButton = findViewById(R.id.desktoplrc_switch);
        mDesktoplrcSwitchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked) {
                    if (!AppOpsUtils.allowFloatWindow(getApplication())) {

                        String tipMsg = getString(R.string.desktoplrc_tip);
                        DialogUIUtils.showMdAlert(MainActivity.this, getString(R.string.tip_title), tipMsg, new DialogUIListener() {
                            @Override
                            public void onPositive() {
                                //跳转权限设置页面
                                IntentUtil.gotoPermissionSetting(MainActivity.this);
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
                if (mConfigInfo.isShowDesktopLrc() != isChecked)
                    mConfigInfo.setShowDesktopLrc(isChecked).save();
            }
        });

        //锁屏歌词开关
        mLocklrcLR = findViewById(R.id.locklrc_lr);
        mLocklrcLR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean flag = mLocklrcSwitchButton.isChecked();
                mLocklrcSwitchButton.setChecked(!flag);
            }
        });
        mLocklrcSwitchButton = findViewById(R.id.locklrc_switch);
        mLocklrcSwitchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (mConfigInfo.isShowLockScreenLrc() != isChecked)
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

    @Override
    protected void onDestroy() {
        destroyReceiver();
        super.onDestroy();
    }

    private void destroyReceiver() {
        if (mFragmentReceiver != null) {
            mFragmentReceiver.unregisterReceiver(mContext);
        }
    }
}
