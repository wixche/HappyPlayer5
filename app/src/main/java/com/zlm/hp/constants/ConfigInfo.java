package com.zlm.hp.constants;


import android.os.Parcel;
import android.os.Parcelable;

import com.zlm.hp.entity.AudioInfo;
import com.zlm.hp.entity.TimerInfo;
import com.zlm.hp.util.CodeLineUtil;
import com.zlm.hp.util.ColorUtil;
import com.zlm.hp.util.ContextUtil;
import com.zlm.hp.util.FileUtil;
import com.zlm.hp.util.RandomUtil;
import com.zlm.hp.util.ResourceUtil;
import com.zlm.hp.util.ZLog;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 配置数据
 * @author: zhangliangming
 * @date: 2018-08-04 21:38
 **/
public class ConfigInfo implements Parcelable {

    /**
     *
     */
    private static byte[] lock = new byte[0];

    //开关

    /**
     * 是否是在wifi环境下使用
     */
    private boolean isWifi;
    /**
     * 是否启动hello语音
     */
    private boolean isSayHello;
    /**
     * 是否启动线控
     */
    private boolean isWire;

    /**
     * 是否显示桌面歌词
     */
    private boolean isShowDesktopLrc;
    /**
     * 是否是显示锁屏歌词
     */
    private boolean isShowLockScreenLrc;

    /**
     * 最小字体大小
     */
    public static final int MIN_LRC_FONT_SIZE = 30;

    /**
     * 最大字体大小
     */
    public static final int MAX_LRC_FONT_SIZE = 100;

    //歌词相关
    /**
     * 歌词字体大小
     */
    private int lrcFontSize = MIN_LRC_FONT_SIZE;
    /**
     * 歌词颜色索引
     */
    private int lrcColorIndex;


    //桌面
    /**
     * 桌面歌词显示可移动
     */
    private boolean isDesktopLrcCanMove = true;
    /**
     * 桌面歌词颜色索引
     */
    private int desktopLrcColorIndex;
    /**
     * 桌面歌词字体大小
     */
    private int desktopLrcFontSize;
    /**
     * 桌面歌词y轴位置
     */
    private int desktopLrcY;

    //歌曲相关数据

    /**
     * 当前播放歌曲的hash
     */
    private String playHash = "";
    /**
     * 当前播放模式
     */
    private int playModel = 0; // 0是 顺序播放 1是随机播放 2是循环播放 3是单曲播放

    /**
     * 当前播放歌曲列表
     */
    private List<AudioInfo> audioInfos;

    /**
     * 显示翻译歌词
     */
    public static final int EXTRALRCSTATUS_SHOWTRANSLATELRC = 0;
    /**
     * 显示音译歌词
     */
    public static final int EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC = 1;
    /**
     * 只显示默认歌词
     */
    public static final int EXTRALRCSTATUS_NOSHOWEXTRALRC = 2;
    /**
     * 默认只显示默认歌词
     */
    private int extraLrcStatus = EXTRALRCSTATUS_NOSHOWEXTRALRC;

    /**
     * 桌面歌词未读颜色
     */
    public static int DESKTOP_LRC_NOREAD_COLORS[][] = {{
            ColorUtil.parserColor("#00348a"),
            ColorUtil.parserColor("#0080c0"),
            ColorUtil.parserColor("#03cafc")
    }, {
            ColorUtil.parserColor("#ffffff"),
            ColorUtil.parserColor("#ffffff"),
            ColorUtil.parserColor("#ffffff")
    }, {
            ColorUtil.parserColor("#ffffff"),
            ColorUtil.parserColor("#ffffff"),
            ColorUtil.parserColor("#ffffff")
    }, {
            ColorUtil.parserColor("#ffac00"),
            ColorUtil.parserColor("#ff0000"),
            ColorUtil.parserColor("#aa0000")
    }, {
            ColorUtil.parserColor("#93ff26"),
            ColorUtil.parserColor("#46b000"),
            ColorUtil.parserColor("#005500")
    }};

    /**
     * 桌面歌词已读颜色
     */
    public static int DESKTOP_LRC_READED_COLORS[][] = {{
            ColorUtil.parserColor("#82f7fd"),
            ColorUtil.parserColor("#ffffff"),
            ColorUtil.parserColor("#03e9fc")
    }, {
            ColorUtil.parserColor("#ffff00"),
            ColorUtil.parserColor("#ffff00"),
            ColorUtil.parserColor("#ffff00")
    }, {
            ColorUtil.parserColor("#e17db3"),
            ColorUtil.parserColor("#e17db3"),
            ColorUtil.parserColor("#e17db3")
    }, {
            ColorUtil.parserColor("#ffffa4"),
            ColorUtil.parserColor("#ffff00"),
            ColorUtil.parserColor("#ff641a")
    }, {
            ColorUtil.parserColor("#ffffff"),
            ColorUtil.parserColor("#9aff11"),
            ColorUtil.parserColor("#ffff00")
    }};

    /**
     * 歌词颜色集合
     */
    public static String[] LRC_COLORS_STRING = {"#fada83", "#fe8db6", "#feb88e",
            "#adfe8e", "#8dc7ff", "#e69bff"};

    /**
     * 定时器
     */
    private TimerInfo timerInfo;

    /**
     * 是否显示字幕
     */
    private boolean isShowSubtitle;

    /**
     * 字幕字体大小
     */
    private int subtitleFontSize = MIN_LRC_FONT_SIZE;

    protected ConfigInfo(Parcel in) {
        if (in != null) {
            isWifi = in.readByte() != 0;
            isSayHello = in.readByte() != 0;
            isWire = in.readByte() != 0;
            isShowDesktopLrc = in.readByte() != 0;
            isShowLockScreenLrc = in.readByte() != 0;
            lrcFontSize = in.readInt();
            lrcColorIndex = in.readInt();
            isDesktopLrcCanMove = in.readByte() != 0;
            desktopLrcColorIndex = in.readInt();
            desktopLrcFontSize = in.readInt();
            desktopLrcY = in.readInt();
            playHash = in.readString();
            playModel = in.readInt();

            isShowSubtitle = in.readByte() != 0;
            subtitleFontSize = in.readInt();/**/
//            audioInfos = in.createTypedArrayList(AudioInfo.CREATOR);
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isWifi ? 1 : 0));
        dest.writeByte((byte) (isSayHello ? 1 : 0));
        dest.writeByte((byte) (isWire ? 1 : 0));
        dest.writeByte((byte) (isShowDesktopLrc ? 1 : 0));
        dest.writeByte((byte) (isShowLockScreenLrc ? 1 : 0));
        dest.writeInt(lrcFontSize);
        dest.writeInt(lrcColorIndex);
        dest.writeByte((byte) (isDesktopLrcCanMove ? 1 : 0));
        dest.writeInt(desktopLrcColorIndex);
        dest.writeInt(desktopLrcFontSize);
        dest.writeInt(desktopLrcY);
        dest.writeString(playHash);
        dest.writeInt(playModel);

        dest.writeByte((byte) (isShowSubtitle ? 1 : 0));
        dest.writeInt(subtitleFontSize);
//        dest.writeTypedList(audioInfos);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ConfigInfo> CREATOR = new Creator<ConfigInfo>() {
        @Override
        public ConfigInfo createFromParcel(Parcel in) {
            return new ConfigInfo(in);
        }

        @Override
        public ConfigInfo[] newArray(int size) {
            return new ConfigInfo[size];
        }
    };

    /**
     * 加载
     *
     * @return
     */
    public static ConfigInfo load() {
        Parcel parcel = null;
        synchronized (lock) {
            String filePath = ResourceUtil.getContextFilePath(ContextUtil.getContext(), ResourceConstants.PATH_CONFIG, Constants.CONFIG);
            try {
                byte[] data = FileUtil.readFile(filePath);
                if (data == null) {
                    parcel = null;
                    ZLog.i(new CodeLineUtil().getCodeLineInfo(), "ConfigInfo.load readFile => null");
                } else {
                    parcel = Parcel.obtain();
                    parcel.unmarshall(data, 0, data.length);
                    parcel.setDataPosition(0);
                }
            } catch (Exception e) {
                ZLog.e(new CodeLineUtil().getCodeLineInfo(), "ConfigInfo.load Exception: ", e.getMessage());
            }
        }
        //
        _ConfigInfo = CREATOR.createFromParcel(parcel);
        return _ConfigInfo;
    }

    /**
     *
     */
    private static ConfigInfo _ConfigInfo;

    /**
     * @return
     */
    public static ConfigInfo obtain() {
        if (_ConfigInfo == null) {
            load();
        }
        return _ConfigInfo;
    }


    /**
     * 保存
     *
     * @return
     */
    public ConfigInfo save() {
        Parcel parcel = Parcel.obtain();
        writeToParcel(parcel, PARCELABLE_WRITE_RETURN_VALUE);
        synchronized (lock) {
            try {
                String filePath = ResourceUtil.getContextFilePath(ContextUtil.getContext(), ResourceConstants.PATH_CONFIG, Constants.CONFIG);
                FileUtil.writeFile(filePath, parcel.marshall());
            } catch (Exception e) {
                ZLog.e(new CodeLineUtil().getCodeLineInfo(), "ConfigInfo.save Exception: ", e.getMessage());
            }
        }
        //
        _ConfigInfo = this;
        return _ConfigInfo;
    }

    public boolean isWifi() {
        return isWifi;
    }

    public ConfigInfo setWifi(boolean wifi) {
        isWifi = wifi;
        return this;
    }

    public boolean isSayHello() {
        return isSayHello;
    }

    public ConfigInfo setSayHello(boolean sayHello) {
        isSayHello = sayHello;
        return this;
    }

    public boolean isWire() {
        return isWire;
    }

    public ConfigInfo setWire(boolean wire) {
        isWire = wire;
        return this;
    }

    public boolean isShowDesktopLrc() {
        return isShowDesktopLrc;
    }

    public ConfigInfo setShowDesktopLrc(boolean showDesktopLrc) {
        isShowDesktopLrc = showDesktopLrc;
        return this;
    }

    public boolean isShowLockScreenLrc() {
        return isShowLockScreenLrc;
    }

    public ConfigInfo setShowLockScreenLrc(boolean showLockScreenLrc) {
        isShowLockScreenLrc = showLockScreenLrc;
        return this;
    }

    public int getLrcFontSize() {
        return lrcFontSize;
    }

    public ConfigInfo setLrcFontSize(int lrcFontSize) {
        this.lrcFontSize = lrcFontSize;
        return this;
    }

    public int getLrcColorIndex() {
        return lrcColorIndex;
    }

    public ConfigInfo setLrcColorIndex(int lrcColorIndex) {
        this.lrcColorIndex = lrcColorIndex;
        return this;
    }

    public boolean isDesktopLrcCanMove() {
        return isDesktopLrcCanMove;
    }

    public ConfigInfo setDesktopLrcCanMove(boolean desktopLrcCanMove) {
        isDesktopLrcCanMove = desktopLrcCanMove;
        return this;
    }

    public int getDesktopLrcColorIndex() {
        return desktopLrcColorIndex;
    }

    public ConfigInfo setDesktopLrcColorIndex(int desktopLrcColorIndex) {
        this.desktopLrcColorIndex = desktopLrcColorIndex;
        return this;
    }

    public int getDesktopLrcFontSize() {
        return desktopLrcFontSize;
    }

    public ConfigInfo setDesktopLrcFontSize(int desktopLrcFontSize) {
        this.desktopLrcFontSize = desktopLrcFontSize;
        return this;
    }

    public int getDesktopLrcY() {
        return desktopLrcY;
    }

    public ConfigInfo setDesktopLrcY(int desktopLrcY) {
        this.desktopLrcY = desktopLrcY;
        return this;
    }

    public String getPlayHash() {
        return playHash;
    }

    public ConfigInfo setPlayHash(String playHash) {
        this.playHash = playHash;
        return this;
    }

    public boolean isShowSubtitle() {
        return isShowSubtitle;
    }

    public ConfigInfo setShowSubtitle(boolean showSubtitle) {
        isShowSubtitle = showSubtitle;
        return this;
    }

    public int getSubtitleFontSize() {
        return subtitleFontSize;
    }

    public ConfigInfo setSubtitleFontSize(int subtitleFontSize) {
        this.subtitleFontSize = subtitleFontSize;
        return this;
    }

    public TimerInfo getTimerInfo() {
        return timerInfo;
    }

    public void setTimerInfo(TimerInfo timerInfo) {
        this.timerInfo = timerInfo;
    }

    public int getPlayModel() {
        return playModel;
    }

    public ConfigInfo setPlayModel(int playModel) {
        this.playModel = playModel;
        return this;
    }

    public int getExtraLrcStatus() {
        return extraLrcStatus;
    }

    public ConfigInfo setExtraLrcStatus(int extraLrcStatus) {
        this.extraLrcStatus = extraLrcStatus;
        return this;
    }

    public List<AudioInfo> getAudioInfos() {
        if (audioInfos == null) {
            loadPlayListData();
        }
        return audioInfos;
    }

    public ConfigInfo setAudioInfos(List<AudioInfo> audioInfos) {
        //添加随机数
        RandomUtil.setNums(audioInfos.size());
        this.audioInfos = audioInfos;
        savePlayListData(audioInfos);
        return this;
    }

    /**
     * @throws
     * @Description: 加载播放列表
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-10-06 23:21
     */
    private void loadPlayListData() {
        Parcel parcel = null;
        synchronized (lock) {
            String filePath = ResourceUtil.getContextFilePath(ContextUtil.getContext(), ResourceConstants.PATH_CONFIG, Constants.PLAYLIST);
            try {
                byte[] data = FileUtil.readFile(filePath);
                if (data == null) {
                    parcel = null;
                    ZLog.i(new CodeLineUtil().getCodeLineInfo(), "ConfigInfo.loadPlayListData readFile => null");
                } else {
                    parcel = Parcel.obtain();
                    parcel.unmarshall(data, 0, data.length);
                    parcel.setDataPosition(0);
                }
            } catch (Exception e) {
                ZLog.e(new CodeLineUtil().getCodeLineInfo(), "ConfigInfo.loadPlayListData Exception: ", e.getMessage());
            }
        }

        if (parcel == null) {
            audioInfos = new ArrayList<AudioInfo>();
        } else {
            audioInfos = parcel.createTypedArrayList(AudioInfo.CREATOR);
        }
        //添加随机数
        RandomUtil.setNums(audioInfos.size());
    }

    /**
     * @throws
     * @Description: 保存播放列表数据
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-10-06 23:26
     */
    private void savePlayListData(List<AudioInfo> audioInfos) {
        Parcel parcel = Parcel.obtain();
        parcel.writeTypedList(audioInfos);
        synchronized (lock) {
            try {
                String filePath = ResourceUtil.getContextFilePath(ContextUtil.getContext(), ResourceConstants.PATH_CONFIG, Constants.PLAYLIST);
                FileUtil.writeFile(filePath, parcel.marshall());
            } catch (Exception e) {
                ZLog.e(new CodeLineUtil().getCodeLineInfo(), "ConfigInfo.savePlayListData Exception: ", e.getMessage());
            }
        }
    }
}
