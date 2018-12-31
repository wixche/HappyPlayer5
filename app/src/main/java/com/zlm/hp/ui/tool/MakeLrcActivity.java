package com.zlm.hp.ui.tool;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.zlm.hp.adapter.ViewPageFragmentAdapter;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.entity.tool.MakeInfo;
import com.zlm.hp.fragment.tool.EditLrcFragment;
import com.zlm.hp.fragment.tool.MakeLrcFragment;
import com.zlm.hp.fragment.tool.PreviewLrcFragment;
import com.zlm.hp.lyrics.LyricsReader;
import com.zlm.hp.lyrics.formats.LyricsFileWriter;
import com.zlm.hp.lyrics.model.LyricsInfo;
import com.zlm.hp.lyrics.model.LyricsLineInfo;
import com.zlm.hp.lyrics.utils.FileUtils;
import com.zlm.hp.lyrics.utils.LyricsIOUtils;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.ui.BaseActivity;
import com.zlm.hp.ui.R;
import com.zlm.hp.util.AndroidBug5497WorkaroundUtils;
import com.zlm.hp.util.ToastUtil;
import com.zlm.hp.widget.CustomViewPager;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * @Description: 制作歌词界面
 * @author: zhangliangming
 * @date: 2018-12-30 23:16
 **/
public class MakeLrcActivity extends BaseActivity {

    /**
     *
     */
    private CustomViewPager mViewPager;

    /**
     * 编辑歌词
     */
    public static final int INDEX_EDITLRC = 0;
    /**
     * 制作歌词
     */
    public static final int INDEX_MAKELRC = 1;
    /**
     * 制作歌词返回
     */
    public static final int INDEX_MAKELRC_BACK = 2;
    /**
     * 预览歌词
     */
    public static final int INDEX_PRELRC = 3;

    /**
     * 编辑界面
     */
    private EditLrcFragment mEditLrcFragment;
    /**
     * 敲打节奏
     */
    private MakeLrcFragment mMakeLrcFragment;

    /**
     * 歌词预览
     */
    private PreviewLrcFragment mPreviewLrcFragment;

    /**
     * 事件回调
     */
    private MakeLrcListener mMakeLrcListener;
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

    @Override
    protected int setContentLayoutResID() {
        return R.layout.activity_make_lrc;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {

        mMakeLrcListener = new MakeLrcListener() {
            @Override
            public void closeView() {
                close();
            }

            @Override
            public void openView(int index) {
                openFragmentView(index);
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

        //编辑歌词界面
        mEditLrcFragment = EditLrcFragment.newInstance();
        mEditLrcFragment.setMakeLrcListener(mMakeLrcListener);
        fragments.add(mEditLrcFragment);

        //敲打节奏

        mMakeLrcFragment = MakeLrcFragment.newInstance();
        mMakeLrcFragment.setMakeLrcListener(mMakeLrcListener);
        fragments.add(mMakeLrcFragment);

        //预览歌词界面
        mPreviewLrcFragment = PreviewLrcFragment.newInstance();
        mPreviewLrcFragment.setMakeLrcListener(mMakeLrcListener);
        fragments.add(mPreviewLrcFragment);

        //添加界面
        ViewPageFragmentAdapter adapter = new ViewPageFragmentAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(fragments.size());
        mViewPager.setScanScroll(false);

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
     * 打开视图
     *
     * @param index
     */
    private void openFragmentView(int index) {
        switch (index) {
            case INDEX_EDITLRC:
                mViewPager.setCurrentItem(0);
                break;
            case INDEX_MAKELRC:

                mMakeLrcFragment.setLrcText(mEditLrcFragment.getLrcText());
                mMakeLrcFragment.setAudioInfo(mAudioInfo);
                mViewPager.setCurrentItem(1);

                break;

            case INDEX_MAKELRC_BACK:
                mViewPager.setCurrentItem(1);
                break;

            case INDEX_PRELRC:

                LyricsInfo lyricsInfo = mMakeLrcFragment.getLyricsInfo();
                mPreviewLrcFragment.setAudioInfo(mAudioInfo);
                mPreviewLrcFragment.setLyricsInfo(lyricsInfo);
                mViewPager.setCurrentItem(2);

                break;


        }
    }

    private void initData() {
        //获取制作歌词数据
        mMakeInfo = getIntent().getParcelableExtra(MakeInfo.DATA_KEY);
        mAudioInfo = mMakeInfo.getAudioInfo();
        mLrcFilePath = mMakeInfo.getLrcFilePath();

        if (!TextUtils.isEmpty(mLrcFilePath)) {
            //加载默认歌词
            mWorkerHandler.sendEmptyMessage(MESSAGE_WHAT_LOADDEFLRC);
        }
    }

    @Override
    protected void handleUIMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_WHAT_LOADDEFLRC:

                String lrcCom = (String) msg.obj;
                mEditLrcFragment.setLrcText(lrcCom);
                mEditLrcFragment.setAudioInfo(mAudioInfo);


                break;
        }
    }

    @Override
    protected void handleWorkerMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_WHAT_LOADDEFLRC:
                File lrcFile = new File(mLrcFilePath);
                if (lrcFile.exists()) {
                    LyricsReader lyricsReader = new LyricsReader();
                    lyricsReader.loadLrc(lrcFile);
                    StringBuilder lrcComSB = new StringBuilder();
                    //获取歌词
                    TreeMap<Integer, LyricsLineInfo> lrcInfos = lyricsReader.getLrcLineInfos();
                    for (int i = 0; i < lrcInfos.size(); i++) {
                        LyricsLineInfo lyricsLineInfo = lrcInfos.get(i);
                        lrcComSB.append(lyricsLineInfo.getLineLyrics() + "\n");
                    }
                    if (lrcComSB.length() > 0) {

                        Message tempMsg = Message.obtain();
                        tempMsg.what = MESSAGE_WHAT_LOADDEFLRC;
                        tempMsg.obj = lrcComSB.toString();
                        mUIHandler.sendMessage(tempMsg);

                    }
                }
                break;
        }
    }

    @Override
    public void finish() {
        if (mEditLrcFragment != null) {
            mEditLrcFragment.onDestroyView();
        }

        if (mMakeLrcFragment != null) {
            mMakeLrcFragment.onDestroyView();
        }

        if (mPreviewLrcFragment != null) {
            mPreviewLrcFragment.onDestroyView();
        }
        super.finish();
    }

    @Override
    public void onBackPressed() {

    }

    /**
     * 制作歌词回调事件
     */
    public interface MakeLrcListener {
        void closeView();

        void openView(int index);

        /**
         * 保存歌词数据
         *
         * @param lyricsInfo
         */
        void saveLrcData(LyricsInfo lyricsInfo);
    }
}
