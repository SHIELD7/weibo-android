package site.imcu.weibo.net;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

public interface CommentService {
    @FormUrlEncoded
    @Headers("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    @POST("/comment")
    Observable<JSONObject> postComment(@HeaderMap Map<String,String> headers, @Field("commentContent")String commentContent,@Field("weiboId")int weiboId);

    @GET("/comment")
    Observable<JSONObject> queryComment(@HeaderMap Map<String ,String> headers, @Query("weiboId")int weiboId);
}
