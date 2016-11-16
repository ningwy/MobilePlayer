package io.github.ningwy.mobileplayer.domain;

/**
 * 一行歌词对应的类
 * Created by ningwy on 2016/11/14.
 */
public class Lyric {

    //每句歌词对应的时间戳
    private long time;

    //该行歌词文本
    private String lyricText;

    //该行歌词显示的时间
    private long showTime;

    public Lyric() {
    }

    public Lyric(String lyricText, long showTime, long time) {
        this.lyricText = lyricText;
        this.showTime = showTime;
        this.time = time;
    }

    public String getLyricText() {
        return lyricText;
    }

    public void setLyricText(String lyricText) {
        this.lyricText = lyricText;
    }

    public long getShowTime() {
        return showTime;
    }

    public void setShowTime(long showTime) {
        this.showTime = showTime;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Lyric{" +
                "lyricText='" + lyricText + '\'' +
                ", time=" + time +
                ", showTime=" + showTime +
                '}';
    }
}
