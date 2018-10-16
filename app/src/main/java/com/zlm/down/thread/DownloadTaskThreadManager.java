package com.zlm.down.thread;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.zlm.down.entity.DownloadTask;
import com.zlm.down.interfaces.IDownloadTaskEvent;
import com.zlm.down.interfaces.IDownloadThreadEvent;
import com.zlm.hp.http.HttpClient;
import com.zlm.hp.http.HttpReturnResult;
import com.zlm.hp.util.FileUtil;
import com.zlm.hp.util.HttpUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * @Description: 下载任务线程管理
 * @author: zhangliangming
 * @date: 2018-08-05 0:19
 **/

public class DownloadTaskThreadManager implements Runnable {

    private Context mContext;
    /**
     * 任务线程
     */
    private List<DownloadTaskThread> mDownloadTaskThreads = new ArrayList<DownloadTaskThread>();
    /**
     * 任务
     */
    private DownloadTask mDownloadTask;

    private Handler mWorkerHandler;
    /**
     * 是否能下载任务
     */
    private volatile boolean isCanDownload = false;
    /**
     * 下载任务回调
     */
    private IDownloadTaskEvent mIDownloadTaskEvent;

    /**
     * 下载线程任务回调
     */
    private IDownloadThreadEvent mIDownloadThreadEvent = new IDownloadThreadEvent() {
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
            synchronized (this) {
                if (mIDownloadTaskEvent != null) {
                    mIDownloadTaskEvent.taskThreadFinish(task, threadId, downloadedSize);
                }
                int taskDownloadedSize = getTaskDownloadedSize();
                if (taskDownloadedSize >= mDownloadTask.getTaskFileSize() && !isCanDownload) {
                    isCanDownload = true;
                    mUpdateDownloadThread.interrupt();
                    if (mIDownloadTaskEvent != null) {
                        mIDownloadTaskEvent.taskFinish(task, taskDownloadedSize);
                    }

                    if (task.getTaskPath() != null) {
                        // 临时文件复制到真正的路径
                        copyFile(task.getTaskTempPath(), task.getTaskPath());
                    }
                    synchronized (mWorkerHandler) {
                        mWorkerHandler.notifyAll();
                    }
                }
            }
        }

        @Override
        public void taskThreadError(DownloadTask task, int threadId, String msg) {

            if (mIDownloadTaskEvent != null) {
                mIDownloadTaskEvent.taskThreadError(task, threadId, msg);
                mIDownloadTaskEvent.taskError(task, msg);
            }
            synchronized (mWorkerHandler) {
                mWorkerHandler.notifyAll();
            }
        }
    };

    public DownloadTaskThreadManager(Context context, Handler workerHandler, DownloadTask downloadTask, IDownloadTaskEvent downloadTaskEvent) {
        this.mContext = context;
        this.mWorkerHandler = workerHandler;
        this.mDownloadTask = downloadTask;
        this.mIDownloadTaskEvent = downloadTaskEvent;
    }

    @Override
    public void run() {
        boolean askWifi = false;
        if (mIDownloadTaskEvent != null) {
            askWifi = mIDownloadTaskEvent.getAskWifi();
        }
        boolean resultFlag = startDownloadTask(mContext, askWifi);
        if (!resultFlag) {
            return;
        }
        mUpdateDownloadThread.start();
        try {
            synchronized (mWorkerHandler) {
                mWorkerHandler.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载任务
     *
     * @param context
     */
    private boolean startDownloadTask(Context context, boolean askWifi) {
        // 1.获取文件的长度
        // 2.对文件进行多线程下载
        try {
            String taskUrl = mDownloadTask.getTaskUrl();
            if (TextUtils.isEmpty(taskUrl)) {
                if (mIDownloadTaskEvent != null) {
                    mIDownloadTaskEvent.taskError(mDownloadTask, HttpReturnResult.ERROR_MSG_NULLURL);
                }


                return false;
            }

            // 1获取文件的长度
            int fileLength = (int) mDownloadTask.getTaskFileSize();
            if (fileLength == 0)
                fileLength = getFileLength(taskUrl);
            if (fileLength <= 0) {
                // 获取文件的长度失败

                if (mIDownloadTaskEvent != null) {
                    mIDownloadTaskEvent.taskError(mDownloadTask, HttpReturnResult.ERROR_FILE_ZERO);
                }

                return false;
            }

            //如果根目录不为空，则判断空间大小是否能继续下载
            String rootPath = mDownloadTask.getRootPath();
            if (!TextUtils.isEmpty(rootPath)) {
                long memorySize = FileUtil.getMemorySize(rootPath);
                if (2 * fileLength > memorySize) {
                    if (mIDownloadTaskEvent != null) {
                        mIDownloadTaskEvent.taskError(mDownloadTask, HttpReturnResult.ERROR_MEMORY);
                    }


                    return false;
                }
            }

            mDownloadTask.setTaskFileSize(fileLength);
            //
            File destFile = new File(mDownloadTask.getTaskTempPath());
            if (!destFile.getParentFile().exists()) {
                destFile.getParentFile().mkdirs();
            }
            //目标文件不存在
            if (!destFile.exists()) {

                // 临时文件
                RandomAccessFile tempFile = new RandomAccessFile(
                        mDownloadTask.getTaskTempPath(), "rw");
                tempFile.setLength(fileLength);
                tempFile.close();
            }


            // 2对文件进行多线程下载
            int threadNum = mDownloadTask.getThreadNum();
            int avg = fileLength / threadNum;
            for (int i = 0; i < threadNum; i++) {
                int threadId = (i + 1);
                int startPos = i * avg;
                int endPos = 0;
                if (i == (threadNum - 1)) {
                    endPos = fileLength;
                } else {
                    endPos = startPos + avg;
                }

                //
                DownloadTaskThread taskThread = new DownloadTaskThread(context, threadId, startPos,
                        endPos, mDownloadTask, mIDownloadThreadEvent, askWifi);
                mDownloadTaskThreads.add(taskThread);
                taskThread.start();

            }
        } catch (Exception e) {
            e.printStackTrace();
            // 下载出错
            if (mIDownloadTaskEvent != null) {
                mIDownloadTaskEvent.taskError(mDownloadTask, HttpReturnResult.ERROR_MSG_NET);
            }
            return false;
        }
        return true;
    }

    /**
     * 更新下载进程
     */
    private Thread mUpdateDownloadThread = new Thread() {
        @Override
        public void run() {
            while (!isCanDownload) {
                synchronized (this) {
                    int taskDownloadedSize = 0;
                    try {
                        taskDownloadedSize = getTaskDownloadedSize();
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //更新任务线程
                    if (mIDownloadTaskEvent != null && !isCanDownload) {
                        if (taskDownloadedSize != 0)
                            mIDownloadTaskEvent.taskDownloading(mDownloadTask, taskDownloadedSize);
                    }
                }
            }
        }
    };

    /**
     * 获取下载文件的长度
     *
     * @param downloadUrl
     * @return
     * @throws Exception
     * @author zhangliangming
     * @date 2017年7月8日
     */
    private int getFileLength(String downloadUrl) {
        if (downloadUrl.startsWith("https://")) {
            return getHttpsFileLength(downloadUrl);
        }
        return getHttpFileLength(downloadUrl);
    }

    /**
     * 获取https文件长度
     *
     * @param downloadUrl
     * @return
     */
    private int getHttpsFileLength(String downloadUrl) {
        int length = 0;
        try {
            URL url = new URL(downloadUrl);
            // 创建SSLContext
            SSLContext sslContext = SSLContext.getInstance("SSL");
            TrustManager[] tm = {new HttpClient.IgnoreSSLTrustManager()};
            // 初始化
            sslContext.init(null, tm, new SecureRandom());
            // 获取SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            HttpUtil.seURLConnectiontHeader(conn);
            // https忽略证书
            conn.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    // 强行返回true 即验证成功
                    return true;
                }
            });
            conn.setSSLSocketFactory(ssf);

            length = conn.getContentLength();
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return length;
    }

    /**
     * 获取http文件长度
     *
     * @param downloadUrl
     * @return
     */
    private int getHttpFileLength(String downloadUrl) {
        int length = 0;
        try {
            URL url = new URL(downloadUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            HttpUtil.seURLConnectiontHeader(conn);
            length = conn.getContentLength();
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return length;
    }

    /**
     * 暂停任务
     */
    public void pauseTaskThread() {
        isCanDownload = true;
        mUpdateDownloadThread.interrupt();

        for (int i = 0; i < mDownloadTaskThreads.size(); i++) {
            DownloadTaskThread taskThread = mDownloadTaskThreads.get(i);
            taskThread.pause();
        }
        if (mIDownloadTaskEvent != null) {
            mIDownloadTaskEvent.taskPause(mDownloadTask, getTaskDownloadedSize());
        }
        synchronized (mWorkerHandler) {
            mWorkerHandler.notifyAll();
        }
    }

    /**
     * 获取任务的总下载进度
     *
     * @return
     * @author zhangliangming
     * @date 2017年7月8日
     */
    public int getTaskDownloadedSize() {
        int downloadedSize = 0;

        for (int i = 0; i < mDownloadTaskThreads.size(); i++) {
            DownloadTaskThread taskThread = mDownloadTaskThreads.get(i);
            downloadedSize += taskThread.getDownloadedSize();
        }
        return downloadedSize;
    }


    /**
     * 取消任务
     */
    public void cancelTaskThread() {
        isCanDownload = true;
        mUpdateDownloadThread.interrupt();

        for (int i = 0; i < mDownloadTaskThreads.size(); i++) {
            DownloadTaskThread taskThread = mDownloadTaskThreads.get(i);
            taskThread.cancel();
        }
        if (mIDownloadTaskEvent != null) {
            mIDownloadTaskEvent.taskCancel(mDownloadTask);
        }
        synchronized (mWorkerHandler) {
            mWorkerHandler.notifyAll();
        }
    }

    /**
     * 复制文件
     *
     * @param oldPath 旧文件路径
     * @param newPath 新文件路径
     * @author zhangliangming
     * @date 2017年7月8日
     */
    private void copyFile(String oldPath, String newPath) {
        File oldfile = null;
        try {
            int bytesum = 0;
            int byteread = 0;
            oldfile = new File(oldPath);
            if (oldfile.exists()) { // 文件存在时
                InputStream inStream = new FileInputStream(oldPath); // 读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; // 字节数 文件大小
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                fs.close();

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (oldfile != null) {
                System.gc();
                oldfile.delete();
            }

        }
    }

}
