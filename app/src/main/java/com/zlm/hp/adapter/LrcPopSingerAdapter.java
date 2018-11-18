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
import com.zlm.hp.handler.WeakRefHandler;
import com.zlm.hp.ui.LrcActivity;
import com.zlm.hp.ui.R;
import com.zlm.hp.util.ImageUtil;
import com.zlm.hp.widget.PopListItemRelativeLayout;

/**
 * 歌手列表
 */
public class LrcPopSingerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private String[] mDatas;
    private WeakRefHandler mUIHandler;
    private WeakRefHandler mWorkerHandler;
    private LrcActivity.PopSingerListener mPopSingerListener;

    public LrcPopSingerAdapter(Context context, String[] datas, WeakRefHandler uiHandler, WeakRefHandler workerHandler, LrcActivity.PopSingerListener popSingerListener) {
        this.mContext = context;
        this.mDatas = datas;
        this.mUIHandler = uiHandler;
        this.mWorkerHandler = workerHandler;
        this.mPopSingerListener = popSingerListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_lrc_singer, null, false);
        LrcPopSingerListViewHolder holder = new LrcPopSingerListViewHolder(view);
        return holder;


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof LrcPopSingerListViewHolder && position < mDatas.length) {
            String singerName = mDatas[position];
            reshViewHolder(position, (LrcPopSingerListViewHolder) viewHolder, singerName);
        }
    }

    /**
     * 刷新
     *
     * @param position
     * @param viewHolder
     * @param singerName
     */
    private void reshViewHolder(final int position, final LrcPopSingerListViewHolder viewHolder, final String singerName) {

        ConfigInfo configInfo = ConfigInfo.obtain();
        ImageUtil.loadSingerImage(mContext, viewHolder.getSingPicImg(), singerName, configInfo.isWifi(), 400, 400, new AsyncHandlerTask(mUIHandler, mWorkerHandler), null);

        viewHolder.getSingerNameTv().setText(singerName);
        //item点击事件
        viewHolder.getListItemRelativeLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPopSingerListener != null) {
                    mPopSingerListener.search(singerName);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return mDatas.length;
    }


    /////////////////////////////////////////////////////

    class LrcPopSingerListViewHolder extends RecyclerView.ViewHolder {
        private View view;
        /**
         * item底部布局
         */
        private PopListItemRelativeLayout listItemRelativeLayout;

        /**
         * 歌手头像按钮
         */
        private ImageView singPicImg;

        /**
         * 歌手名称
         */
        private TextView singerNameTv;


        public LrcPopSingerListViewHolder(View view) {
            super(view);
            this.view = view;
        }

        public PopListItemRelativeLayout getListItemRelativeLayout() {
            if (listItemRelativeLayout == null) {
                listItemRelativeLayout = view.findViewById(R.id.itemBG);
            }
            return listItemRelativeLayout;
        }

        public TextView getSingerNameTv() {
            if (singerNameTv == null) {
                singerNameTv = view.findViewById(R.id.singerName);
            }
            return singerNameTv;
        }


        public ImageView getSingPicImg() {
            if (singPicImg == null) {
                singPicImg = view.findViewById(R.id.singPic);
            }
            return singPicImg;
        }

    }

}
