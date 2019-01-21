package com.zlm.hp.util;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

/**
 * https://blog.csdn.net/zuo_er_lyf/article/details/82259239
 * https://www.cnblogs.com/StephenWu/p/5838943.html
 * 解决WebView中输入时弹出软键盘，挡住输入框的问题
 */

public class AndroidBug5497WorkaroundUtils {

    // For more information, see https://code.google.com/p/android/issues/detail?id=5497
// To use this class, simply invoke assistActivity() on an Activity that already has its content view set.

    public static void assistActivity(final Activity activity) {
        new AndroidBug5497WorkaroundUtils(activity);
    }

    private View mChildOfContent;
    private int usableHeightPrevious;
    private FrameLayout.LayoutParams frameLayoutParams;

    private AndroidBug5497WorkaroundUtils(final Activity activity) {

        FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
        mChildOfContent = content.getChildAt(0);
        mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                possiblyResizeChildOfContent(activity);
            }
        });
        frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();


    }

    private void possiblyResizeChildOfContent(Activity activity) {
        int usableHeightNow = computeUsableHeight();

        if (usableHeightNow != usableHeightPrevious) {
            //获取NavigationBar的高度
            int navigateHeight = AppBarUtil.getNavigationBarHeight(activity);
            boolean hasSoftNavigateBar = AppBarUtil.isShowNavigationBar(activity);
            int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
            if (heightDifference > (usableHeightSansKeyboard / 4) || hasSoftNavigateBar) {
                if (heightDifference == 0) {
                    heightDifference = navigateHeight;
                }
                // keyboard probably just became visible
                frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
            } else {
                // keyboard probably just became hidden
                frameLayoutParams.height = usableHeightSansKeyboard;
            }
            mChildOfContent.requestLayout();
            usableHeightPrevious = usableHeightNow;
        }
    }

    private int computeUsableHeight() {
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        return r.bottom;
    }

}
