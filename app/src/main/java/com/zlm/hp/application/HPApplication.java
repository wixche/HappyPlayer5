package com.zlm.hp.application;

import android.os.Handler;
import android.os.Looper;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import com.dou361.dialogui.DialogUIUtils;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.tencent.bugly.crashreport.CrashReport;
import com.zlm.hp.constants.Constants;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.db.DBHelper;
import com.zlm.hp.manager.ActivityManager;
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
        }

        //封装全局context
        ContextUtil.init(getApplicationContext());
        //封装弹出窗口context
        DialogUIUtils.init(getApplicationContext());
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
