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
import com.zlm.hp.entity.SpecialInfo;
import com.zlm.hp.handler.WeakRefHandler;
import com.zlm.hp.receiver.FragmentReceiver;
import com.zlm.hp.ui.R;
import com.zlm.hp.util.ImageUtil;
import com.zlm.hp.util.ResourceUtil;
import com.zlm.hp.widget.ListItemRelativeLayout;

import java.util.ArrayList;

/**
 * 歌单
 * Created by zhangliangming on 2017/7/29.
 */
public class SpecialAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private WeakRefHandler mUIHandler;
    private WeakRefHandler mWorkerHandler;

    private Context mContext;
    private ArrayList<SpecialInfo> mDatas;

    public SpecialAdapter(WeakRefHandler uiHandler, WeakRefHandler workerHandler, Context context, ArrayList<SpecialInfo> datas) {
        this.mUIHandler = uiHandler;
        this.mWorkerHandler = workerHandler;
        this.mContext = context;
        this.mDatas = datas;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_one_category, null, false);
        SpecialViewHolder holder = new SpecialViewHolder(view);
        return holder;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof SpecialViewHolder && position < mDatas.size()) {
            SpecialInfo specialInfo = mDatas.get(position);
            reshViewHolder(position, (SpecialViewHolder) viewHolder, specialInfo);
        }
    }

    /**
     * 刷新ui
     *
     * @param position
     * @param viewHolder
     * @param specialInfo
     */
    private void reshViewHolder(int position, final SpecialViewHolder viewHolder, final SpecialInfo specialInfo) {

        ConfigInfo configInfo = ConfigInfo.obtain();
        String filePath = ResourceUtil.getFilePath(mContext, ResourceConstants.PATH_CACHE_IMAGE, specialInfo.getImageUrl().hashCode() + ".png");
        ImageUtil.loadImage(mContext, filePath, specialInfo.getImageUrl(), configInfo.isWifi(), viewHolder.getItemImg(), 400, 400, new AsyncHandlerTask(mUIHandler, mWorkerHandler), null);

        viewHolder.getSpecialTitleTv().setText(specialInfo.getSpecialName());
        viewHolder.getListItemRelativeLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentReceiver.sendSpecialFragmentReceiver(mContext, specialInfo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    class SpecialViewHolder extends RecyclerView.ViewHolder {
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
         * 标题
         */
        private TextView specialTitleTv;

        public SpecialViewHolder(View view) {
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


        public TextView getSpecialTitleTv() {
            if (specialTitleTv == null) {
                specialTitleTv = view.findViewById(R.id.title);
            }
            return specialTitleTv;
        }

    }
}
