package io.github.ningwy.mobileplayer.utils;

import android.content.Context;

/**
 * 尺寸工具
 * Created by ningwy on 2016/11/14.
 */
public class DensityUtil {

    /**
     * 将dp转换为px
     * @param context context
     * @param dp 在页面上显示20dp大小的值
     * @return 转换之后的px
     */
    public static int dp2px(Context context, float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (density * dp + 0.5f);
    }

    /**
     * 将px转换为dp
     * @param context context
     * @param px 在页面上显示20px大小的值
     * @return 转换之后的dp
     */
    public static int px2dp(Context context, float px) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (density / px + 0.5f);
    }

}
