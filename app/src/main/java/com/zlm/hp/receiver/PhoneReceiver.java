package com.zlm.hp.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;

import com.zlm.hp.application.HPApplication;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.model.AudioMessage;

import java.util.Timer;
import java.util.TimerTask;

import base.utils.LoggerUtil;

/**
 * 耳机线控
 * Created by zhangliangming on 2017/9/1.
 */

public class PhoneReceiver extends BroadcastReceiver {

    private LoggerUtil logger;
    private Context mContext;
    private Timer timer;
    private static MTask myTimer;
    /**单击次数**/
    private static int clickCount;
    private AudioManager mAudioManager;
    private ComponentName mRemoteControlResponder;

    public PhoneReceiver() {
        this.mContext = HPApplication.getInstance().getApplicationContext();
        logger = LoggerUtil.getZhangLogger(mContext);
        timer = new Timer(true);
        //
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mRemoteControlResponder = new ComponentName(mContext.getPackageName(), this.getClass().getName());
    }

    /**
     * 注册广播
     *
     * @param context
     */
    public void registerReceiver(Context context) {
        mAudioManager
                .registerMediaButtonEventReceiver(mRemoteControlResponder);
    }

    /**
     * 取消注册广播
     */
    public void unregisterReceiver(Context context) {
        mAudioManager
                .unregisterMediaButtonEventReceiver(mRemoteControlResponder);
    }

    @SuppressLint("HandlerLeak")
    private Handler mPhoneHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case 100:// 单击按键广播
                    Bundle data = msg.getData();
                    // 按键值
                    int keyCode = data.getInt("key_code");
                    // 按键时长
                    long eventTime = data.getLong("event_time");
                    // 设置超过10毫秒，就触发长按事件
                    boolean isLongPress = (eventTime > 10);

                    switch (keyCode) {
                        case KeyEvent.KEYCODE_HEADSETHOOK:// 播放或暂停
                        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:// 播放或暂停
                            playOrPause();
                            break;

                        // 短按=播放下一首音乐，长按=当前音乐快进
                        case KeyEvent.KEYCODE_MEDIA_NEXT:
                            if (isLongPress) {
                                fastNext(50 * 1000);// 自定义
                            } else {
                                playNext();// 自定义
                            }
                            break;

                        // 短按=播放上一首音乐，长按=当前音乐快退
                        case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                            if (isLongPress) {
                                fastPrevious(50 * 1000);// 自定义
                            } else {
                                playPrevious();// 自定义
                            }
                            break;
                    }

                    break;
                case 1://单击，暂停或者播放
                    playOrPause();
                    break;
                case 2://双击，下一首音乐
                    playNext();
                    break;
                case 3://三连击，上一首音乐
                    playPrevious();
                    break;
                // 快进
                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                    fastNext(10 * 1000);// 自定义
                    break;
                // 快退
                case KeyEvent.KEYCODE_MEDIA_REWIND:
                    fastPrevious(10 * 1000);// 自定义
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:

                    break;
                default:// 其他消息-则扔回上层处理
                    super.handleMessage(msg);
            }
        }
    };


    /**
     * 快进
     *
     * @param dProgress
     */
    private void fastPrevious(int dProgress) {
        logger.e("快进");
        seekToMusic(dProgress);
    }

    /**
     * 快退
     *
     * @param dProgress
     */
    private void fastNext(int dProgress) {
        logger.e("快退");
        seekToMusic(-dProgress);
    }

    /**
     * 快进播放
     *
     * @param seekProgress
     */
    private void seekToMusic(int seekProgress) {
        if (HPApplication.getInstance().getCurAudioMessage() != null &&
                HPApplication.getInstance().getCurAudioInfo() != null) {
            int progress = (int) (HPApplication.getInstance().getCurAudioMessage().getPlayProgress() + seekProgress);
            //判断歌词快进时，是否超过歌曲的总时间
            if (HPApplication.getInstance().getCurAudioInfo().getDuration() < progress) {
                progress = (int) HPApplication.getInstance().getCurAudioInfo().getDuration();
            } else if (HPApplication.getInstance().getCurAudioInfo().getDuration() < 0) {
                progress = 0;
            }
            //
            int playStatus = HPApplication.getInstance().getPlayStatus();
            if (playStatus == AudioPlayerManager.PLAYING) {
                //正在播放
                if (HPApplication.getInstance().getCurAudioMessage() != null) {
                    AudioMessage audioMessage = HPApplication.getInstance().getCurAudioMessage();
                    // AudioInfo audioInfo = mHPApplication.getCurAudioInfo();
                    //if (audioInfo != null) {
                    //  audioMessage.setAudioInfo(audioInfo);
                    if (audioMessage != null) {
                        audioMessage.setPlayProgress(progress);
                        Intent resumeIntent = new Intent(AudioBroadcastReceiver.ACTION_SEEKTOMUSIC);
                        resumeIntent.putExtra(AudioMessage.KEY, audioMessage);
                        resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        mContext.sendBroadcast(resumeIntent);
                    }
                }
            } else {

                if (HPApplication.getInstance().getCurAudioMessage() != null)
                    HPApplication.getInstance().getCurAudioMessage().setPlayProgress(progress);

                //歌词快进
                Intent lrcSeektoIntent = new Intent(AudioBroadcastReceiver.ACTION_LRCSEEKTO);
                lrcSeektoIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(lrcSeektoIntent);


            }
        }
    }


    /**
     * 播放上一首
     */
    private void playPrevious() {
        logger.e("播放上一首");
        //
        Intent nextIntent = new Intent(AudioBroadcastReceiver.ACTION_PREMUSIC);
        nextIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        mContext.sendBroadcast(nextIntent);
    }

    /**
     * 播放下一首
     */
    private void playNext() {
        logger.e("播放下一首");
        //
        Intent nextIntent = new Intent(AudioBroadcastReceiver.ACTION_NEXTMUSIC);
        nextIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        mContext.sendBroadcast(nextIntent);
    }

    /**
     * 播放或者暂存
     */
    private void playOrPause() {
        logger.e("播放或者暂存");

        int playStatus = HPApplication.getInstance().getPlayStatus();
        if (playStatus == AudioPlayerManager.PAUSE) {

            AudioInfo audioInfo = HPApplication.getInstance().getCurAudioInfo();
            if (audioInfo != null) {

                AudioMessage audioMessage = HPApplication.getInstance().getCurAudioMessage();
                Intent resumeIntent = new Intent(AudioBroadcastReceiver.ACTION_RESUMEMUSIC);
                resumeIntent.putExtra(AudioMessage.KEY, audioMessage);
                resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(resumeIntent);

            }

        } else if (playStatus == AudioPlayerManager.PLAYING) {

            Intent resumeIntent = new Intent(AudioBroadcastReceiver.ACTION_PAUSEMUSIC);
            resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            mContext.sendBroadcast(resumeIntent);

        } else {
            if (HPApplication.getInstance().getCurAudioMessage() != null) {
                AudioMessage audioMessage = HPApplication.getInstance().getCurAudioMessage();
                AudioInfo audioInfo = HPApplication.getInstance().getCurAudioInfo();
                if (audioInfo != null) {
                    audioMessage.setAudioInfo(audioInfo);
                    Intent resumeIntent = new Intent(AudioBroadcastReceiver.ACTION_PLAYMUSIC);
                    resumeIntent.putExtra(AudioMessage.KEY, audioMessage);
                    resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    mContext.sendBroadcast(resumeIntent);
                }
            }
        }

    }

    /**
     * 定时器，用于延迟1秒，判断是否会发生双击和三连击
     */
    class MTask extends TimerTask {
        @Override
        public void run() {
            if (clickCount==1) {
                mPhoneHandler.sendEmptyMessage(1);
            }else if (clickCount==2) {
                mPhoneHandler.sendEmptyMessage(2);
            }
            clickCount=0;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (action.equals("android.intent.action.MEDIA_BUTTON")) {

            KeyEvent keyEvent = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT); //获得KeyEvent对象
            try {
                if(keyEvent.getAction() == KeyEvent.ACTION_UP){
                    if (clickCount==0) {//单击
                        clickCount++;
                        myTimer = new MTask();
                        timer.schedule(myTimer,1000);
                    }else if (clickCount==1) {//双击
                        clickCount++;
                    }else if (clickCount==2) {//三连击
                        clickCount=0;
                        myTimer.cancel();
                        mPhoneHandler.sendEmptyMessage(3);
                    }
                }
            } catch (Exception e) {
            }
            abortBroadcast();//终止广播(不让别的程序收到此广播，免受干扰)
        }
    }
}
