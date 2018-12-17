package com.zlm.hp.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlm.hp.async.AsyncHandlerTask;
import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.db.util.AudioInfoDB;
import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.handler.WeakRefHandler;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.ui.R;
import com.zlm.hp.util.ImageUtil;
import com.zlm.hp.util.ToastUtil;
import com.zlm.hp.widget.IconfontTextView;

import java.util.List;

/**
 * @Description: 歌曲列表弹出窗口
 * @author: zhangliangming
 * @date: 2018-11-28 21:08
 **/
public class PopPlayListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<AudioInfo> mDatas;
    private WeakRefHandler mUIHandler;
    private WeakRefHandler mWorkerHandler;
    private String mOldPlayHash = "";
    private ConfigInfo mConfigInfo;


    public PopPlayListAdapter(Context context, List<AudioInfo> datas, WeakRefHandler uiHandler, WeakRefHandler workerHandler) {
        this.mContext = context;
        this.mDatas = datas;
        this.mUIHandler = uiHandler;
        this.mWorkerHandler = workerHandler;
        mConfigInfo = ConfigInfo.obtain();
        mOldPlayHash = mConfigInfo.getPlayHash();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_popsong, null, false);
        PopListViewHolder holder = new PopListViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof PopListViewHolder && position < mDatas.size()) {
            AudioInfo audioInfo = mDatas.get(position);
            reshViewHolder(position, (PopListViewHolder) viewHolder, audioInfo);
        }
    }

    /**
     * @param position
     * @param viewHolder
     * @param audioInfo
     */
    private void reshViewHolder(final int position, final PopListViewHolder viewHolder, final AudioInfo audioInfo) {

        if (audioInfo.getHash().equals(mConfigInfo.getPlayHash())) {
            mOldPlayHash = audioInfo.getHash();

            viewHolder.getSingPicImg().setVisibility(View.VISIBLE);

            //加载歌手头像
            ImageUtil.loadSingerImage(mContext, viewHolder.getSingPicImg(), audioInfo.getSingerName(), mConfigInfo.isWifi(), 400, 400, new AsyncHandlerTask(mUIHandler, mWorkerHandler), new ImageUtil.ImageLoadCallBack() {
                @Override
                public void callback(Bitmap bitmap) {

                }
            });


            viewHolder.getSongIndexTv().setVisibility(View.INVISIBLE);
        } else {
            viewHolder.getSongIndexTv().setVisibility(View.VISIBLE);
            viewHolder.getSingPicImg().setVisibility(View.INVISIBLE);
        }

        //下载完成/下载
        if (audioInfo.getType() == AudioInfo.TYPE_LOCAL || AudioInfoDB.isDownloadedAudioExists(mContext, audioInfo.getHash())) {
            viewHolder.getDownloadImg().setVisibility(View.INVISIBLE);
            viewHolder.getDownloadedImg().setVisibility(View.VISIBLE);
        } else {
            viewHolder.getDownloadImg().setVisibility(View.VISIBLE);
            viewHolder.getDownloadedImg().setVisibility(View.INVISIBLE);
        }
        //未下载
        viewHolder.getDownloadImg().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //已下载
        viewHolder.getDownloadedImg().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        //喜欢/不喜欢
        if (AudioInfoDB.isLikeAudioExists(mContext, audioInfo.getHash())) {
            viewHolder.getUnLikeTv().setVisibility(View.INVISIBLE);
            viewHolder.getLikedImg().setVisibility(View.VISIBLE);
        } else {
            viewHolder.getUnLikeTv().setVisibility(View.VISIBLE);
            viewHolder.getLikedImg().setVisibility(View.INVISIBLE);
        }
        //不喜欢
        viewHolder.getUnLikeTv().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioInfo != null) {
                    if (!AudioInfoDB.isLikeAudioExists(mContext, audioInfo.getHash())) {
                        boolean result = AudioInfoDB.addLikeAudio(mContext, audioInfo);
                        if (result) {
                            viewHolder.getUnLikeTv().setVisibility(View.INVISIBLE);
                            viewHolder.getLikedImg().setVisibility(View.VISIBLE);
                            ToastUtil.showTextToast(mContext, mContext.getResources().getString(R.string.like_tip_text));

                            //更新喜欢歌曲广播
                            AudioBroadcastReceiver.sendReceiver(mContext, AudioBroadcastReceiver.ACTION_CODE_UPDATE_LIKE);
                        }
                    }
                }
            }
        });

        //喜欢
        viewHolder.getLikedImg().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioInfo != null) {
                    if (AudioInfoDB.isLikeAudioExists(mContext, audioInfo.getHash())) {
                        boolean result = AudioInfoDB.deleteLikeAudio(mContext, audioInfo.getHash());
                        if (result) {
                            viewHolder.getUnLikeTv().setVisibility(View.VISIBLE);
                            viewHolder.getLikedImg().setVisibility(View.INVISIBLE);

                            ToastUtil.showTextToast(mContext, mContext.getResources().getString(R.string.unlike_tip_text));

                            //更新喜欢歌曲广播
                            AudioBroadcastReceiver.sendReceiver(mContext, AudioBroadcastReceiver.ACTION_CODE_UPDATE_LIKE);
                        }
                    }
                }
            }
        });


        //显示歌曲索引
        viewHolder.getSongIndexTv().setText(((position + 1) < 10 ? "0" + (position + 1) : (position + 1) + ""));
        String singerName = audioInfo.getSingerName();
        String songName = audioInfo.getSongName();
        viewHolder.getSongNameTv().setText(songName);
        viewHolder.getSingerNameTv().setText(singerName);
        //item点击事件
        viewHolder.getListItemRelativeLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int oldIndex = getAudioIndex(mConfigInfo.getPlayHash());
                if (oldIndex == position) {
                    AudioPlayerManager.newInstance(mContext).playOrPause();
                    return;
                }

                AudioPlayerManager.newInstance(mContext).playSong(audioInfo);

                if (oldIndex != -1) {
                    notifyItemChanged(oldIndex);
                }
                int newIndex = getAudioIndex(audioInfo.getHash());
                if (newIndex != -1) {
                    notifyItemChanged(newIndex);
                }
            }
        });
    }

    /***
     * 刷新
     * @param audioInfo
     */
    public void reshViewHolder(AudioInfo audioInfo) {
        int oldIndex = getAudioIndex(mOldPlayHash);
        if (oldIndex != -1) {
            notifyItemChanged(oldIndex);
        }
        int newIndex = getAudioIndex(audioInfo);
        if (newIndex != -1) {
            notifyItemChanged(newIndex);
        }
    }

    /**
     * 获取歌曲索引
     *
     * @param audioInfo
     * @return
     */
    private int getAudioIndex(AudioInfo audioInfo) {
        if (audioInfo == null) {
            return -1;
        }
        return getAudioIndex(audioInfo.getHash());
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

    /**
     *
     */
    class PopListViewHolder extends RecyclerView.ViewHolder {
        private View view;
        /**
         * item底部布局
         */
        private RelativeLayout listItemRelativeLayout;

        /**
         * 歌手头像按钮
         */
        private ImageView singPicImg;
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
        /**
         * 下载未完成按钮
         */
        private ImageView downloadImg;
        /**
         * 添加喜欢按钮
         */
        private IconfontTextView unlikeTv;

        /**
         * 下载完成按钮
         */
        private ImageView downloadedImg;

        /**
         * 喜欢按钮
         */
        private ImageView likeImg;


        public PopListViewHolder(View view) {
            super(view);
            this.view = view;
        }

        public RelativeLayout getListItemRelativeLayout() {
            if (listItemRelativeLayout == null) {
                listItemRelativeLayout = view.findViewById(R.id.itemBG);
            }
            return listItemRelativeLayout;
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

        public ImageView getSingPicImg() {
            if (singPicImg == null) {
                singPicImg = view.findViewById(R.id.singPic);
            }
            return singPicImg;
        }

        public TextView getSongIndexTv() {
            if (songIndexTv == null) {
                songIndexTv = view.findViewById(R.id.songIndex);
            }
            return songIndexTv;
        }

        public ImageView getDownloadImg() {
            if (downloadImg == null) {
                downloadImg = view.findViewById(R.id.download);
            }
            return downloadImg;
        }

        public IconfontTextView getUnLikeTv() {
            if (unlikeTv == null) {
                unlikeTv = view.findViewById(R.id.unlike);
            }
            return unlikeTv;
        }

        public ImageView getDownloadedImg() {
            if (downloadedImg == null) {
                downloadedImg = view.findViewById(R.id.downloaded);
            }
            return downloadedImg;
        }

        public ImageView getLikedImg() {
            if (likeImg == null) {
                likeImg = view.findViewById(R.id.liked);
            }
            return likeImg;
        }

    }
}
