package io.github.ningwy.mobileplayer.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import io.github.ningwy.mobileplayer.IMusicPlayService;
import io.github.ningwy.mobileplayer.R;
import io.github.ningwy.mobileplayer.domain.MediaItem;
import io.github.ningwy.mobileplayer.ui.activity.AudioPlayerActivity;
import io.github.ningwy.mobileplayer.utils.ToastUtil;

public class MusicPlayService extends Service {

    //更新seekbar最大值
    public static final String UPDATE_SEEKBAR = "io.github.ningwy.UPDATE_SEEKBAR";
    public static final String ISNOTIFICATION = "is_from_notification";

    /**
     * 播放模式
     */
    //顺序播放
    public static final int ORDER_PLAY = 0;
    //随机播放
    public static final int RANDOM_PLAY = 1;
    //列表循环
    public static final int LIST_CYCLE = 2;
    //单曲循环
    public static final int SINGLE_CYCLE = 3;
    //当前播放模式:默认为顺序播放
    public int currentMode = ORDER_PLAY;

    private MediaPlayer mediaPlayer;
    private ArrayList<MediaItem> mediaItems;

    private int position;

    private NotificationManager mNotificationManager;

    //当前播放的mediaItem对象
    private MediaItem mediaItem;

    private Binder mBinder = new IMusicPlayService.Stub() {

        MusicPlayService musicPlayService = MusicPlayService.this;

        @Override
        public void openAudio(int position) throws RemoteException {
            musicPlayService.openAudio(position);
        }

        @Override
        public void start() throws RemoteException {
            mediaPlayer.start();
        }

        @Override
        public void pause() throws RemoteException {
            mediaPlayer.pause();
        }

        @Override
        public void stop() throws RemoteException {
            mediaPlayer.stop();
        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            if (mediaPlayer != null) {
                return mediaPlayer.getCurrentPosition();
            }
            return 0;
        }

        @Override
        public int getDuration() throws RemoteException {
            if (mediaPlayer != null) {
                return mediaPlayer.getDuration();
            }
            return 0;
        }

        @Override
        public String getArtist() throws RemoteException {
            if (mediaItems != null && mediaItems.size() > 0) {
                return mediaItems.get(position).getArtist();
            }
            return null;
        }

        @Override
        public String getName() throws RemoteException {
            if (mediaItems != null && mediaItems.size() > 0) {
                return mediaItems.get(position).getName();
            }
            return null;
        }

        @Override
        public String getAudioPath() throws RemoteException {
            return mediaItems.get(position).getData();
        }

        @Override
        public void playNext() throws RemoteException {
            musicPlayService.playNext();
        }

        @Override
        public void playPre() throws RemoteException {
            musicPlayService.playPre();
        }

        /**
         * 设置播放模式
         * @param playMode 有ORDER_PLAY、RANDOM_PLAY、LIST_CYCLE、SINGLE_CYCLE
         * @throws RemoteException
         */
        @Override
        public void setPlayMode(int playMode) throws RemoteException {
            currentMode = playMode;
        }

        @Override
        public int getPlayMode() throws RemoteException {
            return musicPlayService.getPlayMode();
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return mediaPlayer.isPlaying();
        }

        @Override
        public void setPlayProgress(int progress) {
            mediaPlayer.seekTo(progress);
        }
    };

    public MusicPlayService() {}

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        //从Intent中得到数据
        mediaItems = intent.getParcelableArrayListExtra("mediaItems");
        return mBinder;
    }

    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            playNext();
        }
    }

    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {

        @Override
        public void onPrepared(MediaPlayer mp) {
            //播放歌曲
            mediaPlayer.start();
            //发送广播通知更新歌曲信息
            sendUpdateMusicBroadcast();
            //显示通知
            showNotification();
        }
    }

    /**
     * 发送广播通知更新歌曲信息
     */
    public void sendUpdateMusicBroadcast() {
        Intent intent = new Intent();
        intent.setAction(UPDATE_SEEKBAR);
        //广播当前播放歌曲的位置
        intent.putExtra("position", position);
        sendBroadcast(intent);
    }

    /**
     * 在通知栏显示播放音乐的通知
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void showNotification() {
        //在通知栏显示播放音乐
        Intent intent = new Intent(this, AudioPlayerActivity.class);
        //标志是否来自通知栏
        intent.putExtra(ISNOTIFICATION, true);
        //携带视频列表信息
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("mediaItems", mediaItems);
        intent.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.notification_music_playing)
                .setContentTitle("321影音")
                .setContentText("正在播放:" + mediaItem.getName())
                .setContentIntent(pendingIntent)
                .build();
        mNotificationManager.notify(1, notification);
    }

    class MyOnErrorListener implements MediaPlayer.OnErrorListener {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Toast.makeText(getApplicationContext(), "播放出错", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    /**
     * 设置播放下一首歌的position
     */
    private void setNextPosition() {
        switch (getPlayMode()) {
            case MusicPlayService.ORDER_PLAY:
                position++;
                if (position >= mediaItems.size()) {
                    ToastUtil.makeText(getApplicationContext(), "已经到最后一首了");
                    position--;
                }
                break;
            case MusicPlayService.RANDOM_PLAY:
                position = (int) (Math.random() * mediaItems.size());
                break;
            case MusicPlayService.LIST_CYCLE:
                position++;
                position = position % mediaItems.size();
                break;
            case MusicPlayService.SINGLE_CYCLE:
                //不错处理
                break;
        }
    }

    /**
     * 设置播放上一首歌的position
     */
    private void setPrePosition() {
        switch (getPlayMode()) {
            case MusicPlayService.ORDER_PLAY:
                position--;
                if (position < 0) {
                    ToastUtil.makeText(getApplicationContext(), "已经到第一首了");
                    position++;
                }
                break;
            case MusicPlayService.RANDOM_PLAY:
                position = (int) (Math.random() * mediaItems.size());
                break;
            case MusicPlayService.LIST_CYCLE:
                position--;
                if (position < 0) {
                    position = mediaItems.size() - 1;
                }
                break;
            case MusicPlayService.SINGLE_CYCLE:
                //不错处理
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //取消通知
        mNotificationManager.cancel(1);
    }

    public void openAudio(int position) {
        //更新position
        this.position = position;
        if (mediaItems != null && mediaItems.size() > 0) {
            mediaItem = mediaItems.get(position);
            try {
                if (mediaPlayer != null) {
                    mediaPlayer.reset();
                }
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(mediaItem.getData());
                //设置监听
                mediaPlayer.setOnPreparedListener(new MyOnPreparedListener());
                mediaPlayer.setOnCompletionListener(new MyOnCompletionListener());
                mediaPlayer.setOnErrorListener(new MyOnErrorListener());
                mediaPlayer.prepareAsync();//当准备好的时候会回调MyOnPreparedListener中的onPrepare方法
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 播放上一首
     */
    public void playNext() {
        //设置播放下一首歌的position
        setNextPosition();
        //播放下一首歌
        openAudio(position);
    }

    /**
     * 播放下一首
     */
    public void playPre() {
        //设置播放上一首歌的position
        setPrePosition();
        //播放上一首歌
        openAudio(position);
    }

    public int getPlayMode() {
        return currentMode;
    }
}
