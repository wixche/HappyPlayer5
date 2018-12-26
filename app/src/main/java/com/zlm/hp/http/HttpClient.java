package com.zlm.hp.http;

import android.text.TextUtils;

import com.zlm.hp.util.ZLog;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by zhangliangming on 2018/6/21 0021.
 */

public class HttpClient {

    public static final int HTTP_OK = 200;
    private static final int MAX_JUMP = 20;
    /**
     * HTTP Status-Code 300: Multiple Choices.
     */
    private final int HTTP_MULT_CHOICE = 300;
    /**
     * HTTP Status-Code 301: Moved Permanently.
     */
    private final int HTTP_MOVED_PERM = 301;

    /**
     * HTTP Status-Code 302: Temporary Redirect.
     */
    private final int HTTP_MOVED_TEMP = 302;

    /**
     * HTTP Status-Code 303: See Other.
     */
    private final int HTTP_SEE_OTHER = 303;

    /**
     * Numeric status code, 307: Temporary Redirect.
     */
    private final int HTTP_TEMP_REDIRECT = 307;
    private final int HTTP_PERM_REDIRECT = 308;

    //
    private final int CONN_TIMEOUT = 30 * 1000;
    private final int READ_TIMEOUT = 30 * 1000;

    public class Result {
        private byte[] data;
        private int httpCode = 0;
        private int jumpCount = 0;

        public boolean isFailCode() {
            return httpCode == 0 || !isSuccessful();
        }

        public boolean isSuccessful() {
            return httpCode >= 200 && httpCode < 300;
        }

        public boolean isRedirect() {
            switch (httpCode) {
                case HTTP_PERM_REDIRECT:
                case HTTP_TEMP_REDIRECT:
                case HTTP_MULT_CHOICE:
                case HTTP_MOVED_PERM:
                case HTTP_MOVED_TEMP:
                case HTTP_SEE_OTHER:
                    return true;
                default:
                    return false;
            }
        }

        public String getDataString() {
            if (data != null && data.length > 0) {
                try {
                    return new String(data, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            return new String();
        }

        public byte[] getData() {
            if (data != null && data.length > 0) {
                return data;
            }
            return new byte[0];
        }

        public int getHttpCode() {
            return httpCode;
        }

        /**
         * 是否跳转
         *
         * @return
         */
        public boolean hasJump() {
            return jumpCount > 0;
        }
    }

    /**
     * @param
     * @return
     * @throws
     * @Description: get请求
     * @author zhangliangming
     * @date 2018/7/6 0006
     */
    public Result get(String url) {
        return doQuery(url, null, null, MAX_JUMP);
    }

    /**
     * @param
     * @return
     * @throws
     * @Description: get请求
     * @author zhangliangming
     * @date 2018/7/6 0006
     */
    public Result get(String url, int jump) {
        return doQuery(url, null, null, jump);
    }

    /**
     * @param
     * @return
     * @throws
     * @Description: get请求
     * @author zhangliangming
     * @date 2018/7/6 0006
     */
    public Result get(String url, Map<String, String> headParams) {
        return doQuery(url, headParams, null, MAX_JUMP);
    }

    /**
     * @param
     * @return
     * @throws
     * @Description: get请求
     * @author zhangliangming
     * @date 2018/7/6 0006
     */
    public Result get(String url, Map<String, String> headParams, int jump) {
        return doQuery(url, headParams, null, jump);
    }

    /**
     * @throws
     * @Description: get请求
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-07-29 14:13
     */
    public Result get(String url, Map<String, String> headParams,
                      Map<String, String> params) {
        return get(url, headParams, params, MAX_JUMP);
    }

    /**
     * @throws
     * @Description: get请求
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-07-29 14:13
     */
    public Result get(String url, Map<String, String> headParams,
                      Map<String, String> params, int jump) {
        StringBuilder sb = new StringBuilder();
        if (params != null && !params.isEmpty()) {
            for (String key : params.keySet()) {
                if (sb.length() != 0) {
                    sb.append("&");
                }
                try {
                    sb.append(key + "="
                            + URLEncoder.encode(params.get(key) + "", "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        String requestUrl = String.format("%s?%s", url, sb.toString());
        return doQuery(requestUrl, headParams, null, jump);
    }

    /**
     * @param
     * @return
     * @throws
     * @Description: post请求
     * @author zhangliangming
     * @date 2018/7/6 0006
     */
    public Result post(String url, byte[] data, int jump) {
        if (data == null) {
            data = "".getBytes();
        }
        return doQuery(url, null, data, jump);
    }

    /**
     * @param
     * @return
     * @throws
     * @Description: post请求
     * @author zhangliangming
     * @date 2018/7/6 0006
     */
    public Result post(String url, byte[] data) {
        if (data == null) {
            data = "".getBytes();
        }
        return doQuery(url, null, data, MAX_JUMP);
    }

    /**
     * @param
     * @return
     * @throws
     * @Description: post请求
     * @author zhangliangming
     * @date 2018/7/6 0006
     */
    public Result post(String url, Map<String, String> headParams, byte[] data,
                       int jump) {
        if (data == null) {
            data = "".getBytes();
        }
        return doQuery(url, headParams, data, jump);
    }

    /**
     * @param
     * @return
     * @throws
     * @Description: post请求
     * @author zhangliangming
     * @date 2018/7/6 0006
     */
    public Result post(String url, Map<String, String> headParams, byte[] data) {
        if (data == null) {
            data = "".getBytes();
        }
        return doQuery(url, headParams, data, MAX_JUMP);
    }

    /**
     * @param
     * @return
     * @throws
     * @Description: post请求
     * @author zhangliangming
     * @date 2018/7/6 0006
     */
    public Result post(String url, Map<String, String> params, int jump) {
        return post(url, null, params, jump);
    }

    /**
     * @param
     * @return
     * @throws
     * @Description: post请求
     * @author zhangliangming
     * @date 2018/7/6 0006
     */
    public Result post(String url, Map<String, String> params) {
        return post(url, null, params, MAX_JUMP);
    }

    /**
     * @param
     * @return
     * @throws
     * @Description: post请求
     * @author zhangliangming
     * @date 2018/7/6 0006
     */
    public Result post(String url, Map<String, String> headParams,
                       Map<String, String> params) {
        return post(url, headParams, params, MAX_JUMP);
    }

    /**
     * @param
     * @return
     * @throws
     * @Description: post请求
     * @author zhangliangming
     * @date 2018/7/6 0006
     */
    public Result post(String url, Map<String, String> headParams,
                       Map<String, String> params, int jump) {
        StringBuilder sb = new StringBuilder();
        if (params != null && !params.isEmpty()) {
            for (String key : params.keySet()) {
                if (sb.length() != 0) {
                    sb.append("&");
                }
                try {
                    sb.append(key + "="
                            + URLEncoder.encode(params.get(key) + "", "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return doQuery(url, headParams, sb.toString().getBytes(), jump);
    }

    /**
     * @param
     * @return
     * @throws
     * @Description: http/https请求
     * @author zhangliangming
     * @date 2018/6/21 0021
     */
    private Result doQuery(String url, Map<String, String> headParams,
                           byte[] data, int jump) {
        if (url.startsWith("https://")) {
            return doQueryHttps(url, headParams, data, jump);
        }
        return doQueryHttp(url, headParams, data, jump);
    }

    /**
     * @param
     * @return
     * @throws
     * @Description: https请求
     * @author zhangliangming
     * @date 2018/6/21 0021
     */
    private Result doQueryHttps(String url, Map<String, String> headParams,
                                byte[] data, int jump) {

        Result result = null;
        HttpsURLConnection conn = null;
        int i = 0;
        if (jump <= 0) {
            jump = 1;
        }
        try {
            for (; i < jump && !TextUtils.isEmpty(url); ++i) {
                result = new Result();
                // 创建SSLContext
                SSLContext sslContext = SSLContext.getInstance("SSL");
                TrustManager[] tm = {new IgnoreSSLTrustManager()};
                // 初始化
                sslContext.init(null, tm, new SecureRandom());
                // 获取SSLSocketFactory对象
                SSLSocketFactory ssf = sslContext.getSocketFactory();
                conn = (HttpsURLConnection) (new URL(url)).openConnection();
                ZLog.i("HttpsClient REQ => " + url);
                conn.setConnectTimeout(CONN_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setInstanceFollowRedirects(false);
                conn.setUseCaches(false);
                //conn.setAllowUserInteraction(false);

                // 设置通用的请求属性
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Charset", "UTF-8");
                conn.setRequestProperty("accept", "*/*");

                // https忽略证书
                conn.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        // 强行返回true 即验证成功
                        return true;
                    }
                });
                conn.setSSLSocketFactory(ssf);

                // 添加头参数
                if (headParams != null && !headParams.isEmpty()) {
                    for (String key : headParams.keySet()) {
                        conn.setRequestProperty(key, headParams.get(key));
                    }
                }

                // 判断post请求或者get请求
                if (data != null) {
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);

                    conn.setRequestProperty("Content-Length",
                            String.valueOf(data.length));
                    ZLog.i(
                            "Content-Length: " + String.valueOf(data.length));
                    OutputStream os = conn.getOutputStream();
                    os.write(data);
                    os.flush();
                    os.close();
                } else {
                    conn.setRequestMethod("GET");
                }

                result.httpCode = conn.getResponseCode();
                ZLog.i(
                        "ResponseCode: " + String.valueOf(result.httpCode));
                if (result.isRedirect()) {
                    // 重定向
                    url = getConnHead(conn, "Location", null);

                    conn.disconnect();
                    conn = null;
                } else {
                    InputStream inputStream = null;
                    if (result.isSuccessful()) {
                        inputStream = conn.getInputStream();
                    } else {
                        inputStream = conn.getErrorStream();
                    }

                    //
                    if (inputStream != null) {
                        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int len = -1;
                        while ((len = inputStream.read(buffer)) != -1) {
                            outStream.write(buffer, 0, len);
                        }
                        outStream.close();
                        inputStream.close();
                        result.data = outStream.toByteArray();
                    }
                    //
                    if (result.isFailCode()) {
                        ZLog.i("HttpsClient httpcode = "
                                + result.isFailCode() + " error msg = "
                                + result.getDataString());
                    }
                    // 结束循环
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ZLog.i(
                    "HttpsClient Exception: " + e.getMessage());
        }
        if (conn != null)
            conn.disconnect();
        result.jumpCount = i;
        return result;

    }

    /**
     * @param
     * @return
     * @throws
     * @Description: http请求
     * @author zhangliangming
     * @date 2018/6/21 0021
     */
    private Result doQueryHttp(String url, Map<String, String> headParams,
                               byte[] data, int jump) {
        Result result = null;
        HttpURLConnection conn = null;
        int i = 0;
        if (jump <= 0) {
            jump = 1;
        }
        try {
            for (; i < jump && !TextUtils.isEmpty(url); ++i) {
                result = new Result();
                conn = (HttpURLConnection) (new URL(url)).openConnection();
                ZLog.i("HttpClient REQ => " + url);
                conn.setConnectTimeout(CONN_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setInstanceFollowRedirects(false);
                conn.setUseCaches(false);
                conn.setAllowUserInteraction(false);

                // 设置通用的请求属性
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Charset", "UTF-8");
                conn.setRequestProperty("accept", "*/*");

                // 添加头参数
                if (headParams != null && !headParams.isEmpty()) {
                    for (String key : headParams.keySet()) {
                        conn.setRequestProperty(key, headParams.get(key));
                    }
                }

                // 判断post请求或者get请求
                if (data != null) {
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);

                    conn.setRequestProperty("Content-Length",
                            String.valueOf(data.length));
                    ZLog.i(
                            "Content-Length: " + String.valueOf(data.length));
                    OutputStream os = conn.getOutputStream();
                    os.write(data);
                    os.flush();
                    os.close();
                } else {
                    conn.setRequestMethod("GET");
                }

                result.httpCode = conn.getResponseCode();
                ZLog.i(
                        "ResponseCode: " + String.valueOf(result.httpCode));
                if (result.isRedirect()) {
                    // 重定向
                    url = getConnHead(conn, "Location", null);

                    conn.disconnect();
                    conn = null;
                } else {
                    InputStream inputStream = null;
                    if (result.isSuccessful()) {
                        inputStream = conn.getInputStream();
                    } else {
                        inputStream = conn.getErrorStream();
                    }

                    //
                    if (inputStream != null) {
                        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int len = -1;
                        while ((len = inputStream.read(buffer)) != -1) {
                            outStream.write(buffer, 0, len);
                        }
                        outStream.close();
                        inputStream.close();
                        result.data = outStream.toByteArray();
                    }
                    //
                    if (result.isFailCode()) {
                        ZLog.i("HttpClient httpcode = "
                                + result.isFailCode() + " error msg = "
                                + result.getDataString());
                    }

                    // 结束循环
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ZLog.i(
                    "HttpClient Exception: " + e.getMessage());
        }
        if (conn != null)
            conn.disconnect();
        result.jumpCount = i;
        return result;
    }

    /**
     * 获取头信息
     *
     * @param conn
     * @param name
     * @param def
     * @return
     */
    private String getConnHead(HttpURLConnection conn, String name, String def) {
        String val = conn.getHeaderField(name);
        if (val == null)
            return def;
        return val.trim();
    }

    /**
     * 忽略ssl证书
     *
     * @author zhangliangming
     */
    public static class IgnoreSSLTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

    }

}
