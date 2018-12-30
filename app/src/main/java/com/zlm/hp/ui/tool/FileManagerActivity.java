package com.zlm.hp.ui.tool;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zlm.hp.adapter.FileManagerAdapter;
import com.zlm.hp.entity.FileInfo;
import com.zlm.hp.entity.StorageInfo;
import com.zlm.hp.ui.BaseActivity;
import com.zlm.hp.ui.R;
import com.zlm.hp.util.FileUtil;
import com.zlm.hp.util.StorageListUtil;
import com.zlm.hp.widget.IconfontImageButtonTextView;
import com.zlm.libs.widget.SwipeBackLayout;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @Description: 文件管理器
 * @author: zhangliangming
 * @date: 2018-12-30 19:11
 **/
public class FileManagerActivity extends BaseActivity {

    /**
     *
     */
    private SwipeBackLayout mSwipeBackLayout;


    /**
     * 列表视图
     */
    private RecyclerView mRecyclerView;

    /**
     * 文件
     */
    private ArrayList<FileInfo> mDatas;
    private FileManagerAdapter mAdapter;

    /**
     * 文件夹路径
     */
    private TextView mFileDirectoryPathTv;
    private List<String> mFilePathList;

    /**
     * 选择文件按钮
     */
    private Button mSelectedFileBtn;

    /**
     * 加载文件数据
     */
    private final int MESSAGE_WHAT_LOADFILEDATA = 0;

    /**
     * 加载文件夹数据
     */
    private final int MESSAGE_WHAT_LOADFILEDIRECTORYDATA = 1;

    /**
     * 文件过滤
     */
    public static final String FILEFILTER_KEY = "FileFilter_Key";

    private String mFileFilter = "";

    @Override
    protected int setContentLayoutResID() {
        return R.layout.activity_file_manager;
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
        titleView.setText(getString(R.string.select_file));

        //返回
        ImageView backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwipeBackLayout.closeView();
            }
        });

        //
        mRecyclerView = findViewById(R.id.file_recyclerView);
        //初始化内容视图
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mDatas = new ArrayList<FileInfo>();
        mAdapter = new FileManagerAdapter(getApplicationContext(), mDatas);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setItemEvent(new FileManagerAdapter.ItemEvent() {
            @Override
            public void fileDirectoryClick(String filePath) {
                mFilePathList.add(mFileDirectoryPathTv.getText().toString());
                loadFileDirectoryData(filePath);
            }
        });

        //
        mFilePathList = new ArrayList<String>();
        mFileDirectoryPathTv = findViewById(R.id.file_directory_path);
        //返回上一级文件

        RelativeLayout bakcBg = findViewById(R.id.op_heaad);
        bakcBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backParentFile();
            }
        });


        IconfontImageButtonTextView fileBack = findViewById(R.id.file_back);
        fileBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backParentFile();
            }
        });

        //
        mSelectedFileBtn = findViewById(R.id.selectFile);
        mSelectedFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectFilePath = mAdapter.getSelectFilePath();
                if (TextUtils.isEmpty(selectFilePath)) {
                    Toast.makeText(getApplicationContext(), "请选择文件!", Toast.LENGTH_SHORT).show();
                } else {
                    Intent in = new Intent();
                    in.putExtra("selectFilePath", selectFilePath);
                    setResult(Activity.RESULT_OK, in);
                    mSwipeBackLayout.closeView();
                }
            }
        });

        //过滤
        mFileFilter = getIntent().getStringExtra(FILEFILTER_KEY);
        loadFileData();
    }

    /**
     * 返回上一级
     */
    private boolean backParentFile() {
        if (mFilePathList.size() == 0) {
            loadFileData();
            return true;
        }
        String filePath = mFilePathList.get(mFilePathList.size() - 1);
        mFilePathList.remove(mFilePathList.size() - 1);
        if (TextUtils.isEmpty(filePath)) {
            loadFileData();
        } else {
            loadFileDirectoryData(filePath);
        }
        return false;
    }

    /**
     * 加载文件夹数据
     *
     * @param filePath
     */
    private synchronized void loadFileDirectoryData(String filePath) {
        Message msg = Message.obtain();
        msg.what = MESSAGE_WHAT_LOADFILEDIRECTORYDATA;
        msg.obj = filePath;
        mWorkerHandler.sendMessage(msg);
    }

    /**
     * 加载文件数据
     */
    private synchronized void loadFileData() {
        mWorkerHandler.sendEmptyMessage(MESSAGE_WHAT_LOADFILEDATA);
    }

    @Override
    protected void handleUIMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_WHAT_LOADFILEDIRECTORYDATA:
                mAdapter.notifyDataSetChanged();

                String filePath = (String) msg.obj;
                mFileDirectoryPathTv.setText(filePath);

                break;
            case MESSAGE_WHAT_LOADFILEDATA:
                mAdapter.notifyDataSetChanged();
                mFileDirectoryPathTv.setText("");

                break;
        }
    }

    @Override
    protected void handleWorkerMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_WHAT_LOADFILEDIRECTORYDATA:
                String filePathTemp = (String) msg.obj;
                mDatas.clear();
                File file = new File(filePathTemp);
                File[] files = file.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File acceptFile) {
                        if (acceptFile.getName().startsWith(".")) {
                            return false;
                        }
                        if (acceptFile.isFile() && !TextUtils.isEmpty(mFileFilter)) {
                            String fileExt = FileUtil.getFileExt(acceptFile);
                            if (mFileFilter.contains(fileExt) && !TextUtils.isEmpty(fileExt)) {
                                return true;
                            } else {
                                return false;
                            }
                        }
                        return true;
                    }
                });
                //文件名排序
                Collections.sort(Arrays.asList(files), new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        if (o1.isDirectory() && o2.isFile())
                            return -1;
                        if (o1.isFile() && o2.isDirectory())
                            return 1;
                        return o1.getName().compareTo(o2.getName());
                    }
                });
                //
                for (int i = 0; i < files.length; i++) {
                    File tempFile = files[i];
                    FileInfo fileInfo = new FileInfo();
                    if (tempFile.isDirectory()) {
                        fileInfo.setFile(false);
                    } else {
                        fileInfo.setFile(true);
                    }

                    String filePath = tempFile.getPath();
                    fileInfo.setFilePath(filePath);
                    fileInfo.setFileName(filePath.substring(filePath.lastIndexOf(File.separator) + 1, filePath.length()));
                    mDatas.add(fileInfo);
                }

                //
                Message msgTemp = Message.obtain();
                msgTemp.what = MESSAGE_WHAT_LOADFILEDIRECTORYDATA;
                msgTemp.obj = filePathTemp;
                mUIHandler.sendMessage(msgTemp);

                break;
            case MESSAGE_WHAT_LOADFILEDATA:

                mDatas.clear();
                List<StorageInfo> list = StorageListUtil
                        .listAvaliableStorage(getApplicationContext());
                if (list == null || list.size() == 0) {

                } else {
                    for (int i = 0; i < list.size(); i++) {
                        StorageInfo storageInfo = list.get(i);
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.setFile(false);
                        String filePath = storageInfo.getPath();
                        fileInfo.setFilePath(filePath);
                        fileInfo.setFileName(filePath.substring(filePath.lastIndexOf(File.separator) + 1, filePath.length()));
                        mDatas.add(fileInfo);
                    }
                }

                mUIHandler.sendEmptyMessage(MESSAGE_WHAT_LOADFILEDATA);

                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (backParentFile()) {
            mSwipeBackLayout.closeView();
        }
    }

}
