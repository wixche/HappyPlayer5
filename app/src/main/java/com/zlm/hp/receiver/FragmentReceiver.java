package com.zlm.hp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;

import com.zlm.hp.constants.Constants;
import com.zlm.hp.entity.RankInfo;
import com.zlm.hp.entity.SpecialInfo;
import com.zlm.hp.fragment.SongFragment;


/**
 * @Description: fragment的广播
 * @param:
 * @return:
 * @throws
 * @author: zhangliangming
 * @date: 2018-10-04 15:16
 */
public class FragmentReceiver {
    /**
     * 打开单个排行的歌曲列表
     */
    public static final int ACTION_CODE_OPEN_RECOMMENDFRAGMENT = 0;

    /**
     * 打开单个歌单的歌曲列表
     */
    public static final int ACTION_CODE_OPEN_SPECIALFRAGMENT = 1;

    /**
     * 关闭
     */
    public static final int ACTION_CODE_CLOSE_FRAGMENT = 2;

    /**
     * 打开本地歌曲
     */
    public static final int ACTION_CODE_OPEN_LOCALFRAGMENT = 3;

    /**
     * 打开喜欢歌曲
     */
    public static final int ACTION_CODE_OPEN_LIKEFRAGMENT = 4;

    /**
     * 打开最近歌曲
     */
    public static final int ACTION_CODE_OPEN_RECENTFRAGMENT = 5;

    /**
     * 打开下载歌曲
     */
    public static final int ACTION_CODE_OPEN_DOWNLOADFRAGMENT = 6;

    /**
     * fragment的receiver的action
     */
    private static final String RECEIVER_ACTION = "com.zlm.hp.receiver.fragment.action";

    /**
     *
     */
    private static final String ACTION_CODE_KEY = "com.zlm.hp.receiver.fragment.action.code.key";

    private BroadcastReceiver mBroadcastReceiver;
    private IntentFilter mIntentFilter;
    private FragmentReceiverListener mFragmentReceiverListener;

    public FragmentReceiver(Context context) {
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(RECEIVER_ACTION);
    }

    /**
     * 注册广播
     *
     * @param context
     */
    public void registerReceiver(Context context) {

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (mFragmentReceiverListener != null) {
                    int code = intent.getIntExtra(ACTION_CODE_KEY, -1);
                    if (code != -1) {
                        mFragmentReceiverListener.onReceive(context, intent, code);
                    }
                }
            }
        };
        context.registerReceiver(mBroadcastReceiver, mIntentFilter, Constants.RECEIVER_PERMISSION, null);
    }

    /**
     * 发广播
     *
     * @param context
     * @param code
     * @param bundleKey
     * @param bundleValue
     */
    public static void sendReceiver(Context context, int code, String bundleKey, Bundle bundleValue) {
        Intent intent = new Intent(RECEIVER_ACTION);
        intent.putExtra(ACTION_CODE_KEY, code);
        if (!TextUtils.isEmpty(bundleKey) && bundleValue != null) {
            intent.putExtra(bundleKey, bundleValue);
        }
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.sendBroadcast(intent, Constants.RECEIVER_PERMISSION);
    }

    /**
     * 发打开歌单广播
     *
     * @param context
     */
    public static void sendSpecialFragmentReceiver(Context context, SpecialInfo specialInfo) {
        Bundle bundle = new Bundle();
        bundle.putInt(SongFragment.SONGTYPE_KEY, SongFragment.SONG_TYPE_SPECIAL);
        bundle.putParcelable(SongFragment.DATA_KEY, specialInfo);

        //打开歌单页面
      sendReceiver(context, FragmentReceiver.ACTION_CODE_OPEN_SPECIALFRAGMENT, SongFragment.ARGUMENTS_KEY, bundle);

    }

    /**
     * 发打开排行广播
     *
     * @param context
     */
    public static void sendRecommendFragmentReceiver(Context context, RankInfo rankInfo) {
        Bundle bundle = new Bundle();
        bundle.putInt(SongFragment.SONGTYPE_KEY, SongFragment.SONG_TYPE_RECOMMEND);
        bundle.putParcelable(SongFragment.DATA_KEY, rankInfo);
        sendReceiver(context, FragmentReceiver.ACTION_CODE_OPEN_RECOMMENDFRAGMENT, SongFragment.ARGUMENTS_KEY, bundle);
    }

    /**
     * 发打开本地歌曲广播
     *
     * @param context
     */
    public static void sendLocalFragmentReceiver(Context context, String title) {
        Bundle bundle = new Bundle();
        bundle.putInt(SongFragment.SONGTYPE_KEY, SongFragment.SONG_TYPE_LOCAL);
        bundle.putString(SongFragment.DATA_KEY, title);
        sendReceiver(context, FragmentReceiver.ACTION_CODE_OPEN_LOCALFRAGMENT, SongFragment.ARGUMENTS_KEY, bundle);
    }

    /**
     * 发打开喜欢歌曲广播
     *
     * @param context
     */
    public static void sendLikeFragmentReceiver(Context context, String title) {
        Bundle bundle = new Bundle();
        bundle.putInt(SongFragment.SONGTYPE_KEY, SongFragment.SONG_TYPE_LIKE);
        bundle.putString(SongFragment.DATA_KEY, title);
        sendReceiver(context, FragmentReceiver.ACTION_CODE_OPEN_LIKEFRAGMENT, SongFragment.ARGUMENTS_KEY, bundle);
    }

    /**
     * 发打开最近歌曲广播
     *
     * @param context
     */
    public static void sendRecentFragmentReceiver(Context context, String title) {
        Bundle bundle = new Bundle();
        bundle.putInt(SongFragment.SONGTYPE_KEY, SongFragment.SONG_TYPE_RECENT);
        bundle.putString(SongFragment.DATA_KEY, title);
        sendReceiver(context, FragmentReceiver.ACTION_CODE_OPEN_RECENTFRAGMENT, SongFragment.ARGUMENTS_KEY, bundle);
    }

    /**
     * 发打开下载歌曲广播
     *
     * @param context
     */
    public static void sendDownloadFragmentReceiver(Context context) {
        Bundle bundle = new Bundle();
        sendReceiver(context, FragmentReceiver.ACTION_CODE_OPEN_DOWNLOADFRAGMENT, SongFragment.ARGUMENTS_KEY, bundle);
    }


    /**
     * 取消注册广播
     */
    public void unregisterReceiver(Context context) {
        if (mBroadcastReceiver != null) {
            context.unregisterReceiver(mBroadcastReceiver);
        }
    }

    public interface FragmentReceiverListener {
        void onReceive(Context context, Intent intent, int code);
    }

    public void setReceiverListener(FragmentReceiverListener mFragmentReceiverListener) {
        this.mFragmentReceiverListener = mFragmentReceiverListener;
    }
}
