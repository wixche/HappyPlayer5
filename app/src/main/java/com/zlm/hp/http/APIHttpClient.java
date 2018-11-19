package com.zlm.hp.http;

import android.content.Context;

import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.entity.VideoInfo;
import com.zlm.hp.util.NetUtil;


/**
 * Created by zhangliangming on 2018-07-29.
 */

public abstract class APIHttpClient {
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
    public abstract HttpReturnResult searchSongList(Context context, String keyword, int page, int pagesize, boolean askWifi);

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
    public abstract HttpReturnResult searchAlbumList(Context context, String keyword, int page, int pagesize, boolean askWifi);

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
    public abstract HttpReturnResult searchSpecialList(Context context, String keyword, int page, int pagesize, boolean askWifi);


    /**
     * 获取歌曲详情：只要获取下载路径、图片等信息
     *
     * @param hash
     * @return
     */
    public abstract Object getSongInfo(Context context, String hash, AudioInfo audioInfo, boolean askWifi);

    /**
     * 音乐新歌榜
     *
     * @param context
     * @param askWifi
     * @return
     */
    public abstract HttpReturnResult lastSongList(Context context, boolean askWifi);

    /**
     * 排行榜
     *
     * @param context
     * @param askWifi
     * @return
     */
    public abstract HttpReturnResult rankList(Context context, boolean askWifi);

    /**
     * 排行榜/歌曲列表
     *
     * @param context
     * @param askWifi
     * @return
     */
    public abstract HttpReturnResult rankSongList(Context context, String rankid, String ranktype, int page, int pagesize, boolean askWifi);


    /**
     * 音乐.歌单
     *
     * @param context
     * @param askWifi
     * @return
     */
    public abstract HttpReturnResult specialList(Context context, int page, int pagesize, boolean askWifi);

    /**
     * 音乐.歌单/歌曲列表
     *
     * @param context
     * @param askWifi
     * @return
     */
    public abstract HttpReturnResult specialSongList(Context context, String specialid, int page, int pagesize, boolean askWifi);


    /**
     * 歌手分类
     *
     * @param context
     * @param askWifi
     * @return
     */
    public abstract HttpReturnResult singerClassList(Context context, boolean askWifi);

    /**
     * 歌手分类/歌手列表
     *
     * @param context
     * @param askWifi
     * @return
     */
    public abstract HttpReturnResult singeList(Context context, String classid, int page, int pagesize, boolean askWifi);


    /**
     * 歌手分类/歌手列表/歌曲列表
     *
     * @param context
     * @param askWifi
     * @return
     */
    public abstract HttpReturnResult singeSongList(Context context, String singerid, int page, int pagesize, boolean askWifi);

    /**
     * 热门搜索
     *
     * @param context
     * @param askWifi
     * @return
     */
    public abstract HttpReturnResult searchHotList(Context context, int page, int pagesize, boolean askWifi);

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
    public abstract HttpReturnResult searchMVList(Context context, String keyword, int page, int pagesize, boolean askWifi);

    /**
     * 获取mv歌曲信息：下载地址、图片等
     *
     * @param hash
     * @return
     */
    public abstract Object getMVInfo(Context context, String hash, VideoInfo videoInfo, boolean askWifi);

    /**
     * 获取歌手头像
     *
     * @param singerName
     * @return
     */
    public abstract HttpReturnResult getSingerIcon(Context context, String singerName, boolean askWifi);

    /**
     * 获取歌手写真图片
     *
     * @param singerName
     * @return
     */
    public abstract HttpReturnResult getSingerPicList(Context context, String singerName,boolean askWifi);

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
    public abstract HttpReturnResult searchLyricsList(Context context, String keyword, String duration, String hash, boolean askWifi);

    /**
     * 获取歌词信息
     *
     * @param context
     * @param id        （不为空）
     * @param accesskey （不为空）
     * @param askWifi
     * @return
     */
    public abstract HttpReturnResult getLyricsInfo(Context context, String id, String accesskey, boolean askWifi);

    /**
     * 获取歌词信息
     *
     * @param keyword singerName + " - " + songName
     * @return
     */
    public abstract HttpReturnResult getLyricsInfo(Context context, String keyword, String duration, String hash, boolean askWifi);

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
