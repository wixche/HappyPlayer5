package com.zlm.hp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlm.down.entity.DownloadTask;
import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.db.util.AudioInfoDB;
import com.zlm.hp.db.util.DownloadTaskDB;
import com.zlm.hp.db.util.DownloadThreadInfoDB;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.entity.Category;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.manager.DownloadAudioManager;
import com.zlm.hp.ui.R;
import com.zlm.hp.util.FileUtil;
import com.zlm.hp.util.ToastUtil;
import com.zlm.hp.widget.IconfontImageButtonTextView;
import com.zlm.hp.widget.IconfontTextView;
import com.zlm.hp.widget.ListItemRelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 下载音乐
 * Created by zhangliangming on 2017/9/9.
 */
public class DownloadMusicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    /**
     * 标题
     */
    public final static int CATEGORYTITLE = 0;
    /**
     * item正在下载
     */
    public final static int ITEMDownloading = 1;
    /**
     * item已经下载
     */
    public final static int ITEMDownloaded = 2;

    private Context mContext;
    private ArrayList<Category> mDatas;

    private ConfigInfo mConfigInfo;
    private String mOldPlayHash = "";
    /**
     * 菜单打开索引
     */
    private int mMenuOpenIndex = -1;


    public DownloadMusicAdapter(Context context, ArrayList<Category> datas) {
        this.mContext = context;
        this.mDatas = datas;
        mConfigInfo = ConfigInfo.obtain();
        mOldPlayHash = mConfigInfo.getPlayHash();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = null;
        if (viewType == CATEGORYTITLE) {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_title, null, false);
            CategoryTitleViewHolder holder = new CategoryTitleViewHolder(view);
            return holder;
        } else if (viewType == ITEMDownloading) {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_downloading, null, false);
            return new DownloadingMusicViewHolder(view);

        } else if (viewType == ITEMDownloaded) {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_song, null, false);

            return new DownloadedMusicViewHolder(view);

        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        if (viewHolder instanceof CategoryTitleViewHolder) {
            CategoryTitleViewHolder categoryViewHolder = (CategoryTitleViewHolder) viewHolder;
            String mCategoryName = (String) getItem(position);

            categoryViewHolder.getCategoryTextTextView().setText(
                    mCategoryName + "("
                            + getItemSizeByCategoryName(mCategoryName) + ")");
        } else if (viewHolder instanceof DownloadingMusicViewHolder) {
            AudioInfo audioInfo = (AudioInfo) getItem(position);
            reshViewHolder(position, (DownloadingMusicViewHolder) viewHolder, audioInfo);
        } else if (viewHolder instanceof DownloadedMusicViewHolder) {
            AudioInfo audioInfo = (AudioInfo) getItem(position);
            reshViewHolder(position, (DownloadedMusicViewHolder) viewHolder, audioInfo);
        }


    }

    /**
     * 通过种类名称来获取该分类下的歌曲数目
     *
     * @param mCategoryName
     * @return
     */
    private int getItemSizeByCategoryName(String mCategoryName) {
        int count = 0;

        if (null != mDatas) {
            // 所有分类中item的总和是ListVIew Item的总个数
            for (Category category : mDatas) {
                if (category.getCategoryName().equals(mCategoryName)) {
                    count = category.getItemCount();
                    break;
                }
            }
        }
        return count;
    }

    /**
     * 下载中刷新ui
     *
     * @param position
     * @param viewHolder
     */
    private void reshViewHolder(final int position, final DownloadingMusicViewHolder viewHolder, final AudioInfo audioInfo) {
        viewHolder.getTitleTv().setText(audioInfo.getTitle());
        String fileSizeText = FileUtil.getFileSize(audioInfo.getFileSize());
        int downloadedSize = DownloadThreadInfoDB.getDownloadedSize(mContext, audioInfo.getHash(), DownloadAudioManager.mThreadNum);
        String downloadSizeText = FileUtil.getFileSize(downloadedSize);
        viewHolder.getDlTipTv().setText(downloadSizeText + "/" + fileSizeText);

        //获取下载任务
        DownloadTask downloadTask = DownloadAudioManager.getInstance(mContext).getDownloadTask(audioInfo.getHash());
        if (downloadTask == null) {
            //点击下载
            viewHolder.getDownloadingImg().setVisibility(View.VISIBLE);
            viewHolder.getDownloadPauseImg().setVisibility(View.INVISIBLE);
            viewHolder.getOpTipTv().setText(mContext.getString(R.string.download_goon_text));
        } else {
            if (downloadTask.getStatus() == DownloadTask.STATUS_WAIT) {
                //等待下载
                viewHolder.getDownloadingImg().setVisibility(View.INVISIBLE);
                viewHolder.getDownloadPauseImg().setVisibility(View.VISIBLE);
                viewHolder.getOpTipTv().setText(mContext.getString(R.string.download_wait_text));
            } else if (downloadTask.getStatus() == DownloadTask.STATUS_DOWNLOADING) {

                //点击暂停
                viewHolder.getDownloadingImg().setVisibility(View.INVISIBLE);
                viewHolder.getDownloadPauseImg().setVisibility(View.VISIBLE);

                viewHolder.getOpTipTv().setText(mContext.getString(R.string.download_pause_text));
            } else {
                //点击下载
                viewHolder.getDownloadingImg().setVisibility(View.VISIBLE);
                viewHolder.getDownloadPauseImg().setVisibility(View.INVISIBLE);
                viewHolder.getOpTipTv().setText(mContext.getString(R.string.download_goon_text));
            }
        }
        //列表点击事件
        viewHolder.getListItemRelativeLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取下载任务
                DownloadTask downloadTask = DownloadAudioManager.getInstance(mContext).getDownloadTask(audioInfo.getHash());
                if (downloadTask == null) {
                    //点击下载
                    DownloadAudioManager.getInstance(mContext).addTask(audioInfo);

                } else {
                    if (downloadTask.getStatus() == DownloadTask.STATUS_WAIT) {
                        //等待下载
                        DownloadAudioManager.getInstance(mContext).cancelTask(downloadTask.getTaskId());

                    } else if (downloadTask.getStatus() == DownloadTask.STATUS_DOWNLOADING) {
                        //点击暂停
                        DownloadAudioManager.getInstance(mContext).pauseTask(downloadTask.getTaskId());
                    } else {
                        //点击下载
                        DownloadAudioManager.getInstance(mContext).addTask(audioInfo);
                    }
                }
            }
        });

        //删除
        viewHolder.getDeleteTv().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取下载任务
                DownloadTask downloadTask = DownloadAudioManager.getInstance(mContext).getDownloadTask(audioInfo.getHash());
                if (downloadTask == null) {
                    //删除
                    DownloadTaskDB.delete(mContext, audioInfo.getHash(), DownloadAudioManager.mThreadNum);
                } else {
                    //取消任务（任务正在下载）
                    DownloadAudioManager.getInstance(mContext).cancelTask(downloadTask.getTaskId());
                }
            }
        });

    }

    /**
     *
     */
    public void resetMenuOpenIndex() {
        mMenuOpenIndex = -1;
    }


    /**
     * 下载完成刷新ui
     *
     * @param position
     * @param viewHolder
     */
    private void reshViewHolder(final int position, final DownloadedMusicViewHolder viewHolder, final AudioInfo audioInfo) {

        //1更多按钮点击事件
        viewHolder.getItemMoreImg().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (position != mMenuOpenIndex) {
                    if (mMenuOpenIndex != -1) {
                        notifyItemChanged(mMenuOpenIndex);
                    }
                    mMenuOpenIndex = position;
                    notifyItemChanged(mMenuOpenIndex);
                } else {
                    if (mMenuOpenIndex != -1) {
                        notifyItemChanged(mMenuOpenIndex);
                        mMenuOpenIndex = -1;
                    }
                }
            }
        });
        //2展开或者隐藏菜单
        if (position == mMenuOpenIndex) {

            //下载/未下载
            viewHolder.getDownloadImg().setVisibility(View.GONE);
            viewHolder.getDownloadedImg().setVisibility(View.GONE);
            //删除
            viewHolder.getDeleteImgBtn().setVisibility(View.VISIBLE);

            //喜欢/不喜欢
            if (AudioInfoDB.isLikeAudioExists(mContext, audioInfo.getHash())) {
                viewHolder.getUnLikeImgBtn().setVisibility(View.GONE);
                viewHolder.getLikedImgBtn().setVisibility(View.VISIBLE);
            } else {
                viewHolder.getUnLikeImgBtn().setVisibility(View.VISIBLE);
                viewHolder.getLikedImgBtn().setVisibility(View.GONE);
            }

            viewHolder.getMenuLinearLayout().setVisibility(View.VISIBLE);
        } else {
            viewHolder.getMenuLinearLayout().setVisibility(View.GONE);
        }

        //喜欢/不喜欢
        viewHolder.getUnLikeImgBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioInfo != null) {
                    if (!AudioInfoDB.isLikeAudioExists(mContext, audioInfo.getHash())) {
                        boolean result = AudioInfoDB.addLikeAudio(mContext, audioInfo, true);
                        if (result) {
                            viewHolder.getUnLikeImgBtn().setVisibility(View.GONE);
                            viewHolder.getLikedImgBtn().setVisibility(View.VISIBLE);
                            ToastUtil.showTextToast(mContext, mContext.getResources().getString(R.string.like_tip_text));
                        }
                    }
                }
            }
        });
        viewHolder.getLikedImgBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioInfo != null) {
                    if (AudioInfoDB.isLikeAudioExists(mContext, audioInfo.getHash())) {
                        boolean result = AudioInfoDB.deleteLikeAudio(mContext, audioInfo.getHash(), true);
                        if (result) {
                            viewHolder.getUnLikeImgBtn().setVisibility(View.VISIBLE);
                            viewHolder.getLikedImgBtn().setVisibility(View.GONE);

                            ToastUtil.showTextToast(mContext, mContext.getResources().getString(R.string.unlike_tip_text));
                        }
                    }
                }
            }
        });

        //删除
        viewHolder.getDeleteImgBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //隐藏更多菜单
                mMenuOpenIndex = -1;
                viewHolder.getMenuLinearLayout().setVisibility(View.GONE);
                DownloadTaskDB.delete(mContext, audioInfo.getHash(), DownloadAudioManager.mThreadNum);
                ToastUtil.showTextToast(mContext, mContext.getResources().getString(R.string.remove_success_tip_text));
            }
        });

        viewHolder.getSingerNameTv().setText(audioInfo.getSingerName());
        viewHolder.getSongNameTv().setText(audioInfo.getSongName());

        if (audioInfo.getHash().equals(mConfigInfo.getPlayHash())) {
            viewHolder.getStatusView().setVisibility(View.VISIBLE);
            mOldPlayHash = audioInfo.getHash();
        } else {
            viewHolder.getStatusView().setVisibility(View.INVISIBLE);
        }

        viewHolder.getListItemRelativeLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int oldIndex = getPlayIndexPosition(mConfigInfo.getPlayHash());
                if (oldIndex == position) {
                    AudioPlayerManager.getInstance(mContext).playOrPause();
                    return;
                }

                mOldPlayHash = audioInfo.getHash();
                //如果是本地歌曲列表，点击列表时，需要替换当前的播放列表为本地歌曲列表
                AudioPlayerManager.getInstance(mContext).playSong(AudioInfoDB.getDownloadedAudios(mContext), audioInfo);

                if (oldIndex != -1) {
                    notifyItemChanged(oldIndex);
                }
                int newIndex = getPlayIndexPosition(audioInfo.getHash());
                if (newIndex != -1) {
                    notifyItemChanged(newIndex);
                }
            }
        });
    }

    /**
     * 刷新view
     *
     * @param hash
     */
    public void reshViewHolder(String hash) {
        if (hash == null || hash.equals("")) {
            return;
        }
        int oldIndex = getPlayIndexPosition(mOldPlayHash);
        if (oldIndex != -1) {
            mOldPlayHash = "";
            notifyItemChanged(oldIndex);
        }
        int index = getPlayIndexPosition(hash);
        if (index != -1) {
            notifyItemChanged(index);
        }
    }

    /**
     * 通过sid获取当前的播放索引
     *
     * @param hash
     * @return
     */
    private int getPlayIndexPosition(String hash) {
        int index = -1;
        // 异常情况处理
        if (null == mDatas) {
            return -1;
        }
        if (TextUtils.isEmpty(hash)) return -1;
        int count = 0;
        for (int i = 0; i < mDatas.size(); i++) {
            Category category = mDatas.get(i);
            List<AudioInfo> downloadInfoList = category.getCategoryItem();
            int j = 0;
            for (; j < downloadInfoList.size(); j++) {
                AudioInfo audioInfo = (AudioInfo) downloadInfoList.get(j);

                if (audioInfo.getHash().equals(hash)) {

                    index = count + j + 1;

                    break;
                }
            }
            count += category.getCount();
        }

        return index;
    }


    /**
     * 根据索引获取内容
     *
     * @param position
     * @return
     */
    private Object getItem(int position) {

        // 异常情况处理
        if (null == mDatas || position < 0 || position > getItemCount()) {
            return null;
        }


        // 同一分类内，第一个元素的索引值
        int categroyFirstIndex = 0;

        for (Category category : mDatas) {
            int size = category.getCount();
            // 在当前分类中的索引值
            int categoryIndex = position - categroyFirstIndex;
            // item在当前分类内
            if (categoryIndex < size) {
                return category.getItem(categoryIndex);
            }
            // 索引移动到当前分类结尾，即下一个分类第一个元素索引
            categroyFirstIndex += size;
        }

        return null;
    }


    @Override
    public int getItemCount() {
        int count = 0;

        if (null != mDatas) {
            // 所有分类中item的总和是ListVIew Item的总个数
            for (Category category : mDatas) {
                count += category.getCount();
            }
        }
        return count;
    }


    @Override
    public int getItemViewType(int position) {
        // 异常情况处理
        if (null == mDatas || position < 0 || position > getItemCount()) {
            return CATEGORYTITLE;
        }

        int categroyFirstIndex = 0;

        for (Category category : mDatas) {
            int size = category.getCount();
            // 在当前分类中的索引值
            int categoryIndex = position - categroyFirstIndex;
            if (categoryIndex == 0) {
                return CATEGORYTITLE;
            } else if (categoryIndex < size) {
                break;
            }
            categroyFirstIndex += size;
        }

        if (getItem(position) instanceof String) {
            return CATEGORYTITLE;
        }

        AudioInfo audioInfo = (AudioInfo) getItem(position);
        if (audioInfo.getStatus() == AudioInfo.STATUS_FINISH) {
            return ITEMDownloaded;
        }
        return ITEMDownloading;
    }

    class CategoryTitleViewHolder extends RecyclerView.ViewHolder {
        private View itemView;
        private TextView categoryTextTextView;
        private View lineView;

        public CategoryTitleViewHolder(View view) {
            super(view);
            this.itemView = view;
        }

        public TextView getCategoryTextTextView() {
            if (categoryTextTextView == null) {
                categoryTextTextView = itemView
                        .findViewById(R.id.category_text);
            }
            return categoryTextTextView;
        }

    }


    /**
     * 下载中
     */
    class DownloadingMusicViewHolder extends RecyclerView.ViewHolder {
        private View view;
        /**
         * item底部布局
         */
        private ListItemRelativeLayout listItemRelativeLayout;
        /**
         * 下载中
         */
        private IconfontTextView downloadingImg;

        /**
         * 下载暂停
         */
        private IconfontTextView downloadPauseImg;

        /**
         * 标题
         */
        private TextView titleTv;
        /**
         * 状态提示
         */
        private TextView opTipTv;
        /**
         * 下载提示
         */
        private TextView dlTipTv;
        /**
         * 删除
         */
        private IconfontTextView deleteTv;

        public DownloadingMusicViewHolder(View view) {
            super(view);
            this.view = view;
        }

        public ListItemRelativeLayout getListItemRelativeLayout() {
            if (listItemRelativeLayout == null) {
                listItemRelativeLayout = view.findViewById(R.id.itemBG);
            }
            return listItemRelativeLayout;
        }


        public IconfontTextView getDownloadingImg() {
            if (downloadingImg == null) {
                downloadingImg = view.findViewById(R.id.download_img);
            }
            return downloadingImg;
        }

        public IconfontTextView getDownloadPauseImg() {
            if (downloadPauseImg == null) {
                downloadPauseImg = view.findViewById(R.id.pause_img);
            }
            return downloadPauseImg;
        }

        public TextView getTitleTv() {
            if (titleTv == null) {
                titleTv = view.findViewById(R.id.titleName);
            }
            return titleTv;
        }

        public TextView getOpTipTv() {
            if (opTipTv == null) {
                opTipTv = view.findViewById(R.id.download_tip);
            }
            return opTipTv;
        }

        public TextView getDlTipTv() {
            if (dlTipTv == null) {
                dlTipTv = view.findViewById(R.id.downloadSizeText);
            }
            return dlTipTv;
        }

        public IconfontTextView getDeleteTv() {
            if (deleteTv == null) {
                deleteTv = view.findViewById(R.id.delete);
            }
            return deleteTv;
        }
    }

    /**
     * 下载完成
     */


    class DownloadedMusicViewHolder extends RecyclerView.ViewHolder {
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
         * 删除按钮
         */
        private IconfontImageButtonTextView deleteImgBtn;

        public DownloadedMusicViewHolder(View view) {
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

        public IconfontImageButtonTextView getDeleteImgBtn() {
            if (deleteImgBtn == null) {
                deleteImgBtn = view.findViewById(R.id.delete_menu);
            }
            deleteImgBtn.setConvert(true);
            return deleteImgBtn;
        }
    }
}
