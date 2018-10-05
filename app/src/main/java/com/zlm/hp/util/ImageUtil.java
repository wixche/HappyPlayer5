package com.zlm.hp.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.LruCache;
import android.widget.ImageView;

import com.zlm.hp.async.AsyncHandlerTask;
import com.zlm.hp.http.HttpClient;
import com.zlm.hp.ui.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Description:
 * @author: zhangliangming
 * @date: 2018-10-05 12:46
 **/
public class ImageUtil {

    // 缓存
    public static LruCache<String, Bitmap> mImageCache = getImageCache();

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
     * 加载图片
     *
     * @param context
     * @param filePath
     * @param imageUrl
     * @param imageView
     * @param asyncHandlerTask
     */
    public static void loadImage(final Context context, final String filePath, final String imageUrl, final boolean askWifi, final ImageView imageView, final int width, final int height, AsyncHandlerTask asyncHandlerTask, final ImageLoadCallBack imageLoadCallBack) {
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
                return loadImageFormCache(context, filePath, imageUrl, key, width, height, askWifi);
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
     * @throws
     * @Description: 从缓存中获取
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-10-05 16:37
     */
    private static Bitmap loadImageFormCache(Context context, String filePath, String imageUrl, String key, int width, int height, boolean askWifi) {
        Bitmap bitmap = null;
        if (mImageCache.get(key) != null) {
            bitmap = mImageCache.get(key);
        }
        if (bitmap == null) {
            bitmap = loadImageFormFile(context, filePath, imageUrl, width, height, askWifi);
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
    private static Bitmap loadImageFormFile(Context context, String filePath, String imageUrl, int width, int height, boolean askWifi) {
        Bitmap bitmap = readBitmapFromFile(filePath, width, height);
        if (bitmap == null) {
            bitmap = loadImageFormUrl(context, imageUrl, width, height, askWifi);
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
    private static Bitmap loadImageFormUrl(Context context, String imageUrl, int width, int height, boolean askWifi) {
        if (askWifi) {
            if (!NetUtil.isWifiConnected(context)) {
                return null;
            }
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

    public interface ImageLoadCallBack {
        void callback(Bitmap bitmap);
    }
}
