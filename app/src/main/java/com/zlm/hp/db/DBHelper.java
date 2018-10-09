package com.zlm.hp.db;

import android.content.Context;

import com.zlm.hp.db.dao.DaoMaster;
import com.zlm.hp.db.dao.DaoSession;
import com.zlm.hp.util.ApkUtil;

import org.greenrobot.greendao.database.Database;

/**
 * 数据库辅助类
 * Created by zhangliangming on 2018-08-18.
 */

public class DBHelper {
    private final String DB_NAME = "happyplayer.db";
    private static DBHelper _DBHelper;
    private static UpdateOpenHelper mDevOpenHelper;
    private static DaoMaster mDaoMaster;
    private static DaoSession mDaoSession;
    private static String mPassword;

    private DBHelper(Context context) {
        init(context);
    }

    private void init(Context context) {
        mPassword = ApkUtil.getUniquePsuedoID(context);
        // 初始化数据库信息
        mDevOpenHelper = new UpdateOpenHelper(context, DB_NAME, null);
        getDaoMaster();
        getDaoSession();
    }

    public static DBHelper getInstance(Context context) {
        if (null == _DBHelper) {
            synchronized (DBHelper.class) {
                if (null == _DBHelper) {
                    _DBHelper = new DBHelper(context);
                }
            }
        }
        return _DBHelper;
    }

    /**
     * 获取可写数据库
     *
     * @return
     */
    public Database getWritableDatabase() {
        return mDevOpenHelper.getEncryptedWritableDb(mPassword);
//        return mDevOpenHelper.getWritableDb();
    }

    /**
     * 获取DaoMaster
     *
     * @return
     */
    private DaoMaster getDaoMaster() {
        if (null == mDaoMaster) {
            synchronized (DBHelper.class) {
                if (null == mDaoMaster) {
                    mDaoMaster = new DaoMaster(getWritableDatabase());
                }
            }
        }
        return mDaoMaster;
    }

    /**
     * 获取DaoSession
     *
     * @return
     */
    public DaoSession getDaoSession() {
        if (null == mDaoSession) {
            synchronized (DBHelper.class) {
                mDaoSession = getDaoMaster().newSession();
            }
        }

        return mDaoSession;
    }
}
