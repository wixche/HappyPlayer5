package com.zlm.hp.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @Description: 歌词信息类
 * @author: zhangliangming
 * @date: 2018-07-31 23:59
 **/

public class LrcInfo implements Parcelable{
    /**
     * 歌词id
     */
    private String id;

    /**
     *
     */
    private String accesskey;
    /**
     * 时长
     */
    private String duration;

    /**
     * 歌手
     */
    private String singerName;
    /**
     * 歌曲名称
     */
    private String songName;

    /**
     * 歌词编码
     */
    private String charset;
    /**
     * 歌词内容(base64)
     */
    private String content;
    /**
     * 歌词格式
     */
    private String fmt;

    public LrcInfo(){

    }

    protected LrcInfo(Parcel in) {
        id = in.readString();
        accesskey = in.readString();
        duration = in.readString();
        singerName = in.readString();
        songName = in.readString();
        charset = in.readString();
        content = in.readString();
        fmt = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(accesskey);
        dest.writeString(duration);
        dest.writeString(singerName);
        dest.writeString(songName);
        dest.writeString(charset);
        dest.writeString(content);
        dest.writeString(fmt);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LrcInfo> CREATOR = new Creator<LrcInfo>() {
        @Override
        public LrcInfo createFromParcel(Parcel in) {
            return new LrcInfo(in);
        }

        @Override
        public LrcInfo[] newArray(int size) {
            return new LrcInfo[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccesskey() {
        return accesskey;
    }

    public void setAccesskey(String accesskey) {
        this.accesskey = accesskey;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getSingerName() {
        return singerName;
    }

    public void setSingerName(String singerName) {
        this.singerName = singerName;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFmt() {
        return fmt;
    }

    public void setFmt(String fmt) {
        this.fmt = fmt;
    }
}
