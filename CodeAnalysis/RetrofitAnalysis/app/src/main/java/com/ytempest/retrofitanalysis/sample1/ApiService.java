package com.ytempest.retrofitanalysis.sample1;

import com.ytempest.retrofitanalysis.sample1.deal.Result;
import com.ytempest.retrofitanalysis.sample1.deal.UserInfoResult;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;


/**
 * @author ytempest
 *         Description：
 */
public interface ApiService {

    /**
     * 通过这个方法获取能进行登录的Call对象
     */
    @FormUrlEncoded
    @POST("OkHttpServlet/login")
    Call<Result<UserInfoResult>> getUserInfo(
            @Field("user") String userName,
            @Field("pwd") String userPassword
    );
}
