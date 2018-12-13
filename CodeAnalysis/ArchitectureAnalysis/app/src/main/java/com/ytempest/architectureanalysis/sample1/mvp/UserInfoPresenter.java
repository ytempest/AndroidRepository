package com.ytempest.architectureanalysis.sample1.mvp;

import com.ytempest.architectureanalysis.retrofit.UserInfoResult;
import com.ytempest.architectureanalysis.sample1.base.BasePresenter;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * @author ytempest
 *         Description：解耦关联层，负责根据事件类型回调 M层和 V层的接口，实现事件的处理；如：
 *         根据用户名和密码，实现将该用户的信息显示到界面上
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
