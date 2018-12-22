package com.zlm.hp.db.util;

import android.content.Context;
import android.database.Cursor;

import com.zlm.down.entity.DownloadThreadInfo;
import com.zlm.hp.db.DBHelper;
import com.zlm.hp.db.dao.DownloadThreadInfoDao;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;

/**
 * @Description: 下载线程db处理
 * @author: zhangliangming
 * @date: 2018-10-07 20:24
 **/
public class DownloadThreadInfoDB {
    /**
     * 添加下载线程任务
     *
     * @param downloadThreadInfo
     */
    public static boolean add(Context context, DownloadThreadInfo downloadThreadInfo) {
        try {
            DBHelper.getInstance(context).getDaoSession().getDownloadThreadInfoDao().insert(downloadThreadInfo);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @throws
     * @Description: 获取线程任务
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-10-07 20:32
     */
    public static DownloadThreadInfo getDownloadThreadInfo(Context context, String tid, int threadNum, int threadId) {
        try {
            List<DownloadThreadInfo> downloadThreadInfos = DBHelper.getInstance(context).getDaoSession().getDownloadThreadInfoDao().queryBuilder().where(new WhereCondition.StringCondition(DownloadThreadInfoDao.Properties.TaskId.columnName + "=? and  " + DownloadThreadInfoDao.Properties.ThreadNum.columnName + "=? and " + DownloadThreadInfoDao.Properties.ThreadId.columnName +
                    "=?", tid + "", threadNum + "", threadId + "")).list();
            if (downloadThreadInfos != null && downloadThreadInfos.size() > 0) {
                return downloadThreadInfos.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 线程任务是否存在
     *
     * @param tid
     * @param threadNum
     * @param threadID
     * @return
     */
    public static boolean isExists(Context context, String tid, int threadNum, int threadID) {
        Cursor cursor = null;
        try {
            String args[] = {tid, threadNum + "", threadID + ""};
            String sql = "select * from " + DownloadThreadInfoDao.TABLENAME + " WHERE " + DownloadThreadInfoDao.Properties.TaskId.columnName + "=? and  " + DownloadThreadInfoDao.Properties.ThreadNum.columnName + "=? and " + DownloadThreadInfoDao.Properties.ThreadId.columnName +
                    "=?";
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
     * 获取下载进度
     *
     * @param tid
     * @return
     */
    public static int getDownloadedSize(Context context, String tid, int threadNum) {
        Cursor cursor = null;
        try {
            String args[] = {tid, threadNum + ""};
            String name = DownloadThreadInfoDao.Properties.DownloadedSize.columnName;
            String sql = "SELECT sum(" + name + ") as " + name + " from " + DownloadThreadInfoDao.TABLENAME
                    + " WHERE " + DownloadThreadInfoDao.Properties.TaskId.columnName + "=? and " + DownloadThreadInfoDao.Properties.ThreadNum.columnName + "=?";

            cursor = DBHelper.getInstance(context).getWritableDatabase().rawQuery(sql, args);
            if (cursor.moveToNext()) {
                return cursor.getInt(cursor.getColumnIndex(name));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

    /**
     * 更新下载线程任务
     */
    public static boolean update(Context context, String tid, int threadNum, int threadID,
                                 int downloadedSize) {
        try {

            String sql = "UPDATE ";
            sql += DownloadThreadInfoDao.TABLENAME;
            sql += " SET " + DownloadThreadInfoDao.Properties.DownloadedSize.columnName + " =?";
            sql += " where " + DownloadThreadInfoDao.Properties.TaskId.columnName + "=? and " + DownloadThreadInfoDao.Properties.ThreadNum.columnName + "=?  and " + DownloadThreadInfoDao.Properties.ThreadId.columnName + "=?";

            String args[] = {downloadedSize + "", tid, threadNum + "", threadID + ""};
            DBHelper.getInstance(context).getWritableDatabase().execSQL(sql, args);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除下载线程任务
     */
    public static boolean delete(Context context, String tid, int threadNum) {
        try {
            String sql = "DELETE FROM ";
            sql += DownloadThreadInfoDao.TABLENAME;
            sql += " where " + DownloadThreadInfoDao.Properties.TaskId.columnName + "=? and " + DownloadThreadInfoDao.Properties.ThreadNum.columnName + "=?";

            String args[] = {tid, threadNum + ""};
            DBHelper.getInstance(context).getWritableDatabase().execSQL(sql, args);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除所有下载线程任务
     */
    public static boolean deleteAll(Context context, int threadNum) {
        try {
            String sql = "DELETE FROM ";
            sql += DownloadThreadInfoDao.TABLENAME;
            sql += " where " + DownloadThreadInfoDao.Properties.ThreadNum.columnName + "=?";

            String args[] = {threadNum + ""};
            DBHelper.getInstance(context).getWritableDatabase().execSQL(sql, args);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
