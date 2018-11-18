package com.zlm.hp.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @Description: 歌手信息
 * @author: zhangliangming
 * @date: 2018-07-30 23:09
 **/
@Entity(
        generateConstructors = false,
        generateGettersSetters = false
)
public class SingerInfo implements Parcelable {
    /**
     * 歌手分类id
     */
    @Transient
    private String classId;
    /**
     * 歌手分类名称
     */
    @Transient
    private String className;
    /**
     * 歌手id
     */
    private String singerId;

    /**
     * 歌手名称
     */
    private String singerName;

    /**
     * 图片
     */
    private String imageUrl;

    /**
     *
     */
    private String createTime;

    public SingerInfo() {
    }


    protected SingerInfo(Parcel in) {
        classId = in.readString();
        className = in.readString();
        singerId = in.readString();
        singerName = in.readString();
        imageUrl = in.readString();
        createTime = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(classId);
        dest.writeString(className);
        dest.writeString(singerId);
        dest.writeString(singerName);
        dest.writeString(imageUrl);
        dest.writeString(createTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SingerInfo> CREATOR = new Creator<SingerInfo>() {
        @Override
        public SingerInfo createFromParcel(Parcel in) {
            return new SingerInfo(in);
        }

        @Override
        public SingerInfo[] newArray(int size) {
            return new SingerInfo[size];
        }
    };

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSingerId() {
        return singerId;
    }

    public void setSingerId(String singerId) {
        this.singerId = singerId;
    }

    public String getSingerName() {
        return singerName;
    }

    public void setSingerName(String singerName) {
        this.singerName = singerName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
