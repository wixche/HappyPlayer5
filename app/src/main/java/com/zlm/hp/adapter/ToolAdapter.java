package com.zlm.hp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlm.hp.entity.ToolInfo;
import com.zlm.hp.ui.R;

import java.util.List;

/**
 * @Description: 工具适配器
 * @author: zhangliangming
 * @date: 2018-12-30 16:59
 **/
public class ToolAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    /**
     * 工具数据
     */
    private List<ToolInfo> mDatas;

    private OnClickListener mClickListener;

    public ToolAdapter(Context context, List<ToolInfo> datas) {
        this.mContext = context;
        this.mDatas = datas;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_tool, null, false);
        ToolViewHolder holder = new ToolViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ToolViewHolder && position < mDatas.size()) {
            ToolInfo toolInfo = mDatas.get(position);
            reshViewHolder(position, (ToolViewHolder) viewHolder, toolInfo);
        }
    }

    /**
     * 刷新
     *
     * @param position
     * @param viewHolder
     * @param toolInfo
     */
    private void reshViewHolder(int position, ToolViewHolder viewHolder, final ToolInfo toolInfo) {
        viewHolder.getToolNameTv().setText(toolInfo.getTitle());
        viewHolder.getListItemRelativeLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mClickListener != null){
                    mClickListener.runTool(toolInfo);
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public void setClickListener(OnClickListener clickListener) {
        this.mClickListener = clickListener;
    }

    /**
     *
     */
    public interface OnClickListener{
        void runTool(ToolInfo toolInfo);
    }

    /**
     *
     */
    class ToolViewHolder extends RecyclerView.ViewHolder {
        private View view;
        /**
         * item底部布局
         */
        private RelativeLayout listItemRelativeLayout;

        /**
         * 工具名称
         */
        private TextView toolNameTv;

        public ToolViewHolder(View view) {
            super(view);
            this.view = view;
        }

        public RelativeLayout getListItemRelativeLayout() {
            if (listItemRelativeLayout == null) {
                listItemRelativeLayout = view.findViewById(R.id.itemBG);
            }
            return listItemRelativeLayout;
        }

        public TextView getToolNameTv() {
            if (toolNameTv == null) {
                toolNameTv = view.findViewById(R.id.tool_name);
            }
            return toolNameTv;
        }
    }
}
