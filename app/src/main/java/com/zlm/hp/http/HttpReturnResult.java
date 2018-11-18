package com.zlm.hp.http;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @Description: http请求结果
 * @author: zhangliangming
 * @date: 2018-07-29 16:55
 **/
public class HttpReturnResult implements Parcelable {

    public static final int STATUS_ERROR_NONET = -1;
    public static final int STATUS_ERROR_NOWIFI = -2;
    public static final int STATUS_ERROR_PARSE = -3;

    public static final String ERROR_MSG_NONET = "无网络";
    public static final String ERROR_MSG_NOWIFI = "非WIFI环境";
    public static final String ERROR_MSG_NET = "网络异常";
    public static final String ERROR_MSG_SERVER = "服务器异常";
    public static final String ERROR_MSG_PARSE = "数据解析出错";
    public static final String ERROR_MEMORY = "内存空间不足";
    public static final String ERROR_FILE_ZERO = "文件长度为0";
    public static final String ERROR_MSG_NULLURL = "地址不存在";
    public static final String ERROR_MSG_NULLDATA = "数据为空";
    /**
     * http状态码
     */
    private int status;
    /***
     * http返回的结果
     */
    private Object result;

    public boolean isSuccessful() {
        return status >= 200 && status < 300;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getErrorMsg() {
        String errorMsg = "";
        if (!isSuccessful()) {
            switch (status) {
                case STATUS_ERROR_NONET:
                    errorMsg = ERROR_MSG_NONET;
                    break;
                case STATUS_ERROR_NOWIFI:
                    errorMsg = ERROR_MSG_NOWIFI;
                    break;
                case STATUS_ERROR_PARSE:
                    errorMsg = ERROR_MSG_PARSE;
                    break;
                default:

                    if (400 <= status && status <= 600) {
                        errorMsg = ERROR_MSG_SERVER;
                    } else {
                        errorMsg = ERROR_MSG_NET;
                    }

                    break;
            }
        }
        return errorMsg;
    }

    public HttpReturnResult() {
    }


    public HttpReturnResult(Parcel in) {
        status = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(status);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<HttpReturnResult> CREATOR = new Creator<HttpReturnResult>() {
        @Override
        public HttpReturnResult createFromParcel(Parcel in) {
            return new HttpReturnResult(in);
        }

        @Override
        public HttpReturnResult[] newArray(int size) {
            return new HttpReturnResult[size];
        }
    };
}
