package com.zlm.hp.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @Description: 视频信息
 * @author: zhangliangming
 * @date: 2018-07-30 1:55
 **/
@Entity(
        generateConstructors = false,
        generateGettersSetters = false
)
public class VideoInfo implements Parcelable {

    /**
     *
     */

    public static final String DATA_KEY = "Data_Key";

    /**
     * 状态
     */
    public static final int STATUS_FINISH = 0;
    public static final int STATUS_INIT = 1;

    /**
     *
     */

    private String hash;

    /**
     * 歌曲后缀名
     */
    private String fileExt;
    /**
     * 文件大小
     */
    private long fileSize;
    private String fileSizeText;
    /**
     * 文件路径
     */
    private String filePath;
    /**
     * 时长
     */
    private long duration;
    private String durationText;

    /**
     * mv名称
     */
    private String mvName;
    /**
     * 歌手名称
     */
    private String singerName;

    /**
     * 图片
     */
    private String imageUrl;

    /**
     * 文件下载路径
     */
    private String downloadUrl;

    /**
     * 状态
     */
    private int status = STATUS_INIT;

    public VideoInfo() {
    }


    protected VideoInfo(Parcel in) {
        hash = in.readString();
        fileExt = in.readString();
        fileSize = in.readLong();
        fileSizeText = in.readString();
        filePath = in.readString();
        duration = in.readLong();
        durationText = in.readString();
        mvName = in.readString();
        singerName = in.readString();
        imageUrl = in.readString();
        downloadUrl = in.readString();
        status = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(hash);
        dest.writeString(fileExt);
        dest.writeLong(fileSize);
        dest.writeString(fileSizeText);
        dest.writeString(filePath);
        dest.writeLong(duration);
        dest.writeString(durationText);
        dest.writeString(mvName);
        dest.writeString(singerName);
        dest.writeString(imageUrl);
        dest.writeString(downloadUrl);
        dest.writeInt(status);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VideoInfo> CREATOR = new Creator<VideoInfo>() {
        @Override
        public VideoInfo createFromParcel(Parcel in) {
            return new VideoInfo(in);
        }

        @Override
        public VideoInfo[] newArray(int size) {
            return new VideoInfo[size];
        }
    };

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getFileExt() {
        return fileExt;
    }

    public void setFileExt(String fileExt) {
        this.fileExt = fileExt;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileSizeText() {
        return fileSizeText;
    }

    public void setFileSizeText(String fileSizeText) {
        this.fileSizeText = fileSizeText;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getDurationText() {
        return durationText;
    }

    public void setDurationText(String durationText) {
        this.durationText = durationText;
    }

    public String getMvName() {
        return mvName;
    }

    public void setMvName(String mvName) {
        this.mvName = mvName;
    }

    public String getSingerName() {
        return singerName;
    }

    public void setSingerName(String singerName) {
        this.singerName = singerName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTitle() {
        if (getSingerName().equals("未知")) {
            return getMvName();
        }
        return getSingerName() + " - " + getMvName();
    }
}
