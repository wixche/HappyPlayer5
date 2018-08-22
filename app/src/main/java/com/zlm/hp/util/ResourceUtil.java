package com.zlm.hp.util;

import android.content.Context;
import android.text.TextUtils;

import com.zlm.hp.entity.StorageInfo;

import java.io.File;
import java.util.List;

/**
 * @Description: 资源文件处理类
 * @Author: zhangliangming
 * @Date: 2017/7/16 13:48
 * @Version:
 */
public class ResourceUtil {
    /**
     * 文件的存储路径
     */
    private static String mStorageFilePath = null;

    /**
     * 获取资源文件的完整路径
     *
     * @param context
     * @param baseFilePath 文件的基本路径
     * @return
     */
    public static String getFilePath(Context context, String baseFilePath) {
        return getFilePath(context, baseFilePath, null);
    }

    /**
     * 获取app内存存储路径
     *
     * @param context
     * @param baseFilePath
     * @return
     */
    public static String getContextFilePath(Context context, String baseFilePath) {
        return getFilePath(context.getFilesDir().getAbsolutePath(), baseFilePath, null);
    }

    /**
     * 获取app内存存储路径
     *
     * @param context
     * @param baseFilePath
     * @param fileName
     * @return
     */
    public static String getContextFilePath(Context context, String baseFilePath, String fileName) {
        return getFilePath(context.getFilesDir().getAbsolutePath(), baseFilePath, fileName);
    }

    /**
     * 获取资源文件的完整路径
     *
     * @param context
     * @param baseFilePath 文件的基本路径
     * @return
     */
    public static String getFilePath(Context context, String baseFilePath, String fileName) {
        if (mStorageFilePath == null) {
            List<StorageInfo> storageInfos = StorageUtil.listAvaliableStorage(context);
            for (int i = 0; i < storageInfos.size(); i++) {
                StorageInfo temp = storageInfos.get(i);
                if (!temp.isRemoveable()) {
                    mStorageFilePath = temp.getPath();
                    break;
                }
            }
        }
        return getFilePath(mStorageFilePath, baseFilePath, fileName);
    }

    /**
     * @param rootFilePath
     * @param baseFilePath
     * @param fileName
     * @return
     */
    private static String getFilePath(String rootFilePath, String baseFilePath, String fileName) {
        String filePath = null;
        //文件名是否为空
        if (!TextUtils.isEmpty(fileName)) {
            filePath = rootFilePath + File.separator + baseFilePath + File.separator + fileName;
            File file = new File(filePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
        } else {
            filePath = rootFilePath + File.separator + baseFilePath;
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return filePath;
    }
}
