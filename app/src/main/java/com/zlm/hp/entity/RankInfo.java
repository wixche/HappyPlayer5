package com.zlm.hp.entity;

/**
 * @Description: 排行榜信息
 * @author: zhangliangming
 * @date: 2018-07-30 23:25
 **/

public class RankInfo {
    /**
     * 排行榜id
     */
    private String rankId;
    /**
     * 排行榜名称
     */
    private String rankName;

    /**
     * 图片
     */
    private String imageUrl;

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
}
