package com.zlm.hp.util;


import com.zlm.hp.http.APIHttpClient;
import com.zlm.hp.http.api.KuGouAndKuWoHttpClient;

import java.net.URLConnection;

/**
 * @Description: httpclient处理类
 * @author: zhangliangming
 * @date: 2018-07-29 21:29
 **/
public class HttpUtil {

    /***
     * 获取httpclient
     * @return
     */
    public static APIHttpClient getHttpClient() {
        APIHttpClient apiHttpClient = new KuGouAndKuWoHttpClient();
        return apiHttpClient;
    }

    /**
     * 设置请求头
     *
     * @param conn
     * @author zhangliangming
     * @date 2017年7月8日
     */
    public static void seURLConnectiontHeader(URLConnection conn) {
        conn.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.3) Gecko/2008092510 Ubuntu/8.04 (hardy) Firefox/3.0.3");
        conn.setRequestProperty("Accept-Language", "en-us,en;q=0.7,zh-cn;q=0.3");
        conn.setRequestProperty("Accept-Encoding", "utf-8");
        conn.setRequestProperty("Accept-Charset",
                "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
        conn.setRequestProperty("Keep-Alive", "300");
        conn.setRequestProperty("connnection", "keep-alive");
        conn.setRequestProperty("If-Modified-Since",
                "Fri, 02 Jan 2009 17:00:05 GMT");
        conn.setRequestProperty("If-None-Match", "\"1261d8-4290-df64d224\"");
        conn.setRequestProperty("Cache-conntrol", "max-age=0");
    }
}
