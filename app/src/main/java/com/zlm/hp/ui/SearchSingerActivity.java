package com.zlm.hp.ui;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlm.hp.adapter.SearchSingerAdapter;
import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.entity.SingerInfo;
import com.zlm.hp.http.APIHttpClient;
import com.zlm.hp.http.HttpReturnResult;
import com.zlm.hp.util.HttpUtil;
import com.zlm.hp.util.ToastUtil;
import com.zlm.hp.widget.IconfontTextView;
import com.zlm.libs.widget.SwipeBackLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: 歌手写真图片搜索
 * @author: zhangliangming
 * @date: 2018-11-18 18:12
 **/
public class SearchSingerActivity extends BaseActivity {

    /**
     *
     */
    private SwipeBackLayout mSwipeBackLayout;

    /**
     * 歌手名称
     */
    private String mSingerName;

    /**
     * 加载中布局
     */
    private RelativeLayout mLoadingContainer;
    /**
     * 加载图标
     */
    private IconfontTextView mLoadImgView;

    /**
     * 旋转动画
     */
    private Animation rotateAnimation;

    /**
     * 内容布局
     */
    private RelativeLayout mContentContainer;

    private RecyclerView mRecyclerView;
    private SearchSingerAdapter mAdapter;
    private List<SingerInfo> mDatas;

    /**
     *
     */
    private final int MESSAGE_CODE_LOADDATA = 0;

    @Override
    protected int setContentLayoutResID() {
        return R.layout.activity_search_singer;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {

        mSingerName = getIntent().getStringExtra("singerName");
        if (TextUtils.isEmpty(mSingerName)) {
            mSingerName = getString(R.string.search_singer_text);
        }

        mSwipeBackLayout = findViewById(R.id.swipeback_layout);
        mSwipeBackLayout.setSwipeBackLayoutListener(new SwipeBackLayout.SwipeBackLayoutListener() {

            @Override
            public void finishActivity() {
                SearchSingerActivity.this.setResult(LrcActivity.RESULT_SINGER_RELOAD);
                finish();
                overridePendingTransition(0, 0);
            }
        });

        TextView titleView = findViewById(R.id.title);
        titleView.setText(mSingerName);

        //返回
        ImageView backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwipeBackLayout.closeView();
            }
        });

        //
        mRecyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplication(), 2);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        // 设置布局管理器
        mRecyclerView.setLayoutManager(gridLayoutManager);

        //
        mLoadingContainer = findViewById(R.id.loading);
        mLoadImgView = findViewById(R.id.load_img);
        rotateAnimation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.anim_rotate);
        rotateAnimation.setInterpolator(new LinearInterpolator());// 匀速
        mLoadImgView.startAnimation(rotateAnimation);
        //
        mContentContainer = findViewById(R.id.content);
        showLoadingView();

        mWorkerHandler.sendEmptyMessageDelayed(MESSAGE_CODE_LOADDATA, 500);
    }

    /**
     * 显示加载中窗口
     */
    private void showLoadingView() {
        mContentContainer.setVisibility(View.GONE);
        mLoadingContainer.setVisibility(View.VISIBLE);
        mLoadImgView.clearAnimation();
        mLoadImgView.startAnimation(rotateAnimation);
    }

    @Override
    protected void handleUIMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_CODE_LOADDATA:

                if(mDatas == null || mDatas.size() == 0){
                    ToastUtil.showTextToast(mContext,HttpReturnResult.ERROR_MSG_NULLDATA);
                }

                mAdapter = new SearchSingerAdapter(mContext, mDatas,mUIHandler,mWorkerHandler);
                mRecyclerView.setAdapter(mAdapter);
                showContentView();

                break;
        }

    }

    @Override
    protected void handleWorkerMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_CODE_LOADDATA:

                APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
                ConfigInfo configInfo = ConfigInfo.obtain();
                HttpReturnResult httpReturnResult = apiHttpClient.getSingerPicList(mContext, mSingerName, configInfo.isWifi());
                if (httpReturnResult.isSuccessful()) {
                    Map<String, Object> mapResult = (Map<String, Object>) httpReturnResult.getResult();
                    mDatas = (List<SingerInfo>) mapResult.get("rows");
                }else{
                    mDatas = new ArrayList<SingerInfo>();
                }

                mUIHandler.sendEmptyMessage(MESSAGE_CODE_LOADDATA);

                break;
        }
    }

    /**
     * 显示内容窗口
     */
    private void showContentView() {
        mLoadingContainer.setVisibility(View.GONE);
        mLoadImgView.clearAnimation();
        mContentContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        mSwipeBackLayout.closeView();
    }

}
