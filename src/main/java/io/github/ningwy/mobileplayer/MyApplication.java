package io.github.ningwy.mobileplayer;

import android.app.Application;

import org.xutils.x;

/**
 * 自定义Application
 * Created by ningwy on 2016/11/5.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
    }
}
