package io.github.ningwy.mobileplayer.ui.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.github.ningwy.mobileplayer.IMusicPlayService;
import io.github.ningwy.mobileplayer.R;
import io.github.ningwy.mobileplayer.domain.MediaItem;
import io.github.ningwy.mobileplayer.service.MusicPlayService;
import io.github.ningwy.mobileplayer.ui.view.LyricTextView;
import io.github.ningwy.mobileplayer.utils.CacheUtil;
import io.github.ningwy.mobileplayer.utils.LogUtil;
import io.github.ningwy.mobileplayer.utils.LyricUtil2;
import io.github.ningwy.mobileplayer.utils.ToastUtil;
import io.github.ningwy.mobileplayer.utils.Utils;

public class AudioPlayerActivity extends Activity implements View.OnClickListener {

    private static final int UPDATE_PROGRESS = 1;
    @InjectView(R.id.iv_icon)
    ImageView ivIcon;
    @InjectView(R.id.tv_artist)
    TextView tvArtist;
    @InjectView(R.id.tv_name)
    TextView tvName;
    @InjectView(R.id.tv_time)
    TextView tvTime;
    @InjectView(R.id.seekbar_audio)
    SeekBar seekbarAudio;
    @InjectView(R.id.btn_audio_playmode)
    Button btnAudioPlaymode;
    @InjectView(R.id.btn_audio_pre)
    Button btnAudioPre;
    @InjectView(R.id.btn_audio_start_pause)
    Button btnAudioStartPause;
    @InjectView(R.id.btn_audio_next)
    Button btnAudioNext;
    @InjectView(R.id.btn_lyrc)
    Button btnLyrc;
    @InjectView(R.id.ltv_lyric)
    LyricTextView ltvLyric;

    private AnimationDrawable ad;

    private Utils utils;
    private LyricUtil2 lyricUtil;

    private ArrayList<MediaItem> mediaItems;

    private UpdateSeekBarBroadcastReceiver receiver;

    /**
     * 歌曲在列表中的位置
     */
    private int position;

    /**
     * 表示是否是从通知栏打开该Activity
     * true：是
     * false：不是
     */
    private boolean isNotification;

    private IMusicPlayService musicPlayService;

    /**
     * 歌词是否显示
     * true：显示
     * false：不显示
     * 默认是显示
     */
    private boolean isLyricShow = true;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_PROGRESS:
                    //设置时间信息
                    try {
                        int progress = musicPlayService.getCurrentPosition();
                        String time = utils.stringForTime(progress) + "/" + utils.stringForTime(musicPlayService
                                .getDuration());
                        tvTime.setText(time);
                        seekbarAudio.setProgress(progress);
                        ltvLyric.setCurrentPosition(progress);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    //每秒更新一次
                    mHandler.removeMessages(UPDATE_PROGRESS);
                    mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 1000);
                    break;
            }
        }
    };

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicPlayService = IMusicPlayService.Stub.asInterface(service);

            //设置音乐信息
            try {
                if (!isNotification) {
                    //不是从通知栏进来的要设置url数据
                    musicPlayService.openAudio(position);
                } else {
                    //如果是从通知栏进来的，则只需要设置seekBar的Max值和发送更新消息即可
                    musicPrepared();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            //播放动画
            if (ad != null) {
                ad.start();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicPlayService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
        ButterKnife.inject(this);

        initData();
        bindAndStartService();
        initEvent();
    }

    /**
     * 设置事件
     */
    private void initEvent() {
        seekbarAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    try {
                        musicPlayService.setPlayProgress(progress);
                        ltvLyric.setCurrentPosition(progress);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //注册广播
        receiver = new UpdateSeekBarBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicPlayService.UPDATE_SEEKBAR);
        registerReceiver(receiver, filter);
    }

    /**
     * 设置数据
     */
    private void setData() {
        if (mediaItems != null && mediaItems.size() > 0) {
            MediaItem mediaItem = mediaItems.get(position);
            tvName.setText(mediaItem.getName());
            tvArtist.setText(mediaItem.getArtist());
        }
    }

    /**
     * 绑定并且启动服务
     */
    private void bindAndStartService() {
        Intent intent = new Intent(this, MusicPlayService.class);
        //携带视频列表信息
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("mediaItems", mediaItems);
        intent.putExtras(bundle);
        //绑定服务
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        //启动服务
        startService(intent);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        //设置点击监听
        btnAudioPlaymode.setOnClickListener(this);
        btnAudioPre.setOnClickListener(this);
        btnAudioStartPause.setOnClickListener(this);
        btnAudioNext.setOnClickListener(this);
        btnLyrc.setOnClickListener(this);

        utils = new Utils();
        lyricUtil = new LyricUtil2();

        //得到动画资源
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            ad = (AnimationDrawable) getDrawable(R.drawable.audio_player_anim);
        } else {
            ad = (AnimationDrawable) getResources().getDrawable(R.drawable.audio_player_anim);
        }
        //设置为ImageView
        ivIcon.setBackgroundDrawable(ad);

        //从Intent中得到数据
        Intent intent = getIntent();
        mediaItems = intent.getParcelableArrayListExtra("mediaItems");
        position = intent.getIntExtra("position", 0);
        isNotification = intent.getBooleanExtra(MusicPlayService.ISNOTIFICATION, false);
    }

    @Override
    public void onClick(View v) {
        if (v == btnAudioPlaymode) {
            // Handle clicks for btnAudioPlaymode
            switchPlayMode();
        } else if (v == btnAudioPre) {
            // Handle clicks for btnAudioPre
            try {
                //设置上一首的播放地址
                if (musicPlayService.getPlayMode() == MusicPlayService.SINGLE_CYCLE) {
                    position--;
                }
                //播放上一首
                musicPlayService.playPre();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (v == btnAudioStartPause) {
            // Handle clicks for btnAudioStartPause
            //更新动画
            if (ad.isRunning()) {
                ad.stop();
            } else {
                ad.start();
            }

            //播放or暂停
            startAndPauseToggle();
        } else if (v == btnAudioNext) {
            // Handle clicks for btnAudioNext
            try {
                //设置下一首的播放地址
                if (musicPlayService.getPlayMode() == MusicPlayService.SINGLE_CYCLE) {
                    position++;
                }
                //播放下一首
                musicPlayService.playNext();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (v == btnLyrc) {
            // Handle clicks for btnLyrc
            if (isLyricShow) {
                ltvLyric.setVisibility(View.GONE);
            } else {
                ltvLyric.setVisibility(View.VISIBLE);
            }
            isLyricShow = !isLyricShow;
        }
    }

    /**
     * 点击按钮切换播放模式
     */
    public void switchPlayMode() {
        int playMode = CacheUtil.getInt(getApplicationContext(), "playMode");
        playMode++;
        playMode = playMode % 4;
        switchModeStatus(playMode);
        //Toast提示
        switch (playMode) {
            case MusicPlayService.ORDER_PLAY:
                ToastUtil.makeText(getApplicationContext(), "顺序播放");
                break;
            case MusicPlayService.RANDOM_PLAY:
                ToastUtil.makeText(getApplicationContext(), "随机播放");
                break;
            case MusicPlayService.LIST_CYCLE:
                ToastUtil.makeText(getApplicationContext(), "列表循环");
                break;
            case MusicPlayService.SINGLE_CYCLE:
                ToastUtil.makeText(getApplicationContext(), "单曲循环");
                break;
        }
    }

    /**
     * 切换播放模式
     */
    private void switchModeStatus(int playMode) {
        try {
            switch (playMode) {
                case MusicPlayService.ORDER_PLAY:
                    btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_order_selector);
                    break;
                case MusicPlayService.RANDOM_PLAY:
                    btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_random_selector);
                    break;
                case MusicPlayService.LIST_CYCLE:
                    btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_list_selector);
                    break;
                case MusicPlayService.SINGLE_CYCLE:
                    btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_single_selector);
                    break;
            }
            musicPlayService.setPlayMode(playMode);
            //保存播放模式
            CacheUtil.putInt(getApplicationContext(), "playMode", playMode);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 播放和暂停切换
     */
    private void startAndPauseToggle() {
        try {
            if (musicPlayService.isPlaying()) {
                musicPlayService.pause();
                btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);
                //停止发消息
                mHandler.removeMessages(UPDATE_PROGRESS);
            } else {
                musicPlayService.start();
                btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
                //继续发消息
                mHandler.sendEmptyMessage(UPDATE_PROGRESS);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    class UpdateSeekBarBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                //更新position数据
                position = intent.getIntExtra("position", 0);
                //当底层解码完成之后调用该方法设置seekBar的最大值为歌曲时长，并发送更新seekBar的消息
                musicPrepared();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 当底层解码完成之后调用该方法设置seekBar的最大值为歌曲时长，并发送更新seekBar的消息
     *
     * @throws RemoteException
     */
    public void musicPrepared() throws RemoteException {
        //设置名字、艺术家等信息
        setData();
        //设置播放模式
        switchModeStatus(CacheUtil.getInt(getApplicationContext(), "playMode"));
        //设置seekBar最大值
        seekbarAudio.setMax(musicPlayService.getDuration());
        //解析歌词
        parseLyric();
        //发消息更新进度条
        mHandler.sendEmptyMessage(UPDATE_PROGRESS);
    }

    /**
     * 解析歌词
     */
    private void parseLyric() {
        String musicName = mediaItems.get(position).getName();
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "Music/Lyric/" + musicName
                .substring(0, musicName.lastIndexOf(".")) + ".lrc";
        LogUtil.e("path:" + path);
        File file = new File(path);
        lyricUtil.readLyricFile(file);//解析歌词
        ltvLyric.setLyrics(lyricUtil.getLyrics());
    }

    @Override
    protected void onResume() {
        super.onResume();
        //获取歌曲在列表中的位置
        if (isNotification) {
            //如果是从通知栏打开Activity，则获取歌曲在列表中的位置，否则就用从Intent中获取的position
            position = CacheUtil.getInt(this, "position");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //保存歌曲在列表中位置信息
        CacheUtil.putInt(this, "position", position);
    }

    @Override
    protected void onDestroy() {
        //移除所有消息
        mHandler.removeCallbacksAndMessages(null);

        //解绑服务
        if (conn != null) {
            unbindService(conn);
            conn = null;
        }

        //取消注册广播
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }
}
