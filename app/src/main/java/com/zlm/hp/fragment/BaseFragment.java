package com.zlm.hp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.zlm.hp.handler.WeakRefHandler;
import com.zlm.hp.ui.R;
import com.zlm.hp.util.AppBarUtil;
import com.zlm.hp.util.ColorUtil;
import com.zlm.hp.util.ContextUtil;
import com.zlm.hp.widget.IconfontTextView;


/**
 * Created by zhangliangming on 2018-08-11.
 */

public abstract class BaseFragment extends Fragment {
    /**
     * 是否是第一次可视
     */
    private boolean mIsFristVisibleToUser;

    /**
     *
     */
    public Context mContext;

    /**
     * 子线程用于执行耗时任务
     */
    public WeakRefHandler mWorkerHandler;
    //创建异步HandlerThread
    private HandlerThread mHandlerThread;
    /**
     * 处理ui任务
     */
    public WeakRefHandler mUIHandler;
    /**
     * 状态栏背景颜色
     */
    private int mStatusBarViewBG = -1;

    private final int SHOWLOADINGVIEW = 1000;
    private final int SHOWCONTENTVIEW = 1001;
    private final int SHOWNONETVIEW = 1002;

    /**
     * 是否添加
     */
    private boolean isAddStatusBarView = true;


    /**
     * 内容布局
     */
    private ViewStub mContentContainer;

    /**
     * 加载中布局
     */
    private ViewStub mLoadingContainer;
    /**
     * 加载图标
     */
    private IconfontTextView mLoadImgView;

    /**
     * 旋转动画
     */
    private Animation rotateAnimation;

    /**
     * 无网络
     */
    private ViewStub mNetContainer;

    /**
     *
     */
    private ConstraintLayout mNetBGLayout;

    private RefreshListener mRefreshListener;

    /**
     * 标题视图
     */
    private int mTitleViewId = R.layout.layout_title;

    /**
     * 初始化
     */
    private void init() {
        this.mIsFristVisibleToUser = false;
        this.mContext = ContextUtil.getContext();
        //创建ui handler
        mUIHandler = new WeakRefHandler(Looper.getMainLooper(), this, new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case SHOWCONTENTVIEW:
                        showContentViewHandler();
                        break;
                    case SHOWLOADINGVIEW:
                        showLoadingViewHandler();
                        break;
                    case SHOWNONETVIEW:
                        showNoNetViewHandler();
                        break;
                    default:
                        handleUIMessage(msg);
                        break;
                }
                return true;
            }
        });

        //创建异步HandlerThread
        mHandlerThread = new HandlerThread("loadFragmentData", Process.THREAD_PRIORITY_BACKGROUND);
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        init();
        mStatusBarViewBG = ColorUtil.parserColor(ContextCompat.getColor(mContext, R.color.defColor));
        preInitStatusBar();
        View mainView = inflater.inflate(R.layout.fragment_base, container, false);

        //添加主布局
        mContentContainer = mainView.findViewById(R.id.viewstub_content_container);
        mContentContainer.setLayoutResource(setContentLayoutResID());
        mContentContainer.inflate();
        mContentContainer.setVisibility(View.GONE);

        //添加titleview
        View titleView = inflater.inflate(mTitleViewId, container, false);
        LinearLayout titleViewLL =  mainView.findViewById(R.id.title_view_parent);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(-1, -1);
        titleViewLL.addView(titleView,llp);

        if (isAddStatusBarView) {
            initStatusBar(mainView);
        }
        initLoadingView(mainView);
        initNoNetView(mainView);

        //初始化view相关数据
        initViews(mainView, savedInstanceState);

        //初始化是否可视
        if (!mIsFristVisibleToUser && getUserVisibleHint()) {
            mIsFristVisibleToUser = true;
            isFristVisibleToUser();
        }
        return mainView;
    }

    @Override
    public void onDestroyView() {

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

        super.onDestroyView();
    }

    /**
     * 初始化状态栏
     *
     * @param
     */
    private void initStatusBar(View mainView) {
        boolean isAddStatusBar = AppBarUtil.isAddStatusBar();
        //添加状态栏
        addStatusBar(mainView, isAddStatusBar);
    }

    /**
     * 添加状态栏
     *
     * @param isAddStatusBar
     */
    private void addStatusBar(View mainView, boolean isAddStatusBar) {
        View statusBarView = mainView.findViewById(R.id.status_bar_view);
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
     * 初始化view之前
     */
    protected void preInitStatusBar() {

    }

    /**
     * 初始化加载界面
     */
    private void initLoadingView(View mainView) {
        mLoadingContainer = mainView.findViewById(R.id.viewstub_loading_container);
        mLoadingContainer.inflate();
        mLoadImgView = mainView.findViewById(R.id.load_img);
        rotateAnimation = AnimationUtils.loadAnimation(getContext(),
                R.anim.anim_rotate);
        rotateAnimation.setInterpolator(new LinearInterpolator());// 匀速
        mLoadImgView.startAnimation(rotateAnimation);
        //mLoadingContainer.setVisibility(View.GONE);

    }

    /**
     * 显示加载窗口
     */
    public void showLoadingView() {
        mUIHandler.sendEmptyMessage(SHOWLOADINGVIEW);
    }

    /**
     * 显示加载窗口
     */
    private void showLoadingViewHandler() {
        if (mNetContainer != null)
            mNetContainer.setVisibility(View.GONE);
        mContentContainer.setVisibility(View.GONE);
        if (mLoadingContainer != null) {
            mLoadingContainer.setVisibility(View.VISIBLE);
            mLoadImgView.clearAnimation();
            mLoadImgView.startAnimation(rotateAnimation);
        }
    }

    /**
     * 初始化没网络界面
     */
    private void initNoNetView(View mainView) {
        //
        mNetContainer = mainView.findViewById(R.id.viewstub_net_container);
        mNetContainer.inflate();
        mNetBGLayout = mainView.findViewById(R.id.net_layout);
        mNetBGLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRefreshListener != null) {
                    mRefreshListener.refresh();
                }
            }
        });
        mNetContainer.setVisibility(View.GONE);
    }

    /**
     * 显示无网络界面
     */
    public void showNoNetView() {
        mUIHandler.sendEmptyMessage(SHOWNONETVIEW);
    }

    /**
     * 显示无网络界面
     */
    private void showNoNetViewHandler() {
        mContentContainer.setVisibility(View.GONE);
        if (mLoadingContainer != null) {
            mLoadingContainer.setVisibility(View.GONE);
            mLoadImgView.clearAnimation();
        }
        if (mNetContainer != null)
            mNetContainer.setVisibility(View.VISIBLE);
    }


    /**
     * 显示主界面
     */
    public void showContentView() {
        mUIHandler.sendEmptyMessage(SHOWCONTENTVIEW);
    }

    /**
     * 显示主界面
     */
    private void showContentViewHandler() {
        mContentContainer.setVisibility(View.VISIBLE);
        if (mLoadingContainer != null) {
            mLoadingContainer.setVisibility(View.GONE);
            mLoadImgView.clearAnimation();
        }
        if (mNetContainer != null)
            mNetContainer.setVisibility(View.GONE);
    }

    public void setAddStatusBarView(boolean addStatusBarView) {
        isAddStatusBarView = addStatusBarView;
    }

    /**
     * 设置状态栏背景颜色
     *
     * @param statusBarViewBG
     */
    public void setStatusBarViewBG(int statusBarViewBG) {
        this.mStatusBarViewBG = statusBarViewBG;
    }

    public void setTitleViewId(int mTitleViewId) {
        this.mTitleViewId = mTitleViewId;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!mIsFristVisibleToUser && getUserVisibleHint() && getView() != null) {
            mIsFristVisibleToUser = true;
            isFristVisibleToUser();
        }
    }

    /**
     * 视图可见，只执行一次
     */
    protected abstract void isFristVisibleToUser();

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
    protected abstract void initViews(View mainView, Bundle savedInstanceState);

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


    public interface RefreshListener {
        void refresh();
    }

    public void setRefreshListener(RefreshListener mRefreshListener) {
        this.mRefreshListener = mRefreshListener;
    }
}
