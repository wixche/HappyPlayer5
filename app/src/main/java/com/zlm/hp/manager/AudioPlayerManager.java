package com.zlm.hp.manager;

import android.content.Context;

import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.entity.AudioInfo;

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
     * 当前播放状态
     */
    private int mPlayStatus = PAUSE;

    private static AudioPlayerManager _AudioPlayerManager;

    /**
     *
     */
    private static Context mContext;

    public AudioPlayerManager(Context context) {
        this.mContext = context;
    }

    /**
     * @param context
     * @return
     */
    public AudioPlayerManager newInstance(Context context) {
        if (_AudioPlayerManager == null) {
            _AudioPlayerManager = new AudioPlayerManager(context);
        }
        return _AudioPlayerManager;
    }

    /**
     * 添加下一首准备播放的歌曲
     */
    public void addNextSong(AudioInfo audioInfo) {
        ConfigInfo configInfo = ConfigInfo.obtain();
        List<AudioInfo> audioInfoList = configInfo.getAudioInfos();
        int curIndex = getCurSongIndex(audioInfoList, configInfo.getPlayHash());
        if (curIndex != -1) {
            audioInfoList.add((curIndex + 1), audioInfo);
        } else {
            audioInfoList.add(audioInfo);
        }
    }

    /**
     * 播放或者暂停
     */
    public void playOrPause() {
        if (mPlayStatus == PLAYING) {
            pause();
        } else {
            play();
        }
    }

    /**
     * 播放歌曲
     */
    public void play() {
        ConfigInfo configInfo = ConfigInfo.obtain();
        AudioInfo curAudioInfo = getCurSong(configInfo.getAudioInfos(), configInfo.getPlayHash());
        if (curAudioInfo != null) {
            play(curAudioInfo);
        }
    }

    /**
     * 播放歌曲
     */
    private void play(AudioInfo audioInfo) {
        switch (audioInfo.getType()) {
            case AudioInfo.TYPE_LOCAL:
                playLocalSong(audioInfo);
                break;
            case AudioInfo.TYPE_NET:
                playNetSong(audioInfo);
                break;
        }

    }

    /**
     * 播放网络歌曲
     *
     * @param audioInfo
     */
    private void playNetSong(AudioInfo audioInfo) {
    }

    /**
     * 播放本地歌曲
     *
     * @param audioInfo
     */
    private void playLocalSong(AudioInfo audioInfo) {
    }

    /**
     * 暂停
     */
    public void pause() {

    }

    /**
     * 下一首
     */
    public void next() {

    }

    /**
     * 上一首
     */
    public void pre() {

    }

    /**
     * 释放
     */
    public void release() {
        mPlayStatus = PAUSE;
    }

    /**
     * 播放歌曲
     */
    public void playSong(AudioInfo audioInfo) {
        addNextSong(audioInfo);
        ConfigInfo configInfo = ConfigInfo.obtain();
        configInfo.setPlayHash(audioInfo.getHash());

    }

    /**
     * 添加当前播放歌曲，并且修改当前的播放列表
     *
     * @param audioInfoList
     * @param audioInfo
     */
    public void playSong(List<AudioInfo> audioInfoList, AudioInfo audioInfo) {
        ConfigInfo configInfo = ConfigInfo.obtain();
        configInfo.setAudioInfos(audioInfoList);
        //播放歌曲
    }

    /**
     * 获取当前播放歌曲索引
     *
     * @return
     */
    private int getCurSongIndex(List<AudioInfo> audioInfoList, String hash) {
        int index = -1;

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
    private AudioInfo getCurSong(List<AudioInfo> audioInfoList, String hash) {
        AudioInfo curAudioInfo = null;
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
