package com.zlm.hp.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @Description: 歌单
 * @author: zhangliangming
 * @date: 2018-07-30 23:28
 **/

public class SpecialInfo implements Parcelable{
    /**
     * 歌单id
     */
    private String specialId;
    /**
     * 歌单名称
     */
    private String specialName;

    /**
     * 图片
     */
    private String imageUrl;

    public SpecialInfo(){

    }

    protected SpecialInfo(Parcel in) {
        specialId = in.readString();
        specialName = in.readString();
        imageUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(specialId);
        dest.writeString(specialName);
        dest.writeString(imageUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SpecialInfo> CREATOR = new Creator<SpecialInfo>() {
        @Override
        public SpecialInfo createFromParcel(Parcel in) {
            return new SpecialInfo(in);
        }

        @Override
        public SpecialInfo[] newArray(int size) {
            return new SpecialInfo[size];
        }
    };

    public String getSpecialId() {
        return specialId;
    }

    public void setSpecialId(String specialId) {
        this.specialId = specialId;
    }

    public String getSpecialName() {
        return specialName;
    }

    public void setSpecialName(String specialName) {
        this.specialName = specialName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
