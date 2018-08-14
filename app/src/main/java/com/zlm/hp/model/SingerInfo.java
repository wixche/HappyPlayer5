package com.zlm.hp.model;

/**
 * @Description: 歌手信息
 * @author: zhangliangming
 * @date: 2018-07-30 23:09
 **/
public class SingerInfo {
    /**
     * 歌手分类id
     */
    private String classId;
    /**
     * 歌手分类名称
     */
    private String className;
    /**
     * 歌手id
     */
    private String singerId;

    /**
     * 歌手名称
     */
    private String singerName;

    /**
     * 图片
     */
    private String imageUrl;

    public String getSingerId() {
        return singerId;
    }

    public void setSingerId(String singerId) {
        this.singerId = singerId;
    }

    public String getSingerName() {
        return singerName;
    }

    public void setSingerName(String singerName) {
        this.singerName = singerName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
