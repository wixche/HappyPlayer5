package com.zlm.hp.db.util;

import android.content.Context;
import android.database.Cursor;

import com.zlm.hp.db.DBHelper;
import com.zlm.hp.db.dao.VideoInfoDao;
import com.zlm.hp.entity.VideoInfo;

/**
 * @Description: Video数据库处理
 * @author: zhangliangming
 * @date: 2019-01-06 1:06
 **/
public class VideoInfoDB {
    /**
     * 添加
     *
     * @param context
     * @param videoInfo
     * @return
     */
    public static boolean addVideoInfo(Context context, VideoInfo videoInfo) {
        try {
            DBHelper.getInstance(context).getDaoSession().getVideoInfoDao().save(videoInfo);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 是否存在
     *
     * @param context
     * @param hash
     * @return
     */
    public static boolean isVideoExists(Context context, String hash) {
        Cursor cursor = null;
        try {
            String args[] = {hash};
            String sql = "select * from " + VideoInfoDao.TABLENAME;
            sql += " where " + VideoInfoDao.Properties.Hash.columnName + "=?";
            cursor = DBHelper.getInstance(context).getWritableDatabase().rawQuery(sql, args);
            if (cursor.moveToNext()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    /**
     * 更新
     *
     * @param context
     * @param hash
     * @param status
     * @return
     */
    public static boolean updateVideo(Context context, String hash, int status) {
        try {

            String sql = "UPDATE ";
            sql += VideoInfoDao.TABLENAME;
            sql += " SET " + VideoInfoDao.Properties.Status.columnName + " =?";
            sql += " where " + VideoInfoDao.Properties.Hash.columnName + "=?";

            String args[] = {status + "", hash};
            DBHelper.getInstance(context).getWritableDatabase().execSQL(sql, args);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
