package com.ytempest.architectureanalysis.sample4.mvp;

import com.ytempest.architectureanalysis.retrofit.UserInfoResult;
import com.ytempest.architectureanalysis.sample4.base.BaseModel;
import com.ytempest.architectureanalysis.sample4.base.BaseView;

import io.reactivex.Observable;

/**
 * @author ytempest
 *         Description：这是一个MVP的协议类，在多人开发的时候最好先定义一个协议类，这个就可以规范
 *         每一个层次（M、V、P）的功能
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
     * Description：这个接口定义了 P层的功能逻辑，用于协调 V层和 M层的逻辑
     */
    interface UserInfoPresenter {
        void getUser(String name, String pwd);
    }

    /**
     * Description：这个接口定义了 M层的功能逻辑，主要用于获取数据，也就是说数据的获取都会在这个接口中
     */
    interface UserInfoModel extends BaseModel {
        Observable<UserInfoResult> getUser(
                String name, String pwd);
    }
}
