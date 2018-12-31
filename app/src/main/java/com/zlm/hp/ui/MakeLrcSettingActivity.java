package com.zlm.hp.ui;

import android.content.Intent;
import android.os.Message;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.zlm.hp.entity.tool.MakeInfo;
import com.zlm.hp.lyrics.LyricsReader;
import com.zlm.hp.ui.tool.MakeLrcActivity;
import com.zlm.hp.ui.tool.MakeTranslateLrcActivity;
import com.zlm.hp.ui.tool.MakeTransliterationLrcActivity;
import com.zlm.hp.util.ToastUtil;
import com.zlm.libs.widget.SwipeBackLayout;

import java.io.File;

/**
 * @Description: 制作歌词设置页面
 * @author: zhangliangming
 * @date: 2018-12-30 22:23
 **/
public class MakeLrcSettingActivity extends BaseActivity {
    /**
     *
     */
    private SwipeBackLayout mSwipeBackLayout;

    /**
     * 制作歌词按钮
     */
    private Button mMakeLrcBtn;

    /**
     * 制作翻译歌词按钮
     */
    private Button mMakeTranslateLrcBtn;


    /**
     * 制作音译歌词按钮
     */
    private Button mMakeTransliterationLrcBtn;

    /**
     * 制作歌词信息
     */
    private MakeInfo mMakeInfo;

    private LyricsReader mLyricsReader;

    /**
     * 加载歌词
     */
    private final int MESSAGE_WHAT_LOADLRC = 0;


    @Override
    protected int setContentLayoutResID() {
        return R.layout.activity_make_lrc_setting;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        //
        mSwipeBackLayout = findViewById(R.id.swipeback_layout);
        mSwipeBackLayout.setSwipeBackLayoutListener(new SwipeBackLayout.SwipeBackLayoutListener() {

            @Override
            public void finishActivity() {
                finish();
                overridePendingTransition(0, 0);
            }
        });
        TextView titleView = findViewById(R.id.title);
        titleView.setText(getString(R.string.make_lrc_text));

        //获取制作歌词数据
        mMakeInfo = getIntent().getParcelableExtra(MakeInfo.DATA_KEY);

        //返回
        ImageView backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwipeBackLayout.closeView();
            }
        });
        //制作歌词按钮
        mMakeLrcBtn = findViewById(R.id.makeLrcBtn);
        mMakeLrcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //打开歌词制作界面
                Intent lrcMakeIntent = new Intent(MakeLrcSettingActivity.this,
                        MakeLrcActivity.class);
                lrcMakeIntent.putExtra(MakeInfo.DATA_KEY, mMakeInfo);
                startActivity(lrcMakeIntent);
                overridePendingTransition(R.anim.in_from_bottom, 0);
                finish();
            }
        });

        //制作翻译歌词按钮
        mMakeTranslateLrcBtn = findViewById(R.id.makeTranslateLrcBtn);
        mMakeTranslateLrcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLyricsReader == null || mLyricsReader.getLrcLineInfos() == null || mLyricsReader.getLrcLineInfos().size() == 0) {
                    //没有默认歌词，不可制作
                    ToastUtil.showTextToast(mContext, getString(R.string.def_lrc_null));
                } else {
                    //打开歌词制作界面
                    Intent lrcMakeIntent = new Intent(MakeLrcSettingActivity.this,
                            MakeTranslateLrcActivity.class);
                    lrcMakeIntent.putExtra(MakeInfo.DATA_KEY, mMakeInfo);
                    startActivity(lrcMakeIntent);
                    overridePendingTransition(R.anim.in_from_bottom, 0);
                    finish();
                }
            }
        });

        //制作音译歌词按钮
        mMakeTransliterationLrcBtn = findViewById(R.id.makeTransliterationLrcBtn);
        mMakeTransliterationLrcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLyricsReader == null || mLyricsReader.getLrcLineInfos() == null || mLyricsReader.getLrcLineInfos().size() == 0) {
                    //没有默认歌词，不可制作
                    ToastUtil.showTextToast(mContext, getString(R.string.def_lrc_null));
                } else {
                    //打开歌词制作界面
                    Intent lrcMakeIntent = new Intent(MakeLrcSettingActivity.this,
                            MakeTransliterationLrcActivity.class);
                    lrcMakeIntent.putExtra(MakeInfo.DATA_KEY, mMakeInfo);
                    startActivity(lrcMakeIntent);
                    overridePendingTransition(R.anim.in_from_bottom, 0);
                    finish();
                }
            }
        });
        mWorkerHandler.sendEmptyMessage(MESSAGE_WHAT_LOADLRC);
    }

    @Override
    protected void handleUIMessage(Message msg) {

    }

    @Override
    protected void handleWorkerMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_WHAT_LOADLRC:

                String lrcFilePath = mMakeInfo.getLrcFilePath();
                if (!TextUtils.isEmpty(lrcFilePath)) {
                    File lrcFile = new File(lrcFilePath);
                    if (lrcFile != null && lrcFile.exists()) {
                        mLyricsReader = new LyricsReader();
                        mLyricsReader.loadLrc(lrcFile);
                    }

                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        mSwipeBackLayout.closeView();
    }
}
