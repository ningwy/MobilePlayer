package io.github.ningwy.mobileplayer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import io.github.ningwy.mobileplayer.R;
import io.github.ningwy.mobileplayer.domain.MediaItem;

/**
 * 本地视频适配器
 * Created by ningwy on 2016/10/23.
 */
public class NetVideoAdapter extends BaseAdapter {

    private List<MediaItem> mediaItems;

    private Context context;


    public NetVideoAdapter(Context context, List<MediaItem> mediaItems) {
        this.context = context;
        this.mediaItems = mediaItems;
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
            convertView = View.inflate(context, R.layout.net_video_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
            viewHolder.tvDesc = (TextView) convertView.findViewById(R.id.tv_desc);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        MediaItem mediaItem = mediaItems.get(position);
        viewHolder.tvName.setText(mediaItem.getName());
        viewHolder.tvDesc.setText(mediaItem.getDesc());
//        x.image().bind(viewHolder.ivIcon, mediaItem.getImageUrl());
        Glide.with(context).load(mediaItem.getImageUrl()).into(viewHolder.ivIcon);

        return convertView;
    }

    static class ViewHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvDesc;
    }
}
