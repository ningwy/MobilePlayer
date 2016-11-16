package io.github.ningwy.mobileplayer.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.Window;

import io.github.ningwy.mobileplayer.R;

public class SplashActivity extends Activity {

    private Handler handler = new Handler();
    private boolean isEnterMainActivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startMainActivity();
            }
        }, 2000);//两秒后进入主界面
    }

    /**
     * 启动MainActivity
     */
    private void startMainActivity() {
        if (!isEnterMainActivity) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            //防止多次点击打开多个MainActivity，也可以用singleTask来处理
            isEnterMainActivity = true;
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在SplashActivity退出应用后防止再次打开MainActivity
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //点击可进入主界面
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            startMainActivity();
            return true;
        }
        return super.onTouchEvent(event);
    }
}
