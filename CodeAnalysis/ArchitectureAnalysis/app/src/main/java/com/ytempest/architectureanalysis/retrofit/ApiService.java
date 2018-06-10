package com.ytempest.architectureanalysis.retrofit;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * @author ytempest
 *         Description：
 */
public interface ApiService {

    @FormUrlEncoded
    @POST("OkHttpServlet/login")
    Observable<UserInfoResult> getUserInfo(
            @Field("user") String userName,
            @Field("pwd") String userPassword);
}
