package io.github.ningwy.mobileplayer.domain;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 媒体信息类
 * Created by ningwy on 2016/10/23.
 */
public class MediaItem implements Parcelable {
    //名称
    private String name;
    //播放地址
    private String data;
    //大小
    private long size;
    //时长
    private long duration;
    //艺术家，作者
    private String artist;
    //截图url
    private String imageUrl;
    //描述信息
    public String desc;

    public MediaItem() {

    }

    //用于展示本地视频列表信息
    public MediaItem(String artist, String data, long duration, String name, long size) {
        this.artist = artist;
        this.data = data;
        this.duration = duration;
        this.name = name;
        this.size = size;
    }

    //用于展示网络视频列表信息
    public MediaItem(String data, String desc, String imageUrl, String name) {
        this.data = data;
        this.desc = desc;
        this.imageUrl = imageUrl;
        this.name = name;
    }

    protected MediaItem(Parcel in) {
        name = in.readString();
        data = in.readString();
        size = in.readLong();
        duration = in.readLong();
        artist = in.readString();
    }

    public static final Creator<MediaItem> CREATOR = new Creator<MediaItem>() {
        @Override
        public MediaItem createFromParcel(Parcel in) {
            return new MediaItem(in);
        }

        @Override
        public MediaItem[] newArray(int size) {
            return new MediaItem[size];
        }
    };

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "MediaItem{" +
                "artist='" + artist + '\'' +
                ", name='" + name + '\'' +
                ", data='" + data + '\'' +
                ", size=" + size +
                ", duration=" + duration +
                ", imageUrl='" + imageUrl + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(data);
        dest.writeLong(size);
        dest.writeLong(duration);
        dest.writeString(artist);
    }
}
