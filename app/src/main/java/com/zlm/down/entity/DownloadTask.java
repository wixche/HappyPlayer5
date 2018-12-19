package com.zlm.down.entity;


import android.os.Parcel;
import android.os.Parcelable;

import com.zlm.down.thread.DownloadTaskThreadManager;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Transient;

import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @Description: 下载任务
 * @author: zhangliangming
 * @date: 2018-08-04 23:01
 **/
@Entity(
        generateConstructors = false,
        generateGettersSetters = false
)
public class DownloadTask implements Parcelable {

    /**
     * 初始化
     */
    public static final int STATUS_INT = 0;
    /**
     * 等待下载
     */
    public static final int STATUS_WAIT = 1;
    /**
     * 下载中
     */
    public static final int STATUS_DOWNLOADING = 2;
    /**
     * 暂停
     */
    public static final int STATUS_PAUSE = 3;
    /**
     * 取消
     */
    public static final int STATUS_CANCEL = 4;
    /**
     * 错误
     */
    public static final int STATUS_ERROR = 5;

    /**
     * 完成
     */
    public static final int STATUS_FINISH = 6;
    /**
     * 任务id
     */

    private String taskId;
    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务文件后缀名
     */
    private String taskExt;
    /**
     * 任务保存路径
     */
    private String taskPath;

    /**
     * 任务临时保存路径
     */
    private String taskTempPath;
    /**
     * 根目录
     */
    private String rootPath;
    /**
     * 任务下载路径
     */
    private String taskUrl;
    /**
     * 添加时间
     */
    private Date createTime;
    /**
     * 任务状态
     */
    private int status = STATUS_INT;

    /**
     * 线程总数
     */

    private int threadNum;
    /**
     * 任务文件大小
     */
    private long taskFileSize;

    /**
     * 下载任务线程
     */
    @Transient
    private DownloadTaskThreadManager downloadTaskThreadManager;

    public DownloadTask() {
    }

    protected DownloadTask(Parcel in) {
        taskId = in.readString();
        taskName = in.readString();
        taskExt = in.readString();
        taskPath = in.readString();
        taskTempPath = in.readString();
        rootPath = in.readString();
        taskUrl = in.readString();
        status = in.readInt();
        threadNum = in.readInt();
        taskFileSize = in.readLong();
    }

    public static final Creator<DownloadTask> CREATOR = new Creator<DownloadTask>() {
        @Override
        public DownloadTask createFromParcel(Parcel in) {
            return new DownloadTask(in);
        }

        @Override
        public DownloadTask[] newArray(int size) {
            return new DownloadTask[size];
        }
    };

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskExt() {
        return taskExt;
    }

    public void setTaskExt(String taskExt) {
        this.taskExt = taskExt;
    }

    public String getTaskPath() {
        return taskPath;
    }

    public void setTaskPath(String taskPath) {
        this.taskPath = taskPath;
    }

    public String getTaskTempPath() {
        return taskTempPath;
    }

    public void setTaskTempPath(String taskTempPath) {
        this.taskTempPath = taskTempPath;
    }

    public String getTaskUrl() {
        return taskUrl;
    }

    public void setTaskUrl(String taskUrl) {
        this.taskUrl = taskUrl;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    public int getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    public long getTaskFileSize() {
        return taskFileSize;
    }

    public void setTaskFileSize(long taskFileSize) {
        this.taskFileSize = taskFileSize;
    }

    public DownloadTaskThreadManager getDownloadTaskThreadManager() {
        return downloadTaskThreadManager;
    }

    public void setDownloadTaskThreadManager(DownloadTaskThreadManager downloadTaskThreadManager) {
        this.downloadTaskThreadManager = downloadTaskThreadManager;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(taskId);
        dest.writeString(taskName);
        dest.writeString(taskExt);
        dest.writeString(taskPath);
        dest.writeString(taskTempPath);
        dest.writeString(rootPath);
        dest.writeString(taskUrl);
        dest.writeInt(status);
        dest.writeInt(threadNum);
        dest.writeLong(taskFileSize);
    }
}
