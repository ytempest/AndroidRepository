package com.ytempest.retrofitanalysis.sample4;

import com.ytempest.retrofitanalysis.sample4.deal.Result;
import com.ytempest.retrofitanalysis.sample4.deal.UserInfoResult;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;


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


    @GET("/")
    Call<DayDayResult> getDayDayResult(
            @Query("m") String m,
            @Query("c") String c,
            @Query("a") String a,
            @Query("appid") String appid,
            @Query("uid") String uid

    );

}
