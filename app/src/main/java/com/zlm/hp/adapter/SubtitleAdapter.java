package com.zlm.hp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.zlm.hp.entity.SubtitleInfo;
import com.zlm.hp.ui.R;
import com.zlm.hp.widget.SubtitleListItemRelativeLayout;

import java.util.ArrayList;

/**
 * @Description: 字幕管理
 * @author: zhangliangming
 * @date: 2019-01-21 21:04
 **/
public class SubtitleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<SubtitleInfo> mDatas;
    private ItemEvent mItemEvent;


    private String mSelectId;
    private int mSelectIndex = -1;

    public SubtitleAdapter(Context context, ArrayList<SubtitleInfo> datas) {
        this.mContext = context;
        this.mDatas = datas;

        mSelectId = "";
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_subtitle, null, false);
        FileViewHolder holder = new FileViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof FileViewHolder && position < mDatas.size()) {
            reshFileViewHolder((FileViewHolder) viewHolder, position);
        }
    }

    /**
     * 文件
     *
     * @param viewHolder
     * @param position
     */
    private void reshFileViewHolder(final FileViewHolder viewHolder, final int position) {
        final SubtitleInfo subtitleInfo = mDatas.get(position);
        viewHolder.getFimeNamTextView().setTextColor(Color.WHITE);
        viewHolder.getFimeNamTextView().setText(subtitleInfo.getFileName());
        final String key = subtitleInfo.getDownloadUrl().hashCode() + "";
        if (key.equals(mSelectId)) {
            viewHolder.getFileRadioButton().setChecked(true);
        } else {
            viewHolder.getFileRadioButton().setChecked(false);
        }
        viewHolder.getListItemRelativeLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectId = key;
                if (mSelectIndex != -1) {
                    notifyItemChanged(mSelectIndex);
                }
                mSelectIndex = position;
                notifyItemChanged(mSelectIndex);

                if (mItemEvent != null) {
                    mItemEvent.subtitleClick(subtitleInfo);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    /**
     * @Description: 文件
     * @author: zhangliangming
     * @date: 2018-05-22 22:34
     **/
    class FileViewHolder extends RecyclerView.ViewHolder {
        private View view;
        /**
         * item底部布局
         */
        private SubtitleListItemRelativeLayout listItemRelativeLayout;

        /**
         * 文件名
         */
        private TextView fimeNamTextView;
        /**
         * 文件选择按钮
         */
        private RadioButton fileRadioButton;

        public FileViewHolder(View view) {
            super(view);
            this.view = view;
        }

        public SubtitleListItemRelativeLayout getListItemRelativeLayout() {
            if (listItemRelativeLayout == null) {
                listItemRelativeLayout = view.findViewById(R.id.itemBG);
            }
            return listItemRelativeLayout;
        }

        public TextView getFimeNamTextView() {
            if (fimeNamTextView == null) {
                fimeNamTextView = view.findViewById(R.id.filename);
            }
            return fimeNamTextView;
        }

        public RadioButton getFileRadioButton() {
            if (fileRadioButton == null) {
                fileRadioButton = view.findViewById(R.id.fileRadioButton);
            }
            return fileRadioButton;
        }
    }

    public void setItemEvent(ItemEvent mItemEvent) {
        this.mItemEvent = mItemEvent;
    }

    public interface ItemEvent {
        public void subtitleClick(SubtitleInfo subtitleInfo);
    }
}
