package com.zlm.hp.http;

import android.text.TextUtils;

import com.zlm.hp.util.CodeLineUtil;
import com.zlm.hp.util.ZLog;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_MULT_CHOICE;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;

/**
 * @Description: http请求
 * @author: zhangliangming
 * @date: 2018-07-29 13:50
 **/

public class HttpClient {
    /**
     * Numeric status code, 307: Temporary Redirect.
     */
    public static final int HTTP_TEMP_REDIRECT = 307;
    public static final int HTTP_PERM_REDIRECT = 308;

    //
    private static final int CONN_TIMEOUT = 30 * 1000;
    private static final int READ_TIMEOUT = 30 * 1000;

    public class Result {
        private byte[] data;
        private int httpCode = 0;

        public boolean isFailCode() {
            return httpCode == 0 && !isSuccessful();
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
            return data;
        }

        public int getHttpCode() {
            return httpCode;
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
        return doQuery(url, null, null);
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
        return doQuery(url, headParams, null);
    }

    /**
     * @throws
     * @Description: get请求
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-07-29 14:13
     */
    public Result get(String url, Map<String, String> headParams, Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        if (params != null && !params.isEmpty()) {
            for (String key : params.keySet()) {
                if (sb.length() != 0) {
                    sb.append("&");
                }
                try {
                    sb.append(key + "=" + URLEncoder.encode(params.get(key) + "", "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        String requestUrl = String.format("%s?%s", url,
                sb.toString());
        return doQuery(requestUrl, headParams, null);
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
        return doQuery(url, null, data);
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
        return doQuery(url, headParams, data);
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
        return post(url, null, params);
    }

    /**
     * @param
     * @return
     * @throws
     * @Description: post请求
     * @author zhangliangming
     * @date 2018/7/6 0006
     */
    public Result post(String url, Map<String, String> headParams, Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        if (params != null && !params.isEmpty()) {
            for (String key : params.keySet()) {
                if (sb.length() != 0) {
                    sb.append("&");
                }
                try {
                    sb.append(key + "=" + URLEncoder.encode(params.get(key) + "", "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return doQuery(url, headParams, sb.toString().getBytes());
    }

    /**
     * @throws
     * @Description: http/https请求
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-08-18 13:46
     */
    private Result doQuery(String url, Map<String, String> headParams, byte[] data) {
        if (!TextUtils.isEmpty(url) && url.startsWith("https://")) {
            return doQueryHttps(url, headParams, data);
        }
        return doQueryHttp(url, headParams, data);
    }

    /**
     * @param
     * @return
     * @throws
     * @Description: https请求
     * @author zhangliangming
     * @date 2018/6/21 0021
     */
    private Result doQueryHttps(String url, Map<String, String> headParams, byte[] data) {
        Result result = new Result();
        HttpsURLConnection conn = null;
        try {
            //创建SSLContext
            SSLContext sslContext = SSLContext.getInstance("SSL");
            TrustManager[] tm = {new IgnoreSSLTrustManager()};
            //初始化
            sslContext.init(null, tm, new java.security.SecureRandom());
            conn = (HttpsURLConnection) (new URL(url)).openConnection();
            ZLog.i(new CodeLineUtil().getCodeLineInfo(), "HttpsClient REQ => ", url);
            conn.setConnectTimeout(CONN_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setInstanceFollowRedirects(false);
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(false);
            //获取SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            //设置当前实例使用的SSLSoctetFactory
            conn.setSSLSocketFactory(ssf);
            conn.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            //设置通用的请求属性
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("accept", "*/*");

            //添加头参数
            if (headParams != null && !headParams.isEmpty()) {
                for (String key : headParams.keySet()) {

                    try {
                        conn.setRequestProperty(key, URLEncoder.encode(headParams.get(key) + "", "utf-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }

            //判断post请求或者get请求
            if (data != null && data.length > 0) {
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                conn.setRequestProperty("Content-Length", String.valueOf(data.length));
                ZLog.i(new CodeLineUtil().getCodeLineInfo(), "Content-Length: ", String.valueOf(data.length));
                OutputStream os = conn.getOutputStream();
                os.write(data);
                os.flush();
                os.close();
            } else {
                conn.setRequestMethod("GET");
            }

            result.httpCode = conn.getResponseCode();
            ZLog.i(new CodeLineUtil().getCodeLineInfo(), "ResponseCode: ", String.valueOf(result.httpCode));

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
                ZLog.e(new CodeLineUtil().getCodeLineInfo(), "HttpsClient httpcode = " + result.isFailCode() + " error msg = " + result.getDataString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            ZLog.e(new CodeLineUtil().getCodeLineInfo(), "HttpsClient Exception: ", e.getMessage());
        }
        if (conn != null) conn.disconnect();
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
    private Result doQueryHttp(String url, Map<String, String> headParams, byte[] data) {
        Result result = new Result();
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) (new URL(url)).openConnection();
            ZLog.i(new CodeLineUtil().getCodeLineInfo(), "HttpClient REQ => ", url);
            conn.setConnectTimeout(CONN_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setInstanceFollowRedirects(false);
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(false);

            //设置通用的请求属性
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("accept", "*/*");

            //添加头参数
            if (headParams != null && !headParams.isEmpty()) {
                for (String key : headParams.keySet()) {

                    try {
                        conn.setRequestProperty(key, URLEncoder.encode(headParams.get(key) + "", "utf-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }

            //判断post请求或者get请求
            if (data != null && data.length > 0) {
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                conn.setRequestProperty("Content-Length", String.valueOf(data.length));
                ZLog.i(new CodeLineUtil().getCodeLineInfo(), "Content-Length: ", String.valueOf(data.length));
                OutputStream os = conn.getOutputStream();
                os.write(data);
                os.flush();
                os.close();
            } else {
                conn.setRequestMethod("GET");
            }

            result.httpCode = conn.getResponseCode();
            ZLog.i(new CodeLineUtil().getCodeLineInfo(), "ResponseCode: ", String.valueOf(result.httpCode));

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
                ZLog.e(new CodeLineUtil().getCodeLineInfo(), "HttpClient httpcode = " + result.isFailCode() + " error msg = " + result.getDataString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            ZLog.e(new CodeLineUtil().getCodeLineInfo(), "HttpClient Exception: ", e.getMessage());
        }
        if (conn != null) conn.disconnect();
        return result;
    }

    /**
     * @Description: 忽略ssl证书
     * @author: zhangliangming
     * @date: 2018-08-18 13:44
     **/
    private class IgnoreSSLTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}
