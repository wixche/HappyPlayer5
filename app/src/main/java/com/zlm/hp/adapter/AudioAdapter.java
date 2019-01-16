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

import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.db.util.AudioInfoDB;
import com.zlm.hp.db.util.DownloadThreadInfoDB;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.fragment.SongFragment;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.manager.DownloadAudioManager;
import com.zlm.hp.manager.OnLineAudioManager;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.ui.R;
import com.zlm.hp.util.ToastUtil;
import com.zlm.hp.widget.IconfontImageButtonTextView;
import com.zlm.hp.widget.ListItemRelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 歌曲适配器
 * @author: zhangliangming
 * @date: 2018-09-24 1:16
 **/
public class AudioAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private Context mContext;
    private ArrayList<AudioInfo> mDatas;
    private int mSongType;
    private String mOldPlayHash = "";
    private ConfigInfo mConfigInfo;

    /**
     * 菜单打开索引
     */
    private int mMenuOpenIndex = -1;

    public AudioAdapter(Context context, ArrayList<AudioInfo> datas, int songType) {
        this.mContext = context;
        this.mDatas = datas;
        this.mSongType = songType;
        mConfigInfo = ConfigInfo.obtain();
        mOldPlayHash = mConfigInfo.getPlayHash();
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
     *
     */
    public void resetMenuOpenIndex() {
        mMenuOpenIndex = -1;
    }


    /**
     * 刷新ui
     *
     * @param position
     * @param viewHolder
     * @param audioInfo
     */
    private void reshViewHolder(final int position, final AudioViewHolder viewHolder, final AudioInfo audioInfo) {
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
            if (mSongType == SongFragment.SONG_TYPE_LOCAL) {
                //本地歌曲
                //喜欢/不喜欢
                if (AudioInfoDB.isLikeAudioExists(mContext, audioInfo.getHash())) {
                    viewHolder.getUnLikeImgBtn().setVisibility(View.GONE);
                    viewHolder.getLikedImgBtn().setVisibility(View.VISIBLE);
                } else {
                    viewHolder.getUnLikeImgBtn().setVisibility(View.VISIBLE);
                    viewHolder.getLikedImgBtn().setVisibility(View.GONE);
                }
                //下载/未下载
                viewHolder.getDownloadImg().setVisibility(View.GONE);
                viewHolder.getDownloadedImg().setVisibility(View.GONE);
                //删除
                viewHolder.getDeleteImgBtn().setVisibility(View.VISIBLE);
            } else {
                if (mSongType == SongFragment.SONG_TYPE_LIKE) {
                    //喜欢
                    viewHolder.getUnLikeImgBtn().setVisibility(View.GONE);
                    viewHolder.getLikedImgBtn().setVisibility(View.GONE);
                    //删除
                    viewHolder.getDeleteImgBtn().setVisibility(View.VISIBLE);
                } else {
                    if (mSongType == SongFragment.SONG_TYPE_RECENT) {
                        //删除
                        viewHolder.getDeleteImgBtn().setVisibility(View.VISIBLE);
                    } else {
                        //删除
                        viewHolder.getDeleteImgBtn().setVisibility(View.GONE);
                    }
                    //喜欢/不喜欢
                    if (AudioInfoDB.isLikeAudioExists(mContext, audioInfo.getHash())) {
                        viewHolder.getUnLikeImgBtn().setVisibility(View.GONE);
                        viewHolder.getLikedImgBtn().setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.getUnLikeImgBtn().setVisibility(View.VISIBLE);
                        viewHolder.getLikedImgBtn().setVisibility(View.GONE);
                    }
                }

                //下载完成
                if (audioInfo.getType() == AudioInfo.TYPE_LOCAL || AudioInfoDB.isDownloadedAudioExists(mContext, audioInfo.getHash())) {
                    viewHolder.getDownloadImg().setVisibility(View.INVISIBLE);
                    viewHolder.getDownloadedImg().setVisibility(View.VISIBLE);
                    viewHolder.getIslocalImg().setVisibility(View.VISIBLE);
                } else {
                    viewHolder.getDownloadImg().setVisibility(View.VISIBLE);
                    viewHolder.getDownloadedImg().setVisibility(View.INVISIBLE);
                    int downloadedSize = DownloadThreadInfoDB.getDownloadedSize(mContext, audioInfo.getHash(), OnLineAudioManager.mThreadNum);
                    if (downloadedSize >= audioInfo.getFileSize()) {
                        viewHolder.getIslocalImg().setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.getIslocalImg().setVisibility(View.GONE);
                    }
                }
            }

            viewHolder.getMenuLinearLayout().setVisibility(View.VISIBLE);
        } else {
            viewHolder.getMenuLinearLayout().setVisibility(View.GONE);
        }


        if (audioInfo.getHash().equals(mConfigInfo.getPlayHash())) {
            viewHolder.getStatusView().setVisibility(View.VISIBLE);
            mOldPlayHash = audioInfo.getHash();
        } else {
            viewHolder.getStatusView().setVisibility(View.INVISIBLE);

        }

        viewHolder.getSongIndexTv().setText(((position + 1) < 10 ? "0" + (position + 1) : (position + 1) + ""));
        viewHolder.getSongIndexTv().setVisibility(View.VISIBLE);
        viewHolder.getSongNameTv().setText(audioInfo.getSongName());
        viewHolder.getSingerNameTv().setText(audioInfo.getSingerName());

        //下载完成
        if (audioInfo.getType() == AudioInfo.TYPE_LOCAL || AudioInfoDB.isDownloadedAudioExists(mContext, audioInfo.getHash())) {
            viewHolder.getIslocalImg().setVisibility(View.VISIBLE);
        } else {
            int downloadedSize = DownloadThreadInfoDB.getDownloadedSize(mContext, audioInfo.getHash(), OnLineAudioManager.mThreadNum);
            if (downloadedSize >= audioInfo.getFileSize()) {
                viewHolder.getIslocalImg().setVisibility(View.VISIBLE);
            } else {
                viewHolder.getIslocalImg().setVisibility(View.GONE);
            }
        }


        viewHolder.getListItemRelativeLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //列表点击后，还原更多菜单
                if (mMenuOpenIndex != -1) {
                    notifyItemChanged(mMenuOpenIndex);
                    mMenuOpenIndex = -1;
                }

                int oldIndex = getAudioIndex(mConfigInfo.getPlayHash());
                if (oldIndex == position) {
                    AudioPlayerManager.getInstance(mContext).playOrPause();
                    return;
                }

                mOldPlayHash = audioInfo.getHash();
                if (mSongType == SongFragment.SONG_TYPE_LOCAL) {
                    //如果是本地歌曲列表，点击列表时，需要替换当前的播放列表为本地歌曲列表
                    AudioPlayerManager.getInstance(mContext).playSong(AudioInfoDB.getLocalAudios(mContext), audioInfo);
                } else {
                    AudioPlayerManager.getInstance(mContext).playSongAndAdd(audioInfo);
                }

                if (oldIndex != -1) {
                    notifyItemChanged(oldIndex);
                }
                int newIndex = getAudioIndex(audioInfo.getHash());
                if (newIndex != -1) {
                    notifyItemChanged(newIndex);
                }
            }
        });


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

                if (mSongType == SongFragment.SONG_TYPE_LIKE) {
                    //移除喜欢
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
                } else if (mSongType == SongFragment.SONG_TYPE_RECENT) {
                    //最近移除
                    if (audioInfo != null) {
                        if (AudioInfoDB.isRecentAudioExists(mContext, audioInfo.getHash())) {
                            boolean result = AudioInfoDB.deleteRecentAudio(mContext, audioInfo.getHash(), true);
                            if (result) {
                                ToastUtil.showTextToast(mContext, mContext.getResources().getString(R.string.remove_success_tip_text));
                            }
                        }
                    }
                } else {
                    //本地歌曲删除
                    //1.先判断是否是当前正在播放的歌曲
                    //2.移除歌曲
                    //3.发通知

                    String curHash = audioInfo.getHash();
                    //是否是正在播放歌曲
                    boolean flag = mConfigInfo.getPlayHash().equals(audioInfo.getHash());
                    //获取下一首歌曲
                    List<AudioInfo> audioInfos = mConfigInfo.getAudioInfos();
                    AudioInfo nextAudioInfo = null;
                    boolean hasNext = false;
                    int curHashIndex = -1;
                    for (int i = 0; i < audioInfos.size(); i++) {
                        AudioInfo temp = audioInfos.get(i);
                        if (hasNext) {
                            nextAudioInfo = temp;
                            break;
                        }
                        if (temp.getHash().equals(curHash)) {
                            curHashIndex = i;
                            if (!flag) break;
                            hasNext = true;
                        }
                    }

                    //播放器如果正在播放
                    if (flag)
                        AudioPlayerManager.getInstance(mContext).stop();
                    //移除数据
                    if (curHashIndex != -1) {
                        audioInfos.remove(curHashIndex);
                        mConfigInfo.setAudioInfos(audioInfos);
                    }

                    //删除歌曲
                    boolean deleteFlag = AudioInfoDB.deleteAudio(mContext, audioInfo.getHash(), true);
                    if (deleteFlag) {
                        ToastUtil.showTextToast(mContext, mContext.getResources().getString(R.string.remove_success_tip_text));
                    }

                    //如果是正在播放歌曲，发播放下一首通知
                    if (!flag) return;
                    if (nextAudioInfo != null) {
                        mConfigInfo.setPlayHash(nextAudioInfo.getHash());
                        nextAudioInfo.setPlayProgress(0);
                        AudioBroadcastReceiver.sendPlayInitReceiver(mContext, nextAudioInfo);
                    } else {
                        mConfigInfo.setPlayHash("");
                        AudioBroadcastReceiver.sendNullReceiver(mContext);
                    }

                    mConfigInfo.save();
                }
            }
        });

        //未下载
        viewHolder.getDownloadImg().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean flag = DownloadAudioManager.getInstance(mContext).isDownloadAudioExists(audioInfo.getHash());
                if (flag) {
                    ToastUtil.showTextToast(mContext, mContext.getResources().getString(R.string.undownload_tip_text));
                } else {
                    ToastUtil.showTextToast(mContext, mContext.getResources().getString(R.string.download_tip_text));
                    DownloadAudioManager.getInstance(mContext).addTask(audioInfo);
                }
            }
        });

    }

    /***
     * 刷新
     */
    public void reshViewHolder(String playHash) {
        int oldIndex = getAudioIndex(mOldPlayHash);
        if (oldIndex != -1) {
            mOldPlayHash = "";
            notifyItemChanged(oldIndex);
        }
        int newIndex = getAudioIndex(playHash);
        if (newIndex != -1) {
            notifyItemChanged(newIndex);
        }
    }

    /**
     * 获取歌曲索引
     *
     * @return
     */
    private int getAudioIndex(String playHash) {
        if (TextUtils.isEmpty(playHash)) return -1;
        if (mDatas != null && mDatas.size() > 0) {
            for (int i = 0; i < mDatas.size(); i++) {
                AudioInfo temp = mDatas.get(i);
                if (temp.getHash().equals(playHash)) {
                    return i;
                }
            }
        }
        return -1;
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

        public IconfontImageButtonTextView getDeleteImgBtn() {
            if (deleteImgBtn == null) {
                deleteImgBtn = view.findViewById(R.id.delete_menu);
            }
            deleteImgBtn.setConvert(true);
            return deleteImgBtn;
        }

    }
}
