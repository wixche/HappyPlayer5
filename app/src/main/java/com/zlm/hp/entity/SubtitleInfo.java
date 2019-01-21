package com.zlm.hp.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @Description: 字幕信息
 * @author: zhangliangming
 * @date: 2019-01-20 0:09
 **/
@Entity(
        generateConstructors = false,
        generateGettersSetters = false
)
public class SubtitleInfo implements Parcelable {

    /**
     * 视频hash
     */

    private String videoHash;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件下载路径
     */
    private String downloadUrl;

    private String id = "";

    protected SubtitleInfo(Parcel in) {
        videoHash = in.readString();
        fileName = in.readString();
        filePath = in.readString();
        downloadUrl = in.readString();
        id = in.readString();
    }

    public SubtitleInfo() {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(videoHash);
        dest.writeString(fileName);
        dest.writeString(filePath);
        dest.writeString(downloadUrl);
        dest.writeString(id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SubtitleInfo> CREATOR = new Creator<SubtitleInfo>() {
        @Override
        public SubtitleInfo createFromParcel(Parcel in) {
            return new SubtitleInfo(in);
        }

        @Override
        public SubtitleInfo[] newArray(int size) {
            return new SubtitleInfo[size];
        }
    };

    public String getVideoHash() {
        return videoHash;
    }

    public void setVideoHash(String videoHash) {
        this.videoHash = videoHash;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
