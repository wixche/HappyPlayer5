package com.zlm.hp.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlm.hp.PageTransformer.ZoomOutPageTransformer;
import com.zlm.hp.adapter.SearchLrcFragmentAdapter;
import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.entity.LrcInfo;
import com.zlm.hp.fragment.LrcFragment;
import com.zlm.hp.http.APIHttpClient;
import com.zlm.hp.http.HttpReturnResult;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.util.HttpUtil;
import com.zlm.hp.util.ToastUtil;
import com.zlm.hp.widget.IconfontTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 歌词搜索
 */
public class SearchLrcActivity extends BaseActivity {


    /**
     *
     */
    public static final String AUDIO_DATA_KEY = "audioDataKey";

    /**
     * 歌曲名称
     */
    private EditText mSongNameEditText;
    private IconfontTextView mSongNameCleanImg;

    /**
     * 歌手名称
     */
    private EditText mSingerNameEditText;
    private IconfontTextView mSingerNameCleanImg;
    private TextView mSearchBtn;
    private TextView mSumTv;
    private TextView mCurIndexTv;
    private AudioInfo mAudioInfo;

    //
    private SearchLrcFragmentAdapter mAdapter;
    private ViewPager mViewPager;

    //
    /**
     * 加载中布局
     */
    private RelativeLayout mLoadingContainer;
    /**
     * 加载图标
     */
    private IconfontTextView mLoadImgView;

    /**
     * 旋转动画
     */
    private Animation rotateAnimation;

    /**
     * 内容布局
     */
    private RelativeLayout mContentContainer;


    /**
     * 加载数据
     */
    private final int MESSAGE_WHAT_LOAD_DATA = 0;

    /**
     * 搜索歌词
     */
    private final int MESSAGE_WHAT_SEARCN_LRC = 1;


    /**
     * 歌词数据
     */
    private List<LrcInfo> mSearchLrcDatas = new ArrayList<LrcInfo>();

    /**
     * 音频广播
     */
    private AudioBroadcastReceiver mAudioBroadcastReceiver;

    @Override
    protected int setContentLayoutResID() {
        return R.layout.activity_search_lrc;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        TextView titleView = findViewById(R.id.title);
        titleView.setText(getString(R.string.select_lrc_text));

        //关闭
        IconfontTextView backTextView = findViewById(R.id.closebtn);
        backTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //歌曲
        mSongNameEditText = findViewById(R.id.songNameEt);
        mSongNameCleanImg = findViewById(R.id.songclean_img);
        mSongNameCleanImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mSongNameEditText.setText("");
                mSongNameEditText.requestFocus();

            }
        });
        mSongNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    //关闭输入法
                    InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.hideSoftInputFromWindow(getCurrentFocus().getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);


                    doSearch();
                }
                return false;
            }
        });
        mSongNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String searchKey = mSongNameEditText.getText().toString();
                if (searchKey == null || searchKey.equals("")) {
                    if (mSongNameCleanImg.getVisibility() != View.INVISIBLE) {
                        mSongNameCleanImg.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (mSongNameCleanImg.getVisibility() != View.VISIBLE) {
                        mSongNameCleanImg.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        //歌手
        mSingerNameEditText = findViewById(R.id.singerNameEt);
        mSingerNameCleanImg = findViewById(R.id.singclean_img);
        mSingerNameCleanImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mSingerNameEditText.setText("");
                mSingerNameEditText.requestFocus();

            }
        });
        mSingerNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    doSearch();
                }
                return false;
            }
        });
        mSingerNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String searchKey = mSingerNameEditText.getText().toString();
                if (searchKey == null || searchKey.equals("")) {
                    if (mSingerNameCleanImg.getVisibility() != View.INVISIBLE) {
                        mSingerNameCleanImg.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (mSingerNameCleanImg.getVisibility() != View.VISIBLE) {
                        mSingerNameCleanImg.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        //搜索按钮
        mSearchBtn = findViewById(R.id.searchbtn);
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //关闭输入法
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(getCurrentFocus().getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);


                doSearch();
            }
        });

        //
        mSumTv = findViewById(R.id.sum);
        mSumTv.setText("0");
        mCurIndexTv = findViewById(R.id.cur_index);
        mCurIndexTv.setText("0");
        //
        mLoadingContainer = findViewById(R.id.loading);
        mLoadImgView = findViewById(R.id.load_img);
        rotateAnimation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.anim_rotate);
        rotateAnimation.setInterpolator(new LinearInterpolator());// 匀速
        mLoadImgView.startAnimation(rotateAnimation);
        //
        mContentContainer = findViewById(R.id.content);

        //
        mViewPager = findViewById(R.id.viewpage);
        mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        mViewPager.setOffscreenPageLimit(0);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                mCurIndexTv.setText((position + 1) + "");
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        showLoadingView();

        initReceiver();

        mWorkerHandler.sendEmptyMessage(MESSAGE_WHAT_LOAD_DATA);
    }

    private void initReceiver() {
        //音频广播
        mAudioBroadcastReceiver = new AudioBroadcastReceiver();
        mAudioBroadcastReceiver.setReceiverListener(new AudioBroadcastReceiver.AudioReceiverListener() {
            @Override
            public void onReceive(Context context, final Intent intent, final int code) {
                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        handleAudioBroadcastReceiver(intent, code);
                    }
                });
            }

            private void handleAudioBroadcastReceiver(Intent intent, int code) {
                switch (code) {
                    case AudioBroadcastReceiver.ACTION_CODE_PLAYING:

                        int playProgress = (int) mAudioInfo.getDuration();
                        Bundle playingBundle = intent.getBundleExtra(AudioBroadcastReceiver.ACTION_BUNDLEKEY);
                        AudioInfo playingAudioInfo = playingBundle.getParcelable(AudioBroadcastReceiver.ACTION_DATA_KEY);
                        if (playingAudioInfo != null && mAudioInfo.getHash().equals(playingAudioInfo.getHash())) {
                            playProgress = playingAudioInfo.getPlayProgress();
                        }

                        if (mAdapter != null && mSearchLrcDatas != null && mSearchLrcDatas.size() > 0) {

                            LrcFragment lrcFragment = (LrcFragment) mAdapter.getCurrentFragment();
                            if (lrcFragment != null)
                                lrcFragment.refreshView(playProgress);

                        }

                        break;

                    case AudioBroadcastReceiver.ACTION_CODE_LRCRELOADED:
                        //歌词重新加载
                        finish();
                        break;
                }
            }
        });
        mAudioBroadcastReceiver.registerReceiver(mContext);
    }

    @Override
    protected void handleUIMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_WHAT_LOAD_DATA:

                if (mAudioInfo != null) {
                    mSongNameEditText.setText(mAudioInfo.getSongName());
                    mSingerNameEditText.setText(mAudioInfo.getSingerName());

                    doSearch();
                } else {
                    ToastUtil.showTextToast(mContext, getString(R.string.select_song_text));
                }


                break;

            case MESSAGE_WHAT_SEARCN_LRC:

                HttpReturnResult httpReturnResult = (HttpReturnResult) msg.obj;
                if (httpReturnResult.isSuccessful()) {
                    Map<String, Object> returnResult = (Map<String, Object>) httpReturnResult.getResult();
                    List<LrcInfo> lists = (List<LrcInfo>) returnResult.get("rows");
                    if (lists != null && lists.size() > 0) {
                        mSearchLrcDatas.addAll(lists);
                        mCurIndexTv.setText("1");
                    } else {
                        ToastUtil.showTextToast(mContext, HttpReturnResult.ERROR_MSG_NULLDATA);
                    }
                } else {
                    ToastUtil.showTextToast(mContext, httpReturnResult.getErrorMsg());
                }

                mSumTv.setText(mSearchLrcDatas.size() + "");
                mAdapter = new SearchLrcFragmentAdapter(getSupportFragmentManager(), mAudioInfo, mSearchLrcDatas);
                mViewPager.setAdapter(mAdapter);
                showContentView();

                break;
        }
    }

    @Override
    protected void handleWorkerMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_WHAT_LOAD_DATA:

                mAudioInfo = getIntent().getParcelableExtra(AUDIO_DATA_KEY);
                mUIHandler.sendEmptyMessage(MESSAGE_WHAT_LOAD_DATA);

                break;
            case MESSAGE_WHAT_SEARCN_LRC:

                doSearchHttp();

                break;
        }
    }

    /**
     * 搜索歌词请求
     */
    private void doSearchHttp() {
        mSearchLrcDatas.clear();
        String songName = mSongNameEditText.getText().toString();
        String singerName = mSingerNameEditText.getText().toString();
        //加载歌词
        String keyWords = "";
        if (singerName.equals(getString(R.string.unknow))) {
            keyWords = songName;
        } else {
            keyWords = singerName + " - " + songName;
        }
        ConfigInfo configInfo = ConfigInfo.obtain();
        APIHttpClient apiHttpClient = HttpUtil.getHttpClient();
        HttpReturnResult httpReturnResult = apiHttpClient.searchLyricsList(mContext, keyWords, mAudioInfo.getDuration() + "", "", configInfo.isWifi());

        Message msg = Message.obtain();
        msg.what = MESSAGE_WHAT_SEARCN_LRC;
        msg.obj = httpReturnResult;
        mUIHandler.sendMessage(msg);

    }

    /**
     * 搜索
     */
    private void doSearch() {
        String songName = mSongNameEditText.getText().toString();
        String singerName = mSingerNameEditText.getText().toString();
        if (songName.equals("") && singerName.equals("")) {
            ToastUtil.showTextToast(getApplicationContext(), getString(R.string.input_key));
            return;
        }
        mCurIndexTv.setText("0");
        showLoadingView();
        mWorkerHandler.sendEmptyMessage(MESSAGE_WHAT_SEARCN_LRC);
    }

    /**
     * 显示加载窗口
     */
    public void showLoadingView() {
        mContentContainer.setVisibility(View.GONE);
        mLoadingContainer.setVisibility(View.VISIBLE);
        mLoadImgView.clearAnimation();
        mLoadImgView.startAnimation(rotateAnimation);
    }

    /**
     * 显示主界面
     */
    public void showContentView() {
        mContentContainer.setVisibility(View.VISIBLE);
        mLoadingContainer.setVisibility(View.GONE);
        mLoadImgView.clearAnimation();
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.out_to_bottom);
    }

    @Override
    protected void onDestroy() {
        destroyReceiver();
        super.onDestroy();
    }

    /**
     * 销毁广播
     */
    private void destroyReceiver() {

        if (mAudioBroadcastReceiver != null) {
            mAudioBroadcastReceiver.unregisterReceiver(mContext);
        }

    }
}
