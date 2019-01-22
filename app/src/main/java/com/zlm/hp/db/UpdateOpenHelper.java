package com.zlm.hp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.zlm.hp.db.dao.DaoMaster;
import com.zlm.hp.db.dao.SubtitleInfoDao;

import org.greenrobot.greendao.database.Database;

/**
 * 更新
 */
public class UpdateOpenHelper extends DaoMaster.OpenHelper {

    public UpdateOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    /**
     * 数据库升级
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            //操作数据库的更新 有几个表升级都可以传入到下面
            SubtitleInfoDao.createTable(db, true);
        }
    }
}