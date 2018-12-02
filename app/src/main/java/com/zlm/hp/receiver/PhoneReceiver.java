package com.zlm.hp.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.media.session.MediaSessionCompat;

/**
 * 耳机线控 5.0及以上
 * Created by zhangliangming on 2017/9/1.
 */

public class PhoneReceiver {

    private Context mContext;
    private final String TAG = this.getClass().getName();
    private MediaSessionCompat mMediaSession;


    public PhoneReceiver(Context context) {
        mContext = context;

        //        第二个参数 tag: 这个是用于调试用的,随便填写即可
        mMediaSession = new MediaSessionCompat(context, TAG);
        //指明支持的按键信息类型
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
    }

    /**
     * 注册广播
     */
    public void registerReceiver(Context context) {
        mMediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                new PhoneV4Receiver(mContext).onReceive(mContext, mediaButtonEvent);
                return super.onMediaButtonEvent(mediaButtonEvent);
            }
        });
        mMediaSession.setActive(true);
    }

    /**
     * 取消注册广播
     */
    public void unregisterReceiver(Context context) {
        if (mMediaSession != null) {
            mMediaSession.setCallback(null);
            mMediaSession.setActive(false);
            mMediaSession.release();
        }
    }
}
