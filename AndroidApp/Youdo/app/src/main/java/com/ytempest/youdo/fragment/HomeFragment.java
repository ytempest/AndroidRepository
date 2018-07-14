package com.ytempest.youdo.fragment;

import android.util.Log;

import com.ytempest.baselibrary.base.BaseFragment;
import com.ytempest.framelibrary.net.OkHttpManager;
import com.ytempest.framelibrary.net.listener.DataDisposeListener;
import com.ytempest.framelibrary.net.request.RequestCreator;
import com.ytempest.framelibrary.net.request.RequestParams;
import com.ytempest.youdo.R;
import com.ytempest.youdo.result.Result;

import okhttp3.Request;

/**
 * @author ytempest
 *         Description：
 */
public class HomeFragment extends BaseFragment {

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home_layout;
    }

    @Override
    protected void initView() {
    }

    @Override
    protected void initData() {
        String url = "http://v2.ffu365.com/index.php?m=Api&c=Index&a=home&appid=1";
        RequestParams params = new RequestParams()
                .put("m", "Api")
                .put("c", "Index")
                .put("a", "home")
                .put("appid", "1");
        final Request request = RequestCreator.createGetRequest(url, params);
        OkHttpManager.get(request, new DataDisposeListener<Result>() {
            @Override
            public void onSucceed(Result result) {
                Log.e(TAG, "onSucceed: result -->" + result.getErrmsg());
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e(TAG, "onFailure: throwable --> " + throwable.getMessage());
            }
        });

    }
}
