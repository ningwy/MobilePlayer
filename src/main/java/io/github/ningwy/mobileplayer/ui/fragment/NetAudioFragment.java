package io.github.ningwy.mobileplayer.ui.fragment;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import io.github.ningwy.mobileplayer.utils.LogUtil;

/**
 * 网络音乐Fragment
 * Created by ningwy on 2016/10/21.
 */
public class NetAudioFragment extends BaseFragment {

    @Override
    public View initView(Context context) {
        TextView tv = new TextView(context);
        tv.setText(this.getClass().getSimpleName());
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(Color.RED);
        return tv;
    }

    @Override
    public void initData() {
        LogUtil.e("网络音乐初始化数据了");
    }
}
