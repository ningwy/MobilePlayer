package io.github.ningwy.mobileplayer.ui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

import io.github.ningwy.mobileplayer.R;
import io.github.ningwy.mobileplayer.ui.fragment.BaseFragment;
import io.github.ningwy.mobileplayer.ui.fragment.NativeAudioFragment;
import io.github.ningwy.mobileplayer.ui.fragment.NativeVideoFragment;
import io.github.ningwy.mobileplayer.ui.fragment.NetAudioFragment;
import io.github.ningwy.mobileplayer.ui.fragment.NetVideoFragment;

public class MainActivity extends FragmentActivity {

    @butterknife.InjectView(R.id.rg_main)
    RadioGroup rgMain;

    //当前选中的Fragment的位置
    private int position;

    private List<BaseFragment> baseFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        butterknife.ButterKnife.inject(this);

        initView();
        initEvent();

        //默认选中第一个
        rgMain.check(R.id.rb_native_video);

    }

    private void initEvent() {
        rgMain.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_native_video :
                        position = 0;
                        break;

                    case R.id.rb_net_video :
                        position = 1;
                        break;

                    case R.id.rb_native_audio :
                        position = 2;
                        break;

                    case R.id.rb_net_audio :
                        position = 3;
                        break;
                    default:
                        position = 0;
                        break;
                }

                switchFragment();
            }
        });
    }

    /**
     * 根据position切换到相应的fragment
     */
    private void switchFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fl_main_content, baseFragments.get(position));
        transaction.commit();
    }

    private void initView() {
        baseFragments = new ArrayList<>();
        baseFragments.add(new NativeVideoFragment());
        baseFragments.add(new NetVideoFragment());
        baseFragments.add(new NativeAudioFragment());
        baseFragments.add(new NetAudioFragment());
    }
}
