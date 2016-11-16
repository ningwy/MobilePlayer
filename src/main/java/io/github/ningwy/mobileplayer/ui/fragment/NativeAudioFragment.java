package io.github.ningwy.mobileplayer.ui.fragment;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.ningwy.mobileplayer.R;
import io.github.ningwy.mobileplayer.adapter.NativeMediaAdapter;
import io.github.ningwy.mobileplayer.domain.MediaItem;
import io.github.ningwy.mobileplayer.service.MusicPlayService;
import io.github.ningwy.mobileplayer.ui.activity.AudioPlayerActivity;
import io.github.ningwy.mobileplayer.utils.CacheUtil;

/**
 * 本地音乐Fragment
 * Created by ningwy on 2016/10/21.
 */
public class NativeAudioFragment extends BaseFragment {

    private ListView lvNativeVideo;
    private TextView tvNomedia;
    private ProgressBar pbLoading;

    //音频列表
    private ArrayList<MediaItem> mediaItems = new ArrayList<>();

    /**
     * 保存ListView滚动的位置
     * index:是ListView第一个可见item的位置
     * top：ListView第一个可见item距离顶部的距离
     */
    private int index;
    private int top;

    /**
     * 当前歌曲在列表中的位置
     */
    private int currentPosition;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                if (mediaItems != null && mediaItems.size() > 0) {
                    tvNomedia.setVisibility(View.GONE);
                    lvNativeVideo.setAdapter(new NativeMediaAdapter(getContext(), mediaItems, false));
                    /**
                     * ListView恢复点击前的位置
                     * 注意：该方法不能在onResume方法中调用，因为只是在onResume方法中发送了消息，还没有设置Adapter，
                     *      只有设置了Adapter后才有效
                     */
                    lvNativeVideo.setSelectionFromTop(index, top);
                } else {
                    tvNomedia.setVisibility(View.VISIBLE);
                }
            }
            pbLoading.setVisibility(View.GONE);
        }
    };

    @Override
    public View initView(Context context) {
        View view = View.inflate(context, R.layout.native_audio_fragment, null);
        lvNativeVideo = (ListView) view.findViewById(R.id.lv_native_audio);
        tvNomedia = (TextView) view.findViewById(R.id.tv_no_audio);
        pbLoading = (ProgressBar) view.findViewById(R.id.pb_native_audio_loading);

        return view;
    }

    @Override
    public void initData() {
        getAudioFromLocal();
    }

    /**
     * 从本地获取音频文件
     */
    private void getAudioFromLocal() {
        new Thread(){
            @Override
            public void run() {
                ContentResolver nativeVideoResolver = getContext().getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] selection = {
                        MediaStore.Audio.Media.DISPLAY_NAME,//音频名称
                        MediaStore.Audio.Media.DATA,//音频的绝对路径
                        MediaStore.Audio.Media.SIZE,//音频的大小
                        MediaStore.Audio.Media.DURATION,//音频的时长
                        MediaStore.Audio.Media.ARTIST//音频作者，艺术家
                };
                Cursor cursor = nativeVideoResolver.query(uri, selection, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String name = cursor.getString(0);
                        String data = cursor.getString(1);
                        long size = cursor.getLong(2);
                        long duration = cursor.getLong(3);
                        String artist = cursor.getString(4);

                        MediaItem mediaItem = new MediaItem(artist, data, duration, name, size);
                        mediaItems.add(mediaItem);
                    }

                    cursor.close();
                }

                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    @Override
    protected void initEvent() {
        lvNativeVideo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), AudioPlayerActivity.class);
                if (CacheUtil.getInt(getContext(), "position") == position) {
                    //说明两次点击的是同一首歌，不需要重新创建一个MediaPlayer
                    intent.putExtra(MusicPlayService.ISNOTIFICATION, true);
                } else {
                    //否则更新currentPosition
                    currentPosition = position;
                }
                //携带视频列表信息
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("mediaItems", mediaItems);
                intent.putExtras(bundle);
                //携带点击的行的position
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });
    }

    private int firstTime = 1;

    @Override
    public void onResume() {
        super.onResume();
        //除了第一次调用onResume方法不发送消息外，其他情况都发送消息，用于恢复界面，
        //如果第一次也发送消息，则会出现异常情况
        if (firstTime != 1) {
            handler.sendEmptyMessage(0);
        }
        firstTime++;

//        lvNativeVideo.smoothScrollToPosition(firsVisiblePosition);

        lvNativeVideo.setSelectionFromTop(index, top);
    }

    @Override
    public void onPause() {
        super.onPause();
        //保存index和top
        index = lvNativeVideo.getFirstVisiblePosition();
        View v = lvNativeVideo.getChildAt(0);
        top = (v == null) ? 0 : v.getTop();

        //保存position
        //保存歌曲在列表中位置currentPosition信息
        CacheUtil.putInt(getContext(), "position", currentPosition);
    }
}
