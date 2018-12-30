package com.zlm.hp.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @Description: 制作歌词信息
 * @author: zhangliangming
 * @date: 2018-12-30 22:34
 **/
public class MakeInfo implements Parcelable{
    /**
     *
     */
    public static final String DATA_KEY = "Data_Key";
    private AudioInfo audioInfo;

    public MakeInfo(){

    }

    protected MakeInfo(Parcel in) {
        audioInfo = in.readParcelable(AudioInfo.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(audioInfo, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MakeInfo> CREATOR = new Creator<MakeInfo>() {
        @Override
        public MakeInfo createFromParcel(Parcel in) {
            return new MakeInfo(in);
        }

        @Override
        public MakeInfo[] newArray(int size) {
            return new MakeInfo[size];
        }
    };

    public AudioInfo getAudioInfo() {
        return audioInfo;
    }

    public void setAudioInfo(AudioInfo audioInfo) {
        this.audioInfo = audioInfo;
    }
}
