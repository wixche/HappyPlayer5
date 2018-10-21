package com.zlm.hp.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;

import com.zlm.hp.entity.SingerInfo;
import com.zlm.hp.handler.WeakRefHandler;
import com.zlm.hp.util.ImageUtil;

import java.util.List;

/**
 * @Description: imageview透明切换图片
 * @author: zhangliangming
 * @date: 2018-10-21 15:39
 **/
public class TransitionImageView extends AppCompatImageView {

    /**
     * 处理ui任务
     */
    private WeakRefHandler mUIHandler;


    /**
     * 图片下载路径
     */
    private List<SingerInfo> mSingerInfo;

    /**
     * 当前图片索引
     */
    private int mIndex = 0;

    /**
     * 过度动画时间
     */
    private int mDuration = 500;

    private boolean isFrist = true;

    /**
     * 切换图片操作
     */
    private Runnable mChangeRunnable = new Runnable() {
        @Override
        public void run() {
            if (mSingerInfo != null && mSingerInfo.size() > 0) {

                Drawable[] drawables = new Drawable[2];

                int curIndex = mIndex;
                int urlSize = mSingerInfo.size();
                if (curIndex >= urlSize) {
                    curIndex = 0;
                    mIndex = 0;
                }


                String curImageUrl = mSingerInfo.get(curIndex).getImageUrl();
                Bitmap curBitmap = ImageUtil.mImageCache.get(curImageUrl.hashCode() + "");
                if (curBitmap != null) {

                    int preIndex = curIndex - 1;
                    if (preIndex < 0) {
                        preIndex = urlSize - 1;
                    }

                    String preImageUrl = mSingerInfo.get(preIndex).getImageUrl();
                    Bitmap preBitmap = ImageUtil.mImageCache.get(preImageUrl.hashCode() + "");


                    if (preBitmap == null || (isFrist && mIndex == 0)) {
                        drawables[0] = new BitmapDrawable(curBitmap);
                    } else {
                        drawables[0] = new BitmapDrawable(preBitmap);
                    }

                    drawables[1] = new BitmapDrawable(curBitmap);
                    TransitionDrawable transitionDrawable = new TransitionDrawable(drawables);
                    setBackground(transitionDrawable);
                    transitionDrawable.startTransition(mDuration);

                    setVisibility(View.VISIBLE);
                    mIndex++;
                }

                mUIHandler.postDelayed(mChangeRunnable, 1000 * 10);
            }
        }
    };

    public TransitionImageView(Context context) {
        super(context);
        init(context);
    }

    public TransitionImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TransitionImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {

        //创建ui handler
        mUIHandler = new WeakRefHandler(Looper.getMainLooper(), this, new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                handleUIMessage(msg);
                return true;
            }

            private void handleUIMessage(Message msg) {

            }
        });

    }

    /**
     * 初始化数据
     */
    public void initData(List<SingerInfo> singerInfos, boolean askWifi) {
        if (mSingerInfo == null || mSingerInfo.size() == 0) {
            this.mSingerInfo = singerInfos;
            start(askWifi);
        }
    }

    /**
     * 开始
     */
    private void start(boolean askWifi) {
        mUIHandler.post(mChangeRunnable);
    }

    /**
     * 结束
     */
    public void resetData() {
        if (mSingerInfo != null)
            mSingerInfo.clear();
        mIndex = 0;
        isFrist = true;
        setBackground(new BitmapDrawable());
        mUIHandler.removeCallbacks(mChangeRunnable);
    }

    public void release() {
        //移除队列任务
        if (mUIHandler != null) {
            mUIHandler.removeCallbacksAndMessages(null);
        }

    }
}
