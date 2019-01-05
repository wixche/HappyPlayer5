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
import com.zlm.hp.entity.VideoInfo;
import com.zlm.hp.handler.WeakRefHandler;
import com.zlm.hp.ui.R;
import com.zlm.hp.util.ImageUtil;
import com.zlm.hp.util.ResourceUtil;
import com.zlm.hp.widget.ListItemRelativeLayout;

import java.util.ArrayList;

/**
 * @Description: mv列表
 * @author: zhangliangming
 * @date: 2019-01-05 20:07
 **/
public class MVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private WeakRefHandler mUIHandler;
    private WeakRefHandler mWorkerHandler;

    private Context mContext;
    private ArrayList<VideoInfo> mDatas;

    private OnClickListener mOnClickListener;

    public MVAdapter(WeakRefHandler uiHandler, WeakRefHandler workerHandler, Context context, ArrayList<VideoInfo> datas) {
        this.mUIHandler = uiHandler;
        this.mWorkerHandler = workerHandler;
        this.mContext = context;
        this.mDatas = datas;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_mv, null, false);
        MvViewHolder holder = new MvViewHolder(view);
        return holder;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof MvViewHolder && position < mDatas.size()) {
            VideoInfo videoInfo = mDatas.get(position);
            reshViewHolder(position, (MvViewHolder) viewHolder, videoInfo);
        }
    }

    /**
     * 刷新ui
     *
     * @param position
     * @param viewHolder
     * @param videoInfo
     */
    private void reshViewHolder(int position, final MvViewHolder viewHolder, final VideoInfo videoInfo) {
        ConfigInfo configInfo = ConfigInfo.obtain();
        String filePath = ResourceUtil.getFilePath(mContext, ResourceConstants.PATH_CACHE_IMAGE, videoInfo.getImageUrl().hashCode() + ".png");
        ImageUtil.loadImage(mContext, filePath, videoInfo.getImageUrl(), configInfo.isWifi(), viewHolder.getItemImg(), 400, 400, new AsyncHandlerTask(mUIHandler, mWorkerHandler), null);
        viewHolder.getTitleTv().setText(videoInfo.getMvName());
        viewHolder.getSingerNameTv().setText(videoInfo.getSingerName());
        viewHolder.getListItemRelativeLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickListener != null) {
                    mOnClickListener.openVideoView(videoInfo);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

    /**
     *
     */
    public interface OnClickListener {
        void openVideoView(VideoInfo videoInfo);
    }

    class MvViewHolder extends RecyclerView.ViewHolder {
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
        private TextView titleTv;

        /**
         * 歌手
         */
        private TextView singerNameTv;

        public MvViewHolder(View view) {
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

        public TextView getTitleTv() {
            if (titleTv == null) {
                titleTv = view.findViewById(R.id.songName);
            }
            return titleTv;
        }

        public TextView getSingerNameTv() {
            if (singerNameTv == null) {
                singerNameTv = view.findViewById(R.id.singerName);
            }
            return singerNameTv;
        }
    }
}
