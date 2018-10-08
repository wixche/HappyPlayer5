package com.zlm.hp;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.zlm.down.entity.DownloadTask;
import com.zlm.down.interfaces.IDownloadTaskEvent;
import com.zlm.down.manager.DownloadTaskManager;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.util.ResourceUtil;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

/**
 * @Description: 测试下载类
 * @author: zhangliangming
 * @date: 2018-08-05 10:50
 **/
@RunWith(AndroidJUnit4.class)
public class DownloadTaskTest {
    /**
     * @throws
     * @Description: 下载任务
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-08-05 10:50
     */
    @Test
    public void DownloadTask() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        DownloadTaskManager downloadTaskManager = new DownloadTaskManager(appContext,"downloadTask", new IDownloadTaskEvent() {
            @Override
            public void taskWaiting(DownloadTask task) {
                //更新当前任务的状态
                Log.d("Test", "taskWaiting->" + task.getTaskName());
            }

            @Override
            public void taskDownloading(DownloadTask task, int downloadedSize) {
                //更新当前任务的进度
                Log.d("Test", "taskDownloading->" + task.getTaskName() + " " + ((float) downloadedSize / task.getTaskFileSize()) * 100);
            }

            @Override
            public void taskPause(DownloadTask task, int downloadedSize) {
                //更新当前任务的状态
            }

            @Override
            public void taskCancel(DownloadTask task) {
                //更新当前任务的状态
            }

            @Override
            public void taskFinish(DownloadTask task, int downloadedSize) {
                //更新当前任务的状态
                Log.d("Test", "taskFinish->" + task.getTaskName());
            }

            @Override
            public void taskError(DownloadTask task, String msg) {
                //更新当前任务的状态
            }

            @Override
            public boolean getAskWifi() {
                return false;
            }

            @Override
            public int getTaskThreadDownloadedSize(DownloadTask task, int threadId) {
                //获取之前线程任务已下载完成的进度
                return 0;
            }

            @Override
            public void taskThreadDownloading(DownloadTask task, int threadId, int downloadedSize) {
                //更新每个线程任务的下载进度
                //Log.d("Test", "taskDownloading->" + threadId + ":" + ((float) downloadedSize / task.getFileSize()) * 100);
            }

            @Override
            public void taskThreadPause(DownloadTask task, int threadId, int downloadedSize) {
                //更新每个线程任务的状态
            }

            @Override
            public void taskThreadFinish(DownloadTask task, int threadId, int downloadedSize) {
                //更新每个线程任务的状态
                // Log.d("Test", "taskThreadFinish->" + threadId + ":" + ((float) downloadedSize / task.getFileSize()) * 100);

            }

            @Override
            public void taskThreadError(DownloadTask task, int threadId, String msg) {
                //更新每个线程任务的状态
            }
        });

        DownloadTask downloadTask2 = new DownloadTask();
        downloadTask2.setTaskName("张学友 - 等你等到我心痛2");
        downloadTask2.setTaskExt("mkv");
        downloadTask2.setTaskId("5F8393A55D5762A63F1A5E92B46E575E");

        String fileName2 = downloadTask2.getTaskName();
        String taskPath2 = ResourceUtil.getFilePath(appContext, ResourceConstants.PATH_VIDEO, fileName2 + "." + downloadTask2.getTaskExt());
        String taskTempPath2 = ResourceUtil.getFilePath(appContext, ResourceConstants.PATH_VIDEO_TEMP, fileName2 + ".temp");

        downloadTask2.setTaskPath(taskPath2);
        downloadTask2.setTaskTempPath(taskTempPath2);
        downloadTask2.setTaskUrl("http://fs.mv.web.kugou.com/201808050215/a842a9d7b8b048433ac1d90a3cf188a1/G030/M06/14/0B/Xg0DAFXeLMiAMle_ATon28lO__I941.mkv");
        downloadTask2.setThreadNum(3);
        downloadTask2.setCreateTime(new Date());


        DownloadTask downloadTask = new DownloadTask();
        downloadTask.setTaskName("张学友 - 等你等到我心痛");
        downloadTask.setTaskExt("mkv");
        downloadTask.setTaskId("5F8393A55D5762A63F1A5E92B46E575E");

        String fileName = downloadTask.getTaskName();
        String taskPath = ResourceUtil.getFilePath(appContext, ResourceConstants.PATH_VIDEO, fileName + "." + downloadTask.getTaskExt());
        String taskTempPath = ResourceUtil.getFilePath(appContext, ResourceConstants.PATH_VIDEO_TEMP, fileName + ".temp");

        downloadTask.setTaskPath(taskPath);
        downloadTask.setTaskTempPath(taskTempPath);
        downloadTask.setTaskUrl("http://fs.mv.web.kugou.com/201808050215/a842a9d7b8b048433ac1d90a3cf188a1/G030/M06/14/0B/Xg0DAFXeLMiAMle_ATon28lO__I941.mkv");
        downloadTask.setThreadNum(3);
        downloadTask.setCreateTime(new Date());

        downloadTaskManager.addDownloadTask(downloadTask);
        downloadTaskManager.addDownloadTask(downloadTask2);
    }

}
