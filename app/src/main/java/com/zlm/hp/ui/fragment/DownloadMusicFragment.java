package com.zlm.hp.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlm.hp.R;
import com.zlm.hp.adapter.DownloadMusicAdapter;
import com.zlm.hp.application.HPApplication;
import com.zlm.hp.db.AudioInfoDB;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.model.Category;
import com.zlm.hp.model.DownloadMessage;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.receiver.DownloadAudioReceiver;
import com.zlm.hp.receiver.FragmentReceiver;

import java.util.ArrayList;
import java.util.List;

import base.utils.ThreadUtil;

/**
 * 下载音乐
 * Created by zhangliangming on 2017/7/23.
 */
public class DownloadMusicFragment extends BaseFragment {

    private DownloadMusicAdapter mAdapter;
    private ArrayList<Category> mDatas;

    /**
     * 列表视图
     */
    private RecyclerView mRecyclerView;
    private static final int LOADDATA = 0;

    /**
     *
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOADDATA:
                    loadDataUtil();
                    break;
            }
        }
    };

    ///注册监听下载广播
    private DownloadAudioReceiver mDownloadAudioReceiver;
    private DownloadAudioReceiver.DownloadAudioReceiverListener mDownloadAudioReceiverListener = new DownloadAudioReceiver.DownloadAudioReceiverListener() {
        @Override
        public void onReceive(Context context, Intent intent) {
            doDownloadAudioReceive(context, intent);
        }

        /**
         * 监听下载广播
         * @param context
         * @param intent
         */
        private void doDownloadAudioReceive(Context context, Intent intent) {
            DownloadMessage downloadMessage = (DownloadMessage) intent.getSerializableExtra(DownloadMessage.KEY);
            if (intent.getAction().equals(DownloadAudioReceiver.ACTION_DOWMLOADMUSICWAIT)) {
                //等待下载
                if (mAdapter != null) {
                    mAdapter.reshViewHolder(downloadMessage.getTaskHash());
                }

            } else if (intent.getAction().equals(DownloadAudioReceiver.ACTION_DOWMLOADMUSICDOWNLOADING)) {
                //下载中
                if (mAdapter != null) {
                    mAdapter.reshViewHolder(downloadMessage.getTaskHash());
                }

            } else if (intent.getAction().equals(DownloadAudioReceiver.ACTION_DOWMLOADMUSICDOWNPAUSE)) {
                //下载暂停
                if (mAdapter != null) {
                    mAdapter.reshViewHolder(downloadMessage.getTaskHash());
                }

            } else if (intent.getAction().equals(DownloadAudioReceiver.ACTION_DOWMLOADMUSICDOWNCANCEL)) {
                //取消下载
                if (mAdapter != null) {
                    mAdapter.resetData();
                }
                mHandler.sendEmptyMessageDelayed(LOADDATA, 100);
            } else if (intent.getAction().equals(DownloadAudioReceiver.ACTION_DOWMLOADMUSICDOWNADD)) {
                //添加新任务
                if (mAdapter != null) {
                    mAdapter.resetData();
                }
                mHandler.sendEmptyMessageDelayed(LOADDATA, 100);
            } else if (intent.getAction().equals(DownloadAudioReceiver.ACTION_DOWMLOADMUSICDOWNFINISH)) {
                //下载完成
                if (mAdapter != null) {
                    mAdapter.resetData();
                }
                mHandler.sendEmptyMessageDelayed(LOADDATA, 100);
            } else if (intent.getAction().equals(DownloadAudioReceiver.ACTION_DOWMLOADMUSICDOWNERROR)) {
                //下载错误
                if (mAdapter != null) {
                    mAdapter.reshViewHolder(null);
                }

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

        private void doAudioReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (action.equals(AudioBroadcastReceiver.ACTION_NULLMUSIC)) {
                mAdapter.reshViewHolderView(null);
            } else if (action.equals(AudioBroadcastReceiver.ACTION_INITMUSIC)) {
                //初始化
                //AudioMessage audioMessage = (AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);
                AudioInfo audioInfo = HPApplication.getInstance().getCurAudioInfo();//audioMessage.getAudioInfo();
                mAdapter.reshViewHolderView(audioInfo);

            }
        }
    };
    private Runnable runnable;


    public DownloadMusicFragment() {

    }

    @Override
    protected void loadData(boolean isRestoreInstance) {
        if (isRestoreInstance) {
            mDatas.clear();
        }
        mHandler.sendEmptyMessageDelayed(LOADDATA, 300);
    }

    @Override
    protected int setContentViewId() {
        return R.layout.layout_fragment_download;
    }

    @Override
    protected void initViews(Bundle savedInstanceState, View mainView) {
        TextView titleView = mainView.findViewById(R.id.title);
        titleView.setText(R.string.download_management);

        //返回
        RelativeLayout backImg = mainView.findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
                Intent closeIntent = new Intent(FragmentReceiver.ACTION_CLOSEDFRAGMENT);
                closeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mActivity.sendBroadcast(closeIntent);

            }
        });

        //
        mRecyclerView = mainView.findViewById(R.id.download_recyclerView);
        //初始化内容视图
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity.getApplicationContext()));

        //
        mDatas = new ArrayList<Category>();
        mAdapter = new DownloadMusicAdapter(mActivity, mDatas);
        mRecyclerView.setAdapter(mAdapter);


        //注册下载广播
        mDownloadAudioReceiver = new DownloadAudioReceiver(mActivity.getApplicationContext());
        mDownloadAudioReceiver.setDownloadAudioReceiverListener(mDownloadAudioReceiverListener);
        mDownloadAudioReceiver.registerReceiver(mActivity.getApplicationContext());


        //注册监听
        mAudioBroadcastReceiver = new AudioBroadcastReceiver(mActivity.getApplicationContext());
        mAudioBroadcastReceiver.setAudioReceiverListener(mAudioReceiverListener);
        mAudioBroadcastReceiver.registerReceiver(mActivity.getApplicationContext());

        //
        showLoadingView();
    }

    private void loadDataUtil() {

        mDatas.clear();

        runnable = new Runnable() {
            @Override
            public void run() {
                Category category = new Category();
                category.setCategoryName(mContext.getString(R.string.downloading));
                List<Object> downloadInfos = AudioInfoDB.getAudioInfoDB(mActivity.getApplicationContext()).getDownloadingAudio();
                category.setCategoryItem(downloadInfos);
                mDatas.add(category);
                //
                category = new Category();
                category.setCategoryName(mContext.getString(R.string.download_success));
                downloadInfos = AudioInfoDB.getAudioInfoDB(mActivity.getApplicationContext()).getDownloadedAudio();
                category.setCategoryItem(downloadInfos);
                mDatas.add(category);

                if (mDatas.size() > 0) {
                    mAdapter.notifyDataSetChanged();
                }
                showContentView();
            }
        };
        ThreadUtil.runInThread(runnable);
    }

    @Override
    public void onDestroy() {
        mAudioBroadcastReceiver.unregisterReceiver(mActivity.getApplicationContext());
        mDownloadAudioReceiver.unregisterReceiver(mActivity.getApplicationContext());
        if(runnable != null) {
            ThreadUtil.cancelThread(runnable);
        }
        super.onDestroy();
    }

    @Override
    protected int setTitleViewId() {
        return R.layout.layout_title;
    }


    @Override
    protected boolean isAddStatusBar() {
        return true;
    }


}
