package com.zlm.hp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlm.hp.db.util.AudioInfoDB;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.fragment.SongFragment;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.ui.R;
import com.zlm.hp.widget.IconfontImageButtonTextView;
import com.zlm.hp.widget.ListItemRelativeLayout;

import java.util.ArrayList;

/**
 * @Description: 歌曲适配器
 * @author: zhangliangming
 * @date: 2018-09-24 1:16
 **/
public class AudioAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private Context mContext;
    private ArrayList<AudioInfo> mDatas;
    private int mSongType;

    public AudioAdapter(Context context, ArrayList<AudioInfo> datas, int songType) {
        this.mContext = context;
        this.mDatas = datas;
        this.mSongType = songType;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_song, null, false);
        AudioViewHolder holder = new AudioViewHolder(view);
        return holder;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof AudioViewHolder && position < mDatas.size()) {
            AudioInfo audioInfo = mDatas.get(position);
            reshViewHolder(position, (AudioViewHolder) viewHolder, audioInfo);
        }
    }

    /**
     * 刷新ui
     *
     * @param position
     * @param viewHolder
     * @param audioInfo
     */
    private void reshViewHolder(int position, final AudioViewHolder viewHolder, final AudioInfo audioInfo) {

        viewHolder.getSongIndexTv().setText((position + 1) + "");
        viewHolder.getSongIndexTv().setVisibility(View.VISIBLE);

        viewHolder.getSongNameTv().setText(audioInfo.getSongName());
        viewHolder.getSingerNameTv().setText(audioInfo.getSingerName());
        viewHolder.getMenuLinearLayout().setVisibility(View.GONE);
        viewHolder.getListItemRelativeLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSongType == SongFragment.SONG_TYPE_LOCAL) {
                    //如果是本地歌曲列表，点击列表时，需要替换当前的播放列表为本地歌曲列表
                    AudioPlayerManager.newInstance(mContext).playSong(AudioInfoDB.getLocalAudios(mContext), audioInfo);
                } else {
                    AudioPlayerManager.newInstance(mContext).playSong(audioInfo);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    class AudioViewHolder extends RecyclerView.ViewHolder {
        private View view;
        /**
         * item底部布局
         */
        private ListItemRelativeLayout listItemRelativeLayout;

        /**
         * 更多按钮
         */
        private ImageView moreImg;
        /**
         * 状态标记view
         */
        private View statusView;
        /**
         * 歌曲索引
         */
        private TextView songIndexTv;

        /**
         * 歌曲名称
         */
        private TextView songNameTv;

        /**
         * 歌手名称
         */
        private TextView singerNameTv;

        /**
         * 是否存在本地
         */
        private ImageView islocalImg;

        //、、、、、、、、、、、、、、、、、、、、更多菜单、、、、、、、、、、、、、、、、、、、、、、、、

        /**
         * 更多按钮
         */
        private ImageView itemMoreImg;

        /**
         * 菜单
         */
        private LinearLayout menuLinearLayout;
        /**
         * 不喜欢按钮
         */
        private IconfontImageButtonTextView unLikeImgBtn;
        /**
         * 不喜欢按钮
         */
        private IconfontImageButtonTextView likedImgBtn;
        /**
         * 下载布局
         */
        private RelativeLayout downloadParentRl;

        /**
         * 下载完成按钮
         */
        private ImageView downloadedImg;
        /**
         * 下载按钮
         */
        private ImageView downloadImg;
        /**
         * 详情按钮
         */
        private IconfontImageButtonTextView detailImgBtn;

        /**
         * 删除按钮
         */
        private IconfontImageButtonTextView deleteImgBtn;

        public AudioViewHolder(View view) {
            super(view);
            this.view = view;
        }

        public ListItemRelativeLayout getListItemRelativeLayout() {
            if (listItemRelativeLayout == null) {
                listItemRelativeLayout = view.findViewById(R.id.itemBG);
            }
            return listItemRelativeLayout;
        }

        public ImageView getMoreImg() {
            if (moreImg == null) {
                moreImg = view.findViewById(R.id.item_more);
            }
            return moreImg;
        }

        public View getStatusView() {
            if (statusView == null) {
                statusView = view.findViewById(R.id.status);
            }
            return statusView;
        }

        public TextView getSongNameTv() {
            if (songNameTv == null) {
                songNameTv = view.findViewById(R.id.songName);
            }
            return songNameTv;
        }

        public TextView getSingerNameTv() {
            if (singerNameTv == null) {
                singerNameTv = view.findViewById(R.id.singerName);
            }
            return singerNameTv;
        }

        public ImageView getIslocalImg() {
            if (islocalImg == null) {
                islocalImg = view.findViewById(R.id.islocal);
            }
            return islocalImg;
        }

        public TextView getSongIndexTv() {

            if (songIndexTv == null) {
                songIndexTv = view.findViewById(R.id.songIndex);
            }
            return songIndexTv;
        }

        public ImageView getItemMoreImg() {
            if (itemMoreImg == null) {
                itemMoreImg = view.findViewById(R.id.item_more);
            }
            return itemMoreImg;
        }

        public LinearLayout getMenuLinearLayout() {
            if (menuLinearLayout == null) {
                menuLinearLayout = view.findViewById(R.id.menu);
            }
            return menuLinearLayout;
        }

        public IconfontImageButtonTextView getLikedImgBtn() {
            if (likedImgBtn == null) {
                likedImgBtn = view.findViewById(R.id.liked_menu);
            }
            likedImgBtn.setConvert(true);
            return likedImgBtn;
        }

        public IconfontImageButtonTextView getUnLikeImgBtn() {
            if (unLikeImgBtn == null) {
                unLikeImgBtn = view.findViewById(R.id.unlike_menu);
            }
            unLikeImgBtn.setConvert(true);
            return unLikeImgBtn;
        }

        public RelativeLayout getDownloadParentRl() {
            if (downloadParentRl == null) {
                downloadParentRl = view.findViewById(R.id.downloadParent);
            }
            return downloadParentRl;
        }

        public ImageView getDownloadedImg() {
            if (downloadedImg == null) {
                downloadedImg = view.findViewById(R.id.downloaded_menu);
            }
            return downloadedImg;
        }

        public ImageView getDownloadImg() {
            if (downloadImg == null) {
                downloadImg = view.findViewById(R.id.download_menu);
            }
            return downloadImg;
        }

        public IconfontImageButtonTextView getDetailImgBtn() {
            if (detailImgBtn == null) {
                detailImgBtn = view.findViewById(R.id.detail_menu);
            }
            detailImgBtn.setConvert(true);
            return detailImgBtn;
        }

        public IconfontImageButtonTextView getDeleteImgBtn() {
            if (deleteImgBtn == null) {
                deleteImgBtn = view.findViewById(R.id.delete_menu);
            }
            deleteImgBtn.setConvert(true);
            return deleteImgBtn;
        }

    }
}
