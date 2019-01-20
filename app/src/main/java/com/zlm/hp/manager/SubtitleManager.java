package com.zlm.hp.manager;

import android.content.Context;

import com.zlm.hp.async.AsyncHandlerTask;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.db.util.SubtitleInfoDB;
import com.zlm.hp.entity.SubtitleInfo;
import com.zlm.hp.http.HttpClient;
import com.zlm.hp.util.ResourceUtil;
import com.zlm.subtitlelibrary.SubtitleReader;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 字幕管理器
 * @author: zhangliangming
 * @date: 2019-01-20 14:50
 **/
public class SubtitleManager {
    private static Map<String, SoftReference<SubtitleReader>> mSubtitleReaderCache =
            new HashMap<String, SoftReference<SubtitleReader>>();

    private static SubtitleManager _SubtitleReader;
    private static Context mContext;

    private SubtitleManager(Context context) {
        this.mContext = context;
    }

    public synchronized static SubtitleManager getInstance(Context context) {
        if (_SubtitleReader == null) {
            _SubtitleReader = new SubtitleManager(context);
        }
        return _SubtitleReader;
    }

    /**
     * 加载字幕
     * @param videoHash
     * @param keyword 关键字
     * @param subtitleInfo 字幕内容
     * @param asyncHandlerTask
     * @param loadSubtitleCallBack
     */
    public void loadSubtitle(final String videoHash, final String keyword, final SubtitleInfo subtitleInfo, AsyncHandlerTask asyncHandlerTask, final LoadSubtitleCallBack loadSubtitleCallBack) {
        asyncHandlerTask.execute(new AsyncHandlerTask.Task() {
            @Override
            protected SubtitleReader doInBackground() {
                String key = subtitleInfo.getDownloadUrl().hashCode() + "";
                if (mSubtitleReaderCache.get(key).get() == null) {
                    HttpClient.Result result = new HttpClient().get(subtitleInfo.getDownloadUrl());
                    if (result.isSuccessful()) {

                        String dataResult = result.getDataString();
                        File saveFile = new File(ResourceUtil.getFilePath(mContext, ResourceConstants.PATH_SUBTITLE + File.separator + keyword, subtitleInfo.getFileName()));
                        SubtitleReader subtitleReader = new SubtitleReader();
                        subtitleReader.readText(dataResult, saveFile);
                        if (subtitleReader.getSubtitleInfo() != null) {
                            if (subtitleReader.getSubtitleInfo().getSubtitleLineInfos() != null) {
                                if (subtitleReader.getSubtitleInfo().getSubtitleLineInfos().size() > 0) {
                                    mSubtitleReaderCache.put(key, new SoftReference<SubtitleReader>(subtitleReader));

                                    if (SubtitleInfoDB.isSubtitleExists(mContext, videoHash)) {
                                        SubtitleInfoDB.updateSubtitleInfo(mContext, videoHash, subtitleInfo.getFileName(), subtitleInfo.getFilePath(), subtitleInfo.getDownloadUrl());
                                    } else {
                                        subtitleInfo.setFilePath(saveFile.getPath());
                                        subtitleInfo.setVideoHash(videoHash);
                                        SubtitleInfoDB.addSubtitleInfo(mContext, subtitleInfo);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    return mSubtitleReaderCache.get(key).get();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object result) {
                super.onPostExecute(result);
                if (loadSubtitleCallBack != null) {
                    if (result != null) {
                        SubtitleReader subtitleReader = (SubtitleReader) result;
                        loadSubtitleCallBack.callback(subtitleReader);
                    } else {
                        loadSubtitleCallBack.callback(null);
                    }
                }
            }
        });
    }

    public interface LoadSubtitleCallBack {
        void callback(SubtitleReader subtitleReader);
    }
}
