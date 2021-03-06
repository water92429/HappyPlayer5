package com.zlm.hp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;

import java.util.Date;

import base.utils.LoggerUtil;

/**
 * 系统广播
 * Created by zhangliangming on 2017/8/15.
 */

public class SystemReceiver {

    private static final String base_action = "bear.love.peach";
    private LoggerUtil logger;
    /**
     * 是否注册成功
     */
    private boolean isRegisterSuccess = false;
    private Context mContext;
    /**
     * 注册成功广播
     */
    private String ACTION_SUCCESS = base_action + ".system.success_" + new Date().getTime();
    /**
     * 弹出窗口提示
     */
    public static String ACTION_TOASTMESSAGE = base_action + ".system.toast";
    /**
     * 打开线控
     */
    public static String ACTION_OPENWIREMESSAGE = base_action + ".phone.br.openwire";
    /**
     * 关闭线控
     */
    public static String ACTION_CLOSEWIREMESSAGE = base_action + ".phone.br.closewire";
    /**
     * 打开锁屏歌词
     */
    public static String ACTION_OPENLRCMESSAGE = base_action + ".lock.lrc.open";
    /**
     * 关闭锁屏歌词
     */
    public static String ACTION_CLOSELRCMESSAGE = base_action + ".lock.lrc.close";

    private BroadcastReceiver mSystemBroadcastReceiver;
    private IntentFilter mSystemIntentFilter;
    private SystemReceiverListener mSystemReceiverListener;

    public SystemReceiver(Context context) {
        this.mContext = context;
        logger = LoggerUtil.getZhangLogger(context);
        mSystemIntentFilter = new IntentFilter();

        //
        mSystemIntentFilter.addAction(ACTION_SUCCESS);
        mSystemIntentFilter.addAction(ACTION_TOASTMESSAGE);
        //WIFI
        mSystemIntentFilter.addAction(ACTION_OPENWIREMESSAGE);
        mSystemIntentFilter.addAction(ACTION_CLOSEWIREMESSAGE);
        //歌词
        mSystemIntentFilter.addAction(ACTION_OPENLRCMESSAGE);
        mSystemIntentFilter.addAction(ACTION_CLOSELRCMESSAGE);
        //耳机
        mSystemIntentFilter.addAction("android.media.AUDIO_BECOMING_NOISY");
        //短信
        mSystemIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
    }

    /**
     *
     */
    private Handler mSystemHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mSystemReceiverListener != null) {
                Intent intent = (Intent) msg.obj;
                if (intent.getAction().equals(ACTION_SUCCESS)) {
                    isRegisterSuccess = true;

                } else {
                    mSystemReceiverListener.onReceive(mContext, intent);
                }

            }
        }
    };

    /**
     * 注册广播
     *
     * @param context
     */
    public void registerReceiver(Context context) {
        if (mSystemBroadcastReceiver == null) {
            //
            mSystemBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    Message msg = new Message();
                    msg.obj = intent;
                    mSystemHandler.sendMessage(msg);


                }
            };

            mContext.registerReceiver(mSystemBroadcastReceiver, mSystemIntentFilter);
            //发送注册成功广播
            Intent successIntent = new Intent(ACTION_SUCCESS);
            successIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            mContext.sendBroadcast(successIntent);

        }
    }

    /**
     * 取消注册广播
     */
    public void unregisterReceiver(Context context) {
        if (mSystemReceiverListener != null && isRegisterSuccess) {

            mContext.unregisterReceiver(mSystemBroadcastReceiver);

        }

    }

    ///////////////////////////////////
    public interface SystemReceiverListener {
        void onReceive(Context context, Intent intent);
    }

    public void setSystemReceiverListener(SystemReceiverListener mSystemReceiverListener) {
        this.mSystemReceiverListener = mSystemReceiverListener;
    }
}
