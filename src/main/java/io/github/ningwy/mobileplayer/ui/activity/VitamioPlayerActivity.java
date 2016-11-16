package io.github.ningwy.mobileplayer.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.github.ningwy.mobileplayer.R;
import io.github.ningwy.mobileplayer.domain.MediaItem;
import io.github.ningwy.mobileplayer.ui.view.VitamioVideoView;
import io.github.ningwy.mobileplayer.utils.Utils;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;


public class VitamioPlayerActivity extends Activity implements View.OnClickListener {

    //更新进度条的message id
    private static final int PROGRESS = 1;
    //隐藏控制面板的message id
    private static final int HIDE_MEDIA_CONTROLLER = 2;

    /**
     * 全屏播放
     */
    private static final int FULL_SCREEN = 3;
    /**
     * 默认大小播放
     */
    private static final int DEFAULT_SCREEN = 4;

    //显示网速的message id
    private static final int SHOW_NET_SPEED = 5;


    @InjectView(R.id.vv_player)
    VitamioVideoView vvPlayer;
    @InjectView(R.id.tv_name)
    TextView tvName;
    @InjectView(R.id.iv_battery)
    ImageView ivBattery;
    @InjectView(R.id.tv_system_time)
    TextView tvSystemTime;
    @InjectView(R.id.btn_voice)
    Button btnVoice;
    @InjectView(R.id.seekbar_voice)
    SeekBar seekbarVoice;
    @InjectView(R.id.btn_swich_player)
    Button btnSwichPlayer;
    @InjectView(R.id.ll_top)
    LinearLayout llTop;
    @InjectView(R.id.tv_current_time)
    TextView tvCurrentTime;
    @InjectView(R.id.seekbar_video)
    SeekBar seekbarVideo;
    @InjectView(R.id.tv_duration)
    TextView tvDuration;
    @InjectView(R.id.btn_exit)
    Button btnExit;
    @InjectView(R.id.btn_video_pre)
    Button btnVideoPre;
    @InjectView(R.id.btn_video_start_pause)
    Button btnVideoStartPause;
    @InjectView(R.id.btn_video_next)
    Button btnVideoNext;
    @InjectView(R.id.btn_video_siwch_screen)
    Button btnVideoSiwchScreen;
    @InjectView(R.id.ll_bottom)
    LinearLayout llBottom;
    @InjectView(R.id.rl_media_controller)
    RelativeLayout rlMediaController;
    @InjectView(R.id.tv_buffer_netspeed)
    TextView tvBufferNetspeed;
    @InjectView(R.id.ll_net_speed)
    LinearLayout llNetSpeed;

    /**
     * 视频列表数据
     */
    private ArrayList<MediaItem> mediaItems;

    private Utils utils;

    private BatteryChangedReceiver receiver;

    //当前播放视频在视频列表的位置
    private int position;

    /**
     * 如果是从其他应用中调用该播放器，则传递该uri过去
     */
    private Uri uri;

    private AudioManager mAudioManager;
    /**
     * 最大音量
     */
    private int maxVolume;

    /**
     * 当前音量
     */
    private int volume;

    /**
     * 是否静音
     */
    private boolean isMute;

    /**
     * 表示控制面板的隐藏和现实状态，注意是状态
     * true： 表示控制面板不可见
     * false：表示控制面板可见
     * 默认为隐藏，不可见
     */
    private boolean isHidden = false;

    /**
     * 标识当前视频是否是网络视频
     * true：是网络视频
     * false：不是网络视频
     */
    private boolean isNetUri;

    private GestureDetector detector;

    //屏幕宽度
    private int screenWidth;
    //屏幕高度
    private int screenHeight;

    //视频的真实宽高
    private int videoWidth;
    private int videoHeight;

    //视频播放的当前进度
    private int curPostion;

    /**
     * 视频最后一次播放的位置
     */
    private int lastPosition;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case SHOW_NET_SPEED:
                    tvBufferNetspeed.setText(utils.getNetSpeed(getApplicationContext()));

                    //每秒更新一次：1秒更新一次
                    mHandler.removeMessages(SHOW_NET_SPEED);
                    mHandler.sendEmptyMessageDelayed(SHOW_NET_SPEED, 2000);
                    break;

                case PROGRESS:

                    //获取当前播放进度
                    int currentPosition = (int) vvPlayer.getCurrentPosition();
                    //更新进度条
                    seekbarVideo.setProgress(currentPosition);
                    //更新已播放时间
                    tvCurrentTime.setText(utils.stringForTime(currentPosition));
                    //更新当前时间
                    tvSystemTime.setText(getSystemTime());

                    //如果播放的是网络视频，更新缓冲进度条
                    if (isNetUri) {
                        int bufferPercentage = vvPlayer.getBufferPercentage();//数值范围是0-100
                        int secondProgress = bufferPercentage * seekbarVideo.getMax() / 100;
                        seekbarVideo.setSecondaryProgress(secondProgress);
                    }

                    /**
                     * 如果系统版本低于17，那么之前设置的监听卡顿就会失效，这里需要自定义监听卡顿
                     */
                    if (isNetUri && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        if (currentPosition - lastPosition < 500) {
                            //两次之差如果小于500则视为卡顿了
                            llNetSpeed.setVisibility(View.VISIBLE);
                        } else {
                            llNetSpeed.setVisibility(View.GONE);
                        }
                        //更新lastPosition
                        lastPosition = currentPosition;
                    }

                    //每秒更新一次：1秒更新一次
                    mHandler.removeMessages(PROGRESS);
                    mHandler.sendEmptyMessageDelayed(PROGRESS, 1000);

                    break;

                case HIDE_MEDIA_CONTROLLER:
                    //隐藏控制面板
                    hideMediaController();
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_voice:
                if (!isMute) {
                    updateVolume(0);
                } else {
                    updateVolume(volume);
                }
                isMute = !isMute;

                break;

            case R.id.btn_swich_player:
                showSwitchSystemPlayerDialog();
                break;

            case R.id.btn_exit:
                finish();
                break;

            case R.id.btn_video_pre:
                playPre();
                break;

            case R.id.btn_video_start_pause:
                startPauseToggle();
                break;

            case R.id.btn_video_next:
                playNext();
                break;

            case R.id.btn_video_siwch_screen:
                fullScreenToggle();
                sendHiddenMessage(0);
                break;
        }
    }

    /**
     * 显示切换系统播放器的对话框
     */
    private void showSwitchSystemPlayerDialog() {
        new AlertDialog.Builder(this)
                .setTitle("警示：")
                .setMessage("确定要切换为系统播放器吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startSystemPlayer();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     *打开系统播放器  并且关闭 自己
     */
    private void startSystemPlayer() {

        if (vvPlayer != null) {
            //得到当前播放进度
            curPostion = (int) vvPlayer.getCurrentPosition();
            //释放媒体资源
            vvPlayer.stopPlayback();
        }

        Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.putExtra("curPosition", curPostion);
        if (mediaItems != null && mediaItems.size() > 0) {
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("mediaItems", mediaItems);
            //携带点击的行的position
            intent.putExtra("position", position);
            intent.putExtras(bundle);
        } else if (uri != null) {
            intent.setData(uri);
        }
        startActivity(intent);

        //关闭自己
        finish();
    }

    /**
     * 暂停和播放开关
     */
    public void startPauseToggle() {
        if (vvPlayer.isPlaying()) {
            //视频在播放-设置暂停
            vvPlayer.pause();
            //按钮状态设置播放
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_start_selector);
        } else {
            //视频播放
            vvPlayer.start();
            //按钮状态设置暂停
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
        }
    }

    /**
     * 是否全屏，true：全屏  false：不全屏
     * 默认不全屏
     */
    private boolean isFullScreen;

    /**
     * 全屏切换开关
     */
    private void fullScreenToggle() {
        switchVideoViewSize(isFullScreen ? FULL_SCREEN : DEFAULT_SCREEN);
        isFullScreen = !isFullScreen;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vitamio_player);
        ButterKnife.inject(this);
        //注册Vitamio
        Vitamio.isInitialized(getApplicationContext());

        initData();
        setData();
        setListener();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        utils = new Utils();
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        Intent intent = getIntent();

        //获得curPosition值,默认为0
        curPostion = intent.getIntExtra("curPosition", 0);

        //获得uri
        uri = intent.getData();

        //获取MediaItems
        mediaItems = intent.getParcelableArrayListExtra("mediaItems");
        //获得点击的行的位置
        position = intent.getIntExtra("position", 0);

        //获得屏幕宽高
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

        //获得最大音量
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //设置音量seekbar最大值
        seekbarVoice.setMax(maxVolume);
        //获得当前音量
        volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        //设置seekbar符合当前音量
        seekbarVoice.setProgress(volume);

        //初始化手势识别器
        detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            //双击
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                //全屏切换
                fullScreenToggle();
                //立刻关闭控制面板
                sendHiddenMessage(0);
                return super.onDoubleTap(e);
            }

            //长按
            @Override
            public void onLongPress(MotionEvent e) {
                startPauseToggle();
                sendHiddenMessage(4000);
                super.onLongPress(e);
            }

            //单击
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                rlMediaController.setVisibility(isHidden ? View.VISIBLE : View.GONE);
                isHidden = !isHidden;
                sendHiddenMessage(4000);
                return super.onSingleTapConfirmed(e);
            }

        });

        //开始实时更新网速
        mHandler.sendEmptyMessage(SHOW_NET_SPEED);

        //4秒后隐藏控制面板
        sendHiddenMessage(4000);
    }

    /**
     * 发送隐藏控制面板的消息
     */
    public void sendHiddenMessage(int time) {
        mHandler.removeMessages(HIDE_MEDIA_CONTROLLER);
        mHandler.sendEmptyMessageDelayed(HIDE_MEDIA_CONTROLLER, time);
    }

    /**
     * 设置数据
     */
    private void setData() {
        //设置视频路径
        if (uri != null) {
            //uri不为空，说明是从其他地方调用该播放器的
            vvPlayer.setVideoURI(uri);
            isNetUri = utils.isNetUri(uri.toString());
            //设置name
            //http://www.172.21.201.45:8989/dance4.mp4;
            String name = uri.toString().substring(uri.toString().lastIndexOf("/") + 1, uri.toString().lastIndexOf("" +
                    "."));
            tvName.setText(name);
        } else {
            //uri为空，则根据ListView传递来的数据设置视频路径
            MediaItem mediaItem = mediaItems.get(position);
            //设置name
            tvName.setText(mediaItem.getName());
            vvPlayer.setVideoPath(mediaItem.getData());
            isNetUri = utils.isNetUri(mediaItem.getData());
        }
    }

    /**
     * 得到系统时间
     *
     * @return
     */
    private String getSystemTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }

    /**
     * 设置监听
     */
    private void setListener() {

        //监听电量变化
        receiver = new BatteryChangedReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiver, filter);

        //设置准备监听
        vvPlayer.setOnPreparedListener(new MyOnPreparedListener());

        //设置播放完成监听
        vvPlayer.setOnCompletionListener(new MyOnCompletionListener());

        //设置播放出错监听
        vvPlayer.setOnErrorListener(new MyOnErrorListener());

        //设置播放进度条的拖动监听
        seekbarVideo.setOnSeekBarChangeListener(new VideoOnSeekBarChangedListener());

        //设置音量进度条
        seekbarVoice.setOnSeekBarChangeListener(new VolumeOnSeekBarChangedListener());

        //监听播放网络视频时由于网速不佳导致的卡顿
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            vvPlayer.setOnInfoListener(new MyOnInfoListener());
        }

        //设置点击监听
        btnVoice.setOnClickListener(this);
        btnSwichPlayer.setOnClickListener(this);
        btnExit.setOnClickListener(this);
        btnVideoPre.setOnClickListener(this);
        btnVideoStartPause.setOnClickListener(this);
        btnVideoNext.setOnClickListener(this);
        btnVideoSiwchScreen.setOnClickListener(this);
    }

    class MyOnInfoListener implements MediaPlayer.OnInfoListener {

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            if (isNetUri) {
                switch (what) {
                    //卡顿开始
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START :
                        llNetSpeed.setVisibility(View.VISIBLE);
                        break;

                    //卡顿结束
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END :
                        llNetSpeed.setVisibility(View.GONE);
                        break;
                }
            }
            return true;
        }
    }

    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {

        //当底层解码完成时调用
        @Override
        public void onPrepared(final MediaPlayer mp) {
            vvPlayer.start();//开始播放
            //切换播放器时从记录位置播起
            vvPlayer.seekTo(curPostion);

            //隐藏加载视图
            llNetSpeed.setVisibility(View.GONE);

            //设置按钮状态
            setButtonStatus();

            /**
             * 设置时长并关联到seekBar上
             */
            //得到播放时长
            int duration = (int) vvPlayer.getDuration();
            tvDuration.setText(utils.stringForTime(duration));
            seekbarVideo.setMax(duration);

            //发送更新进度的消息
            mHandler.sendEmptyMessage(PROGRESS);

            //得到视频的真实大小
            videoWidth = mp.getVideoWidth();
            videoHeight = mp.getVideoHeight();

            //设置屏幕大小为默认
            switchVideoViewSize(DEFAULT_SCREEN);
        }
    }

    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (uri != null) {
                finish();
            } else {
                playNext();
            }
        }
    }

    class MyOnErrorListener implements MediaPlayer.OnErrorListener {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Toast.makeText(VitamioPlayerActivity.this, "出错了，该视频无法播放(>_<)", Toast.LENGTH_SHORT).show();
            finish();
            return true;
        }
    }

    class VideoOnSeekBarChangedListener implements SeekBar.OnSeekBarChangeListener {

        /**
         * 进度条变化时回调该方法
         *
         * @param seekBar  进度条
         * @param progress 进度
         * @param fromUser 该值很重要，如果是用户拖动产生的变化，该值为true，
         *                 如果进度条是由handler更新则为false
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser)
                vvPlayer.seekTo(progress);
        }

        //手指接触进度条回调该方法
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mHandler.removeMessages(HIDE_MEDIA_CONTROLLER);
        }

        //手指离开进度条回调该方法
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            sendHiddenMessage(4000);
        }
    }

    class VolumeOnSeekBarChangedListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                /**
                 * 0代表不显示系统音量变化界面，
                 * 1代表显示
                 */
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                volume = progress;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mHandler.removeMessages(HIDE_MEDIA_CONTROLLER);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            sendHiddenMessage(4000);
        }
    }

    class BatteryChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", 0);//0~100
            setBattery(level);
        }
    }

    /**
     * 根据标志切换视频大小
     *
     * @param size
     */
    public void switchVideoViewSize(int size) {
        switch (size) {
            case FULL_SCREEN:
                vvPlayer.setVideoViewSize(screenWidth, screenHeight);
                btnVideoSiwchScreen.setBackgroundResource(R.drawable.btn_video_siwch_screen_default_selector);
                break;

            case DEFAULT_SCREEN:
                //1.设置视频画面的大小
                //视频真实的宽和高
                int mVideoWidth = videoWidth;
                int mVideoHeight = videoHeight;

                //屏幕的宽和高
                int width = screenWidth;
                int height = screenHeight;

                // for compatibility, we adjust size based on aspect ratio
                if (mVideoWidth * height < width * mVideoHeight) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight;
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth;
                }

                vvPlayer.setVideoViewSize(width, height);
                //2.设置按钮的状态--全屏
                btnVideoSiwchScreen.setBackgroundResource(R.drawable.btn_video_siwch_screen_full_selector);
                break;
        }
    }

    /**
     * 设置电量变化
     *
     * @param level
     */
    private void setBattery(int level) {
        if (level <= 0) {
            ivBattery.setImageResource(R.drawable.ic_battery_0);
        } else if (level <= 10) {
            ivBattery.setImageResource(R.drawable.ic_battery_10);
        } else if (level <= 20) {
            ivBattery.setImageResource(R.drawable.ic_battery_20);
        } else if (level <= 40) {
            ivBattery.setImageResource(R.drawable.ic_battery_40);
        } else if (level <= 60) {
            ivBattery.setImageResource(R.drawable.ic_battery_60);
        } else if (level <= 80) {
            ivBattery.setImageResource(R.drawable.ic_battery_80);
        } else if (level <= 100) {
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        }
    }

    /**
     * 播放前一个视频
     */
    private void playPre() {
        position--;
        if (mediaItems != null && mediaItems.size() > 0 && position >= 0) {
            setData();
            vvPlayer.start();
            setButtonStatus();
        }
    }

    /**
     * 播放下一个视频
     */
    private void playNext() {
        position++;
        if (mediaItems != null && mediaItems.size() > 0 && position < mediaItems.size()) {
            setData();
            vvPlayer.start();
            setButtonStatus();
        }
    }

    private void setButtonStatus() {
        //只有在uri为空即不是从其他应用调用该播放器的时候才设置按钮状态，否则会引发
        //NullPointException
        if (mediaItems != null && mediaItems.size() > 0) {
            setButtonPreState(position != 0);
            setButtonNextState(position != (mediaItems.size() - 1));
        } else if (uri != null) {
            //两个按钮设置灰色
            setButtonPreState(false);
            setButtonNextState(false);
        }
    }

    /**
     * 设置播放上一个视频按钮的状态
     *
     * @param isEnable
     */
    private void setButtonPreState(boolean isEnable) {
        if (isEnable) {
            btnVideoPre.setEnabled(true);
            btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
        } else {
            //设置播放上一首按钮不可用
            btnVideoPre.setEnabled(false);
            btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
        }
    }

    /**
     * 设置播放下一个视频按钮的状态
     *
     * @param isEnable
     */
    private void setButtonNextState(boolean isEnable) {
        if (isEnable) {
            //恢复设置
            btnVideoNext.setEnabled(true);
            btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
        } else {
            //设置播放下一个不可用
            btnVideoNext.setEnabled(false);
            btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
        }
    }

    /**
     * 隐藏控制面板
     */
    public void hideMediaController() {
        if (!isHidden) {
            //isHidden为true表示控制面板不可见，取反表示可见
            rlMediaController.setVisibility(View.GONE);
            //将isHidden设置为true
            isHidden = true;
        }
    }

    /**
     * 显示控制面板
     */
    public void showMediaController() {
        if (isHidden) {//如果控制面板不可见，则
            //设置为可见
            rlMediaController.setVisibility(View.VISIBLE);
            //并将isHidden设置为true
            isHidden = false;
        }
    }

    private float startY = 0;
    private int currentVolume;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = (int) event.getY();
                currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                //取消隐藏控制面板
                mHandler.removeMessages(HIDE_MEDIA_CONTROLLER);
                break;

            case MotionEvent.ACTION_MOVE:
                //将控制面板显示出来
                showMediaController();
                float endY = event.getY();
                float dY = (int) (startY - endY);
                //计算改变的音量值
                float delta = dY / screenHeight * maxVolume;
                //计算最终的音量值：为原来的加上改变的
                int volume = (int) Math.min(Math.max(currentVolume + delta, 0), maxVolume);
                updateVolume(volume);
                break;

            case MotionEvent.ACTION_UP:
                sendHiddenMessage(4000);
                break;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //将控制面板显示出来
        showMediaController();
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            currentVolume--;
            updateVolume(currentVolume);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            currentVolume++;
            updateVolume(currentVolume);
            return true;
        }

        //移除4秒后自动隐藏的消息
        mHandler.removeMessages(HIDE_MEDIA_CONTROLLER);
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 更新音量
     *
     * @param volume
     */
    public void updateVolume(int volume) {
        seekbarVoice.setProgress(volume);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        sendHiddenMessage(4000);
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy() {

        //移除所有消息
        mHandler.removeCallbacksAndMessages(null);

        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }
}
