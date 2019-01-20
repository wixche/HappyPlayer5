package com.zlm.hp.manager;

import android.content.Context;

import com.zlm.hp.async.AsyncHandlerTask;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.entity.LrcInfo;
import com.zlm.hp.http.HttpReturnResult;
import com.zlm.hp.lyrics.LyricsReader;
import com.zlm.hp.lyrics.model.LyricsInfo;
import com.zlm.hp.lyrics.utils.LyricsIOUtils;
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

    public synchronized static LyricsManager getInstance(Context context) {
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
                File lrcFile = LyricsUtils.getLrcFile(fileName, ResourceUtil.getFilePath(mContext, ResourceConstants.PATH_LYRICS, null));

                try {

                    if (getLyricsReader(hash) == null) {
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
                } catch (Exception e) {
                    e.printStackTrace();
                    if (lrcFile != null && lrcFile.exists()) {
                        lrcFile.delete();
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
     * 设置歌词读取器
     *
     * @param key
     * @param lyricsReader
     */
    public void setLyricsReader(String key, LyricsReader lyricsReader) {
        if (mLyricsReaderCache.containsKey(key)) {
            mLyricsReaderCache.remove(key);
        }
        mLyricsReaderCache.put(key, new SoftReference<LyricsReader>(lyricsReader));
        //保存歌词文件
        saveLrcFile(lyricsReader.getLrcFilePath(), lyricsReader.getLyricsInfo());
    }

    /**
     * @param lrcFilePath
     * @param lyricsInfo
     */
    private void saveLrcFile(String lrcFilePath, LyricsInfo lyricsInfo) {
        //保存修改的歌词文件
        try {
            LyricsIOUtils.getLyricsFileWriter(lrcFilePath).writer(lyricsInfo, lrcFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param hash
     */
    public void remove(String hash) {
        mLyricsReaderCache.remove(hash);
    }

    /**
     *
     */
    public void release() {
        if (mLyricsReaderCache != null)
            mLyricsReaderCache.clear();
    }

    /**
     * 获取歌词读取器
     *
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
