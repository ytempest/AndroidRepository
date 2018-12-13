package com.ytempest.architectureanalysis.sample1.mvp;

import com.ytempest.architectureanalysis.retrofit.UserInfoResult;
import com.ytempest.architectureanalysis.sample1.base.BaseView;

import io.reactivex.Observable;

/**
 * @author ytempest
 *         Description：这是一个MVP的协议类，在多人开发的时候最好先定义一个协议类，这样在开发一个
 *         层次（如M）的工作时就不会另一个层次（如V）
 */
public interface UserInfoContract {
    /**
     * Description：这个接口定义 V层的功能逻辑，主要用于呈现数据
     */
    interface UserInfoView extends BaseView {
        void onLoading();

        void onError(Throwable throwable);

        void onSucceed(UserInfoResult result);
    }

    /**
     * Description：这个接口定义了 P层的功能逻辑，用于协调 V层和 M层的逻辑，事件最初由P层发出，然后
     * 通过回调 V层和 M层的方法将获取的数据显示到界面上
     */
    interface UserInfoPresenter {
        void getUser(String name, String pwd);
    }

    /**
     * Description：这个接口定义了 M层的功能逻辑，主要用于获取数据，如：发出请求向服务器请求数据、从
     * 数据库中查询数据等，也就是数据的获取都会在这个接口中
     */
    interface UserInfoModel {
        Observable<UserInfoResult> getUser(
                String name, String pwd);
    }
}
