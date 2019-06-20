package site.imcu.weibo.net;
import com.alibaba.fastjson.JSONObject;


import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import rx.Observable;

public interface UserService {
    @FormUrlEncoded
    @POST("users/login")
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    Observable<JSONObject> login(@Field("username")String username, @Field("password")String password);

    @FormUrlEncoded
    @POST("users/register")
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    Observable<JSONObject> register(@Field("newUsername")String newUsername,@Field("newPassword")String newPassword);

    @GET("users/profile")
    Observable<JSONObject> profile(@HeaderMap Map<String,String> headers,@Query("profileId") int profileId);

    @POST("users/changeAvatar")
    @Multipart
    Observable<JSONObject> changeAvatar(@HeaderMap Map<String,String> headers,@Part MultipartBody.Part part);

}
