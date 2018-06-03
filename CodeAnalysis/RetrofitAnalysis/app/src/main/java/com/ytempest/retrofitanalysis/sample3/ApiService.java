package com.ytempest.retrofitanalysis.sample3;

import com.ytempest.retrofitanalysis.sample3.deal.Result;
import com.ytempest.retrofitanalysis.sample3.deal.UserInfoResult;
import com.ytempest.retrofitanalysis.sample3.imitate.Call;
import com.ytempest.retrofitanalysis.sample3.imitate.htpp.Field;
import com.ytempest.retrofitanalysis.sample3.imitate.htpp.POST;


/**
 * @author ytempest
 *         Description：
 */
public interface ApiService {

    /**
     * 通过这个方法获取能进行登录的Call对象
     */
    @POST("OkHttpServlet/login")
    Call<Result<UserInfoResult>> getUserInfo(
            @Field("user") String userName,
            @Field("pwd") String userPassword
    );
}
