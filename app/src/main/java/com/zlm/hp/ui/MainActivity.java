package com.zlm.hp.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dou361.dialogui.DialogUIUtils;
import com.dou361.dialogui.listener.DialogUIListener;
import com.suke.widget.SwitchButton;
import com.zlm.down.entity.DownloadTask;
import com.zlm.hp.adapter.PopPlayListAdapter;
import com.zlm.hp.adapter.TabFragmentAdapter;
import com.zlm.hp.application.HPApplication;
import com.zlm.hp.async.AsyncHandlerTask;
import com.zlm.hp.audio.utils.MediaUtil;
import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.constants.Constants;
import com.zlm.hp.db.util.DownloadThreadInfoDB;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.entity.TimerInfo;
import com.zlm.hp.fragment.DownloadMusicFragment;
import com.zlm.hp.fragment.LastSongFragment;
import com.zlm.hp.fragment.MeFragment;
import com.zlm.hp.fragment.RecommendFragment;
import com.zlm.hp.fragment.SearchFragment;
import com.zlm.hp.fragment.SongFragment;
import com.zlm.hp.fragment.SpecialFragment;
import com.zlm.hp.manager.ActivityManager;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.manager.DownloadAudioManager;
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
import com.zlm.hp.util.PreferencesUtil;
import com.zlm.hp.util.TimeUtil;
import com.zlm.hp.util.ToastUtil;
import com.zlm.hp.widget.IconfontImageButtonTextView;
import com.zlm.hp.widget.IconfontIndicatorTextView;
import com.zlm.hp.widget.IconfontTextView;
import com.zlm.hp.widget.WhiteTranLinearLayout;
import com.zlm.hp.widget.WhiteTranRelativeLayout;
import com.zlm.libs.widget.MusicSeekBar;
import com.zlm.libs.widget.SlidingMenuLayout;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * 定时关闭
     */
    private WhiteTranRelativeLayout mTimerPowerOffLL;
    private TextView mTimerTv;

    //wifi
    private WhiteTranRelativeLayout mWifiLR;

    /**
     * 工具
     */
    private WhiteTranLinearLayout mToolLL;

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

    ///////////////////////////////歌曲列表弹出窗口布局/////////////////////////////////////////
    private boolean mIsShowPopPlayList = false;
    /**
     * 播放列表全屏界面
     */
    private RelativeLayout mPopPlayListRL;

    /**
     * 播放列表内容界面
     */
    private RelativeLayout mPopPlayContentRL;

    /**
     *
     */
    private RecyclerView mPlayListRListView;
    private PopPlayListAdapter mAdapter;

    /**
     * 当前播放列表歌曲总数
     */
    private TextView mPopListSizeTv;

    //播放模式
    private IconfontTextView mModeAllTv;
    private IconfontTextView mModeRandomTv;
    private IconfontTextView mModeSingleTv;


    ////////////////////////////////////////////////////////////////////////////

    /**
     * 基本数据
     */
    private ConfigInfo mConfigInfo;
    /**
     * 加载基本数据
     */
    private final int LOAD_CONFIG_DATA = 1;

    /**
     * 更新计时器
     */
    private final int MESSAGE_WHAT_TIMERUPDATE = 2;

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
                    case FragmentReceiver.ACTION_CODE_OPEN_LIKEFRAGMENT:
                    case FragmentReceiver.ACTION_CODE_OPEN_RECENTFRAGMENT:

                        Bundle bundle = intent.getBundleExtra(SongFragment.ARGUMENTS_KEY);
                        SongFragment songFragment = SongFragment.newInstance();
                        songFragment.setArguments(bundle);

                        mSlidingMenuOnListener.addAndShowFragment(songFragment);
                        break;
                    case FragmentReceiver.ACTION_CODE_OPEN_DOWNLOADFRAGMENT:

                        DownloadMusicFragment downloadMusicFragment = DownloadMusicFragment.newInstance();
                        mSlidingMenuOnListener.addAndShowFragment(downloadMusicFragment);

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

                        if (mAdapter != null)
                            mAdapter.reshViewHolder(null);

                        break;
                    case AudioBroadcastReceiver.ACTION_CODE_INIT:
                        Bundle initBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                        final AudioInfo initAudioInfo = initBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
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
                            ImageUtil.loadSingerImage(mContext, mArtistImageView, initAudioInfo.getSingerName(), mConfigInfo.isWifi(), 400, 400, new AsyncHandlerTask(mUIHandler, mWorkerHandler), new ImageUtil.ImageLoadCallBack() {
                                @Override
                                public void callback(Bitmap bitmap) {
                                    //if (bitmap != null) {
                                    AudioBroadcastReceiver.sendNotifiyImgLoadedReceiver(mContext, initAudioInfo);
                                    // }
                                }
                            });

                            //加载歌词
                            String keyWords = initAudioInfo.getTitle();
                            LyricsManager.getInstance(mContext).loadLyrics(keyWords, keyWords, initAudioInfo.getDuration() + "", initAudioInfo.getHash(), mConfigInfo.isWifi(), new AsyncHandlerTask(mUIHandler, mWorkerHandler), null);

                            if (mAdapter != null) {

                                if (mIsShowPopPlayList) {
                                    //定位
                                    int position = AudioPlayerManager.getInstance(mContext).getCurSongIndex(mConfigInfo.getAudioInfos(), mConfigInfo.getPlayHash());
                                    if (position != -1) {
                                        ((LinearLayoutManager) mPlayListRListView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
                                    }
                                }

                                mAdapter.reshViewHolder(initAudioInfo.getHash());
                            }
                        } else {
                            if (mAdapter != null)
                                mAdapter.reshViewHolder(null);
                        }

                        break;
                    case AudioBroadcastReceiver.ACTION_CODE_PLAY:
                        if (mPauseImageView.getVisibility() != View.VISIBLE)
                            mPauseImageView.setVisibility(View.VISIBLE);

                        if (mPlayImageView.getVisibility() != View.INVISIBLE)
                            mPlayImageView.setVisibility(View.INVISIBLE);

                        break;
                    case AudioBroadcastReceiver.ACTION_CODE_PLAYING:

                        Bundle playingBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                        AudioInfo playingAudioInfo = playingBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
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

                    case AudioBroadcastReceiver.ACTION_CODE_DOWNLOAD_FINISH:
                    case AudioBroadcastReceiver.ACTION_CODE_DOWNLOADONEDLINESONG:
                        if (!mIsShowPopPlayList || mAdapter == null) {
                            return;
                        }
                        //网络歌曲下载完成
                        Bundle downloadedBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                        DownloadTask downloadedTask = downloadedBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                        String downloadedHash = downloadedTask.getTaskId();
                        if (downloadedTask != null && !TextUtils.isEmpty(downloadedHash)) {
                            mAdapter.reshViewHolder(downloadedHash);
                        }

                        break;

                    case AudioBroadcastReceiver.ACTION_CODE_UPDATE_PLAYLIST:
                        if (!mIsShowPopPlayList || mAdapter == null) {
                            return;
                        }

                        //设置当前歌曲数据
                        List<AudioInfo> audioInfoList = mConfigInfo.getAudioInfos();
                        mPopListSizeTv.setText(audioInfoList.size() + "");

                        mAdapter.notifyDataSetChanged();

                        break;

                    case AudioBroadcastReceiver.ACTION_CODE_DOWNLOADONLINESONG:
                        //网络歌曲下载中
                        Bundle downloadOnlineSongBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                        DownloadTask downloadingTask = downloadOnlineSongBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                        String hash = mConfigInfo.getPlayHash();
                        AudioInfo audioInfo = AudioPlayerManager.getInstance(mContext).getCurSong(hash);
                        if (audioInfo != null && downloadingTask != null && !TextUtils.isEmpty(hash) && hash.equals(downloadingTask.getTaskId())) {
                            int downloadedSize = DownloadThreadInfoDB.getDownloadedSize(mContext, downloadingTask.getTaskId(), OnLineAudioManager.mThreadNum);
                            double pre = downloadedSize * 1.0 / audioInfo.getFileSize();
                            int downloadProgress = (int) (mMusicSeekBar.getMax() * pre);
                            mMusicSeekBar.setSecondaryProgress(downloadProgress);
                        }

                        break;
                    case AudioBroadcastReceiver.ACTION_CODE_NOTIFY_DESLRC_HIDE_ACTION:

                        mDesktoplrcSwitchButton.setChecked(false);
                        mConfigInfo.setShowDesktopLrc(false).save();
                        //
                        AudioBroadcastReceiver.sendReceiver(mContext, AudioBroadcastReceiver.ACTION_CODE_NOTIFY_DESLRC);
                        //关闭桌面歌词
                        HPApplication applicationTtemp = (HPApplication) getApplication();
                        applicationTtemp.stopFloatService();

                        break;
                    case AudioBroadcastReceiver.ACTION_CODE_NOTIFY_DESLRC_SHOW_ACTION:

                        if (!hasShowFloatWindowPermission()) return;

                        mDesktoplrcSwitchButton.setChecked(true);
                        mConfigInfo.setShowDesktopLrc(true).save();
                        //
                        AudioBroadcastReceiver.sendReceiver(mContext, AudioBroadcastReceiver.ACTION_CODE_NOTIFY_DESLRC);
                        //启动桌面歌词
                        HPApplication application = (HPApplication) getApplication();
                        application.startFloatService();

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
                    case AppSystemReceiver.ACTION_CODE_TIMER_SETTING:
                        mUIHandler.removeMessages(MESSAGE_WHAT_TIMERUPDATE);
                        //设置timer
                    case AppSystemReceiver.ACTION_CODE_TIMER_UPDATE:
                        Message tempMsg = Message.obtain();
                        tempMsg.what = MESSAGE_WHAT_TIMERUPDATE;

                        Bundle timerBundle = intent.getBundleExtra(AppSystemReceiver.ACTION_BUNDLEKEY);
                        TimerInfo timerInfo = timerBundle.getParcelable(AppSystemReceiver.ACTION_DATA_KEY);
                        mConfigInfo.setTimerInfo(timerInfo);
                        if (timerInfo != null) {
                            tempMsg.obj = timerInfo;
                            mUIHandler.sendMessageDelayed(tempMsg, 1000);
                        } else {
                            mUIHandler.sendMessage(tempMsg);
                        }
                        //更新
                        break;
                    case AppSystemReceiver.ACTION_CODE_SCREEN_OFF:
                        //关闭屏幕
                        if (mConfigInfo.isShowLockScreenLrc()) {

                            Intent lockIntent = new Intent(MainActivity.this,
                                    LockActivity.class);
                            lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            startActivity(lockIntent);
                            //去掉动画
                            overridePendingTransition(0, 0);
                        }

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
            case MESSAGE_WHAT_TIMERUPDATE:

                TimerInfo timerInfo = (TimerInfo) msg.obj;
                if (timerInfo != null) {
                    timerInfo.setCurTime(timerInfo.getCurTime() - 1000);
                    mTimerTv.setText(TimeUtil.parseTimeToTimerString(timerInfo.getCurTime()));
                    if (timerInfo.getCurTime() <= 0) {
                        //定时关闭应用
                        ActivityManager.getInstance().exit();
                    } else {
                        AppSystemReceiver.sendTimerUpdateMsgReceiver(mContext, timerInfo);
                    }
                } else {
                    mTimerTv.setText("");
                }
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

        //wifi
        mWifiSwitchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (mConfigInfo.isWifi() != isChecked)
                    mConfigInfo.setWifi(isChecked).save();
            }
        });

        //桌面
        mDesktoplrcSwitchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked) {
                    if (!hasShowFloatWindowPermission()) return;
                }
                if (mConfigInfo.isShowDesktopLrc() != isChecked) {
                    mConfigInfo.setShowDesktopLrc(isChecked).save();
                    //
                    AudioBroadcastReceiver.sendReceiver(mContext, AudioBroadcastReceiver.ACTION_CODE_NOTIFY_DESLRC);
                }
            }
        });

        //锁屏
        mLocklrcSwitchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                boolean askLockPermission = PreferencesUtil.getBoolean(mContext, Constants.ASK_LOCK_PERMISSION, true);
                if (askLockPermission) {
                    //弹出窗口显示

                    String tipMsg = getString(R.string.locklrc_tip);
                    DialogUIUtils.showMdAlert(MainActivity.this, getString(R.string.tip_title), tipMsg, new DialogUIListener() {
                        @Override
                        public void onPositive() {

                            PreferencesUtil.putBoolean(mContext, Constants.ASK_LOCK_PERMISSION, false);

                            //跳转权限设置页面
                            IntentUtil.gotoPermissionSetting(MainActivity.this);
                            mLocklrcSwitchButton.setChecked(false);
                        }

                        @Override
                        public void onNegative() {
                            mLocklrcSwitchButton.setChecked(false);
                        }

                        @Override
                        public void onCancle() {
                            mLocklrcSwitchButton.setChecked(false);
                        }
                    }).setCancelable(true, false).show();

                    return;
                }

                if (mConfigInfo.isShowLockScreenLrc() != isChecked)
                    mConfigInfo.setShowLockScreenLrc(isChecked).save();
            }
        });
    }

    @Override
    protected void handleWorkerMessage(Message msg) {
        switch (msg.what) {
            case LOAD_CONFIG_DATA:

                mConfigInfo = ConfigInfo.obtain();
                AudioPlayerManager.getInstance(mContext).init();

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

                if (mIsShowPopPlayList) {
                    hidePopPlayListView();
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

            @Override
            public void onHideFragment(Fragment fragment) {
                if(fragment != null && fragment instanceof SearchFragment){
                    //强制关闭输入法
                    hideInput(mContext,mSlidingMenuLayout);
                }
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
     * 强制隐藏输入法键盘
     *
     * @param context Context
     * @param view    EditText
     */
    private void hideInput(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
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

                SearchFragment searchFragment = SearchFragment.newInstance();
                mSlidingMenuOnListener.addAndShowFragment(searchFragment);

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
        //工具
        mToolLL = findViewById(R.id.tool_ll);
        mToolLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ToolActivity.class);
                startActivity(intent);
                //去掉动画
                overridePendingTransition(0, 0);
            }
        });

        mTimerTv = findViewById(R.id.timer_text);
        //定时关闭
        mTimerPowerOffLL = findViewById(R.id.timer_power_off_ll);
        mTimerPowerOffLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, TimerPowerOffActivity.class);
                startActivity(intent);
                //去掉动画
                overridePendingTransition(0, 0);
            }
        });

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

    }

    /**
     * 是否有显示桌面的权限
     *
     * @return
     */
    private boolean hasShowFloatWindowPermission() {
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

            return false;
        }
        return true;
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
                AudioPlayerManager.getInstance(mContext).play(mMusicSeekBar.getProgress());
            }
        });
        //暂停
        mPauseImageView = findViewById(R.id.bar_pause);
        mPauseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioPlayerManager.getInstance(mContext).pause();
            }
        });
        //下一首
        mNextImageView = findViewById(R.id.bar_next);
        mNextImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioPlayerManager.getInstance(mContext).next();
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
                AudioInfo audioInfo = AudioPlayerManager.getInstance(mContext).getCurSong(mConfigInfo.getPlayHash());
                if (audioInfo != null && progress <= audioInfo.getDuration()) {
                    audioInfo.setPlayProgress(progress);
                    AudioPlayerManager.getInstance(mContext).seekto(audioInfo);
                }
            }
        });

        //播放列表按钮
        ImageView listMenuImg = findViewById(R.id.list_menu);
        listMenuImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsShowPopPlayList) {
                    hidePopPlayListView();
                } else {
                    if (mPopPlayListRL == null) {
                        initPopPlayListViews();
                    }
                    showPopPlayListView();
                }
            }


        });
    }

    /**
     * 初始化歌曲列表弹出窗口视图
     */
    private void initPopPlayListViews() {
        ViewStub stub = findViewById(R.id.viewstub_main_pop);
        stub.inflate();

        mPlayListRListView = findViewById(R.id.curplaylist_recyclerView);
        //初始化内容视图
        mPlayListRListView.setLayoutManager(new LinearLayoutManager(mContext));
        mPlayListRListView.setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.bar_height));

        //全屏视图
        mPopPlayListRL = findViewById(R.id.list_pop);
        mPopPlayListRL.setVisibility(View.INVISIBLE);
        mPopPlayListRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hidePopPlayListView();
            }
        });

        //内容布局
        mPopPlayContentRL = findViewById(R.id.pop_content);
        mPopListSizeTv = findViewById(R.id.list_size);
        //播放模式
        mModeAllTv = findViewById(R.id.modeAll);
        mModeRandomTv = findViewById(R.id.modeRandom);
        mModeSingleTv = findViewById(R.id.modeSingle);

        mModeAllTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPlayModeView(1, mModeAllTv, mModeRandomTv, mModeSingleTv, true);
            }
        });

        mModeRandomTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPlayModeView(3, mModeAllTv, mModeRandomTv, mModeSingleTv, true);
            }
        });

        mModeSingleTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPlayModeView(0, mModeAllTv, mModeRandomTv, mModeSingleTv, true);
            }
        });
    }

    /**
     * 初始化播放列表播放模式
     *
     * @param playMode
     * @param modeAllImg
     * @param modeRandomImg
     * @param modeSingleImg
     */
    private void initPlayModeView(int playMode, IconfontTextView modeAllImg, IconfontTextView modeRandomImg, IconfontTextView modeSingleImg, boolean isTipShow) {
        if (playMode == 0) {
            if (isTipShow)
                ToastUtil.showTextToast(mContext, getString(R.string.mode_all_text));
            modeAllImg.setVisibility(View.VISIBLE);
            modeRandomImg.setVisibility(View.INVISIBLE);
            modeSingleImg.setVisibility(View.INVISIBLE);
        } else if (playMode == 1) {
            if (isTipShow)
                ToastUtil.showTextToast(mContext, getString(R.string.mode_random_text));
            modeAllImg.setVisibility(View.INVISIBLE);
            modeRandomImg.setVisibility(View.VISIBLE);
            modeSingleImg.setVisibility(View.INVISIBLE);
        } else {
            if (isTipShow)
                ToastUtil.showTextToast(mContext, getString(R.string.mode_single_text));
            modeAllImg.setVisibility(View.INVISIBLE);
            modeRandomImg.setVisibility(View.INVISIBLE);
            modeSingleImg.setVisibility(View.VISIBLE);
        }
        //保存
        if (isTipShow)
            mConfigInfo.setPlayModel(playMode);
    }

    /**
     * 显示歌曲列表弹出窗口
     */
    private void showPopPlayListView() {
        //设置当前播放模式
        initPlayModeView(mConfigInfo.getPlayModel(), mModeAllTv, mModeRandomTv, mModeSingleTv, false);
        //设置当前歌曲数据
        List<AudioInfo> audioInfoList = mConfigInfo.getAudioInfos();
        mPopListSizeTv.setText(audioInfoList.size() + "");
        mAdapter = new PopPlayListAdapter(mContext, audioInfoList, mUIHandler, mWorkerHandler);
        mPlayListRListView.setAdapter(mAdapter);

        //定位
        int position = AudioPlayerManager.getInstance(mContext).getCurSongIndex(mConfigInfo.getAudioInfos(), mConfigInfo.getPlayHash());
        if (position != -1) {
            ((LinearLayoutManager) mPlayListRListView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
        }
        /**
         * 如果该界面还没初始化，则监听
         */
        if (mPopPlayContentRL.getHeight() == 0) {
            mPopPlayContentRL.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mPopPlayContentRL.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    showPopPlayListViewAnimation();
                }
            });

        } else {
            showPopPlayListViewAnimation();
        }
    }

    /**
     * 显示动画
     */
    private void showPopPlayListViewAnimation() {
        mPopPlayListRL.setVisibility(View.VISIBLE);
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, mPopPlayContentRL.getHeight(), 0);
        translateAnimation.setDuration(250);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mIsShowPopPlayList = true;

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mPopPlayListRL.setBackgroundColor(ColorUtil.parserColor(Color.BLACK, 120));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mPopPlayContentRL.clearAnimation();
        mPopPlayContentRL.startAnimation(translateAnimation);
    }

    /**
     * 隐藏歌曲列表弹出窗口
     */
    private void hidePopPlayListView() {
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, mPopPlayContentRL.getHeight());
        translateAnimation.setDuration(250);//设置动画持续时间
        translateAnimation.setFillAfter(true);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIsShowPopPlayList = false;
                mPopPlayListRL.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mPopPlayContentRL.clearAnimation();
        mPopPlayContentRL.startAnimation(translateAnimation);
    }

    @Override
    public void onBackPressed() {
        if (mIsShowPopPlayList) {
            hidePopPlayListView();
        } else if (mSlidingMenuLayout.isShowingFragment()) {
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
        DownloadAudioManager.getInstance(mContext).release();
        AudioPlayerManager.getInstance(mContext).release();
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
