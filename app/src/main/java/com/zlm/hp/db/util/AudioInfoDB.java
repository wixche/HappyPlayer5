package com.zlm.hp.db.util;

import android.content.Context;
import android.database.Cursor;

import com.zlm.hp.db.DBHelper;
import com.zlm.hp.db.dao.AudioInfoDao;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.manager.DownloadAudioManager;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.util.DateUtil;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.Date;
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
    public static boolean addAudioInfo(Context context, AudioInfo audioInfo, boolean notifyData) {
        try {
            DBHelper.getInstance(context).getDaoSession().getAudioInfoDao().insert(audioInfo);
            if (notifyData) {
                AudioBroadcastReceiver.sendReceiver(context, AudioBroadcastReceiver.ACTION_CODE_UPDATE_LOCAL);
            }
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
                    "=? )", AudioInfo.TYPE_LOCAL + "", AudioInfo.TYPE_NET + "", AudioInfo.STATUS_FINISH + "")).orderAsc(AudioInfoDao.Properties.Category, AudioInfoDao.Properties.ChildCategory).list();
            return audioInfos;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<AudioInfo>();
    }

    /**
     * 添加喜欢歌曲
     *
     * @param context
     * @param audioInfo
     */
    public static boolean addLikeAudio(Context context, AudioInfo audioInfo, boolean notifyData) {
        int type = audioInfo.getType();
        try {
            if (type == AudioInfo.TYPE_LOCAL) {
                audioInfo.setType(AudioInfo.TYPE_LIKE_LOCAL);
            } else {
                audioInfo.setType(AudioInfo.TYPE_LIKE_NET);
            }
            audioInfo.setCreateTime(DateUtil.parseDateToString(new Date()));
            DBHelper.getInstance(context).getDaoSession().getAudioInfoDao().insert(audioInfo);
            if (notifyData) {
                AudioBroadcastReceiver.sendLikeReceiver(context, audioInfo.getHash());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            audioInfo.setType(type);
        }
        return false;
    }

    /**
     * 获取喜欢歌曲个数
     *
     * @param context
     * @return
     */
    public static int getLikeAudioCount(Context context) {
        Cursor cursor = null;
        int count = 0;
        try {
            String args[] = {AudioInfo.TYPE_LIKE_LOCAL + "", AudioInfo.TYPE_LIKE_NET + ""};
            String sql = "select count(*) from " + AudioInfoDao.TABLENAME;
            sql += " where " + AudioInfoDao.Properties.Type.columnName + "=? or " + AudioInfoDao.Properties.Type.columnName + "=?";

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
     * 删除歌曲
     *
     * @param context
     */
    public static boolean deleteAudio(Context context, String hash, boolean notifyData) {

        //删除喜欢
        //删除下载
        //删除最近
        //删除下载任务

        deleteLikeAudio(context, hash, notifyData);
        DownloadTaskDB.delete(context, hash, DownloadAudioManager.mThreadNum);
        deleteRecentAudio(context, hash, notifyData);

        try {
            String sql = "DELETE FROM ";
            sql += AudioInfoDao.TABLENAME;
            sql += " where " + AudioInfoDao.Properties.Hash.columnName + "=?";
            String args[] = {hash};
            DBHelper.getInstance(context).getWritableDatabase().execSQL(sql, args);
            if (notifyData) {
                AudioBroadcastReceiver.sendReceiver(context, AudioBroadcastReceiver.ACTION_CODE_UPDATE_LOCAL);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除喜欢歌曲
     *
     * @param context
     */
    public static boolean deleteLikeAudio(Context context, String hash, boolean notifyData) {
        try {
            String sql = "DELETE FROM ";
            sql += AudioInfoDao.TABLENAME;
            sql += " where (" + AudioInfoDao.Properties.Type.columnName + "=? or " + AudioInfoDao.Properties.Type.columnName + "=? ) and " + AudioInfoDao.Properties.Hash.columnName + "=?";

            String args[] = {AudioInfo.TYPE_LIKE_LOCAL + "", AudioInfo.TYPE_LIKE_NET + "", hash};
            DBHelper.getInstance(context).getWritableDatabase().execSQL(sql, args);
            if (notifyData) {
                AudioBroadcastReceiver.sendLikeReceiver(context, hash);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断当前歌曲是否是喜欢歌曲
     *
     * @param context
     * @param hash
     * @return
     */
    public static boolean isLikeAudioExists(Context context, String hash) {
        Cursor cursor = null;
        try {
            String args[] = {AudioInfo.TYPE_LIKE_LOCAL + "", AudioInfo.TYPE_LIKE_NET + "", hash};
            String sql = "select * from " + AudioInfoDao.TABLENAME;
            sql += " where (" + AudioInfoDao.Properties.Type.columnName + "=? or " + AudioInfoDao.Properties.Type.columnName + "=? ) and " + AudioInfoDao.Properties.Hash.columnName + "=?";
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
     * 获取喜欢音频列表
     *
     * @param context
     * @return
     */
    public static List<AudioInfo> getLikeAudios(Context context) {
        try {
            List<AudioInfo> audioInfos = DBHelper.getInstance(context).getDaoSession().getAudioInfoDao().queryBuilder().where(new WhereCondition.StringCondition("( " + AudioInfoDao.Properties.Type.columnName + "=? or " + AudioInfoDao.Properties.Type.columnName +
                    "=? )", AudioInfo.TYPE_LIKE_LOCAL + "", AudioInfo.TYPE_LIKE_NET + "")).orderDesc(AudioInfoDao.Properties.CreateTime).list();
            if (audioInfos != null && audioInfos.size() > 0) {

                for (int i = 0; i < audioInfos.size(); i++) {
                    AudioInfo temp = audioInfos.get(i);
                    int type = temp.getType();
                    //添加时修改了类型，从数据库中获取后，需要修改为原来的状态
                    if (type == AudioInfo.TYPE_LIKE_LOCAL) {
                        temp.setType(AudioInfo.TYPE_LOCAL);
                    } else {
                        temp.setType(AudioInfo.TYPE_NET);
                    }
                }
            }
            return audioInfos;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<AudioInfo>();
    }


    /**
     * 添加最近歌曲
     *
     * @param context
     * @param audioInfo
     */
    public static boolean addRecentAudio(Context context, AudioInfo audioInfo, boolean notifyData) {
        int type = audioInfo.getType();
        try {
            if (type == AudioInfo.TYPE_LOCAL) {
                audioInfo.setType(AudioInfo.TYPE_RECENT_LOCAL);
            } else {
                audioInfo.setType(AudioInfo.TYPE_RECENT_NET);
            }
            audioInfo.setCreateTime(DateUtil.parseDateToString(new Date()));
            DBHelper.getInstance(context).getDaoSession().getAudioInfoDao().insert(audioInfo);
            if (notifyData) {
                AudioBroadcastReceiver.sendReceiver(context, AudioBroadcastReceiver.ACTION_CODE_UPDATE_RECENT);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            audioInfo.setType(type);
        }
        return false;
    }

    /**
     * 获取最近歌曲个数
     *
     * @param context
     * @return
     */
    public static int getRecentAudioCount(Context context) {
        Cursor cursor = null;
        int count = 0;
        try {
            String args[] = {AudioInfo.TYPE_RECENT_LOCAL + "", AudioInfo.TYPE_RECENT_NET + ""};
            String sql = "select count(*) from " + AudioInfoDao.TABLENAME;
            sql += " where " + AudioInfoDao.Properties.Type.columnName + "=? or " + AudioInfoDao.Properties.Type.columnName + "=?";

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
     * 删除最近歌曲
     *
     * @param context
     */
    public static boolean deleteRecentAudio(Context context, String hash, boolean notifyData) {
        try {
            String sql = "DELETE FROM ";
            sql += AudioInfoDao.TABLENAME;
            sql += " where (" + AudioInfoDao.Properties.Type.columnName + "=? or " + AudioInfoDao.Properties.Type.columnName + "=? ) and " + AudioInfoDao.Properties.Hash.columnName + "=?";

            String args[] = {AudioInfo.TYPE_RECENT_LOCAL + "", AudioInfo.TYPE_RECENT_NET + "", hash};
            DBHelper.getInstance(context).getWritableDatabase().execSQL(sql, args);
            if (notifyData) {
                AudioBroadcastReceiver.sendReceiver(context, AudioBroadcastReceiver.ACTION_CODE_UPDATE_RECENT);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断当前歌曲是否是最近歌曲
     *
     * @param context
     * @param hash
     * @return
     */
    public static boolean isRecentAudioExists(Context context, String hash) {
        Cursor cursor = null;
        try {
            String args[] = {AudioInfo.TYPE_RECENT_LOCAL + "", AudioInfo.TYPE_RECENT_NET + "", hash};
            String sql = "select * from " + AudioInfoDao.TABLENAME;
            sql += " where (" + AudioInfoDao.Properties.Type.columnName + "=? or " + AudioInfoDao.Properties.Type.columnName + "=? ) and " + AudioInfoDao.Properties.Hash.columnName + "=?";
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
     * 获取最近音频列表
     *
     * @param context
     * @return
     */
    public static List<AudioInfo> getRecentAudios(Context context) {
        try {
            List<AudioInfo> audioInfos = DBHelper.getInstance(context).getDaoSession().getAudioInfoDao().queryBuilder().where(new WhereCondition.StringCondition("( " + AudioInfoDao.Properties.Type.columnName + "=? or " + AudioInfoDao.Properties.Type.columnName +
                    "=? )", AudioInfo.TYPE_RECENT_LOCAL + "", AudioInfo.TYPE_RECENT_NET + "")).orderDesc(AudioInfoDao.Properties.CreateTime).list();
            if (audioInfos != null && audioInfos.size() > 0) {

                for (int i = 0; i < audioInfos.size(); i++) {
                    AudioInfo temp = audioInfos.get(i);
                    int type = temp.getType();
                    //添加时修改了类型，从数据库中获取后，需要修改为原来的状态
                    if (type == AudioInfo.TYPE_RECENT_LOCAL) {
                        temp.setType(AudioInfo.TYPE_LOCAL);
                    } else {
                        temp.setType(AudioInfo.TYPE_NET);
                    }
                }
            }
            return audioInfos;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<AudioInfo>();
    }

    /**
     * 更新最近歌曲时间
     */
    public static boolean updateRecentAudio(Context context, String hash, String createTime) {
        try {

            String sql = "UPDATE ";
            sql += AudioInfoDao.TABLENAME;
            sql += " SET " + AudioInfoDao.Properties.CreateTime.columnName + " =?";
            sql += " where " + AudioInfoDao.Properties.Hash.columnName + "=? and (" + AudioInfoDao.Properties.Type.columnName + "=?  or " + AudioInfoDao.Properties.Type.columnName + "=?)";

            String args[] = {createTime, hash, AudioInfo.TYPE_RECENT_LOCAL + "", AudioInfo.TYPE_RECENT_NET + ""};
            DBHelper.getInstance(context).getWritableDatabase().execSQL(sql, args);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取下载歌曲个数
     *
     * @param context
     * @return
     */
    public static int getDownloadAudioCount(Context context) {
        Cursor cursor = null;
        int count = 0;
        try {
            String args[] = {AudioInfo.TYPE_NET + "", AudioInfo.STATUS_DOWNLOADING + "", AudioInfo.STATUS_FINISH + ""};
            String sql = "select count(*) from " + AudioInfoDao.TABLENAME + " WHERE " + AudioInfoDao.Properties.Type.columnName + "=? and (" + AudioInfoDao.Properties.Status.columnName +
                    "=? or " + AudioInfoDao.Properties.Status.columnName + " =?)";
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
     * 添加下载歌曲
     *
     * @param context
     * @param audioInfo
     */
    public static boolean addDownloadAudio(Context context, AudioInfo audioInfo, boolean notifyData) {
        try {
            audioInfo.setStatus(AudioInfo.STATUS_DOWNLOADING);
            audioInfo.setCreateTime(DateUtil.parseDateToString(new Date()));
            DBHelper.getInstance(context).getDaoSession().getAudioInfoDao().insert(audioInfo);
            if (notifyData) {
                AudioBroadcastReceiver.sendReceiver(context, AudioBroadcastReceiver.ACTION_CODE_UPDATE_DOWNLOAD);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更新下载歌曲时间
     */
    public static boolean addDownloadedAudio(Context context, String hash, boolean notifyData) {
        boolean flag = updateDownloadAudio(context, hash, DateUtil.parseDateToString(new Date()), AudioInfo.STATUS_FINISH);
        if (notifyData) {
            AudioBroadcastReceiver.sendReceiver(context, AudioBroadcastReceiver.ACTION_CODE_UPDATE_LOCAL);
        }
        return flag;
    }

    /**
     * 更新下载歌曲时间
     */
    public static boolean updateDownloadAudio(Context context, String hash, String createTime, int status) {
        try {

            String sql = "UPDATE ";
            sql += AudioInfoDao.TABLENAME;
            sql += " SET " + AudioInfoDao.Properties.CreateTime.columnName + " =?," + AudioInfoDao.Properties.Status.columnName + " =?";
            sql += " where " + AudioInfoDao.Properties.Hash.columnName + "=? and " + AudioInfoDao.Properties.Type.columnName + "=?";

            String args[] = {createTime, status + "", hash, AudioInfo.TYPE_NET + ""};
            DBHelper.getInstance(context).getWritableDatabase().execSQL(sql, args);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 删除下载歌曲
     *
     * @param context
     */
    public static boolean deleteDownloadAudio(Context context, String hash, boolean notifyData) {
        try {
            String sql = "DELETE FROM ";
            sql += AudioInfoDao.TABLENAME;
            sql += " where " + AudioInfoDao.Properties.Type.columnName + "=? and " + AudioInfoDao.Properties.Hash.columnName + "=? and (" + AudioInfoDao.Properties.Status.columnName + " =? or " + AudioInfoDao.Properties.Status.columnName + " =?)";

            String args[] = {AudioInfo.TYPE_NET + "", hash, AudioInfo.STATUS_DOWNLOADING + "", AudioInfo.STATUS_FINISH + ""};
            DBHelper.getInstance(context).getWritableDatabase().execSQL(sql, args);
            if (notifyData) {
                AudioBroadcastReceiver.sendReceiver(context, AudioBroadcastReceiver.ACTION_CODE_UPDATE_DOWNLOAD);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断当前歌曲是否是下载歌曲
     *
     * @param context
     * @param hash
     * @return
     */
    public static boolean isDownloadAudioExists(Context context, String hash) {
        Cursor cursor = null;
        try {
            String args[] = {AudioInfo.TYPE_NET + "", hash, AudioInfo.STATUS_DOWNLOADING + "", AudioInfo.STATUS_FINISH + ""};
            String sql = "select * from " + AudioInfoDao.TABLENAME;
            sql += " where " + AudioInfoDao.Properties.Type.columnName + "=? and " + AudioInfoDao.Properties.Hash.columnName + "=? and (" + AudioInfoDao.Properties.Status.columnName + " =? or " + AudioInfoDao.Properties.Status.columnName + " =?)";
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
     * 判断当前歌曲是否是下载完成歌曲
     *
     * @param context
     * @param hash
     * @return
     */
    public static boolean isDownloadedAudioExists(Context context, String hash) {
        Cursor cursor = null;
        try {
            String args[] = {AudioInfo.TYPE_NET + "", hash, AudioInfo.STATUS_FINISH + ""};
            String sql = "select * from " + AudioInfoDao.TABLENAME;
            sql += " where " + AudioInfoDao.Properties.Type.columnName + "=? and " + AudioInfoDao.Properties.Hash.columnName + "=? and " + AudioInfoDao.Properties.Status.columnName + " =?";
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
     * 获取下载中音频列表
     *
     * @param context
     * @return
     */
    public static List<AudioInfo> getDownloadingAudios(Context context) {
        try {
            List<AudioInfo> audioInfos = DBHelper.getInstance(context).getDaoSession().getAudioInfoDao().queryBuilder().where(new WhereCondition.StringCondition(AudioInfoDao.Properties.Type.columnName + "=? and " + AudioInfoDao.Properties.Status.columnName +
                    "=?", AudioInfo.TYPE_NET + "", AudioInfo.STATUS_DOWNLOADING + "")).orderDesc(AudioInfoDao.Properties.CreateTime).list();
            return audioInfos;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<AudioInfo>();
    }

    /**
     * 获取下载完成音频列表
     *
     * @param context
     * @return
     */
    public static List<AudioInfo> getDownloadedAudios(Context context) {
        try {
            List<AudioInfo> audioInfos = DBHelper.getInstance(context).getDaoSession().getAudioInfoDao().queryBuilder().where(new WhereCondition.StringCondition(AudioInfoDao.Properties.Type.columnName + "=? and " + AudioInfoDao.Properties.Status.columnName +
                    "=?", AudioInfo.TYPE_NET + "", AudioInfo.STATUS_FINISH + "")).orderDesc(AudioInfoDao.Properties.CreateTime).list();
            return audioInfos;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<AudioInfo>();
    }

}
