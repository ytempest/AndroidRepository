package com.ytempest.retrofitanalysis.simple5;


import com.ytempest.retrofitanalysis.simple5.retrofit.ErrorHandle;

import io.reactivex.Observer;

/**
 * Created by hcDarren on 2017/12/23.
 */

public abstract class BaseObserver<T> implements Observer<T> {

    @Override
    public void onComplete() {

    }

    @Override
    public void onError(Throwable e) {
        // e ：网络异常，解析异常，结果处理过程中异常
        e.printStackTrace();
        if(e instanceof ErrorHandle.ServiceError){
            ErrorHandle.ServiceError serviceError = (ErrorHandle.ServiceError) e;
            onError("",serviceError.getMessage());
        }else {
            // 自己处理
            onError("","未知异常");
        }
    }

    protected abstract void onError(String errorCode, String errorMessage);
}
