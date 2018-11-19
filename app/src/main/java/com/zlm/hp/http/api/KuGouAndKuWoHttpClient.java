package com.zlm.hp.http.api;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.zlm.hp.entity.AlbumInfo;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.entity.LrcInfo;
import com.zlm.hp.entity.RankInfo;
import com.zlm.hp.entity.SingerInfo;
import com.zlm.hp.entity.SpecialInfo;
import com.zlm.hp.entity.VideoInfo;
import com.zlm.hp.http.APIHttpClient;
import com.zlm.hp.http.HttpClient;
import com.zlm.hp.http.HttpReturnResult;
import com.zlm.hp.util.CodeLineUtil;
import com.zlm.hp.util.FileUtil;
import com.zlm.hp.util.TimeUtil;
import com.zlm.hp.util.ZLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Description: 酷狗和酷我网络请求
 * @author: zhangliangming
 * @date: 2018-07-29 16:40
 **/

public class KuGouAndKuWoHttpClient extends APIHttpClient {
    /**
     * 搜索-歌曲
     */
    private final String SEARCH_TYPE_SONG = "song";

    /**
     * 搜索-歌单
     */
    private final String SEARCH_TYPE_SPECIAL = "special";

    /**
     * 搜索-专辑
     */
    private final String SEARCH_TYPE_ALBUM = "album";


    public KuGouAndKuWoHttpClient() {

    }

    /**
     * 搜索-歌曲
     *
     * @param context
     * @param keyword
     * @param page
     * @param pagesize
     * @param askWifi
     * @return
     */
    @Override
    public HttpReturnResult searchSongList(Context context, String keyword, int page, int pagesize, boolean askWifi) {
        HttpReturnResult httpReturnResult = search(context, SEARCH_TYPE_SONG, keyword, page, pagesize, askWifi);
        if (httpReturnResult.isSuccessful()) {
            String result = (String) httpReturnResult.getResult();
            JSONObject jsonNode = null;
            try {
                jsonNode = new JSONObject(result);

                int status = jsonNode.optInt("status", 0);
                if (status == 1) {

                    Map<String, Object> returnResult = new HashMap<String, Object>();
                    JSONObject dataJsonNode = jsonNode.getJSONObject("data");
                    returnResult.put("total", dataJsonNode.optInt("total", 0));
                    JSONArray infoJsonNode = dataJsonNode.getJSONArray("info");
                    List<AudioInfo> lists = new ArrayList<AudioInfo>();
                    for (int i = 0; i < infoJsonNode.length(); i++) {

                        JSONObject infoDataNode = infoJsonNode.getJSONObject(i);

                        AudioInfo audioInfo = new AudioInfo();
                        audioInfo.setHash(infoDataNode.optString("hash", "").toLowerCase());
                        audioInfo.setMvHash(infoDataNode.optString("mvhash", "").toLowerCase());
                        audioInfo.setDuration(infoDataNode.optInt("duration", 0) * 1000);
                        audioInfo.setDurationText(TimeUtil.parseTimeToAudioString((int) audioInfo.getDuration()));
                        audioInfo.setType(AudioInfo.TYPE_NET);
                        audioInfo.setStatus(AudioInfo.STATUS_INIT);
                        audioInfo.setFileExt(infoDataNode.optString("extname", ""));
                        audioInfo.setFileSize(infoDataNode.optLong("filesize", 0));
                        audioInfo.setFileSizeText(FileUtil.getFileSize(audioInfo.getFileSize()));
                        String singerName = infoDataNode.optString("singername", "");
                        audioInfo.setSingerName(singerName.equals("") ? "未知" : singerName);
                        audioInfo.setSongName(infoDataNode.optString("songname", ""));

                        lists.add(audioInfo);
                    }
                    returnResult.put("rows", lists);
                    httpReturnResult.setResult(returnResult);

                    return httpReturnResult;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            httpReturnResult.setStatus(HttpReturnResult.STATUS_ERROR_PARSE);
        }
        return httpReturnResult;
    }

    /**
     * 搜索-歌单
     *
     * @param context
     * @param keyword
     * @param page
     * @param pagesize
     * @param askWifi
     * @return
     */
    @Override
    public HttpReturnResult searchSpecialList(Context context, String keyword, int page, int pagesize, boolean askWifi) {
        HttpReturnResult httpReturnResult = search(context, SEARCH_TYPE_SPECIAL, keyword, page, pagesize, askWifi);
        if (httpReturnResult.isSuccessful()) {
            String result = (String) httpReturnResult.getResult();
            JSONObject jsonNode = null;
            try {
                jsonNode = new JSONObject(result);

                int status = jsonNode.optInt("status", 0);
                if (status == 1) {

                    Map<String, Object> returnResult = new HashMap<String, Object>();
                    JSONObject dataJsonNode = jsonNode.getJSONObject("data");
                    returnResult.put("total", dataJsonNode.optInt("total", 0));
                    JSONArray infoJsonNode = dataJsonNode.getJSONArray("info");
                    List<SpecialInfo> lists = new ArrayList<SpecialInfo>();
                    for (int i = 0; i < infoJsonNode.length(); i++) {

                        JSONObject infoDataNode = infoJsonNode.getJSONObject(i);
                        SpecialInfo specialInfo = new SpecialInfo();

                        specialInfo.setSpecialId(infoDataNode.optString("specialid", ""));
                        specialInfo.setSpecialName(infoDataNode.optString("specialname", ""));
                        specialInfo.setImageUrl(infoDataNode.optString("imgurl", "")
                                .replace("{size}", "400"));

                        lists.add(specialInfo);
                    }
                    returnResult.put("rows", lists);
                    httpReturnResult.setResult(returnResult);

                    return httpReturnResult;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            httpReturnResult.setStatus(HttpReturnResult.STATUS_ERROR_PARSE);
        }
        return httpReturnResult;
    }

    /**
     * 搜索-专辑
     *
     * @param context
     * @param keyword
     * @param page
     * @param pagesize
     * @param askWifi
     * @return
     */
    @Override
    public HttpReturnResult searchAlbumList(Context context, String keyword, int page, int pagesize, boolean askWifi) {
        HttpReturnResult httpReturnResult = search(context, SEARCH_TYPE_ALBUM, keyword, page, pagesize, askWifi);
        if (httpReturnResult.isSuccessful()) {
            String result = (String) httpReturnResult.getResult();
            JSONObject jsonNode = null;
            try {
                jsonNode = new JSONObject(result);

                int status = jsonNode.optInt("status", 0);
                if (status == 1) {

                    Map<String, Object> returnResult = new HashMap<String, Object>();
                    JSONObject dataJsonNode = jsonNode.getJSONObject("data");
                    returnResult.put("total", dataJsonNode.optInt("total", 0));
                    JSONArray infoJsonNode = dataJsonNode.getJSONArray("info");
                    List<AlbumInfo> lists = new ArrayList<AlbumInfo>();
                    for (int i = 0; i < infoJsonNode.length(); i++) {

                        JSONObject infoDataNode = infoJsonNode.getJSONObject(i);
                        AlbumInfo albumInfo = new AlbumInfo();

                        albumInfo.setAlbumId(infoDataNode.optString("albumid", ""));
                        albumInfo.setAlbumName(infoDataNode.optString("albumname", ""));
                        albumInfo.setImageUrl(infoDataNode.optString("imgurl", "")
                                .replace("{size}", "400"));

                        lists.add(albumInfo);
                    }
                    returnResult.put("rows", lists);
                    httpReturnResult.setResult(returnResult);

                    return httpReturnResult;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            httpReturnResult.setStatus(HttpReturnResult.STATUS_ERROR_PARSE);
        }
        return httpReturnResult;
    }

    /**
     * 搜索
     *
     * @param context
     * @param searchType 搜索类型：歌曲、专辑、歌单
     * @param keyword
     * @param page
     * @param pagesize
     * @param askWifi    是否需要是wifi环境
     * @return
     */
    private HttpReturnResult search(Context context, String searchType, String keyword, int page, int pagesize, boolean askWifi) {
        HttpReturnResult httpReturnResult = checkNetWork(context, askWifi);
        if (httpReturnResult != null) return httpReturnResult;
        httpReturnResult = new HttpReturnResult();

        String url = "http://mobilecdn.kugou.com/api/v3/search/" + searchType;
        Map<String, String> params = new HashMap<String, String>();
        params.put("format", "json");
        params.put("keyword", keyword);
        params.put("page", page + "");
        params.put("pagesize", pagesize + "");
        HttpClient.Result result = new HttpClient().get(url, null, params);
        httpReturnResult.setStatus(result.getHttpCode());

        if (result.isSuccessful()) {
            String dataResult = result.getDataString();
            ZLog.d(new CodeLineUtil().getCodeLineInfo(), "search result ->" + dataResult);
            httpReturnResult.setResult(dataResult);
        }
        return httpReturnResult;
    }

    /**
     * 获取歌曲详情
     *
     * @param hash
     * @param audioInfo
     * @return
     */
    @Override
    public Object getSongInfo(Context context, String hash, AudioInfo audioInfo, boolean askWifi) {
        HttpReturnResult httpReturnResult = checkNetWork(context, askWifi);
        if (httpReturnResult != null) return httpReturnResult;

        String downloadUrl = null;
        try {

            String url = "http://m.kugou.com/app/i/getSongInfo.php";
            Map<String, String> params = new HashMap<String, String>();
            params.put("cmd", "playInfo");
            params.put("hash", hash);
            // 获取数据
            HttpClient.Result result = new HttpClient().get(url, null, params);

            if (result.isSuccessful()) {

                JSONObject jsonNode = new JSONObject(result.getDataString());
                int status = jsonNode.optInt("status", 0);
                if (status == 1) {
                    downloadUrl = jsonNode.optString("url", "");
                    if (audioInfo != null) {

                        audioInfo.setFileSizeText(FileUtil.getFileSize(audioInfo.getFileSize()));
                        audioInfo.setAlbumId(jsonNode.optString("albumid", ""));
                        audioInfo.setSingerId(jsonNode.optString("singerId", ""));
                        audioInfo.setImageUrl(jsonNode.optString("album_img", "")
                                .replace("{size}", "400"));
                        audioInfo.setDownloadUrl(downloadUrl);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ZLog.d(new CodeLineUtil().getCodeLineInfo(), "audio downloadurl ->" + downloadUrl);
        return downloadUrl;
    }

    /**
     * @throws
     * @Description: 音乐新歌榜
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-07-29 22:13
     */
    @Override
    public HttpReturnResult lastSongList(Context context, boolean askWifi) {
        HttpReturnResult httpReturnResult = checkNetWork(context, askWifi);
        if (httpReturnResult != null) return httpReturnResult;
        httpReturnResult = new HttpReturnResult();

        String url = "http://m.kugou.com";
        Map<String, String> params = new HashMap<String, String>();
        params.put("json", "true");
        HttpClient.Result result = new HttpClient().get(url, null, params);
        httpReturnResult.setStatus(result.getHttpCode());

        if (result.isSuccessful()) {

            String dataResult = result.getDataString();

            ZLog.d(new CodeLineUtil().getCodeLineInfo(), "lastSongList result ->" + dataResult);
            JSONObject jsonNode = null;
            try {
                jsonNode = new JSONObject(dataResult);

                Map<String, Object> returnResult = new HashMap<String, Object>();
                JSONArray dataJsonNode = jsonNode.getJSONArray("data");
                List<AudioInfo> lists = new ArrayList<AudioInfo>();
                for (int i = 0; i < dataJsonNode.length(); i++) {

                    JSONObject dataDataNode = dataJsonNode.getJSONObject(i);

                    AudioInfo audioInfo = new AudioInfo();
                    audioInfo.setHash(dataDataNode.optString("hash", "").toLowerCase());
                    audioInfo.setMvHash(dataDataNode.optString("mvhash", "").toLowerCase());
                    audioInfo.setDuration(dataDataNode.optInt("duration", 0) * 1000);
                    audioInfo.setDurationText(TimeUtil.parseTimeToAudioString((int) audioInfo.getDuration()));
                    audioInfo.setType(AudioInfo.TYPE_NET);
                    audioInfo.setStatus(AudioInfo.STATUS_INIT);
                    audioInfo.setFileExt(dataDataNode.optString("extname", ""));
                    audioInfo.setFileSize(dataDataNode.optLong("filesize", 0));
                    audioInfo.setFileSizeText(FileUtil.getFileSize(audioInfo.getFileSize()));

                    //分割文件名获取歌手名称和歌曲名称
                    String fileName = dataDataNode.optString("filename", "");
                    String regex = "\\s*-\\s*";
                    String[] temps = fileName.split(regex);
                    String singerName = "";
                    String songName = "";
                    if (temps.length >= 2) {
                        //去掉首尾空格
                        singerName = fileName.split(regex)[0].trim();
                        songName = fileName.split(regex)[1].trim();
                    } else {
                        songName = fileName;
                    }
                    audioInfo.setSingerName(singerName.equals("") ? "未知" : singerName);
                    audioInfo.setSongName(songName);


                    lists.add(audioInfo);
                }
                returnResult.put("rows", lists);
                httpReturnResult.setResult(returnResult);

                return httpReturnResult;

            } catch (JSONException e) {
                e.printStackTrace();
            }
            httpReturnResult.setStatus(HttpReturnResult.STATUS_ERROR_PARSE);

        }
        return httpReturnResult;
    }

    /**
     * 排行榜
     *
     * @param context
     * @param askWifi
     * @return
     */
    @Override
    public HttpReturnResult rankList(Context context, boolean askWifi) {
        HttpReturnResult httpReturnResult = checkNetWork(context, askWifi);
        if (httpReturnResult != null) return httpReturnResult;
        httpReturnResult = new HttpReturnResult();

        String url = "http://m.kugou.com/rank/list";
        Map<String, String> params = new HashMap<String, String>();
        params.put("json", "true");
        HttpClient.Result result = new HttpClient().get(url, null, params);
        httpReturnResult.setStatus(result.getHttpCode());

        if (result.isSuccessful()) {

            String dataResult = result.getDataString();

            ZLog.d(new CodeLineUtil().getCodeLineInfo(), "rankList result ->" + dataResult);
            JSONObject jsonNode = null;
            try {
                jsonNode = new JSONObject(dataResult);

                Map<String, Object> returnResult = new HashMap<String, Object>();
                JSONObject rankJsonNode = jsonNode.getJSONObject("rank");
                returnResult.put("total", rankJsonNode.optInt("total", 0));
                JSONArray listJsonNode = rankJsonNode.getJSONArray("list");
                List<RankInfo> lists = new ArrayList<RankInfo>();
                for (int i = 0; i < listJsonNode.length(); i++) {

                    JSONObject dataDataNode = listJsonNode.getJSONObject(i);

                    RankInfo rankInfo = new RankInfo();
                    rankInfo.setRankId(dataDataNode.optString("rankid", ""));
                    rankInfo.setRankName(dataDataNode.optString("rankname", ""));
                    rankInfo.setImageUrl(dataDataNode.optString("imgurl", "")
                            .replace("{size}", "400"));

                    lists.add(rankInfo);
                }
                returnResult.put("rows", lists);
                httpReturnResult.setResult(returnResult);

                return httpReturnResult;

            } catch (JSONException e) {
                e.printStackTrace();
            }
            httpReturnResult.setStatus(HttpReturnResult.STATUS_ERROR_PARSE);

        }
        return httpReturnResult;
    }

    /**
     * 排行榜/歌曲列表
     *
     * @param context
     * @param askWifi
     * @return
     */
    @Override
    public HttpReturnResult rankSongList(Context context, String rankid, String rankType, int page, int pagesize, boolean askWifi) {
        HttpReturnResult httpReturnResult = checkNetWork(context, askWifi);
        if (httpReturnResult != null) return httpReturnResult;
        httpReturnResult = new HttpReturnResult();

        String url = "http://mobilecdn.kugou.com/api/v3/rank/song";
        Map<String, String> params = new HashMap<String, String>();
        params.put("plat", "0");
        params.put("version", "8352");
        params.put("with_res_tag", "1");
        params.put("ranktype", rankType);
        params.put("rankid", rankid);
        params.put("page", page + "");
        params.put("pagesize", pagesize + "");

//        String url = "http://m.kugou.com/rank/info";
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("rankid", rankid);
//        params.put("ranktype", rankType);
//        params.put("json", "true");
//        params.put("page", page + "");
//        params.put("pagesize", pagesize + "");

        HttpClient.Result result = new HttpClient().get(url, null, params);
        httpReturnResult.setStatus(result.getHttpCode());

        if (result.isSuccessful()) {

            String dataResult = result.getDataString();


            //new
            dataResult = dataResult.substring(dataResult.indexOf("{"),
                    dataResult.lastIndexOf("}") + 1);


            ZLog.d(new CodeLineUtil().getCodeLineInfo(), "rankSongList result ->" + dataResult);
            JSONObject jsonNode = null;
            try {
                jsonNode = new JSONObject(dataResult);

                Map<String, Object> returnResult = new HashMap<String, Object>();
                //new
                JSONObject songsJsonNode = jsonNode.getJSONObject("data");
                //JSONObject songsJsonNode = jsonNode.getJSONObject("songs");
                returnResult.put("total", songsJsonNode.optInt("total", 0));
                //JSONArray listJsonNode = songsJsonNode.getJSONArray("list");

                //new
                JSONArray listJsonNode = songsJsonNode.getJSONArray("info");
                List<AudioInfo> lists = new ArrayList<AudioInfo>();
                for (int i = 0; i < listJsonNode.length(); i++) {

                    JSONObject dataDataNode = listJsonNode.getJSONObject(i);

                    AudioInfo audioInfo = new AudioInfo();
                    audioInfo.setHash(dataDataNode.optString("hash", "").toLowerCase());
                    audioInfo.setMvHash(dataDataNode.optString("mvhash", "").toLowerCase());
                    audioInfo.setDuration(dataDataNode.optInt("duration", 0) * 1000);
                    audioInfo.setDurationText(TimeUtil.parseTimeToAudioString((int) audioInfo.getDuration()));
                    audioInfo.setType(AudioInfo.TYPE_NET);
                    audioInfo.setStatus(AudioInfo.STATUS_INIT);
                    audioInfo.setFileExt(dataDataNode.optString("extname", ""));
                    audioInfo.setFileSize(dataDataNode.optLong("filesize", 0));
                    audioInfo.setFileSizeText(FileUtil.getFileSize(audioInfo.getFileSize()));

                    //分割文件名获取歌手名称和歌曲名称
                    String fileName = dataDataNode.optString("filename", "");
                    String regex = "\\s*-\\s*";
                    String[] temps = fileName.split(regex);
                    String singerName = "";
                    String songName = "";
                    if (temps.length >= 2) {
                        //去掉首尾空格
                        singerName = fileName.split(regex)[0].trim();
                        songName = fileName.split(regex)[1].trim();
                    } else {
                        songName = fileName;
                    }
                    audioInfo.setSingerName(singerName.equals("") ? "未知" : singerName);
                    audioInfo.setSongName(songName);


                    lists.add(audioInfo);
                }
                returnResult.put("rows", lists);
                httpReturnResult.setResult(returnResult);

                return httpReturnResult;

            } catch (JSONException e) {
                e.printStackTrace();
            }
            httpReturnResult.setStatus(HttpReturnResult.STATUS_ERROR_PARSE);

        }
        return httpReturnResult;
    }

    /**
     * 音乐.歌单
     *
     * @param context
     * @param askWifi
     * @return
     */
    @Override
    public HttpReturnResult specialList(Context context, int page, int pagesize, boolean askWifi) {
        HttpReturnResult httpReturnResult = checkNetWork(context, askWifi);
        if (httpReturnResult != null) return httpReturnResult;
        httpReturnResult = new HttpReturnResult();

        String url = "http://m.kugou.com/plist/index";
        Map<String, String> params = new HashMap<String, String>();
        params.put("json", "true");
        params.put("page", page + "");
        params.put("pagesize", pagesize + "");
        HttpClient.Result result = new HttpClient().get(url, null, params);
        httpReturnResult.setStatus(result.getHttpCode());

        if (result.isSuccessful()) {

            String dataResult = result.getDataString();

            ZLog.d(new CodeLineUtil().getCodeLineInfo(), "songList result ->" + dataResult);
            JSONObject jsonNode = null;
            try {
                jsonNode = new JSONObject(dataResult);

                Map<String, Object> returnResult = new HashMap<String, Object>();
                JSONObject plistJsonNode = jsonNode.getJSONObject("plist");
                JSONObject listJsonNode = plistJsonNode.getJSONObject("list");
                returnResult.put("total", listJsonNode.optInt("total", 0));
                JSONArray infoJsonNode = listJsonNode.getJSONArray("info");
                List<SpecialInfo> lists = new ArrayList<SpecialInfo>();
                for (int i = 0; i < infoJsonNode.length(); i++) {

                    JSONObject dataDataNode = infoJsonNode.getJSONObject(i);

                    SpecialInfo specialInfo = new SpecialInfo();
                    specialInfo.setSpecialId(dataDataNode.optString("specialid", ""));
                    specialInfo.setSpecialName(dataDataNode.optString("specialname", ""));
                    specialInfo.setImageUrl(dataDataNode.optString("imgurl", "")
                            .replace("{size}", "400"));

                    lists.add(specialInfo);
                }
                returnResult.put("rows", lists);
                httpReturnResult.setResult(returnResult);

                return httpReturnResult;

            } catch (JSONException e) {
                e.printStackTrace();
            }
            httpReturnResult.setStatus(HttpReturnResult.STATUS_ERROR_PARSE);

        }
        return httpReturnResult;
    }

    /**
     * 音乐.歌单/歌曲列表
     *
     * @param context
     * @param askWifi
     * @return
     */
    @Override
    public HttpReturnResult specialSongList(Context context, String specialid, int page, int pagesize, boolean askWifi) {
        HttpReturnResult httpReturnResult = checkNetWork(context, askWifi);
        if (httpReturnResult != null) return httpReturnResult;
        httpReturnResult = new HttpReturnResult();

        String url = "http://m.kugou.com/plist/list/" + specialid;
        Map<String, String> params = new HashMap<String, String>();
        params.put("json", "true");
        params.put("page", page + "");
        params.put("pagesize", pagesize + "");
        HttpClient.Result result = new HttpClient().get(url, null, params);
        httpReturnResult.setStatus(result.getHttpCode());

        if (result.isSuccessful()) {

            String dataResult = result.getDataString();

            ZLog.d(new CodeLineUtil().getCodeLineInfo(), "specialSongList result ->" + dataResult);
            JSONObject jsonNode = null;
            try {
                jsonNode = new JSONObject(dataResult);

                Map<String, Object> returnResult = new HashMap<String, Object>();
                JSONObject plistJsonNode = jsonNode.getJSONObject("list");
                JSONObject listJsonNode = plistJsonNode.getJSONObject("list");
                returnResult.put("total", listJsonNode.optInt("total", 0));
                JSONArray infoJsonNode = listJsonNode.getJSONArray("info");
                List<AudioInfo> lists = new ArrayList<AudioInfo>();
                for (int i = 0; i < infoJsonNode.length(); i++) {

                    JSONObject dataDataNode = infoJsonNode.getJSONObject(i);

                    AudioInfo audioInfo = new AudioInfo();
                    audioInfo.setHash(dataDataNode.optString("hash", "").toLowerCase());
                    audioInfo.setMvHash(dataDataNode.optString("mvhash", "").toLowerCase());
                    audioInfo.setDuration(dataDataNode.optInt("duration", 0) * 1000);
                    audioInfo.setDurationText(TimeUtil.parseTimeToAudioString((int) audioInfo.getDuration()));
                    audioInfo.setType(AudioInfo.TYPE_NET);
                    audioInfo.setStatus(AudioInfo.STATUS_INIT);
                    audioInfo.setFileExt(dataDataNode.optString("extname", ""));
                    audioInfo.setFileSize(dataDataNode.optLong("filesize", 0));
                    audioInfo.setFileSizeText(FileUtil.getFileSize(audioInfo.getFileSize()));

                    //分割文件名获取歌手名称和歌曲名称
                    String fileName = dataDataNode.optString("filename", "");
                    String regex = "\\s*-\\s*";
                    String[] temps = fileName.split(regex);
                    String singerName = "";
                    String songName = "";
                    if (temps.length >= 2) {
                        //去掉首尾空格
                        singerName = fileName.split(regex)[0].trim();
                        songName = fileName.split(regex)[1].trim();
                    } else {
                        songName = fileName;
                    }
                    audioInfo.setSingerName(singerName.equals("") ? "未知" : singerName);
                    audioInfo.setSongName(songName);

                    lists.add(audioInfo);
                }
                returnResult.put("rows", lists);
                httpReturnResult.setResult(returnResult);

                return httpReturnResult;

            } catch (JSONException e) {
                e.printStackTrace();
            }
            httpReturnResult.setStatus(HttpReturnResult.STATUS_ERROR_PARSE);

        }
        return httpReturnResult;
    }

    /**
     * 歌手分类
     *
     * @param context
     * @param askWifi
     * @return
     */
    @Override
    public HttpReturnResult singerClassList(Context context, boolean askWifi) {
        HttpReturnResult httpReturnResult = checkNetWork(context, askWifi);
        if (httpReturnResult != null) return httpReturnResult;
        httpReturnResult = new HttpReturnResult();

        String url = "http://m.kugou.com/singer/class";
        Map<String, String> params = new HashMap<String, String>();
        params.put("json", "true");
        HttpClient.Result result = new HttpClient().get(url, null, params);
        httpReturnResult.setStatus(result.getHttpCode());

        if (result.isSuccessful()) {

            String dataResult = result.getDataString();

            ZLog.d(new CodeLineUtil().getCodeLineInfo(), "singerClass result ->" + dataResult);
            JSONObject jsonNode = null;
            try {
                jsonNode = new JSONObject(dataResult);

                Map<String, Object> returnResult = new HashMap<String, Object>();
                JSONArray listJsonNode = jsonNode.getJSONArray("list");
                List<SingerInfo> lists = new ArrayList<SingerInfo>();
                for (int i = 0; i < listJsonNode.length(); i++) {

                    JSONObject dataDataNode = listJsonNode.getJSONObject(i);

                    SingerInfo singerInfo = new SingerInfo();
                    singerInfo.setClassId(dataDataNode.optString("classid", ""));
                    singerInfo.setClassName(dataDataNode.optString("classname", ""));
                    singerInfo.setImageUrl(dataDataNode.optString("imgurl", "")
                            .replace("{size}", "400"));

                    lists.add(singerInfo);
                }
                returnResult.put("rows", lists);
                httpReturnResult.setResult(returnResult);

                return httpReturnResult;

            } catch (JSONException e) {
                e.printStackTrace();
            }
            httpReturnResult.setStatus(HttpReturnResult.STATUS_ERROR_PARSE);

        }
        return httpReturnResult;
    }

    /**
     * 歌手分类/歌手列表
     *
     * @param context
     * @param askWifi
     * @return
     */
    @Override
    public HttpReturnResult singeList(Context context, String classid, int page, int pagesize, boolean askWifi) {
        HttpReturnResult httpReturnResult = checkNetWork(context, askWifi);
        if (httpReturnResult != null) return httpReturnResult;
        httpReturnResult = new HttpReturnResult();

        String url = "http://m.kugou.com/singer/list/" + classid;
        Map<String, String> params = new HashMap<String, String>();
        params.put("json", "true");
        params.put("page", page + "");
        params.put("pagesize", pagesize + "");
        HttpClient.Result result = new HttpClient().get(url, null, params);
        httpReturnResult.setStatus(result.getHttpCode());

        if (result.isSuccessful()) {

            String dataResult = result.getDataString();

            ZLog.d(new CodeLineUtil().getCodeLineInfo(), "singeList result ->" + dataResult);
            JSONObject jsonNode = null;
            try {
                jsonNode = new JSONObject(dataResult);

                Map<String, Object> returnResult = new HashMap<String, Object>();
                JSONObject plistJsonNode = jsonNode.getJSONObject("singers");
                JSONObject listJsonNode = plistJsonNode.getJSONObject("list");
                returnResult.put("total", listJsonNode.optInt("total", 0));
                JSONArray infoJsonNode = listJsonNode.getJSONArray("info");
                List<SingerInfo> lists = new ArrayList<SingerInfo>();
                for (int i = 0; i < infoJsonNode.length(); i++) {

                    JSONObject dataDataNode = infoJsonNode.getJSONObject(i);

                    SingerInfo singerInfo = new SingerInfo();
                    singerInfo.setSingerId(dataDataNode.optString("singerid", ""));
                    singerInfo.setSingerName(dataDataNode.optString("singername", ""));
                    singerInfo.setImageUrl(dataDataNode.optString("imgurl", "")
                            .replace("{size}", "400"));

                    lists.add(singerInfo);
                }
                returnResult.put("rows", lists);
                httpReturnResult.setResult(returnResult);

                return httpReturnResult;

            } catch (JSONException e) {
                e.printStackTrace();
            }
            httpReturnResult.setStatus(HttpReturnResult.STATUS_ERROR_PARSE);

        }
        return httpReturnResult;
    }

    /**
     * 歌手分类/歌手列表/歌曲列表
     *
     * @param context
     * @param askWifi
     * @return
     */
    @Override
    public HttpReturnResult singeSongList(Context context, String singerid, int page, int pagesize, boolean askWifi) {
        HttpReturnResult httpReturnResult = checkNetWork(context, askWifi);
        if (httpReturnResult != null) return httpReturnResult;
        httpReturnResult = new HttpReturnResult();

        String url = "http://mobilecdn.kugou.com/api/v3/singer/song";
        Map<String, String> params = new HashMap<String, String>();
        params.put("json", "true");
        params.put("singerid", singerid);
        params.put("page", page + "");
        params.put("pagesize", pagesize + "");
        HttpClient.Result result = new HttpClient().get(url, null, params);
        httpReturnResult.setStatus(result.getHttpCode());

        if (result.isSuccessful()) {

            String dataResult = result.getDataString();

            ZLog.d(new CodeLineUtil().getCodeLineInfo(), "singeSongList result ->" + dataResult);
            JSONObject jsonNode = null;
            try {
                jsonNode = new JSONObject(dataResult);

                Map<String, Object> returnResult = new HashMap<String, Object>();
                JSONObject dataJsonNode = jsonNode.getJSONObject("data");
                returnResult.put("total", dataJsonNode.optInt("total", 0));
                JSONArray infoJsonNode = dataJsonNode.getJSONArray("info");
                List<AudioInfo> lists = new ArrayList<AudioInfo>();
                for (int i = 0; i < infoJsonNode.length(); i++) {

                    JSONObject infoDataNode = infoJsonNode.getJSONObject(i);

                    AudioInfo audioInfo = new AudioInfo();
                    audioInfo.setHash(infoDataNode.optString("hash", "").toLowerCase());
                    audioInfo.setMvHash(infoDataNode.optString("mvhash", "").toLowerCase());
                    audioInfo.setDuration(infoDataNode.optInt("duration", 0) * 1000);
                    audioInfo.setDurationText(TimeUtil.parseTimeToAudioString((int) audioInfo.getDuration()));
                    audioInfo.setType(AudioInfo.TYPE_NET);
                    audioInfo.setStatus(AudioInfo.STATUS_INIT);
                    audioInfo.setFileExt(infoDataNode.optString("extname", ""));
                    audioInfo.setFileSize(infoDataNode.optLong("filesize", 0));
                    audioInfo.setFileSizeText(FileUtil.getFileSize(audioInfo.getFileSize()));

                    //分割文件名获取歌手名称和歌曲名称
                    String fileName = infoDataNode.optString("filename", "");
                    String regex = "\\s*-\\s*";
                    String[] temps = fileName.split(regex);
                    String singerName = "";
                    String songName = "";
                    if (temps.length >= 2) {
                        //去掉首尾空格
                        singerName = fileName.split(regex)[0].trim();
                        songName = fileName.split(regex)[1].trim();
                    } else {
                        songName = fileName;
                    }
                    audioInfo.setSingerName(singerName.equals("") ? "未知" : singerName);
                    audioInfo.setSongName(songName);

                    lists.add(audioInfo);
                }
                returnResult.put("rows", lists);
                httpReturnResult.setResult(returnResult);

                return httpReturnResult;


            } catch (JSONException e) {
                e.printStackTrace();
            }
            httpReturnResult.setStatus(HttpReturnResult.STATUS_ERROR_PARSE);

        }
        return httpReturnResult;
    }

    /**
     * 热门搜索
     *
     * @param context
     * @param askWifi
     * @return
     */
    @Override
    public HttpReturnResult searchHotList(Context context, int page, int pagesize, boolean askWifi) {
        HttpReturnResult httpReturnResult = checkNetWork(context, askWifi);
        if (httpReturnResult != null) return httpReturnResult;
        httpReturnResult = new HttpReturnResult();

        String url = "http://mobilecdn.kugou.com/api/v3/search/hot";
        Map<String, String> params = new HashMap<String, String>();
        params.put("json", "true");
        params.put("plat", page + "");
        params.put("count", pagesize + "");
        HttpClient.Result result = new HttpClient().get(url, null, params);
        httpReturnResult.setStatus(result.getHttpCode());

        if (result.isSuccessful()) {

            String dataResult = result.getDataString();

            ZLog.d(new CodeLineUtil().getCodeLineInfo(), "searchHotList result ->" + dataResult);
            JSONObject jsonNode = null;
            try {
                jsonNode = new JSONObject(dataResult);

                Map<String, Object> returnResult = new HashMap<String, Object>();
                JSONObject dataJsonNode = jsonNode.getJSONObject("data");
                JSONArray infoJsonNode = dataJsonNode.getJSONArray("info");
                List<AudioInfo> lists = new ArrayList<AudioInfo>();
                for (int i = 0; i < infoJsonNode.length(); i++) {

                    JSONObject infoDataNode = infoJsonNode.getJSONObject(i);

                    AudioInfo audioInfo = new AudioInfo();
                    audioInfo.setKeyword(infoDataNode.optString("keyword", ""));

                    lists.add(audioInfo);
                }
                returnResult.put("rows", lists);
                httpReturnResult.setResult(returnResult);

                return httpReturnResult;


            } catch (JSONException e) {
                e.printStackTrace();
            }
            httpReturnResult.setStatus(HttpReturnResult.STATUS_ERROR_PARSE);

        }
        return httpReturnResult;
    }

    /**
     * 搜索-mv
     *
     * @param context
     * @param keyword
     * @param page
     * @param pagesize
     * @param askWifi
     * @return
     */
    @Override
    public HttpReturnResult searchMVList(Context context, String keyword, int page, int pagesize, boolean askWifi) {
        HttpReturnResult httpReturnResult = checkNetWork(context, askWifi);
        if (httpReturnResult != null) return httpReturnResult;
        httpReturnResult = new HttpReturnResult();

        String url = "http://mvsearch.kugou.com/mv_search";
        Map<String, String> params = new HashMap<String, String>();
        params.put("json", "true");
        params.put("keyword", keyword);
        params.put("plat", page + "");
        params.put("count", pagesize + "");
        HttpClient.Result result = new HttpClient().get(url, null, params);
        httpReturnResult.setStatus(result.getHttpCode());

        if (result.isSuccessful()) {

            String dataResult = result.getDataString();

            ZLog.d(new CodeLineUtil().getCodeLineInfo(), "searchMV result ->" + dataResult);
            JSONObject jsonNode = null;
            try {
                jsonNode = new JSONObject(dataResult);

                Map<String, Object> returnResult = new HashMap<String, Object>();
                JSONObject dataJsonNode = jsonNode.getJSONObject("data");
                returnResult.put("total", dataJsonNode.optInt("total", 0));
                JSONArray infoJsonNode = dataJsonNode.getJSONArray("lists");
                List<VideoInfo> lists = new ArrayList<VideoInfo>();
                for (int i = 0; i < infoJsonNode.length(); i++) {

                    JSONObject infoDataNode = infoJsonNode.getJSONObject(i);

                    VideoInfo videoInfo = new VideoInfo();
                    videoInfo.setHash(infoDataNode.optString("MvHash", ""));

                    getMVInfo(context, videoInfo.getHash(), videoInfo, false);
                    lists.add(videoInfo);
                }
                returnResult.put("rows", lists);
                httpReturnResult.setResult(returnResult);

                return httpReturnResult;


            } catch (JSONException e) {
                e.printStackTrace();
            }
            httpReturnResult.setStatus(HttpReturnResult.STATUS_ERROR_PARSE);

        }
        return httpReturnResult;
    }

    /**
     * 获取mv详情
     *
     * @param hash
     * @param videoInfo
     * @return
     */
    @Override
    public Object getMVInfo(Context context, String hash, VideoInfo videoInfo, boolean askWifi) {
        HttpReturnResult httpReturnResult = checkNetWork(context, askWifi);
        if (httpReturnResult != null) return httpReturnResult;

        String downloadUrl = null;
        try {
            String url = "http://m.kugou.com/app/i/mv.php";
            Map<String, String> params = new HashMap<String, String>();
            params.put("cmd", "100");
            params.put("hash", hash);
            // 获取数据
            HttpClient.Result result = new HttpClient().get(url, null, params);

            if (result.isSuccessful()) {

                JSONObject jsonNode = new JSONObject(result.getDataString());

                if (videoInfo != null) {

                    String singerName = jsonNode.optString("singer", "");
                    videoInfo.setSingerName(singerName.equals("") ? "未知" : singerName);

                    videoInfo.setMvName(jsonNode.optString("songname", ""));
                    videoInfo.setImageUrl(jsonNode.optString("mvicon", "")
                            .replace("{size}", "400"));
                }

                //获取mv下载路径
                if (jsonNode.has("mvdata")) {
                    JSONObject mvDataObject = jsonNode.getJSONObject("mvdata");
                    Iterator<String> it = mvDataObject.keys();
                    while (it.hasNext()) {
                        // 获得key
                        String key = it.next();
                        JSONObject data = mvDataObject.getJSONObject(key);
                        if (data.has("downurl")) {

                            downloadUrl = data.optString("downurl", "");

                            if (videoInfo != null) {
                                videoInfo.setDownloadUrl(downloadUrl);
                                videoInfo.setFileExt(downloadUrl.substring((downloadUrl.lastIndexOf(".") + 1), downloadUrl.length()));
                                videoInfo.setFileSize(data.optLong("filesize", 0));
                                videoInfo.setFileSizeText(FileUtil.getFileSize(videoInfo.getFileSize()));
                                videoInfo.setDuration(data.optInt("timelength", 0));
                                videoInfo.setDurationText(TimeUtil.parseTimeToVideoString((int) videoInfo.getDuration()));
                            }

                            break;
                        }
                    }
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ZLog.d(new CodeLineUtil().getCodeLineInfo(), "video downloadurl ->" + downloadUrl);
        return downloadUrl;
    }

    /**
     * 获取歌手头像
     *
     * @param singerName
     * @return
     */
    @Override
    public HttpReturnResult getSingerIcon(Context context, String singerName, boolean askWifi) {
        HttpReturnResult httpReturnResult = checkNetWork(context, askWifi);
        if (httpReturnResult != null) return httpReturnResult;
        httpReturnResult = new HttpReturnResult();

        String url = "http://mobilecdn.kugou.com/new/app/i/yueku.php";
        Map<String, String> params = new HashMap<String, String>();

        params.put("singer", singerName);
        params.put("size", "400");
        params.put("cmd", "104");
        params.put("type", "softhead");
        HttpClient.Result result = new HttpClient().get(url, null, params);
        httpReturnResult.setStatus(result.getHttpCode());

        if (result.isSuccessful()) {

            String dataResult = result.getDataString();

            ZLog.d(new CodeLineUtil().getCodeLineInfo(), "getSingerIcon result ->" + dataResult);
            JSONObject jsonNode = null;
            try {
                jsonNode = new JSONObject(dataResult);

                int status = jsonNode.optInt("status", 0);
                if (status == 1) {

                    SingerInfo singerInfo = new SingerInfo();
                    singerInfo.setSingerName(jsonNode.optString("singer", ""));
                    singerInfo.setImageUrl(jsonNode.optString("url", ""));

                    httpReturnResult.setResult(singerInfo);
                }

                return httpReturnResult;


            } catch (JSONException e) {
                e.printStackTrace();
            }
            httpReturnResult.setStatus(HttpReturnResult.STATUS_ERROR_PARSE);

        }
        return httpReturnResult;
    }

    /**
     * 获取歌手写真图片
     *
     * @param singerName
     * @return
     */
    @Override
    public HttpReturnResult getSingerPicList(Context context, String singerName, boolean askWifi) {
        HttpReturnResult httpReturnResult = checkNetWork(context, askWifi);
        if (httpReturnResult != null) return httpReturnResult;
        httpReturnResult = new HttpReturnResult();

        String url = "http://artistpicserver.kuwo.cn/pic.web";
        Map<String, String> params = new HashMap<String, String>();

        String fromType = "app"; // app/pc
        params.put("type", "big_artist_pic");
        params.put("pictype", "url");
        params.put("content", "list");
        params.put("id", "0");
        params.put("name", singerName);
        params.put("from", fromType);
        params.put("json", "1");
        params.put("version", "1");
        params.put("width", "720");
        params.put("height", "1080");

        HttpClient.Result result = new HttpClient().get(url, null, params);
        httpReturnResult.setStatus(result.getHttpCode());

        if (result.isSuccessful()) {

            String dataResult = result.getDataString();

            ZLog.d(new CodeLineUtil().getCodeLineInfo(), "getSingerPicList result ->" + dataResult);
            JSONObject jsonNode = null;
            try {
                jsonNode = new JSONObject(dataResult);
                Map<String, Object> returnResult = new HashMap<String, Object>();
                List<SingerInfo> lists = new ArrayList<SingerInfo>();
                JSONArray arrayJsonNode = jsonNode.getJSONArray("array");
                if (arrayJsonNode != null) {
                    for (int i = 0; i < arrayJsonNode.length(); i++) {

                        JSONObject arrayInfo = arrayJsonNode.getJSONObject(i);
                        SingerInfo singerInfo = new SingerInfo();
                        String imgUrl = null;
                        if (fromType.equals("app")) {
                            imgUrl = arrayInfo.optString("key", "");
                        } else {
                            if (arrayInfo.has("bkurl")) {
                                imgUrl = arrayInfo.optString("bkurl", "");
                            }
                        }
                        if (TextUtils.isEmpty(imgUrl)) {
                            continue;
                        }

                        singerInfo.setSingerName(singerName);
                        singerInfo.setImageUrl(imgUrl);
                        lists.add(singerInfo);
                    }

                    //排序
                    Collections.sort(lists, new Comparator<SingerInfo>() {

                        @Override
                        public int compare(SingerInfo o1, SingerInfo o2) {
                            return o2.getImageUrl().compareTo(o1.getImageUrl());
                        }
                    });

                    returnResult.put("rows", lists);
                    httpReturnResult.setResult(returnResult);
                }

                return httpReturnResult;


            } catch (JSONException e) {
                e.printStackTrace();
            }
            httpReturnResult.setStatus(HttpReturnResult.STATUS_ERROR_PARSE);

        }
        return httpReturnResult;
    }

    /**
     * 搜索-歌词
     *
     * @param context
     * @param keyword  歌曲名（不为空）：singerName + " - " + songName
     * @param duration 歌曲总时长(毫秒)（不为空）
     * @param hash     歌曲Hash值
     * @param askWifi
     * @return
     */
    @Override
    public HttpReturnResult searchLyricsList(Context context, String keyword, String duration, String hash, boolean askWifi) {
        HttpReturnResult httpReturnResult = checkNetWork(context, askWifi);
        if (httpReturnResult != null) return httpReturnResult;
        httpReturnResult = new HttpReturnResult();

        String url = "http://lyrics.kugou.com/search";
        Map<String, String> params = new HashMap<String, String>();
        params.put("ver", "1");
        params.put("man", "yes");
        params.put("client", "pc");
        params.put("keyword", keyword);
        params.put("duration", duration);
        if (!TextUtils.isEmpty(hash)) {
            params.put("hash", hash);
        }
        HttpClient.Result result = new HttpClient().get(url, null, params);
        httpReturnResult.setStatus(result.getHttpCode());

        if (result.isSuccessful()) {

            String dataResult = result.getDataString();

            ZLog.d(new CodeLineUtil().getCodeLineInfo(), "searchLyricsList result ->" + dataResult);
            JSONObject jsonNode = null;
            try {
                jsonNode = new JSONObject(dataResult);
                Map<String, Object> returnResult = new HashMap<String, Object>();

                int status = jsonNode.optInt("status", 0);
                List<LrcInfo> lists = new ArrayList<LrcInfo>();
                if (status == 200) {
                    JSONArray candidatesNode = jsonNode.getJSONArray("candidates");
                    for (int i = 0; i < candidatesNode.length(); i++) {
                        JSONObject candidateNode = candidatesNode.getJSONObject(i);

                        LrcInfo lrcInfo = new LrcInfo();
                        lrcInfo.setId(candidateNode.optString("id", ""));
                        lrcInfo.setAccesskey(candidateNode.optString(
                                "accesskey", ""));
                        lrcInfo.setDuration(candidateNode
                                .optString("duration", ""));
                        lrcInfo.setSingerName(candidateNode
                                .optString("singer", ""));
                        lrcInfo.setSongName(candidateNode.optString("song", ""));

                        lists.add(lrcInfo);
                    }
                }

                returnResult.put("rows", lists);
                httpReturnResult.setResult(returnResult);

                return httpReturnResult;

            } catch (JSONException e) {
                e.printStackTrace();
            }
            httpReturnResult.setStatus(HttpReturnResult.STATUS_ERROR_PARSE);
        }
        return httpReturnResult;
    }

    /**
     * 获取歌词信息
     *
     * @param context
     * @param id        （不为空）
     * @param accesskey （不为空）
     * @param askWifi
     * @return
     */
    @Override
    public HttpReturnResult getLyricsInfo(Context context, String id, String accesskey, boolean askWifi) {
        HttpReturnResult httpReturnResult = checkNetWork(context, askWifi);
        if (httpReturnResult != null) return httpReturnResult;
        httpReturnResult = new HttpReturnResult();

        String url = "http://lyrics.kugou.com/download";
        Map<String, String> params = new HashMap<String, String>();
        params.put("ver", "1");
        params.put("client", "pc");
        params.put("id", id);
        params.put("accesskey", accesskey);
        params.put("charset", "utf8");
        params.put("fmt", "krc"); //lrc 或 krc

        HttpClient.Result result = new HttpClient().get(url, null, params);
        httpReturnResult.setStatus(result.getHttpCode());

        if (result.isSuccessful()) {

            String dataResult = result.getDataString();

            ZLog.d(new CodeLineUtil().getCodeLineInfo(), "getLyricsInfo result ->" + dataResult);
            JSONObject jsonNode = null;
            try {
                jsonNode = new JSONObject(dataResult);
                int status = jsonNode.optInt("status", 0);
                if (status == 200) {

                    LrcInfo lrcInfo = new LrcInfo();
                    lrcInfo.setCharset("utf8");
                    lrcInfo.setContent(jsonNode.optString("content", ""));
                    lrcInfo.setFmt(jsonNode.optString("fmt", ""));
                    httpReturnResult.setResult(lrcInfo);
                }

                return httpReturnResult;

            } catch (JSONException e) {
                e.printStackTrace();
            }
            httpReturnResult.setStatus(HttpReturnResult.STATUS_ERROR_PARSE);
        }
        return httpReturnResult;
    }

    /**
     * 获取歌词信息
     *
     * @param keyword singerName + " - " + songName
     * @return
     */
    @Override
    public HttpReturnResult getLyricsInfo(Context context, String keyword, String duration, String hash, boolean askWifi) {
        HttpReturnResult httpReturnResult = checkNetWork(context, askWifi);
        if (httpReturnResult != null) return httpReturnResult;
        httpReturnResult = new HttpReturnResult();

        String url = "http://mobilecdn.kugou.com/new/app/i/krc.php";
        Map<String, String> params = new HashMap<String, String>();
        params.put("ver", "1");
        params.put("client", "pc");
        params.put("keyword", keyword);
        params.put("timelength", duration);
        params.put("cmd", "200");
        params.put("hash", hash);

        HttpClient.Result result = new HttpClient().get(url, null, params);
        httpReturnResult.setStatus(result.getHttpCode());

        if (result.isSuccessful() && result.getData() != null && result.getData().length > 1024) {

            LrcInfo lrcInfo = new LrcInfo();
            lrcInfo.setCharset("utf8");
            lrcInfo.setContent(Base64.encodeToString(result.getData(), Base64.NO_WRAP));
            lrcInfo.setFmt("krc");
            httpReturnResult.setResult(lrcInfo);

        }
        return httpReturnResult;
    }
}
