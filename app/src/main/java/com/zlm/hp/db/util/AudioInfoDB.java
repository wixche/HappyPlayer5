package com.zlm.hp.db.util;

import android.content.Context;
import android.database.Cursor;

import com.zlm.hp.db.DBHelper;
import com.zlm.hp.db.dao.AudioInfoDao;
import com.zlm.hp.model.AudioInfo;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.List;

/**
 * 音频数据库表处理
 * Created by zhangliangming on 2018-08-18.
 */

public class AudioInfoDB {

    /**
     * 添加歌曲数据
     *
     * @param context
     * @param audioInfo
     * @return
     */
    public static boolean addAudioInfo(Context context, AudioInfo audioInfo) {
        try {
            DBHelper.getInstance(context).getDaoSession().getAudioInfoDao().insert(audioInfo);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 批量添加歌曲数据
     *
     * @param context
     * @param audioInfos
     * @return
     */
    public static boolean addAudioInfos(Context context, List<AudioInfo> audioInfos) {
        try {
            DBHelper.getInstance(context).getDaoSession().getAudioInfoDao().insertInTx(audioInfos);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取本地歌曲个数
     *
     * @param context
     * @return
     */
    public static int getLocalAudioCount(Context context) {
        Cursor cursor = null;
        int count = 0;
        try {
            String args[] = {AudioInfo.TYPE_LOCAL + "", AudioInfo.TYPE_NET + "", AudioInfo.STATUS_FINISH + ""};
            String sql = "select count(*) from " + AudioInfoDao.TABLENAME + " WHERE " + AudioInfoDao.Properties.Type.columnName + "=? or ( " + AudioInfoDao.Properties.Type.columnName + "=? and " + AudioInfoDao.Properties.Status.columnName +
                    "=? )";
            cursor = DBHelper.getInstance(context).getWritableDatabase().rawQuery(sql, args);
            cursor.moveToFirst();
            count = cursor.getInt(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }

    /**
     * 获取本地音频列表
     *
     * @param context
     * @return
     */
    public static List<AudioInfo> getLocalAudios(Context context) {
        try {
            List<AudioInfo> audioInfos = DBHelper.getInstance(context).getDaoSession().getAudioInfoDao().queryBuilder().where(new WhereCondition.StringCondition(AudioInfoDao.Properties.Type.columnName + "=? or ( " + AudioInfoDao.Properties.Type.columnName + "=? and " + AudioInfoDao.Properties.Status.columnName +
                    "=? )", AudioInfo.TYPE_LOCAL + "", AudioInfo.TYPE_NET + "", AudioInfo.STATUS_FINISH + "")).list();
            return audioInfos;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<AudioInfo>();
    }

}
