package com.zlm.hp.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dou361.dialogui.DialogUIUtils;
import com.dou361.dialogui.listener.DialogUIListener;
import com.suke.widget.SwitchButton;
import com.zlm.down.entity.DownloadTask;
import com.zlm.hp.adapter.TabFragmentAdapter;
import com.zlm.hp.async.AsyncHandlerTask;
import com.zlm.hp.audio.utils.MediaUtil;
import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.db.util.DownloadThreadInfoDB;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.fragment.LastSongFragment;
import com.zlm.hp.fragment.MeFragment;
import com.zlm.hp.fragment.RecommendFragment;
import com.zlm.hp.fragment.SongFragment;
import com.zlm.hp.fragment.SpecialFragment;
import com.zlm.hp.manager.ActivityManager;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.manager.LyricsManager;
import com.zlm.hp.manager.OnLineAudioManager;
import com.zlm.hp.receiver.AppSystemReceiver;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.receiver.FragmentReceiver;
import com.zlm.hp.receiver.PhoneReceiver;
import com.zlm.hp.receiver.PhoneV4Receiver;
import com.zlm.hp.service.AudioPlayerService;
import com.zlm.hp.util.AppBarUtil;
import com.zlm.hp.util.AppOpsUtils;
import com.zlm.hp.util.ColorUtil;
import com.zlm.hp.util.ImageUtil;
import com.zlm.hp.util.IntentUtil;
import com.zlm.hp.util.ToastUtil;
import com.zlm.hp.widget.IconfontImageButtonTextView;
import com.zlm.hp.widget.IconfontIndicatorTextView;
import com.zlm.hp.widget.WhiteTranRelativeLayout;
import com.zlm.libs.widget.MusicSeekBar;
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
     * 歌手头像
     */
    private ImageView mArtistImageView;

    /**
     * 歌曲名称tv
     */
    private TextView mSongNameTextView;
    /**
     * 歌手tv
     */
    private TextView mSingerNameTextView;
    /**
     * 播放按钮
     */
    private ImageView mPlayImageView;
    /**
     * 暂停按钮
     */
    private ImageView mPauseImageView;
    /**
     * 下一首按钮
     */
    private ImageView mNextImageView;
    /**
     * 歌曲进度
     */
    private MusicSeekBar mMusicSeekBar;

    /**
     * 基本数据
     */
    private ConfigInfo mConfigInfo;
    /**
     * 加载基本数据
     */
    private final int LOAD_CONFIG_DATA = 1;


    /**
     *
     */
    private FragmentReceiver mFragmentReceiver;
    /**
     * 音频广播
     */
    private AudioBroadcastReceiver mAudioBroadcastReceiver;
    /**
     * app系统广播
     */
    private AppSystemReceiver mAppSystemReceiver;

    /**
     * 线控 5.0以下
     */
    private PhoneV4Receiver mPhoneV4Receiver;

    /**
     * 线控 5.0以上
     */
    private PhoneReceiver mPhoneReceiver;


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
        initPlayBarViews();
        initReceiver();
        initService();
        loadData();
    }


    /**
     * 初始服务
     */
    private void initService() {
        AudioPlayerService.startService(this);
    }


    /**
     * 初始化广播
     */
    private void initReceiver() {

        //fragment广播
        mFragmentReceiver = new FragmentReceiver(mContext);
        mFragmentReceiver.setReceiverListener(new FragmentReceiver.FragmentReceiverListener() {
            @Override
            public void onReceive(Context context, final Intent intent, final int code) {
                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        handleFragmentReceiver(intent, code);
                    }
                });
            }

            /**
             * 处理fragment
             * @param intent
             * @param code
             */
            private void handleFragmentReceiver(final Intent intent, int code) {

                switch (code) {
                    case FragmentReceiver.ACTION_CODE_OPEN_RECOMMENDFRAGMENT:

                        //排行
                        Bundle recommendBundle = intent.getBundleExtra(SongFragment.ARGUMENTS_KEY);
                        SongFragment recommendSongFragment = SongFragment.newInstance();
                        recommendSongFragment.setArguments(recommendBundle);
                        mSlidingMenuOnListener.addAndShowFragment(recommendSongFragment);


                        break;
                    case FragmentReceiver.ACTION_CODE_OPEN_SPECIALFRAGMENT:
                    case FragmentReceiver.ACTION_CODE_OPEN_LOCALFRAGMENT:

                        Bundle bundle = intent.getBundleExtra(SongFragment.ARGUMENTS_KEY);
                        SongFragment songFragment = SongFragment.newInstance();
                        songFragment.setArguments(bundle);

                        mSlidingMenuOnListener.addAndShowFragment(songFragment);
                        break;

                    case FragmentReceiver.ACTION_CODE_CLOSE_FRAGMENT:

                        mSlidingMenuOnListener.hideFragment();

                        break;
                }
            }
        });
        mFragmentReceiver.registerReceiver(mContext);

        //音频广播
        mAudioBroadcastReceiver = new AudioBroadcastReceiver();
        mAudioBroadcastReceiver.setReceiverListener(new AudioBroadcastReceiver.AudioReceiverListener() {
            @Override
            public void onReceive(Context context, final Intent intent, final int code) {
                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        handleAudioBroadcastReceiver(intent, code);
                    }
                });
            }

            private void handleAudioBroadcastReceiver(Intent intent, int code) {
                switch (code) {
                    case AudioBroadcastReceiver.ACTION_CODE_NULL:

                        //空数据
                        mSongNameTextView.setText(R.string.def_songName);
                        mSingerNameTextView.setText(R.string.def_artist);
                        mPauseImageView.setVisibility(View.INVISIBLE);
                        mPlayImageView.setVisibility(View.VISIBLE);

                        //
                        mMusicSeekBar.setEnabled(false);
                        mMusicSeekBar.setProgress(0);
                        mMusicSeekBar.setSecondaryProgress(0);
                        mMusicSeekBar.setMax(0);

                        //
                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.bpz);
                        mArtistImageView.setImageDrawable(new BitmapDrawable(bitmap));
                        mArtistImageView.setTag("");

                        //重置额外歌词状态
                        mConfigInfo.setExtraLrcStatus(ConfigInfo.EXTRALRCSTATUS_NOSHOWEXTRALRC);

                        break;
                    case AudioBroadcastReceiver.ACTION_CODE_INIT:
                        Bundle initBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                        AudioInfo initAudioInfo = initBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                        if (initAudioInfo != null) {
                            mSongNameTextView.setText(initAudioInfo.getSongName());
                            mSingerNameTextView.setText(initAudioInfo.getSingerName());
                            mPauseImageView.setVisibility(View.INVISIBLE);
                            mPlayImageView.setVisibility(View.VISIBLE);

                            //设置进度条
                            mMusicSeekBar.setEnabled(true);
                            mMusicSeekBar.setMax((int) initAudioInfo.getDuration());
                            mMusicSeekBar.setProgress((int) initAudioInfo.getPlayProgress());
                            mMusicSeekBar.setSecondaryProgress(0);

                            //加载歌手头像
                            ImageUtil.loadSingerImage(mContext, mArtistImageView, initAudioInfo.getSingerName(), mConfigInfo.isWifi(), 400, 400, new AsyncHandlerTask(mUIHandler, mWorkerHandler), null);

                            //加载歌词
                            String keyWords = "";
                            if (initAudioInfo.getSingerName().equals(getString(R.string.unknow))) {
                                keyWords = initAudioInfo.getSongName();
                            } else {
                                keyWords = initAudioInfo.getTitle();
                            }
                            LyricsManager.newInstance(mContext).loadLyrics(keyWords, keyWords, initAudioInfo.getDuration() + "", initAudioInfo.getHash(), mConfigInfo.isWifi(), new AsyncHandlerTask(mUIHandler, mWorkerHandler), null);
                        }
                        break;
                    case AudioBroadcastReceiver.ACTION_CODE_PLAY:
                        if (mPauseImageView.getVisibility() != View.VISIBLE)
                            mPauseImageView.setVisibility(View.VISIBLE);

                        if (mPlayImageView.getVisibility() != View.INVISIBLE)
                            mPlayImageView.setVisibility(View.INVISIBLE);

                        break;
                    case AudioBroadcastReceiver.ACTION_CODE_PLAYING:

                        Bundle playBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                        AudioInfo playingAudioInfo = playBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                        if (playingAudioInfo != null) {
                            mMusicSeekBar.setProgress((int) playingAudioInfo.getPlayProgress());
                        }

                        break;
                    case AudioBroadcastReceiver.ACTION_CODE_STOP:
                        //暂停完成
                        if (mPauseImageView.getVisibility() != View.INVISIBLE)
                            mPauseImageView.setVisibility(View.INVISIBLE);

                        if (mPlayImageView.getVisibility() != View.VISIBLE)
                            mPlayImageView.setVisibility(View.VISIBLE);

                        break;

                    case AudioBroadcastReceiver.ACTION_CODE_SEEKTO:
                        Bundle seektoBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                        AudioInfo seektoAudioInfo = seektoBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                        if (seektoAudioInfo != null) {
                            mMusicSeekBar.setProgress(seektoAudioInfo.getPlayProgress());
                        }
                        break;

                    case AudioBroadcastReceiver.ACTION_CODE_DOWNLOADONLINESONG:
                        //网络歌曲下载中
                        Bundle downloadOnlineSongBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                        DownloadTask downloadingTask = downloadOnlineSongBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                        String hash = mConfigInfo.getPlayHash();
                        AudioInfo audioInfo = AudioPlayerManager.newInstance(mContext).getCurSong(hash);
                        if (audioInfo != null && downloadingTask != null && !TextUtils.isEmpty(hash) && hash.equals(downloadingTask.getTaskId())) {
                            int downloadedSize = DownloadThreadInfoDB.getDownloadedSize(mContext, downloadingTask.getTaskId(), OnLineAudioManager.threadNum);
                            double pre = downloadedSize * 1.0 / audioInfo.getFileSize();
                            int downloadProgress = (int) (mMusicSeekBar.getMax() * pre);
                            mMusicSeekBar.setSecondaryProgress(downloadProgress);
                        }

                        break;
                }
            }
        });
        mAudioBroadcastReceiver.registerReceiver(mContext);

        //系统
        mAppSystemReceiver = new AppSystemReceiver();
        mAppSystemReceiver.setReceiverListener(new AppSystemReceiver.AppSystemReceiverListener() {
            @Override
            public void onReceive(Context context, final Intent intent, final int code) {
                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        handleAppSystemBroadcastReceiver(intent, code);
                    }
                });
            }

            private void handleAppSystemBroadcastReceiver(Intent intent, int code) {
                switch (code) {
                    case AppSystemReceiver.ACTION_CODE_TOAST_ERRORMSG:
                        Bundle toastErrorMSGBundle = intent.getBundleExtra(AppSystemReceiver.ACTION_BUNDLEKEY);
                        String msg = toastErrorMSGBundle.getString(AppSystemReceiver.ACTION_DATA_KEY);
                        ToastUtil.showTextToast(mContext, msg);

                        break;
                }
            }
        });
        mAppSystemReceiver.registerReceiver(mContext);

        //线控
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPhoneReceiver = new PhoneReceiver(mContext);
            mPhoneReceiver.registerReceiver(mContext);
        } else {
            mPhoneV4Receiver = new PhoneV4Receiver(mContext);
            mPhoneV4Receiver.registerReceiver(mContext);
        }
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
                AudioPlayerManager.newInstance(mContext).init();

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
        mPlayerBarLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSlidingMenuLayout.isShowingMenu()) {
                    mSlidingMenuLayout.hideMenu();
                    return;
                }

                Intent intent = new Intent(MainActivity.this, LrcActivity.class);
                startActivity(intent);
                //去掉动画
                overridePendingTransition(0, 0);
            }
        });
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

        ArrayList<Class> fragmentsClass = new ArrayList<Class>();
        //
        fragmentsClass.add(MeFragment.class);
        fragmentsClass.add(LastSongFragment.class);
        fragmentsClass.add(RecommendFragment.class);
        fragmentsClass.add(SpecialFragment.class);

        TabFragmentAdapter adapter = new TabFragmentAdapter(getSupportFragmentManager(), fragmentsClass);
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(fragmentsClass.size());
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

    /**
     * 初始化底部bar视图
     */
    private void initPlayBarViews() {
        mArtistImageView = findViewById(R.id.play_bar_artist);
        //
        mSongNameTextView = findViewById(R.id.songName);
        mSingerNameTextView = findViewById(R.id.singerName);
        //播放
        mPlayImageView = findViewById(R.id.bar_play);
        mPlayImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioPlayerManager.newInstance(mContext).play(mMusicSeekBar.getProgress());
            }
        });
        //暂停
        mPauseImageView = findViewById(R.id.bar_pause);
        mPauseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioPlayerManager.newInstance(mContext).pause();
            }
        });
        //下一首
        mNextImageView = findViewById(R.id.bar_next);
        mNextImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioPlayerManager.newInstance(mContext).next();
            }
        });

        mMusicSeekBar = findViewById(R.id.seekBar);
        mMusicSeekBar.setOnMusicListener(new MusicSeekBar.OnMusicListener() {
            @Override
            public String getTimeText() {
                return MediaUtil.formatTime(mMusicSeekBar.getProgress());
            }

            @Override
            public String getLrcText() {

                return null;
            }

            @Override
            public void onProgressChanged(MusicSeekBar musicSeekBar) {

            }

            @Override
            public void onTrackingTouchStart(MusicSeekBar musicSeekBar) {

            }

            @Override
            public void onTrackingTouchFinish(MusicSeekBar musicSeekBar) {
                int progress = mMusicSeekBar.getProgress();
                AudioInfo audioInfo = AudioPlayerManager.newInstance(mContext).getCurSong(mConfigInfo.getPlayHash());
                if (audioInfo != null && progress <= audioInfo.getDuration()) {
                    audioInfo.setPlayProgress(progress);
                    AudioPlayerManager.newInstance(mContext).seekto(audioInfo);
                }
            }
        });

        //播放列表按钮
        ImageView listMenuImg = findViewById(R.id.list_menu);
        listMenuImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
        releaseData();
        destroyService();
        destroyReceiver();
        super.onDestroy();
    }

    /**
     * 销毁服务
     */
    private void destroyService() {
        AudioPlayerService.stopService(this);
    }

    /**
     * 释放数据
     */
    private void releaseData() {
        ImageUtil.release();
        AudioPlayerManager.newInstance(mContext).release();
        ToastUtil.release();
    }

    /**
     * 销毁广播
     */
    private void destroyReceiver() {
        if (mFragmentReceiver != null) {
            mFragmentReceiver.unregisterReceiver(mContext);
        }

        if (mAudioBroadcastReceiver != null) {
            mAudioBroadcastReceiver.unregisterReceiver(mContext);
        }

        if (mAppSystemReceiver != null) {
            mAppSystemReceiver.unregisterReceiver(mContext);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mPhoneReceiver != null) {
                mPhoneReceiver.unregisterReceiver(mContext);
            }
        } else {
            if (mPhoneV4Receiver != null) {
                mPhoneV4Receiver.unregisterReceiver(mContext);
            }
        }
    }
}
