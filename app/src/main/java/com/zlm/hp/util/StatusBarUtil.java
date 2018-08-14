package com.zlm.hp.util;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;

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
            //透明状态栏
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //设置状态栏的颜色
            window.setStatusBarColor(Color.TRANSPARENT);

            //修复android7.0半透明问题
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    Field field = window.getDecorView().getClass().getDeclaredField("mSemiTransparentStatusBarColor");  //获取特定的成员变量
                    field.setAccessible(true);   //设置对此属性的可访问性
                    field.setInt(window.getDecorView(), Color.TRANSPARENT);  //修改属性值

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
