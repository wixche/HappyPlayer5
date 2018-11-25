package com.zlm.hp.util;

/**
 * @Description: time处理类
 * @author: zhangliangming
 * @date: 2018-07-29 18:07
 **/

public class TimeUtil {
    /**
     * 设置倒计时
     *
     * @param time
     * @return
     */
    public static String parseTimeToTimerString(int time) {
        time /= 1000;
        int minute = time / 60;
        int hour = minute / 60;
        int second = time % 60;
        minute %= 60;
        if (hour == 1 || hour == 0) {
            minute += hour * 60;
            return String.format("%02d:%02d", minute, second);
        }
        return String.format("%02d:%02d:%02d", hour, minute, second);

    }

    /**
     * @throws
     * @Description: 转音频时间字符串
     * @param:
     * @return: 00:00
     * @author: zhangliangming
     * @date: 2018-08-05 11:44
     */
    public static String parseTimeToAudioString(int time) {

        time /= 1000;
        int minute = time / 60;
        int second = time % 60;
        minute %= 60;
        return String.format("%02d:%02d", minute, second);
    }

    /**
     * @throws
     * @Description: 转视频时间字符串
     * @param:
     * @return: 00:00:00
     * @author: zhangliangming
     * @date: 2018-08-05 11:44
     */
    public static String parseTimeToVideoString(int time) {
        time /= 1000;
        int minute = time / 60;
        int hour = minute / 60;
        int second = time % 60;
        minute %= 60;
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }
}
