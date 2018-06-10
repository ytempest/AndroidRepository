package com.ytempest.architectureanalysis.sample2.mvp;

import com.ytempest.architectureanalysis.retrofit.UserInfoResult;
import com.ytempest.architectureanalysis.sample2.base.BasePresenter;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * @author ytempest
 *         Description：
 */
public class UserInfoPresenter extends BasePresenter<UserInfoContract.UserInfoView, UserInfoModel>
        implements UserInfoContract.UserInfoPresenter {


    @Override
    public void getUser(String name, String pwd) {

        getView().onLoading();

        getModel().getUser(name, pwd).subscribe(new Observer<UserInfoResult>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(UserInfoResult userInfoResult) {
                getView().onSucceed(userInfoResult);
            }

            @Override
            public void onError(Throwable e) {
                getView().onError(e);

            }

            @Override
            public void onComplete() {

            }
        });
    }
}
