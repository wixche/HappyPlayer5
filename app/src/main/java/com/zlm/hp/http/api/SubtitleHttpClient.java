package com.zlm.hp.http.api;

import android.content.Context;
import android.text.TextUtils;

import com.zlm.hp.entity.SubtitleInfo;
import com.zlm.hp.http.HttpClient;
import com.zlm.hp.http.HttpReturnResult;
import com.zlm.hp.util.CodeLineUtil;
import com.zlm.hp.util.NetUtil;
import com.zlm.hp.util.ZLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 字幕下载
 * @author: zhangliangming
 * @date: 2019-01-19 23:45
 **/
public class SubtitleHttpClient {
    /**
     * 搜索
     *
     * @param context
     * @param keyword  搜索字符串，至少长度为3
     * @param page
     * @param pagesize
     * @param askWifi
     * @return
     */
    public HttpReturnResult searchSubtitle(Context context, String keyword, int page, int pagesize, boolean askWifi) {
        HttpReturnResult httpReturnResult = checkNetWork(context, askWifi);
        if (httpReturnResult != null) return httpReturnResult;
        httpReturnResult = new HttpReturnResult();

        String url = "http://api.assrt.net/v1/sub/search";
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", "7zDg04e5xHU5fSlMwMZ5ecwEGmRVn37B");
        params.put("q", keyword);
        params.put("pos", ((page - 1) * pagesize) + "");
        params.put("cnt", pagesize + "");
        HttpClient.Result result = new HttpClient().get(url, null, params);
        httpReturnResult.setStatus(result.getHttpCode());

        if (httpReturnResult.isSuccessful()) {
            String dataResult = result.getDataString();

            ZLog.d(new CodeLineUtil().getCodeLineInfo(), "searchSubtitle result ->" + dataResult);
            JSONObject jsonNode = null;
            try {
                jsonNode = new JSONObject(dataResult);

                int status = jsonNode.optInt("status", -1);
                if (status == 0) {

                    Map<String, Object> returnResult = new HashMap<String, Object>();
                    JSONObject subJsonNode = jsonNode.getJSONObject("sub");
                    if (subJsonNode.has("subs")) {
                        List<SubtitleInfo> lists = new ArrayList<SubtitleInfo>();
                        if (subJsonNode.get("subs") instanceof JSONArray) {
                            JSONArray subsJsonNode = subJsonNode.getJSONArray("subs");
                            for (int i = 0; i < subsJsonNode.length(); i++) {
                                JSONObject dataDataNode = subsJsonNode.getJSONObject(i);

                                String id = dataDataNode.optString("id", "");
                                if (TextUtils.isEmpty(id)) {
                                    continue;
                                }

                                //
                                List<SubtitleInfo> subtitleInfos = getSubtitleInfos(id);
                                if (subtitleInfos != null && subtitleInfos.size() > 0) {
                                    lists.addAll(subtitleInfos);
                                }
                            }
                        }
                        returnResult.put("rows", lists);
                        httpReturnResult.setResult(returnResult);

                        return httpReturnResult;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            httpReturnResult.setStatus(HttpReturnResult.STATUS_ERROR_PARSE);
        }
        return httpReturnResult;
    }

    /**
     * 获取字幕信息
     *
     * @param id
     * @return
     */
    private List<SubtitleInfo> getSubtitleInfos(String id) {
        List<SubtitleInfo> subtitleInfos = new ArrayList<SubtitleInfo>();
        String url = "http://api.assrt.net/v1/sub/detail";
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", "7zDg04e5xHU5fSlMwMZ5ecwEGmRVn37B");
        params.put("id", id);
        HttpClient.Result result = new HttpClient().get(url, null, params);
        if (result.isSuccessful()) {
            String dataResult = result.getDataString();

            ZLog.d(new CodeLineUtil().getCodeLineInfo(), "getSubtitleInfos result ->" + dataResult);
            JSONObject jsonNode = null;
            try {
                jsonNode = new JSONObject(dataResult);

                int status = jsonNode.optInt("status", -1);
                if (status == 0) {

                    JSONObject subJsonNode = jsonNode.getJSONObject("sub");
                    if (subJsonNode.has("subs") && subJsonNode.get("subs") instanceof JSONArray) {
                        JSONArray subsJsonNode = subJsonNode.getJSONArray("subs");
                        for (int i = 0; i < subsJsonNode.length(); i++) {
                            JSONObject dataDataNode = subsJsonNode.getJSONObject(i);
                            if (dataDataNode.has("filelist")) {
                                if (dataDataNode.get("filelist") instanceof JSONArray) {

                                    JSONArray filelistJsonArray = dataDataNode.getJSONArray("filelist");
                                    for (int j = 0; j < filelistJsonArray.length(); j++) {
                                        JSONObject filelistJSONObject = filelistJsonArray.getJSONObject(j);
                                        SubtitleInfo subtitleInfo = getSubtitleInfo(id, filelistJSONObject);
                                        if (subtitleInfo != null) {
                                            subtitleInfos.add(subtitleInfo);
                                        }
                                    }

                                } else if (dataDataNode.get("filelist") instanceof JSONObject) {

                                    JSONObject filelistJSONObject = dataDataNode.getJSONObject("filelist");
                                    SubtitleInfo subtitleInfo = getSubtitleInfo(id, filelistJSONObject);
                                    if (subtitleInfo != null) {
                                        subtitleInfos.add(subtitleInfo);
                                    }

                                }
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return subtitleInfos;
    }

    /**
     * 获取字幕信息
     *
     * @param filelistJSONObject
     * @return
     */
    private SubtitleInfo getSubtitleInfo(String id, JSONObject filelistJSONObject) {
        if (filelistJSONObject != null) {

            SubtitleInfo subtitleInfo = new SubtitleInfo();
            subtitleInfo.setId(id);
            subtitleInfo.setDownloadUrl(filelistJSONObject.optString("url", ""));
            subtitleInfo.setFileName(filelistJSONObject.optString("f", ""));

            return subtitleInfo;
        }
        return null;
    }

    /**
     * 网络检测
     *
     * @param askWifi 是否需要是wifi环境
     * @return
     */
    public HttpReturnResult checkNetWork(Context context, boolean askWifi) {
        HttpReturnResult httpReturnResult = null;
        if (!NetUtil.isNetworkAvailable(context)) {
            //网络不可用
            httpReturnResult = new HttpReturnResult();
            httpReturnResult.setStatus(HttpReturnResult.STATUS_ERROR_NONET);
        }

        if (askWifi && !NetUtil.isWifiConnected(context)) {
            //非wifi环境
            httpReturnResult = new HttpReturnResult();
            httpReturnResult.setStatus(HttpReturnResult.STATUS_ERROR_NOWIFI);
        }
        return httpReturnResult;
    }
}
