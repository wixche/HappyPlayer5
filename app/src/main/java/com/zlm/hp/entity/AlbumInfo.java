package com.zlm.hp.entity;

/**
 * @Description: 专辑信息
 * @author: zhangliangming
 * @date: 2018-07-30 23:21
 **/

public class AlbumInfo {
    /**
     * 专辑id
     */
    private String albumId;

    /**
     * 专辑名称
     */
    private String albumName;

    /**
     * 图片
     */
    private String imageUrl;

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
