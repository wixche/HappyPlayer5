package com.zlm.hp.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @Description: 排行榜信息
 * @author: zhangliangming
 * @date: 2018-07-30 23:25
 **/

public class RankInfo implements Parcelable{
    /**
     * 排行榜id
     */
    private String rankId;
    /**
     *
     */
    private String rankType = "";
    /**
     * 排行榜名称
     */
    private String rankName;

    /**
     * 图片
     */
    private String imageUrl;

    public RankInfo(){

    }

    public RankInfo(String rankId, String rankName,String rankType) {
        this.rankId = rankId;
        this.rankName = rankName;
        this.rankType = rankType;
    }

    public static RankInfo newDefRankInfo(){
        return new RankInfo("6666","2","酷狗飙升榜");
    }

    protected RankInfo(Parcel in) {
        rankId = in.readString();
        rankName = in.readString();
        imageUrl = in.readString();
        rankType = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(rankId);
        dest.writeString(rankName);
        dest.writeString(imageUrl);
        dest.writeString(rankType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RankInfo> CREATOR = new Creator<RankInfo>() {
        @Override
        public RankInfo createFromParcel(Parcel in) {
            return new RankInfo(in);
        }

        @Override
        public RankInfo[] newArray(int size) {
            return new RankInfo[size];
        }
    };

    public String getRankId() {
        return rankId;
    }

    public void setRankId(String rankId) {
        this.rankId = rankId;
    }

    public String getRankName() {
        return rankName;
    }

    public void setRankName(String rankName) {
        this.rankName = rankName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getRankType() {
        return rankType;
    }

    public void setRankType(String rankType) {
        this.rankType = rankType;
    }
}
