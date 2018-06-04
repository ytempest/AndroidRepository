package com.ytempest.retrofitanalysis.simple5.retrofit;





import com.ytempest.retrofitanalysis.simple5.deal.Result;
import com.ytempest.retrofitanalysis.simple5.deal.UserInfoResult;


import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;


/**
 * Created by hcDarren on 2017/12/16.
 * 请求后台访问数据的 接口类
 */
public interface ServiceApi {
    // 接口涉及到解耦，userLogin 方法是没有任何实现代码的
    // 如果有一天要换 GoogleHttp

    @POST("OkHttpServlet/login")
    @FormUrlEncoded
    Observable<Result<UserInfoResult>> userLogin(
            // @Query(后台需要解析的字段)
            @Field("user") String userName,
            @Field("pwd") String userPwd);


    // 上传文件怎么用？
}
