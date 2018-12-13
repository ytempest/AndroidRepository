package com.ytempest.studentmanage.activity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.ytempest.studentmanage.R;
import com.ytempest.studentmanage.util.LoginInfoUtils;

/**
 * @author ytempest
 *         Description：
 */
public class WelcomeActivity extends Activity {

    private static final long WAIT_TIME = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // wait for a moment start activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 判断用户是否已经登录，如果已经登录则直接跳转到主页，否则跳转到登录页面
                Intent intent = new Intent(WelcomeActivity.this,
                        LoginInfoUtils.isUserLogined(WelcomeActivity.this) ? MainActivity.class : UserLoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, WAIT_TIME);

        TextView textView = findViewById(R.id.text_view);
        ObjectAnimator textAnimation = ObjectAnimator.ofFloat(textView, "alpha", 0, 1f);
        textAnimation.setDuration(WAIT_TIME);
        textAnimation.start();
    }


}
