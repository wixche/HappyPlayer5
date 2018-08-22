package com.zlm.hp.entity;


/**
 * @Description: 储存卡
 * @author: zhangliangming
 * @date: 2018-07-29 11:59
 **/
public class StorageInfo {
    /**
     * 路径
     */
    private String path;
    /**
     * 挂载状态
     */
    private String state;
    /**
     * 是否移除
     */
    private boolean isRemoveable;

    public StorageInfo(String path) {
        this.path = path;
    }


    public boolean isMounted() {
        return "mounted".equals(state);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isRemoveable() {
        return isRemoveable;
    }

    public void setRemoveable(boolean removeable) {
        isRemoveable = removeable;
    }
}