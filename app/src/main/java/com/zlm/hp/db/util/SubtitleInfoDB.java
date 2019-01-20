package com.zlm.hp.db.util;

import android.content.Context;
import android.database.Cursor;

import com.zlm.hp.db.DBHelper;
import com.zlm.hp.db.dao.SubtitleInfoDao;
import com.zlm.hp.entity.SubtitleInfo;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;

/**
 * @Description: 字幕数据库处理
 * @author: zhangliangming
 * @date: 2019-01-20 15:30
 **/
public class SubtitleInfoDB {

    /**
     * 添加
     *
     * @param context
     * @return
     */
    public static boolean addSubtitleInfo(Context context, SubtitleInfo subtitleInfo) {
        try {
            DBHelper.getInstance(context).getDaoSession().getSubtitleInfoDao().save(subtitleInfo);
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
     * @param videoHash
     * @return
     */
    public static boolean isSubtitleExists(Context context, String videoHash) {
        Cursor cursor = null;
        try {
            String args[] = {videoHash};
            String sql = "select * from " + SubtitleInfoDao.TABLENAME;
            sql += " where " + SubtitleInfoDao.Properties.VideoHash.columnName + "=?";
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
     */
    public static boolean updateSubtitleInfo(Context context, String videoHash, String fileName, String filePath, String downloadUrl) {
        try {

            String sql = "UPDATE ";
            sql += SubtitleInfoDao.TABLENAME;
            sql += " SET " + SubtitleInfoDao.Properties.FilePath.columnName + " =?";
            sql += "," + SubtitleInfoDao.Properties.DownloadUrl.columnName + " =?";
            sql += "," + SubtitleInfoDao.Properties.FileName.columnName + " =?";
            sql += " where " + SubtitleInfoDao.Properties.VideoHash.columnName + "=?";

            String args[] = {filePath, downloadUrl, fileName, videoHash};
            DBHelper.getInstance(context).getWritableDatabase().execSQL(sql, args);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取字幕
     *
     * @param context
     * @return
     */
    public static SubtitleInfo getSubtitleInfo(Context context, String videoHash) {
        try {
            List<SubtitleInfo> subtitleInfoList = DBHelper.getInstance(context).getDaoSession().getSubtitleInfoDao().queryBuilder().where(new WhereCondition.StringCondition(SubtitleInfoDao.Properties.VideoHash.columnName + "=?", videoHash)).list();
            if (subtitleInfoList != null && subtitleInfoList.size() > 0) {
                return subtitleInfoList.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
