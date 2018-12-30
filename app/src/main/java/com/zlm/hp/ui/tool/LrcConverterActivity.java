package com.zlm.hp.ui.tool;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Message;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dou361.dialogui.DialogUIUtils;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.entity.ToolInfo;
import com.zlm.hp.lyrics.formats.LyricsFileReader;
import com.zlm.hp.lyrics.formats.LyricsFileWriter;
import com.zlm.hp.lyrics.model.LyricsInfo;
import com.zlm.hp.lyrics.utils.FileUtils;
import com.zlm.hp.lyrics.utils.LyricsIOUtils;
import com.zlm.hp.ui.BaseActivity;
import com.zlm.hp.ui.R;
import com.zlm.hp.util.ResourceUtil;
import com.zlm.libs.widget.SwipeBackLayout;

import java.io.File;

/**
 * @Description: 歌词转换器
 * @author: zhangliangming
 * @date: 2018-12-30 17:40
 **/
public class LrcConverterActivity extends BaseActivity {
    /**
     *
     */
    private SwipeBackLayout mSwipeBackLayout;

    /**
     * 工具信息
     */
    private ToolInfo mToolInfo;

    /**
     * 源文件路径
     */
    private TextView mOrigFilePathTv;
    private String mOrigFilePath;
    /**
     * 输出格式
     */
    private RadioGroup mOutFormatsRG;
    private String[] mOutFormats = new String[]{"ksc", "krc", "hrc", "lrc"};
    private int[] mOutFormatsRadioButtonId = new int[]{R.id.kscRadioButton, R.id.krcRadioButton, R.id.hrcRadioButton, R.id.lrcRadioButton};
    /**
     * 选择源文件请求码
     */
    private final int SELECTORIGFILE = 0;
    /**
     * 设置源文件路径
     */
    private final int MESSAGE_WHAT_SETORIGFILEPATH = 0;

    /**
     * 转换歌词
     */
    private final int MESSAGE_WHAT_CONVERTERLRC = 1;

    /**
     * 加载窗口
     */
    private Dialog mLoadingDialog = null;

    @Override
    protected int setContentLayoutResID() {
        return R.layout.activity_lrc_converter;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        mSwipeBackLayout = findViewById(R.id.swipeback_layout);
        mSwipeBackLayout.setSwipeBackLayoutListener(new SwipeBackLayout.SwipeBackLayoutListener() {

            @Override
            public void finishActivity() {
                if (mLoadingDialog != null)
                    DialogUIUtils.dismiss(mLoadingDialog);
                finish();
                overridePendingTransition(0, 0);
            }
        });

        //获取工具信息
        mToolInfo = getIntent().getParcelableExtra(ToolInfo.DATA_KEY);
        TextView titleView = findViewById(R.id.title);
        if (mToolInfo != null) {
            titleView.setText(mToolInfo.getTitle());
        } else {
            titleView.setText(getString(R.string.tab_tool));
        }

        //返回
        ImageView backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwipeBackLayout.closeView();
            }
        });

        //选择源文件按钮
        Button origSelectFileBtn = findViewById(R.id.origSelectFile);
        origSelectFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent selectFileIntent = new Intent(LrcConverterActivity.this, FileManagerActivity.class);
                selectFileIntent.putExtra(FileManagerActivity.FILEFILTER_KEY, "krc,ksc,hrc");
                startActivityForResult(selectFileIntent, SELECTORIGFILE);
                //去掉动画
                overridePendingTransition(0, 0);
            }
        });

        //源文件路径
        mOrigFilePathTv = findViewById(R.id.origFilePath);
        mOutFormatsRG = findViewById(R.id.outFormats);
        mOutFormatsRG.check(mOutFormatsRadioButtonId[2]);
        //转换按钮
        Button converterBtn = findViewById(R.id.converter);
        converterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOrigFilePath != null && !mOrigFilePath.equals("")) {
                    int checkedRadioButtonId = mOutFormatsRG.getCheckedRadioButtonId();
                    if (checkedRadioButtonId == -1) {
                        Toast.makeText(getApplicationContext(), "请选择歌词的输出格式！", Toast.LENGTH_SHORT).show();
                    } else {
                        //获取输出格式索引
                        int index = mOutFormatsRG.indexOfChild(mOutFormatsRG.findViewById(checkedRadioButtonId));
                        //获取输出格式
                        String outFormat = mOutFormats[index];
                        converterLrc(outFormat);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "请选择歌词文件！", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /**
     * 转换歌词
     *
     * @param outFormat
     */
    private void converterLrc(String outFormat) {
        File orgFile = new File(mOrigFilePath);
        if (!orgFile.exists()) {
            Toast.makeText(getApplicationContext(), "源文件不存在，请重新选择源文件！", Toast.LENGTH_SHORT).show();
        } else {

            /**
             * 加载框
             *
             * @param context          上下文
             * @param msg              提示文本
             * @param isVertical       true为竖直方向false为水平方向
             * @param cancleable       true为可以取消false为不可取消
             * @param outsideTouchable true为可以点击空白区域false为不可点击
             * @param isWhiteBg        true为白色背景false为灰色背景
             */
            mLoadingDialog = DialogUIUtils.showLoading(LrcConverterActivity.this, getString(R.string.loading_tip), false, false, false, true).show();


            Message msg = Message.obtain();
            msg.what = MESSAGE_WHAT_CONVERTERLRC;
            msg.obj = outFormat;
            mWorkerHandler.sendMessage(msg);
        }
    }

    @Override
    protected void handleUIMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_WHAT_CONVERTERLRC:

                if (mLoadingDialog != null)
                    DialogUIUtils.dismiss(mLoadingDialog);

                boolean result = (boolean) msg.obj;
                if (result) {
                    Toast.makeText(getApplicationContext(), "歌词转换完成！", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "歌词转换失败！", Toast.LENGTH_SHORT).show();
                }

                break;
            case MESSAGE_WHAT_SETORIGFILEPATH:

                if (mOrigFilePath != null && !mOrigFilePath.equals("")) {
                    mOrigFilePathTv.setText("源文件路径：" + mOrigFilePath);
                } else {
                    mOrigFilePathTv.setText("源文件路径：");
                }

                break;
        }
    }

    @Override
    protected void handleWorkerMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_WHAT_CONVERTERLRC:
                File orgFile = new File(mOrigFilePath);
                String outFormat = (String) msg.obj;
                //1.先读取源文件歌词
                LyricsFileReader lyricsFileReader = LyricsIOUtils.getLyricsFileReader(orgFile);
                String outFileName = FileUtils.removeExt(orgFile.getName());
                String outFilePath = ResourceUtil.getFilePath(getApplicationContext(), ResourceConstants.PATH_LYRICS, File.separator + outFileName + "." + outFormat);
                File outFile = new File(outFilePath);
                //2.生成转换歌词文件
                LyricsFileWriter lyricsFileWriter = LyricsIOUtils.getLyricsFileWriter(outFile);
                LyricsInfo lyricsInfo = lyricsFileReader.readFile(orgFile);
                boolean result = false;
                if (lyricsInfo != null) {
                    result = lyricsFileWriter.writer(lyricsInfo, outFile.getPath());
                }

                Message msgTemp = Message.obtain();
                msgTemp.what = MESSAGE_WHAT_CONVERTERLRC;
                msgTemp.obj = result;
                mUIHandler.sendMessage(msgTemp);

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECTORIGFILE) {
            if (resultCode == Activity.RESULT_OK) {
                mOrigFilePath = data.getStringExtra("selectFilePath");
                if (mOrigFilePath != null && !mOrigFilePath.equals("")) {
                    String ext = FileUtils.getFileExt(mOrigFilePath);
                    if (!ext.equals("krc") && !ext.equals("ksc") && !ext.equals("hrc")) {
                        Toast.makeText(getApplicationContext(), "请选择支持的歌词文件！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            } else {
                mOrigFilePath = null;
            }
            mUIHandler.sendEmptyMessage(MESSAGE_WHAT_SETORIGFILEPATH);
        }
    }

    @Override
    public void onBackPressed() {
        mSwipeBackLayout.closeView();
    }
}
