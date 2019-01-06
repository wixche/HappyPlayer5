package com.zlm.hp.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.zlm.hp.handler.WeakRefHandler;
import com.zlm.hp.manager.ActivityManager;
import com.zlm.hp.util.AppBarUtil;
import com.zlm.hp.util.ColorUtil;


/**
 * Created by zhangliangming on 2018-08-04.
 */

public abstract class BaseActivity extends AppCompatActivity {

    /**
     * 是否全屏
     */
    private boolean mIsFullScreen = false;

    /**
     * 子线程用于执行耗时任务
     */
    public WeakRefHandler mWorkerHandler;
    /**
     * 处理ui任务
     */
    public WeakRefHandler mUIHandler;
    /**
     * 自定义根view
     */
    private ViewGroup mRootView;
    /**
     *
     */
    public Context mContext;
    //创建异步HandlerThread
    private HandlerThread mHandlerThread;
    /**
     * 状态栏背景颜色
     */
    private int mStatusBarViewBG = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        mContext = getApplicationContext();
        mStatusBarViewBG = ColorUtil.parserColor(ContextCompat.getColor(mContext, R.color.defColor));

        preInitStatusBar();
        View view = LayoutInflater.from(mContext).inflate(setContentLayoutResID(), null);
        initStatusBar(view);

        if (mRootView != null) {
            addChildView(mRootView, view, -1, -1);
            super.setContentView(mRootView);
        } else {
            super.setContentView(view);
        }

        //创建ui handler
        mUIHandler = new WeakRefHandler(Looper.getMainLooper(), this, new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                handleUIMessage(msg);
                return true;
            }
        });

        //创建异步HandlerThread
        mHandlerThread = new HandlerThread("loadActivityData", Process.THREAD_PRIORITY_BACKGROUND);
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
        ActivityManager.getInstance().addActivity(this);

        //初始化view相关数据
        initViews(savedInstanceState);

    }

    /**
     * 初始化状态栏
     *
     * @param view
     */
    private void initStatusBar(View view) {
        if (!mIsFullScreen)
            AppBarUtil.initBar(this.getWindow());
        boolean isAddStatusBar = AppBarUtil.isAddStatusBar();
        //添加状态栏
        addStatusBar(view, isAddStatusBar);
    }

    /**
     * 添加自定义状态栏
     *
     * @param view
     * @param isAddStatusBar 是否添加状态栏
     */
    private void addStatusBar(View view, boolean isAddStatusBar) {
        View statusBarView = view.findViewById(R.id.status_bar_view);
        if (statusBarView == null) return;
        if (!isAddStatusBar) {
            statusBarView.setVisibility(View.GONE);
            return;
        }

        ViewParent parentView = statusBarView.getParent();
        int statusBarViewHeight = AppBarUtil.getStatusBarHeight(mContext);
        if (parentView instanceof ConstraintLayout) {
            ConstraintLayout.LayoutParams clp = new ConstraintLayout.LayoutParams(-1, statusBarViewHeight);
            statusBarView.setLayoutParams(clp);
        } else if (parentView instanceof LinearLayout) {
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(-1, statusBarViewHeight);
            statusBarView.setLayoutParams(llp);
        } else if (parentView instanceof RelativeLayout) {
            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(-1, statusBarViewHeight);
            statusBarView.setLayoutParams(rlp);
        } else if (parentView instanceof FrameLayout) {
            FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(-1, statusBarViewHeight);
            statusBarView.setLayoutParams(flp);
        } else if (parentView instanceof ViewGroup) {
            ViewGroup.LayoutParams vplp = new ViewGroup.LayoutParams(-1, statusBarViewHeight);
            statusBarView.setLayoutParams(vplp);
        }
        statusBarView.setVisibility(View.VISIBLE);
        statusBarView.setBackgroundColor(mStatusBarViewBG);

    }

    /**
     * 添加子view
     *
     * @param parentView 父view
     * @param childView  子view
     */
    private void addChildView(ViewGroup parentView, View childView, int viewWidth, int viewHeight) {

        if (parentView instanceof ConstraintLayout) {

            ConstraintLayout.LayoutParams clp = new ConstraintLayout.LayoutParams(viewWidth, viewHeight);
            parentView.addView(childView, clp);
        } else if (parentView instanceof LinearLayout) {


            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(viewWidth, viewHeight);
            parentView.addView(childView, llp);
        } else if (parentView instanceof RelativeLayout) {


            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(viewWidth, viewHeight);
            parentView.addView(childView, rlp);
        } else if (parentView instanceof FrameLayout) {

            FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(viewWidth, viewHeight);
            parentView.addView(childView, flp);

        } else if (parentView instanceof ViewGroup) {

            ViewGroup.LayoutParams vplp = new ViewGroup.LayoutParams(viewWidth, viewHeight);
            parentView.addView(childView, vplp);
        }
    }

    @Override
    public void finish() {
        ActivityManager.getInstance().removeActivity(this);
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

        super.finish();
    }

    /**
     * 设置状态栏背景颜色
     *
     * @param statusBarViewBG
     */
    public void setStatusBarViewBG(int statusBarViewBG) {
        this.mStatusBarViewBG = statusBarViewBG;
    }

    public void setFullScreen(boolean isFullScreen) {
        this.mIsFullScreen = isFullScreen;
    }

    /**
     * 初始化view之前
     */
    protected void preInitStatusBar() {

    }

    /**
     * 设置主界面内容视图
     *
     * @return
     */
    protected abstract int setContentLayoutResID();

    /**
     * 初始化view视图
     *
     * @param savedInstanceState
     */
    protected abstract void initViews(Bundle savedInstanceState);

    /**
     * 处理UI
     *
     * @param msg
     */
    protected abstract void handleUIMessage(Message msg);

    /**
     * 处理子线程worker
     *
     * @param msg
     */
    protected abstract void handleWorkerMessage(Message msg);

    /**
     * 设置 rootview
     *
     * @param rootView
     */
    protected void setRootView(ViewGroup rootView) {
        this.mRootView = rootView;
    }

}
