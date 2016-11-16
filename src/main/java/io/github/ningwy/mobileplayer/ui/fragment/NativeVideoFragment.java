package io.github.ningwy.mobileplayer.ui.fragment;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
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

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import io.github.ningwy.mobileplayer.R;
import io.github.ningwy.mobileplayer.adapter.NativeMediaAdapter;
import io.github.ningwy.mobileplayer.domain.MediaItem;
import io.github.ningwy.mobileplayer.ui.activity.VideoPlayerActivity;

/**
 * 本地视频Fragment
 * Created by ningwy on 2016/10/21.
 */
public class NativeVideoFragment extends BaseFragment {

    private ListView lvNativeVideo;
    private TextView tvNomedia;
    private ProgressBar pbLoading;

    private Context context;

    //视频列表
    private ArrayList<MediaItem> mediaItems;

    /**
     * 保存ListView滚动的位置
     * index:是ListView第一个可见item的位置
     * top：ListView第一个可见item距离顶部的距离
     */
    private int index;
    private int top;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                if (mediaItems != null && mediaItems.size() > 0) {
                    tvNomedia.setVisibility(View.GONE);
                    //设置Adapter
                    lvNativeVideo.setAdapter(new NativeMediaAdapter(context, mediaItems, true));
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

        this.context = context;

        View view = View.inflate(context, R.layout.native_video_fragment, null);
        lvNativeVideo = (ListView) view.findViewById(R.id.lv_native_video);
        tvNomedia = (TextView) view.findViewById(R.id.tv_nomedia);
        pbLoading = (ProgressBar) view.findViewById(R.id.pb_loading);

        return view;
    }

    @Override
    public void initData() {
        mediaItems = new ArrayList<>();

        getDataFromNative();

    }

    @Override
    protected void initEvent() {
        lvNativeVideo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(context, VideoPlayerActivity.class);

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

    /**
     * 获取本地视频数据
     */
    private void getDataFromNative() {
        new Thread(){
            @Override
            public void run() {
                ContentResolver nativeVideoResolver = context.getContentResolver();
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] selection = {
                        MediaStore.Video.Media.DISPLAY_NAME,//视频名称
                        MediaStore.Video.Media.DATA,//视频的绝对路径
                        MediaStore.Video.Media.SIZE,//视频的大小
                        MediaStore.Video.Media.DURATION,//视频的时长
                        MediaStore.Video.Media.ARTIST//视频作者，艺术家
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

    /**
     * 扫描本地视频
     * @param list
     * @param file
     */
    private void getVideoFile(final List<MediaItem> list, File file) {// 获得视频文件

        file.listFiles(new FileFilter() {

            @Override
            public boolean accept(File file) {
                // sdCard找到视频名称
                String name = file.getName();

                int i = name.indexOf('.');
                if (i != -1) {
                    name = name.substring(i);
                    if (name.equalsIgnoreCase(".mp4")
                            || name.equalsIgnoreCase(".3gp")
                            || name.equalsIgnoreCase(".wmv")
                            || name.equalsIgnoreCase(".ts")
                            || name.equalsIgnoreCase(".rmvb")
                            || name.equalsIgnoreCase(".mov")
                            || name.equalsIgnoreCase(".m4v")
                            || name.equalsIgnoreCase(".avi")
                            || name.equalsIgnoreCase(".m3u8")
                            || name.equalsIgnoreCase(".3gpp")
                            || name.equalsIgnoreCase(".3gpp2")
                            || name.equalsIgnoreCase(".mkv")
                            || name.equalsIgnoreCase(".flv")
                            || name.equalsIgnoreCase(".divx")
                            || name.equalsIgnoreCase(".f4v")
                            || name.equalsIgnoreCase(".rm")
                            || name.equalsIgnoreCase(".asf")
                            || name.equalsIgnoreCase(".ram")
                            || name.equalsIgnoreCase(".mpg")
                            || name.equalsIgnoreCase(".v8")
                            || name.equalsIgnoreCase(".swf")
                            || name.equalsIgnoreCase(".m2v")
                            || name.equalsIgnoreCase(".asx")
                            || name.equalsIgnoreCase(".ra")
                            || name.equalsIgnoreCase(".ndivx")
                            || name.equalsIgnoreCase(".xvid")) {
                        MediaItem vi = new MediaItem();
                        vi.setName(file.getName());
                        vi.setData(file.getAbsolutePath());
                        vi.setSize(file.length());
                        MediaPlayer mMediaPlayer = MediaPlayer.create(context, Uri.parse(file.getAbsolutePath()));
                        vi.setDuration(mMediaPlayer.getDuration());
                        list.add(vi);
                        return true;
                    }
                } else if (file.isDirectory()) {
                    getVideoFile(list, file);
                }
                return false;
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
    }

    @Override
    public void onPause() {
        super.onPause();
        //保存index和top
        index = lvNativeVideo.getFirstVisiblePosition();
        View v = lvNativeVideo.getChildAt(0);
        top = (v == null) ? 0 : v.getTop();
    }
}
