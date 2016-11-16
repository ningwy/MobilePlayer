package io.github.ningwy.mobileplayer.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * 吐司工具类
 * Created by ningwy on 2016/11/11.
 */
public class ToastUtil {

    private static Toast toast;

    public static void makeText(Context context, String text) {
        if (toast == null) {
            toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        } else {
            toast.setText(text);
        }
        toast.show();
    }

}
