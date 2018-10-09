package com.zlm.hp.manager;

import android.content.Context;
import android.os.Bundle;

import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.db.util.DownloadThreadInfoDB;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
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
     * 当前播放状态
     */
    private int mPlayStatus = PAUSE;

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
    public static AudioPlayerManager newInstance(Context context) {
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
        configInfo.setAudioInfos(audioInfoList);
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
        //还有旧的歌曲在播放
        if (mPlayStatus == PLAYING) {
            pause();
        }
        mPlayStatus = PLAYING;

        ConfigInfo configInfo = ConfigInfo.obtain();
        configInfo.setPlayHash(audioInfo.getHash());
        configInfo.save();

        switch (audioInfo.getType()) {
            case AudioInfo.TYPE_LOCAL:
                playLocalSong(audioInfo);
                break;
            case AudioInfo.TYPE_NET:

                String fileName = audioInfo.getTitle();
                String filePath = ResourceUtil.getFilePath(mContext, ResourceConstants.PATH_AUDIO, fileName + "." + audioInfo.getFileExt());
                File audioFile = new File(filePath);
                if (audioFile.exists()) {
                    //设置文件路径
                    audioInfo.setFilePath(filePath);
                    playLocalSong(audioInfo);
                } else {
                    int downloadedSize = DownloadThreadInfoDB.getDownloadedSize(mContext, audioInfo.getHash(), OnLineAudioManager.threadNum);
                    if (downloadedSize > 1024 * 200) {
                        playNetSong(audioInfo);
                    }
                    mOnLineAudioManager.addDownloadTask(audioInfo);
                }
                break;
        }

    }

    /**
     * 播放正在下载中的网络歌曲
     *
     * @param audioInfo
     */
    public void playDownloadingNetSong(AudioInfo audioInfo) {
        //还有旧的歌曲在播放
        if (mPlayStatus == PLAYING) {
            pause();
        }
        playNetSong(audioInfo);
    }

    /**
     * 播放网络歌曲
     *
     * @param audioInfo
     */
    private void playNetSong(AudioInfo audioInfo) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY, audioInfo);
        AudioBroadcastReceiver.sendReceiver(mContext, AudioBroadcastReceiver.ACTION_CODE_PLAYNETSONG, AudioBroadcastReceiver.ACTION_BUNDLEKEY, bundle);
    }

    /**
     * 播放本地歌曲
     *
     * @param audioInfo
     */
    private void playLocalSong(AudioInfo audioInfo) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY, audioInfo);
        AudioBroadcastReceiver.sendReceiver(mContext, AudioBroadcastReceiver.ACTION_CODE_PLAYLOCALSONG, AudioBroadcastReceiver.ACTION_BUNDLEKEY, bundle);
    }

    /**
     * 暂停
     */
    public void pause() {

        mPlayStatus = PAUSE;
        //暂停在线任务
        mOnLineAudioManager.pauseTask();

        AudioBroadcastReceiver.sendReceiver(mContext, AudioBroadcastReceiver.ACTION_CODE_STOP);

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
        mOnLineAudioManager.release();
    }

    /**
     * 播放歌曲
     */
    public void playSong(AudioInfo audioInfo) {
        addNextSong(audioInfo);
        play(audioInfo);
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
        play(audioInfo);
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
    public AudioInfo getCurSong(String hash) {
        ConfigInfo configInfo = ConfigInfo.obtain();
        AudioInfo curAudioInfo = getCurSong(configInfo.getAudioInfos(), hash);
        return curAudioInfo;
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
