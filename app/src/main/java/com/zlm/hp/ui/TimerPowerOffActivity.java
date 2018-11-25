package com.zlm.hp.ui;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.zlm.hp.constants.ConfigInfo;
import com.zlm.hp.entity.TimerInfo;
import com.zlm.hp.receiver.AppSystemReceiver;
import com.zlm.hp.util.TimeUtil;
import com.zlm.hp.util.ToastUtil;
import com.zlm.hp.widget.IconfontTextView;
import com.zlm.hp.widget.ListItemRelativeLayout;
import com.zlm.libs.widget.SwipeBackLayout;


/**
 * @Description: 定时关闭
 * @author: zhangliangming
 * @date: 2018-11-25 13:00
 **/
public class TimerPowerOffActivity extends BaseActivity {
    /**
     *
     */
    private SwipeBackLayout mSwipeBackLayout;

    /**
     * 提示语
     */
    private TextView mTimeTipTv;

    /**
     * 关闭
     */
    private ListItemRelativeLayout mTimerCloseRl;
    private TextView mTimeCloseTv;
    private IconfontTextView mTimeCloseImg;

    /**
     * 10
     */
    private ListItemRelativeLayout mTimer10Rl;
    private TextView mTime10Tv;
    private IconfontTextView mTime10Img;


    /**
     * 20
     */
    private ListItemRelativeLayout mTimer20Rl;
    private TextView mTime20Tv;
    private IconfontTextView mTime20Img;

    /**
     * 30
     */
    private ListItemRelativeLayout mTimer30Rl;
    private TextView mTime30Tv;
    private IconfontTextView mTime30Img;

    /**
     * 60
     */
    private ListItemRelativeLayout mTimer60Rl;
    private TextView mTime60Tv;
    private IconfontTextView mTime60Img;

    /**
     * def
     */
    private ListItemRelativeLayout mTimerDefRl;
    private TextView mTimeDefTv;
    private IconfontTextView mTimeDefImg;
    private TextView mTimeDefTimeTv;

    //
    private ConfigInfo mConfigInfo;
    private AppSystemReceiver mAppSystemReceiver;

    private final int MESSAGE_WHAT_LOADDATA = 0;
    private final int MESSAGE_WHAT_TIMERUPDATE = 1;

    @Override
    protected int setContentLayoutResID() {
        return R.layout.activity_timer_power_off;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        mSwipeBackLayout = findViewById(R.id.swipeback_layout);
        mSwipeBackLayout.setSwipeBackLayoutListener(new SwipeBackLayout.SwipeBackLayoutListener() {

            @Override
            public void finishActivity() {
                finish();
                overridePendingTransition(0, 0);
            }
        });

        TextView titleView = findViewById(R.id.title);
        titleView.setText(getString(R.string.timer_power_off));

        //返回
        ImageView backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwipeBackLayout.closeView();
            }
        });

        //提示语
        mTimeTipTv = findViewById(R.id.time_tip);

        //关闭
        mTimerCloseRl = findViewById(R.id.timer_close_rl);
        mTimerCloseRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppSystemReceiver.sendTimerSettingMsgReceiver(mContext, null);
            }
        });
        mTimeCloseTv = findViewById(R.id.timer_close_text);
        mTimeCloseImg = findViewById(R.id.timer_close_img);

        //10
        mTimer10Rl = findViewById(R.id.timer_10_rl);
        mTimer10Rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimerInfo timerInfo = new TimerInfo();
                timerInfo.setSumTime(10 * 60 * 1000);
                timerInfo.setCurTime(timerInfo.getSumTime());
                AppSystemReceiver.sendTimerSettingMsgReceiver(mContext, timerInfo);
            }
        });
        mTime10Tv = findViewById(R.id.timer_10_text);
        mTime10Img = findViewById(R.id.timer_10_img);

        //20
        mTimer20Rl = findViewById(R.id.timer_20_rl);
        mTimer20Rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimerInfo timerInfo = new TimerInfo();
                timerInfo.setSumTime(20 * 60 * 1000);
                timerInfo.setCurTime(timerInfo.getSumTime());
                AppSystemReceiver.sendTimerSettingMsgReceiver(mContext, timerInfo);
            }
        });
        mTime20Tv = findViewById(R.id.timer_20_text);
        mTime20Img = findViewById(R.id.timer_20_img);

        //30
        mTimer30Rl = findViewById(R.id.timer_30_rl);
        mTimer30Rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimerInfo timerInfo = new TimerInfo();
                timerInfo.setSumTime(30 * 60 * 1000);
                timerInfo.setCurTime(timerInfo.getSumTime());
                AppSystemReceiver.sendTimerSettingMsgReceiver(mContext, timerInfo);
            }
        });
        mTime30Tv = findViewById(R.id.timer_30_text);
        mTime30Img = findViewById(R.id.timer_30_img);

        //60
        mTimer60Rl = findViewById(R.id.timer_60_rl);
        mTimer60Rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimerInfo timerInfo = new TimerInfo();
                timerInfo.setSumTime(60 * 60 * 1000);
                timerInfo.setCurTime(timerInfo.getSumTime());
                AppSystemReceiver.sendTimerSettingMsgReceiver(mContext, timerInfo);
            }
        });
        mTime60Tv = findViewById(R.id.timer_60_text);
        mTime60Img = findViewById(R.id.timer_60_img);

        //def
        mTimeDefTimeTv = findViewById(R.id.timer_def_time_text);
        mTimerDefRl = findViewById(R.id.timer_def_rl);
        mTimerDefRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new TimePickerDialog(TimerPowerOffActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT, new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        int sumTime = hourOfDay * (60 * 60 * 1000) + minute * (60 * 1000);
                        if(sumTime == 0){

                            ToastUtil.showTextToast(mContext,getString(R.string.timer_setting_time));

                            return;
                        }

                        TimerInfo timerInfo = new TimerInfo();
                        timerInfo.setType(TimerInfo.TYPE_DEF);
                        timerInfo.setSumTime(sumTime);
                        timerInfo.setCurTime(timerInfo.getSumTime());
                        AppSystemReceiver.sendTimerSettingMsgReceiver(mContext, timerInfo);

                    }
                }, 0, 0, true).show();

            }
        });
        mTimeDefTv = findViewById(R.id.timer_def_text);
        mTimeDefImg = findViewById(R.id.timer_def_img);

        //
        //系统
        mAppSystemReceiver = new AppSystemReceiver();
        mAppSystemReceiver.setReceiverListener(new AppSystemReceiver.AppSystemReceiverListener() {
            @Override
            public void onReceive(Context context, final Intent intent, final int code) {
                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        handleAppSystemBroadcastReceiver(intent, code);
                    }
                });
            }

            private void handleAppSystemBroadcastReceiver(Intent intent, int code) {

                Bundle timerBundle = intent.getBundleExtra(AppSystemReceiver.ACTION_BUNDLEKEY);
                TimerInfo timerInfo = timerBundle.getParcelable(AppSystemReceiver.ACTION_DATA_KEY);

                Message msg = Message.obtain();
                msg.what = MESSAGE_WHAT_TIMERUPDATE;
                msg.obj = timerInfo;

                mUIHandler.sendMessage(msg);

            }
        });
        mAppSystemReceiver.registerReceiver(mContext);
        //
        mWorkerHandler.sendEmptyMessage(MESSAGE_WHAT_LOADDATA);
    }

    @Override
    protected void handleUIMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_WHAT_LOADDATA:

                TimerInfo timerInfo = mConfigInfo.getTimerInfo();
                refreshView(timerInfo);


                break;
            case MESSAGE_WHAT_TIMERUPDATE:
                TimerInfo curTimerInfo = (TimerInfo) msg.obj;
                refreshView(curTimerInfo);

                break;
        }
    }

    /**
     * 刷新view
     *
     * @param timerInfo
     */
    private void refreshView(TimerInfo timerInfo) {
        if (timerInfo == null) {
            mTimeTipTv.setText(getString(R.string.timer_close_text));

            //close
            mTimeCloseTv.setTextColor(getResources().getColor(R.color.defColor));
            mTimeCloseImg.setVisibility(View.VISIBLE);

            //10
            mTime10Tv.setTextColor(Color.BLACK);
            mTime10Img.setVisibility(View.INVISIBLE);

            //20
            mTime20Tv.setTextColor(Color.BLACK);
            mTime20Img.setVisibility(View.INVISIBLE);

            //30
            mTime30Tv.setTextColor(Color.BLACK);
            mTime30Img.setVisibility(View.INVISIBLE);

            //60
            mTime60Tv.setTextColor(Color.BLACK);
            mTime60Img.setVisibility(View.INVISIBLE);

            //def
            mTimeDefTv.setTextColor(Color.BLACK);
            mTimeDefTimeTv.setVisibility(View.INVISIBLE);
            mTimeDefImg.setVisibility(View.INVISIBLE);
        } else {
            mTimeTipTv.setText(String.format(getString(R.string.timer_open_text), TimeUtil.parseTimeToTimerString(timerInfo.getCurTime())));

            //close
            mTimeCloseTv.setTextColor(Color.BLACK);
            mTimeCloseImg.setVisibility(View.INVISIBLE);

            int type = timerInfo.getType();
            if (type == TimerInfo.TYPE_DEF) {
                //自定义
                //10
                mTime10Tv.setTextColor(Color.BLACK);
                mTime10Img.setVisibility(View.INVISIBLE);

                //20
                mTime20Tv.setTextColor(Color.BLACK);
                mTime20Img.setVisibility(View.INVISIBLE);

                //30
                mTime30Tv.setTextColor(Color.BLACK);
                mTime30Img.setVisibility(View.INVISIBLE);

                //60
                mTime60Tv.setTextColor(Color.BLACK);
                mTime60Img.setVisibility(View.INVISIBLE);

                //def
                mTimeDefTimeTv.setText(TimeUtil.parseTimeToTimerString(timerInfo.getCurTime()));
                mTimeDefTv.setTextColor(getResources().getColor(R.color.defColor));
                mTimeDefImg.setVisibility(View.VISIBLE);
                mTimeDefTimeTv.setVisibility(View.VISIBLE);

            } else {
                int sumTime = timerInfo.getSumTime();

                //10
                if (sumTime / (60 * 10) == 1000) {
                    mTime10Tv.setTextColor(getResources().getColor(R.color.defColor));
                    mTime10Img.setVisibility(View.VISIBLE);
                } else {
                    mTime10Tv.setTextColor(Color.BLACK);
                    mTime10Img.setVisibility(View.INVISIBLE);
                }

                //20
                if (sumTime / (60 * 20) == 1000) {
                    mTime20Tv.setTextColor(getResources().getColor(R.color.defColor));
                    mTime20Img.setVisibility(View.VISIBLE);
                } else {
                    mTime20Tv.setTextColor(Color.BLACK);
                    mTime20Img.setVisibility(View.INVISIBLE);
                }

                //30
                if (sumTime / (60 * 30) == 1000) {
                    mTime30Tv.setTextColor(getResources().getColor(R.color.defColor));
                    mTime30Img.setVisibility(View.VISIBLE);
                } else {
                    mTime30Tv.setTextColor(Color.BLACK);
                    mTime30Img.setVisibility(View.INVISIBLE);
                }

                //60
                if (sumTime / (60 * 60) == 1000) {
                    mTime60Tv.setTextColor(getResources().getColor(R.color.defColor));
                    mTime60Img.setVisibility(View.VISIBLE);
                } else {
                    mTime60Tv.setTextColor(Color.BLACK);
                    mTime60Img.setVisibility(View.INVISIBLE);
                }


                //def
                mTimeDefTv.setTextColor(Color.BLACK);
                mTimeDefImg.setVisibility(View.INVISIBLE);
                mTimeDefTimeTv.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        destroyReceiver();
        super.onDestroy();
    }

    /**
     * 销毁广播
     */
    private void destroyReceiver() {

        if (mAppSystemReceiver != null) {
            mAppSystemReceiver.unregisterReceiver(mContext);
        }

    }

    @Override
    protected void handleWorkerMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_WHAT_LOADDATA:

                mConfigInfo = ConfigInfo.obtain();

                break;
        }
    }


    @Override
    public void onBackPressed() {
        mSwipeBackLayout.closeView();
    }
}
