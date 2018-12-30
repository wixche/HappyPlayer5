package com.zlm.hp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zlm.hp.adapter.ToolAdapter;
import com.zlm.hp.entity.ToolInfo;
import com.zlm.libs.widget.SwipeBackLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 相关工具
 * @author: zhangliangming
 * @date: 2018-12-30 0:41
 **/
public class ToolActivity extends BaseActivity {
    /**
     *
     */
    private SwipeBackLayout mSwipeBackLayout;

    /**
     * 列表视图
     */
    private RecyclerView mRecyclerView;
    private ToolAdapter mAdapter;

    /**
     * 工具数据
     */
    private List<ToolInfo> mToolInfoList = new ArrayList<ToolInfo>();

    private final int MESSAGE_WHAT_LOADDATA = 0;

    @Override
    protected int setContentLayoutResID() {
        return R.layout.activity_tool;
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
        titleView.setText(getString(R.string.tab_tool));

        //返回
        ImageView backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwipeBackLayout.closeView();
            }
        });

        //
        mRecyclerView = findViewById(R.id.recyclerView);
        //初始化内容视图
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mAdapter = new ToolAdapter(mContext, mToolInfoList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setClickListener(new ToolAdapter.OnClickListener() {
            @Override
            public void runTool(ToolInfo toolInfo) {
                try {

                    Intent intent = new Intent(mContext, Class.forName(toolInfo.getPackageName()));
                    intent.putExtra(ToolInfo.DATA_KEY, toolInfo);
                    startActivity(intent);
                    //去掉动画
                    overridePendingTransition(0, 0);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        initData();
    }

    private void initData() {
        mWorkerHandler.sendEmptyMessage(MESSAGE_WHAT_LOADDATA);
    }

    @Override
    protected void handleUIMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_WHAT_LOADDATA:
                mAdapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    protected void handleWorkerMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_WHAT_LOADDATA:
                mToolInfoList.clear();
                loadData();
                mUIHandler.sendEmptyMessage(MESSAGE_WHAT_LOADDATA);
                break;
        }
    }

    /**
     * 加载数据
     */
    private void loadData() {
        try {
            //添加数据
            String[] tools = getResources().getStringArray(R.array.tool);
            for (int i = 0; i < tools.length; i++) {
                String toolJsonString = tools[i];
                JSONObject toolJsonObject = new JSONObject(toolJsonString);

                ToolInfo toolInfo = new ToolInfo();
                toolInfo.setTitle(toolJsonObject.optString("toolName", ""));
                toolInfo.setPackageName(toolJsonObject.optString("packageName", ""));

                mToolInfoList.add(toolInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        mSwipeBackLayout.closeView();
    }
}
