package android.zhanghai.me.mobsmsdemo.base;

import android.app.Application;

import com.mob.MobSDK;

/**
 * @author ytempest
 *         Description：
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化 MobSDK
        MobSDK.init(this, "27cdc1d5f5ff5", "558df308bbb5de301500d31121438c15");
    }
}
