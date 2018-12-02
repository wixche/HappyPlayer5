package com.zlm.hp.manager;

import android.app.Activity;

import java.util.LinkedList;
import java.util.List;

/**
 * activity的管理:退出时，遍历所有的activity，并finish,最后退出系统。
 *
 * @author Administrator 最近修改时间2013年12月10日
 */
public class ActivityManager {

    /**
     * activity列表
     */
    private List<Activity> activityList = new LinkedList<Activity>();
    private static ActivityManager instance = null;

    private ActivityManager() {

    }

    public static ActivityManager getInstance() {
        if (instance == null) {
            instance = new ActivityManager();
        }
        return instance;
    }

    /**
     * 添加
     *
     * @param activity
     */
    public void addActivity(Activity activity) {
        activityList.add(activity);
    }

    public void removeActivity(Activity activity) {
        activityList.remove(activity);
    }

    /**
     * 退出
     */
    public void exit() {
        for (Activity activity : activityList) {
            if (!activity.isFinishing() && activity != null) {
                activity.finish();
            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}
