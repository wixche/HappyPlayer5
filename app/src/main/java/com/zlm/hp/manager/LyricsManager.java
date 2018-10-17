package com.zlm.hp.manager;

import android.content.Context;
import android.graphics.Bitmap;

import com.zlm.hp.async.AsyncHandlerTask;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.entity.LrcInfo;
import com.zlm.hp.http.HttpReturnResult;
import com.zlm.hp.lyrics.LyricsReader;
import com.zlm.hp.lyrics.utils.LyricsUtils;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.util.HttpUtil;
import com.zlm.hp.util.ResourceUtil;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 歌词管理
 * @author: zhangliangming
 * @date: 2018-10-16 20:58
 **/
public class LyricsManager {
    private static Map<String, SoftReference<LyricsReader>> mLyricsReaderCache =
            new HashMap<String, SoftReference<LyricsReader>>();

    private static LyricsManager _LyricsManager;
    private static Context mContext;

    private LyricsManager(Context context) {
        this.mContext = context;
    }

    public synchronized static LyricsManager newInstance(Context context) {
        if (_LyricsManager == null) {
            _LyricsManager = new LyricsManager(context);
        }
        return _LyricsManager;
    }

    /**
     * @throws
     * @Description: 加载歌词
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-10-16 21:16
     */
    public void loadLyrics(final String fileName, final String keyword, final String duration, final String hash, final boolean askWifi, AsyncHandlerTask asyncHandlerTask, LoadLyricsCallBack loadLyricsCallBack) {
        asyncHandlerTask.execute(new AsyncHandlerTask.Task() {
            @Override
            protected Object doInBackground() {
                if (!mLyricsReaderCache.containsKey(hash)) {
                    File lrcFile = LyricsUtils.getLrcFile(fileName, ResourceUtil.getFilePath(mContext, ResourceConstants.PATH_LYRICS, null));
                    if (lrcFile != null && lrcFile.exists()) {
                        LyricsReader lyricsReader = new LyricsReader();
                        lyricsReader.setHash(hash);
                        lyricsReader.loadLrc(lrcFile);
                        mLyricsReaderCache.put(hash, new SoftReference<LyricsReader>(lyricsReader));
                    } else {
                        //下载歌词
                        File saveLrcFile = new File(ResourceUtil.getFilePath(mContext, ResourceConstants.PATH_LYRICS, fileName + ".krc"));
                        HttpReturnResult httpReturnResult = HttpUtil.getHttpClient().getLyricsInfo(mContext, keyword, duration, hash, askWifi);
                        if (httpReturnResult.isSuccessful() && httpReturnResult.getResult() != null) {
                            LrcInfo lrcInfo = (LrcInfo) httpReturnResult.getResult();
                            LyricsReader lyricsReader = new LyricsReader();
                            lyricsReader.setHash(hash);
                            lyricsReader.loadLrc(lrcInfo.getContent(), saveLrcFile, saveLrcFile.getName());
                            mLyricsReaderCache.put(hash, new SoftReference<LyricsReader>(lyricsReader));
                        }
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object result) {
                super.onPostExecute(result);
                //
                AudioBroadcastReceiver.sendLrcLoadedReceiver(mContext, hash);
            }
        });
    }

    /**
     * 获取歌词读取器
     * @param hash
     * @return
     */
    public LyricsReader getLyricsReader(String hash) {
        SoftReference<LyricsReader> lyricsReaderSoftReference = mLyricsReaderCache.get(hash);
        if (lyricsReaderSoftReference != null) {
            return lyricsReaderSoftReference.get();
        }
        return null;
    }

    public interface LoadLyricsCallBack {
        void callback(String hash);
    }
}
