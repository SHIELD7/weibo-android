package site.imcu.weibo.net;

import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;
import rx.Observable;

public interface WeiboService {
    @GET("weibo/timeline")
    Observable<JSONObject> queryTimeline(@HeaderMap Map<String, String> headers, @Query("pageNum")int pageNum,@Query("pageSize")int pageSize);

    @POST("weibo/post")
    @Multipart
    Observable<JSONObject> postWeibo(@HeaderMap Map<String,String> headers,@PartMap Map<String, RequestBody> requestBodyMap);

    @Multipart
    @POST("weibo/img")
    Observable<JSONObject> uploadImg(@HeaderMap Map<String,String> headers,@Part MultipartBody.Part part);

    @GET("weibo/android")
    Observable<JSONObject> loadWeibo(@HeaderMap Map<String,String> headers,@Query("mark")int mark,@Query("afterOrBefore")int afterOrBefore,@Query("pageNum")int pageNum,@Query("pageSize")int pageSize);

    @GET("weibo/all")
    Observable<JSONObject> queryAll(@HeaderMap Map<String, String> headers);

    @GET("weibo/someone")
    Observable<JSONObject> querySomeone(@HeaderMap Map<String, String> headers,@Query("userId")int userId, @Query("pageNum")int pageNum,@Query("pageSize")int pageSize);

    @GET("weibo/one")
    Observable<JSONObject> queryOne(@HeaderMap Map<String, String> headers,@Query("weiboId")int weiboId);
}
