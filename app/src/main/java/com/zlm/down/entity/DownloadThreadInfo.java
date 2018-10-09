package com.zlm.down.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @Description: 下载线程任务
 * @author: zhangliangming
 * @date: 2018-10-07 20:19
 **/
@Entity(
        generateConstructors = false,
        generateGettersSetters = false
)
public class DownloadThreadInfo implements Parcelable {

    private String taskId;

    private int threadNum;

    private int threadId;
    private int downloadedSize;

    public DownloadThreadInfo() {

    }

    protected DownloadThreadInfo(Parcel in) {
        taskId = in.readString();
        threadNum = in.readInt();
        threadId = in.readInt();
        downloadedSize = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(taskId);
        dest.writeInt(threadNum);
        dest.writeInt(threadId);
        dest.writeInt(downloadedSize);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DownloadThreadInfo> CREATOR = new Creator<DownloadThreadInfo>() {
        @Override
        public DownloadThreadInfo createFromParcel(Parcel in) {
            return new DownloadThreadInfo(in);
        }

        @Override
        public DownloadThreadInfo[] newArray(int size) {
            return new DownloadThreadInfo[size];
        }
    };

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public int getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public int getDownloadedSize() {
        return downloadedSize;
    }

    public void setDownloadedSize(int downloadedSize) {
        this.downloadedSize = downloadedSize;
    }
}
