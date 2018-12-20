package com.zlm.hp.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.dou361.dialogui.DialogUIUtils;
import com.dou361.dialogui.listener.DialogUIListener;
import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.constants.Constants;
import com.zlm.hp.db.util.AudioInfoDB;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.manager.ActivityManager;
import com.zlm.hp.util.ColorUtil;
import com.zlm.hp.util.IntentUtil;
import com.zlm.hp.util.MediaUtil;
import com.zlm.hp.util.PreferencesUtil;
import com.zlm.hp.widget.IconfontTextView;

import java.io.IOException;
import java.util.List;


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
     * 跳转到home
     */
    private final int GOHOME = 2;

    /**
     * 检测权限
     */
    private final int PERMISSION = 3;

    /**
     * 读文件权限通知
     */
    private final int REQUEST_CODE_READSTORAGE = 0;
    /**
     * 写文件权限通知
     */
    private final int REQUEST_CODE_WRITESTORAGE = 1;
    /**
     * 读写文件权限通知
     */
    private int[] REQUESTCODES = {REQUEST_CODE_READSTORAGE, REQUEST_CODE_WRITESTORAGE};

    /**
     * 权限列表
     */
    private String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    /**
     * 问候语
     */
    private MediaPlayer mMediaPlayer;

    /**
     * 图标图片
     */
    private IconfontTextView mIconImg;

    /**
     * 动画
     */
    private Animation mAnimation = null;


    @Override
    protected void preInitStatusBar() {
        setStatusBarViewBG(ColorUtil.parserColor(Color.BLACK, 0));
    }

    @Override
    protected int setContentLayoutResID() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {

        mIconImg = findViewById(R.id.icon_img);
        mAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.balloonscale);
        mIconImg.setAnimation(mAnimation);
        mAnimation.start();

        checkPermission();
    }

    /**
     * 权限检测
     */
    private void checkPermission() {
        for (int i = 0; i < PERMISSIONS.length; i++) {
            String permission = PERMISSIONS[i];
            int permissionCheck = PermissionChecker.checkSelfPermission(mContext, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                //有权限
                if (i == PERMISSIONS.length - 1)
                    mWorkerHandler.sendEmptyMessage(LOADTATA);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{permission}, REQUESTCODES[i]);
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_READSTORAGE:

                int readPermissionCheck = PermissionChecker.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE);
                if (readPermissionCheck == PackageManager.PERMISSION_GRANTED) {
                    checkPermission();
                } else {
                    alertNoStoragePermissionDialog();
                }
                break;
            case REQUEST_CODE_WRITESTORAGE:

                int writePermissionCheck = PermissionChecker.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (writePermissionCheck == PackageManager.PERMISSION_GRANTED) {
                    checkPermission();
                } else {
                    alertNoStoragePermissionDialog();
                }
                break;
        }
    }

    private void alertNoStoragePermissionDialog() {
        //弹出窗口显示

        String tipMsg = getString(R.string.storage_tip);
        DialogUIUtils.showMdAlert(this, getString(R.string.tip_title), tipMsg, new DialogUIListener() {
            @Override
            public void onPositive() {
                //跳转权限设置页面
                IntentUtil.gotoPermissionSetting(SplashActivity.this);
                mWorkerHandler.sendEmptyMessageDelayed(PERMISSION, 5 * 1000);
            }

            @Override
            public void onNegative() {
                ActivityManager.getInstance().exit();
            }
        }).setCancelable(true, false).show();
    }

    @Override
    protected void handleUIMessage(Message msg) {
        switch (msg.what) {
            case GOHOME:

                goHome();
                break;

        }
    }

    @Override
    protected void handleWorkerMessage(Message msg) {
        switch (msg.what) {
            case LOADTATA:
                loadData();
                break;
            case PERMISSION:
                checkPermission();
                break;
        }

    }

    /**
     * 初始化加载数据
     */
    private void loadData() {
        ConfigInfo configInfo = ConfigInfo.load();

        boolean isFrist = PreferencesUtil.getBoolean(getApplicationContext(), Constants.IS_FRIST_KEY, true);
        if (isFrist) {

            //1.扫描本地歌曲列表
            List<AudioInfo> audioInfos = MediaUtil.scanLocalMusic(getApplicationContext(), null);
            if (audioInfos != null && audioInfos.size() > 0) {
                AudioInfoDB.addAudioInfos(getApplicationContext(), audioInfos);
                List<AudioInfo> localAudioInfos = AudioInfoDB.getLocalAudios(getApplicationContext());
                if (localAudioInfos != null && localAudioInfos.size() > 0) {
                    AudioInfo audioInfo = localAudioInfos.get(0);
                    configInfo.setPlayHash(audioInfo.getHash());
                    configInfo.save();
                }
                configInfo.setAudioInfos(localAudioInfos);
            }

            PreferencesUtil.putBoolean(getApplicationContext(), Constants.IS_FRIST_KEY, false);
        }

        //2.加载基本数据
        if (configInfo.isSayHello()) {
            loadSplashMusic();
        } else {
            if (isFrist) {
                //第一次因为需要扫描歌曲，时间可小一点
                mUIHandler.sendEmptyMessageDelayed(GOHOME, 1000);
            } else {
                mUIHandler.sendEmptyMessageDelayed(GOHOME, 5000);
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
                    mUIHandler.sendEmptyMessageDelayed(GOHOME, 3000);
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
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

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
