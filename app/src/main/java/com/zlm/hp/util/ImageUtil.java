package com.zlm.hp.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;

import com.zlm.hp.async.AsyncHandlerTask;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.entity.SingerInfo;
import com.zlm.hp.http.APIHttpClient;
import com.zlm.hp.http.HttpClient;
import com.zlm.hp.http.HttpReturnResult;
import com.zlm.hp.ui.R;
import com.zlm.hp.widget.TransitionImageView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @author: zhangliangming
 * @date: 2018-10-05 12:46
 **/
public class ImageUtil {

    // 缓存
    private static LruCache<String, Bitmap> mImageCache = getImageCache();

    /**
     * 初始化图片内存
     */
    private static LruCache<String, Bitmap> getImageCache() {
        // 获取系统分配给每个应用程序的最大内存，每个应用系统分配32M
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int mCacheSize = maxMemory / 8;
        // 给LruCache分配1/8 4M
        LruCache<String, Bitmap> sImageCache = new LruCache<String, Bitmap>(
                mCacheSize) {

            // 必须重写此方法，来测量Bitmap的大小
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };

        return sImageCache;
    }

    /**
     * 加载歌手头像
     *
     * @param context
     * @param imageView
     */
    public static void loadSingerImage(final Context context, ImageView imageView, final String singerName, final boolean askWifi, int width, int height, AsyncHandlerTask asyncHandlerTask, ImageLoadCallBack imageLoadCallBack) {
        //多个歌手，则取第一个歌手头像
        String regex = "\\s*、\\s*";
        String searchSingerName = singerName;
        if (singerName.contains(regex)) {
            searchSingerName = singerName.split(regex)[0];
        }

        final String filePath = ResourceUtil.getFilePath(context, ResourceConstants.PATH_SINGER, searchSingerName + File.separator + searchSingerName + ".jpg");

        final String finalSearchSingerName = searchSingerName;
        LoadImgUrlCallBack loadImgUrlCallBack = new LoadImgUrlCallBack() {
            @Override
            public String getImageUrl() {

                APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
                HttpReturnResult httpReturnResult = apiHttpClient.getSingerIcon(context, finalSearchSingerName, askWifi);
                if (httpReturnResult.isSuccessful() && httpReturnResult.getResult() != null) {
                    SingerInfo singerInfo = (SingerInfo) httpReturnResult.getResult();
                    return singerInfo.getImageUrl();
                }

                return null;
            }
        };
        loadImage(context, filePath, null, askWifi, imageView, width, height, asyncHandlerTask, loadImgUrlCallBack, imageLoadCallBack);
    }

    /**
     * 加载图片
     *
     * @param context
     * @param filePath
     * @param imageUrl
     * @param imageView
     * @param asyncHandlerTask
     */
    public static void loadImage(final Context context, final String filePath, final String imageUrl, final boolean askWifi, final ImageView imageView, final int width, final int height, AsyncHandlerTask asyncHandlerTask, final ImageLoadCallBack imageLoadCallBack) {
        loadImage(context, filePath, imageUrl, askWifi, imageView, width, height, asyncHandlerTask, null, imageLoadCallBack);
    }


    /**
     * 加载图片
     *
     * @param context
     * @param filePath
     * @param imageUrl
     * @param imageView
     * @param asyncHandlerTask
     */
    private static void loadImage(final Context context, final String filePath, final String imageUrl, final boolean askWifi, final ImageView imageView, final int width, final int height, AsyncHandlerTask asyncHandlerTask, final LoadImgUrlCallBack loadImgUrlCallBack, final ImageLoadCallBack imageLoadCallBack) {
        final String key = filePath.hashCode() + "";
        //如果当前的图片与上一次一样，则不操作
        if (imageView.getTag() != null && imageView.getTag().equals(key)) {
            return;
        }

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.bpz);
        imageView.setImageDrawable(new BitmapDrawable(bitmap));

        imageView.setTag(key);
        asyncHandlerTask.execute(new AsyncHandlerTask.Task<Bitmap>() {
            @Override
            protected Bitmap doInBackground() {
                return loadImageFormCache(context, filePath, imageUrl, key, width, height, askWifi, loadImgUrlCallBack);
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                super.onPostExecute(result);
                if (result != null && imageView.getTag() != null && imageView.getTag().equals(key)) {
                    imageView.setImageDrawable(new BitmapDrawable(result));
                } else {
                    imageView.setTag(null);
                }
                if (imageLoadCallBack != null) {
                    imageLoadCallBack.callback(result);
                }
            }
        });
    }

    /**
     * 加载歌手写真图片
     */
    public static void loadSingerImage(final Context context, final TransitionImageView singerImageView, final String singerName, final boolean askWifi, final AsyncHandlerTask asyncHandlerTask) {

        final String key = singerName.hashCode() + "";
        //如果当前的图片与上一次一样，则不操作
        if (singerImageView.getTag() != null && singerImageView.getTag().equals(key)) {
            return;
        }

        singerImageView.setVisibility(View.INVISIBLE);
        singerImageView.resetData();

        String[] singerNameArray = null;
        if (singerName.contains("、")) {

            String regex = "\\s*、\\s*";
            singerNameArray = singerName.split(regex);

        } else {
            singerNameArray = new String[1];
            singerNameArray[0] = singerName;
        }
        singerImageView.setTag(key);

        final List<SingerInfo> returnResult = new ArrayList<SingerInfo>();
        for (int i = 0; i < singerNameArray.length; i++) {
            final String searchSingerName = singerNameArray[i];

            asyncHandlerTask.execute(new AsyncHandlerTask.Task<List<SingerInfo>>() {
                @Override
                protected List<SingerInfo> doInBackground() {
                    APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
                    HttpReturnResult httpReturnResult = apiHttpClient.getSingerPicList(context, searchSingerName, askWifi);
                    if (httpReturnResult.isSuccessful()) {
                        Map<String, Object> returnResult = (Map<String, Object>) httpReturnResult.getResult();
                        List<SingerInfo> lists = (List<SingerInfo>) returnResult.get("rows");
                        List<SingerInfo> listResult = new ArrayList<SingerInfo>();
                        if (lists != null) {
                            int maxSize = 3;
                            int size = lists.size() > maxSize ? maxSize : lists.size();
                            if (size > 0) {
                                for (int i = 0; i < size; i++) {
                                    SingerInfo singerInfo = lists.get(i);
                                    String imageUrl = singerInfo.getImageUrl();
                                    ImageUtil.loadSingerImage(context, asyncHandlerTask, singerInfo.getSingerName(), imageUrl, askWifi);

                                    listResult.add(singerInfo);
                                }
                            }
                            return listResult;
                        }
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(List<SingerInfo> result) {
                    super.onPostExecute(result);
                    if (result != null && result.size() > 0) {
                        returnResult.addAll(result);
                        singerImageView.initData(returnResult);
                    }
                }
            });
        }
    }

    /**
     * 获取歌手写真图片
     *
     * @param context
     * @param asyncHandlerTask
     * @param singerName
     * @return
     */
    public static void loadSingerImage(final Context context, AsyncHandlerTask asyncHandlerTask, String singerName, final String imageUrl, final boolean askWifi) {
        final String filePath = ResourceUtil.getFilePath(context, ResourceConstants.PATH_SINGER, singerName + File.separator + imageUrl.hashCode() + ".jpg");
        final String key = imageUrl.hashCode() + "";
        asyncHandlerTask.execute(new AsyncHandlerTask.Task() {
            @Override
            protected Object doInBackground() {
                loadImageFormCache(context, filePath, imageUrl, key, 720, 1080, askWifi, null);
                return null;
            }
        });
    }


    /**
     * @throws
     * @Description: 从缓存中获取
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-10-05 16:37
     */
    private static Bitmap loadImageFormCache(Context context, String filePath, String imageUrl, String key, int width, int height, boolean askWifi, LoadImgUrlCallBack loadImgUrlCallBack) {
        Bitmap bitmap = null;
        if (mImageCache.get(key) != null) {
            bitmap = mImageCache.get(key);
        }
        if (bitmap == null) {
            bitmap = loadImageFormFile(context, filePath, imageUrl, width, height, askWifi, loadImgUrlCallBack);
            if (bitmap != null) {
                mImageCache.put(key, bitmap);
            }
        }
        return bitmap;
    }

    /**
     * @throws
     * @Description: 从本地获取图片
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-10-05 15:18
     */
    private static Bitmap loadImageFormFile(Context context, String filePath, String imageUrl, int width, int height, boolean askWifi, LoadImgUrlCallBack loadImgUrlCallBack) {
        Bitmap bitmap = readBitmapFromFile(filePath, width, height);
        if (bitmap == null) {
            bitmap = loadImageFormUrl(context, imageUrl, width, height, askWifi, loadImgUrlCallBack);
            if (bitmap != null) {
                writeBitmapToFile(filePath, bitmap, 100);
            }
        }
        return bitmap;
    }

    /**
     * @throws
     * @Description: 从网上获取图片
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-10-05 15:19
     */
    private static Bitmap loadImageFormUrl(Context context, String imageUrl, int width, int height, boolean askWifi, LoadImgUrlCallBack loadImgUrlCallBack) {
        if (askWifi) {
            if (!NetUtil.isWifiConnected(context)) {
                return null;
            }
        }
        if (imageUrl == null && loadImgUrlCallBack == null) {
            return null;
        }

        if (imageUrl == null && loadImgUrlCallBack != null) {
            imageUrl = loadImgUrlCallBack.getImageUrl();
        }

        if (imageUrl == null) {
            return null;
        }

        HttpClient.Result result = new HttpClient().get(imageUrl);
        if (!result.isSuccessful() || result.getData() == null || result.getData().length == 0) {
            return null;
        }
        byte[] data = result.getData();
        Bitmap bitmap = readBitmapFromByteArray(data, width, height);
        return bitmap;
    }

    /**
     * 获取缩放后的本地图片
     *
     * @param filePath 文件路径
     * @param width    宽
     * @param height   高
     * @return
     */
    private static Bitmap readBitmapFromFile(String filePath, int width, int height) {
        try {
            FileInputStream fis = new FileInputStream(filePath);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fis.getFD(), null, options);
            float srcWidth = options.outWidth;
            float srcHeight = options.outHeight;
            int inSampleSize = 1;

            if (srcHeight > height || srcWidth > width) {
                if (srcWidth > srcHeight) {
                    inSampleSize = Math.round(srcHeight / height);
                } else {
                    inSampleSize = Math.round(srcWidth / width);
                }
            }

            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;

            return BitmapFactory.decodeFileDescriptor(fis.getFD(), null, options);
        } catch (Exception ex) {
        }
        return null;
    }

    /**
     * 获取缩放后的本地图片
     *
     * @param ins    输入流
     * @param width  宽
     * @param height 高
     * @return
     */
    private static Bitmap readBitmapFromInputStream(InputStream ins, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(ins, null, options);
        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;
        int inSampleSize = 1;

        if (srcHeight > height || srcWidth > width) {
            if (srcWidth > srcHeight) {
                inSampleSize = Math.round(srcHeight / height);
            } else {
                inSampleSize = Math.round(srcWidth / width);
            }
        }

        options.inJustDecodeBounds = false;
        options.inSampleSize = inSampleSize;

        return BitmapFactory.decodeStream(ins, null, options);
    }

    /**
     * 从资源文件中获取图片
     *
     * @param resources
     * @param resourcesId
     * @param width
     * @param height
     * @return
     */
    private static Bitmap readBitmapFromResource(Resources resources, int resourcesId, int width, int height) {
        InputStream ins = resources.openRawResource(resourcesId);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(ins, null, options);
        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;
        int inSampleSize = 1;

        if (srcHeight > height || srcWidth > width) {
            if (srcWidth > srcHeight) {
                inSampleSize = Math.round(srcHeight / height);
            } else {
                inSampleSize = Math.round(srcWidth / width);
            }
        }

        options.inJustDecodeBounds = false;
        options.inSampleSize = inSampleSize;

        return BitmapFactory.decodeStream(ins, null, options);
    }

    /**
     * 从二进制数据中获取图片
     *
     * @param data
     * @param width
     * @param height
     * @return
     */
    private static Bitmap readBitmapFromByteArray(byte[] data, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;
        int inSampleSize = 1;

        if (srcHeight > height || srcWidth > width) {
            if (srcWidth > srcHeight) {
                inSampleSize = Math.round(srcHeight / height);
            } else {
                inSampleSize = Math.round(srcWidth / width);
            }
        }

        options.inJustDecodeBounds = false;
        options.inSampleSize = inSampleSize;

        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    /**
     * 获取缩放后的本地图片
     *
     * @param filePath 文件路径
     * @return
     */
    private static Bitmap readBitmapFromAssetsFile(Context context, String filePath) {
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try {
            InputStream is = am.open(filePath);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * 保存文件
     *
     * @param filePath
     * @param b
     * @param quality
     */
    private static void writeBitmapToFile(String filePath, Bitmap b, int quality) {
        try {
            File desFile = new File(filePath);
            FileOutputStream fos = new FileOutputStream(desFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            b.compress(Bitmap.CompressFormat.PNG, quality, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param key
     * @return
     */
    public static Bitmap getBitmapFromCache(String key) {
        return mImageCache.get(key);
    }

    private interface LoadImgUrlCallBack {
        String getImageUrl();
    }

    public interface ImageLoadCallBack {
        void callback(Bitmap bitmap);
    }
}
