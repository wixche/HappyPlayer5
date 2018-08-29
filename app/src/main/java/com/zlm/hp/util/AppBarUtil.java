package com.zlm.hp.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Method;


/**
 * app状态栏和底部导航菜单
 * Created by zhangliangming on 2018-08-04.
 */

public class AppBarUtil {

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
     * 初始化bar
     *
     * @param window
     * @return
     */
    public static void initBar(Window window) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS );
           // window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            //使得布局延伸到状态栏和导航栏区域
            View decorView = window.getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN /**| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE **/);

            //透明状态栏/导航栏
            window.setStatusBarColor(Color.TRANSPARENT);
            //window.setNavigationBarColor(Color.TRANSPARENT);

        }

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

    /**
     * @throws
     * @Description: 获取底部navigationBar高度
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-08-29 21:46
     */
    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    /**
     * @throws
     * @Description: 获取设备是否存在NavigationBar
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-08-29 21:47
     */
    public static boolean isAddNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hasNavigationBar;
    }
}
