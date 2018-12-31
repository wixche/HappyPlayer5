package com.zlm.hp.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @Description: 歌曲信息
 * @author: zhangliangming
 * @date: 2018-07-29 17:50
 **/
@Entity(
        generateConstructors = false,
        generateGettersSetters = false
)
public class AudioInfo implements Parcelable {

    /**
     * 状态
     */
    public static final int STATUS_FINISH = 0;
    public static final int STATUS_DOWNLOADING = 1;
    public static final int STATUS_INIT = 2;

    /**
     * 状态
     */
    private int status = STATUS_INIT;

    /**
     * 类型
     */
    public static final int TYPE_LOCAL = 0;
    public static final int TYPE_NET = 1;
    //最近-本地
    public static final int TYPE_RECENT_LOCAL = 2;
    //最近-网络
    public static final int TYPE_RECENT_NET = 3;

    /**
     * 喜欢（网络-本地）
     */
    public static final int TYPE_LIKE_LOCAL = 4;
    public static final int TYPE_LIKE_NET = 5;

    /**
     * 类型
     */
    private int type = TYPE_LOCAL;

    /**
     * 关键字
     */
    private String keyword;

    /**
     * 图片
     */
    private String imageUrl;

    /**
     * 歌单id
     */
    private String specialId;

    /**
     * 歌手id
     */
    private String singerId;

    /**
     * 专辑id
     */
    private String albumId;

    /**
     * 歌曲名称
     */
    private String songName;
    /**
     * 歌手名称
     */
    private String singerName;
    /**
     *
     */
    private String hash;
    /**
     * mv hash
     */
    private String mvHash;
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
     * 添加时间
     */
    private String createTime;

    /**
     * 分类索引
     */
    private String category;
    private String childCategory;

    /**
     * 文件下载路径
     */
    private String downloadUrl;

    /**
     *
     */
    @Transient
    private int playProgress;

    public AudioInfo() {

    }

    protected AudioInfo(Parcel in) {
        if (in != null) {
            status = in.readInt();
            type = in.readInt();
            keyword = in.readString();
            imageUrl = in.readString();
            specialId = in.readString();
            singerId = in.readString();
            albumId = in.readString();
            songName = in.readString();
            singerName = in.readString();
            hash = in.readString();
            mvHash = in.readString();
            fileExt = in.readString();
            fileSize = in.readLong();
            fileSizeText = in.readString();
            filePath = in.readString();
            duration = in.readLong();
            durationText = in.readString();
            createTime = in.readString();
            category = in.readString();
            childCategory = in.readString();
            downloadUrl = in.readString();
            playProgress = in.readInt();
        }
    }

    public String getTitle() {
        if (getSingerName().equals("未知")) {
            return getSongName();
        }
        return getSingerName() + " - " + getSongName();
    }

    public static final Creator<AudioInfo> CREATOR = new Creator<AudioInfo>() {
        @Override
        public AudioInfo createFromParcel(Parcel in) {
            return new AudioInfo(in);
        }

        @Override
        public AudioInfo[] newArray(int size) {
            return new AudioInfo[size];
        }
    };

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSpecialId() {
        return specialId;
    }

    public void setSpecialId(String specialId) {
        this.specialId = specialId;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSingerName() {
        return singerName;
    }

    public void setSingerName(String singerName) {
        this.singerName = singerName;
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

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getChildCategory() {
        return childCategory;
    }

    public void setChildCategory(String childCategory) {
        this.childCategory = childCategory;
    }

    public String getMvHash() {
        return mvHash;
    }

    public void setMvHash(String mvHash) {
        this.mvHash = mvHash;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSingerId() {
        return singerId;
    }

    public void setSingerId(String singerId) {
        this.singerId = singerId;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public int getPlayProgress() {
        return playProgress;
    }

    public void setPlayProgress(int playProgress) {
        this.playProgress = playProgress;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(status);
        dest.writeInt(type);
        dest.writeString(keyword);
        dest.writeString(imageUrl);
        dest.writeString(specialId);
        dest.writeString(singerId);
        dest.writeString(albumId);
        dest.writeString(songName);
        dest.writeString(singerName);
        dest.writeString(hash);
        dest.writeString(mvHash);
        dest.writeString(fileExt);
        dest.writeLong(fileSize);
        dest.writeString(fileSizeText);
        dest.writeString(filePath);
        dest.writeLong(duration);
        dest.writeString(durationText);
        dest.writeString(createTime);
        dest.writeString(category);
        dest.writeString(childCategory);
        dest.writeString(downloadUrl);
        dest.writeInt(playProgress);
    }
}
