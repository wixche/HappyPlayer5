package com.zlm.down.manager;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

import com.zlm.down.entity.DownloadTask;
import com.zlm.down.interfaces.IDownloadTaskEvent;
import com.zlm.down.thread.DownloadTaskThreadManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 任务下载管理类
 * @author: zhangliangming
 * @date: 2018-08-04 23:07
 **/
public class DownloadTaskManager {

    /**
     * 子线程用于执行耗时任务
     */
    public Handler mWorkerHandler;
    //创建异步HandlerThread
    private HandlerThread mHandlerThread;

    private Context mContext;
    /**
     * 任务队列
     */
    private List<DownloadTask> mDownloadTasks = new ArrayList<DownloadTask>();

    /**
     * 对外下载回调
     */
    private IDownloadTaskEvent mIDownloadTaskEvent;

    /**
     * 对内下载回调
     */
    private IDownloadTaskEvent mInnerIDownloadTaskEvent = new IDownloadTaskEvent() {
        @Override
        public void taskWaiting(DownloadTask task) {
            if (mIDownloadTaskEvent != null) {
                task.setStatus(DownloadTask.STATUS_WAIT);
                mIDownloadTaskEvent.taskWaiting(task);
            }
        }

        @Override
        public void taskDownloading(DownloadTask task, int downloadedSize) {
            if (task.getTaskFileSize() <= downloadedSize) {
                return;
            }
            if (mIDownloadTaskEvent != null) {
                task.setStatus(DownloadTask.STATUS_DOWNLOADING);
                mIDownloadTaskEvent.taskDownloading(task, downloadedSize);
            }
        }

        @Override
        public void taskPause(DownloadTask task, int downloadedSize) {

            removeTask(task);

            if (task.getTaskFileSize() <= downloadedSize) {
                return;
            }

            if (mIDownloadTaskEvent != null) {
                task.setStatus(DownloadTask.STATUS_PAUSE);
                mIDownloadTaskEvent
                        .taskPause(task, downloadedSize);
            }
        }

        @Override
        public void taskCancel(DownloadTask task) {
            removeTask(task);
            if (mIDownloadTaskEvent != null) {
                task.setStatus(DownloadTask.STATUS_CANCEL);
                mIDownloadTaskEvent
                        .taskCancel(task);
            }
        }

        @Override
        public void taskFinish(DownloadTask task, int downloadedSize) {

            removeTask(task);

            if (task.getTaskFileSize() > downloadedSize) {
                return;
            }

            if (mIDownloadTaskEvent != null) {
                task.setStatus(DownloadTask.STATUS_FINISH);
                mIDownloadTaskEvent
                        .taskFinish(task, downloadedSize);
            }
        }

        @Override
        public void taskError(DownloadTask task, String msg) {
            removeTask(task);

            if (mIDownloadTaskEvent != null) {
                task.setStatus(DownloadTask.STATUS_ERROR);
                mIDownloadTaskEvent
                        .taskError(task, msg);
            }
        }

        @Override
        public boolean getAskWifi() {
            if (mIDownloadTaskEvent != null) {
                return mIDownloadTaskEvent.getAskWifi();
            }
            return false;
        }

        @Override
        public int getTaskThreadDownloadedSize(DownloadTask task, int threadId) {
            if (mIDownloadTaskEvent != null) {
                return mIDownloadTaskEvent.getTaskThreadDownloadedSize(task, threadId);
            }
            return 0;
        }

        @Override
        public void taskThreadDownloading(DownloadTask task, int threadId, int downloadedSize) {
            if (mIDownloadTaskEvent != null) {
                mIDownloadTaskEvent.taskThreadDownloading(task, threadId, downloadedSize);
            }
        }

        @Override
        public void taskThreadPause(DownloadTask task, int threadId, int downloadedSize) {
            if (mIDownloadTaskEvent != null) {
                mIDownloadTaskEvent.taskThreadPause(task, threadId, downloadedSize);
            }
        }


        @Override
        public void taskThreadFinish(DownloadTask task, int threadId, int downloadedSize) {
            if (mIDownloadTaskEvent != null) {
                mIDownloadTaskEvent.taskThreadFinish(task, threadId, downloadedSize);
            }
        }

        @Override
        public void taskThreadError(DownloadTask task, int threadId, String msg) {
            if (mIDownloadTaskEvent != null) {
                mIDownloadTaskEvent.taskThreadError(task, threadId, msg);
            }
        }
    };

    public DownloadTaskManager(Context context, String threadName, IDownloadTaskEvent downloadTaskEvent) {
        this.mContext = context;
        this.mIDownloadTaskEvent = downloadTaskEvent;

        //创建异步HandlerThread
        mHandlerThread = new HandlerThread(threadName, Process.THREAD_PRIORITY_BACKGROUND);
        //必须先开启线程
        mHandlerThread.start();
        //子线程Handler
        mWorkerHandler = new Handler(mHandlerThread.getLooper());

    }

    /**
     * 释放
     */
    public void release() {
        //移除队列任务
        if (mWorkerHandler != null) {
            mWorkerHandler.removeCallbacksAndMessages(null);
        }

        //关闭线程
        if (mHandlerThread != null)
            mHandlerThread.quit();
    }

    /**
     * 添加下载任务
     *
     * @param downloadTask
     */
    public void addDownloadTask(DownloadTask downloadTask) {
        DownloadTaskThreadManager downloadTaskThreadManager = new DownloadTaskThreadManager(mContext, mWorkerHandler, downloadTask, mInnerIDownloadTaskEvent);
        downloadTask.setDownloadTaskThreadManager(downloadTaskThreadManager);
        if (mDownloadTasks.size() == 0 || mDownloadTasks.size() > 0) {
            downloadTask.setStatus(DownloadTask.STATUS_WAIT);
            if (mIDownloadTaskEvent != null) {
                mIDownloadTaskEvent.taskWaiting(downloadTask);
            }
        }
        mDownloadTasks.add(downloadTask);
        mWorkerHandler.post(downloadTask.getDownloadTaskThreadManager());
    }

    /**
     * 暂停任务
     *
     * @param downloadTaskId
     */
    public void pauseDownloadTask(String downloadTaskId) {
        for (int i = 0; i < mDownloadTasks.size(); i++) {
            DownloadTask task = mDownloadTasks.get(i);
            if (task.getTaskId().equals(downloadTaskId)) {
                if (i == 0) {

                    DownloadTaskThreadManager downloadTaskThreadManager = task.getDownloadTaskThreadManager();
                    downloadTaskThreadManager.pauseTaskThread();

                } else {
                    if (mIDownloadTaskEvent != null) {
                        mIDownloadTaskEvent.taskPause(task, 0);
                    }
                }
                if (mDownloadTasks.size() > 0) {
                    mDownloadTasks.remove(i);
                }
                break;
            }
        }
    }

    /**
     * 取消下载任务
     *
     * @param downloadTaskId
     */
    public void cancelDownloadTask(String downloadTaskId) {
        for (int i = 0; i < mDownloadTasks.size(); i++) {
            DownloadTask task = mDownloadTasks.get(i);
            if (task.getTaskId().equals(downloadTaskId)) {
                if (i == 0) {

                    DownloadTaskThreadManager downloadTaskThreadManager = task.getDownloadTaskThreadManager();
                    downloadTaskThreadManager.cancelTaskThread();

                } else {

                    if (mIDownloadTaskEvent != null) {
                        mIDownloadTaskEvent.taskCancel(task);
                    }
                }
                break;
            }
        }
    }

    /**
     * 移除任务
     *
     * @param task
     */
    private void removeTask(DownloadTask task) {
        for (int i = 0; i < mDownloadTasks.size(); i++) {
            DownloadTask temp = mDownloadTasks.get(i);
            if (temp.getTaskId().equals(task.getTaskId())) {
                if (mDownloadTasks.size() > 0) {
                    mDownloadTasks.remove(i);
                    mWorkerHandler.removeCallbacks(temp.getDownloadTaskThreadManager());
                }
                break;
            }
        }
    }

    public List<DownloadTask> getDownloadTasks() {
        return mDownloadTasks;
    }
}
