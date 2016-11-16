package io.github.ningwy.mobileplayer.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import io.github.ningwy.mobileplayer.domain.Lyric;

/**
 * 歌词解析工具类
 * Created by ningwy on 2016/11/14.
 */
public class LyricUtil {

    private List<Lyric> lyrics;

    public List<Lyric> parseLyrics(File file) {
        if (file != null && file.exists()) {
            //歌词文件存在
            BufferedReader br;
            try {
                lyrics = new ArrayList<>();
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GBK"));
                String line = "";
                while ((line = br.readLine()) != null) {
                    //[02:04.12][03:37.32][00:59.73]我在这里欢笑
                    Lyric lyric = parseOneLine(line);
                    lyrics.add(lyric);
                }
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            //歌词文件不存在
            lyrics = null;
        }

        return lyrics;
    }

    /**
     * 解析一句歌词
     * @param line 如：[02:04.12]我在这里欢笑
     * @return 歌词内容
     */
    private Lyric parseOneLine(String line) {
        Lyric lyric = new Lyric();

        int pos1 = line.indexOf("[");
        int pos2 = line.indexOf("]");
        if (pos1 == 0 && pos2 != -1) {
            //歌词存在

            //歌词内容
            String content = line.substring(line.lastIndexOf("]") + 1);
            lyric.setLyricText(content);
            //歌词时间
            String[] strs1 = line.split("\\[");//[ 和 02:04.12]我在这里欢笑
            String[] strs2 = strs1[1].split("\\]");//02:04.12 和 ]我在这里欢笑
            String[] strs3 = strs2[0].split(":");// 02 和 04.12
            String min = strs3[0];
            String[] strs4 = strs3[1].split("\\.");// 04 和 12
            String second = strs4[0];
            String mill = strs4[1];
            long time = Long.parseLong(min) * 60 * 1000 + Long.parseLong(second) * 1000 + Long.parseLong(mill) * 10;
            lyric.setTime(time);
        }
        return lyric;
    }

}
