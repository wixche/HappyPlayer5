package com.zlm.hp.model;

/**
 * @Description: 歌单
 * @author: zhangliangming
 * @date: 2018-07-30 23:28
 **/

public class SpecialInfo {
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
