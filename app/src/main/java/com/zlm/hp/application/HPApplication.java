package com.zlm.hp.application;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import com.dou361.dialogui.DialogUIUtils;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.tencent.bugly.crashreport.CrashReport;
import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.constants.Constants;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.db.DBHelper;
import com.zlm.hp.manager.ActivityManager;
import com.zlm.hp.service.FloatService;
import com.zlm.hp.ui.R;
import com.zlm.hp.util.ApkUtil;
import com.zlm.hp.util.CodeLineUtil;
import com.zlm.hp.util.ContextUtil;
import com.zlm.hp.util.ResourceUtil;
import com.zlm.hp.util.ToastUtil;
import com.zlm.hp.util.ZLog;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by zhangliangming on 2018-07-29.
 */

public class HPApplication extends MultiDexApplication {

    private Handler mHandler;

    /**
     * 用来后续监控可能发生泄漏的对象
     */
    private static RefWatcher sRefWatcher;
    /**
     * 用于记录ativity的个数
     */
    private int mActivityCounter = 0;
    /**
     * 全局收集错误信息
     */
    private Thread.UncaughtExceptionHandler mErrorHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            e.printStackTrace();

            //修改保存路径
            initLog(ResourceConstants.PATH_CRASH);
            //输出配置信息
            String codeLineInfo = new CodeLineUtil().getCodeLineInfo();
            ZLog.logBuildInfo(getApplicationContext(), codeLineInfo);
            ZLog.e(codeLineInfo, "UncaughtException: ", e.getMessage());

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    ToastUtil.showTextToast(getApplicationContext(), getString(R.string.exit_tip));
                    //关闭app
                    ActivityManager.getInstance().exit();
                }
            }, 5000);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        mHandler = new Handler(Looper.getMainLooper());

        //全局收集
        Thread.setDefaultUncaughtExceptionHandler(mErrorHandler);

        //初始化日志
        initLog(ResourceConstants.PATH_LOGCAT);

        //初始化bugly
        initBugly();

        //初始化LeakCanary
        initLeakCanary();

        // 获取当前进程名
        String processName = getProcessName(android.os.Process.myPid());
        if (getApplicationContext().getPackageName().equals(processName)) {
            //主进程
            //输出配置信息
            ZLog.logBuildInfo(getApplicationContext(), new CodeLineUtil().getCodeLineInfo());
            //初始化数据库
            initDB();
            registerActivityLifecycleCallbacks();
        }

        //封装全局context
        ContextUtil.init(getApplicationContext());
        //封装弹出窗口context
        DialogUIUtils.init(getApplicationContext());
    }

    /**
     * 注册activity
     */
    private void registerActivityLifecycleCallbacks() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                mActivityCounter++;
                stopFloatService();
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                mActivityCounter--;
                startFloatService();
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }


    public void startFloatService() {
        if (mActivityCounter <= 0) {
            ZLog.i(new CodeLineUtil().getCodeLineInfo(), "app background");

            if (!isServiceRunning(FloatService.class.getName())) {
                ConfigInfo configInfo = ConfigInfo.obtain();
                if (configInfo.isShowDesktopLrc()) {
                    //启动悬浮窗口服务
                    Intent floatServiceIntent = new Intent(getApplicationContext(), FloatService.class);
                    startService(floatServiceIntent);
                }
            }
        }
    }

    public void stopFloatService() {
        if (isServiceRunning(FloatService.class.getName())) {
            //关闭悬浮窗口服务
            Intent floatServiceIntent = new Intent(getApplicationContext(), FloatService.class);
            stopService(floatServiceIntent);
        }
    }

    /**
     * 判断服务是否正在运行
     *
     * @param serviceName
     * @return
     */
    private boolean isServiceRunning(String serviceName) {
        android.app.ActivityManager manager = (android.app.ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (android.app.ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 初始化数据库
     */
    private void initDB() {
        DBHelper.getInstance(getApplicationContext());
    }

    /**
     * @throws
     * @Description: 初始化LeakCanary
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-08-05 15:24
     */
    private void initLeakCanary() {
        //初始化LeakCanary
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }

        sRefWatcher = LeakCanary.install(this);
    }

    /**
     * 用来后续监控可能发生泄漏的对象
     *
     * @return
     */
    public static RefWatcher getRefWatcher() {
        return sRefWatcher;
    }

    /**
     * @throws
     * @Description: 初始化日志
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-07-29 11:01
     */
    private void initLog(String path) {
        ZLog.init(getApplicationContext(), ResourceUtil.getFilePath(getApplicationContext(), path), Constants.APPNAME);
    }

    /**
     * @throws
     * @Description: 初始化bugly
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-07-29 10:06
     */
    private void initBugly() {
        CrashReport.initCrashReport(getApplicationContext(), "969a9196a8", false);
        CrashReport.putUserData(getApplicationContext(), "DeviceID", ApkUtil.getUniquePsuedoID(getApplicationContext()));
    }

    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }


}
