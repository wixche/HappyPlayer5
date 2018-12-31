package com.zlm.hp.entity.tool;

import android.os.Parcel;
import android.os.Parcelable;

import com.zlm.hp.entity.AudioInfo;

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
    /**
     * 默认歌词读取路径
     */
    private String lrcFilePath;
    /**
     * 保存歌词路径
     */
    private String saveLrcFilePath;


    public MakeInfo(){

    }

    protected MakeInfo(Parcel in) {
        audioInfo = in.readParcelable(AudioInfo.class.getClassLoader());
        lrcFilePath = in.readString();
        saveLrcFilePath = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(audioInfo, flags);
        dest.writeString(lrcFilePath);
        dest.writeString(saveLrcFilePath);
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

    public String getSaveLrcFilePath() {
        return saveLrcFilePath;
    }

    public void setSaveLrcFilePath(String saveLrcFilePath) {
        this.saveLrcFilePath = saveLrcFilePath;
    }

    public String getLrcFilePath() {
        return lrcFilePath;
    }

    public void setLrcFilePath(String lrcFilePath) {
        this.lrcFilePath = lrcFilePath;
    }

    public AudioInfo getAudioInfo() {
        return audioInfo;
    }

    public void setAudioInfo(AudioInfo audioInfo) {
        this.audioInfo = audioInfo;
    }
}
