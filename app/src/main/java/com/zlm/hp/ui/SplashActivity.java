package com.zlm.hp.ui;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Message;
import android.os.Bundle;

import com.zlm.hp.constants.Constants;
import com.zlm.hp.model.ConfigInfo;
import com.zlm.hp.util.ColorUtil;
import com.zlm.hp.util.PreferencesUtil;

import java.io.IOException;


/**
 * @Description: 启动页
 * @author: zhangliangming
 * @date: 2018-08-04 18:55
 **/
public class SplashActivity extends BaseActivity {

    /**
     * 加载数据
     */
    private final int LOADTATA = 0;

    /**
     * 问候语
     */
    private MediaPlayer mMediaPlayer;


    @Override
    protected void preInitStatusBar() {
        setStatusBarViewBG(ColorUtil.parserColor(Color.BLACK, 30));
    }

    @Override
    protected int setContentLayoutResID() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        mWorkerHandler.sendEmptyMessage(LOADTATA);
    }

    @Override
    protected void handleUIMessage(Message msg) {
        goHome();
    }

    @Override
    protected void handleWorkerMessage(Message msg) {
        switch (msg.what) {
            case LOADTATA:
                loadData();
                break;
        }

    }

    /**
     * 初始化加载数据
     */
    private void loadData() {
        boolean isFrist = PreferencesUtil.getBoolean(getApplicationContext(), Constants.IS_FRIST_KEY, true);
        if (isFrist) {
            //1.扫描本地歌曲列表
            PreferencesUtil.putBoolean(getApplication(), Constants.IS_FRIST_KEY, true);
        }
        //2.加载基本数据
        ConfigInfo configInfo = ConfigInfo.load();
        if (configInfo.isSayHello()) {
            loadSplashMusic();
        } else {
            if (isFrist) {
                //第一次因为需要扫描歌曲，时间可小一点
                mUIHandler.sendEmptyMessageDelayed(0, 1000);
            } else {
                mUIHandler.sendEmptyMessageDelayed(0, 5000);
            }
        }
    }

    /**
     * 加载启动页面的问候语
     */
    protected void loadSplashMusic() {
        AssetManager assetManager = getAssets();
        AssetFileDescriptor fileDescriptor;
        try {
            fileDescriptor = assetManager.openFd("audio/hellolele.mp3");
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),
                    fileDescriptor.getStartOffset(),
                    fileDescriptor.getLength());
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //播放完成后跳转
                    mUIHandler.sendEmptyMessageDelayed(0, 3000);
                }
            });
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转到主页面
     */
    private void goHome() {

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.zoomin, R.anim.zoomout);

        finish();
    }

    @Override
    public void finish() {
        //
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        super.finish();
    }
}
