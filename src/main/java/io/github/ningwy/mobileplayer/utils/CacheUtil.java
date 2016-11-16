package io.github.ningwy.mobileplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 缓存工具类
 * Created by ningwy on 2016/11/6.
 */
public class CacheUtil {

    public static void putString(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences("net_video_cache", Context.MODE_PRIVATE);
        sp.edit().putString(key, value).apply();
    }

    public static String getString(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences("net_video_cache", Context.MODE_PRIVATE);
        return sp.getString(key, null);
    }

    public static void putInt(Context context, String key, int value) {
        SharedPreferences sp = context.getSharedPreferences("audio_info", Context.MODE_PRIVATE);
        sp.edit().putInt(key, value).apply();
    }

    public static int getInt(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences("audio_info", Context.MODE_PRIVATE);
        return sp.getInt(key, 0);
    }
}
