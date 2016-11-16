package io.github.ningwy.mobileplayer.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * baseFragment
 * Created by ningwy on 2016/10/21.
 */
public abstract class BaseFragment extends Fragment {

    public View rootView;
    private Context context;

    //数据加载标志，防止重复多次加载数据，浪费流量
    public boolean isInitData;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {

        context = getContext();

        rootView = initView(context);

        //初始化数据
        if (!isInitData) {
            initData();
            isInitData = true;
        }

        initEvent();

        return rootView;
    }

    public abstract View initView(Context context);

    /**
     * 初始化数据
     */
    protected void initData() {

    }

    /**
     * 初始化事件
     */
    protected void initEvent() {

    }
}
