package com.zlm.hp.util;

/**
 * @Description: 获取代码行信息
 * @author: zhangliangming
 * @date: 2018-07-29 12:37
 **/

public class CodeLineUtil {
    /**
     * 获取方法名
     *
     * @return
     */
    public String getCodeLineInfo() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return null;
        }
        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }
            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }
            if (st.getClassName().equals(this.getClass().getName())) {
                continue;
            }
            return Thread.currentThread().getName() + "/"
                    + st.getClassName() + ":" + st.getLineNumber() + "/"
                    + st.getMethodName();
        }
        return "";
    }
}
