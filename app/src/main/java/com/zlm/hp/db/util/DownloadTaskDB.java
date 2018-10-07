package com.zlm.hp.db.util;

import android.content.Context;

import com.zlm.down.entity.DownloadTask;
import com.zlm.hp.db.DBHelper;

/**
 * @Description: 下载任务db管理
 * @author: zhangliangming
 * @date: 2018-10-07 19:42
 **/
public class DownloadTaskDB {

    /**
     * 添加下载任务
     *
     * @param downloadTask
     */
    public static boolean add(Context context, DownloadTask downloadTask) {
        try {
            DBHelper.getInstance(context).getDaoSession().getDownloadTaskDao().insert(downloadTask);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
