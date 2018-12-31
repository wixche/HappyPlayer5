package com.zlm.hp.ui.tool;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlm.hp.adapter.ViewPageFragmentAdapter;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.entity.tool.MakeInfo;
import com.zlm.hp.fragment.tool.MakeExtraLrcFragment;
import com.zlm.hp.fragment.tool.PreviewLrcFragment;
import com.zlm.hp.lyrics.formats.LyricsFileWriter;
import com.zlm.hp.lyrics.model.LyricsInfo;
import com.zlm.hp.lyrics.model.make.MakeExtraLrcLineInfo;
import com.zlm.hp.lyrics.utils.ColorUtils;
import com.zlm.hp.lyrics.utils.FileUtils;
import com.zlm.hp.lyrics.utils.LyricsIOUtils;
import com.zlm.hp.lyrics.utils.StringUtils;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.ui.BaseActivity;
import com.zlm.hp.ui.R;
import com.zlm.hp.util.AndroidBug5497WorkaroundUtils;
import com.zlm.hp.util.ToastUtil;
import com.zlm.hp.widget.CustomViewPager;

import java.util.ArrayList;

/**
 * @Description: 制作翻译歌词
 * @author: zhangliangming
 * @date: 2019-01-01 0:29
 **/
public class MakeTranslateLrcActivity extends BaseActivity {
    /**
     *
     */
    private CustomViewPager mViewPager;

    /**
     * 翻译歌词视图
     */
    private MakeExtraLrcFragment mMakeExtraLrcFragment;

    /**
     * 歌词预览
     */
    private PreviewLrcFragment mPreviewLrcFragment;

    /**
     * 事件回调
     */
    private MakeLrcActivity.MakeLrcListener mMakeLrcListener;

    /**
     * 制作信息
     */
    private MakeInfo mMakeInfo;

    /**
     * 歌曲信息
     */
    private AudioInfo mAudioInfo;

    /**
     * 歌词文件路径
     */
    private String mLrcFilePath;

    /**
     * 加载默认歌词
     */
    private final int MESSAGE_WHAT_LOADDEFLRC = 0;

    /////////////////////////////////额外歌词///////////////////////////////////
    /**
     * 制作额外歌词事件
     */
    private MakeExtraLrcFragment.ExtraItemEvent mExtraItemEvent;
    private RelativeLayout mExtraLrcLL;
    private TextView mExtraLrcIndexTv;
    private TextView mLineLyricsTv;
    private EditText mExtraLrcEt;
    private MakeExtraLrcLineInfo mMakeExtraLrcLineInfo;


    @Override
    protected int setContentLayoutResID() {
        return R.layout.activity_make_translate_lrc;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        mMakeLrcListener = new MakeLrcActivity.MakeLrcListener() {
            @Override
            public void closeView() {
                close();
            }

            @Override
            public void openView(int index) {
                switch (index) {
                    case 2:
                        mViewPager.setCurrentItem(0);
                        break;
                    case 3:

                        LyricsInfo lyricsInfo = mMakeExtraLrcFragment.getLyricsInfo();
                        mPreviewLrcFragment.setExtraLrcType(MakeExtraLrcFragment.EXTRA_LRC_TYPE_TRANSLATE);
                        mPreviewLrcFragment.setAudioInfo(mAudioInfo);
                        mPreviewLrcFragment.setLyricsInfo(lyricsInfo);

                        mViewPager.setCurrentItem(1);

                        break;
                }
            }

            @Override
            public void saveLrcData(LyricsInfo lyricsInfo) {
                String saveLrcFilePath = mMakeInfo.getSaveLrcFilePath();
                lyricsInfo.setLyricsFileExt(FileUtils.getFileExt(saveLrcFilePath));
                lyricsInfo.setBy(getString(R.string.def_artist));
                LyricsFileWriter
                        lyricsFileWriter = LyricsIOUtils.getLyricsFileWriter(saveLrcFilePath);
                boolean result = lyricsFileWriter.writer(lyricsInfo, saveLrcFilePath);
                if (result) {
                    //保存成功
                    ToastUtil.showTextToast(mContext, getString(R.string.make_lrc_success));
                    String hash = mAudioInfo.getHash();
                    if (!TextUtils.isEmpty(hash)) {
                        //发送制作歌词保存成功广播
                        AudioBroadcastReceiver.sendMakeLrcSuccessReceiver(mContext, hash);
                    }
                    close();
                } else {
                    //保存失败
                    ToastUtil.showTextToast(mContext, getString(R.string.make_lrc_error));
                }
            }
        };
        mViewPager = findViewById(R.id.viewpage);
        ArrayList<Fragment> fragments = new ArrayList<Fragment>();

        //翻译歌词
        mMakeExtraLrcFragment = MakeExtraLrcFragment.newInstance();
        mMakeExtraLrcFragment.setExtraLrcType(MakeExtraLrcFragment.EXTRA_LRC_TYPE_TRANSLATE);
        mMakeExtraLrcFragment.setMakeLrcListener(mMakeLrcListener);
        fragments.add(mMakeExtraLrcFragment);

        //预览歌词界面
        mPreviewLrcFragment = PreviewLrcFragment.newInstance();
        mPreviewLrcFragment.setMakeLrcListener(mMakeLrcListener);
        fragments.add(mPreviewLrcFragment);

        //添加界面
        ViewPageFragmentAdapter adapter = new ViewPageFragmentAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(fragments.size());
        mViewPager.setScanScroll(false);

        /**
         *
         */
        mExtraItemEvent = new MakeExtraLrcFragment.ExtraItemEvent() {
            @Override
            public void itemClick(int index) {
                extraItemClick(index);
            }
        };
        mMakeExtraLrcFragment.setExtraItemEvent(mExtraItemEvent);

        //翻译
        mExtraLrcLL = findViewById(R.id.extraLL);
        mExtraLrcLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveAndUpdate();

                if (mExtraLrcLL.getVisibility() != View.GONE) {
                    mExtraLrcLL.setVisibility(View.GONE);
                }
            }
        });
        mExtraLrcLL.setBackgroundColor(ColorUtils.parserColor(Color.BLACK, 50));
        mExtraLrcLL.setVisibility(View.GONE);
        mExtraLrcIndexTv = findViewById(R.id.extraLrcIndex);
        mLineLyricsTv = findViewById(R.id.lineLyrics);
        mExtraLrcEt = findViewById(R.id.extraLrcEt);

        Button preLineBtn = findViewById(R.id.preLineBtn);
        preLineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAndUpdate();
                extraItemClick(mMakeExtraLrcFragment.getPreIndex());
            }
        });

        Button nextLineBtn = findViewById(R.id.nextLineBtn);
        nextLineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAndUpdate();
                extraItemClick(mMakeExtraLrcFragment.getNextIndex());
            }
        });


        //修复输入法遮挡底部菜单的问题
        AndroidBug5497WorkaroundUtils.assistActivity(this);
        initData();
    }

    /**
     * 关闭
     */
    private void close() {
        overridePendingTransition(0, R.anim.out_to_bottom);
        finish();
    }

    /**
     * 更新
     */
    private void saveAndUpdate() {
        String extraLineLyrics = mExtraLrcEt.getText().toString();
        if (StringUtils.isBlank(extraLineLyrics)) {
            extraLineLyrics = "";
        }
        mMakeExtraLrcLineInfo.setExtraLineLyrics(extraLineLyrics);
        mMakeExtraLrcFragment.saveAndUpdate();
    }

    /**
     * 额外歌词列表item点击
     *
     * @param index
     */
    private void extraItemClick(final int index) {

        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... strings) {

                mMakeExtraLrcLineInfo = mMakeExtraLrcFragment.getMakeExtraLrcLineInfo(index);
                return null;
            }

            @Override
            protected void onPostExecute(String result) {

                showInputExtraLrcDialog(index);

                super.onPostExecute(result);
            }
        }.execute("");

    }

    /**
     * 显示输入额外歌词窗口
     *
     * @param index
     */
    private void showInputExtraLrcDialog(int index) {
        mExtraLrcIndexTv.setText(String.format("%0" + (mMakeExtraLrcFragment.getLrcDataSize() + "").length() + "d", (index + 1)));
        mLineLyricsTv.setText(mMakeExtraLrcLineInfo.getLyricsLineInfo().getLineLyrics());

        String extraLineLyrics = mMakeExtraLrcLineInfo.getExtraLineLyrics();
        if (StringUtils.isNotBlank(extraLineLyrics)) {
            mExtraLrcEt.setText(extraLineLyrics);
        } else {
            mExtraLrcEt.setText("");
        }

        mExtraLrcLL.setVisibility(View.VISIBLE);

    }

    @Override
    protected void handleUIMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_WHAT_LOADDEFLRC:

                mMakeExtraLrcFragment.setLrcFilePath(mLrcFilePath);
                mMakeExtraLrcFragment.setAudioInfo(mAudioInfo);

                break;
        }
    }

    @Override
    protected void handleWorkerMessage(Message msg) {
        switch (msg.what) {

        }

    }

    private void initData() {
        //获取制作歌词数据
        mMakeInfo = getIntent().getParcelableExtra(MakeInfo.DATA_KEY);
        mAudioInfo = mMakeInfo.getAudioInfo();
        mLrcFilePath = mMakeInfo.getLrcFilePath();

        if (!TextUtils.isEmpty(mLrcFilePath)) {
            //加载默认歌词
            mUIHandler.sendEmptyMessageDelayed(MESSAGE_WHAT_LOADDEFLRC, 500);
        }
    }

    @Override
    public void onBackPressed() {

    }

}
