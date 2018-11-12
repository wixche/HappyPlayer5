package com.zlm.hp.util;

import android.content.Context;
import android.text.TextUtils;

import com.zlm.hp.audio.AudioFileReader;
import com.zlm.hp.audio.TrackInfo;
import com.zlm.hp.audio.utils.AudioUtil;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.entity.StorageInfo;
import com.zlm.hp.ui.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 媒体处理类
 * Created by zhangliangming on 2018-08-18.
 */

public class MediaUtil {

    /**
     * 扫描本地歌曲
     *
     * @param context
     * @param operateListener
     * @return
     */
    public static List<AudioInfo> scanLocalMusic(Context context, OperateListener operateListener) {
        List<AudioInfo> result = new ArrayList<AudioInfo>();
        List<StorageInfo> list = StorageUtil
                .listAvaliableStorage(context);
        if (list == null || list.size() == 0) {

        } else {
            List<String> filterFormatList = AudioUtil.getSupportAudioExts();
            String[] filterFormat = new String[filterFormatList.size()];
            filterFormatList.toArray(filterFormat);
            for (int i = 0; i < list.size(); i++) {
                StorageInfo storageInfo = list.get(i);
                scanLocalAudioFile(context,result, storageInfo.getPath(), filterFormat, operateListener);
            }
        }
        return result;
    }

    /**
     * 扫描本地歌曲
     *
     * @param context
     * @param result          扫描结果集合
     * @param path            路径
     * @param filterFormat    文件格式
     * @param operateListener 操作回调
     */
    private static void scanLocalAudioFile(Context context, List<AudioInfo> result, String path, String[] filterFormat, OperateListener operateListener) {
        File[] files = new File(path).listFiles();
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                File temp = files[i];
                if (temp.isFile()) {

                    String fileName = temp.getName();
                    String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();

                    for (int j = 0; j < filterFormat.length; j++) {
                        if (fileExt.equals(filterFormat[j])) {
                            handleAudioFile(context,result, temp, operateListener);
                            break;
                        }
                    }

                } else if (temp.isDirectory() && temp.getPath().indexOf("/.") == -1) // 忽略点文件（隐藏文件/文件夹）
                {
                    scanLocalAudioFile(context, result, temp.getPath(), filterFormat, operateListener);
                }
            }
        }
    }

    /**
     * 处理音频文件
     *
     * @param context
     * @param result          扫描结果集合
     * @param audioFile       音频文件
     * @param operateListener 操作回调
     */
    private static void handleAudioFile(Context context, List<AudioInfo> result, File audioFile, OperateListener operateListener) {

        //歌曲文件hash值
        String hash = MD5Util.getFileMd5(audioFile).toLowerCase();

        if (operateListener != null) {
            if (operateListener.filter(hash)) {
                return;
            }
        }
        //
        String singerName = context.getResources().getString(R.string.unknow);
        String fileName = FileUtil.getFileNameWithoutExt(audioFile);
        String songName = fileName;
        if (fileName.contains("-")) {
            String regex = "\\s*-\\s*";
            String[] temps = fileName.split(regex);
            if (temps.length >= 2) {
                //去掉首尾空格
                singerName = fileName.split(regex)[0].trim();
                songName = fileName.split(regex)[1].trim();
            }
        }

        String filePath = audioFile.getPath();
        //歌曲文件后缀名
        String fileExt = FileUtil.getFileExt(filePath);

        //
        AudioFileReader audioFileReader = AudioUtil
                .getAudioFileReaderByFilePath(filePath);
        if (audioFileReader == null)
            return;
        TrackInfo trackInfoData = audioFileReader.read(audioFile);
        if (trackInfoData == null) {
            return;
        }

        //过滤时间短的歌曲
        int duration = (int) trackInfoData.getDuration();
        if (audioFile.length() < 1024 * 1024 || duration < 5000) {
            return;
        }
        String durationText = TimeUtil.parseTimeToAudioString(duration);

        // 歌曲文件的大小
        long fileSize = audioFile.length();
        String fileSizeText = FileUtil.getFileSize(fileSize);

        AudioInfo audioInfo = new AudioInfo();
        audioInfo.setCreateTime(DateUtil.parseDateToString(new Date()));
        audioInfo.setDuration(duration);
        audioInfo.setDurationText(durationText);
        audioInfo.setFileExt(fileExt);
        audioInfo.setFilePath(filePath);
        audioInfo.setFileSize(fileSize);
        audioInfo.setFileSizeText(fileSizeText);
        audioInfo.setHash(hash);
        audioInfo.setSongName(songName);
        audioInfo.setSingerName(singerName);

        setAudioCategory(audioInfo);

        audioInfo.setType(AudioInfo.TYPE_LOCAL);
        audioInfo.setStatus(AudioInfo.STATUS_FINISH);

        if (operateListener != null) {
            operateListener.foreach(audioInfo);
        }

        result.add(audioInfo);
    }

    /**
     * 设置音频的分类
     *
     * @param audioInfo
     */
    public static void setAudioCategory(AudioInfo audioInfo) {
        String singerName = audioInfo.getSingerName();
        if (!TextUtils.isEmpty(singerName)) {
            //分类tag为空
            String categoryName = PinYinUtil.getPinYin(singerName).toUpperCase();
            if (TextUtils.isEmpty(categoryName)) {
                audioInfo.setCategory("#");
            } else {
                String categoryTagName = categoryName.charAt(0) + "";
                if (categoryTagName.compareTo("A") >= 0 && categoryTagName.compareTo("Z") <= 0) {
                    audioInfo.setCategory(categoryTagName);
                    audioInfo.setChildCategory(categoryName);
                } else {
                    audioInfo.setCategory("#");
                }
            }
        }
    }

    /**
     *
     */
    public interface OperateListener {
        /**
         * 遍历
         *
         * @param audioInfo
         */
        void foreach(AudioInfo audioInfo);

        /**
         * 过滤 true则跳过
         *
         * @param hash
         * @return
         */
        boolean filter(String hash);
    }
}
