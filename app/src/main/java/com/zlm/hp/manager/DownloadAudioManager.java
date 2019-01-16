package com.zlm.hp.manager;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

import com.zlm.down.entity.DownloadTask;
import com.zlm.down.entity.DownloadThreadInfo;
import com.zlm.down.interfaces.IDownloadTaskEvent;
import com.zlm.down.manager.DownloadTaskManager;
import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.db.util.AudioInfoDB;
import com.zlm.hp.db.util.DownloadTaskDB;
import com.zlm.hp.db.util.DownloadThreadInfoDB;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.http.APIHttpClient;
import com.zlm.hp.receiver.AppSystemReceiver;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.util.CodeLineUtil;
import com.zlm.hp.util.HttpUtil;
import com.zlm.hp.util.ResourceUtil;
import com.zlm.hp.util.ZLog;

import java.util.Date;
import java.util.List;

/**
 * @Description: 下载管理
 * @author: zhangliangming
 * @date: 2018-12-18 21:16
 **/
public class DownloadAudioManager {

    /**
     * 子线程用于执行耗时任务
     */
    private Handler mWorkerHandler;
    //创建异步HandlerThread
    private HandlerThread mHandlerThread;


    private static DownloadAudioManager _DownloadAudioManager;

    /**
     * 下载管理器
     */
    private static DownloadTaskManager mDownloadTaskManager;

    /**
     * 线程个数
     */
    public static final int mThreadNum = 2;

    /**
     *
     */
    private static Context mContext;

    private DownloadAudioManager(final Context context) {
        this.mContext = context;

        //创建异步HandlerThread
        mHandlerThread = new HandlerThread("downloadAudioThread", Process.THREAD_PRIORITY_BACKGROUND);
        //必须先开启线程
        mHandlerThread.start();
        //子线程Handler
        mWorkerHandler = new Handler(mHandlerThread.getLooper());
        //
        mDownloadTaskManager = new DownloadTaskManager(context, "downloadAudioManager", new IDownloadTaskEvent() {
            @Override
            public void taskWaiting(DownloadTask task) {
                AudioBroadcastReceiver.sendDownloadWaitReceiver(mContext, task);
            }

            @Override
            public void taskDownloading(DownloadTask task, int downloadedSize) {

                ZLog.d(new CodeLineUtil().getCodeLineInfo(), "task taskDownloading ->" + task.getTaskName() + " " + downloadedSize);
                AudioBroadcastReceiver.sendDownloadingSongReceiver(mContext, task);
            }

            @Override
            public void taskPause(DownloadTask task, int downloadedSize) {

                ZLog.d(new CodeLineUtil().getCodeLineInfo(), "task taskPause ->" + task.getTaskName() + " " + downloadedSize);
                AudioBroadcastReceiver.sendDownloadPauseReceiver(mContext, task);
            }

            @Override
            public void taskCancel(DownloadTask task) {
                //删除任务
                if (DownloadTaskDB.isExists(mContext, task.getTaskId(), mThreadNum)) {
                    DownloadTaskDB.delete(mContext, task.getTaskId(), mThreadNum);
                }
                ZLog.d(new CodeLineUtil().getCodeLineInfo(), "task taskCancel ->" + task.getTaskName());
                AudioBroadcastReceiver.sendDownloadCancelReceiver(mContext, task);
            }

            @Override
            public void taskFinish(DownloadTask task, int downloadedSize) {

                //添加本地歌曲
                if (AudioInfoDB.isDownloadAudioExists(mContext, task.getTaskId())) {
                    AudioInfoDB.addDownloadedAudio(mContext, task.getTaskId(), true);
                }
                ZLog.d(new CodeLineUtil().getCodeLineInfo(), "task taskFinish ->" + task.getTaskName() + " " + downloadedSize);
                AudioBroadcastReceiver.sendDownloadFinishReceiver(mContext, task);
            }

            @Override
            public void taskError(DownloadTask task, String msg) {
                ZLog.d(new CodeLineUtil().getCodeLineInfo(), "task taskError ->" + task.getTaskName());
                AppSystemReceiver.sendToastErrorMsgReceiver(mContext, msg);
            }

            @Override
            public boolean getAskWifi() {
                ConfigInfo configInfo = ConfigInfo.obtain();
                return configInfo.isWifi();
            }

            @Override
            public int getTaskThreadDownloadedSize(DownloadTask task, int threadId) {
                if (DownloadThreadInfoDB.isExists(mContext, task.getTaskId(), mThreadNum, threadId)) {
                    //任务存在
                    DownloadThreadInfo downloadThreadInfo = DownloadThreadInfoDB.getDownloadThreadInfo(mContext, task.getTaskId(), mThreadNum, threadId);
                    if (downloadThreadInfo != null) {
                        ZLog.d(new CodeLineUtil().getCodeLineInfo(), "task getTaskThreadDownloadedSize -> 下载任务名称：" + task.getTaskName() + " 子任务线程id: " + threadId + " 已下载大小：" + downloadThreadInfo.getDownloadedSize());
                        return downloadThreadInfo.getDownloadedSize();
                    }
                }
                return 0;
            }

            @Override
            public void taskThreadDownloading(DownloadTask task, int threadId, int downloadedSize) {
                DownloadThreadInfo downloadThreadInfo = new DownloadThreadInfo();
                downloadThreadInfo.setDownloadedSize(downloadedSize);
                downloadThreadInfo.setThreadId(threadId);
                downloadThreadInfo.setTaskId(task.getTaskId());
                downloadThreadInfo.setThreadNum(mThreadNum);

                if (DownloadThreadInfoDB.isExists(mContext, task.getTaskId(), mThreadNum, threadId)) {
                    //任务存在
                    DownloadThreadInfoDB.update(mContext, task.getTaskId(), mThreadNum, threadId, downloadedSize);
                } else {
                    //任务不存在
                    DownloadThreadInfoDB.add(mContext, downloadThreadInfo);
                }
            }

            @Override
            public void taskThreadPause(DownloadTask task, int threadId, int downloadedSize) {

            }

            @Override
            public void taskThreadFinish(DownloadTask task, int threadId, int downloadedSize) {

                DownloadThreadInfo downloadThreadInfo = new DownloadThreadInfo();
                downloadThreadInfo.setDownloadedSize(downloadedSize);
                downloadThreadInfo.setThreadId(threadId);
                downloadThreadInfo.setTaskId(task.getTaskId());
                downloadThreadInfo.setThreadNum(mThreadNum);
                if (DownloadThreadInfoDB.isExists(mContext, task.getTaskId(), mThreadNum, threadId)) {
                    //任务存在
                    DownloadThreadInfoDB.update(mContext, task.getTaskId(), mThreadNum, threadId, downloadedSize);
                } else {
                    //任务不存在
                    DownloadThreadInfoDB.add(mContext, downloadThreadInfo);
                }

                //更新任务
                if (DownloadTaskDB.isExists(mContext, task.getTaskId(), mThreadNum)) {
                    //任务存在
                    DownloadTaskDB.update(mContext, task.getTaskId(), mThreadNum, DownloadTask.STATUS_FINISH);
                } else {
                    //任务不存在
                    task.setStatus(DownloadTask.STATUS_FINISH);
                    DownloadTaskDB.add(mContext, task);
                }

            }

            @Override
            public void taskThreadError(DownloadTask task, int threadId, String msg) {

            }
        });
    }

    public static DownloadAudioManager getInstance(Context context) {
        if (_DownloadAudioManager == null) {
            _DownloadAudioManager = new DownloadAudioManager(context);
        }
        return _DownloadAudioManager;
    }

    /**
     * @param audioInfo
     */
    public void addTask(final AudioInfo audioInfo) {

        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        ConfigInfo configInfo = ConfigInfo.obtain();

        apiHttpClient.getSongInfo(mContext, audioInfo.getHash(), audioInfo, configInfo.isWifi());

        final DownloadTask downloadTask = new DownloadTask();
        downloadTask.setTaskName(audioInfo.getTitle());
        downloadTask.setTaskExt(audioInfo.getFileExt());
        downloadTask.setTaskId(audioInfo.getHash());

        String fileName = audioInfo.getTitle();
        String taskPath = ResourceUtil.getFilePath(mContext, ResourceConstants.PATH_AUDIO, fileName + "." + downloadTask.getTaskExt());
        String taskTempPath = ResourceUtil.getFilePath(mContext, ResourceConstants.PATH_AUDIO_TEMP, audioInfo.getHash() + ".temp");

        downloadTask.setTaskPath(taskPath);
        downloadTask.setTaskTempPath(taskTempPath);
        downloadTask.setThreadNum(mThreadNum);
        downloadTask.setCreateTime(new Date());

        //添加音频
        boolean flag = isDownloadAudioExists(audioInfo.getHash());
        if (!flag) {
            //避免重复添加任务
            AudioInfoDB.addDownloadAudio(mContext, audioInfo, true);
            //添加任务
            DownloadTaskDB.add(mContext, downloadTask);
        }
        //重新获取歌曲下载路径，防止下载地址失效
        mWorkerHandler.post(new Runnable() {
            @Override
            public void run() {
                getAudioDownloadUrl(downloadTask, audioInfo);
            }
        });
    }

    /**
     * @throws
     * @Description: 重新获取下载路径
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-12-19 21:11
     */
    private void getAudioDownloadUrl(DownloadTask downloadTask, AudioInfo audioInfo) {
        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        apiHttpClient.getSongInfo(mContext, audioInfo.getHash(), audioInfo, ConfigInfo.obtain().isWifi());
        downloadTask.setTaskUrl(audioInfo.getDownloadUrl());

        //下载任务
        mDownloadTaskManager.addDownloadTask(downloadTask);
    }

    /**
     * 暂停任务
     *
     * @param
     */
    public void pauseTask(String taskId) {
        mDownloadTaskManager.pauseDownloadTask(taskId);
    }

    /**
     * 取消任务
     *
     * @param
     */
    public void cancelTask(String taskId) {
        mDownloadTaskManager.cancelDownloadTask(taskId);
    }

    /**
     * 下载任务是否存在
     *
     * @param taskId
     * @return
     */
    public boolean isDownloadAudioExists(String taskId) {
        if (AudioInfoDB.isDownloadAudioExists(mContext, taskId)) {
            return true;
        }
        List<DownloadTask> downloadTasks = mDownloadTaskManager.getDownloadTasks();
        if (downloadTasks != null) {
            for (int i = 0; i < downloadTasks.size(); i++) {
                DownloadTask downloadTask = downloadTasks.get(i);
                if (downloadTask.getTaskId().equals(taskId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取下载任务
     *
     * @param hash
     * @return
     */
    public DownloadTask getDownloadTask(String hash) {
        List<DownloadTask> downloadTasks = mDownloadTaskManager.getDownloadTasks();
        if (downloadTasks != null) {
            for (int i = 0; i < downloadTasks.size(); i++) {
                DownloadTask downloadTask = downloadTasks.get(i);
                if (downloadTask.getTaskId().equals(hash)) {
                    return downloadTask;
                }
            }
        }
        return null;
    }

    /**
     * 释放
     */
    public void release() {

        mDownloadTaskManager.release();

        //移除队列任务
        if (mWorkerHandler != null) {
            mWorkerHandler.removeCallbacksAndMessages(null);
        }

        //关闭线程
        if (mHandlerThread != null)
            mHandlerThread.quit();
    }
}
