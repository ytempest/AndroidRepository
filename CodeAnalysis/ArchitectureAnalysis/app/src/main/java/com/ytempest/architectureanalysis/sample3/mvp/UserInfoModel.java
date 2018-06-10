package com.ytempest.architectureanalysis.sample3.mvp;

import com.ytempest.architectureanalysis.retrofit.RetrofitClient;
import com.ytempest.architectureanalysis.retrofit.UserInfoResult;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @author ytempest
 *         Description：
 */
public class UserInfoModel implements UserInfoContract.UserInfoModel {
    @Override
    public Observable<UserInfoResult> getUser(String name, String pwd) {
        return RetrofitClient.getApiService().getUserInfo(name, pwd)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
