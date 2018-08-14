package com.zlm.hp.util;

import android.content.Context;

/**
 * @Description: 全部context处理类
 * @author: zhangliangming
 * @date: 2018-08-05 14:11
 **/

public class ContextUtil {
    private static Context mContext;

    /**
     * 初始context
     * @param context
     */
    public static void init(Context context) {
        mContext = context;
    }

    /**
     * 获取全局的context
     *
     * @return 返回全局context对象
     */
    public static Context getContext() {
        return mContext;
    }
}
