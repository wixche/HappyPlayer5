package com.zlm.hp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.zlm.hp.async.AsyncHandlerTask;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.entity.SingerInfo;
import com.zlm.hp.handler.WeakRefHandler;
import com.zlm.hp.ui.R;
import com.zlm.hp.util.ImageUtil;
import com.zlm.hp.util.ResourceUtil;

import java.io.File;
import java.util.List;

/**
 * 搜索歌手写真
 */
public class SearchSingerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<SingerInfo> mDatas;
    private WeakRefHandler mUIHandler;
    private WeakRefHandler mWorkerHandler;
    private List<SingerInfo> mSelectDatas;


    public SearchSingerAdapter(Context context, List<SingerInfo> datas, List<SingerInfo> selectDatas, WeakRefHandler uiHandler, WeakRefHandler workerHandler) {
        this.mContext = context;
        this.mDatas = datas;
        this.mUIHandler = uiHandler;
        this.mWorkerHandler = workerHandler;
        this.mSelectDatas = selectDatas;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_search_singer, null, false);
        SearchSingerViewHolder holder = new SearchSingerViewHolder(view);
        return holder;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof SearchSingerViewHolder && position < mDatas.size()) {
            SingerInfo singerInfo = mDatas.get(position);
            reshViewHolder(position, (SearchSingerViewHolder) viewHolder, singerInfo);
        }
    }

    /**
     * 刷新
     *
     * @param position
     * @param viewHolder
     * @param singerInfo
     */
    private void reshViewHolder(final int position, final SearchSingerViewHolder viewHolder, final SingerInfo singerInfo) {
        String filePath = ResourceUtil.getFilePath(mContext, ResourceConstants.PATH_SINGER, (singerInfo.getSingerName() + File.separator + singerInfo.getImageUrl().hashCode() + ".jpg"));
        ImageUtil.loadSingerPic(mContext, filePath, singerInfo.getImageUrl(), true, viewHolder.getSingPicImg(), 720, 1080, new AsyncHandlerTask(mUIHandler, mWorkerHandler), null);
        if (contains(singerInfo)) {
            viewHolder.getSelectedImg().setVisibility(View.VISIBLE);
            viewHolder.getUnSelectImg().setVisibility(View.INVISIBLE);
        } else {
            viewHolder.getSelectedImg().setVisibility(View.INVISIBLE);
            viewHolder.getUnSelectImg().setVisibility(View.VISIBLE);
        }
        //
        viewHolder.getListItemRelativeLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewHolder.getSelectedImg().getVisibility() == View.VISIBLE) {
                    viewHolder.getSelectedImg().setVisibility(View.INVISIBLE);
                    viewHolder.getUnSelectImg().setVisibility(View.VISIBLE);

                    delete(singerInfo);

                } else {
                    viewHolder.getSelectedImg().setVisibility(View.VISIBLE);
                    viewHolder.getUnSelectImg().setVisibility(View.INVISIBLE);
                    if (!contains(singerInfo)) {
                        mSelectDatas.add(singerInfo);
                    }
                }
            }
        });
    }

    /**
     * @param singerInfo
     */
    private void delete(SingerInfo singerInfo) {
        if (mSelectDatas != null && mSelectDatas.size() > 0) {
            for (int i = 0; i < mSelectDatas.size(); i++) {
                SingerInfo temp = mSelectDatas.get(i);
                if (temp.getImageUrl().equals(singerInfo.getImageUrl())) {
                    mSelectDatas.remove(i);
                    break;
                }
            }
        }
    }

    /**
     * @param singerInfo
     * @return
     */
    private boolean contains(SingerInfo singerInfo) {
        if (mSelectDatas != null && mSelectDatas.size() > 0) {
            for (int i = 0; i < mSelectDatas.size(); i++) {
                SingerInfo temp = mSelectDatas.get(i);
                if (temp.getImageUrl().equals(singerInfo.getImageUrl())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * @return
     */
    public List<SingerInfo> getSelectDatas() {
        return mSelectDatas;
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    /////////////////////////////////////////////////////

    class SearchSingerViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private ImageView singPicImg;
        /**
         * item底部布局
         */
        private RelativeLayout listItemRelativeLayout;
        /**
         * 未选择
         */
        private ImageView unSelectImg;
        /**
         * 选择
         */
        private ImageView selectedImg;

        public SearchSingerViewHolder(View view) {
            super(view);
            this.view = view;
        }

        public ImageView getSingPicImg() {
            if (singPicImg == null) {
                singPicImg = view.findViewById(R.id.singPic);
            }
            return singPicImg;
        }

        public RelativeLayout getListItemRelativeLayout() {
            if (listItemRelativeLayout == null) {
                listItemRelativeLayout = view.findViewById(R.id.itemBG);
            }
            return listItemRelativeLayout;
        }

        public ImageView getUnSelectImg() {

            if (unSelectImg == null) {
                unSelectImg = view.findViewById(R.id.unselect);
            }
            return unSelectImg;
        }

        public ImageView getSelectedImg() {
            if (selectedImg == null) {
                selectedImg = view.findViewById(R.id.selected);
            }
            return selectedImg;
        }
    }

}
