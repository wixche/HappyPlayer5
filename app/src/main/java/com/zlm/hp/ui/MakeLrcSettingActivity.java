package com.zlm.hp.ui;

import android.content.Intent;
import android.os.Message;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.zlm.hp.entity.MakeInfo;
import com.zlm.libs.widget.SwipeBackLayout;

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

        //
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
                //去掉动画
                overridePendingTransition(0, 0);

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        finish();
                    }
                }.start();

            }
        });

        //制作翻译歌词按钮
        mMakeTranslateLrcBtn = findViewById(R.id.makeTranslateLrcBtn);
        mMakeTranslateLrcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //制作音译歌词按钮
        mMakeTransliterationLrcBtn = findViewById(R.id.makeTransliterationLrcBtn);
        mMakeTransliterationLrcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    protected void handleUIMessage(Message msg) {

    }

    @Override
    protected void handleWorkerMessage(Message msg) {

    }

    @Override
    public void onBackPressed() {
        mSwipeBackLayout.closeView();
    }
}
