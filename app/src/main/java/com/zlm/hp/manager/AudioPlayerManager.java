package com.zlm.hp.manager;

import android.content.Context;
import android.text.TextUtils;

import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.db.util.DownloadThreadInfoDB;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.util.RandomUtil;
import com.zlm.hp.util.ResourceUtil;

import java.io.File;
import java.util.List;

/**
 * @Description: 音频处理
 * @author: zhangliangming
 * @date: 2018-09-22 0:26
 **/
public class AudioPlayerManager {

    /**
     * 正在播放
     */
    public static final int PLAYING = 0;
    /**
     * 暂停
     */
    public static final int PAUSE = 1;

    /**
     * 停止
     */
    public static final int STOP = 2;

    /**
     * 正在播放
     */
    public static final int PLAYINGNET = 3;

    /**
     * seekto
     */
    public static final int SEEKTO = 4;

    /**
     * 当前播放状态
     */
    private int mPlayStatus = STOP;

    private static AudioPlayerManager _AudioPlayerManager;

    /**
     * 在线音频管理
     */
    private OnLineAudioManager mOnLineAudioManager;

    /**
     *
     */
    private static Context mContext;

    public AudioPlayerManager(Context context) {
        this.mContext = context;
        mOnLineAudioManager = new OnLineAudioManager(context);
    }

    /**
     * @param context
     * @return
     */
    public synchronized static AudioPlayerManager getInstance(Context context) {
        if (_AudioPlayerManager == null) {
            _AudioPlayerManager = new AudioPlayerManager(context);
        }
        return _AudioPlayerManager;
    }

    /**
     *
     */
    public synchronized void init() {
        ConfigInfo configInfo = ConfigInfo.obtain();
        AudioInfo audioInfo = getCurSong(configInfo.getPlayHash());
        if (audioInfo != null) {
            AudioBroadcastReceiver.sendPlayInitReceiver(mContext, audioInfo);
        } else {
            AudioBroadcastReceiver.sendNullReceiver(mContext);
        }
    }

    /**
     * 添加下一首准备播放的歌曲
     */
    public synchronized void addNextSong(AudioInfo audioInfo) {
        ConfigInfo configInfo = ConfigInfo.obtain();
        List<AudioInfo> audioInfoList = configInfo.getAudioInfos();
        int nextSongIndex = getCurSongIndex(audioInfoList, audioInfo.getHash());
        if (nextSongIndex != -1) {
            audioInfoList.remove(nextSongIndex);
        }
        int curIndex = getCurSongIndex(audioInfoList, configInfo.getPlayHash());
        if (curIndex != -1) {
            audioInfoList.add((curIndex + 1), audioInfo);
        } else {
            audioInfoList.add(audioInfo);
        }
        configInfo.setAudioInfos(audioInfoList);
    }

    /**
     * 快进
     *
     * @param audioInfo
     */
    public synchronized void seekto(AudioInfo audioInfo) {
        //还有旧的歌曲在播放
        if (mPlayStatus == PLAYING || mPlayStatus == PLAYINGNET) {
            seektoPause();
            play(audioInfo);
        } else {
            mPlayStatus = SEEKTO;
        }
        AudioBroadcastReceiver.sendSeektoSongReceiver(mContext, audioInfo);
    }

    /**
     * 播放歌曲
     *
     * @param playProgress
     */
    public synchronized void play(int playProgress) {
        ConfigInfo configInfo = ConfigInfo.obtain();
        AudioInfo curAudioInfo = getCurSong(configInfo.getAudioInfos(), configInfo.getPlayHash());
        if (curAudioInfo != null) {
            if (playProgress > 0) mPlayStatus = SEEKTO;
            curAudioInfo.setPlayProgress(playProgress);
            play(curAudioInfo);
        }
    }

    /**
     * 播放
     *
     * @param audioInfo
     */
    public void playSong(AudioInfo audioInfo) {
        play(audioInfo);
    }


    /**
     * 播放歌曲
     */
    public synchronized void playOrPause() {
        if (mPlayStatus == PLAYING) {
            pause();
            return;
        }
        ConfigInfo configInfo = ConfigInfo.obtain();
        AudioInfo curAudioInfo = getCurSong(configInfo.getAudioInfos(), configInfo.getPlayHash());
        if (curAudioInfo != null) {
            play(curAudioInfo);
        }
    }

    /**
     * 播放歌曲
     */
    public void play(AudioInfo audioInfo) {
        boolean isSeekTo = mPlayStatus == SEEKTO;
        boolean isInit = ((mPlayStatus != PAUSE) && !isSeekTo);
        //还有旧的歌曲在播放
        if ((mPlayStatus == PLAYING || mPlayStatus == PLAYINGNET) && !isSeekTo) {
            pause();
        }

        if (isInit) {
            audioInfo.setPlayProgress(0);
        }

        //更新数据
        ConfigInfo configInfo = ConfigInfo.obtain();
        configInfo.setPlayHash(audioInfo.getHash());
        AudioInfo curAudioInfo = getCurSong(configInfo.getAudioInfos(), audioInfo.getHash());
        curAudioInfo.setPlayProgress(audioInfo.getPlayProgress());
        configInfo.save();

        //

        if (!isSeekTo) {
            //发送play init 数据
            AudioBroadcastReceiver.sendPlayInitReceiver(mContext, audioInfo);
        }

        switch (audioInfo.getType()) {
            case AudioInfo.TYPE_LOCAL:
                mPlayStatus = PLAYING;
                AudioBroadcastReceiver.sendPlayLocalSongReceiver(mContext, audioInfo);
                break;
            case AudioInfo.TYPE_NET:

                String fileName = audioInfo.getTitle();
                String filePath = ResourceUtil.getFilePath(mContext, ResourceConstants.PATH_AUDIO, fileName + "." + audioInfo.getFileExt());
                File audioFile = new File(filePath);
                if (audioFile.exists()) {
                    mPlayStatus = PLAYING;
                    //设置文件路径
                    audioInfo.setFilePath(filePath);
                    AudioBroadcastReceiver.sendPlayLocalSongReceiver(mContext, audioInfo);
                } else {
                    filePath = ResourceUtil.getFilePath(mContext, ResourceConstants.PATH_CACHE_AUDIO, audioInfo.getHash() + ".temp");
                    audioFile = new File(filePath);
                    if (!audioFile.exists()) {
                        //临时文件不存在，删除数据库中的数据
                        DownloadThreadInfoDB.delete(mContext, audioInfo.getHash(), OnLineAudioManager.mThreadNum);
                    }
                    mPlayStatus = PLAYINGNET;
                    int downloadedSize = DownloadThreadInfoDB.getDownloadedSize(mContext, audioInfo.getHash(), OnLineAudioManager.mThreadNum);
                    if (downloadedSize == audioInfo.getFileSize()) {
                        mPlayStatus = PLAYING;
                        //设置文件路径
                        audioInfo.setFilePath(filePath);
                        AudioBroadcastReceiver.sendPlayLocalSongReceiver(mContext, audioInfo);
                    } else {
                        if (downloadedSize > 1024 * 500) {
                            AudioBroadcastReceiver.sendPlayNetSongReceiver(mContext, audioInfo);
                        }
                        mOnLineAudioManager.addDownloadTask(audioInfo);
                    }
                }
                break;
        }

    }

    /**
     * 播放正在下载中的网络歌曲
     *
     * @param audioInfo
     */
    public synchronized void playDownloadingNetSong(AudioInfo audioInfo) {
        mPlayStatus = PLAYING;
        audioInfo.setPlayProgress(0);
        AudioBroadcastReceiver.sendPlayNetSongReceiver(mContext, audioInfo);
    }

    /**
     * seek to pause
     */
    private void seektoPause() {
        mPlayStatus = SEEKTO;
        //暂停在线任务
        mOnLineAudioManager.pauseTask();
    }


    /**
     * 暂停
     */
    public synchronized void pause() {
        mPlayStatus = PAUSE;
        //暂停在线任务
        mOnLineAudioManager.pauseTask();
        AudioBroadcastReceiver.sendStopReceiver(mContext);

    }

    /**
     * 停止
     */
    public synchronized void stop() {
        mPlayStatus = STOP;
        //暂停在线任务
        mOnLineAudioManager.pauseTask();
        AudioBroadcastReceiver.sendStopReceiver(mContext);

    }

    /**
     * 下一首
     */
    public synchronized void next() {
        pause();

        //下一首时，说明现在播放停止了
        mPlayStatus = STOP;

        AudioInfo nextAudioInfo = null;
        ConfigInfo configInfo = ConfigInfo.obtain();
        List<AudioInfo> audioInfoList = configInfo.getAudioInfos();
        String hash = configInfo.getPlayHash();
        //获取播放索引
        int playIndex = getCurSongIndex(audioInfoList, hash);

        if (playIndex == -1) {

            AudioBroadcastReceiver.sendNullReceiver(mContext);

            return;
        }

        int playModel = configInfo.getPlayModel();
        switch (playModel) {
            case 0:
                // 顺序播放

                playIndex++;


                break;
            case 1:
                // 随机播放

                playIndex = RandomUtil.createRandomNum();


                break;
            case 2:
                // 循环播放

                playIndex++;
                if (playIndex >= audioInfoList.size()) {
                    playIndex = 0;
                }

                break;
            case 3:
                // 单曲播放
                break;
        }

        if (playIndex >= audioInfoList.size()) {

            AudioBroadcastReceiver.sendNullReceiver(mContext);
            return;
        }
        if (audioInfoList.size() > 0) {

            nextAudioInfo = audioInfoList.get(playIndex);

        }

        if (nextAudioInfo == null) {

            AudioBroadcastReceiver.sendNullReceiver(mContext);
        } else {
            play(nextAudioInfo);
        }

    }

    /**
     * 上一首
     */
    public synchronized void pre() {
        pause();
        //上一首时，说明现在播放停止了
        mPlayStatus = STOP;

        AudioInfo nextAudioInfo = null;
        ConfigInfo configInfo = ConfigInfo.obtain();
        List<AudioInfo> audioInfoList = configInfo.getAudioInfos();
        String hash = configInfo.getPlayHash();
        //获取播放索引
        int playIndex = getCurSongIndex(audioInfoList, hash);

        if (playIndex == -1) {

            AudioBroadcastReceiver.sendNullReceiver(mContext);

            return;
        }

        int playModel = configInfo.getPlayModel();
        switch (playModel) {
            case 0:
                // 顺序播放

                playIndex--;


                break;
            case 1:
                // 随机播放

                playIndex = RandomUtil.createRandomNum();


                break;
            case 2:
                // 循环播放

                // 循环播放
                playIndex--;
                if (playIndex < 0) {
                    playIndex = 0;
                }
                if (playIndex >= audioInfoList.size()) {
                    playIndex = 0;
                }

                break;
            case 3:
                // 单曲播放
                break;
        }

        if (playIndex < 0) {
            AudioBroadcastReceiver.sendNullReceiver(mContext);
            return;
        }
        if (audioInfoList.size() > 0) {

            nextAudioInfo = audioInfoList.get(playIndex);

        }

        if (nextAudioInfo == null) {
            AudioBroadcastReceiver.sendNullReceiver(mContext);
        } else {
            play(nextAudioInfo);
        }
    }

    /**
     * 释放
     */
    public synchronized void release() {
        mPlayStatus = PAUSE;
        mOnLineAudioManager.release();
    }

    /**
     * 插队播放歌曲
     */
    public synchronized void playSongAndAdd(AudioInfo audioInfo) {
        ConfigInfo configInfo = ConfigInfo.obtain();
        List<AudioInfo> audioInfoList = configInfo.getAudioInfos();
        int nextIndex = getCurSongIndex(audioInfoList, audioInfo.getHash());
        if (nextIndex != -1) {
            audioInfoList.remove(nextIndex);
        }
        int curIndex = getCurSongIndex(audioInfoList, configInfo.getPlayHash());
        if (curIndex == -1) {
            audioInfoList.add(audioInfo);
            configInfo.setAudioInfos(audioInfoList);
        } else {
            audioInfoList.add(curIndex + 1, audioInfo);
            configInfo.setAudioInfos(audioInfoList);
        }

        play(audioInfo);
    }

    /**
     * 添加当前播放歌曲，并且修改当前的播放列表
     *
     * @param audioInfoList
     * @param audioInfo
     */
    public synchronized void playSong(List<AudioInfo> audioInfoList, AudioInfo audioInfo) {
        if (audioInfoList != null) {
            ConfigInfo configInfo = ConfigInfo.obtain();
            configInfo.setAudioInfos(audioInfoList);
            //播放歌曲
            play(audioInfo);
        }
    }

    /**
     * 获取当前播放歌曲索引
     *
     * @return
     */
    public int getCurSongIndex(List<AudioInfo> audioInfoList, String hash) {
        int index = -1;
        if (audioInfoList == null) return index;
        for (int i = 0; i < audioInfoList.size(); i++) {
            AudioInfo temp = audioInfoList.get(i);
            if (temp.getHash().equals(hash)) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * 获取当前播放歌曲
     *
     * @return
     */
    public synchronized AudioInfo getCurSong(String hash) {
        ConfigInfo configInfo = ConfigInfo.obtain();
        AudioInfo curAudioInfo = getCurSong(configInfo.getAudioInfos(), hash);
        return curAudioInfo;
    }

    /**
     * 获取当前播放歌曲
     *
     * @return
     */
    public AudioInfo getCurSong(List<AudioInfo> audioInfoList, String hash) {
        AudioInfo curAudioInfo = null;
        if (audioInfoList == null || TextUtils.isEmpty(hash)) return null;
        for (int i = 0; i < audioInfoList.size(); i++) {
            AudioInfo temp = audioInfoList.get(i);
            if (temp.getHash().equals(hash)) {
                curAudioInfo = temp;
                break;
            }
        }
        return curAudioInfo;
    }

    public int getPlayStatus() {
        return mPlayStatus;
    }

    public void setPlayStatus(int playStatus) {
        this.mPlayStatus = playStatus;
    }
}
