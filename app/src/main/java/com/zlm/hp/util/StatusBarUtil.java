package com.zlm.hp.util;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


/**
 * 状态栏处理类
 * Created by zhangliangming on 2018-08-04.
 */

public class StatusBarUtil {

    /**
     * 是否添加状态栏
     *
     * @return
     */
    public static boolean isAddStatusBar() {
        boolean isAddStatusBar = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isAddStatusBar = true;
        }
        return isAddStatusBar;
    }

    /**
     * 是否添加状态栏
     *
     * @param window
     * @return
     */
    public static boolean isAddStatusBar(Window window) {
        boolean isAddStatusBar = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isAddStatusBar = true;


            //使得布局延伸到状态栏和导航栏区域
            View decorView = window.getDecorView();
            int systemUiVisibility = decorView.getSystemUiVisibility();
            decorView.setSystemUiVisibility(systemUiVisibility | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            //透明状态栏/导航栏
            window.setStatusBarColor(Color.TRANSPARENT);

        }
        return isAddStatusBar;
    }

    /**
     * @Description: 获取状态栏高度
     * @Param: context
     * @Return:
     * @Author: zhangliangming
     * @Date: 2017/7/15 19:30
     * @Throws:
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
