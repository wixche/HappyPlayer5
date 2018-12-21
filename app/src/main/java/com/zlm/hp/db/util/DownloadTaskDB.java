package com.zlm.hp.db.util;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import com.zlm.down.entity.DownloadTask;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.db.DBHelper;
import com.zlm.hp.db.dao.DownloadTaskDao;
import com.zlm.hp.util.ResourceUtil;

import java.io.File;

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

    /**
     * 任务是否存在
     *
     * @param tid
     * @param threadNum
     * @return
     */
    public static boolean isExists(Context context, String tid, int threadNum) {
        Cursor cursor = null;
        try {
            String args[] = {tid, threadNum + ""};
            String sql = "select * from " + DownloadTaskDao.TABLENAME + " WHERE " + DownloadTaskDao.Properties.TaskId.columnName + "=? and  " + DownloadTaskDao.Properties.ThreadNum.columnName + "=?";
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
     * 更新下载任务
     */
    public static boolean update(Context context, String tid, int threadNum,int status) {
        try {

            String sql = "UPDATE ";
            sql += DownloadTaskDao.TABLENAME;
            sql += " SET " + DownloadTaskDao.Properties.Status.columnName + " =?";
            sql += " where " + DownloadTaskDao.Properties.TaskId.columnName + "=? and " + DownloadTaskDao.Properties.ThreadNum.columnName + "=?";

            String args[] = {status + "",tid, threadNum + ""};
            DBHelper.getInstance(context).getWritableDatabase().execSQL(sql, args);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除hash对应的数据
     */
    public static void delete(Context context,String taskid,int threadNum) {
        try {

            if(AudioInfoDB.isDownloadAudioExists(context,taskid)){
                //删除下载歌曲
                AudioInfoDB.deleteDownloadAudio(context,taskid,true);
            }

            if(isExists(context,taskid,threadNum)){
                //删除任务
                deleteTask(context,taskid, threadNum);

                //删除任务线程
                DownloadThreadInfoDB.delete(context,taskid, threadNum);
            }

            //删除本地缓存文件
            String tempFilePath = ResourceUtil.getFilePath(context, ResourceConstants.PATH_AUDIO_TEMP, taskid + ".temp");
            File tempFile = new File(tempFilePath);
            if (tempFile.exists()) {
                tempFile.delete();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除下载任务
     */
    private static boolean deleteTask(Context context, String tid,int threadNum) {
        try {
            String sql = "DELETE FROM ";
            sql += DownloadTaskDao.TABLENAME;
            sql += " where " + DownloadTaskDao.Properties.TaskId.columnName + "=? and " + DownloadTaskDao.Properties.ThreadNum.columnName + "=?";

            String args[] = {tid, threadNum + ""};
            DBHelper.getInstance(context).getWritableDatabase().execSQL(sql, args);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
