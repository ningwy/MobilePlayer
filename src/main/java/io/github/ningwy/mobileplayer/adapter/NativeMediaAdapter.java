package io.github.ningwy.mobileplayer.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.github.ningwy.mobileplayer.R;
import io.github.ningwy.mobileplayer.domain.MediaItem;
import io.github.ningwy.mobileplayer.utils.Utils;

/**
 * 本地媒体数据适配器
 * Created by ningwy on 2016/10/23.
 */
public class NativeMediaAdapter extends BaseAdapter {

    private List<MediaItem> mediaItems;

    private Context context;

    private Utils utils;

    /**
     * 用该标识来标志是否是视频数据
     * true：是视频
     * false：音频
     * 默认是视频
     */
    private boolean isVideo = true;

    public NativeMediaAdapter(Context context, List<MediaItem> mediaItems, boolean isVideo) {
        this.context = context;
        this.mediaItems = mediaItems;
        this.isVideo = isVideo;
        utils = new Utils();
    }

    @Override
    public int getCount() {
        return mediaItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mediaItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.native_video_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
            viewHolder.tvTime = (TextView) convertView.findViewById(R.id.tv_time);
            viewHolder.tvSize = (TextView) convertView.findViewById(R.id.tv_size);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        MediaItem mediaItem = mediaItems.get(position);
        viewHolder.tvName.setText(mediaItem.getName());
        viewHolder.tvTime.setText(utils.stringForTime((int) mediaItem.getDuration()));
        viewHolder.tvSize.setText(Formatter.formatFileSize(context, mediaItem.getSize()));

        if (isVideo) {
            viewHolder.ivIcon.setImageResource(R.drawable.video_default_icon);
        } else {
            viewHolder.ivIcon.setImageResource(R.drawable.music_default_bg);
        }

//        //打标签
//        viewHolder.ivIcon.setTag(mediaItem.getData());
//
//        VideoThumbnailLoader.getInstance().display(viewHolder.ivIcon, context, context.getContentResolver(),
//                mediaItem.getData(), new VideoThumbnailLoader.ThumbnailListener() {
//
//            @Override
//            public void onThumbnailLoadCompleted(String url, ImageView iv, Bitmap bitmap) {
//                //通过判断imageview的tag,和我们加载的图片的url是否是同一个来判断是否显示,这样可以避免滑动造成的位置错乱等问题.
//                String tag = (String) iv.getTag();
//                if (null != bitmap && null != tag && tag.equals(url)) {
//                    iv.setImageBitmap(bitmap);
//                } else {
//                    iv.setImageResource(R.drawable.video_default_icon);
//                }
//            }
//        });

        return convertView;
    }

    static class ViewHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvTime;
        TextView tvSize;
    }
}
