package com.zlm.hp.ui.tool;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.entity.tool.MakeInfo;
import com.zlm.hp.lyrics.utils.FileUtils;
import com.zlm.hp.ui.BaseActivity;
import com.zlm.hp.ui.MakeLrcSettingActivity;
import com.zlm.hp.ui.R;
import com.zlm.hp.util.ResourceUtil;
import com.zlm.libs.widget.SwipeBackLayout;

import java.io.File;

/**
 * @Description: 歌词制作器
 * @author: zhangliangming
 * @date: 2019-01-01 5:40
 **/
public class LrcMakerActivity extends BaseActivity {

    /**
     *
     */
    private SwipeBackLayout mSwipeBackLayout;

    /**
     * 选择歌曲文件请求码
     */
    private final int SELECTAUDIOFILE = 0;
    /**
     * 选择歌曲文件请求码
     */
    private final int SELECTLRCFILE = 1;

    /**
     * 设置歌曲文件路径
     */
    private final int SETAUDIOFILEPATH = 0;

    /**
     * 设置歌词文件路径
     */
    private final int SETLRCFILEPATH = 1;

    /**
     * 歌曲路径tv
     */
    private TextView mAudioFilePathTv;

    /**
     * 歌词路径tv
     */
    private TextView mLrcFilePathTv;

    /**
     * 歌曲路径
     */
    private String mAudioFilePath;

    /**
     * 歌词路径
     */
    private String mLrcFilePath;

    @Override
    protected int setContentLayoutResID() {
        return R.layout.activity_lrc_maker;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        mSwipeBackLayout = findViewById(R.id.swipeback_layout);
        mSwipeBackLayout.setSwipeBackLayoutListener(new SwipeBackLayout.SwipeBackLayoutListener() {

            @Override
            public void finishActivity() {
                finish();
                overridePendingTransition(0, 0);
            }
        });

        TextView titleView = findViewById(R.id.title);
        titleView.setText("歌词制作器");

        //返回
        ImageView backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwipeBackLayout.closeView();
            }
        });


        //选择歌曲按钮
        Button selectAudioFile = findViewById(R.id.selectAudioFile);
        selectAudioFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent selectFileIntent = new Intent(LrcMakerActivity.this, FileManagerActivity.class);
                selectFileIntent.putExtra(FileManagerActivity.FILEFILTER_KEY, "mp3,ape,flac,wav");
                startActivityForResult(selectFileIntent, SELECTAUDIOFILE);
                //去掉动画
                overridePendingTransition(0, 0);


            }
        });
        mAudioFilePathTv = findViewById(R.id.audioFilePath);

        //制作歌词
        Button mMakeLrcBtn = findViewById(R.id.makeLrcBtn);
        mMakeLrcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //传递音频和歌词数据
                if (mAudioFilePath == null || mAudioFilePath.equals("")) {

                    Toast.makeText(getApplicationContext(), "请选择支持的音频文件！", Toast.LENGTH_SHORT).show();

                    return;
                }

                //获取制作歌词所需的音频信息
                MakeInfo makeInfo = new MakeInfo();
                AudioInfo temp = new AudioInfo();
                temp.setFilePath(mAudioFilePath);
                makeInfo.setAudioInfo(temp);
                //默认歌词路径
                if (!TextUtils.isEmpty(mLrcFilePath)) {
                    makeInfo.setLrcFilePath(mLrcFilePath);
                }

                File audioFile = new File(mAudioFilePath);
                //保存歌词路径
                String saveLrcFilePath = ResourceUtil.getFilePath(mContext, ResourceConstants.PATH_LYRICS, null) + File.separator + FileUtils.removeExt(audioFile.getName()) + ".hrc";
                makeInfo.setSaveLrcFilePath(saveLrcFilePath);


                //打开制作歌词设置页面
                Intent intent = new Intent(LrcMakerActivity.this, MakeLrcSettingActivity.class);
                intent.putExtra(MakeInfo.DATA_KEY, makeInfo);
                startActivity(intent);
                overridePendingTransition(0, 0);

            }
        });

        //选择歌词文件
        Button selectLrcFile = findViewById(R.id.selectLrcFile);
        selectLrcFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent selectFileIntent = new Intent(LrcMakerActivity.this, FileManagerActivity.class);
                selectFileIntent.putExtra(FileManagerActivity.FILEFILTER_KEY, "ksc,krc,hrc,lrc");
                startActivityForResult(selectFileIntent, SELECTLRCFILE);
                //去掉动画
                overridePendingTransition(0, 0);


            }
        });
        mLrcFilePathTv = findViewById(R.id.lrcFilePath);
    }

    @Override
    protected void handleUIMessage(Message msg) {
        switch (msg.what) {
            case SETAUDIOFILEPATH:

                if (mAudioFilePath != null && !mAudioFilePath.equals("")) {
                    mAudioFilePathTv.setText("歌曲文件路径：" + mAudioFilePath);
                } else {
                    mAudioFilePathTv.setText("歌曲文件路径：");
                }
                break;

            case SETLRCFILEPATH:

                if (mLrcFilePath != null && !mLrcFilePath.equals("")) {
                    mLrcFilePathTv.setText("歌词文件路径：" + mLrcFilePath);
                } else {
                    mLrcFilePathTv.setText("歌词文件路径：");
                }
                break;
        }
    }

    @Override
    protected void handleWorkerMessage(Message msg) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECTAUDIOFILE) {
            if (resultCode == Activity.RESULT_OK) {

                mAudioFilePath = data.getStringExtra("selectFilePath");
                if (mAudioFilePath != null && !mAudioFilePath.equals("")) {
                    String ext = FileUtils.getFileExt(mAudioFilePath);
                    if (!ext.equals("mp3") && !ext.equals("ape") && !ext.equals("flac") && !ext.equals("wav")) {
                        Toast.makeText(getApplicationContext(), "请选择支持的音频文件！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            } else {
                mAudioFilePath = null;
            }
            mUIHandler.sendEmptyMessage(SETAUDIOFILEPATH);
        } else {
            if (resultCode == Activity.RESULT_OK) {

                mLrcFilePath = data.getStringExtra("selectFilePath");
                if (mLrcFilePath != null && !mLrcFilePath.equals("")) {
                    String ext = FileUtils.getFileExt(mLrcFilePath);
                    if (!ext.equals("krc") && !ext.equals("hrc") && !ext.equals("ksc") && !ext.equals("lrc")) {
                        Toast.makeText(getApplicationContext(), "请选择支持的歌词文件！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            } else {
                mLrcFilePath = null;
            }
            mUIHandler.sendEmptyMessage(SETLRCFILEPATH);
        }
    }


    @Override
    public void onBackPressed() {
        mSwipeBackLayout.closeView();
    }
}
