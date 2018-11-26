package com.zlm.hp.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;

import com.zlm.hp.async.AsyncHandlerTask;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.db.util.SingerInfoDB;
import com.zlm.hp.entity.SingerInfo;
import com.zlm.hp.http.APIHttpClient;
import com.zlm.hp.http.HttpClient;
import com.zlm.hp.http.HttpReturnResult;
import com.zlm.hp.ui.R;
import com.zlm.hp.widget.TransitionImageView;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
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
     * 加载通知栏歌手头像
     *
     * @param context
     * @param singerName
     * @param asyncHandlerTask
     * @return
     */
    public static Bitmap getNotifiIcon(Context context, String singerName, int width, int height, AsyncHandlerTask asyncHandlerTask) {
        //多个歌手，则取第一个歌手头像
        String regex = "、";
        String searchSingerName = singerName;
        if (singerName.contains(regex)) {
            String reg = "\\s*、\\s*";
            String[] temp = singerName.split(reg);
            if (temp.length > 0) {
                searchSingerName = temp[0];
            }
        }

        String filePath = ResourceUtil.getFilePath(context, ResourceConstants.PATH_SINGER, searchSingerName + File.separator + searchSingerName + ".jpg");
        String key = filePath.hashCode() + "";

        Bitmap bitmap = null;
        if (mImageCache.get(key) != null) {
            bitmap = mImageCache.get(key);
        }
        //从本地文件中获取
        if (bitmap == null && filePath != null) {
            bitmap = readBitmapFromFile(filePath, width, height);
        }
        if (mImageCache.get(key) == null && bitmap != null) {
            mImageCache.put(key, bitmap);
        }
        return bitmap;
    }

    /**
     * 加载歌手头像
     *
     * @param context
     * @param imageView
     */
    public static void loadSingerImage(final Context context, ImageView imageView, final String singerName, final boolean askWifi, int width, int height, AsyncHandlerTask asyncHandlerTask, ImageLoadCallBack imageLoadCallBack) {
        //多个歌手，则取第一个歌手头像
        String regex = "、";
        String searchSingerName = singerName;
        if (singerName.contains(regex)) {
            String reg = "\\s*、\\s*";
            String[] temp = singerName.split(reg);
            if (temp.length > 0) {
                searchSingerName = temp[0];
            }
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
        loadImage(context, R.mipmap.bpz, filePath, null, askWifi, imageView, width, height, asyncHandlerTask, loadImgUrlCallBack, imageLoadCallBack);
    }

    /**
     * 加载歌手写真
     *
     * @param context
     * @param filePath
     * @param imageUrl
     * @param imageView
     * @param asyncHandlerTask
     */
    public static void loadSingerPic(final Context context, final String filePath, final String imageUrl, final boolean askWifi, final ImageView imageView, final int width, final int height, AsyncHandlerTask asyncHandlerTask, final ImageLoadCallBack imageLoadCallBack) {
        loadImage(context, R.mipmap.picture_manager_default, filePath, imageUrl, askWifi, imageView, width, height, asyncHandlerTask, null, imageLoadCallBack);
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
        loadImage(context, R.mipmap.bpz, filePath, imageUrl, askWifi, imageView, width, height, asyncHandlerTask, null, imageLoadCallBack);
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
    private static void loadImage(final Context context, int resourceId, final String filePath, final String imageUrl, final boolean askWifi, final ImageView imageView, final int width, final int height, AsyncHandlerTask asyncHandlerTask, final LoadImgUrlCallBack loadImgUrlCallBack, final ImageLoadCallBack imageLoadCallBack) {
        final String key = filePath.hashCode() + "";
        //如果当前的图片与上一次一样，则不操作
        if (imageView.getTag() != null && imageView.getTag().equals(key)) {
            if (imageLoadCallBack != null) {
                Bitmap result = null;
                if (mImageCache.get(key) != null) {
                    result = mImageCache.get(key);
                }
                //从本地文件中获取
                if (result == null && filePath != null) {
                    result = readBitmapFromFile(filePath, width, height);
                }
                if (mImageCache.get(key) == null && result != null) {
                    mImageCache.put(key, result);
                }
                imageLoadCallBack.callback(result);
            }
            return;
        }

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
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

        //网络获取歌手图片
        final String[] finalSingerNameArray = singerNameArray;
        asyncHandlerTask.execute(new AsyncHandlerTask.Task<Void>() {
            @Override
            protected Void doInBackground() {

                List<SingerInfo> returnResult = new ArrayList<SingerInfo>();
                List<List<SingerInfo>> tempReturnResult = new ArrayList<List<SingerInfo>>();
                List<String> singerNames = new ArrayList<String>();
                for (int i = 0; i < finalSingerNameArray.length; i++) {
                    String singerName = finalSingerNameArray[i];
                    //                    //数据库中获取图片
                    List<SingerInfo> dbListResult = SingerInfoDB.getAllSingerImage(context, singerName);
                    if (dbListResult != null && dbListResult.size() > 0) {
                        singerNames.add(singerName);
                        tempReturnResult.add(dbListResult);

                        continue;
                    }

                    APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
                    HttpReturnResult httpReturnResult = apiHttpClient.getSingerPicList(context, singerName, askWifi);
                    if (httpReturnResult.isSuccessful()) {
                        Map<String, Object> mapResult = (Map<String, Object>) httpReturnResult.getResult();
                        List<SingerInfo> lists = (List<SingerInfo>) mapResult.get("rows");
                        List<SingerInfo> listResult = new ArrayList<SingerInfo>();
                        if (lists != null) {
                            int maxSize = 3;
                            int size = lists.size() > maxSize ? maxSize : lists.size();
                            if (size > 0) {
                                for (int k = 0; k < size; k++) {
                                    SingerInfo singerInfo = lists.get(k);

                                    singerInfo.setCreateTime(DateUtil.parseDateToString(new Date()));
                                    SingerInfoDB.add(context, singerInfo);

                                    listResult.add(singerInfo);
                                }
                                singerNames.add(singerName);
                                tempReturnResult.add(listResult);
                            }
                        }
                    }
                }

                ///
                addAndSortData(returnResult, tempReturnResult, singerNames);

                for (int j = 0; j < returnResult.size(); j++) {
                    SingerInfo singerInfo = returnResult.get(j);
                    String imageUrl = singerInfo.getImageUrl();
                    ZLog.d(new CodeLineUtil().getCodeLineInfo(), "loadSingerImage singerName ->" + singerInfo.getSingerName());
                    ZLog.d(new CodeLineUtil().getCodeLineInfo(), "loadSingerImage imageUrl ->" + singerInfo.getImageUrl());
                    ImageUtil.loadSingerImage(context, asyncHandlerTask, singerInfo.getSingerName(), imageUrl, askWifi);
                }

                startSingerImage(singerImageView, returnResult);
                return null;
            }
        });

    }

    /**
     * 添加和排序数据
     *
     * @param returnResult
     * @param tempReturnResult
     * @param singerNames
     */
    private static void addAndSortData(List<SingerInfo> returnResult, List<List<SingerInfo>> tempReturnResult, List<String> singerNames) {
        for (int i = 0; i < singerNames.size(); i++) {
            if (i == 0) {
                returnResult.addAll(tempReturnResult.get(i));
            } else {
                String tag = singerNames.get(i - 1);
                List<SingerInfo> tempList = tempReturnResult.get(i);

                boolean flag = tempList.size() - returnResult.size() > 0 ? true : false;
                int minSize = tempList.size() > returnResult.size() ? returnResult.size() : tempList.size();
                int modSize = Math.abs(tempList.size() - returnResult.size());
                int z = 0;
                for (int j = 0; j < minSize; j++) {
                    for (; z < returnResult.size(); z++) {
                        SingerInfo temp = returnResult.get(z);
                        if (temp.getSingerName().equals(tag)) {
                            returnResult.add(++z, tempList.get(j));
                            break;
                        }
                    }
                }
                if (flag) {
                    for (int k = modSize; k < tempList.size(); k++) {
                        returnResult.add(tempList.get(k));
                    }
                }
            }
        }
    }

    /**
     * @param singerImageView
     * @param returnResult
     */
    private static void startSingerImage(TransitionImageView
                                                 singerImageView, List<SingerInfo> returnResult) {
        singerImageView.initData(returnResult);
    }

    /**
     * 获取歌手写真图片
     *
     * @param context
     * @param asyncHandlerTask
     * @param singerName
     * @return
     */
    public static void loadSingerImage(final Context context, AsyncHandlerTask
            asyncHandlerTask, String singerName, final String imageUrl, final boolean askWifi) {
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
    private static Bitmap loadImageFormCache(Context context, String filePath, String
            imageUrl, String key, int width, int height, boolean askWifi, LoadImgUrlCallBack
                                                     loadImgUrlCallBack) {
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
    private static Bitmap loadImageFormFile(Context context, String filePath, String imageUrl,
                                            int width, int height, boolean askWifi, LoadImgUrlCallBack loadImgUrlCallBack) {
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
    private static Bitmap loadImageFormUrl(Context context, String imageUrl, int width,
                                           int height, boolean askWifi, LoadImgUrlCallBack loadImgUrlCallBack) {
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
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;

            /** 这里是获取手机屏幕的分辨率用来处理 图片 溢出问题的。begin */

            int displaypixels = width / 2 * height / 2;

            options.inSampleSize = computeSampleSize(options, -1, displaypixels);
            options.inJustDecodeBounds = false;
            try {
                Bitmap bmp = BitmapFactory.decodeFile(filePath, options);
                return bmp;
            } catch (OutOfMemoryError err) {
                err.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        try {
            int displaypixels = width / 2 * height / 2;
            byte[] bytes = getBytes(ins);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            // 这3句是处理图片溢出的begin( 如果不需要处理溢出直接 opts.inSampleSize=1;)
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
            opts.inSampleSize = computeSampleSize(opts, -1, displaypixels);
            // end
            opts.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory
                    .decodeByteArray(bytes, 0, bytes.length, opts);
            ins.close();

            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
        try {

            int displaypixels = width / 2 * height / 2;
            BitmapFactory.Options opts = new BitmapFactory.Options();
            // 这3句是处理图片溢出的begin( 如果不需要处理溢出直接 opts.inSampleSize=1;)
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, opts);
            opts.inSampleSize = computeSampleSize(opts, -1, displaypixels);
            // end
            opts.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory
                    .decodeByteArray(data, 0, data.length, opts);

            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private static byte[] getBytes(InputStream input) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        copy(input, result);
        result.close();
        return result.toByteArray();
    }

    private static void copy(InputStream input, OutputStream output) throws IOException {
        copy(input, output, 2048);
    }

    private static void copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        byte[] buf = new byte[bufferSize];

        for (int bytesRead = input.read(buf); bytesRead != -1; bytesRead = input.read(buf)) {
            output.write(buf, 0, bytesRead);
        }

        output.flush();
    }

    private static int computeSampleSize(BitmapFactory.Options options,
                                         int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
                .sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
                Math.floor(w / minSideLength), Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }
        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
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
            b.compress(Bitmap.CompressFormat.JPEG, quality, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取转换颜色的图标
     *
     * @param imageView
     * @param resourceId     图片资源id
     * @param translateColor 需要转换成的颜色
     * @return
     */
    public static void getTranslateColorImg(final Context context, final ImageView imageView, final int resourceId, final int translateColor) {

        final String key = resourceId + "";

        new AsyncTask<String, Integer, Bitmap>() {

            @Override
            protected Bitmap doInBackground(String... params) {

                if (mImageCache.get(key) != null) {
                    return mImageCache.get(key);
                }

                Bitmap baseBitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);

                Bitmap defBitmap = Bitmap.createBitmap(baseBitmap.getWidth(),
                        baseBitmap.getHeight(), baseBitmap.getConfig());
                Canvas pCanvas = new Canvas(defBitmap);
                Paint paint = new Paint();
                paint.setDither(true);
                paint.setAntiAlias(true);

                float progressR = Color.red(translateColor) / 255f;
                float progressG = Color.green(translateColor) / 255f;
                float progressB = Color.blue(translateColor) / 255f;
                float progressA = Color.alpha(translateColor) / 255f;

                // 根据SeekBar定义RGBA的矩阵
                float[] src = new float[]{progressR, 0, 0, 0, 0, 0, progressG, 0,
                        0, 0, 0, 0, progressB, 0, 0, 0, 0, 0, progressA, 0};
                // 定义ColorMatrix，并指定RGBA矩阵
                ColorMatrix colorMatrix = new ColorMatrix();
                colorMatrix.set(src);
                // 设置Paint的颜色
                paint.setColorFilter(new ColorMatrixColorFilter(src));
                // 通过指定了RGBA矩阵的Paint把原图画到空白图片上
                pCanvas.drawBitmap(baseBitmap, new Matrix(), paint);


                return defBitmap;
            }

            @SuppressLint("NewApi")
            @Override
            protected void onPostExecute(Bitmap result) {

                if (result != null) {
                    imageView.setImageDrawable(new BitmapDrawable(result));

                    if (mImageCache.get(key) == null) {
                        mImageCache.put(key, result);
                    }
                }
            }
        }.execute("");
    }

    /**
     * @param key
     * @return
     */
    public static Bitmap getBitmapFromCache(String key) {
        return mImageCache.get(key);
    }

    /**
     * @param key
     */
    public static void remove(String key) {
        if (mImageCache != null) {
            mImageCache.remove(key);
        }
    }

    public static void release() {
        if (mImageCache != null) {
            mImageCache = getImageCache();
        }
    }

    private interface LoadImgUrlCallBack {
        String getImageUrl();
    }

    public interface ImageLoadCallBack {
        void callback(Bitmap bitmap);
    }
}
