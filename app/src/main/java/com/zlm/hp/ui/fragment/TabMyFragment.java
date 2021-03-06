package com.zlm.hp.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zlm.hp.R;
import com.zlm.hp.application.HPApplication;
import com.zlm.hp.db.AudioInfoDB;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.receiver.FragmentReceiver;
import com.zlm.hp.receiver.LockLrcReceiver;
import com.zlm.hp.receiver.SystemReceiver;
import com.zlm.hp.ui.widget.dialog.AlartOneButtonDialog;
import com.zlm.hp.ui.widget.dialog.AlartTwoButtonDialog;
import com.zlm.hp.utils.AppOpsUtils;
import com.zlm.hp.utils.IntentUtils;

import base.utils.ThreadUtil;
import base.widget.SetupBGButton;

/**
 * @Description: tab我的界面
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/7/16 20:42
 * @Throws:
 */
public class TabMyFragment extends BaseFragment {

    /**
     * 本地音乐
     */
    private LinearLayout mLocalMusic;

    /**
     * 本地音乐个数
     */
    private TextView mLocalCountTv;
    /**
     * 本地音乐个数
     */
    private int mLocalCount = 0;

    //////////////////////////////////////////////

    /**
     * 喜欢音乐
     */
    private LinearLayout mLikeMusic;
    /**
     * 喜欢音乐个数
     */
    private TextView mLikeCountTv;
    /**
     * 喜欢音乐个数
     */
    private int mLikeCount = 0;

    /**
     * wifi设置按钮
     */
    private SetupBGButton mWifiSetupBGButton;
    /**
     * 桌面歌词设置按钮
     */
    private SetupBGButton mDesktopBGButton;

    /***
     * 桌面歌词弹出窗口
     */
    private AlartOneButtonDialog mFloatWPAlartDialog;

    /**
     * 锁屏歌词设置按钮
     */
    private SetupBGButton mLockScreenBGButton;
    /**
     * 问候语按钮
     */
    private SetupBGButton mSayHelloSetupBGButton;

    /**
     * 线控
     */
    private SetupBGButton mWireSetupBGButton;

    /**
     * 退出设置按钮
     */
    private SetupBGButton mExitSetupBGButton;

    /**
     * 退出提示窗口
     */
    private AlartTwoButtonDialog mExitAlartDialog;

    /**
     * 更新本地音乐
     */
    private final int UPDATELOCALCOUNT = 0;
    /**
     * 更新最近音乐
     */
    private final int UPDATERECENTCOUNT = 1;

    /**
     * 更新喜欢音乐
     */
    private final int UPDATELIKECOUNT = 2;
    /**
     * 更新下载音乐
     */
    private final int UPDATEDOWNLOADCOUNT = 3;
    /////////////////////////////////////////////////////////

    /**
     * 最近音乐
     */
    private LinearLayout mRecentMusic;

    /**
     * 最近音乐个数
     */
    private TextView mRecentCountTv;
    /**
     * 最近音乐个数
     */
    private int mRecentCount = 0;

    ////////////////////////////////////////////////////////

    /**
     * 下载音乐
     */
    private LinearLayout mDownloadMusic;
    /**
     * 下载音乐个数
     */
    private TextView mDownloadCountTv;
    /**
     * 下载音乐个数
     */
    private int mDownloadCount = 0;

    ////////////////////////////////
    /**
     *
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATELOCALCOUNT:
                    mLocalCountTv.setText(mLocalCount + "");
                    break;
                case UPDATERECENTCOUNT:
                    mRecentCountTv.setText(mRecentCount + "");
                    break;
                case UPDATELIKECOUNT:
                    mLikeCountTv.setText(mLikeCount + "");
                    break;
                case UPDATEDOWNLOADCOUNT:
                    mDownloadCountTv.setText(mDownloadCount + "");
                    break;
            }
        }
    };

    private AudioBroadcastReceiver mAudioBroadcastReceiver;
    /**
     * 广播监听
     */
    private AudioBroadcastReceiver.AudioReceiverListener mAudioReceiverListener = new AudioBroadcastReceiver.AudioReceiverListener() {
        @Override
        public void onReceive(Context context, Intent intent) {
            doAudioReceive(context, intent);
        }
    };

    private LockLrcReceiver mLockLrcReceiver;
    private LockLrcReceiver.LockLrcReceiverListener mLockLrcReceiverListener = new LockLrcReceiver.LockLrcReceiverListener() {
        @Override
        public void onReceive(Context context, Intent intent) {
            doLockLrcReceive(context, intent);
        }
    };

    public TabMyFragment() {

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected int setContentViewId() {
        return R.layout.layout_fragment_my;
    }

    @Override
    protected int setTitleViewId() {
        return 0;
    }

    @Override
    protected void initViews(Bundle savedInstanceState, View mainView) {

        //本地音乐
        mLocalMusic = mainView.findViewById(R.id.tab_local_music);
        mLocalMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //打开
                Intent openIntent = new Intent(FragmentReceiver.ACTION_OPENLOCALMUSICFRAGMENT);
                openIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mActivity.sendBroadcast(openIntent);
            }
        });
        mLocalCountTv = mainView.findViewById(R.id.local_music_count);


        //喜欢的音乐
        mLikeMusic = mainView.findViewById(R.id.tab_like_music);
        mLikeMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //打开
                Intent openIntent = new Intent(FragmentReceiver.ACTION_OPENLIKEMUSICFRAGMENT);
                openIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mActivity.sendBroadcast(openIntent);

            }
        });
        mLikeCountTv = mainView.findViewById(R.id.like_music_count);

        //下载音乐
        mDownloadMusic = mainView.findViewById(R.id.tab_download_music);
        mDownloadMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //打开
                Intent openIntent = new Intent(FragmentReceiver.ACTION_OPENDOWNLOADMUSICFRAGMENT);
                openIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mActivity.sendBroadcast(openIntent);
            }
        });
        mDownloadCountTv = mainView.findViewById(R.id.download_music_count);

        //最近音乐
        mRecentMusic = mainView.findViewById(R.id.tab_centent_music);
        mRecentMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //打开
                Intent openIntent = new Intent(FragmentReceiver.ACTION_OPENRECENTMUSICFRAGMENT);
                openIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mActivity.sendBroadcast(openIntent);

            }
        });
        mRecentCountTv = mainView.findViewById(R.id.recent_music_count);


        //wifi设置按钮
        mWifiSetupBGButton = mainView.findViewById(R.id.wifibg);
        if (HPApplication.getInstance().isWifi()) {
            mWifiSetupBGButton.setSelect(true);
        }
        mWifiSetupBGButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
                boolean selected = mWifiSetupBGButton.isSelect();
                HPApplication.getInstance().setWifi(!selected);
                mWifiSetupBGButton.setSelect(HPApplication.getInstance().isWifi());
            }
        });

        mFloatWPAlartDialog = new AlartOneButtonDialog(getActivity(), new AlartOneButtonDialog.ButtonDialogListener() {
            @Override
            public void ButtonClick() {
                IntentUtils.gotoPermissionSetting(getActivity());
            }
        });

        //桌面歌词设置按钮
        mDesktopBGButton = mainView.findViewById(R.id.desktopbg);
        if (HPApplication.getInstance().isDesktop()) {
            mDesktopBGButton.setSelect(true);
        }
        mDesktopBGButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //

                boolean selected = mDesktopBGButton.isSelect();
                if (!selected) {
                    if (!AppOpsUtils.allowFloatWindow(getActivity())) {
                        mFloatWPAlartDialog.showDialog(getString(R.string.desktop_dialog_tip), getString(R.string.desktop_dialog_title));
                        return;
                    }
                }

                HPApplication.getInstance().setDesktop(!selected);
                mDesktopBGButton.setSelect(HPApplication.getInstance().isDesktop());
            }
        });

        //锁屏歌词设置按钮
        mLockScreenBGButton = mainView.findViewById(R.id.lockScreenbg);
        if (HPApplication.getInstance().isLockScreen()) {
            mLockScreenBGButton.setSelect(true);
        }
        mLockScreenBGButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
                boolean selected = mLockScreenBGButton.isSelect();
                HPApplication.getInstance().setLockScreen(!selected);
                mLockScreenBGButton.setSelect(HPApplication.getInstance().isLockScreen());
                if (HPApplication.getInstance().isLockScreen()) {
                    Intent openIntent = new Intent(SystemReceiver.ACTION_OPENLRCMESSAGE);
                    openIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    mActivity.sendBroadcast(openIntent);
                } else {
                    Intent closeIntent = new Intent(SystemReceiver.ACTION_CLOSELRCMESSAGE);
                    closeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    mActivity.sendBroadcast(closeIntent);
                }
            }
        });

        //问候语按钮
        mSayHelloSetupBGButton = mainView.findViewById(R.id.sayhello);
        if (HPApplication.getInstance().isSayHello()) {
            mSayHelloSetupBGButton.setSelect(true);
        }
        mSayHelloSetupBGButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean selected = mSayHelloSetupBGButton.isSelect();

                HPApplication.getInstance().setSayHello(!selected);
                mSayHelloSetupBGButton.setSelect(HPApplication.getInstance().isSayHello());
            }
        });

        //线控
        mWireSetupBGButton = mainView.findViewById(R.id.wire);
        if (HPApplication.getInstance().isWire()) {
            mWireSetupBGButton.setSelect(true);
        }
        mWireSetupBGButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean selected = mWireSetupBGButton.isSelect();
                HPApplication.getInstance().setWire(!selected);
                mWireSetupBGButton.setSelect(HPApplication.getInstance().isWire());
                if (HPApplication.getInstance().isWire()) {
                    Intent openIntent = new Intent(SystemReceiver.ACTION_OPENWIREMESSAGE);
                    openIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    mActivity.sendBroadcast(openIntent);
                } else {
                    Intent closeIntent = new Intent(SystemReceiver.ACTION_CLOSEWIREMESSAGE);
                    closeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    mActivity.sendBroadcast(closeIntent);
                }
            }
        });

        //关闭设置按钮
        mExitSetupBGButton = mainView.findViewById(R.id.exitbg);
        mExitSetupBGButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mExitAlartDialog.showDialog(mContext.getString(R.string.do_you_quit_the_application),
                        mContext.getString(R.string.cancel),
                        mContext.getString(R.string.sure));
            }
        });
        //
        mExitAlartDialog = new AlartTwoButtonDialog(mActivity, new AlartTwoButtonDialog.TwoButtonDialogListener() {
            @Override
            public void onLeftButtonClick() {

            }

            @Override
            public void onRightButtonClick() {
                    HPApplication.getInstance().exit();
            }
        });

        showContentView();

        //注册监听
        mAudioBroadcastReceiver = new AudioBroadcastReceiver(mActivity.getApplicationContext());
        mAudioBroadcastReceiver.setAudioReceiverListener(mAudioReceiverListener);
        mAudioBroadcastReceiver.registerReceiver(mActivity.getApplicationContext());

        //注册锁屏歌词广播
        mLockLrcReceiver = new LockLrcReceiver(mActivity.getApplicationContext());
        mLockLrcReceiver.setLockLrcReceiverListener(mLockLrcReceiverListener);
        if (HPApplication.getInstance().isLockScreen()) {
            mLockLrcReceiver.registerReceiver(mActivity.getApplicationContext());
        }
    }

    @Override
    protected void loadData(boolean isRestoreInstance) {
        loadLocalCount();
        loadRecentCount();
        loadLikeCount();
        loadDownloadCount();
    }

    /**
     * 加载喜欢歌曲列表
     */
    private void loadDownloadCount() {
        ThreadUtil.runInThread(new Runnable() {
            @Override
            public void run() {
                mDownloadCount = AudioInfoDB.getAudioInfoDB(mActivity.getApplicationContext()).getDonwloadAudioCount();
                mHandler.sendEmptyMessage(UPDATEDOWNLOADCOUNT);
            }
        });
    }

    /**
     * 加载喜欢歌曲列表
     */
    private void loadLikeCount() {
        ThreadUtil.runInThread(new Runnable() {
            @Override
            public void run() {
                mLikeCount = AudioInfoDB.getAudioInfoDB(mActivity.getApplicationContext()).getLikeAudioCount();
                mHandler.sendEmptyMessage(UPDATELIKECOUNT);
            }
        });
    }

    /**
     * 加载本地音乐个数
     */
    private void loadLocalCount() {
        ThreadUtil.runInThread(new Runnable() {
            @Override
            public void run() {
                mLocalCount = AudioInfoDB.getAudioInfoDB(mActivity.getApplicationContext()).getLocalAudioCount();
                mHandler.sendEmptyMessage(UPDATELOCALCOUNT);
            }
        });
    }

    /**
     * 获取最近音乐个数
     */
    private void loadRecentCount() {
        ThreadUtil.runInThread(new Runnable() {
            @Override
            public void run() {
                mRecentCount = AudioInfoDB.getAudioInfoDB(mActivity.getApplicationContext()).getRecentAudioCount();
                mHandler.sendEmptyMessage(UPDATERECENTCOUNT);
            }
        });
    }

    /**
     * 处理音频监听事件
     *
     * @param context
     * @param intent
     */
    private void doAudioReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(AudioBroadcastReceiver.ACTION_LOCALUPDATE)) {
            loadLocalCount();
        } else if (action.equals(AudioBroadcastReceiver.ACTION_RECENTUPDATE)) {
            loadRecentCount();
        } else if (action.equals(AudioBroadcastReceiver.ACTION_LIKEUPDATE)) {
            loadLikeCount();
        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_PLAYMUSIC)) {
            //将正在播放的歌曲加入最近播放列表中
            AudioInfo audioInfo = HPApplication.getInstance().getCurAudioInfo();
            if (audioInfo != null) {
                if (AudioInfoDB.getAudioInfoDB(mActivity.getApplication()).isRecentOrLikeExists(audioInfo.getHash(), audioInfo.getType(), true)) {
                    AudioInfoDB.getAudioInfoDB(mActivity.getApplication()).updateRecentAudio(audioInfo.getHash(), audioInfo.getType(), true);
                } else {
                    AudioInfoDB.getAudioInfoDB(mActivity.getApplication()).addRecentOrLikeAudio(audioInfo, true);
                }
                loadRecentCount();
            }
        } else if (action.equals(AudioBroadcastReceiver.ACTION_DOWNLOADUPDATE)) {
            loadDownloadCount();
        } else if (action.equals(AudioBroadcastReceiver.ACTION_LIKEADD)) {
            //添加喜欢
            AudioInfo audioInfo = (AudioInfo) intent.getSerializableExtra(AudioInfo.KEY);
            AudioInfoDB.getAudioInfoDB(mActivity.getApplication()).addRecentOrLikeAudio(audioInfo, false);
            loadLikeCount();
        } else if (action.equals(AudioBroadcastReceiver.ACTION_LIKEDELETE)) {
            //删除喜欢
            AudioInfo audioInfo = (AudioInfo) intent.getSerializableExtra(AudioInfo.KEY);
            AudioInfoDB.getAudioInfoDB(mActivity.getApplication()).deleteRecentOrLikeAudio(audioInfo.getHash(), audioInfo.getType(), false);
            loadLikeCount();
        }
    }

    /**
     * 处理锁屏歌词广播事件
     *
     * @param context
     * @param intent
     */
    private void doLockLrcReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(LockLrcReceiver.ACTION_SHOWLRCMESSAGE)) {
            //显示锁屏歌词
            HPApplication.getInstance().setShowLockScreen(true);
        } else if (action.equals(LockLrcReceiver.ACTION_HIDELRCMESSAGE)) {
            //隐藏锁屏歌词
            HPApplication.getInstance().setShowLockScreen(false);
        }

    }

    @Override
    public void onDestroy() {
        mAudioBroadcastReceiver.unregisterReceiver(mActivity.getApplicationContext());
        super.onDestroy();
    }

    @Override
    protected boolean isAddStatusBar() {
        return false;
    }

}  