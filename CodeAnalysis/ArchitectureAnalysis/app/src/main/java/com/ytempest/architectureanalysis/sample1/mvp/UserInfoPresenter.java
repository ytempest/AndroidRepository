package com.ytempest.architectureanalysis.sample1.mvp;

import com.ytempest.architectureanalysis.retrofit.UserInfoResult;
import com.ytempest.architectureanalysis.sample1.base.BasePresenter;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * @author ytempest
 *         Description：
 */
public class UserInfoPresenter extends BasePresenter<UserInfoContract.UserInfoView>
        implements UserInfoContract.UserInfoPresenter {

    private UserInfoContract.UserInfoModel mModel;

    public UserInfoPresenter() {
        mModel = new UserInfoModel();
    }


    @Override
    public void getUser(String name, String pwd) {

        getView().onLoading();

        mModel.getUser(name, pwd).subscribe(new Observer<UserInfoResult>() {
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
