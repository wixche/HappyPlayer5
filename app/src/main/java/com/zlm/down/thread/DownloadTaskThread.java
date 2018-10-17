package com.zlm.down.thread;

import android.content.Context;

import com.zlm.down.entity.DownloadTask;
import com.zlm.down.interfaces.IDownloadThreadEvent;
import com.zlm.hp.http.HttpClient;
import com.zlm.hp.http.HttpReturnResult;
import com.zlm.hp.util.HttpUtil;
import com.zlm.hp.util.NetUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * @Description: 下载任务线程
 * @author: zhangliangming
 * @date: 2018-08-04 23:14
 **/
public class DownloadTaskThread extends Thread {
    /**
     * 连接超时时间
     */
    private final int CONNECTTIME = 30 * 1000;
    /**
     * 读取数据超时时间
     */
    private final int READTIME = 30 * 1000;

    /**
     *
     */
    private final int BUFF_LENGTH = 1024 * 8;

    /**
     * 是否是wifi条件
     */
    private boolean isAskWifi;
    /**
     * 是否完成任务
     */
    private boolean isFinish = false;

    /**
     * 任务
     */
    private DownloadTask mDownloadTask;
    /**
     * 线程id
     */
    private int mThreadId = -1;
    /**
     * 旧的开始位置
     */
    private int mStartPos = 0;
    /**
     * 新的开始位置
     */
    private int mNewStartPos = 0;
    /**
     * 结束位置
     */
    private int mEndPos = 0;
    /**
     * 下载大小
     */
    private int mDownloadedSize = 0;

    /**
     * 是否能继续下载
     */
    private volatile boolean isCanDownload = true;

    /**
     * 临时文件
     */
    private RandomAccessFile mItemFile;

    /**
     * 线程下载任务回调
     */
    private IDownloadThreadEvent mIDownloadThreadEvent;

    /**
     *
     */
    private Context mContext;

    public DownloadTaskThread(Context context, int threadId, int startPos, int endPos,
                              DownloadTask downloadTask, IDownloadThreadEvent downloadThreadEvent, boolean askWifi) {
        this.mContext = context;
        this.mThreadId = threadId;
        this.mStartPos = startPos;
        this.mEndPos = endPos;
        this.mDownloadTask = downloadTask;
        this.mIDownloadThreadEvent = downloadThreadEvent;
        this.isAskWifi = askWifi;
        mDownloadedSize = mIDownloadThreadEvent.getTaskThreadDownloadedSize(mDownloadTask, mThreadId);
        // 设置新的开始位置
        mNewStartPos = mStartPos + mDownloadedSize;
    }

    /**
     * 更新下载进程
     */
    private Thread mUpdateDownloadThread = new Thread() {
        @Override
        public void run() {
            while (isCanDownload && !isFinish) {
                synchronized (this) {
                    int taskThreadDownloadedSize = getDownloadedSize();
                    //更新任务线程
                    if (mIDownloadThreadEvent != null && !isFinish) {
                            mIDownloadThreadEvent.taskThreadDownloading(mDownloadTask, mThreadId, taskThreadDownloadedSize);
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    @Override
    public void run() {
        mUpdateDownloadThread.start();
        if (mEndPos > mNewStartPos) {

            try {

                URL url = new URL(mDownloadTask.getTaskUrl());
                // 获取文件输入流，读取文件内容
                InputStream is = getUrlInputStream(mDownloadTask.getTaskUrl(), url);

                //
                mItemFile = new RandomAccessFile(mDownloadTask.getTaskTempPath(), "rw");
                mItemFile.seek(mNewStartPos);

                byte[] buff = new byte[BUFF_LENGTH];
                int length = -1;
                while ((length = is.read(buff)) > 0
                        && (mStartPos + mDownloadedSize) < mEndPos && isCanDownload) {

                    if (!NetUtil.isNetworkAvailable(mContext)) {


                        if (mIDownloadThreadEvent != null) {
                            //无网络
                            mIDownloadThreadEvent.taskThreadError(mDownloadTask, mThreadId,
                                    HttpReturnResult.ERROR_MSG_NONET);
                        }

                        break;
                    }

                    if (isAskWifi && !NetUtil.isWifiConnected(mContext)) {
                        // 不是wifi
                        if (mIDownloadThreadEvent != null) {
                            mIDownloadThreadEvent.taskThreadError(mDownloadTask, mThreadId,
                                    HttpReturnResult.ERROR_MSG_NOWIFI);
                        }
                        break;
                    }


                    mItemFile.write(buff, 0, length);
                    mDownloadedSize += length;
                }

                is.close();

            } catch (Exception e) {
                e.printStackTrace();

                if (mIDownloadThreadEvent != null) {
                    mIDownloadThreadEvent.taskThreadError(mDownloadTask, mThreadId,
                            HttpReturnResult.ERROR_MSG_NET);
                }

            } finally {
                if (mItemFile != null)
                    try {
                        mItemFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }

        }
        //在允许下载的状态下，说明下载完成
        if (isCanDownload) {
            isFinish = true;
            mUpdateDownloadThread.interrupt();
            mDownloadedSize = mEndPos - mStartPos;
            if (mIDownloadThreadEvent != null)
                mIDownloadThreadEvent.taskThreadFinish(mDownloadTask, mThreadId,
                        getDownloadedSize());
        }
    }

    /**
     * @param taskUrl
     * @param url
     * @return
     */
    private InputStream getUrlInputStream(String taskUrl, URL url) throws Exception {
        if (taskUrl.startsWith("https://")) {
            HttpsURLConnection conn = getHttpsConnection(url);
            // 设置连接超时时间
            conn.setConnectTimeout(CONNECTTIME);
            // 设置读取数据超时时间
            conn.setReadTimeout(READTIME);
            HttpUtil.seURLConnectiontHeader(conn);
            conn.setRequestProperty("Range", "bytes=" + mNewStartPos + "-"
                    + mEndPos);
            return conn.getInputStream();

        } else {
            HttpURLConnection conn = getHttpConnection(url);
            // 设置连接超时时间
            conn.setConnectTimeout(CONNECTTIME);
            // 设置读取数据超时时间
            conn.setReadTimeout(READTIME);
            HttpUtil.seURLConnectiontHeader(conn);
            conn.setRequestProperty("Range", "bytes=" + mNewStartPos + "-"
                    + mEndPos);
            return conn.getInputStream();
        }
    }

    /**
     * @param url
     * @return
     */
    private HttpsURLConnection getHttpsConnection(URL url) throws Exception {

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

        return conn;

    }

    /**
     * @param url
     * @return
     * @throws Exception
     */
    private HttpURLConnection getHttpConnection(URL url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        return conn;
    }

    /***
     *
     * 获取总下载进度
     *
     * @return
     * @author zhangliangming
     * @date 2017年7月8日
     */
    public int getDownloadedSize() {
        int tempDownloadedSize = Math.min(mDownloadedSize,
                (mEndPos - mStartPos));
        return tempDownloadedSize;
    }

    /**
     * 暂停
     */
    public void pause() {
        isCanDownload = false;
        mUpdateDownloadThread.interrupt();
        //更新任务线程
        if (mIDownloadThreadEvent != null) {
            int taskThreadDownloadedSize = getDownloadedSize();
            if (taskThreadDownloadedSize != 0)
                mIDownloadThreadEvent.taskThreadPause(mDownloadTask, mThreadId, taskThreadDownloadedSize);
        }
    }

    /**
     * 取消
     */
    public void cancel() {
        isCanDownload = false;
        mUpdateDownloadThread.interrupt();
    }
}
