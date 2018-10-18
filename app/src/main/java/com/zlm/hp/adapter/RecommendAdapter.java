package com.zlm.hp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zlm.hp.async.AsyncHandlerTask;
import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.entity.RankInfo;
import com.zlm.hp.handler.WeakRefHandler;
import com.zlm.hp.receiver.FragmentReceiver;
import com.zlm.hp.ui.R;
import com.zlm.hp.util.ImageUtil;
import com.zlm.hp.util.ResourceUtil;
import com.zlm.hp.widget.ListItemRelativeLayout;

import java.util.ArrayList;

/**
 * 排行
 * Created by zhangliangming on 2017/7/29.
 */
public class RecommendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private WeakRefHandler mUIHandler;
    private WeakRefHandler mWorkerHandler;

    private Context mContext;
    private ArrayList<RankInfo> mDatas;

    public RecommendAdapter(WeakRefHandler uiHandler, WeakRefHandler workerHandler, Context context, ArrayList<RankInfo> datas) {
        this.mUIHandler = uiHandler;
        this.mWorkerHandler = workerHandler;
        this.mContext = context;
        this.mDatas = datas;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_one_category, null, false);
        RecommendViewHolder holder = new RecommendViewHolder(view);
        return holder;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof RecommendViewHolder && position < mDatas.size()) {
            RankInfo rankInfo = mDatas.get(position);
            reshViewHolder(position, (RecommendViewHolder) viewHolder, rankInfo);
        }
    }

    /**
     * 刷新ui
     *
     * @param position
     * @param viewHolder
     * @param rankInfo
     */
    private void reshViewHolder(int position, final RecommendViewHolder viewHolder, final RankInfo rankInfo) {
        ConfigInfo configInfo = ConfigInfo.obtain();
        String filePath = ResourceUtil.getFilePath(mContext, ResourceConstants.PATH_CACHE_IMAGE, rankInfo.getImageUrl().hashCode() + ".png");
        ImageUtil.loadImage(mContext, filePath, rankInfo.getImageUrl(), configInfo.isWifi(), viewHolder.getItemImg(), 400, 400, new AsyncHandlerTask(mUIHandler, mWorkerHandler), null);
        viewHolder.getRankTitleTv().setText(rankInfo.getRankName());
        viewHolder.getListItemRelativeLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开排行页面
                FragmentReceiver.sendRecommendFragmentReceiver(mContext, rankInfo);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    class RecommendViewHolder extends RecyclerView.ViewHolder {
        private View view;
        /**
         * item底部布局
         */
        private ListItemRelativeLayout listItemRelativeLayout;

        /**
         * item图片
         */
        private ImageView itemImg;
        /**
         * 排行标题
         */
        private TextView rankTitleTv;

        public RecommendViewHolder(View view) {
            super(view);
            this.view = view;
        }

        public ListItemRelativeLayout getListItemRelativeLayout() {
            if (listItemRelativeLayout == null) {
                listItemRelativeLayout = view.findViewById(R.id.itemBG);
            }
            return listItemRelativeLayout;
        }

        public ImageView getItemImg() {
            if (itemImg == null) {
                itemImg = view.findViewById(R.id.item_icon);
            }
            return itemImg;
        }

        public TextView getRankTitleTv() {
            if (rankTitleTv == null) {
                rankTitleTv = view.findViewById(R.id.title);
            }
            return rankTitleTv;
        }

    }
}
