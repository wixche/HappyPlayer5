package com.zlm.hp.db.util;

import android.content.Context;
import android.database.Cursor;

import com.zlm.hp.db.DBHelper;
import com.zlm.hp.db.dao.SingerInfoDao;
import com.zlm.hp.entity.SingerInfo;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;

/**
 * @Description: 歌手写真
 * @author: zhangliangming
 * @date: 2018-11-18 12:48
 **/
public class SingerInfoDB {

    /**
     * 添加
     *
     * @param context
     * @param singerInfo
     * @return
     */
    public static boolean add(Context context, SingerInfo singerInfo) {
        try {
            DBHelper.getInstance(context).getDaoSession().getSingerInfoDao().insert(singerInfo);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 获取歌手写真图片
     *
     * @param singerName
     * @return
     */
    public static List<SingerInfo> getAllSingerImage(Context context, String singerName) {
        try {
            List<SingerInfo> singerInfos = DBHelper.getInstance(context).getDaoSession().getSingerInfoDao().queryBuilder().where(new WhereCondition.StringCondition(SingerInfoDao.Properties.SingerName.columnName + "=?", singerName)).orderDesc(SingerInfoDao.Properties.CreateTime).list();
            return singerInfos;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param context
     * @param imageUrl
     * @return
     */
    public static boolean isExists(Context context, String imageUrl) {
        Cursor cursor = null;
        try {
            String args[] = {imageUrl};
            String sql = "select * from " + SingerInfoDao.TABLENAME + " WHERE " + SingerInfoDao.Properties.ImageUrl.columnName + "=?";
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
     * 删除
     */
    public static boolean deleteBySingerName(Context context, String singerName) {
        try {
            String sql = "DELETE FROM ";
            sql += SingerInfoDao.TABLENAME;
            sql += " where " + SingerInfoDao.Properties.SingerName.columnName + "=?";
            String args[] = {singerName};
            DBHelper.getInstance(context).getWritableDatabase().execSQL(sql, args);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除
     */
    public static boolean delete(Context context, String imageUrl) {
        try {
            String sql = "DELETE FROM ";
            sql += SingerInfoDao.TABLENAME;
            sql += " where " + SingerInfoDao.Properties.ImageUrl.columnName + "=?";
            String args[] = {imageUrl};
            DBHelper.getInstance(context).getWritableDatabase().execSQL(sql, args);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
