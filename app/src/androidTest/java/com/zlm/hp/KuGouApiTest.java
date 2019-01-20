package com.zlm.hp;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.zlm.hp.http.APIHttpClient;
import com.zlm.hp.util.HttpUtil;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class KuGouApiTest {

    /**
     * @throws
     * @Description: 搜索-歌曲
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-07-29 18:43
     */
    @Test
    public void KuGouApiSearchSong() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        apiHttpClient.searchSongList(appContext, "爱就一个字", 1, 2, true);
    }

    /**
     * @throws
     * @Description: 搜索-歌单
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-07-29 18:43
     */
    @Test
    public void KuGouApiSearchSpecial() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        apiHttpClient.searchSpecialList(appContext, "爱就一个字", 1, 2, true);
    }

    /**
     * @throws
     * @Description: 搜索-专辑
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-07-29 18:43
     */
    @Test
    public void KuGouApiSearchAlbum() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        apiHttpClient.searchAlbumList(appContext, "爱就一个字", 1, 2, true);
    }

    /**
     * @throws
     * @Description: 音乐新歌榜
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-07-29 18:43
     */
    @Test
    public void KuGouApiLastSongList() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        apiHttpClient.lastSongList(appContext, true);
    }

    /**
     * @throws
     * @Description: 排行榜
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-07-29 18:43
     */
    @Test
    public void KuGouApiRankList() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        apiHttpClient.rankList(appContext, true);
    }

    /**
     * @throws
     * @Description: 排行榜/歌曲列表
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-07-29 18:43
     */
    @Test
    public void KuGouApiRankSongList() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        apiHttpClient.rankSongList(appContext, "6666", "",1, 10, true);
    }

    /**
     * @throws
     * @Description: 音乐.歌单
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-07-29 18:43
     */
    @Test
    public void KuGouApiSpecialList() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        apiHttpClient.specialList(appContext, 1, 10, true);
    }

    /**
     * 音乐.歌单/歌曲列表
     *
     * @return
     */
    @Test
    public void KuGouApiSpecialSongList() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        apiHttpClient.specialSongList(appContext, "125032", 1, 10, true);
    }

    /**
     * 歌手分类
     *
     * @return
     */
    @Test
    public void KuGouApiSingerClass() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        apiHttpClient.singerClassList(appContext, true);
    }

    /**
     * 歌手分类/歌手列表
     *
     * @return
     */
    @Test
    public void KuGouApiSingeList() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        apiHttpClient.singeList(appContext, "88", 1, 10, true);
    }

    /**
     * @throws
     * @Description: 歌手分类/歌手列表/歌曲列表
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-07-29 18:43
     */
    @Test
    public void KuGouApiSingeSongList() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        apiHttpClient.singeSongList(appContext, "3060", 1, 10, true);
    }


    /**
     * @throws
     * @Description: 热门搜索
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-07-29 18:43
     */
    @Test
    public void KuGouApiSearchHotList() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        apiHttpClient.searchHotList(appContext, 1, 10, true);
    }

    /**
     * @throws
     * @Description: 搜索-mv
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-07-29 18:43
     */
    @Test
    public void KuGouApiSearchMV() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        apiHttpClient.searchMVList(appContext, "爱就一个字", 1, 10, true);
    }

    /**
     * @throws
     * @Description: 获取歌曲下载地址
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-07-30 22:44
     */
    @Test
    public void KuGouApiGetSongInfo() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        apiHttpClient.getSongInfo(appContext, "CB7EE97F4CC11C4EA7A1FA4B516A5D97", null, true);
    }

    /**
     * @throws
     * @Description: 获取mv歌曲下载地址
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-07-30 22:44
     */
    @Test
    public void KuGouApiGetMVInfo() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        apiHttpClient.getMVInfo(appContext, "5F8393A55D5762A63F1A5E92B46E575E", null, true);
    }

    /**
     * @throws
     * @Description: 获取歌手头像
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-07-30 22:44
     */
    @Test
    public void KuGouApiGetSingerIcon() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        apiHttpClient.getSingerIcon(appContext, "周杰伦", true);
    }

    /**
     * @throws
     * @Description: 获取歌手写真
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-07-30 22:44
     */
    @Test
    public void KuGouApiGetSingerPicList() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        apiHttpClient.getSingerPicList(appContext, "周杰伦", true);
    }

    /**
     * @throws
     * @Description: 搜索-歌词
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-08-01 0:44
     */
    @Test
    public void KuGouApiSearchLyricsList() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        apiHttpClient.searchLyricsList(appContext, "王力宏 - 就是现在", "270000", "", true);
    }

    /**
     * @throws
     * @Description: 获取歌词信息
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-08-01 0:48
     */
    @Test
    public void KuGouApiGetLyricsInfo() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        apiHttpClient.getLyricsInfo(appContext, "16835663", "32BA8996EA9E72C30069713CCD58236D", true);
    }

    /**
     * @throws
     * @Description: 获取歌词信息
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-08-01 0:48
     */
    @Test
    public void KuGouApiGetLyricsInfo2() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        apiHttpClient.getLyricsInfo(appContext, "张杰 - 微笑着胜利【庆祝建军91周年网宣主题曲】", "271000", "37F4B77A2BAD8471BF1C70E4F7667400", true);
    }
}
