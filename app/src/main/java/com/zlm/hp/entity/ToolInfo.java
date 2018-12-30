package com.zlm.hp.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @Description: 工具信息
 * @author: zhangliangming
 * @date: 2018-12-30 16:46
 **/
public class ToolInfo implements Parcelable{
    public static final String DATA_KEY = "Data_Key";

    private String title;
    private String packageName;

    public ToolInfo(){

    }

    protected ToolInfo(Parcel in) {
        title = in.readString();
        packageName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(packageName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ToolInfo> CREATOR = new Creator<ToolInfo>() {
        @Override
        public ToolInfo createFromParcel(Parcel in) {
            return new ToolInfo(in);
        }

        @Override
        public ToolInfo[] newArray(int size) {
            return new ToolInfo[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
