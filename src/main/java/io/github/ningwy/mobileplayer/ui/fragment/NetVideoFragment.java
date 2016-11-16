package io.github.ningwy.mobileplayer.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;

import io.github.ningwy.mobileplayer.R;
import io.github.ningwy.mobileplayer.adapter.NetVideoAdapter;
import io.github.ningwy.mobileplayer.domain.MediaItem;
import io.github.ningwy.mobileplayer.ui.activity.VideoPlayerActivity;
import io.github.ningwy.mobileplayer.utils.CacheUtil;
import io.github.ningwy.mobileplayer.utils.Contstants;
import io.github.ningwy.mobileplayer.utils.LogUtil;

/**
 * 网络视频Fragment
 * Created by ningwy on 2016/10/21.
 */
public class NetVideoFragment extends BaseFragment {

    private static final int UPDATE_LIST_VIEW = 1;
    private ListView mListView;
    private TextView tvNoNet;
    private ProgressBar pbLoading;

    //视频列表
    private ArrayList<MediaItem> mediaItems = new ArrayList<>();

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
            switch (msg.what) {
                case UPDATE_LIST_VIEW :

                    //隐藏progressBar
                    pbLoading.setVisibility(View.GONE);

                    if (mediaItems != null && mediaItems.size() > 0) {
                        tvNoNet.setVisibility(View.GONE);
                        mListView.setAdapter(new NetVideoAdapter(getContext(), mediaItems));
                        /**
                         * ListView恢复点击前的位置
                         * 注意：该方法不能在onResume方法中调用，因为只是在onResume方法中发送了消息，还没有设置Adapter，
                         *      只有设置了Adapter后才有效
                         */
                        mListView.setSelectionFromTop(index, top);
                    } else {
                        tvNoNet.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        }
    };

    @Override
    public View initView(Context context) {
        View view = View.inflate(context, R.layout.net_video_fragment, null);
        mListView = (ListView) view.findViewById(R.id.lv_net_video);
        tvNoNet = (TextView) view.findViewById(R.id.tv_no_net);
        pbLoading = (ProgressBar) view.findViewById(R.id.pb_loading);

        return view;
    }

    //在主线程中运行
    @Override
    public void initData() {
        //读取缓存
        String cacheJson = CacheUtil.getString(getContext(),Contstants.NET_URL);
        if (cacheJson != null) {
            processData(cacheJson);
        } else {
            getDataFromNet();
        }
    }

    @Override
    protected void initEvent() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), VideoPlayerActivity.class);

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
     * 根据url获得数据
     */
    private void getDataFromNet() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                RequestParams entity = new RequestParams(Contstants.NET_URL);
                x.http().get(entity, new Callback.CommonCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        LogUtil.e("onSuccess: " + result);
                        //保存数据
                        CacheUtil.putString(getContext(), Contstants.NET_URL, result);
                        processData(result);
                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {
                        LogUtil.e("onError: " + ex.getMessage());
                    }

                    @Override
                    public void onCancelled(CancelledException cex) {
                        LogUtil.e("onCancelled: " + cex.getMessage());
                    }

                    @Override
                    public void onFinished() {
                        LogUtil.e("onFinished: ");
                        handler.sendEmptyMessage(UPDATE_LIST_VIEW);
                    }
                });
            }
        }.start();
    }

    /**
     * 解析数据
     *
     * @param result
     */
    private void processData(String result) {
        parseJson(result);
    }

    /**
     * 解析json数据
     */
    private void parseJson(String json) {
        try {
            JSONObject jo = new JSONObject(json);
            JSONArray trailers = jo.optJSONArray("trailers");

            if (trailers != null && trailers.length() > 0) {
                for (int i = 0; i < trailers.length(); i++) {
                    JSONObject jsonObject = (JSONObject) trailers.get(i);

                    if (jsonObject != null) {
                        String movieName = jsonObject.optString("movieName");//视频名称
                        String videoTitle = jsonObject.optString("videoTitle");//视频描述
                        String coverImg = jsonObject.optString("coverImg");//视频截图url
                        String hightUrl = jsonObject.optString("hightUrl");//视频高清url
                        MediaItem mediaItem = new MediaItem(hightUrl, videoTitle, coverImg, movieName);
                        //添加到集合中去
                        mediaItems.add(mediaItem);
                    }
                }
            }

            handler.sendEmptyMessage(UPDATE_LIST_VIEW);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private int firstTime = 1;

    @Override
    public void onResume() {
        super.onResume();
        if (firstTime != 1) {
            handler.sendEmptyMessage(UPDATE_LIST_VIEW);
        }
        firstTime++;
    }

    @Override
    public void onPause() {
        super.onPause();
        //保存index和top
        index = mListView.getFirstVisiblePosition();
        View v = mListView.getChildAt(0);
        top = (v == null) ? 0 : v.getTop();
    }
}
