package com.zlm.hp.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @Description: 定时器信息
 * @author: zhangliangming
 * @date: 2018-11-25 13:45
 **/
public class TimerInfo implements Parcelable{
    public static final int TYPE_SYS = 0;
    public static final int TYPE_DEF = 1;

    private int sumTime;
    private int curTime;
    private int type = TYPE_SYS;

    public TimerInfo(){

    }


    protected TimerInfo(Parcel in) {
        sumTime = in.readInt();
        curTime = in.readInt();
        type = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(sumTime);
        dest.writeInt(curTime);
        dest.writeInt(type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TimerInfo> CREATOR = new Creator<TimerInfo>() {
        @Override
        public TimerInfo createFromParcel(Parcel in) {
            return new TimerInfo(in);
        }

        @Override
        public TimerInfo[] newArray(int size) {
            return new TimerInfo[size];
        }
    };

    public int getSumTime() {
        return sumTime;
    }

    public void setSumTime(int sumTime) {
        this.sumTime = sumTime;
    }

    public int getCurTime() {
        return curTime;
    }

    public void setCurTime(int curTime) {
        this.curTime = curTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
