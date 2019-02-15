package com.zlm.hp.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zlm.hp.async.AsyncHandlerTask;
import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.handler.WeakRefHandler;
import com.zlm.hp.lyrics.LyricsReader;
import com.zlm.hp.lyrics.utils.LyricsUtils;
import com.zlm.hp.lyrics.widget.AbstractLrcView;
import com.zlm.hp.lyrics.widget.FloatLyricsView;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.manager.LyricsManager;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.ui.MainActivity;
import com.zlm.hp.ui.R;
import com.zlm.hp.util.ColorUtil;
import com.zlm.hp.widget.des.FloatLinearLayout;

/**
 * @Description: 悬浮窗口服务
 * @author: zhangliangming
 * @date: 2018-05-12 10:39
 **/
public class FloatService extends Service {
    /**
     * 窗口管理
     */
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayout;
    private int mWindowY = 0;

    //////////////////////////////////////////////////
    /**
     * 桌面布局
     */
    private FloatLinearLayout mFloatLinearLayout;
    /**
     * 标题栏
     */
    private RelativeLayout mTitleRelativeLayout;
    /**
     * 歌曲名称
     */
    private TextView mSongNameTv;
    /**
     * 双行歌词视图
     */
    private FloatLyricsView mFloatLyricsView;
    /**
     * 操作布局
     */
    private LinearLayout mOperateLinearLayout;
    /**
     * 播放按钮
     */
    private ImageView mPlayBtn;
    /**
     * 暂停按钮
     */
    private ImageView mPauseBtn;

    /**
     * 设置布局
     */
    private LinearLayout mSettingLinearLayout;

    private ConfigInfo mConfigInfo;

    ////////////////////////////////////////////////////////
    /**
     * 音频广播
     */
    private AudioBroadcastReceiver mAudioBroadcastReceiver;

    /**
     * 处理ui任务
     */
    private WeakRefHandler mUIHandler;

    /**
     * 子线程用于执行耗时任务
     */
    private WeakRefHandler mWorkerHandler;

    //创建异步HandlerThread
    private HandlerThread mHandlerThread;

    /**
     * 广播监听
     */
    private AudioBroadcastReceiver.AudioReceiverListener mAudioReceiverListener = new AudioBroadcastReceiver.AudioReceiverListener() {
        @Override
        public void onReceive(Context context, final Intent intent, final int code) {
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    handleAudioBroadcastReceiver(intent, code);
                }
            });
        }
    };

    /**
     * 处理界面隐藏
     */
    private int mAlpha = 200;
    private int mDelayMs = 3 * 1000;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            if (mTitleRelativeLayout.getVisibility() != View.INVISIBLE) {
                mTitleRelativeLayout.setVisibility(View.INVISIBLE);
            }
            if (mOperateLinearLayout.getVisibility() != View.INVISIBLE) {
                mOperateLinearLayout.setVisibility(View.INVISIBLE);
            }
            if (mSettingLinearLayout.getVisibility() != View.GONE) {
                mSettingLinearLayout.setVisibility(View.GONE);
            }

            mFloatLinearLayout.setBackgroundColor(ColorUtil.parserColor(Color.BLACK, 0));
            mFloatLinearLayout.setTag(0);
        }
    };
    /**
     * 显示界面
     */
    private Runnable mShowRunnable = new Runnable() {
        @Override
        public void run() {

            mFloatLinearLayout.setBackgroundColor(ColorUtil.parserColor(Color.BLACK, mAlpha));
            mFloatLinearLayout.setTag(mAlpha);

            if (mTitleRelativeLayout.getVisibility() != View.VISIBLE) {
                mTitleRelativeLayout.setVisibility(View.VISIBLE);
            }
            if (mOperateLinearLayout.getVisibility() != View.VISIBLE) {
                mOperateLinearLayout.setVisibility(View.VISIBLE);
            }

            mUIHandler.postDelayed(mRunnable, mDelayMs);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //创建ui handler
        mUIHandler = new WeakRefHandler(Looper.getMainLooper(), this, new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                handleUIMessage(msg);
                return true;
            }
        });

        //创建异步HandlerThread
        mHandlerThread = new HandlerThread("loadFloatServiceData", Process.THREAD_PRIORITY_BACKGROUND);
        //必须先开启线程
        mHandlerThread.start();
        //子线程Handler
        mWorkerHandler = new WeakRefHandler(mHandlerThread.getLooper(), this, new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                handleWorkerMessage(msg);
                return true;
            }
        });

        initData();
        registerReceiver();
        return super.onStartCommand(intent, START_STICKY, startId);
    }

    private void handleWorkerMessage(Message msg) {
    }

    private void handleUIMessage(Message msg) {

    }

    /**
     *
     */
    private void registerReceiver() {
        //注册接收音频播放广播
        mAudioBroadcastReceiver = new AudioBroadcastReceiver();
        mAudioBroadcastReceiver.setReceiverListener(mAudioReceiverListener);
        mAudioBroadcastReceiver.registerReceiver(getApplicationContext());

    }

    /**
     * 初始化数据
     */
    private void initData() {
        //加载数据
        mConfigInfo = ConfigInfo.obtain();
        //创建窗口
        createWindowManager();
        //创建桌面布局
        createDesktopLayout();

        AudioInfo curAudioInfo = AudioPlayerManager.getInstance(getApplicationContext()).getCurSong(mConfigInfo.getPlayHash());
        Intent intent = new Intent();
        if (curAudioInfo != null) {

            Bundle bundle = new Bundle();
            bundle.putParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY, curAudioInfo);
            intent.putExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY, bundle);
            handleAudioBroadcastReceiver(intent, AudioBroadcastReceiver.ACTION_CODE_INIT);

            int playStatus = AudioPlayerManager.getInstance(getApplicationContext()).getPlayStatus();
            if (playStatus == AudioPlayerManager.PLAYING) {
                handleAudioBroadcastReceiver(intent, AudioBroadcastReceiver.ACTION_CODE_PLAY);
            }

        } else {
            handleAudioBroadcastReceiver(intent, AudioBroadcastReceiver.ACTION_CODE_NULL);
        }


        if (mFloatLinearLayout.getParent() == null) {
            mWindowManager.addView(mFloatLinearLayout, mLayout);
        }

        //
        mUIHandler.postDelayed(mRunnable, 0);

    }

    /**
     * 创建窗口
     */
    @SuppressLint("WrongConstant")
    private void createWindowManager() {
        // 取得系统窗体
        mWindowManager = (WindowManager) getApplicationContext()
                .getSystemService("window");

        // 窗体的布局样式
        mLayout = new WindowManager.LayoutParams();
        // 设置窗体显示类型——TYPE_SYSTEM_ALERT(系统提示)
        //mLayout.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//6.0+
            mLayout.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mLayout.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        // 设置显示的模式
        mLayout.format = PixelFormat.RGBA_8888;
        // 设置对齐的方法
        mLayout.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        // 设置窗体宽度和高度
        DisplayMetrics dm = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        mLayout.width = dm.widthPixels - 20;
        mLayout.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayout.y = mConfigInfo.getDesktopLrcY();

        // 设置窗体焦点及触摸：
        boolean desktopLyricsIsMove = mConfigInfo.isDesktopLrcCanMove();
        if (desktopLyricsIsMove) {
            mLayout.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        } else {
            mLayout.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        }

        mWindowY = mLayout.y;
    }


    /**
     * 创建窗口界面
     */
    private void createDesktopLayout() {
        ViewGroup mainView = (ViewGroup) LayoutInflater.from(getApplicationContext()).inflate(
                R.layout.window_float_layout, null);
        mFloatLinearLayout = mainView.findViewById(R.id.floatLinearLayout);
        mFloatLinearLayout.setBackgroundColor(ColorUtil.parserColor(Color.BLACK, mAlpha));
        mFloatLinearLayout.setTag(mAlpha);
        mFloatLinearLayout.setFloatEventCallBack(new FloatLinearLayout.FloatEventCallBack() {
            @Override
            public void moveStart() {
                mUIHandler.removeCallbacks(mRunnable);
            }

            @Override
            public void move(int dy) {

                mLayout.y = mWindowY - dy;
                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        mWindowManager.updateViewLayout(mFloatLinearLayout, mLayout);
                    }
                });


            }

            @Override
            public void moveEnd() {
                mWindowY = mLayout.y;
                mUIHandler.postDelayed(mRunnable, mDelayMs);
                mConfigInfo.setDesktopLrcY(mWindowY).save();
            }

            @Override
            public void click() {

                mUIHandler.removeCallbacks(mRunnable);
                if (Integer.valueOf(mFloatLinearLayout.getTag() + "") == 0) {
                    mUIHandler.postDelayed(mShowRunnable, 0);
                } else {
                    mUIHandler.removeCallbacks(mShowRunnable);
                    mUIHandler.postDelayed(mRunnable, 100);
                }
            }
        });

        //标题面板
        mTitleRelativeLayout = mainView.findViewById(R.id.title);
        mSongNameTv = mainView.findViewById(R.id.songName);

        //图标
        ImageView iconBtn = mainView.findViewById(R.id.iconbtn);
        iconBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setClass(getBaseContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

                startActivity(intent);
            }
        });

        //关闭歌词
        ImageView closeBtn = mainView.findViewById(R.id.closebtn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioBroadcastReceiver.sendReceiver(getApplicationContext(), AudioBroadcastReceiver.ACTION_CODE_NOTIFY_DESLRC_HIDE_ACTION);
            }
        });

        //双行歌词视图
        mFloatLyricsView = mainView.findViewById(R.id.floatLyricsView);
        mFloatLyricsView.setOrientation(FloatLyricsView.ORIENTATION_CENTER);
        //
        mFloatLyricsView.setExtraLyricsListener(new AbstractLrcView.ExtraLyricsListener() {
            @Override
            public void extraLrcCallback() {
                if (mFloatLyricsView.getLyricsReader() == null) {
                    return;
                }
                int extraLrcType = mFloatLyricsView.getExtraLrcType();
                if (extraLrcType == AbstractLrcView.EXTRALRCTYPE_NOLRC) {

                } else if (extraLrcType == AbstractLrcView.EXTRALRCTYPE_TRANSLATELRC) {
                    if (mConfigInfo.getExtraLrcStatus() == ConfigInfo.EXTRALRCSTATUS_SHOWTRANSLATELRC) {
                        mFloatLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC);
                    } else {
                        mFloatLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_NOSHOWEXTRALRC);
                    }
                } else if (extraLrcType == AbstractLrcView.EXTRALRCTYPE_TRANSLITERATIONLRC) {

                    if (mConfigInfo.getExtraLrcStatus() == ConfigInfo.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC) {
                        mFloatLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC);
                    } else {
                        mFloatLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_NOSHOWEXTRALRC);
                    }

                } else if (extraLrcType == AbstractLrcView.EXTRALRCTYPE_BOTH) {
                    if (mConfigInfo.getExtraLrcStatus() == ConfigInfo.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC) {
                        mFloatLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC);
                    } else if (mConfigInfo.getExtraLrcStatus() == ConfigInfo.EXTRALRCSTATUS_SHOWTRANSLATELRC) {
                        mFloatLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC);

                    } else {

                        mFloatLyricsView.setExtraLrcStatus(AbstractLrcView.EXTRALRCSTATUS_NOSHOWEXTRALRC);

                    }
                }
            }
        });
        //设置字体大小
        final float floatVH = getResources().getDimension(R.dimen.bar_height);
        final int minFontSize = (int) (floatVH / 4);
        final int maxFontSize = (int) (floatVH / 3);
        int fontSize = Math.max(mConfigInfo.getDesktopLrcFontSize(), minFontSize);
        //设置字体文件
        Typeface typeFace = Typeface.createFromAsset(getAssets(),
                "fonts/pingguolihei.ttf");
        mFloatLyricsView.setTypeFace(typeFace);
        final Paint paint = new Paint();
        paint.setTypeface(typeFace);
        setLrcFontSize(paint, floatVH, fontSize, false);

        //
        int desktopLrcColorIndex = mConfigInfo.getDesktopLrcColorIndex();
        setLrcColor(desktopLrcColorIndex, false);

        //操作面板
        mOperateLinearLayout = mainView.findViewById(R.id.operate);
        //锁按钮
        RelativeLayout lockBtn = mainView.findViewById(R.id.lockbtn);
        lockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUIHandler.removeCallbacks(mRunnable);


                mConfigInfo.setDesktopLrcCanMove(false).save();
                AudioBroadcastReceiver.sendReceiver(getApplicationContext(), AudioBroadcastReceiver.ACTION_CODE_NOTIFY_LOCK);

                Toast.makeText(getApplicationContext(), "歌词已锁，可通过点击通知栏解锁按钮进行解锁!", Toast.LENGTH_LONG).show();
                mUIHandler.postDelayed(mRunnable, 0);
            }
        });

        //上一首
        RelativeLayout preBtn = mainView.findViewById(R.id.prebtn);
        preBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUIHandler.removeCallbacks(mRunnable);


                AudioPlayerManager.getInstance(getApplicationContext()).pre();

                mUIHandler.postDelayed(mRunnable, mDelayMs);
            }
        });

        //播放
        mPlayBtn = mainView.findViewById(R.id.play_btn);
        mPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUIHandler.removeCallbacks(mRunnable);

                AudioInfo audioInfo = AudioPlayerManager.getInstance(getApplicationContext()).getCurSong(mConfigInfo.getPlayHash());
                if (audioInfo != null) {
                    AudioPlayerManager.getInstance(getApplicationContext()).play(audioInfo.getPlayProgress());
                }

                mUIHandler.postDelayed(mRunnable, mDelayMs);
            }
        });

        //暂停
        mPauseBtn = mainView.findViewById(R.id.pause_btn);
        mPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUIHandler.removeCallbacks(mRunnable);

                AudioPlayerManager.getInstance(getApplicationContext()).pause();

                mUIHandler.postDelayed(mRunnable, mDelayMs);
            }
        });


        //下一首
        RelativeLayout nextBtn = mainView.findViewById(R.id.nextbtn);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUIHandler.removeCallbacks(mRunnable);

                AudioPlayerManager.getInstance(getApplicationContext()).next();

                mUIHandler.postDelayed(mRunnable, mDelayMs);
            }
        });

        //设置按钮
        RelativeLayout settingBtn = mainView.findViewById(R.id.settingbtn);
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUIHandler.removeCallbacks(mRunnable);

                if (mSettingLinearLayout.getVisibility() == View.GONE) {
                    mSettingLinearLayout.setVisibility(View.VISIBLE);
                } else {
                    mSettingLinearLayout.setVisibility(View.GONE);
                }

                mUIHandler.postDelayed(mRunnable, mDelayMs);
            }
        });

        //设置布局
        mSettingLinearLayout = mainView.findViewById(R.id.setting);
        mSettingLinearLayout.setVisibility(View.GONE);

        //初始化歌词颜色面板
        initLrcColorPanel(mainView);

        //字体减小
        RelativeLayout lrcSizeDecrease = mainView.findViewById(R.id.lyric_decrease);
        lrcSizeDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUIHandler.removeCallbacks(mRunnable);

                int fontSize = mConfigInfo.getDesktopLrcFontSize();
                fontSize -= 2;
                fontSize = Math.max(fontSize, minFontSize);
                setLrcFontSize(paint, floatVH, fontSize, true);


                mUIHandler.postDelayed(mRunnable, mDelayMs);
            }
        });

        //字体增加
        RelativeLayout lrcSizeIncrease = mainView.findViewById(R.id.lyric_increase);
        lrcSizeIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUIHandler.removeCallbacks(mRunnable);

                int fontSize = mConfigInfo.getDesktopLrcFontSize();
                fontSize += 2;
                fontSize = Math.min(fontSize, maxFontSize);
                setLrcFontSize(paint, floatVH, fontSize, true);


                mUIHandler.postDelayed(mRunnable, mDelayMs);
            }
        });
    }

    /**
     * @param paint
     * @param fontSize
     */
    private void setLrcFontSize(Paint paint, float floatVH, int fontSize, boolean isInvalidateView) {
        float spaceLineHeight = (floatVH - LyricsUtils.getTextHeight(paint) * 2) / 3;
        float extraLrcSpaceLineHeight = spaceLineHeight;
        mFloatLyricsView.setSpaceLineHeight(spaceLineHeight);
        mFloatLyricsView.setExtraLrcSpaceLineHeight(extraLrcSpaceLineHeight);
        mFloatLyricsView.setSize(fontSize, fontSize, isInvalidateView);
        mConfigInfo.setDesktopLrcFontSize(fontSize).save();
    }

    /**
     * @param mainView
     */
    private void initLrcColorPanel(ViewGroup mainView) {
        //歌词颜色面板
        ImageView[] colorPanel = new ImageView[5];
        final ImageView[] colorStatus = new ImageView[colorPanel.length];

        int i = 0;
        //
        colorPanel[i] = mainView.findViewById(R.id.color_panel1);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUIHandler.removeCallbacks(mRunnable);

                int index = mConfigInfo.getDesktopLrcColorIndex();
                if (index != 0) {
                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[0].setVisibility(View.VISIBLE);
                    setLrcColor(0, true);
                }

                mUIHandler.postDelayed(mRunnable, mDelayMs);
            }
        });
        colorStatus[i] = mainView.findViewById(R.id.color_status1);
        colorStatus[i].setVisibility(View.GONE);
        //
        i++;
        colorPanel[i] = mainView.findViewById(R.id.color_panel2);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUIHandler.removeCallbacks(mRunnable);

                int index = mConfigInfo.getDesktopLrcColorIndex();
                if (index != 1) {

                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[1].setVisibility(View.VISIBLE);

                    setLrcColor(1, true);

                }

                mUIHandler.postDelayed(mRunnable, mDelayMs);

            }
        });
        colorStatus[i] = mainView.findViewById(R.id.color_status2);
        colorStatus[i].setVisibility(View.GONE);
        //
        i++;
        colorPanel[i] = mainView.findViewById(R.id.color_panel3);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUIHandler.removeCallbacks(mRunnable);

                int index = mConfigInfo.getDesktopLrcColorIndex();
                if (index != 2) {

                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[2].setVisibility(View.VISIBLE);

                    setLrcColor(2, true);
                }

                mUIHandler.postDelayed(mRunnable, mDelayMs);
            }
        });
        colorStatus[i] = mainView.findViewById(R.id.color_status3);
        colorStatus[i].setVisibility(View.GONE);
        //
        i++;
        colorPanel[i] = mainView.findViewById(R.id.color_panel4);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUIHandler.removeCallbacks(mRunnable);

                int index = mConfigInfo.getDesktopLrcColorIndex();
                if (index != 3) {

                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[3].setVisibility(View.VISIBLE);

                    setLrcColor(3, true);
                }

                mUIHandler.postDelayed(mRunnable, mDelayMs);
            }
        });
        colorStatus[i] = mainView.findViewById(R.id.color_status4);
        colorStatus[i].setVisibility(View.GONE);
        //
        i++;
        colorPanel[i] = mainView.findViewById(R.id.color_panel5);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUIHandler.removeCallbacks(mRunnable);

                int index = mConfigInfo.getDesktopLrcColorIndex();
                if (index != 4) {

                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[4].setVisibility(View.VISIBLE);

                    setLrcColor(4, true);
                }

                mUIHandler.postDelayed(mRunnable, mDelayMs);
            }
        });
        colorStatus[i] = mainView.findViewById(R.id.color_status5);
        colorStatus[i].setVisibility(View.GONE);

        //
        colorStatus[mConfigInfo.getDesktopLrcColorIndex()].setVisibility(View.VISIBLE);
    }

    /**
     * 设置歌词颜色
     *
     * @param index
     */
    private void setLrcColor(int index, boolean invalidate) {
        if (invalidate) {
            mConfigInfo.setDesktopLrcColorIndex(index);
        }
        //未读颜色
        int paintColors[] = ConfigInfo.DESKTOP_LRC_NOREAD_COLORS[index];
        //已读颜色
        int paintHLColors[] = ConfigInfo.DESKTOP_LRC_READED_COLORS[index];
        mFloatLyricsView.setPaintColor(paintColors);
        mFloatLyricsView.setPaintHLColor(paintHLColors, invalidate);
    }

    @Override
    public void onDestroy() {

        //移除队列任务
        if (mUIHandler != null) {
            mUIHandler.removeCallbacksAndMessages(null);
        }

        //移除队列任务
        if (mWorkerHandler != null) {
            mWorkerHandler.removeCallbacksAndMessages(null);
        }

        //关闭线程
        if (mHandlerThread != null)
            mHandlerThread.quit();

        if (mFloatLinearLayout.getParent() != null) {
            mWindowManager.removeView(mFloatLinearLayout);
        }
        //注销广播
        mAudioBroadcastReceiver.unregisterReceiver(getApplicationContext());

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 处理音频广播事件
     *
     * @param intent
     */
    private void handleAudioBroadcastReceiver(Intent intent, int code) {
        switch (code) {
            case AudioBroadcastReceiver.ACTION_CODE_NULL:
                //空数据
                mSongNameTv.setText(R.string.def_text);
                mPauseBtn.setVisibility(View.INVISIBLE);
                mPlayBtn.setVisibility(View.VISIBLE);
                //
                mFloatLyricsView.initLrcData();

                break;

            case AudioBroadcastReceiver.ACTION_CODE_INIT:
                Bundle initBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                AudioInfo initAudioInfo = initBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                if (initAudioInfo != null) {
                    mSongNameTv.setText(initAudioInfo.getSingerName() + "-" + initAudioInfo.getSongName());

                    mFloatLyricsView.initLrcData();
                    //加载中
                    mFloatLyricsView.setLrcStatus(AbstractLrcView.LRCSTATUS_LOADING);

                    LyricsReader oldLyricsReader = mFloatLyricsView.getLyricsReader();
                    if (oldLyricsReader == null || !oldLyricsReader.getHash().equals(initAudioInfo.getHash())) {
                        //加载歌词
                        String keyWords = initAudioInfo.getTitle();
                        LyricsManager.getInstance(getApplicationContext()).loadLyrics(keyWords, keyWords, initAudioInfo.getDuration() + "", initAudioInfo.getHash(), mConfigInfo.isWifi(), new AsyncHandlerTask(mUIHandler, mWorkerHandler), null);
                        //加载中
                        mFloatLyricsView.initLrcData();
                        mFloatLyricsView.setLrcStatus(AbstractLrcView.LRCSTATUS_LOADING);
                    }
                }
                mPauseBtn.setVisibility(View.INVISIBLE);
                mPlayBtn.setVisibility(View.VISIBLE);
                break;

            case AudioBroadcastReceiver.ACTION_CODE_PLAY:
                mPauseBtn.setVisibility(View.VISIBLE);
                mPlayBtn.setVisibility(View.INVISIBLE);
                break;
            case AudioBroadcastReceiver.ACTION_CODE_STOP:

                if (mFloatLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mFloatLyricsView.pause();
                }

                mPauseBtn.setVisibility(View.INVISIBLE);
                mPlayBtn.setVisibility(View.VISIBLE);
                break;
            case AudioBroadcastReceiver.ACTION_CODE_PLAYING:

                Bundle playingBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                AudioInfo playingAudioInfo = playingBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                if (playingAudioInfo != null) {
                    if (mFloatLyricsView.getLyricsReader() != null && mFloatLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mFloatLyricsView.getLrcPlayerStatus() != AbstractLrcView.LRCPLAYERSTATUS_PLAY && mFloatLyricsView.getLyricsReader().getHash().equals(playingAudioInfo.getHash())) {
                        mFloatLyricsView.play((int) playingAudioInfo.getPlayProgress());
                    }
                }

                break;
            case AudioBroadcastReceiver.ACTION_CODE_SEEKTO:
                Bundle seektoBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                AudioInfo seektoAudioInfo = seektoBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                if (seektoAudioInfo != null) {

                    if (mFloatLyricsView.getLyricsReader() != null && mFloatLyricsView.getLyricsReader().getHash().equals(seektoAudioInfo.getHash())) {
                        mFloatLyricsView.seekto((int) seektoAudioInfo.getPlayProgress());
                    }

                }
                break;
            case AudioBroadcastReceiver.ACTION_CODE_LRCLOADED:
                //歌词加载完成
                Bundle lrcloadedBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                String lrcHash = lrcloadedBundle.getString(AudioBroadcastReceiver.ACTION_DATA_KEY);
                AudioInfo curAudioInfo = AudioPlayerManager.getInstance(getApplicationContext()).getCurSong(mConfigInfo.getPlayHash());
                if (curAudioInfo != null && lrcHash.equals(curAudioInfo.getHash())) {
                    LyricsReader oldLyricsReader = mFloatLyricsView.getLyricsReader();
                    LyricsReader newLyricsReader = LyricsManager.getInstance(getApplicationContext()).getLyricsReader(lrcHash);
                    if (oldLyricsReader != null && newLyricsReader != null && oldLyricsReader.getHash().equals(newLyricsReader.getHash())) {

                    } else {
                        mFloatLyricsView.setLyricsReader(newLyricsReader);
                    }

                    if (oldLyricsReader != null || newLyricsReader != null) {
                        if (mFloatLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                            mFloatLyricsView.seekto((int) curAudioInfo.getPlayProgress());
                        }
                    }
                }
                break;
            case AudioBroadcastReceiver.ACTION_CODE_NOTIFY_LOCK:
            case AudioBroadcastReceiver.ACTION_CODE_NOTIFY_UNLOCK:
                if (mConfigInfo.isDesktopLrcCanMove()) {
                    mLayout.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                } else {
                    mLayout.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                }
                if (mFloatLinearLayout.getParent() != null) {
                    mWindowManager.updateViewLayout(mFloatLinearLayout, mLayout);
                }
                break;

        }
    }
}
